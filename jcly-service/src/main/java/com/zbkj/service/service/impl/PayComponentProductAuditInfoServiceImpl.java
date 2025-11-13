package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.wechat.video.PayComponentProductAuditInfo;
import com.zbkj.service.dao.PayComponentProductAuditInfoDao;
import com.zbkj.service.service.PayComponentProductAuditInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
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
public class PayComponentProductAuditInfoServiceImpl extends ServiceImpl<PayComponentProductAuditInfoDao, PayComponentProductAuditInfo> implements PayComponentProductAuditInfoService {

    @Resource
    private PayComponentProductAuditInfoDao dao;

//    /**
//     * 获取最后一条商品审核信息
//     * @param productId 商品id
//     * @param auditId 审核单id
//     * @return PayComponentProductAuditInfo
//     */
//    @Override
//    public PayComponentProductAuditInfo getByProductIdAndAuditId(Integer productId, String auditId) {
//        LambdaQueryWrapper<PayComponentProductAuditInfo> lqw = Wrappers.lambdaQuery();
//        lqw.eq(PayComponentProductAuditInfo::getProductId, productId);
//        lqw.eq(PayComponentProductAuditInfo::getAuditId, auditId);
//        return dao.selectOne(lqw);
//    }
}

