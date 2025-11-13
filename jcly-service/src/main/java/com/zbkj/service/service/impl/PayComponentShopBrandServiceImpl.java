package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.wechat.video.PayComponentShopBrand;
import com.zbkj.service.dao.PayComponentShopBrandDao;
import com.zbkj.service.service.PayComponentShopBrandService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * PayComponentShopBrandServiceImpl 接口实现
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Service
public class PayComponentShopBrandServiceImpl extends ServiceImpl<PayComponentShopBrandDao, PayComponentShopBrand> implements PayComponentShopBrandService {

    @Resource
    private PayComponentShopBrandDao dao;

//    @Autowired
//    private WechatVideoBeforeService wechatVideoBeforeService;

//    /**
//     * 上传品牌
//     * @param request 上传品牌请求参数
//     * @return 审核单id
//     */
//    @Override
//    public String auditBrand(ShopAuditBrandRequestVo request) {
//        // 参数校验
//
//        // 组件上传品牌
//        ShopAuditBrandResponseVo brandResponseVo = wechatVideoBeforeService.shopAuditBrand(request);
//
//        // 数据库存储
//        PayComponentShopBrand shopBrand = new PayComponentShopBrand();
//        ShopAuditBrandRequestItemVo auditReq = request.getAuditReq();
//        ShopAuditBrandRequestItemDataVo brandInfo = auditReq.getBrandInfo();
//        BeanUtils.copyProperties(brandInfo, shopBrand);
//        shopBrand.setAuditId(brandResponseVo.getAuditId());
//        shopBrand.setLicense(auditReq.getLicense());
//        shopBrand.setStatus(PayComponentCatStatusEnum.INIT.getCode());
//        shopBrand.setCreateTime(DateUtil.date());
//        save(shopBrand);
//        return brandResponseVo.getAuditId();
//    }
//
//    /**
//     * 获取品牌详情（根据审核单号）
//     * @param auditId 审核单号
//     * @return 品牌详情
//     */
//    @Override
//    public PayComponentShopBrand getByAuditId(String auditId) {
//        LambdaQueryWrapper<PayComponentShopBrand> lqw = Wrappers.lambdaQuery();
//        lqw.eq(PayComponentShopBrand::getAuditId, auditId);
//        lqw.orderByDesc(PayComponentShopBrand::getId);
//        lqw.last(" limit 1");
//        return dao.selectOne(lqw);
//    }
//
//    /**
//     * 获取品牌列表
//     * @param pageParamRequest 分页参数
//     * @param status 审核状态, 0：审核中，1：审核成功，9：审核拒绝
//     * @return 品牌列表
//     */
//    @Override
//    public PageInfo<PayComponentShopBrand> findList(PageParamRequest pageParamRequest, Integer status) {
//        Page<PayComponentShopBrand> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
//        LambdaQueryWrapper<PayComponentShopBrand> lqw = Wrappers.lambdaQuery();
//        if (ObjectUtil.isNotNull(status)) {
//            lqw.eq(PayComponentShopBrand::getStatus, status);
//        }
//        lqw.orderByDesc(PayComponentShopBrand::getId);
//        List<PayComponentShopBrand> list = dao.selectList(lqw);
//        return CommonPage.copyPageInfo(page, list);
//    }
//
//    /**
//     * 获取品牌列表 不分页
//     * @return 品牌列表
//     */
//    @Override
//    public List<PayComponentShopBrand> getUsableList() {
//        LambdaQueryWrapper<PayComponentShopBrand> lqw = Wrappers.lambdaQuery();
//        lqw.select(PayComponentShopBrand::getId, PayComponentShopBrand::getBrandId, PayComponentShopBrand::getBrandWording);
//        lqw.eq(PayComponentShopBrand::getStatus, 1);
//        lqw.orderByDesc(PayComponentShopBrand::getId);
//        return dao.selectList(lqw);
//    }
}

