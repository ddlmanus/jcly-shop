package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.*;
import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.order.WxPayMwebOrderResult;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.*;
import com.github.binarywang.wxpay.bean.result.*;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.zbkj.common.constants.PayConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.wechat.WechatExceptions;
import com.zbkj.common.model.wechat.WechatPayInfo;
import com.zbkj.common.utils.RequestUtil;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.common.wxjava.WxPayServiceFactory;
import com.zbkj.service.service.WechatExceptionsService;
import com.zbkj.service.service.WechatPayInfoService;
import com.zbkj.service.service.WechatPayService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 微信支付服务实现
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/3/27
 */
@Service
public class WechatPayServiceImpl implements WechatPayService {

    private final Logger LOGGER = LoggerFactory.getLogger(WechatPayServiceImpl.class);

    @Autowired
    private WechatExceptionsService wechatExceptionsService;

    @Autowired
    private WxPayServiceFactory wxPayServiceFactory;

    @Autowired
    private WechatPayInfoService wechatPayInfoService;


    /**
     * 创建微信订单
     *
     * @param wxPayUnifiedOrderRequest 微信支付预下单对象
     * @param openid                   微信用户标识openId
     * @param channel                  支付渠道：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param paySource                支付来源：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param apiDomain                  回调地址域名
     */
    @Override
    public MyRecord createOrder(WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest, String openid, String channel, String paySource, String apiDomain) {
        // 根据支付渠道进行参数补充
        setTradeAndOpenid(wxPayUnifiedOrderRequest, channel, openid);
        // 保存微信预下单
        WechatPayInfo wechatPayInfo = createWechatPayInfo(wxPayUnifiedOrderRequest);
        // 根据支付来源选择使用的Service
        MyRecord record = new MyRecord();
        switch (channel) {
            case PayConstants.PAY_CHANNEL_WECHAT_MINI:
            case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
                try {
                    wxPayUnifiedOrderRequest.setNotifyUrl(apiDomain + PayConstants.WX_MA_PAY_NOTIFY_API_URI);
                    WxPayMpOrderResult payResult = wxPayServiceFactory.getService("ma").createOrder(wxPayUnifiedOrderRequest);
                    record.set("appId", payResult.getAppId());
                    record.set("nonceStr", payResult.getNonceStr());
                    record.set("package", payResult.getPackageValue());
                    record.set("signType", payResult.getSignType());
                    record.set("paySign", payResult.getPaySign());
                    record.set("timeStamp", payResult.getTimeStamp());
                } catch (WxPayException payException) {
                    LOGGER.error("微信V2小程序预下单失败！");
                    LOGGER.error(payException.getMessage(), payException);
                    wechatPayInfo.setErrCode(payException.getErrCode());
                    wechatPayInfoService.save(wechatPayInfo);
                    throw new CrmebException("微信小程序预下单失败！");
                } catch (Exception e) {
                    LOGGER.error("微信V2小程序预下单失败！");
                    LOGGER.error(e.getMessage(), e);
                    throw new CrmebException("微信小程序预下单失败！");
                }
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                try {
                    wxPayUnifiedOrderRequest.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_NOTIFY_API_URI);
                    WxPayMpOrderResult payResult = wxPayServiceFactory.getService("mp").createOrder(wxPayUnifiedOrderRequest);
                    record.set("appId", payResult.getAppId());
                    record.set("nonceStr", payResult.getNonceStr());
                    record.set("package", payResult.getPackageValue());
                    record.set("signType", payResult.getSignType());
                    record.set("paySign", payResult.getPaySign());
                    record.set("timeStamp", payResult.getTimeStamp());
                } catch (WxPayException payException) {
                    LOGGER.error("微信V2公众号预下单失败！");
                    LOGGER.error(payException.getMessage(), payException);
                    wechatPayInfo.setErrCode(payException.getErrCode());
                    wechatPayInfoService.save(wechatPayInfo);
                    throw new CrmebException("微信公众号预下单失败！");
                } catch (Exception e) {
                    LOGGER.error("微信V2公众号预下单失败！");
                    LOGGER.error(e.getMessage(), e);
                    throw new CrmebException("微信公众号预下单失败！");
                }
                break;
            case PayConstants.PAY_CHANNEL_H5:
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_MINI)) {
                    try {
                        wxPayUnifiedOrderRequest.setNotifyUrl(apiDomain + PayConstants.WX_MA_PAY_NOTIFY_API_URI);
                        WxPayMwebOrderResult mwebOrderResult = wxPayServiceFactory.getService("ma").createOrder(wxPayUnifiedOrderRequest);
                        record.set("mwebUrl", mwebOrderResult.getMwebUrl());
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V2H5预下单失败！支付渠道小程序！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信H5预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V2H5预下单失败！支付渠道小程序！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信H5预下单失败！");
                    }
                }
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC)) {
                    try {
                        wxPayUnifiedOrderRequest.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_NOTIFY_API_URI);
                        WxPayMwebOrderResult mwebOrderResult = wxPayServiceFactory.getService("mp").createOrder(wxPayUnifiedOrderRequest);
                        record.set("mwebUrl", mwebOrderResult.getMwebUrl());
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V2H5预下单失败！支付渠道公众号！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信H5预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V2H5预下单失败！支付渠道公众号！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信H5预下单失败！");
                    }
                }
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_NATIVE:
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_MINI)) {
                    try {
                        wxPayUnifiedOrderRequest.setNotifyUrl(apiDomain + PayConstants.WX_MA_PAY_NOTIFY_API_URI);
                        WxPayNativeOrderResult nativeOrderResult = wxPayServiceFactory.getService("ma").createOrder(wxPayUnifiedOrderRequest);
                        record.set("codeUrl", nativeOrderResult.getCodeUrl());
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V2Native扫码支付预下单失败！支付渠道小程序！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信V2Native扫码支付预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V2Native扫码支付预下单失败！支付渠道小程序！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信V2Native扫码支付预下单失败！");
                    }
                }
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC)) {
                    try {
                        wxPayUnifiedOrderRequest.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_NOTIFY_API_URI);
                        WxPayNativeOrderResult nativeOrderResult = wxPayServiceFactory.getService("mp").createOrder(wxPayUnifiedOrderRequest);
                        record.set("codeUrl", nativeOrderResult.getCodeUrl());
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V2Native扫码支付预下单失败！支付渠道公众号！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信V2Native扫码支付预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V2Native扫码支付预下单失败！支付渠道公众号！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信V2Native扫码支付预下单失败！");
                    }
                }
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
            case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                try {
                    wxPayUnifiedOrderRequest.setNotifyUrl(apiDomain + PayConstants.WX_APP_PAY_NOTIFY_API_URI);
                    WxPayAppOrderResult appOrderResult = wxPayServiceFactory.getService("app").createOrder(wxPayUnifiedOrderRequest);
                    record.set("appId", appOrderResult.getAppId());
                    record.set("partnerId", appOrderResult.getPartnerId());
                    record.set("prepayId", appOrderResult.getPrepayId());
                    record.set("package", appOrderResult.getPackageValue());
                    record.set("nonceStr", appOrderResult.getNonceStr());
                    record.set("timeStamp", appOrderResult.getTimeStamp());
                    record.set("sign", appOrderResult.getSign());
                } catch (WxPayException payException) {
                    LOGGER.error("微信V2APP预下单失败！");
                    LOGGER.error(payException.getMessage(), payException);
                    wechatPayInfo.setErrCode(payException.getErrCode());
                    wechatPayInfoService.save(wechatPayInfo);
                    throw new CrmebException("微信V2APP预下单失败！");
                } catch (Exception e) {
                    LOGGER.error("微信V2APP预下单失败！");
                    LOGGER.error(e.getMessage(), e);
                    throw new CrmebException("微信V2APP预下单失败！");
                }
                break;
        }
        wechatPayInfo.setErrCode("200");
        wechatPayInfoService.save(wechatPayInfo);
        return record;
    }

    /**
     * 生成微信订单表对象
     */
    private WechatPayInfo createWechatPayInfo(WxPayUnifiedOrderV3Request v3Request) {
        WechatPayInfo payInfo = new WechatPayInfo();
        payInfo.setAppId(v3Request.getAppid());
//        payInfo.setMchId(v3Request.getMchId());
//        payInfo.setDeviceInfo(v3Request.getDeviceInfo());
//        payInfo.setOpenId(v3Request.getOpenid());
//        payInfo.setNonceStr(v3Request.getNonceStr());
//        payInfo.setSign(v3Request.getSign());
//        payInfo.setSignType(v3Request.getSignType());
//        payInfo.setBody(v3Request.getBody());
//        payInfo.setDetail(v3Request.getDetail().toString());
        payInfo.setAttach(v3Request.getAttach());
        payInfo.setOutTradeNo(v3Request.getOutTradeNo());
//        payInfo.setFeeType(v3Request.getFeeType());
//        payInfo.setTotalFee(v3Request.getTotalFee());
//        payInfo.setSpbillCreateIp(v3Request.getSpbillCreateIp());
//        payInfo.setTimeStart(v3Request.getTimeStart());
//        payInfo.setTimeExpire(v3Request.getTimeExpire());
//        payInfo.setNotifyUrl(v3Request.getNotifyUrl());
//        payInfo.setTradeType(v3Request.getTradeType());
//        payInfo.setProductId(v3Request.getProductId());
//        payInfo.setSceneInfo(v3Request.getSceneInfo().toString());
        return payInfo;
    }

    /**
     * 生成微信订单表对象
     */
    private WechatPayInfo createWechatPayInfo(WxPayUnifiedOrderRequest unifiedOrderRequest) {
        WechatPayInfo payInfo = new WechatPayInfo();
        payInfo.setAppId(unifiedOrderRequest.getAppid());
        payInfo.setMchId(unifiedOrderRequest.getMchId());
        payInfo.setDeviceInfo(unifiedOrderRequest.getDeviceInfo());
        payInfo.setOpenId(unifiedOrderRequest.getOpenid());
        payInfo.setNonceStr(unifiedOrderRequest.getNonceStr());
        payInfo.setSign(unifiedOrderRequest.getSign());
        payInfo.setSignType(unifiedOrderRequest.getSignType());
        payInfo.setBody(unifiedOrderRequest.getBody());
        payInfo.setDetail(unifiedOrderRequest.getDetail());
        payInfo.setAttach(unifiedOrderRequest.getAttach());
        payInfo.setOutTradeNo(unifiedOrderRequest.getOutTradeNo());
        payInfo.setFeeType(unifiedOrderRequest.getFeeType());
        payInfo.setTotalFee(unifiedOrderRequest.getTotalFee());
        payInfo.setSpbillCreateIp(unifiedOrderRequest.getSpbillCreateIp());
        payInfo.setTimeStart(unifiedOrderRequest.getTimeStart());
        payInfo.setTimeExpire(unifiedOrderRequest.getTimeExpire());
        payInfo.setNotifyUrl(unifiedOrderRequest.getNotifyUrl());
        payInfo.setTradeType(unifiedOrderRequest.getTradeType());
        payInfo.setProductId(unifiedOrderRequest.getProductId());
        payInfo.setSceneInfo(unifiedOrderRequest.getSceneInfo());
        return payInfo;
    }

    /**
     * 根据不同的支付渠道，设置交易类型及微信用户openid标识
     */
    private void setTradeAndOpenid(WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest, String channel, String openid) {
        switch (channel) {
            case PayConstants.PAY_CHANNEL_H5:
                wxPayUnifiedOrderRequest.setTradeType(PayConstants.WX_PAY_TRADE_TYPE_H5);
                wxPayUnifiedOrderRequest.setOpenid(null);
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
            case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                wxPayUnifiedOrderRequest.setTradeType(PayConstants.WX_PAY_TRADE_TYPE_APP);
                wxPayUnifiedOrderRequest.setOpenid(null);
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_NATIVE:
                wxPayUnifiedOrderRequest.setTradeType(PayConstants.WX_PAY_TRADE_TYPE_NATIVE);
                wxPayUnifiedOrderRequest.setOpenid(null);
                break;
            default:
                wxPayUnifiedOrderRequest.setTradeType(PayConstants.WX_PAY_TRADE_TYPE_JS);
                wxPayUnifiedOrderRequest.setOpenid(openid);
        }
    }

    /**
     * 解析支付通知结果
     * @param notifyData 微信退款回调信息
     * @param serviceKey wxPayServiceKey
     */
    @Override
    public WxPayOrderNotifyResult parseOrderNotifyResult(String notifyData, String serviceKey) {
        WxPayOrderNotifyResult wxPayOrderNotifyResult;
        try {
            wxPayOrderNotifyResult = wxPayServiceFactory.getService(serviceKey).parseOrderNotifyResult(notifyData);
        } catch (Exception e) {
            LOGGER.error("微信支付回调解析订单通知结果失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("微信支付回调解析订单通知结果失败");
        }
        return wxPayOrderNotifyResult;
    }

    /**
     * 查询微信订单
     *
     * @param outTradeNo 商户订单号
     * @param paySource  支付来源
     */
    @Override
    public WxPayOrderQueryResult queryOrder(String outTradeNo, String paySource) {
        WxPayOrderQueryRequest request = new WxPayOrderQueryRequest();
        request.setOutTradeNo(StringUtils.trimToNull(outTradeNo));
        WxPayOrderQueryResult queryResult = new WxPayOrderQueryResult();
        try {
            switch (paySource) {
                case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                    queryResult = wxPayServiceFactory.getService("mp").queryOrder(request);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_MINI:
                case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
                    queryResult = wxPayServiceFactory.getService("ma").queryOrder(request);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
                case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                    queryResult = wxPayServiceFactory.getService("app").queryOrder(request);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("查询微信订单失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("查询微信订单失败，outTradeNo = " + outTradeNo);
        }
        checkQueryResult(queryResult);
        return queryResult;
    }

    /**
     * 微信退款
     *
     * @param wxPayRefundRequest 微信退款请求对象
     */
    @Override
    public WxPayRefundResult refund(WxPayRefundRequest wxPayRefundRequest, String paySource, String apiDomain) {
        WxPayRefundResult refundResult = new WxPayRefundResult();
        try {
            switch (paySource) {
                case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                    wxPayRefundRequest.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_REFUND_NOTIFY_API_URI);
                    refundResult = wxPayServiceFactory.getService("mp").refundV2(wxPayRefundRequest);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_MINI:
                case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
                    wxPayRefundRequest.setNotifyUrl(apiDomain +"/admin-api"+ PayConstants.WX_MA_PAY_REFUND_NOTIFY_API_URI);
                    refundResult = wxPayServiceFactory.getService("ma").refundV2(wxPayRefundRequest);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
                case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                    wxPayRefundRequest.setNotifyUrl(apiDomain + PayConstants.WX_APP_PAY_REFUND_NOTIFY_API_URI);
                    refundResult = wxPayServiceFactory.getService("app").refundV2(wxPayRefundRequest);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("微信V2退款失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("微信V2退款失败，outTradeNo = " + wxPayRefundRequest.getOutTradeNo());
        }
        checkRefundResult(refundResult);
        return refundResult;
    }

    /**
     * 解析退款通知结果
     *
     * @param notifyData 微信退款回调信息
     * @param serviceKey wxPayServiceKey
     * @return WxPayRefundNotifyResult
     */
    @Override
    public WxPayRefundNotifyResult parseRefundNotifyResult(String notifyData, String serviceKey) {
        WxPayRefundNotifyResult wxPayRefundNotifyResult;
        try {
            wxPayRefundNotifyResult = wxPayServiceFactory.getService(serviceKey).parseRefundNotifyResult(notifyData);
        } catch (Exception e) {
            LOGGER.error("微信退款回调解析订单通知结果失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("微信退款回调解析退款通知结果失败");
        }
        return wxPayRefundNotifyResult;
    }

    private void checkRefundResult(WxPayRefundResult refundResult) {
        if (refundResult.getReturnCode().equalsIgnoreCase("FAIL")) {
            wxPayRefundExceptionDispose(refundResult, "微信退款通信异常");
            throw new CrmebException("微信退款失败1！" + refundResult.getReturnMsg());
        }

        if (refundResult.getResultCode().equalsIgnoreCase("FAIL")) {
            wxPayRefundExceptionDispose(refundResult, "微信支付查询订单结果异常");
            throw new CrmebException("微信退款失败2！" + refundResult.getErrCode() + refundResult.getErrCodeDes());
        }
    }

    private void checkQueryResult(WxPayOrderQueryResult queryResult) {
        if (queryResult.getReturnCode().equalsIgnoreCase("FAIL")) {
            wxPayQueryExceptionDispose(queryResult, "微信支付查询订单通信异常");
            throw new CrmebException("微信订单查询失败1！" + queryResult.getReturnMsg());
        }

        if (queryResult.getResultCode().equalsIgnoreCase("FAIL")) {
            wxPayQueryExceptionDispose(queryResult, "微信支付查询订单结果异常");
            throw new CrmebException("微信订单查询失败2！" + queryResult.getErrCode() + queryResult.getErrCodeDes());
        }
    }

    /**
     * 微信支付查询异常处理
     *
     * @param queryResult 微信返回数据
     * @param remark      备注
     */
    private void wxPayQueryExceptionDispose(WxPayOrderQueryResult queryResult, String remark) {
        WechatExceptions wechatExceptions = new WechatExceptions();
        if (queryResult.getReturnCode().equalsIgnoreCase("FAIL")) {
            wechatExceptions.setErrcode("-200");
            wechatExceptions.setErrmsg(queryResult.getReturnMsg());
        } else if (queryResult.getResultCode().equalsIgnoreCase("FAIL")) {
            wechatExceptions.setErrcode(queryResult.getErrCode());
            wechatExceptions.setErrmsg(queryResult.getErrCodeDes());
        } else if (!queryResult.getTradeState().equalsIgnoreCase("SUCCESS")) {
            wechatExceptions.setErrcode("-201");
            wechatExceptions.setErrmsg(queryResult.getTradeState());
        }
        wechatExceptions.setData(JSONObject.toJSONString(queryResult));
        wechatExceptions.setRemark(remark);
        wechatExceptions.setCreateTime(DateUtil.date());
        wechatExceptions.setUpdateTime(DateUtil.date());
        wechatExceptionsService.save(wechatExceptions);
    }

    /**
     * 微信支付查询异常处理
     *
     * @param refundResult 微信返回数据
     * @param remark       备注
     */
    private void wxPayRefundExceptionDispose(WxPayRefundResult refundResult, String remark) {
        WechatExceptions wechatExceptions = new WechatExceptions();
        if (refundResult.getReturnCode().equalsIgnoreCase("FAIL")) {
            wechatExceptions.setErrcode("-100");
            wechatExceptions.setErrmsg(refundResult.getReturnMsg());
        } else if (refundResult.getResultCode().equalsIgnoreCase("FAIL")) {
            wechatExceptions.setErrcode(refundResult.getErrCode());
            wechatExceptions.setErrmsg(refundResult.getErrCodeDes());
        }
        wechatExceptions.setData(JSONObject.toJSONString(refundResult));
        wechatExceptions.setRemark(remark);
        wechatExceptions.setCreateTime(DateUtil.date());
        wechatExceptions.setUpdateTime(DateUtil.date());
        wechatExceptionsService.save(wechatExceptions);
    }

    /**
     * 创建微信订单V3
     *
     * @param wxPayUnifiedOrderV3Request 微信支付预下单对象
     * @param openid                     微信用户标识openId
     * @param channel                    支付渠道：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param paySource                  支付来源：public-公众号,mini-小程序，h5-网页支付，wechatIos-微信Ios，wechatAndroid-微信Android
     * @param apiDomain                  回调地址域名
     */
    @Override
    public MyRecord createV3Order(WxPayUnifiedOrderV3Request wxPayUnifiedOrderV3Request, String openid, String channel, String paySource, String apiDomain) {
        WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
        payer.setOpenid(openid);
        wxPayUnifiedOrderV3Request.setPayer(payer);
        // 保存微信预下单
        WechatPayInfo wechatPayInfo = createWechatPayInfo(wxPayUnifiedOrderV3Request);
        // 根据支付来源选择使用的Service
        MyRecord record = new MyRecord();
        switch (channel) {
            case PayConstants.PAY_CHANNEL_WECHAT_MINI:
            case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
                try {
                    wxPayUnifiedOrderV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MA_PAY_NOTIFY_API_URI);
                    WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = wxPayServiceFactory.getService("ma").createOrderV3(TradeTypeEnum.JSAPI, wxPayUnifiedOrderV3Request);
                    record.set("appId", jsapiResult.getAppId());
                    record.set("nonceStr", jsapiResult.getNonceStr());
                    record.set("package", jsapiResult.getPackageValue());
                    record.set("signType", jsapiResult.getSignType());
                    record.set("paySign", jsapiResult.getPaySign());
                    record.set("timeStamp", jsapiResult.getTimeStamp());
                } catch (WxPayException payException) {
                    LOGGER.error("微信V3小程序预下单失败！");
                    LOGGER.error(payException.getMessage(), payException);
                    wechatPayInfo.setErrCode(payException.getErrCode());
                    wechatPayInfoService.save(wechatPayInfo);
                    throw new CrmebException("微信小程序预下单失败！");
                } catch (Exception e) {
                    LOGGER.error("微信V3小程序预下单失败！");
                    LOGGER.error(e.getMessage(), e);
                    throw new CrmebException("微信小程序预下单失败！");
                }
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                try {
                    wxPayUnifiedOrderV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_NOTIFY_API_URI);
                    WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = wxPayServiceFactory.getService("mp").createOrderV3(TradeTypeEnum.JSAPI, wxPayUnifiedOrderV3Request);
                    record.set("appId", jsapiResult.getAppId());
                    record.set("nonceStr", jsapiResult.getNonceStr());
                    record.set("package", jsapiResult.getPackageValue());
                    record.set("signType", jsapiResult.getSignType());
                    record.set("paySign", jsapiResult.getPaySign());
                    record.set("timeStamp", jsapiResult.getTimeStamp());
                } catch (WxPayException payException) {
                    LOGGER.error("微信V3公众号预下单失败！");
                    LOGGER.error(payException.getMessage(), payException);
                    wechatPayInfo.setErrCode(payException.getErrCode());
                    wechatPayInfoService.save(wechatPayInfo);
                    throw new CrmebException("微信公众号预下单失败！");
                } catch (Exception e) {
                    LOGGER.error("微信V3公众号预下单失败！");
                    LOGGER.error(e.getMessage(), e);
                    throw new CrmebException("微信公众号预下单失败！");
                }
                break;
            case PayConstants.PAY_CHANNEL_H5:
                WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
                sceneInfo.setPayerClientIp(RequestUtil.getClientIp());
                WxPayUnifiedOrderV3Request.H5Info h5Info = new WxPayUnifiedOrderV3Request.H5Info();
                // 场景类型，使用H5支付的场景：Wap、iOS、Android
                h5Info.setType("Wap");
                sceneInfo.setH5Info(h5Info);
                wxPayUnifiedOrderV3Request.setSceneInfo(sceneInfo);
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_MINI)) {
                    try {
                        wxPayUnifiedOrderV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MA_PAY_NOTIFY_API_URI);
                        String h5Url = wxPayServiceFactory.getService("ma").createOrderV3(TradeTypeEnum.H5, wxPayUnifiedOrderV3Request);
                        record.set("mwebUrl", h5Url);
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V3H5预下单失败！支付渠道小程序！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信H5预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V3H5预下单失败！支付渠道小程序！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信H5预下单失败！");
                    }
                }
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC)) {
                    try {
                        wxPayUnifiedOrderV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_NOTIFY_API_URI);
                        String h5Url = wxPayServiceFactory.getService("mp").createOrderV3(TradeTypeEnum.H5, wxPayUnifiedOrderV3Request);
                        record.set("mwebUrl", h5Url);
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V3H5预下单失败！支付渠道公众号！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信H5预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V3H5预下单失败！支付渠道公众号！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信H5预下单失败！");
                    }
                }
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_NATIVE:
                wxPayUnifiedOrderV3Request.setPayer(null);
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_MINI)) {
                    try {
                        wxPayUnifiedOrderV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MA_PAY_NOTIFY_API_URI);
                        String codeUrl = wxPayServiceFactory.getService("ma").createOrderV3(TradeTypeEnum.NATIVE, wxPayUnifiedOrderV3Request);
                        record.set("codeUrl", codeUrl);
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V3Native扫码支付预下单失败！支付渠道小程序！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信V3Native扫码支付预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V3Native扫码支付预下单失败！支付渠道小程序！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信V3Native扫码支付预下单失败！");
                    }
                }
                if (paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC)) {
                    try {
                        wxPayUnifiedOrderV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_NOTIFY_API_URI);
                        String codeUrl = wxPayServiceFactory.getService("mp").createOrderV3(TradeTypeEnum.NATIVE, wxPayUnifiedOrderV3Request);
                        record.set("codeUrl", codeUrl);
                    } catch (WxPayException payException) {
                        LOGGER.error("微信V3Native扫码支付预下单失败！支付渠道公众号！");
                        LOGGER.error(payException.getMessage(), payException);
                        wechatPayInfo.setErrCode(payException.getErrCode());
                        wechatPayInfoService.save(wechatPayInfo);
                        throw new CrmebException("微信V3Native扫码支付预下单失败！");
                    } catch (Exception e) {
                        LOGGER.error("微信V3Native扫码支付预下单失败！支付渠道公众号！");
                        LOGGER.error(e.getMessage(), e);
                        throw new CrmebException("微信V3Native扫码支付预下单失败！");
                    }
                }
                break;
            case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
            case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                wxPayUnifiedOrderV3Request.setPayer(null);
                try {
                    wxPayUnifiedOrderV3Request.setNotifyUrl(apiDomain + PayConstants.WX_APP_PAY_NOTIFY_API_URI);
                    WxPayUnifiedOrderV3Result.AppResult appResult = wxPayServiceFactory.getService("app").createOrderV3(TradeTypeEnum.APP, wxPayUnifiedOrderV3Request);
                    record.set("appId", appResult.getAppid());
                    record.set("partnerId", appResult.getPartnerId());
                    record.set("prepayId", appResult.getPrepayId());
                    record.set("package", appResult.getPackageValue());
                    record.set("nonceStr", appResult.getNoncestr());
                    record.set("timeStamp", appResult.getTimestamp());
                    record.set("sign", appResult.getSign());
                } catch (WxPayException payException) {
                    LOGGER.error("微信V3APP预下单失败！");
                    LOGGER.error(payException.getMessage(), payException);
                    wechatPayInfo.setErrCode(payException.getErrCode());
                    wechatPayInfoService.save(wechatPayInfo);
                    throw new CrmebException("微信V3APP预下单失败！");
                } catch (Exception e) {
                    LOGGER.error("微信V3APP预下单失败！");
                    LOGGER.error(e.getMessage(), e);
                    throw new CrmebException("微信V3APP预下单失败！");
                }
                break;
        }
        wechatPayInfo.setErrCode("200");
        wechatPayInfoService.save(wechatPayInfo);
        return record;
    }

    /**
     * 查询微信订单V3
     *
     * @param outTradeNo 商户订单号
     * @param paySource  支付来源
     */
    @Override
    public WxPayOrderQueryV3Result queryOrderV3(String outTradeNo, String paySource) {
        WxPayOrderQueryV3Request queryV3Request = new WxPayOrderQueryV3Request();
        queryV3Request.setOutTradeNo(StringUtils.trimToNull(outTradeNo));
        WxPayOrderQueryV3Result queryV3Result = new WxPayOrderQueryV3Result();
        try {
            switch (paySource) {
                case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                    queryV3Result = wxPayServiceFactory.getService("mp").queryOrderV3(queryV3Request);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_MINI:
                case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
                    queryV3Result = wxPayServiceFactory.getService("ma").queryOrderV3(queryV3Request);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
                case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                    queryV3Result = wxPayServiceFactory.getService("app").queryOrderV3(queryV3Request);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("查询微信订单失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("查询微信订单失败，outTradeNo = " + outTradeNo);
        }
        return queryV3Result;
    }

    /**
     * 微信退款
     *
     * @param wxPayRefundV3Request 微信退款请求对象
     * @param paySource            支付来源
     */
    @Override
    public WxPayRefundV3Result refundV3(WxPayRefundV3Request wxPayRefundV3Request, String paySource, String apiDomain) {
        WxPayRefundV3Result refundV3Result = new WxPayRefundV3Result();
        try {
            switch (paySource) {
                case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                    wxPayRefundV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MP_PAY_REFUND_NOTIFY_API_URI);
                    refundV3Result = wxPayServiceFactory.getService("mp").refundV3(wxPayRefundV3Request);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_MINI:
                case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
                    wxPayRefundV3Request.setNotifyUrl(apiDomain + PayConstants.WX_MA_PAY_REFUND_NOTIFY_API_URI);
                    refundV3Result = wxPayServiceFactory.getService("ma").refundV3(wxPayRefundV3Request);
                    break;
                case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
                case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                    wxPayRefundV3Request.setNotifyUrl(apiDomain + PayConstants.WX_APP_PAY_REFUND_NOTIFY_API_URI);
                    refundV3Result = wxPayServiceFactory.getService("app").refundV3(wxPayRefundV3Request);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("微信V3退款失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("微信V3退款失败，outTradeNo = " + wxPayRefundV3Request.getOutTradeNo());
        }
        return refundV3Result;
    }

    /**
     * 解析支付通知结果V3
     * @param notifyData 微信退款回调信息
     * @param header 请求header信息
     * @param serviceKey wxPayServiceKey
     */
    @Override
    public WxPayNotifyV3Result parseOrderNotifyV3Result(String notifyData, SignatureHeader header, String serviceKey) {
        try {
            return wxPayServiceFactory.getService(serviceKey).parseOrderNotifyV3Result(notifyData, header);
        } catch (Exception e) {
            LOGGER.error("微信解析支付通知结果V3失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("微信支付回调解析订单通知结果失败");
        }
    }

    /**
     * 解析退款通知结果V3
     * @param notifyData 微信退款回调信息
     * @param header 请求header信息
     * @param serviceKey wxPayServiceKey
     */
    @Override
    public WxPayRefundNotifyV3Result parseRefundNotifyV3Result(String notifyData, SignatureHeader header, String serviceKey) {
        try {
            return wxPayServiceFactory.getService(serviceKey).parseRefundNotifyV3Result(notifyData, header);
        } catch (Exception e) {
            LOGGER.error("微信解析退款通知结果V3失败");
            LOGGER.error(e.getMessage(), e);
            throw new CrmebException("微信退款回调解析订单通知结果失败");
        }
    }
}
