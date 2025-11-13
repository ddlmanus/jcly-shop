package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.github.binarywang.wxpay.bean.notify.*;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.zbkj.common.config.CrmebConfig;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.alipay.AliPayCallback;
import com.zbkj.common.model.member.PaidMemberOrder;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.RechargeOrder;
import com.zbkj.common.model.order.RefundOrder;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.wechat.WechatPayInfo;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.vo.AttachVo;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;


/**
 * 订单支付回调 CallbackService 实现类
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
public class PayCallbackServiceImpl implements PayCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(PayCallbackServiceImpl.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private WechatPayInfoService wechatPayInfoService;
    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Lazy
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private RefundOrderService refundOrderService;
    @Autowired
    private AliPayCallbackService aliPayCallbackService;
    @Autowired
    private PaidMemberOrderService paidMemberOrderService;
    @Autowired
    private WechatPayService wechatPayService;
    @Autowired
    private CrmebConfig crmebConfig;

    /**
     * 微信小程序支付回调
     *
     * @param notifyData 微信回调json
     * @return String
     */
    @Override
    public ResponseEntity<String> wechatMaPayCallback(String notifyData, HttpServletRequest servletRequest) {
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayOrderNotifyResult wxPayOrderNotifyResult = wechatPayService.parseOrderNotifyResult(notifyData, "ma");
            String callback = wechatCommonPayCallback(wxPayOrderNotifyResult);
            return ResponseEntity.status(200).body(callback);
        }
        SignatureHeader header = getRequestHeader(servletRequest);
        try {
            WxPayNotifyV3Result wxPayNotifyV3Result = wechatPayService.parseOrderNotifyV3Result(notifyData, header, "ma");
            wechatCommonPayCallbackV3(wxPayNotifyV3Result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("错误"));
        }
        return ResponseEntity.status(200).body("");
    }


    /**
     * 微信公众号支付回调
     *
     * @param notifyData 微信回调json
     * @return String
     */
    @Override
    public ResponseEntity<String> wechatMpPayCallback(String notifyData, HttpServletRequest servletRequest) {
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayOrderNotifyResult wxPayOrderNotifyResult = wechatPayService.parseOrderNotifyResult(notifyData, "mp");
            String callback = wechatCommonPayCallback(wxPayOrderNotifyResult);
            return ResponseEntity.status(200).body(callback);
        }
        SignatureHeader header = getRequestHeader(servletRequest);
        try {
            WxPayNotifyV3Result wxPayNotifyV3Result = wechatPayService.parseOrderNotifyV3Result(notifyData, header, "mp");
            wechatCommonPayCallbackV3(wxPayNotifyV3Result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("错误"));
        }
        return ResponseEntity.status(200).body("");
    }

    /**
     * 微信APP支付回调
     *
     * @param notifyData 微信回调json
     * @return String
     */
    @Override
    public ResponseEntity<String> wechatAPPPayCallback(String notifyData, HttpServletRequest servletRequest) {
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayOrderNotifyResult wxPayOrderNotifyResult = wechatPayService.parseOrderNotifyResult(notifyData, "app");
            String callback = wechatCommonPayCallback(wxPayOrderNotifyResult);
            return ResponseEntity.status(200).body(callback);
        }
        SignatureHeader header = getRequestHeader(servletRequest);
        try {
            WxPayNotifyV3Result wxPayNotifyV3Result = wechatPayService.parseOrderNotifyV3Result(notifyData, header, "app");
            wechatCommonPayCallbackV3(wxPayNotifyV3Result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("错误"));
        }
        return ResponseEntity.status(200).body("");
    }

    private String wechatCommonPayCallback(WxPayOrderNotifyResult wxPayOrderNotifyResult) {
        String attach = wxPayOrderNotifyResult.getAttach();
        AttachVo attachVo = JSONObject.toJavaObject(JSONObject.parseObject(attach), AttachVo.class);
        // 获取用户信息
        User user = userService.getById(attachVo.getUserId());
        if (ObjectUtil.isNull(user)) {
            //用户信息错误
            logger.error("微信回调未查询到支付用户信息");
            return WxPayNotifyResponse.fail("为查询到支付用户");
        }
        switch (attachVo.getType()) {
            case PayConstants.PAY_SERVICE_TYPE_ORDER:
                return wechatPayOrderCallback(wxPayOrderNotifyResult);
            case PayConstants.PAY_SERVICE_TYPE_RECHARGE:
                return wechatRechargeOrderCallback(wxPayOrderNotifyResult);
            case PayConstants.PAY_SERVICE_TYPE_SVIP:
                return wechatSvipOrderCallback(wxPayOrderNotifyResult);
            default:
                logger.error("wechat pay err : 未知的订单类型==》" + attachVo.getType());
                throw new CrmebException("未知的订单类型！");
        }
    }

    /**
     * 微信支付回调公共处理V3
     */
    private ResponseEntity<String> wechatCommonPayCallbackV3(WxPayNotifyV3Result wxPayNotifyV3Result) {
        WxPayNotifyV3Result.DecryptNotifyResult decryptRes = wxPayNotifyV3Result.getResult();
        if (!WxPayConstants.WxpayTradeStatus.SUCCESS.equals(decryptRes.getTradeState())) {
            //失败返回4xx或5xx，且需要构造body信息
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("支付未成功"));
        }
        //成功返回200/204，body无需有内容
        String attach = decryptRes.getAttach();
        AttachVo attachVo = JSONObject.toJavaObject(JSONObject.parseObject(attach), AttachVo.class);
        // 获取用户信息
        User user = userService.getById(attachVo.getUserId());
        if (ObjectUtil.isNull(user)) {
            //用户信息错误
            logger.error("微信回调未查询到支付用户信息");
            return ResponseEntity.status(200).body("");
        }
        switch (attachVo.getType()) {
            case PayConstants.PAY_SERVICE_TYPE_ORDER:
                wechatPayOrderCallbackV3(wxPayNotifyV3Result);
                break;
            case PayConstants.PAY_SERVICE_TYPE_RECHARGE:
                wechatRechargeOrderCallbackV3(wxPayNotifyV3Result);
                break;
            case PayConstants.PAY_SERVICE_TYPE_SVIP:
                wechatSvipOrderCallbackV3(wxPayNotifyV3Result);
                break;
            default:
                logger.error("wechat pay err : 未知的订单类型==》" + attachVo.getType());
                break;
        }
        return ResponseEntity.status(200).body("");
    }


    /**
     * 微信支付商城订单支付回调处理
     */
    private String wechatPayOrderCallback(WxPayOrderNotifyResult wxPayOrderNotifyResult, String xmlInfo, User user) {
        Order order = orderService.getByOutTradeNo(wxPayOrderNotifyResult.getOutTradeNo());
        if (ObjectUtil.isNull(order) || !order.getUid().equals(user.getId())) {
            logger.error("wechat pay error : 商城订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 商城订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        if (order.getPaid()) {
            return WxPayNotifyResponse.success("成功");
        }
        String paySource = order.getPayChannel();
        if (paySource.equals(PayConstants.PAY_CHANNEL_H5) || paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_NATIVE)) {
            String source = systemConfigService.getValueByKey(SysConfigConstants.WECHAT_PAY_SOURCE_H5_PC);
            if (StrUtil.isNotBlank(source) && source.equals(PayConstants.WECHAT_PAY_SOURCE_MINI)) {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_MINI;
            } else {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_PUBLIC;
            }
        }
        WxPayOrderNotifyResult notifyResult = wechatPayService.parseOrderNotifyResult(xmlInfo, paySource);
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(order.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信订单信息不存在==》" + notifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 微信订单信息不存在==》" + notifyResult.getOutTradeNo());
        }
        wechatPayInfo.setIsSubscribe(notifyResult.getIsSubscribe());
        wechatPayInfo.setBankType(notifyResult.getBankType());
        wechatPayInfo.setCashFee(notifyResult.getCashFee());
        wechatPayInfo.setCouponFee(notifyResult.getCouponFee());
        wechatPayInfo.setTransactionId(notifyResult.getTransactionId());
        wechatPayInfo.setTimeEnd(notifyResult.getTimeEnd());
        // 添加支付成功redis队列
        Boolean execute = transactionTemplate.execute(e -> {
            order.setPaid(true);
            order.setPayTime(CrmebDateUtil.nowDateTime());
            order.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
            order.setUpdateTime(DateUtil.date());
            orderService.updateById(order);
            wechatPayInfoService.updateById(wechatPayInfo);
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error("wechat pay error : 订单更新失败==》" + notifyResult.getOutTradeNo());
            return WxPayNotifyResponse.fail("订单更新失败");
        }
        asyncService.orderPaySuccessSplit(order.getOrderNo());
        return WxPayNotifyResponse.success("成功");
    }

    /**
     * 微信支付充值订单支付回调处理
     */
    private String wechatRechargeOrderCallback(WxPayOrderNotifyResult wxPayOrderNotifyResult, String xmlInfo, User user) {
        RechargeOrder rechargeOrder = rechargeOrderService.getByOutTradeNo(wxPayOrderNotifyResult.getOutTradeNo());
        if (ObjectUtil.isNull(rechargeOrder) || !rechargeOrder.getUid().equals(user.getId())) {
            logger.error("wechat pay error : 充值订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 充值订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        if (rechargeOrder.getPaid()) {
            return WxPayNotifyResponse.success("成功");
        }
        String paySource = rechargeOrder.getPayChannel();
        if (paySource.equals(PayConstants.PAY_CHANNEL_H5) || paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_NATIVE)) {
            String source = systemConfigService.getValueByKey(SysConfigConstants.WECHAT_PAY_SOURCE_H5_PC);
            if (StrUtil.isNotBlank(source) && source.equals(PayConstants.WECHAT_PAY_SOURCE_MINI)) {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_MINI;
            } else {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_PUBLIC;
            }
        }
        WxPayOrderNotifyResult notifyResult = wechatPayService.parseOrderNotifyResult(xmlInfo, paySource);
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(rechargeOrder.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信充值订单信息不存在==》" + notifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 微信充值订单信息不存在==》" + notifyResult.getOutTradeNo());
        }
        wechatPayInfo.setIsSubscribe(notifyResult.getIsSubscribe());
        wechatPayInfo.setBankType(notifyResult.getBankType());
        wechatPayInfo.setCashFee(notifyResult.getCashFee());
        wechatPayInfo.setCouponFee(notifyResult.getCouponFee());
        wechatPayInfo.setTransactionId(notifyResult.getTransactionId());
        wechatPayInfo.setTimeEnd(notifyResult.getTimeEnd());
        wechatPayInfoService.updateById(wechatPayInfo);
        // 支付成功处理
        Boolean rechargePayAfter = rechargeOrderService.paySuccessAfter(rechargeOrder);
        if (!rechargePayAfter) {
            logger.error("wechat recharge pay after error : 充值订单数据保存失败==》" + notifyResult.getOutTradeNo());
            throw new CrmebException("wechat recharge pay after error : 充值订单数据保存失败==》" + notifyResult.getOutTradeNo());
        }
        return WxPayNotifyResponse.success("成功");
    }

    /**
     * 微信支付SVIP订单支付回调处理
     */
    private String wechatSvipOrderCallback(WxPayOrderNotifyResult wxPayOrderNotifyResult, String xmlInfo, User user) {
        PaidMemberOrder svipOrder = paidMemberOrderService.getByOutTradeNo(wxPayOrderNotifyResult.getOutTradeNo());
        if (ObjectUtil.isNull(svipOrder) || !svipOrder.getUid().equals(user.getId())) {
            logger.error("wechat pay error : SVIP订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : SVIP订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        if (svipOrder.getPaid()) {
            return WxPayNotifyResponse.success("成功");
        }
        String paySource = svipOrder.getPayChannel();
        if (paySource.equals(PayConstants.PAY_CHANNEL_H5) || paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_NATIVE)) {
            String source = systemConfigService.getValueByKey(SysConfigConstants.WECHAT_PAY_SOURCE_H5_PC);
            if (StrUtil.isNotBlank(source) && source.equals(PayConstants.WECHAT_PAY_SOURCE_MINI)) {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_MINI;
            } else {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_PUBLIC;
            }
        }
        WxPayOrderNotifyResult notifyResult = wechatPayService.parseOrderNotifyResult(xmlInfo, paySource);
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(svipOrder.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信SVIP订单信息不存在==》" + notifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 微信SVIP订单信息不存在==》" + notifyResult.getOutTradeNo());
        }
        wechatPayInfo.setIsSubscribe(notifyResult.getIsSubscribe());
        wechatPayInfo.setBankType(notifyResult.getBankType());
        wechatPayInfo.setCashFee(notifyResult.getCashFee());
        wechatPayInfo.setCouponFee(notifyResult.getCouponFee());
        wechatPayInfo.setTransactionId(notifyResult.getTransactionId());
        wechatPayInfo.setTimeEnd(notifyResult.getTimeEnd());
        wechatPayInfoService.updateById(wechatPayInfo);
        // 支付成功处理
        Boolean svipPayAfter = paidMemberOrderService.paySuccessAfter(svipOrder);
        if (!svipPayAfter) {
            logger.error("wechat recharge pay after error : 数据保存失败==》" + notifyResult.getOutTradeNo());
            throw new CrmebException("wechat recharge pay after error : 数据保存失败==》" + notifyResult.getOutTradeNo());
        }
        return WxPayNotifyResponse.success("成功");
    }

    /**
     * 微信支付商城订单支付回调处理
     */
    private String wechatPayOrderCallback(WxPayOrderNotifyResult wxPayOrderNotifyResult) {
        Order order = orderService.getByOutTradeNo(wxPayOrderNotifyResult.getOutTradeNo());
        if (ObjectUtil.isNull(order)) {
            logger.error("wechat pay error : 商城订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 商城订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        if (order.getPaid()) {
            return WxPayNotifyResponse.success("成功");
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(order.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 微信订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        wechatPayInfo.setIsSubscribe(wxPayOrderNotifyResult.getIsSubscribe());
        wechatPayInfo.setBankType(wxPayOrderNotifyResult.getBankType());
        wechatPayInfo.setCashFee(wxPayOrderNotifyResult.getCashFee());
        wechatPayInfo.setCouponFee(wxPayOrderNotifyResult.getCouponFee());
        wechatPayInfo.setTransactionId(wxPayOrderNotifyResult.getTransactionId());
        wechatPayInfo.setTimeEnd(wxPayOrderNotifyResult.getTimeEnd());
        // 添加支付成功redis队列
        Boolean execute = transactionTemplate.execute(e -> {
            order.setPaid(true);
            order.setPayTime(CrmebDateUtil.nowDateTime());
            order.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
            order.setUpdateTime(DateUtil.date());
            orderService.updateById(order);
            wechatPayInfoService.updateById(wechatPayInfo);
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error("wechat pay error : 订单更新失败==》" + wxPayOrderNotifyResult.getOutTradeNo());
            return WxPayNotifyResponse.fail("订单更新失败");
        }
        asyncService.orderPaySuccessSplit(order.getOrderNo());
        return WxPayNotifyResponse.success("成功");
    }

    /**
     * 微信支付充值订单支付回调处理
     */
    private String wechatRechargeOrderCallback(WxPayOrderNotifyResult wxPayOrderNotifyResult) {
        RechargeOrder rechargeOrder = rechargeOrderService.getByOutTradeNo(wxPayOrderNotifyResult.getOutTradeNo());
        if (ObjectUtil.isNull(rechargeOrder)) {
            logger.error("wechat pay error : 充值订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 充值订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        if (rechargeOrder.getPaid()) {
            return WxPayNotifyResponse.success("成功");
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(rechargeOrder.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信充值订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 微信充值订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        wechatPayInfo.setIsSubscribe(wxPayOrderNotifyResult.getIsSubscribe());
        wechatPayInfo.setBankType(wxPayOrderNotifyResult.getBankType());
        wechatPayInfo.setCashFee(wxPayOrderNotifyResult.getCashFee());
        wechatPayInfo.setCouponFee(wxPayOrderNotifyResult.getCouponFee());
        wechatPayInfo.setTransactionId(wxPayOrderNotifyResult.getTransactionId());
        wechatPayInfo.setTimeEnd(wxPayOrderNotifyResult.getTimeEnd());
        wechatPayInfoService.updateById(wechatPayInfo);
        // 支付成功处理
        Boolean rechargePayAfter = rechargeOrderService.paySuccessAfter(rechargeOrder);
        if (!rechargePayAfter) {
            logger.error("wechat recharge pay after error : 充值订单数据保存失败==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat recharge pay after error : 充值订单数据保存失败==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        return WxPayNotifyResponse.success("成功");
    }

    /**
     * 微信支付SVIP订单支付回调处理
     */
    private String wechatSvipOrderCallback(WxPayOrderNotifyResult wxPayOrderNotifyResult) {
        PaidMemberOrder svipOrder = paidMemberOrderService.getByOutTradeNo(wxPayOrderNotifyResult.getOutTradeNo());
        if (ObjectUtil.isNull(svipOrder)) {
            logger.error("wechat pay error : SVIP订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : SVIP订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        if (svipOrder.getPaid()) {
            return WxPayNotifyResponse.success("成功");
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(svipOrder.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信SVIP订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat pay error : 微信SVIP订单信息不存在==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        wechatPayInfo.setIsSubscribe(wxPayOrderNotifyResult.getIsSubscribe());
        wechatPayInfo.setBankType(wxPayOrderNotifyResult.getBankType());
        wechatPayInfo.setCashFee(wxPayOrderNotifyResult.getCashFee());
        wechatPayInfo.setCouponFee(wxPayOrderNotifyResult.getCouponFee());
        wechatPayInfo.setTransactionId(wxPayOrderNotifyResult.getTransactionId());
        wechatPayInfo.setTimeEnd(wxPayOrderNotifyResult.getTimeEnd());
        wechatPayInfoService.updateById(wechatPayInfo);
        // 支付成功处理
        Boolean svipPayAfter = paidMemberOrderService.paySuccessAfter(svipOrder);
        if (!svipPayAfter) {
            logger.error("wechat recharge pay after error : 数据保存失败==》" + wxPayOrderNotifyResult.getOutTradeNo());
            throw new CrmebException("wechat recharge pay after error : 数据保存失败==》" + wxPayOrderNotifyResult.getOutTradeNo());
        }
        return WxPayNotifyResponse.success("成功");
    }

    /**
     * 微信支付商城订单支付回调处理
     */
    private void wechatPayOrderCallbackV3(WxPayNotifyV3Result wxPayNotifyV3Result) {
        WxPayNotifyV3Result.DecryptNotifyResult decryptRes = wxPayNotifyV3Result.getResult();
        String outTradeNo = decryptRes.getOutTradeNo();
        Order order = orderService.getByOutTradeNo(outTradeNo);
        if (ObjectUtil.isNull(order)) {
            logger.error("wechat pay error : 商城订单信息不存在==》" + outTradeNo);
            return;
        }
        if (order.getPaid()) {
            logger.info("wechat pay info : 商城订单信息已支付==》" + outTradeNo);
            return;
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(order.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信订单信息不存在==》" + outTradeNo);
            return;
        }
        wechatPayInfo.setBankType(decryptRes.getBankType());
//        wechatPayInfo.setCashFee(decryptRes.getCashFee());
//        wechatPayInfo.setCouponFee(decryptRes.getCouponFee());
        wechatPayInfo.setTransactionId(decryptRes.getTransactionId());
//        wechatPayInfo.setTimeEnd(decryptRes.getTimeEnd());
        // 添加支付成功redis队列
        Boolean execute = transactionTemplate.execute(e -> {
            order.setPaid(true);
            order.setPayTime(CrmebDateUtil.nowDateTime());
            order.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
            order.setUpdateTime(DateUtil.date());
            orderService.updateById(order);
            wechatPayInfoService.updateById(wechatPayInfo);
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error("wechat pay error : 订单更新失败==》" + outTradeNo);
            return;
        }
        asyncService.orderPaySuccessSplit(order.getOrderNo());
    }

    /**
     * 微信支付充值订单支付回调处理
     */
    private void wechatRechargeOrderCallbackV3(WxPayNotifyV3Result wxPayNotifyV3Result) {
        WxPayNotifyV3Result.DecryptNotifyResult decryptRes = wxPayNotifyV3Result.getResult();
        String outTradeNo = decryptRes.getOutTradeNo();
        RechargeOrder rechargeOrder = rechargeOrderService.getByOutTradeNo(outTradeNo);
        if (ObjectUtil.isNull(rechargeOrder)) {
            logger.error("wechat pay error : 充值订单信息不存在==》" + outTradeNo);
            return;
        }
        if (rechargeOrder.getPaid()) {
            logger.info("wechat pay info : 充值订单信息已支付==》" + outTradeNo);
            return;
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(rechargeOrder.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信充值订单信息不存在==》" + outTradeNo);
            return;
        }
//        wechatPayInfo.setIsSubscribe(decryptRes.getIsSubscribe());
        wechatPayInfo.setBankType(decryptRes.getBankType());
//        wechatPayInfo.setCashFee(decryptRes.getCashFee());
//        wechatPayInfo.setCouponFee(decryptRes.getCouponFee());
        wechatPayInfo.setTransactionId(decryptRes.getTransactionId());
//        wechatPayInfo.setTimeEnd(decryptRes.getTimeEnd());
        wechatPayInfoService.updateById(wechatPayInfo);
        // 支付成功处理
        Boolean rechargePayAfter = rechargeOrderService.paySuccessAfter(rechargeOrder);
        if (!rechargePayAfter) {
            logger.error("wechat recharge pay after error : 充值订单数据保存失败==》" + outTradeNo);
            return;
        }
    }

    /**
     * 微信支付SVIP订单支付回调处理
     */
    private void wechatSvipOrderCallbackV3(WxPayNotifyV3Result wxPayNotifyV3Result) {
        WxPayNotifyV3Result.DecryptNotifyResult decryptRes = wxPayNotifyV3Result.getResult();
        String outTradeNo = decryptRes.getOutTradeNo();
        PaidMemberOrder svipOrder = paidMemberOrderService.getByOutTradeNo(outTradeNo);
        if (ObjectUtil.isNull(svipOrder)) {
            logger.error("wechat pay error : SVIP订单信息不存在==》" + outTradeNo);
            return;
        }
        if (svipOrder.getPaid()) {
            logger.info("wechat pay info : SVIP订单信息已支付==》" + outTradeNo);
            return;
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(svipOrder.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            logger.error("wechat pay error : 微信SVIP订单信息不存在==》" + outTradeNo);
            return;
        }
//        wechatPayInfo.setIsSubscribe(decryptRes.getIsSubscribe());
        wechatPayInfo.setBankType(decryptRes.getBankType());
//        wechatPayInfo.setCashFee(decryptRes.getCashFee());
//        wechatPayInfo.setCouponFee(decryptRes.getCouponFee());
        wechatPayInfo.setTransactionId(decryptRes.getTransactionId());
//        wechatPayInfo.setTimeEnd(decryptRes.getTimeEnd());
        wechatPayInfoService.updateById(wechatPayInfo);
        // 支付成功处理
        Boolean svipPayAfter = paidMemberOrderService.paySuccessAfter(svipOrder);
        if (!svipPayAfter) {
            logger.error("wechat recharge pay after error : 数据保存失败==》" + outTradeNo);
            return;
        }
    }

    /**
     * 支付宝支付回调
     */
    @Override
    public String aliPayCallback(HttpServletRequest request) {
        Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
        String paramsJson = JSON.toJSONString(params);
        // 保存支付宝回调信息
        saveAliPayCallbackInfo(params);
        try {
            //商户订单号
            String outTradeNo = params.get("out_trade_no");
            String outRequestNo = params.get("out_request_no");
            // 判断是否是退款订单
            String refundFee = params.get("refund_fee");
            if (StrUtil.isNotBlank(refundFee)) {// 订单退款
                return "success";
            }

            // 判断订单类型
            String passbackParams = params.get("passback_params");
            if (StrUtil.isNotBlank(passbackParams)) {
                String decode;
                try {
                    decode = URLDecoder.decode(passbackParams, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    logger.error("ali pay error : 订单支付类型解码失败==》" + outTradeNo);
                    return "fail";
                }
                String[] split = decode.split("=");
                String orderType = split[1];
                if (PayConstants.PAY_SERVICE_TYPE_RECHARGE.equals(orderType)) {// 充值订单
                    RechargeOrder rechargeOrder = rechargeOrderService.getByOutTradeNo(outTradeNo);
                    if (ObjectUtil.isNull(rechargeOrder)) {
                        logger.error("ali pay error : 充值订单后置处理，没有找到对应订单，支付服务方订单号：{}", outTradeNo);
                        return "fail";
                    }
                    if (rechargeOrder.getPaid()) {
                        return "success";
                    }
                    // 支付成功处理
                    Boolean rechargePayAfter = rechargeOrderService.paySuccessAfter(rechargeOrder);
                    if (!rechargePayAfter) {
                        logger.error("ali pay recharge pay after error : 数据保存失败==》" + outTradeNo);
                        return "fail";
                    }
                    return "success";
                }
                if (PayConstants.PAY_SERVICE_TYPE_SVIP.equals(orderType)) {// 充值订单
                    PaidMemberOrder svipOrder = paidMemberOrderService.getByOutTradeNo(outTradeNo);
                    if (ObjectUtil.isNull(svipOrder)) {
                        logger.error("ali pay error : SVIP订单后置处理，没有找到对应订单，支付服务方订单号：{}", outTradeNo);
                        return "fail";
                    }
                    if (svipOrder.getPaid()) {
                        return "success";
                    }
                    // 支付成功处理
                    Boolean svipPayAfter = paidMemberOrderService.paySuccessAfter(svipOrder);
                    if (!svipPayAfter) {
                        logger.error("ali pay svip pay after error : 数据保存失败==》" + outTradeNo);
                        return "fail";
                    }
                    return "success";
                }
                
                // 如果是普通订单，则走订单处理逻辑
                if (PayConstants.PAY_SERVICE_TYPE_ORDER.equals(orderType)) {
                    return processAliPayOrderCallback(outTradeNo, params, paramsJson);
                }
            }
            
            // 默认处理为普通订单
            return processAliPayOrderCallback(outTradeNo, params, paramsJson);
        } catch (Exception e) {
            logger.error("支付宝回调异常: {}", e.getMessage(), e);
            return "fail";
        }
    }
    /**
     * 处理充值订单回调
     */
    private void handleRechargeCallback(String outTradeNo) {
        try {
            // 根据充值订单号查询充值记录
            RechargeOrder rechargeOrder = rechargeOrderService.getByOrderNo(outTradeNo);
            if (ObjectUtil.isNull(rechargeOrder)) {
                logger.error("银联充值订单不存在: {}", outTradeNo);
                return;
            }
            
            if (rechargeOrder.getPaid()) {
                logger.info("银联充值订单已处理: {}", outTradeNo);
                return;
            }
            
            // 处理充值成功后的余额更新逻辑
            Boolean rechargePayAfter = rechargeOrderService.paySuccessAfter(rechargeOrder);
            if (!rechargePayAfter) {
                logger.error("银联充值订单处理失败: {}", outTradeNo);
                return;
            }
            
            logger.info("银联充值订单处理成功: {}", outTradeNo);
        } catch (Exception e) {
            logger.error("处理充值订单回调异常: {}", outTradeNo, e);
        }
    }

    /**
     * 处理商城订单回调
     */
    private void handleOrderCallback(String outTradeNo) {
        try {
            // 查询商城订单
            Order order = orderService.getByOrderNo(outTradeNo);
            if (ObjectUtil.isNull(order)) {
                logger.error("银联支付回调订单不存在: {}", outTradeNo);
                return;
            }
            
            if (order.getPaid()) {
                logger.info("银联支付回调订单已支付: {}", outTradeNo);
                return;
            }
            
            // 更新订单状态
            Boolean execute = transactionTemplate.execute(e -> {
                order.setPaid(true);
                order.setPayTime(CrmebDateUtil.nowDateTime());
                order.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
                order.setUpdateTime(DateUtil.date());
                orderService.updateById(order);
                return Boolean.TRUE;
            });
            
            if (!execute) {
                logger.error("银联支付回调订单更新失败: {}", outTradeNo);
                return;
            }
            
            // 异步处理订单支付成功后的逻辑
            asyncService.orderPaySuccessSplit(order.getOrderNo());
            
            logger.info("银联支付回调订单处理成功: {}", outTradeNo);
        } catch (Exception e) {
            logger.error("处理商城订单回调异常: {}", outTradeNo, e);
        }
    }

    /**
     * 处理支付宝回调中的订单逻辑
     */
    private String processAliPayOrderCallback(String outTradeNo, Map<String, String> params, String paramsJson) {
        try {
            // 找到原订单
            Order order = orderService.getByOrderNo(outTradeNo);
            if (ObjectUtil.isNull(order)) {
                logger.error("ali pay error : 订单信息不存在==》" + outTradeNo);
                return "fail";
            }
            if (order.getPaid()) {
                logger.error("ali pay error : 订单已处理==》" + outTradeNo);
                return "success";
            }
            //判断openid
            User user = userService.getById(order.getUid());
            if (ObjectUtil.isNull(user)) {
                //用户信息错误
                logger.error("支付宝回调用户信息错误，paramsJson = " + paramsJson);
                return "fail";
            }

            //支付宝交易号
            String tradeNo = params.get("trade_no");
            //交易状态
            String tradeStatus = params.get("trade_status");

            // 调用SDK验证签名
//            String aliPayPublicKey2 = systemConfigService.getValueByKey(AlipayConfig.ALIPAY_PUBLIC_KEY_2);
            String publicKey = systemConfigService.getValueByKey(AlipayConfig.ALIPAY_PUBLIC_KEY);
            boolean signVerified = AlipaySignature.rsaCheckV1(params, publicKey, AlipayConfig.CHARSET, "RSA2");
            if (signVerified) {//验证成功
                if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {//交易成功
                    // 添加支付成功redis队列
                    Boolean execute = transactionTemplate.execute(e -> {
                        order.setPaid(true);
                        order.setPayTime(CrmebDateUtil.nowDateTime());
                        order.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
                        order.setUpdateTime(DateUtil.date());
                        orderService.updateById(order);
                        return Boolean.TRUE;
                    });
                    if (!execute) {
                        logger.error("ali pay error : 订单更新失败==》" + outTradeNo);
                        return "fail";
                    }
                    asyncService.orderPaySuccessSplit(order.getOrderNo());
                }
                return "success";
            } else {
                logger.error("支付宝回调签名认证失败，signVerified=false, paramsJson:{}", paramsJson);
                return "fail";
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调签名认证失败,paramsJson:{},errorMsg:{}", paramsJson, e.getMessage());
            return "fail";
        } catch (Exception e) {
            logger.error("支付宝回调处理异常,paramsJson:{},errorMsg:{}", paramsJson, e.getMessage());
            return "fail";
        }
    }

    /**
     * 保存支付宝回调信息
     *
     * @param params
     */
    private void saveAliPayCallbackInfo(Map<String, String> params) {
        AliPayCallback aliPayCallback = new AliPayCallback();
        aliPayCallback.setNotifyType(params.get("notify_type"));
        aliPayCallback.setNotifyId(params.get("notify_id"));
        aliPayCallback.setAppId(params.get("app_id"));
        aliPayCallback.setCharset(params.get("charset"));
        aliPayCallback.setVersion(params.get("version"));
        aliPayCallback.setSignType(params.get("sign_type"));
        aliPayCallback.setSign(params.get("sign"));
        aliPayCallback.setTradeNo(params.get("trade_no"));
        aliPayCallback.setOutTradeNo(params.get("out_trade_no"));
        aliPayCallback.setTradeStatus(params.get("trade_status"));
        aliPayCallback.setTotalAmount(new BigDecimal(params.get("total_amount")));
        aliPayCallback.setReceiptAmount(ObjectUtil.isNotNull(params.get("receipt_amount")) ? new BigDecimal(params.get("receipt_amount")) : null);
        aliPayCallback.setRefundFee(ObjectUtil.isNotNull(params.get("refund_fee")) ? new BigDecimal(params.get("refund_fee")) : null);
        aliPayCallback.setSubject(params.get("subject"));
        aliPayCallback.setBody(Optional.ofNullable(params.get("body")).orElse(""));
        aliPayCallback.setPassbackParams(params.get("passback_params"));
        aliPayCallback.setNotifyTime(DateUtil.parse(params.get("notify_time")));
        aliPayCallback.setAddTime(DateUtil.date());
        aliPayCallbackService.save(aliPayCallback);
    }

    /**
     * 将request中的参数转换成Map
     *
     * @param request
     * @return
     */
    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String> retMap = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            retMap.put(name, valueStr);
        }
        return retMap;
    }

    /**
     * 微信小程序退款回调
     * @param notifyData 微信回调json
     * @return String
     */
    @Override
    public ResponseEntity<String> wechatMaRefundCallback(String notifyData, HttpServletRequest servletRequest) {
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayRefundNotifyResult wxPayRefundNotifyResult = wechatPayService.parseRefundNotifyResult(notifyData, "ma");
            String callback = wechatCommonRefundCallback(wxPayRefundNotifyResult);
            return ResponseEntity.status(200).body(callback);
        }
        SignatureHeader header = getRequestHeader(servletRequest);
        try {
            WxPayRefundNotifyV3Result wxPayRefundNotifyV3Result = wechatPayService.parseRefundNotifyV3Result(notifyData, header, "ma");
            wechatCommonRefundCallbackV3(wxPayRefundNotifyV3Result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("错误"));
        }
        return ResponseEntity.status(200).body("");
    }


    /**
     * 微信公众号退款回调
     * @param notifyData 微信回调json
     * @return String
     */
    @Override
    public ResponseEntity<String> wechatMpRefundCallback(String notifyData, HttpServletRequest servletRequest) {
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayRefundNotifyResult wxPayRefundNotifyResult = wechatPayService.parseRefundNotifyResult(notifyData, "mp");
            String callback = wechatCommonRefundCallback(wxPayRefundNotifyResult);
            return ResponseEntity.status(200).body(callback);
        }
        SignatureHeader header = getRequestHeader(servletRequest);
        try {
            WxPayRefundNotifyV3Result wxPayRefundNotifyV3Result = wechatPayService.parseRefundNotifyV3Result(notifyData, header, "mp");
            wechatCommonRefundCallbackV3(wxPayRefundNotifyV3Result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("错误"));
        }
        return ResponseEntity.status(200).body("");
    }

    /**
     * 微信APP退款回调
     * @param notifyData 微信回调json
     * @return String
     */
    @Override
    public ResponseEntity<String> wechatAPPRefundCallback(String notifyData, HttpServletRequest servletRequest) {
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayRefundNotifyResult wxPayRefundNotifyResult = wechatPayService.parseRefundNotifyResult(notifyData, "app");
            String callback = wechatCommonRefundCallback(wxPayRefundNotifyResult);
            return ResponseEntity.status(200).body(callback);
        }
        SignatureHeader header = getRequestHeader(servletRequest);
        try {
            WxPayRefundNotifyV3Result wxPayRefundNotifyV3Result = wechatPayService.parseRefundNotifyV3Result(notifyData, header, "app");
            wechatCommonRefundCallbackV3(wxPayRefundNotifyV3Result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("错误"));
        }
        return ResponseEntity.status(200).body("");
    }

    private String wechatCommonRefundCallback(WxPayRefundNotifyResult wxPayRefundNotifyResult) {
        if (!Constants.SUCCESS.equals(wxPayRefundNotifyResult.getReqInfo().getRefundStatus())) {
            logger.error("微信退款回调退款状态未成功==>notifyResult = {}", wxPayRefundNotifyResult);
            logger.error("微信退款回调退款状态未成功==>refund_status = {}", wxPayRefundNotifyResult.getReqInfo().getRefundStatus());
            return WxPayNotifyResponse.success("成功");
        }
        return wechatRefundOrderCallback(wxPayRefundNotifyResult);
    }

    private String wechatRefundOrderCallback(WxPayRefundNotifyResult wxPayRefundNotifyResult) {
        String outRefundNo = wxPayRefundNotifyResult.getReqInfo().getOutRefundNo();
        List<RefundOrder> refundOrderList = refundOrderService.findByOutRefundNo(outRefundNo);
        if (CollUtil.isEmpty(refundOrderList)) {
            logger.error("微信退款订单查询失败==>outRefundNo = {}, notifyResult = {}", outRefundNo, wxPayRefundNotifyResult);
            return WxPayNotifyResponse.fail("退款订单查询失败");
        }
        RefundOrder refundOrder = refundOrderList.get(0);
        if (refundOrder.getRefundStatus().equals(OrderConstants.MERCHANT_REFUND_ORDER_STATUS_REFUND)) {
            logger.error("微信退款订单已确认成功==>outRefundNo = {}, notifyResult = {}", outRefundNo, wxPayRefundNotifyResult);
            return WxPayNotifyResponse.success("成功");
        }
        refundOrderList.forEach(r -> {
            r.setRefundStatus(OrderConstants.MERCHANT_REFUND_ORDER_STATUS_REFUND);
            r.setUpdateTime(DateUtil.date());
        });
        boolean update = refundOrderService.updateBatchById(refundOrderList);
        if (update) {
            // 退款task
            refundOrderList.forEach(ro -> {
                redisUtil.lPush(TaskConstants.ORDER_TASK_REDIS_KEY_AFTER_REFUND_BY_USER, ro.getRefundOrderNo());
            });
        } else {
            logger.error("微信退款订单更新失败==>outRefundNo = {}, notifyResult = {}", outRefundNo, wxPayRefundNotifyResult);
        }
        return WxPayNotifyResponse.success("成功");
    }

    private ResponseEntity<String> wechatCommonRefundCallbackV3(WxPayRefundNotifyV3Result wxPayRefundNotifyV3Result) {
        WxPayRefundNotifyV3Result.DecryptNotifyResult decryptRes = wxPayRefundNotifyV3Result.getResult();
        if (!WxPayConstants.RefundStatus.SUCCESS.equals(decryptRes.getRefundStatus())) {
            return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("退款未成功"));
        }
        // 处理退款业务逻辑
        wechatRefundOrderCallbackV3(wxPayRefundNotifyV3Result);
        return ResponseEntity.status(200).body("");
    }

    private void wechatRefundOrderCallbackV3(WxPayRefundNotifyV3Result wxPayRefundNotifyV3Result) {
        WxPayRefundNotifyV3Result.DecryptNotifyResult decryptRes = wxPayRefundNotifyV3Result.getResult();
        String outRefundNo = decryptRes.getOutRefundNo();
        List<RefundOrder> refundOrderList = refundOrderService.findByOutRefundNo(outRefundNo);
        if (CollUtil.isEmpty(refundOrderList)) {
            logger.error("微信退款订单查询失败==>outRefundNo = {}, notifyResult = {}", outRefundNo, decryptRes);
            return;
        }
        RefundOrder refundOrder = refundOrderList.get(0);
        if (refundOrder.getRefundStatus().equals(OrderConstants.MERCHANT_REFUND_ORDER_STATUS_REFUND)) {
            logger.error("微信退款订单已确认成功==>outRefundNo = {}, notifyResult = {}", outRefundNo, decryptRes);
            return;
        }
        refundOrderList.forEach(r -> {
            r.setRefundStatus(OrderConstants.MERCHANT_REFUND_ORDER_STATUS_REFUND);
            r.setUpdateTime(DateUtil.date());
        });
        boolean update = refundOrderService.updateBatchById(refundOrderList);
        if (update) {
            // 退款task
            refundOrderList.forEach(ro -> {
                redisUtil.lPush(TaskConstants.ORDER_TASK_REDIS_KEY_AFTER_REFUND_BY_USER, ro.getRefundOrderNo());
            });
        } else {
            logger.error("微信退款订单更新失败==>outRefundNo = {}, notifyResult = {}", outRefundNo, decryptRes);
        }
    }

    /**
     * 组装请求头重的前面信息
     *
     * @param request
     * @return
     */
    private SignatureHeader getRequestHeader(HttpServletRequest request) {
        // 获取通知签名
        String signature = request.getHeader("Wechatpay-Signature");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String serial = request.getHeader("Wechatpay-Serial");
        String timestamp = request.getHeader("Wechatpay-Timestamp");

        SignatureHeader signatureHeader = new SignatureHeader();
        signatureHeader.setSignature(signature);
        signatureHeader.setNonce(nonce);
        signatureHeader.setSerial(serial);
        signatureHeader.setTimeStamp(timestamp);
        return signatureHeader;
    }
}
