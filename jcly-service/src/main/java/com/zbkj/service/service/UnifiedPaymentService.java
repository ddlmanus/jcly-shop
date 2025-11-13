package com.zbkj.service.service;

import com.zbkj.common.model.order.Order;
import com.zbkj.common.vo.UnifiedPaymentRequestVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 统一支付服务接口
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
public interface UnifiedPaymentService {

    /**
     * 统一支付入口
     * @param uid 用户ID
     * @param request 支付请求
     * @return 支付结果
     */
    Map<String, Object> unifiedPay(Integer uid, UnifiedPaymentRequestVo request);

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付状态查询结果
     */
    Map<String, Object> queryPaymentStatus(String orderNo);

    /**
     * 统一退款入口
     * @param orderNo 订单号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款结果
     */
    Map<String, Object> unifiedRefund(String orderNo, BigDecimal refundAmount, String reason);

    /**
     * 统一退货入口
     * @param orderNo 订单号
     * @param returnAmount 退货金额
     * @param reason 退货原因
     * @return 退货结果
     */
    Map<String, Object> unifiedReturn(String orderNo, BigDecimal returnAmount, String reason);
}
