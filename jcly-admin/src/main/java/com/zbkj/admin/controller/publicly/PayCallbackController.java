package com.zbkj.admin.controller.publicly;

import com.alibaba.fastjson.JSON;
import com.zbkj.common.annotation.CustomResponseAnnotation;
import com.zbkj.service.service.PayCallbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * 支付回调
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
@Slf4j
@RestController
@RequestMapping("api/publicly/payment/callback")
@Api(tags = "支付回调控制器")
@CustomResponseAnnotation
public class PayCallbackController {

    @Autowired
    private PayCallbackService callbackService;

    @ApiOperation(value = "微信小程序支付回调")
    @RequestMapping(value = "/wechat/ma", method = RequestMethod.POST)
    public ResponseEntity<String> wechatMaPayCallback(@RequestBody String  notifyData, HttpServletRequest servletRequest) {
        System.out.println("微信小程序支付回调 request ===> " + notifyData);
        ResponseEntity<String> stringResponseEntity = callbackService.wechatMaPayCallback(notifyData, servletRequest);
        System.out.println("微信小程序支付回调 response ===> " + stringResponseEntity.getBody());
        return stringResponseEntity;
    }

    @ApiOperation(value = "微信公众号支付回调")
    @RequestMapping(value = "/wechat/mp", method = RequestMethod.POST)
    public ResponseEntity<String> wechatMpPayCallback(@RequestBody String  notifyData, HttpServletRequest servletRequest) {
        System.out.println("微信公众号支付回调 request ===> " + notifyData);
        ResponseEntity<String> stringResponseEntity = callbackService.wechatMpPayCallback(notifyData, servletRequest);
        System.out.println("微信公众号支付回调 response ===> " + stringResponseEntity.getBody());
        return stringResponseEntity;
    }

    @ApiOperation(value = "微信APP支付回调")
    @RequestMapping(value = "/wechat/app", method = RequestMethod.POST)
    public ResponseEntity<String> wechatAPPPayCallback(@RequestBody String  notifyData, HttpServletRequest servletRequest) {
        System.out.println("微信APP支付回调 request ===> " + notifyData);
        ResponseEntity<String> stringResponseEntity = callbackService.wechatAPPPayCallback(notifyData, servletRequest);
        System.out.println("微信APP支付回调 response ===> " + stringResponseEntity.getBody());
        return stringResponseEntity;
    }

    @ApiOperation(value = "支付宝支付回调 ")
    @RequestMapping(value = "/alipay", method = RequestMethod.POST)
    public String aliPay(HttpServletRequest request){
        //支付宝支付回调
        System.out.println("支付宝支付回调 request ===> " + JSON.toJSONString(request.getParameterMap()));
        return callbackService.aliPayCallback(request);
    }


    /**
     * 微信退款回调
     */
    @ApiOperation(value = "微信小程序退款回调")
    @RequestMapping(value = "/wechat/refund/ma", method = RequestMethod.POST)
    public ResponseEntity<String> weChatMaRefund(@RequestBody String notifyData, HttpServletRequest servletRequest) {
        System.out.println("微信小程序退款回调 request ===> " + notifyData);
        ResponseEntity<String> responseEntity = callbackService.wechatMaRefundCallback(notifyData, servletRequest);
        System.out.println("微信小程序退款回调 response ===> " + responseEntity.getBody());
        return responseEntity;
    }

    /**
     * 微信退款回调
     */
    @ApiOperation(value = "微信公众号退款回调")
    @RequestMapping(value = "/wechat/refund/mp", method = RequestMethod.POST)
    public ResponseEntity<String> weChatMpRefund(@RequestBody String notifyData, HttpServletRequest servletRequest) {
        System.out.println("微信公众号退款回调 request ===> " + notifyData);
        ResponseEntity<String> responseEntity = callbackService.wechatMpRefundCallback(notifyData, servletRequest);
        System.out.println("微信公众号退款回调 response ===> " + responseEntity.getBody());
        return responseEntity;
    }

    /**
     * 微信退款回调
     */
    @ApiOperation(value = "微信APP退款回调")
    @RequestMapping(value = "/wechat/refund/app", method = RequestMethod.POST)
    public ResponseEntity<String> weChatAppRefund(@RequestBody String notifyData, HttpServletRequest servletRequest) {
        System.out.println("微信APP退款回调 request ===> " + notifyData);
        ResponseEntity<String> responseEntity = callbackService.wechatAPPRefundCallback(notifyData, servletRequest);
        System.out.println("微信APP退款回调 response ===> " + responseEntity.getBody());
        return responseEntity;
    }
}



