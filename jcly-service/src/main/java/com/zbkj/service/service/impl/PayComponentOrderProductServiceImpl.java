package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.wechat.video.PayComponentOrderProduct;
import com.zbkj.service.dao.PayComponentOrderProductDao;
import com.zbkj.service.service.PayComponentOrderProductService;
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
public class PayComponentOrderProductServiceImpl extends ServiceImpl<PayComponentOrderProductDao, PayComponentOrderProduct> implements PayComponentOrderProductService {

    @Resource
    private PayComponentOrderProductDao dao;

//    /**
//     * 获取订单商品列表
//     * @param orderNo 订单编号
//     * @return List
//     */
//    @Override
//    public List<PayComponentOrderProduct> getListByOrderNo(String orderNo) {
//        LambdaQueryWrapper<PayComponentOrderProduct> lqw = Wrappers.lambdaQuery();
//        lqw.eq(PayComponentOrderProduct::getOrderNo, orderNo);
//        return dao.selectList(lqw);
//    }
}

