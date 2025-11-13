package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.RedisConstants;
import com.zbkj.common.enums.AuditStatusEnum;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.product.ProductBrand;
import com.zbkj.common.model.product.ProductBrandCategory;
import com.zbkj.common.model.product.ProductCategory;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.BrandCategorySearchRequest;
import com.zbkj.common.request.MerchantApplyProductBrandRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.ProductBrandAuditRequest;
import com.zbkj.common.request.ProductBrandAuditListRequest;
import com.zbkj.common.request.ProductBrandBatchAuditRequest;
import com.zbkj.common.request.ProductBrandRequest;
import com.zbkj.common.response.ProductBrandListResponse;
import com.zbkj.common.response.ProductBrandResponse;
import com.zbkj.common.response.ProductBrandAuditListResponse;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.result.ProductResultCode;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zbkj.service.dao.ProductBrandDao;
import com.zbkj.service.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ProductBrandServiceImpl 接口实现
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Service
public class ProductBrandServiceImpl extends ServiceImpl<ProductBrandDao, ProductBrand> implements ProductBrandService {

    @Resource
    private ProductBrandDao dao;

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private SystemAttachmentService systemAttachmentService;
    @Autowired
    private ProductBrandCategoryService brandCategoryService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ProductService productService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private SystemAdminService systemAdminService;

    /**
     * 品牌分页列表
     *
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<ProductBrandListResponse> getAdminPage(PageParamRequest pageParamRequest) {
        Page<ProductBrand> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductBrand::getIsDel, false);
        lqw.orderByDesc(ProductBrand::getSort);
        List<ProductBrand> brandList = dao.selectList(lqw);
        if (CollUtil.isEmpty(brandList)) {
            return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
        }
        List<ProductBrandListResponse> responseList = brandList.stream().map(e -> {
            ProductBrandListResponse response = new ProductBrandListResponse();
            BeanUtils.copyProperties(e, response);
            List<ProductBrandCategory> list = brandCategoryService.getListByBrandId(e.getId());
            if (CollUtil.isEmpty(list)) {
                response.setCategoryIds("");
            } else {
                List<String> cidList = list.stream().map(bc -> bc.getCid().toString()).collect(Collectors.toList());
                response.setCategoryIds(String.join(",", cidList));
            }
            return response;
        }).collect(Collectors.toList());
        return CommonPage.copyPageInfo(page, responseList);
    }

    /**
     * 添加品牌
     *
     * @param request 添加参数
     * @return Boolean
     */
    @Override
    public Boolean add(ProductBrandRequest request) {
        validateName(request.getName());
        ProductBrand brand = new ProductBrand();
        BeanUtils.copyProperties(request, brand);
        brand.setId(null);
        if (StrUtil.isNotBlank(brand.getIcon())) {
            brand.setIcon(systemAttachmentService.clearPrefix(brand.getIcon()));
        }
        
        // 平台端新增的品牌默认为审核通过状态
        brand.setApplyMerId(null); // 平台直接创建，不是商户申请
        brand.setAuditStatus(null); // 平台直接创建，不需要审核
        brand.setApplyTime(null);
        brand.setRejectReason(null);
        brand.setAuditTime(null);
        brand.setAuditorId(null);
        
        Boolean execute = transactionTemplate.execute(e -> {
            save(brand);
            if (StrUtil.isNotBlank(request.getCategoryIds())) {
                List<ProductBrandCategory> initList = brandCategoryInit(brand.getId(), request.getCategoryIds());
                brandCategoryService.saveBatch(initList, 100);
            }
            return Boolean.TRUE;
        });
        if (execute) {
            redisUtil.delete(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY);
        }
        return execute;
    }

    /**
     * 删除品牌
     *
     * @param id 品牌ID
     * @return Boolean
     */
    @Override
    public Boolean delete(Integer id) {
        ProductBrand brand = getByIdException(id);
        // 判断商品是否使用品牌
        if (productService.isUseBrand(brand.getId())) {
            throw new CrmebException(ProductResultCode.PRODUCT_BRAND_USED);
        }
        brand.setIsDel(true);
        brand.setUpdateTime(DateUtil.date());
        Boolean execute = transactionTemplate.execute(e -> {
            updateById(brand);
            brandCategoryService.deleteByBid(brand.getId());
            // 删除品牌关联的优惠券
            couponService.deleteByBrandId(brand.getId());
            return Boolean.TRUE;
        });
        if (execute) {
            redisUtil.delete(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY);
        }
        return execute;
    }

    /**
     * 编辑品牌
     *
     * @param request 修改参数
     * @return Boolean
     */
    @Override
    public Boolean edit(ProductBrandRequest request) {
        if (ObjectUtil.isNull(request.getId())) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "品牌id不能为空");
        }
        ProductBrand oldBrand = getByIdException(request.getId());
        if (!oldBrand.getName().equals(request.getName())) {
            validateName(request.getName());
        }
        ProductBrand brand = new ProductBrand();
        BeanUtils.copyProperties(request, brand);
        if (StrUtil.isNotBlank(brand.getIcon())) {
            brand.setIcon(systemAttachmentService.clearPrefix(brand.getIcon()));
        }
        brand.setUpdateTime(DateUtil.date());
        Boolean execute = transactionTemplate.execute(e -> {
            updateById(brand);
            brandCategoryService.deleteByBid(brand.getId());
            if (StrUtil.isNotBlank(request.getCategoryIds())) {
                List<ProductBrandCategory> initList = brandCategoryInit(brand.getId(), request.getCategoryIds());
                brandCategoryService.saveBatch(initList, 100);
            }
            return Boolean.TRUE;
        });
        if (execute) {
            redisUtil.delete(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY);
        }
        return execute;
    }

    /**
     * 修改品牌显示状态
     *
     * @param id 品牌ID
     * @return Boolean
     */
    @Override
    public Boolean updateShowStatus(Integer id) {
        ProductBrand brand = getByIdException(id);
        brand.setIsShow(!brand.getIsShow());
        brand.setUpdateTime(DateUtil.date());
        boolean update = updateById(brand);
        if (update) {
            redisUtil.delete(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY);
        }
        return update;
    }

    /**
     * 品牌缓存列表(全部)
     *
     * @return List
     */
    @Override
    public List<ProductBrandResponse> getCacheAllList() {
        if (redisUtil.exists(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY)) {
            return redisUtil.get(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY);
        }
        LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductBrand::getIsDel, false);
        lqw.eq(ProductBrand::getIsShow, true);
        // 只查询审核通过的品牌（为空表示平台直接创建的品牌，不为空且为审核通过状态的是商户申请审核通过的品牌）
        lqw.and(wrapper -> wrapper.isNull(ProductBrand::getAuditStatus)
                .or()
                .eq(ProductBrand::getAuditStatus, AuditStatusEnum.APPROVED.getCode()));
        lqw.orderByDesc(ProductBrand::getSort);
        List<ProductBrand> brandList = dao.selectList(lqw);
        if (CollUtil.isEmpty(brandList)) {
            return CollUtil.newArrayList();
        }
        List<ProductBrandResponse> responseList = brandList.stream().map(e -> {
            ProductBrandResponse brandResponse = new ProductBrandResponse();
            BeanUtils.copyProperties(e, brandResponse);
            return brandResponse;
        }).collect(Collectors.toList());
        redisUtil.set(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY, responseList);
        return responseList;
    }

    /**
     * 通过分类查询品牌分页列表
     *
     * @param request          查询参数
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<ProductBrandResponse> getPageListByCategory(BrandCategorySearchRequest request, PageParamRequest pageParamRequest) {
        Page<ProductBrand> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        Map<String, Object> map = CollUtil.newHashMap();
        map.put("cid", request.getCid());
        if (StrUtil.isNotBlank(request.getBrandName())) {
            map.put("brandName", request.getBrandName());
        }
        List<ProductBrand> brandList = dao.getPageListByCategory(map);
        if (CollUtil.isEmpty(brandList)) {
            return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
        }
        List<ProductBrandResponse> responseList = brandList.stream().map(e -> {
            ProductBrandResponse brandResponse = new ProductBrandResponse();
            BeanUtils.copyProperties(e, brandResponse);
            return brandResponse;
        }).collect(Collectors.toList());
        return CommonPage.copyPageInfo(page, responseList);
    }

    /**
     * 根据ID列表获取品牌列表
     *
     * @param brandIdList 品牌ID列表
     */
    @Override
    public List<ProductBrand> findByIdList(List<Integer> brandIdList) {
        if (brandIdList == null || brandIdList.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductBrand::getIsDel, false);
        lqw.in(ProductBrand::getId, brandIdList);
        return dao.selectList(lqw);
    }

    /**
     * 商户申请平台品牌
     *
     * @param request 申请参数
     * @return Boolean
     */
    @Override
    public Boolean merchantApplyBrand(MerchantApplyProductBrandRequest request) {
        // 获取当前登录的商户
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        if (ObjectUtil.isNull(admin.getMerId())) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "商户信息不存在");
        }

        // 校验品牌名是否重复
        validateName(request.getName());

        // 创建品牌申请记录
        ProductBrand brand = new ProductBrand();
        BeanUtils.copyProperties(request, brand);
        brand.setId(null);
        brand.setApplyMerId(admin.getMerId());
        brand.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        brand.setApplyTime(DateUtil.date());
        brand.setIsShow(false); // 未审核通过前不显示
        brand.setIsDel(false);
        brand.setCreateTime(DateUtil.date());
        brand.setUpdateTime(DateUtil.date());
        
        if (StrUtil.isNotBlank(brand.getIcon())) {
            brand.setIcon(systemAttachmentService.clearPrefix(brand.getIcon()));
        }

        return transactionTemplate.execute(e -> {
            boolean save = save(brand);
            if (save && StrUtil.isNotBlank(request.getCategoryIds())) {
                // 保存品牌分类关联
                List<String> categoryIds = Arrays.asList(request.getCategoryIds().split(","));
                for (String categoryId : categoryIds) {
                    ProductBrandCategory brandCategory = new ProductBrandCategory();
                    brandCategory.setBid(brand.getId());
                    brandCategory.setCid(Integer.valueOf(categoryId));
                    brandCategoryService.save(brandCategory);
                }
            }
            return save;
        });
    }

    /**
     * 审核商户申请的品牌
     *
     * @param request 审核参数
     * @return Boolean
     */
    @Override
    public Boolean auditMerchantBrand(ProductBrandAuditRequest request) {
        // 获取当前登录的管理员
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        
        ProductBrand brand = getByIdException(request.getId());
        if (ObjectUtil.isNull(brand.getApplyMerId())) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "该品牌不是商户申请的品牌");
        }
        
        if (!brand.getAuditStatus().equals(AuditStatusEnum.PENDING.getCode())) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "该品牌已经审核过了");
        }

        // 审核拒绝
        if (request.getAuditStatus().equals(AuditStatusEnum.REJECTED.getCode())) {
            if (StrUtil.isBlank(request.getRejectReason())) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "审核拒绝时必须填写拒绝原因");
            }
            brand.setAuditStatus(AuditStatusEnum.REJECTED.getCode());
            brand.setRejectReason(request.getRejectReason());
            brand.setIsShow(false);
        } else {
            // 审核通过
            brand.setAuditStatus(AuditStatusEnum.APPROVED.getCode());
            brand.setIsShow(true);
            brand.setRejectReason(null);
        }
        
        brand.setAuditTime(DateUtil.date());
        brand.setAuditorId(admin.getId());
        brand.setUpdateTime(DateUtil.date());
        
        return updateById(brand);
    }

    private ProductBrand getByIdException(Integer id) {
        ProductBrand brand = getById(id);
        if (ObjectUtil.isNull(brand) || brand.getIsDel()) {
            throw new CrmebException(ProductResultCode.PRODUCT_BRAND_NOT_EXIST);
        }
        return brand;
    }

    /**
     * 初始化品牌分类关联
     *
     * @param categoryIds 分类ids
     * @return List<ProductBrandCategory>
     */
    private List<ProductBrandCategory> brandCategoryInit(Integer bid, String categoryIds) {
        String[] split = categoryIds.split(",");
        return Arrays.stream(split).map(e -> {
            ProductBrandCategory brandCategory = new ProductBrandCategory();
            brandCategory.setBid(bid);
            brandCategory.setCid(Integer.valueOf(e));
            return brandCategory;
        }).collect(Collectors.toList());
    }

    /**
     * 校验品牌名是否重复
     *
     * @param name 品牌名
     */
    private void validateName(String name) {
        LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductBrand::getIsDel, false);
        lqw.eq(ProductBrand::getName, name);
        lqw.last(" limit 1");
        ProductBrand brand = dao.selectOne(lqw);
        if (ObjectUtil.isNotNull(brand)) {
            throw new CrmebException(ProductResultCode.PRODUCT_BRAND_EXIST);
        }
    }

    /**
     * 获取品牌审核列表
     *
     * @param request 查询参数
     * @return 分页结果
     */
    @Override
    public PageInfo<ProductBrandAuditListResponse> getAuditList(ProductBrandAuditListRequest request) {
        PageHelper.startPage(request.getPage(), request.getLimit());
        LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
        lqw.isNotNull(ProductBrand::getApplyMerId);
        lqw.eq(ProductBrand::getIsDel, false);
        
        if (StrUtil.isNotBlank(request.getName())) {
            lqw.like(ProductBrand::getName, request.getName());
        }
        if (ObjectUtil.isNotNull(request.getAuditStatus())) {
            lqw.eq(ProductBrand::getAuditStatus, request.getAuditStatus());
        }
        if (StrUtil.isNotBlank(request.getStartTime())) {
            lqw.ge(ProductBrand::getApplyTime, request.getStartTime() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(request.getEndTime())) {
            lqw.le(ProductBrand::getApplyTime, request.getEndTime() + " 23:59:59");
        }
        
        lqw.orderByDesc(ProductBrand::getApplyTime);
        List<ProductBrand> brandList = dao.selectList(lqw);
        
        List<ProductBrandAuditListResponse> responseList = brandList.stream().map(brand -> {
            ProductBrandAuditListResponse response = new ProductBrandAuditListResponse();
            BeanUtils.copyProperties(brand, response);
            
            // 获取关联分类信息
            List<ProductBrandCategory> brandCategoryList = brandCategoryService.getListByBrandId(brand.getId());
            if (CollUtil.isNotEmpty(brandCategoryList)) {
                StringBuilder categoryIds = new StringBuilder();
                StringBuilder categoryNames = new StringBuilder();
                for (ProductBrandCategory brandCategory : brandCategoryList) {
                    categoryIds.append(brandCategory.getCid()).append(",");
                    // 这里需要获取分类名称
                    // categoryNames.append(分类名称).append(",");
                }
                if (categoryIds.length() > 0) {
                    response.setCategoryIds(categoryIds.substring(0, categoryIds.length() - 1));
                }
                if (categoryNames.length() > 0) {
                    response.setCategoryNames(categoryNames.substring(0, categoryNames.length() - 1));
                }
            }
            
            // 获取申请商户名称
            if (ObjectUtil.isNotNull(brand.getApplyMerId())) {
                // 这里需要调用商户服务获取商户名称
                 response.setApplyMerchantName(merchantService.getById(brand.getApplyMerId()).getName());
            }
            
            // 获取审核人员名称
            if (ObjectUtil.isNotNull(brand.getAuditorId())) {
                // 这里需要调用系统管理员服务获取审核人员名称
                // response.setAuditorName(systemAdminService.getById(brand.getAuditorId()).getRealName());
            }
            
            return response;
        }).collect(Collectors.toList());
        
        return PageInfo.of(responseList);
    }

    /**
     * 批量审核商户申请的品牌
     *
     * @param request 审核参数
     * @return 审核结果
     */
    @Override
    public Boolean batchAuditMerchantBrand(ProductBrandBatchAuditRequest request) {
        // 获取当前登录的管理员
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        
        // 获取待审核的品牌列表
        List<ProductBrand> brandList = listByIds(request.getIds());
        for (ProductBrand brand : brandList) {
            if (ObjectUtil.isNull(brand.getApplyMerId())) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "品牌ID:" + brand.getId() + " 不是商户申请的品牌");
            }
            if (!brand.getAuditStatus().equals(AuditStatusEnum.PENDING.getCode())) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "品牌ID:" + brand.getId() + " 已经审核过了");
            }
        }
        
        // 批量更新审核状态
        LambdaUpdateWrapper<ProductBrand> wrapper = Wrappers.lambdaUpdate();
        wrapper.set(ProductBrand::getAuditStatus, request.getAuditStatus());
        wrapper.set(ProductBrand::getAuditTime, DateUtil.date());
        wrapper.set(ProductBrand::getAuditorId, admin.getId());
        wrapper.set(ProductBrand::getUpdateTime, DateUtil.date());
        
        if (request.getAuditStatus().equals(AuditStatusEnum.REJECTED.getCode())) {
            // 审核拒绝
            if (StrUtil.isBlank(request.getRejectReason())) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "审核拒绝时必须填写拒绝原因");
            }
            wrapper.set(ProductBrand::getRejectReason, request.getRejectReason());
            wrapper.set(ProductBrand::getIsShow, false);
        } else {
            // 审核通过
            wrapper.set(ProductBrand::getIsShow, true);
            wrapper.set(ProductBrand::getRejectReason, null);
            if (redisUtil.exists(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY)) {
                 redisUtil.delete(RedisConstants.PRODUCT_ALL_BRAND_LIST_KEY);
            }
        }
        
        wrapper.in(ProductBrand::getId, request.getIds());
        return update(wrapper);
    }

    /**
     * 获取商户自己申请的品牌列表
     *
     * @param pageParamRequest 分页参数
     * @return 分页结果
     */
    @Override
    public PageInfo<ProductBrandAuditListResponse> getMerchantAppliedBrandList(PageParamRequest pageParamRequest) {
        // 获取当前登录的商户
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        if (ObjectUtil.isNull(admin.getMerId())) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "商户信息不存在");
        }

        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductBrand::getApplyMerId, admin.getMerId()); // 只查询当前商户申请的
        lqw.eq(ProductBrand::getIsDel, false);
        lqw.orderByDesc(ProductBrand::getApplyTime);
        List<ProductBrand> brandList = dao.selectList(lqw);
        
        List<ProductBrandAuditListResponse> responseList = brandList.stream().map(brand -> {
            ProductBrandAuditListResponse response = new ProductBrandAuditListResponse();
            BeanUtils.copyProperties(brand, response);
            
            // 获取关联分类信息
            List<ProductBrandCategory> brandCategoryList = brandCategoryService.getListByBrandId(brand.getId());
            if (CollUtil.isNotEmpty(brandCategoryList)) {
                StringBuilder categoryIds = new StringBuilder();
                StringBuilder categoryNames = new StringBuilder();
                for (ProductBrandCategory brandCategory : brandCategoryList) {
                    categoryIds.append(brandCategory.getCid()).append(",");
                    // 获取分类名称
                    ProductCategory category = productCategoryService.getById(brandCategory.getCid());
                    if (ObjectUtil.isNotNull(category)) {
                        categoryNames.append(category.getName()).append(",");
                    }
                }
                if (categoryIds.length() > 0) {
                    response.setCategoryIds(categoryIds.substring(0, categoryIds.length() - 1));
                }
                if (categoryNames.length() > 0) {
                    response.setCategoryNames(categoryNames.substring(0, categoryNames.length() - 1));
                }
            }
            
            // 设置申请商户名称（当前商户）
            response.setApplyMerchantName(merchantService.getById(admin.getMerId()).getName());
            
            // 获取审核人员名称
            if (ObjectUtil.isNotNull(brand.getAuditorId())) {
                SystemAdmin auditor = systemAdminService.getById(brand.getAuditorId());
                if (ObjectUtil.isNotNull(auditor)) {
                    response.setAuditorName(auditor.getRealName());
                }
            }
            
            return response;
        }).collect(Collectors.toList());
        
        return PageInfo.of(responseList);
    }
}

