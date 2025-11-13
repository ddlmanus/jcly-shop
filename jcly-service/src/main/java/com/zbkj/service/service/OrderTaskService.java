package com.zbkj.service.service;


/**
 * 订单任务服务 StoreOrderService 接口
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
public interface OrderTaskService {

    /**
     * 用户取消订单后置处理
     */
     void cancelByUser();

    /**
     * 订单退款
     */
     void orderRefund();
//
//     void complete();

    /**
     * 订单支付成功后置处理
     */
    void orderPaySuccessAfter();

    /**
     * 自动取消未支付订单
     */
    void autoCancel();

    /**
     * 订单收货
     */
    void orderReceiving();

    /**
     * 订单自动完成
     */
    void autoComplete();

    /**
     * 订单自动收货
     */
    void autoTakeDelivery();

    /**
     * 查询退款订单支付宝退款
     */
    void queryRefundOrderAliPayRefund();
}
