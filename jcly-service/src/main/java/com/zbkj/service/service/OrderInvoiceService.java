package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.OrderInvoice;
import com.zbkj.common.response.OrderInvoiceResponse;

import java.util.List;

/**
 * OrderInvoiceService 接口
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
public interface OrderInvoiceService extends IService<OrderInvoice> {

    /**
     * 获取订单发货单列表
     * @param orderNo 订单号
     * @return 发货单列表
     */
    List<OrderInvoiceResponse> findByOrderNo(String orderNo);
}