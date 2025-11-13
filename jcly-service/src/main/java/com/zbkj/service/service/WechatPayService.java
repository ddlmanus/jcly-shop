package com.zbkj.service.service;

import com.github.binarywang.wxpay.bean.notify.*;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.zbkj.common.vo.MyRecord;

/**
 * 微信支付服务
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/3/27
 */
public interface WechatPayService {

    /**
     * 创建微信订单
     *
     * @param wxPayUnifiedOrderRequest 微信支付预下单对象
     * @param openid                   微信用户标识openId
     * @param channel                  支付渠道：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param paySource                支付来源：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param apiDomain                  回调地址域名
     */
    MyRecord createOrder(WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest, String openid, String channel, String paySource, String apiDomain);

//    /**
//     * 解析订单通知结果
//     *
//     * @param xmlData   微信支付回调信息
//     * @param paySource 支付来源
//     * @return WxPayOrderNotifyResult
//     */
//    WxPayOrderNotifyResult parseOrderNotifyResult(String xmlData, String paySource);

    /**
     * 解析支付通知结果
     * @param notifyData 微信退款回调信息
     * @param serviceKey wxPayServiceKey
     */
    WxPayOrderNotifyResult parseOrderNotifyResult(String notifyData, String serviceKey);

    /**
     * 查询微信订单
     *
     * @param outTradeNo 商户订单号
     * @param paySource  支付来源
     */
    WxPayOrderQueryResult queryOrder(String outTradeNo, String paySource);

    /**
     * 微信退款
     *
     * @param wxPayRefundRequest 微信退款请求对象
     */
    WxPayRefundResult refund(WxPayRefundRequest wxPayRefundRequest, String paySource, String apiDomain);

//    /**
//     * 解析退款通知结果
//     *
//     * @param xmlData 微信退款回调信息
//     * @param appid   微信退款回调信息中的appid
//     * @return WxPayRefundNotifyResult
//     */
//    WxPayRefundNotifyResult parseRefundNotifyResult(String xmlData, String appid);

    /**
     * 解析退款通知结果
     *
     * @param notifyData 微信退款回调信息
     * @param serviceKey wxPayServiceKey
     * @return WxPayRefundNotifyResult
     */
    WxPayRefundNotifyResult parseRefundNotifyResult(String notifyData, String serviceKey);

    /**
     * =========================================================================================================
     * 以下为V3支付处理
     * =========================================================================================================
     */

    /**
     * 创建微信订单V3
     *
     * @param wxPayUnifiedOrderV3Request 微信支付预下单对象
     * @param openid                     微信用户标识openId
     * @param channel                    支付渠道：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param paySource                  支付来源：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param apiDomain                  回调地址域名
     */
    MyRecord createV3Order(WxPayUnifiedOrderV3Request wxPayUnifiedOrderV3Request, String openid, String channel, String paySource, String apiDomain);

    /**
     * 查询微信订单V3
     *
     * @param outTradeNo 商户订单号
     * @param paySource  支付来源
     */
    WxPayOrderQueryV3Result queryOrderV3(String outTradeNo, String paySource);

    /**
     * 微信退款V3
     *
     * @param wxPayRefundV3Request 微信退款请求对象
     * @param paySource            支付来源
     */
    WxPayRefundV3Result refundV3(WxPayRefundV3Request wxPayRefundV3Request, String paySource, String apiDomain);

    /**
     * 解析支付通知结果V3
     * @param notifyData 微信退款回调信息
     * @param header 请求header信息
     * @param serviceKey wxPayServiceKey
     */
    WxPayNotifyV3Result parseOrderNotifyV3Result(String notifyData, SignatureHeader header, String serviceKey);

    /**
     * 解析退款通知结果V3
     * @param notifyData 微信退款回调信息
     * @param header 请求header信息
     * @param serviceKey wxPayServiceKey
     */
    WxPayRefundNotifyV3Result parseRefundNotifyV3Result(String notifyData, SignatureHeader header, String serviceKey);
}
