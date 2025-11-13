package com.zbkj.service.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * 订单支付回调 service
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
public interface PayCallbackService {

    /**
     * 微信小程序支付回调
     *
     * @param notifyData 微信回调json
     * @return String
     */
    ResponseEntity<String> wechatMaPayCallback(String notifyData, HttpServletRequest servletRequest);

    /**
     * 微信公众号支付回调
     * @param notifyData 微信回调json
     * @return String
     */
    ResponseEntity<String> wechatMpPayCallback(String notifyData, HttpServletRequest servletRequest);

    /**
     * 微信APP支付回调
     * @param notifyData 微信回调json
     * @return String
     */
    ResponseEntity<String> wechatAPPPayCallback(String notifyData, HttpServletRequest servletRequest);

    /**
     * 支付宝支付回调
     */
    String aliPayCallback(HttpServletRequest request);

    /**
     * 微信小程序退款回调
     * @param notifyData 微信回调json
     * @return String
     */
    ResponseEntity<String> wechatMaRefundCallback(String notifyData, HttpServletRequest servletRequest);

    /**
     * 微信公众号退款回调
     * @param notifyData 微信回调json
     * @return String
     */
    ResponseEntity<String> wechatMpRefundCallback(String notifyData, HttpServletRequest servletRequest);

    /**
     * 微信APP退款回调
     * @param notifyData 微信回调json
     * @return String
     */
    ResponseEntity<String> wechatAPPRefundCallback(String notifyData, HttpServletRequest servletRequest);
}
