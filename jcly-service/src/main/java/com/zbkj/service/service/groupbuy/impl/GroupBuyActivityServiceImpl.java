package com.zbkj.service.service.groupbuy.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.DateConstants;
import com.zbkj.common.constants.ProductConstants;
import com.zbkj.common.enums.GroupBuyGroupStatusEnum;
import com.zbkj.common.enums.RoleEnum;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.groupbuy.GroupBuyActivity;
import com.zbkj.common.model.groupbuy.GroupBuyActivitySku;
import com.zbkj.common.model.groupbuy.GroupBuyUser;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductCategory;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.groupbuy.GroupBuyActivityRequest;
import com.zbkj.common.request.groupbuy.GroupBuyActivitySearchRequest;
import com.zbkj.common.request.groupbuy.GroupBuyActivitySkuRequest;
import com.zbkj.common.request.groupbuy.GroupBuyProductCategorySearchRequest;
import com.zbkj.common.request.groupbuy.PatGroupBuyActivitySearchRequest;
import com.zbkj.common.response.AttrValueResponse;
import com.zbkj.common.response.ProductInfoResponse;
import com.zbkj.common.response.groupbuy.*;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.ProCategoryCacheVo;
import com.zbkj.service.dao.groupby.GroupBuyActivityDao;
import com.zbkj.service.service.MerchantService;
import com.zbkj.service.service.ProductAttrValueService;
import com.zbkj.service.service.ProductCategoryService;
import com.zbkj.service.service.ProductService;
import com.zbkj.service.service.groupbuy.GroupBuyActivityService;
import com.zbkj.service.service.groupbuy.GroupBuyActivitySkuService;
import com.zbkj.service.service.groupbuy.GroupBuyUserService;
import com.zbkj.service.service.KnowledgeMarkdownService;
import com.zbkj.service.service.SystemConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dazongzi
 * @description GroupBuyActivityServiceImpl 接口实现
 * @date 2024-08-13
 */
@Service
public class GroupBuyActivityServiceImpl extends ServiceImpl<GroupBuyActivityDao, GroupBuyActivity> implements GroupBuyActivityService {

    @Resource
    private GroupBuyActivityDao dao;

    @Resource
    private GroupBuyActivitySkuService groupBuyActivitySkuService;

    @Resource
    private ProductService productService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private GroupBuyUserService groupBuyUserService;

    @Resource
    private ProductCategoryService productCategoryService;

    @Resource
    private KnowledgeMarkdownService knowledgeMarkdownService;

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 此方法商户和平台公用，用平台的参数包含了商户的参数，在controller 层面做了参数区分
     * 列表
     *
     * @param request          请求参数
     * @param pageParamRequest 分页类参数
     * @return List<GroupBuyActivity>
     * @author dazongzi
     * @since 2024-08-13
     */
    @Override
    public PageInfo<GroupBuyActivityResponse> getList(PatGroupBuyActivitySearchRequest request, PageParamRequest pageParamRequest) {

        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        //带 GroupBuyActivity 类的多条件查询
        LambdaQueryWrapper<GroupBuyActivity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(GroupBuyActivity::getIsDel, 0);
        // 根据权限判断查询自己所属商户的活动拼团列表
        if (admin.getType().equals(RoleEnum.SUPER_MERCHANT.getValue()) || admin.getType().equals(RoleEnum.MERCHANT_ADMIN.getValue())) {
            lambdaQueryWrapper.eq(GroupBuyActivity::getMerId, admin.getMerId());
        }
        if(StringUtils.isNotBlank(request.getGroupName())){
            if (ObjectUtil.isNotEmpty(request.getGroupName().trim())) {
                lambdaQueryWrapper.like(GroupBuyActivity::getGroupName, URLUtil.decode(request.getGroupName().trim()));
            }
        }
        // 活动进程 活动进程  0=未开始 1=进行中 2=已结束
        if (ObjectUtil.isNotEmpty(request.getGroupProcess())) {
            switch (request.getGroupProcess()) {
                case 0:
                    lambdaQueryWrapper.ge(GroupBuyActivity::getStartTime, CrmebDateUtil.nowDateTime());
                    break;
                case 1:
                    lambdaQueryWrapper.le(GroupBuyActivity::getStartTime, CrmebDateUtil.nowDateTime())
                            .gt(GroupBuyActivity::getEndTime, CrmebDateUtil.nowDateTime());
                    break;
                case 2:
                    lambdaQueryWrapper.lt(GroupBuyActivity::getEndTime, CrmebDateUtil.nowDateTime());
                    break;
            }
        }

        // 活动日期
        if (ObjectUtil.isNotEmpty(request.getStartTime())) {
            Date date = CrmebDateUtil.strToDate(request.getStartTime(), DateConstants.DATE_FORMAT_DATE);
            String startTime = CrmebDateUtil.dateToStr(date, DateConstants.DATE_FORMAT_START);
            String endTime = CrmebDateUtil.dateToStr(date, DateConstants.DATE_FORMAT_END);
            lambdaQueryWrapper.le(GroupBuyActivity::getStartTime, startTime)
                    .ge(GroupBuyActivity::getEndTime, endTime);
        }

        // 活动状态 开启或者关闭
        if (ObjectUtil.isNotEmpty(request.getGroupStatus())) {
            lambdaQueryWrapper.eq(GroupBuyActivity::getGroupStatus, request.getGroupStatus());
        }

        // 给商户查询做准备
        // 商户分类 根据商户分类找到对应商户再触发商户查询
        if (ObjectUtil.isNotEmpty(request.getCategoryId())) {
            List<Merchant> merchantList = merchantService.getMerchantListByType(request.getCategoryId());
            List<Integer> merIds = new ArrayList<>();
            if (merchantList.isEmpty()) {
                merIds.add(0);
            } else {
                merIds = merchantList.stream().map(Merchant::getId).collect(Collectors.toList());
            }
            lambdaQueryWrapper.in(GroupBuyActivity::getMerId, merIds);
        }

        // 指定商户查询
        if (StrUtil.isNotBlank(request.getMerName())) {
            lambdaQueryWrapper.like(GroupBuyActivity::getMerName, URLUtil.decode(request.getMerName()));
        }

        // 活动状态 关闭/开启
        if (ObjectUtil.isNotEmpty(request.getActivityStatus())) {
            lambdaQueryWrapper.eq(GroupBuyActivity::getActivityStatus, request.getActivityStatus());
        }

        lambdaQueryWrapper.orderByDesc(GroupBuyActivity::getCreateTime);
        Page<GroupBuyActivity> activityGroupPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<GroupBuyActivity> groupBuyActivities = dao.selectList(lambdaQueryWrapper);

        List<GroupBuyActivityResponse> groupBuyActivityResponses = new ArrayList<>();
        groupBuyActivities.stream().map(groupBuyActivity -> {
            GroupBuyActivityResponse groupBuyActivityResponse = new GroupBuyActivityResponse();
            BeanUtils.copyProperties(groupBuyActivity, groupBuyActivityResponse);
            //获取拼团商品
            List<GroupBuyActivitySku> groupBuyActivitySkuList = groupBuyActivitySkuService.getListByGroupActivityId(groupBuyActivity.getId());
            if (!groupBuyActivitySkuList.isEmpty()) {
                Map<Integer, List<GroupBuyActivitySku>> skuListGroupByProductIdMap =
                        groupBuyActivitySkuList.stream().collect(Collectors.groupingBy(GroupBuyActivitySku::getProductId));
                
                List<GroupBuyActivityProductResponse> groupBuyActivityProductResponseList = new ArrayList<>();
                for (Map.Entry<Integer, List<GroupBuyActivitySku>> productIdEntry : skuListGroupByProductIdMap.entrySet()) {
                    Integer productId = productIdEntry.getKey();
                    List<GroupBuyActivitySku> groupBuyActivitySkuListByProductId = productIdEntry.getValue();
                    Product product = productService.getById(productId);
                    if (product != null && !product.getIsDel()) {
                        GroupBuyActivityProductResponse productResponse = new GroupBuyActivityProductResponse();
                        productResponse.setProductId(productId);
                        productResponse.setProductName(product.getName());
                        productResponse.setImage(product.getImage());
                        
                        List<GroupBuyActivitySkuResponse> groupBuyActivitySkuResponseList = new ArrayList<>();
                        for (GroupBuyActivitySku sku : groupBuyActivitySkuListByProductId) {
                            GroupBuyActivitySkuResponse skuResponse = new GroupBuyActivitySkuResponse();
                            BeanUtils.copyProperties(sku, skuResponse);
                            skuResponse.setProductName(product.getName());
                            groupBuyActivitySkuResponseList.add(skuResponse);
                        }
                        productResponse.setGroupBuyActivitySkuResponses(groupBuyActivitySkuResponseList);
                        groupBuyActivityProductResponseList.add(productResponse);
                    }
                }
                groupBuyActivityResponse.setGroupBuyActivityProductResponseList(groupBuyActivityProductResponseList);
            }
            groupBuyActivityResponses.add(groupBuyActivityResponse);
            return null;
        }).collect(Collectors.toList());
        return CommonPage.copyPageInfo(activityGroupPage, groupBuyActivityResponses);
    }

    /**
     * 获取拼团头部 数据
     *
     * @return 对应状态的数量
     */
    @Override
    public List<GroupBuyActivityListHeaderCount> getListHeaderCount(GroupBuyActivitySearchRequest request, SystemAdmin systemAdmin) {
        // 0=初始化 1=已拒绝 2=已撤销 3=待审核 4=已通过
        List<GroupBuyActivityListHeaderCount> headerCountList = new ArrayList<>();
        if (systemAdmin.getMerId() == 0) {
            GroupBuyActivityListHeaderCount headerCount1 = new GroupBuyActivityListHeaderCount(1, 0);
            GroupBuyActivityListHeaderCount headerCount3 = new GroupBuyActivityListHeaderCount(3, 0);
            GroupBuyActivityListHeaderCount headerCount4 = new GroupBuyActivityListHeaderCount(4, 0);
            headerCountList.add(headerCount1);
            headerCountList.add(headerCount3);
            headerCountList.add(headerCount4);
        } else {
            GroupBuyActivityListHeaderCount headerCount1 = new GroupBuyActivityListHeaderCount(1, 0);
            GroupBuyActivityListHeaderCount headerCount2 = new GroupBuyActivityListHeaderCount(2, 0);
            GroupBuyActivityListHeaderCount headerCount3 = new GroupBuyActivityListHeaderCount(3, 0);
            GroupBuyActivityListHeaderCount headerCount4 = new GroupBuyActivityListHeaderCount(4, 0);
            headerCountList.add(headerCount1);
            headerCountList.add(headerCount2);
            headerCountList.add(headerCount3);
            headerCountList.add(headerCount4);
        }
        LambdaQueryWrapper<GroupBuyActivity> queryWrapper = Wrappers.lambdaQuery();
        for (GroupBuyActivityListHeaderCount headerCount : headerCountList) {
            queryWrapper.clear();
            queryWrapper.eq(GroupBuyActivity::getIsDel, 0);
            // 指定商户查询
            if (StrUtil.isNotBlank(request.getMerName())) {
                queryWrapper.like(GroupBuyActivity::getMerName, URLUtil.decode(request.getMerName()));
            }
            if (StrUtil.isNotBlank(request.getGroupName().trim())) {
                String groupNameDecode = URLUtil.decode(request.getGroupName().trim());
                queryWrapper.like(GroupBuyActivity::getGroupName, groupNameDecode);
            }
            // 活动进程 活动进程  0=未开始 1=进行中 2=已结束
            if (ObjectUtil.isNotEmpty(request.getGroupProcess())) {
                switch (request.getGroupProcess()) {
                    case 0:
                        queryWrapper.ge(GroupBuyActivity::getStartTime, CrmebDateUtil.nowDateTime());
                        break;
                    case 1:
                        queryWrapper.le(GroupBuyActivity::getStartTime, CrmebDateUtil.nowDateTime())
                                .gt(GroupBuyActivity::getEndTime, CrmebDateUtil.nowDateTime());
                        break;
                    case 2:
                        queryWrapper.lt(GroupBuyActivity::getEndTime, CrmebDateUtil.nowDateTime());
                        break;
                }
            }
            // 活动日期
            if (ObjectUtil.isNotEmpty(request.getStartTime())) {
                Date date = CrmebDateUtil.strToDate(request.getStartTime(), DateConstants.DATE_FORMAT_DATE);
                String startTime = CrmebDateUtil.dateToStr(date, DateConstants.DATE_FORMAT_START);
                String endTime = CrmebDateUtil.dateToStr(date, DateConstants.DATE_FORMAT_END);
                queryWrapper.le(GroupBuyActivity::getStartTime, startTime)
                        .ge(GroupBuyActivity::getEndTime, endTime);
            }
            // 商户分类 根据商户分类找到对应商户再触发商户查询
            if (ObjectUtil.isNotEmpty(request.getCategoryId())) {
                List<Merchant> merchantList = merchantService.getMerchantListByType(request.getCategoryId());
                List<Integer> merIdList = new ArrayList<>();
                if (merchantList.isEmpty()) {
                    merIdList.add(0);
                } else {
                    merIdList = merchantList.stream().map(Merchant::getId).collect(Collectors.toList());
                }
                queryWrapper.in(GroupBuyActivity::getMerId, merIdList);
            }
            // 活动状态 关闭/开启
            if (ObjectUtil.isNotEmpty(request.getActivityStatus())) {
                queryWrapper.eq(GroupBuyActivity::getActivityStatus, request.getActivityStatus());
            }
            // 根据权限判断商户还是平台
            if (systemAdmin.getType().equals(RoleEnum.MERCHANT_ADMIN.getValue()) ||
                    systemAdmin.getType().equals(RoleEnum.SUPER_MERCHANT.getValue())) {
                queryWrapper.eq(GroupBuyActivity::getMerId, systemAdmin.getMerId());
            }
            // 控制状态
            queryWrapper.eq(GroupBuyActivity::getGroupStatus, headerCount.getGroupStatus());
            headerCount.setCount(dao.selectCount(queryWrapper));
        }

        return headerCountList;
    }

    /**
     * 新增拼团
     *
     * @param request 拼团原始对象
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean addGroupBuyActivity(GroupBuyActivityRequest request) {
        // 获取拼团基础活动数据
        GroupBuyActivity groupBuyActivity = new GroupBuyActivity();
        BeanUtils.copyProperties(request, groupBuyActivity);
        // 获取拼团商品数据
        List<GroupBuyActivitySku> groupBuySkuList = new ArrayList<>();
        List<GroupBuyActivitySkuRequest> groupBuySkuRequestList = request.getGroupBuySkuRequest();
        for (GroupBuyActivitySkuRequest groupSkuRequest : groupBuySkuRequestList) {
            GroupBuyActivitySku groupSku = new GroupBuyActivitySku();
            BeanUtils.copyProperties(groupSkuRequest, groupSku);
            groupSku.setQuota(groupSkuRequest.getQuotaShow());
            groupBuySkuList.add(groupSku);
        }
        // 安全校验 拼团商品售卖的库存不得大于原始商品库存
        validGroupProduct(groupBuySkuList);

        // 设置当前拼团活动状态 有初始化状态 但是这里默认指定为 待审核
        groupBuyActivity.setGroupStatus(GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_AUDIT.getCode());
        // 设置当前登录的商户
        SystemAdmin currentMerchantAdmin = SecurityUtil.getLoginUserVo().getUser();
        Merchant currentMerchant = merchantService.getByIdException(currentMerchantAdmin.getMerId());
        groupBuyActivity.setMerId(currentMerchantAdmin.getMerId());
        groupBuyActivity.setMerName(currentMerchant.getName());

        // 保存到拼团活动表和对应sku
        dao.insert(groupBuyActivity);
        groupBuySkuList = groupBuySkuList.stream().map(sku -> sku.setGroupActivityId(groupBuyActivity.getId())).collect(Collectors.toList());
        groupBuyActivitySkuService.saveBatch(groupBuySkuList);
        return Boolean.TRUE;
    }


    /**
     * 修改拼团
     *
     * @param request 拼团待修改对象
     * @return Boolean
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean updateGroupBuyActivity(GroupBuyActivityRequest request) {
        // 获取拼团基础活动数据
        GroupBuyActivity groupBuyActivity = new GroupBuyActivity();
        BeanUtils.copyProperties(request, groupBuyActivity);
        groupBuyActivity.setGroupName(request.getGroupName());

        // 获取拼团商品数据
        List<GroupBuyActivitySku> groupBuySkuList = new ArrayList<>();
        List<GroupBuyActivitySkuRequest> groupBuySkuRequestList = request.getGroupBuySkuRequest();
        for (GroupBuyActivitySkuRequest groupSkuRequest : groupBuySkuRequestList) {
            GroupBuyActivitySku groupSku = new GroupBuyActivitySku();
            BeanUtils.copyProperties(groupSkuRequest, groupSku);
            groupSku.setQuota(groupSkuRequest.getQuotaShow());
            groupBuySkuList.add(groupSku);
        }

        // 安全校验 拼团商品售卖的库存不得大于原始商品库存
        validGroupProduct(groupBuySkuList);

        // 设置当前拼团活动状态 修改后默认为待审核状态
        groupBuyActivity.setGroupStatus(GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_AUDIT.getCode());
        // 保存到拼团活动表和对应sku
        groupBuyActivity.setUpdateTime(DateUtil.date());
        dao.updateById(groupBuyActivity);
        // 删除历史SKU 数据后添加新的数据 不更新
        List<GroupBuyActivitySku> listByGroupActivityId = groupBuyActivitySkuService.getListByGroupActivityId(request.getId());
        if (ObjectUtil.isNotEmpty(listByGroupActivityId)) {
            List<Integer> skuIds = listByGroupActivityId.stream().map(GroupBuyActivitySku::getId).collect(Collectors.toList());
            groupBuyActivitySkuService.removeByIds(skuIds);
        }
        groupBuyActivitySkuService.saveBatch(groupBuySkuList);
        return Boolean.TRUE;
    }

    /**
     * 获取拼团详情
     *
     * @param id 拼团活动id
     * @return 拼团活动详情
     */
    @Override
    public GroupBuyActivityResponse getGroupBuyActivity(Integer id) {
        GroupBuyActivity groupBuyActivity = dao.selectById(id);
        if (ObjectUtil.isEmpty(groupBuyActivity)) {
            return null;
        }
        List<GroupBuyActivityProductResponse> groupBuyActivityProductResponseList = new ArrayList<>();
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();

        // set 拼团活动数据
        GroupBuyActivityResponse groupBuyActivityResponse = new GroupBuyActivityResponse();
        BeanUtils.copyProperties(groupBuyActivity, groupBuyActivityResponse);

        // 获取当前拼团下所有的sku列表
        List<GroupBuyActivitySku> groupBuyActivitySkuList = groupBuyActivitySkuService.getListByGroupActivityId(id);

        Map<Integer, List<GroupBuyActivitySku>> skuListGroupByProductIdMap =
                groupBuyActivitySkuList.stream().collect(Collectors.groupingBy(GroupBuyActivitySku::getProductId));

        for (Map.Entry<Integer, List<GroupBuyActivitySku>> productIdEntry : skuListGroupByProductIdMap.entrySet()) {
            Integer productId = productIdEntry.getKey();
            List<GroupBuyActivitySku> groupBuyActivitySkuListByProductId = productIdEntry.getValue();
            ProductInfoResponse productInfoResponse = productService.getInfo(productId, systemAdmin);
            if (productInfoResponse.getAuditStatus().equals(ProductConstants.AUDIT_STATUS_WAIT) ||
                    productInfoResponse.getAuditStatus().equals(ProductConstants.AUDIT_STATUS_FAIL)) continue;
            List<GroupBuyActivitySkuResponse> groupBuyActivitySkuResponseList = new ArrayList<>();

            for (GroupBuyActivitySku groupBuyActivitySku : groupBuyActivitySkuListByProductId) {

                // 确定是否有相同的商品id
                GroupBuyActivitySkuResponse skuResponse = new GroupBuyActivitySkuResponse();

                skuResponse.setId(groupBuyActivitySku.getId());
                skuResponse.setGroupActivityId(groupBuyActivitySku.getGroupActivityId());
                skuResponse.setProductId(productId);
                skuResponse.setActivePrice(groupBuyActivitySku.getActivePrice());
                skuResponse.setQuotaShow(groupBuyActivitySku.getQuotaShow());
                skuResponse.setQuota(groupBuyActivitySku.getQuota());
                skuResponse.setProductName(productInfoResponse.getName());
                skuResponse.setSkuId(groupBuyActivitySku.getSkuId());

                // 找到商品匹配的SKU 数据
                List<AttrValueResponse> seamSkuList =
                        productInfoResponse.getAttrValueList().stream().filter(attrValue -> attrValue.getId().equals(groupBuyActivitySku.getSkuId())).collect(Collectors.toList());
                if (!seamSkuList.isEmpty()) {
                    skuResponse.setAttrValue(seamSkuList);
                    groupBuyActivitySkuResponseList.add(skuResponse);
                }
            }

            GroupBuyActivityProductResponse groupBuyActivityProductResponse = new GroupBuyActivityProductResponse();
            groupBuyActivityProductResponse.setProductId(productId);
            groupBuyActivityProductResponse.setProductName(productInfoResponse.getName());
            groupBuyActivityProductResponse.setImage(productInfoResponse.getImage());
            groupBuyActivityProductResponse.setGroupBuyActivitySkuResponses(groupBuyActivitySkuResponseList);
            groupBuyActivityProductResponseList.add(groupBuyActivityProductResponse);
        }

        groupBuyActivityResponse.setGroupBuyActivityProductResponseList(groupBuyActivityProductResponseList);

        return groupBuyActivityResponse;
    }

    /**
     * 拼团活动详情 针对移动端
     *
     * @param id 拼团活动id
     * @return 拼团活动详情
     */
    @Override
    public GroupBuyActivityResponse getGroupBuyActivityForFront(Integer id) {
        GroupBuyActivity groupBuyActivity = dao.selectById(id);
        if (ObjectUtil.isEmpty(groupBuyActivity)) {
            return null;
        }
        List<GroupBuyActivityProductResponse> groupBuyActivityProductResponseList = new ArrayList<>();

        // set 拼团活动数据
        GroupBuyActivityResponse groupBuyActivityResponse = new GroupBuyActivityResponse();
        BeanUtils.copyProperties(groupBuyActivity, groupBuyActivityResponse);

        // 获取当前拼团下所有的sku列表
        List<GroupBuyActivitySku> groupBuyActivitySkuList = groupBuyActivitySkuService.getListByGroupActivityId(id);

        Map<Integer, List<GroupBuyActivitySku>> skuListGroupByProductIdMap =
                groupBuyActivitySkuList.stream().collect(Collectors.groupingBy(GroupBuyActivitySku::getProductId));

        for (Map.Entry<Integer, List<GroupBuyActivitySku>> productIdEntry : skuListGroupByProductIdMap.entrySet()) {
            Integer productId = productIdEntry.getKey();
            List<GroupBuyActivitySku> groupBuyActivitySkuListByProductId = productIdEntry.getValue();
            List<GroupBuyActivitySkuResponse> groupBuyActivitySkuResponseList = new ArrayList<>();

            for (GroupBuyActivitySku groupBuyActivitySku : groupBuyActivitySkuListByProductId) {

                GroupBuyActivitySkuResponse skuResponse = new GroupBuyActivitySkuResponse();

                skuResponse.setId(groupBuyActivitySku.getId());
                skuResponse.setGroupActivityId(groupBuyActivitySku.getGroupActivityId());
                skuResponse.setProductId(productId);
                skuResponse.setActivePrice(groupBuyActivitySku.getActivePrice());
                skuResponse.setQuotaShow(groupBuyActivitySku.getQuotaShow());
                skuResponse.setQuota(groupBuyActivitySku.getQuota());
                skuResponse.setSkuId(groupBuyActivitySku.getSkuId());

                groupBuyActivitySkuResponseList.add(skuResponse);
            }

            GroupBuyActivityProductResponse groupBuyActivityProductResponse = new GroupBuyActivityProductResponse();
            groupBuyActivityProductResponse.setProductId(productId);

            groupBuyActivityProductResponse.setGroupBuyActivitySkuResponses(groupBuyActivitySkuResponseList);
            groupBuyActivityProductResponseList.add(groupBuyActivityProductResponse);
        }

        groupBuyActivityResponse.setGroupBuyActivityProductResponseList(groupBuyActivityProductResponseList);

        return groupBuyActivityResponse;
    }

    /**
     * 拼团活动状态修改
     *
     * @param groupBuyActivityId 拼团活动id
     * @param status             活动状态
     * @return Boolean
     */
    @Override
    public Boolean groupBuyActivityStatusOnOrOff(Integer groupBuyActivityId, Integer status) {
        GroupBuyActivity groupBuyActivity = dao.selectById(groupBuyActivityId);
        if (ObjectUtil.isNull(groupBuyActivity)) {
            throw new CrmebException("活动不存在");
        }
        groupBuyActivity.setActivityStatus(status);
        groupBuyActivity.setUpdateTime(DateUtil.date());
        return dao.updateById(groupBuyActivity) > 0;
    }

    /**
     * 拼团状态审核
     *
     * @param groupBuyActivityId 拼团活动id
     * @param groupStatus        拼团状态枚举值
     * @param reason             审核决绝原因
     * @return Boolean 结果
     */
    @Override
    public Boolean groupBuyGroupStatusProgress(Integer groupBuyActivityId, Integer groupStatus, String reason) {
        if (ObjectUtil.isNotNull(groupBuyActivityId) && ObjectUtil.isNotNull(groupStatus)) {
            GroupBuyActivity groupBuyActivity = dao.selectById(groupBuyActivityId);
            if (ObjectUtil.isNull(groupBuyActivity)) {
                throw new CrmebException("活动不存在");
            }
            // 如果是拒绝 则需要填写拒绝理由
            if (groupStatus.equals(GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_REFUSE.getCode())
                    && ObjectUtil.isEmpty(reason)) {
                throw new CrmebException("请填写拒绝理由");
            } else {
                groupBuyActivity.setRefusal(reason);
            }
            groupBuyActivity.setGroupStatus(groupStatus);

            // 设置在团购商品中对应的SKU 状态
            boolean status = groupStatus.equals(GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_PASS.getCode());
            groupBuyActivitySkuService.reviewGroupBuySkuStatus(groupBuyActivityId, status);
            groupBuyActivity.setUpdateTime(DateUtil.date());
            boolean updateResult = dao.updateById(groupBuyActivity) > 0;
            
            // 如果审核通过，异步上传到Coze知识库
            if (updateResult && status) {
                try {
                    String knowledgeId = systemConfigService.getValueByKey("coze_knowledge_id");
                    if (StrUtil.isNotBlank(knowledgeId)) {
                        // 异步执行，避免影响主流程
                        new Thread(() -> {
                            try {
                                knowledgeMarkdownService.generateAndUploadGroupBuyMarkdown(
                                    groupBuyActivityId, knowledgeId, groupBuyActivity.getMerId());
                                // 使用log4j2的logger
                                System.out.println("拼团活动知识库上传成功，活动ID: " + groupBuyActivityId);
                            } catch (Exception ex) {
                                System.err.println("拼团活动知识库上传失败，活动ID: " + groupBuyActivityId + ", 错误: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }).start();
                    } else {
                        System.out.println("未配置Coze知识库ID，跳过拼团活动知识库上传");
                    }
                } catch (Exception ex) {
                    System.err.println("启动拼团活动知识库上传任务失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            
            return updateResult;
        }
        return Boolean.FALSE;
    }

    /**
     * 平台强制关闭
     *
     * @param groupBuyActivityId 拼团活动id
     * @param reason             关闭原因
     * @return 关闭结果
     */
    @Override
    public Boolean groupBuyGroupStatusProgressClose(Integer groupBuyActivityId, String reason) {
        if (ObjectUtil.isNotNull(groupBuyActivityId)) {
            GroupBuyActivity groupBuyActivity = dao.selectById(groupBuyActivityId);
            if (ObjectUtil.isNull(groupBuyActivity)) {
                throw new CrmebException("活动不存在");
            }
            groupBuyActivity.setGroupStatus(GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_REFUSE.getCode());
            if (ObjectUtil.isNotEmpty(reason)) {
                groupBuyActivity.setRefusal(reason);
            }
            groupBuyActivity.setUpdateTime(DateUtil.date());
            return dao.updateById(groupBuyActivity) > 0;
        }
        return null;
    }

    /**
     * 删除拼团活动
     *
     * @param id 活动id
     * @return 删除结果
     */
    @Override
    public Boolean deleteGroupBuyActivity(Integer id) {
        GroupBuyActivity activity = getById(id);
        GroupBuyActivityResponse groupBuyActivityResponse = new GroupBuyActivityResponse();
        BeanUtils.copyProperties(activity, groupBuyActivityResponse);
        if (groupBuyActivityResponse.getGroupProcess().equals(1)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "进行中的拼团活动不能删除");
        }
        GroupBuyActivity groupBuyActivity = new GroupBuyActivity();
        groupBuyActivity.setId(id);
        groupBuyActivity.setIsDel(1);
        groupBuyActivity.setUpdateTime(DateUtil.date());
        return dao.updateById(groupBuyActivity) > 0;
    }

    /**
     * 获取正在拼团中的列表
     *
     * @param limit 限制数量
     * @return 拼团列表
     */
    private List<GroupBuyActivity> getListForShopActive(Integer limit) {
        LambdaQueryWrapper<GroupBuyActivity> queryWrapper = Wrappers.lambdaQuery();
        Date date = CrmebDateUtil.nowDateTime();
        queryWrapper.le(GroupBuyActivity::getStartTime, date)
                .gt(GroupBuyActivity::getEndTime, date)
                .eq(GroupBuyActivity::getIsDel, Boolean.FALSE)
                .eq(GroupBuyActivity::getGroupStatus, GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_PASS.getCode())
                .eq(GroupBuyActivity::getActivityStatus, Boolean.TRUE)
                .orderByDesc(GroupBuyActivity::getId)
                .last("limit " + limit);
        return dao.selectList(queryWrapper);
    }

    /**
     * 获取正在拼团中的列表
     *
     * @param limit 限制数量
     * @return 拼团列表
     */
    private List<GroupBuyActivity> getListForShopActive(Integer merId, Integer limit) {
        LambdaQueryWrapper<GroupBuyActivity> queryWrapper = Wrappers.lambdaQuery();
        Date date = CrmebDateUtil.nowDateTime();
        queryWrapper.le(GroupBuyActivity::getStartTime, date)
                .gt(GroupBuyActivity::getEndTime, date)
                .eq(GroupBuyActivity::getIsDel, Boolean.FALSE)
                .eq(GroupBuyActivity::getGroupStatus, GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_PASS.getCode())
                .eq(GroupBuyActivity::getActivityStatus, Boolean.TRUE)
                .eq(GroupBuyActivity::getMerId, merId)
                .orderByDesc(GroupBuyActivity::getId)
                .last("limit " + limit);
        return dao.selectList(queryWrapper);
    }

    //////////////////////////////////////////////////////////////////// 以下为移动端方法

    /**
     * 拼团活动首页数据 移动端商城首页
     *
     * @return GroupBuyActivityFrontResponse
     */
    @Override
    public GroupBuyActivityFrontResponse getGroupBuyActivityFrontIndex(Integer limit) {
        GroupBuyActivityFrontResponse response = new GroupBuyActivityFrontResponse();
        // 查询开启的拼团列表前3个，获取到每个拼团活动的第一个主图 和拼团基础信息
        List<GroupBuyActivity> groupBuyActivityList = getListForShopActive(limit);

        List<GroupBuyActivityFrontItemResponse> itemResponse = new ArrayList<>();
        for (GroupBuyActivity groupBuyActivity : groupBuyActivityList) {
            GroupBuyActivityFrontItemResponse item = new GroupBuyActivityFrontItemResponse();
            BeanUtils.copyProperties(groupBuyActivity, item);

            // 根据拼团主id 查询 对应拼团sku 信息和商品信息
            // 主商品的价格数据拿第一个拼团价最低sku的数据
            GroupBuyActivitySku groupBuyActivitySku = groupBuyActivitySkuService.getFrontMinActivePriceByGroupActivityId(groupBuyActivity.getId());
            GroupBuyActivityProductResponse productResponse = new GroupBuyActivityProductResponse();
            if (ObjectUtil.isNotNull(groupBuyActivitySku)) {
                GroupBuyActivitySkuResponse skuResponse = new GroupBuyActivitySkuResponse();
                List<GroupBuyActivitySkuResponse> skuResponses = new ArrayList<>();
                BeanUtils.copyProperties(groupBuyActivitySku, skuResponse);

                Product product = productService.getById(groupBuyActivitySku.getProductId());
                ProductAttrValue attrValue = productAttrValueService.getById(groupBuyActivitySku.getSkuId());

                AttrValueResponse attrValueResponse = new AttrValueResponse();
                BeanUtils.copyProperties(attrValue, attrValueResponse);

                // 商品基础的sku 信息
                skuResponse.setProductName(product.getName());
                List<AttrValueResponse> attrValueResponseList = new ArrayList<>();
                attrValueResponse.setPrice(attrValue.getPrice());
                attrValueResponseList.add(attrValueResponse);
                skuResponse.setAttrValue(attrValueResponseList);

                skuResponse.setLatestBuyCount(groupBuyUserService.getOrderDoneCountByProductIdAndActivityId(groupBuyActivitySku.getProductId(), groupBuyActivity.getId()));

                productResponse.setProductId(groupBuyActivitySku.getProductId());
                productResponse.setProductName(product.getName());
                productResponse.setImage(product.getImage());

                skuResponses.add(skuResponse);
                productResponse.setGroupBuyActivitySkuResponses(skuResponses);
                item.setGroupBuyActivityProductResponse(productResponse);
            }

            item.setGroupBuyActivityProductResponse(productResponse);
            itemResponse.add(item);
        }


        response.setItems(itemResponse);
        // 获取历史拼团成功的数量
        response.setTotalAllOrderDone(groupBuyUserService.getGroupBuyUserDoneTotalCount());


        // 获取拼团成功的最后5个用户头像
        List<GroupBuyUser> lastGroupBuyUserList = groupBuyUserService.getLastGroupBuyUserList(null, 5);
        List<String> groupDoneUserImages = Collections.emptyList();
        if (!lastGroupBuyUserList.isEmpty()) {
            groupDoneUserImages = lastGroupBuyUserList.stream().map(GroupBuyUser::getGroupAvatar).collect(Collectors.toList());
        }
        response.setOrderDoneUserImages(groupDoneUserImages);
        return response;
    }

    /**
     * 商户首页拼团卡片数据获取
     * @param merId 商户ID
     * @param limit 商品条数
     */
    @Override
    public GroupBuyActivityFrontResponse getGroupBuyActivityMerchantFrontIndex(Integer merId, Integer limit) {
        GroupBuyActivityFrontResponse response = new GroupBuyActivityFrontResponse();
        // 查询开启的拼团列表前3个，获取到每个拼团活动的第一个主图 和拼团基础信息
        List<GroupBuyActivity> groupBuyActivityList = getListForShopActive(merId, limit);

        List<GroupBuyActivityFrontItemResponse> itemResponse = new ArrayList<>();
        for (GroupBuyActivity groupBuyActivity : groupBuyActivityList) {
            GroupBuyActivityFrontItemResponse item = new GroupBuyActivityFrontItemResponse();
            BeanUtils.copyProperties(groupBuyActivity, item);

            // 根据拼团主id 查询 对应拼团sku 信息和商品信息
            // 主商品的价格数据拿第一个拼团价最低sku的数据
            GroupBuyActivitySku groupBuyActivitySku = groupBuyActivitySkuService.getFrontMinActivePriceByGroupActivityId(groupBuyActivity.getId());
            GroupBuyActivityProductResponse productResponse = new GroupBuyActivityProductResponse();
            if (ObjectUtil.isNotNull(groupBuyActivitySku)) {
                GroupBuyActivitySkuResponse skuResponse = new GroupBuyActivitySkuResponse();
                List<GroupBuyActivitySkuResponse> skuResponses = new ArrayList<>();
                BeanUtils.copyProperties(groupBuyActivitySku, skuResponse);

                Product product = productService.getById(groupBuyActivitySku.getProductId());
                ProductAttrValue attrValue = productAttrValueService.getById(groupBuyActivitySku.getSkuId());

                AttrValueResponse attrValueResponse = new AttrValueResponse();
                BeanUtils.copyProperties(attrValue, attrValueResponse);

                // 商品基础的sku 信息
                skuResponse.setProductName(product.getName());
                List<AttrValueResponse> attrValueResponseList = new ArrayList<>();
                attrValueResponse.setPrice(attrValue.getPrice());
                attrValueResponseList.add(attrValueResponse);
                skuResponse.setAttrValue(attrValueResponseList);

                skuResponse.setLatestBuyCount(groupBuyUserService.getOrderDoneCountByProductIdAndActivityId(groupBuyActivitySku.getProductId(), groupBuyActivity.getId()));

                productResponse.setProductId(groupBuyActivitySku.getProductId());
                productResponse.setProductName(product.getName());
                productResponse.setImage(product.getImage());

                skuResponses.add(skuResponse);
                productResponse.setGroupBuyActivitySkuResponses(skuResponses);
                item.setGroupBuyActivityProductResponse(productResponse);
            }

            item.setGroupBuyActivityProductResponse(productResponse);
            itemResponse.add(item);
        }


        response.setItems(itemResponse);
        // 获取历史拼团成功的数量
        response.setTotalAllOrderDone(groupBuyUserService.getGroupBuyUserDoneTotalCount(merId));


        // 获取拼团成功的最后5个用户头像
        List<GroupBuyUser> lastGroupBuyUserList = groupBuyUserService.getLastGroupBuyUserList(null, merId, 5);
        List<String> groupDoneUserImages = Collections.emptyList();
        if (!lastGroupBuyUserList.isEmpty()) {
            groupDoneUserImages = lastGroupBuyUserList.stream().map(GroupBuyUser::getGroupAvatar).collect(Collectors.toList());
        }
        response.setOrderDoneUserImages(groupDoneUserImages);
        return response;
    }

    /**
     * 通过商品平台分类id查询拼团商品列表
     * @return 拼团商品列表
     */
    @Override
    public PageInfo<GroupBuyActivityResponse> getGroupBuyProductListByCategoryId(GroupBuyProductCategorySearchRequest request) {
        Integer categoryId = request.getCategoryId();

        // 获取所有子分类ID（包括当前分类）
        List<Integer> categoryIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(categoryId)) {
            // 先获取当前分类信息
            ProductCategory currentCategory = productCategoryService.getById(categoryId);
            if (currentCategory != null) {
                categoryIds.add(categoryId);
                // 递归获取所有子分类
                List<ProductCategory> childCategories = productCategoryService.findAllChildListByPid(categoryId, currentCategory.getLevel());
                if (CollUtil.isNotEmpty(childCategories)) {
                    List<Integer> childIds = childCategories.stream().map(ProductCategory::getId).collect(Collectors.toList());
                    categoryIds.addAll(childIds);
                }
            }
        }

        // 构建查询条件
        LambdaQueryWrapper<GroupBuyActivity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(GroupBuyActivity::getIsDel, 0);

        // 只查询已通过审核且正在进行中的活动
        Date now = CrmebDateUtil.nowDateTime();
        lambdaQueryWrapper.le(GroupBuyActivity::getStartTime, now)
                .gt(GroupBuyActivity::getEndTime, now)
                .eq(GroupBuyActivity::getGroupStatus, GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_PASS.getCode())
                .eq(GroupBuyActivity::getActivityStatus, Boolean.TRUE);

        // 排序处理
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField.toLowerCase()) {
                case "createtime":
                    lambdaQueryWrapper.orderBy(true, isAsc, GroupBuyActivity::getCreateTime);
                    break;
                default:
                    lambdaQueryWrapper.orderByDesc(GroupBuyActivity::getCreateTime);
                    break;
            }
        } else {
            lambdaQueryWrapper.orderByDesc(GroupBuyActivity::getCreateTime);
        }

        Page<GroupBuyActivity> activityGroupPage = PageHelper.startPage(request.getPage(), request.getLimit());
        List<GroupBuyActivity> groupBuyActivities = dao.selectList(lambdaQueryWrapper);

        List<GroupBuyActivityResponse> groupBuyActivityResponses = new ArrayList<>();
        for (GroupBuyActivity groupBuyActivity : groupBuyActivities) {
            GroupBuyActivityResponse groupBuyActivityResponse = new GroupBuyActivityResponse();
            BeanUtils.copyProperties(groupBuyActivity, groupBuyActivityResponse);

            // 获取拼团商品
            List<GroupBuyActivitySku> groupBuyActivitySkuList = groupBuyActivitySkuService.getListByGroupActivityId(groupBuyActivity.getId());
            if (!groupBuyActivitySkuList.isEmpty()) {
                Map<Integer, List<GroupBuyActivitySku>> skuListGroupByProductIdMap =
                        groupBuyActivitySkuList.stream().collect(Collectors.groupingBy(GroupBuyActivitySku::getProductId));

                List<GroupBuyActivityProductResponse> groupBuyActivityProductResponseList = new ArrayList<>();
                for (Map.Entry<Integer, List<GroupBuyActivitySku>> productIdEntry : skuListGroupByProductIdMap.entrySet()) {
                    Integer productId = productIdEntry.getKey();
                    List<GroupBuyActivitySku> groupBuyActivitySkuListByProductId = productIdEntry.getValue();
                    Product product = productService.getById(productId);

                    // 根据分类ID过滤商品：支持查询指定分类及其所有子分类的商品
                    if (product != null && !product.getIsDel() &&
                        (ObjectUtil.isEmpty(categoryId) || categoryIds.contains(product.getCategoryId()))) {
                        GroupBuyActivityProductResponse productResponse = new GroupBuyActivityProductResponse();
                        productResponse.setProductId(productId);
                        productResponse.setProductName(product.getName());
                        productResponse.setImage(product.getImage());

                        List<GroupBuyActivitySkuResponse> groupBuyActivitySkuResponseList = new ArrayList<>();
                        for (GroupBuyActivitySku sku : groupBuyActivitySkuListByProductId) {
                            GroupBuyActivitySkuResponse skuResponse = new GroupBuyActivitySkuResponse();
                            BeanUtils.copyProperties(sku, skuResponse);
                            skuResponse.setProductName(product.getName());
                            ProductAttrValue attrValue = productAttrValueService.getById(sku.getSkuId());
                            AttrValueResponse attrValueResponse = new AttrValueResponse();
                            BeanUtils.copyProperties(attrValue, attrValueResponse);
                            List<AttrValueResponse> attrValueResponseList = new ArrayList<>();
                            attrValueResponse.setPrice(attrValue.getPrice());
                            attrValueResponseList.add(attrValueResponse);
                            skuResponse.setAttrValue(attrValueResponseList);
                            groupBuyActivitySkuResponseList.add(skuResponse);
                        }
                        productResponse.setGroupBuyActivitySkuResponses(groupBuyActivitySkuResponseList);
                        groupBuyActivityProductResponseList.add(productResponse);
                    }
                }

                // 只有当该拼团活动包含指定分类的商品时，才将该活动加入结果
                if (!groupBuyActivityProductResponseList.isEmpty()) {
                    groupBuyActivityResponse.setGroupBuyActivityProductResponseList(groupBuyActivityProductResponseList);

                    // 根据排序字段对商品进行排序
                    if (StrUtil.isNotBlank(sortField) && !"createtime".equalsIgnoreCase(sortField)) {
                        sortProductsByField(groupBuyActivityProductResponseList, sortField, sortOrder);
                    }

                    groupBuyActivityResponses.add(groupBuyActivityResponse);
                }
            }
        }
        return CommonPage.copyPageInfo(activityGroupPage, groupBuyActivityResponses);
    }

    /**
     * 获取所有团购商品的父级分类列表
     * @return 父级分类列表
     */
    @Override
    public List<ProCategoryCacheVo> getParentCategoryIdsByGroupBuyProducts() {
        // 构建查询条件：只查询已通过审核且正在进行中的活动
        LambdaQueryWrapper<GroupBuyActivity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(GroupBuyActivity::getIsDel, 0);
        Date now = CrmebDateUtil.nowDateTime();
        lambdaQueryWrapper.le(GroupBuyActivity::getStartTime, now)
                .gt(GroupBuyActivity::getEndTime, now)
                .eq(GroupBuyActivity::getGroupStatus, GroupBuyGroupStatusEnum.GROUP_BUY_ENUM_ACTIVITY_STATUS_PASS.getCode())
                .eq(GroupBuyActivity::getActivityStatus, Boolean.TRUE);

        List<GroupBuyActivity> groupBuyActivities = dao.selectList(lambdaQueryWrapper);

        // 收集所有团购商品的分类ID
        Set<Integer> categoryIds = new HashSet<>();
        for (GroupBuyActivity groupBuyActivity : groupBuyActivities) {
            // 获取该团购活动下的所有商品SKU
            List<GroupBuyActivitySku> groupBuyActivitySkuList = groupBuyActivitySkuService.getListByGroupActivityId(groupBuyActivity.getId());
            for (GroupBuyActivitySku sku : groupBuyActivitySkuList) {
                Product product = productService.getById(sku.getProductId());
                if (product != null && !product.getIsDel() && product.getCategoryId() != null) {
                    categoryIds.add(product.getCategoryId());
                }
            }
        }

        // 获取所有分类的父级ID（顶级分类）
        Set<Integer> parentCategoryIds = new HashSet<>();
        for (Integer categoryId : categoryIds) {
            ProductCategory category = productCategoryService.getById(categoryId);
            if (category != null) {
                // 如果是一级分类，直接加入
                if (category.getPid() == 0 || category.getLevel() == 1) {
                    parentCategoryIds.add(categoryId);
                } else {
                    // 如果不是一级分类，查找其顶级父分类
                    Integer parentId = findTopParentCategoryId(categoryId);
                    if (parentId != null) {
                        parentCategoryIds.add(parentId);
                    }
                }
            }
        }

        // 构建返回结果：将父级分类ID转换为ProCategoryCacheVo
        List<ProCategoryCacheVo> result = new ArrayList<>();
        for (Integer parentCategoryId : parentCategoryIds) {
            ProductCategory category = productCategoryService.getById(parentCategoryId);
            if (category != null && !category.getIsDel() && category.getIsShow()) {
                ProCategoryCacheVo vo = new ProCategoryCacheVo();
                vo.setId(category.getId());
                vo.setPid(category.getPid());
                vo.setName(category.getName());
                vo.setIcon(category.getIcon());
                vo.setLevel(category.getLevel());
                vo.setSort(category.getSort());
                vo.setIsShow(category.getIsShow());
                vo.setCreateTime(category.getCreateTime());
                result.add(vo);
            }
        }

        return sortList(result);
    }
    // 排序
    private List<ProCategoryCacheVo> sortList(List<ProCategoryCacheVo> treeMenus) {
        treeMenus = treeMenus.stream().sorted(Comparator.comparing(ProCategoryCacheVo::getSort).reversed()).collect(Collectors.toList());
        treeMenus.forEach(e -> {
            if (CollUtil.isNotEmpty(e.getChildList())) {
                e.setChildList(sortList(e.getChildList()));
            }
        });
        return treeMenus;
    }
    /**
     * 递归查找顶级父分类ID
     * @param categoryId 分类ID
     * @return 顶级父分类ID
     */
    private Integer findTopParentCategoryId(Integer categoryId) {
        ProductCategory category = productCategoryService.getById(categoryId);
        if (category == null) {
            return null;
        }

        // 如果是顶级分类（pid为0或level为1）
        if (category.getPid() == 0 || category.getLevel() == 1) {
            return category.getId();
        }

        // 递归查找父分类
        return findTopParentCategoryId(category.getPid());
    }

    //////////////////////////////////////////////////////////////////// 以下为工具方法

    /**
     * 拼团商品必要时的验证
     *
     * @param groupBuySkuList 拼团商品 SKU 列表
     */
    private void validGroupProduct(List<GroupBuyActivitySku> groupBuySkuList) {
        if (ObjectUtil.isNotEmpty(groupBuySkuList)) {
            for (GroupBuyActivitySku groupBuySku : groupBuySkuList) {
                ProductAttrValue sku = productAttrValueService.getById(groupBuySku.getSkuId());
                if (ObjectUtil.isNull(sku)) {
                    throw new CrmebException("商品不存在");
                }
                if (sku.getStock() < groupBuySku.getQuotaShow()) {
                    throw new CrmebException("商品库存不得大于原始商品库存");
                }
            }
        }
    }

    /**
     * 根据排序字段对商品进行排序
     *
     * @param productList 商品列表
     * @param sortField   排序字段
     * @param sortOrder   排序方式
     */
    private void sortProductsByField(List<GroupBuyActivityProductResponse> productList, String sortField, String sortOrder) {
        if (CollUtil.isEmpty(productList) || StrUtil.isBlank(sortField)) {
            return;
        }

        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        
        switch (sortField.toLowerCase()) {
            case "price":
                productList.sort((p1, p2) -> {
                    BigDecimal price1 = getMinPrice(p1);
                    BigDecimal price2 = getMinPrice(p2);
                    return isAsc ? price1.compareTo(price2) : price2.compareTo(price1);
                });
                break;
            case "sales":
                productList.sort((p1, p2) -> {
                    Integer sales1 = getTotalSales(p1);
                    Integer sales2 = getTotalSales(p2);
                    return isAsc ? sales1.compareTo(sales2) : sales2.compareTo(sales1);
                });
                break;
            case "rating":
                // 评价数排序，这里先用销量替代，实际项目中需要获取真实评价数据
                productList.sort((p1, p2) -> {
                    Integer rating1 = getTotalSales(p1); // 临时用销量替代
                    Integer rating2 = getTotalSales(p2);
                    return isAsc ? rating1.compareTo(rating2) : rating2.compareTo(rating1);
                });
                break;
            case "stock":
                productList.sort((p1, p2) -> {
                    Integer stock1 = getTotalStock(p1);
                    Integer stock2 = getTotalStock(p2);
                    return isAsc ? stock1.compareTo(stock2) : stock2.compareTo(stock1);
                });
                break;
            default:
                // 默认不排序
                break;
        }
    }

    /**
     * 获取商品最低价格
     */
    private BigDecimal getMinPrice(GroupBuyActivityProductResponse product) {
        if (CollUtil.isEmpty(product.getGroupBuyActivitySkuResponses())) {
            return BigDecimal.ZERO;
        }
        return product.getGroupBuyActivitySkuResponses().stream()
                .map(GroupBuyActivitySkuResponse::getActivePrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 获取商品总销量
     */
    private Integer getTotalSales(GroupBuyActivityProductResponse product) {
        if (CollUtil.isEmpty(product.getGroupBuyActivitySkuResponses())) {
            return 0;
        }
        // 由于GroupBuyActivitySkuResponse中没有sales字段，这里使用quotaShow-quota作为销量计算
        return product.getGroupBuyActivitySkuResponses().stream()
                .mapToInt(sku -> (sku.getQuotaShow() != null ? sku.getQuotaShow() : 0) - 
                               (sku.getQuota() != null ? sku.getQuota() : 0))
                .sum();
    }

    /**
     * 获取商品总库存
     */
    private Integer getTotalStock(GroupBuyActivityProductResponse product) {
        if (CollUtil.isEmpty(product.getGroupBuyActivitySkuResponses())) {
            return 0;
        }
        // 使用quota作为库存
        return product.getGroupBuyActivitySkuResponses().stream()
                .mapToInt(sku -> sku.getQuota() != null ? sku.getQuota() : 0)
                .sum();
    }
}

