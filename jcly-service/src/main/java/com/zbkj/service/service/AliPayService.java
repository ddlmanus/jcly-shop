package com.zbkj.service.service;


import com.alipay.api.AlipayApiException;

import java.math.BigDecimal;

/**
 * 支付宝支付 Service
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
public interface AliPayService {


    /**
     * 查询支付结果
     * @param orderNo 订单编号
     * @return Boolean
     */
    Boolean queryPayResult(String orderNo);

    /**
     * 查询支付结果（返回完整响应）
     * @param orderNo 订单编号
     * @return AlipayTradeQueryResponse
     */
    com.alipay.api.response.AlipayTradeQueryResponse queryPayResultResponse(String orderNo);

    /**
     * 支付宝退款
     *
     * @param outTradeNo             支付宝交易号
     * @param refundOrderNo          退款单号
     * @param refundReasonWapExplain 退款说明
     * @param refundPrice            退款金额
     * @return Boolean
     */
    Boolean refund(String outTradeNo, String refundOrderNo, String refundReasonWapExplain, BigDecimal refundPrice);

    /**
     * 查询退款
     * @param orderNo 订单编号
     */
    Boolean queryRefund(String outTradeNo, String refundOrderNo);

    /**
     * 支付宝支付
     * @param orderNo 订单号
     * @param price 支付金额
     * @param orderType 订单类型：order - 商品订单，recharge - 充值订单
     * @param payChannel 支付渠道：alipayApp - 支付宝app支付, alipay - 支付宝支付
     * @param timeExpire 绝对超时时间，格式为yyyy-MM-dd HH:mm:ss。
     * @return 支付宝调用结果
     */
    String pay(String orderNo, BigDecimal price, String orderType, String payChannel, String timeExpire);

    /**
     * 支付宝二维码支付预创建
     * @param orderNo 订单号
     * @param price 支付金额
     * @param subject 订单标题
     * @param orderType 订单类型：order - 商品订单，recharge - 充值订单，svip - 付费会员订单
     * @param timeExpire 绝对超时时间，格式为yyyy-MM-dd HH:mm:ss。
     * @return 二维码字符串
     */
    String qrCodePay(String orderNo, BigDecimal price, String subject, String orderType, String timeExpire);

}
