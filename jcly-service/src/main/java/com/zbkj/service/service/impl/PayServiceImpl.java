package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.zbkj.common.config.CrmebConfig;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.alipay.AliPayInfo;
import com.zbkj.common.model.bill.Bill;
import com.zbkj.common.model.bill.MerchantBill;
import com.zbkj.common.model.coupon.CouponUser;
import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.member.PaidMemberOrder;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.merchant.MerchantBalanceRecord;
import com.zbkj.common.model.order.*;
import com.zbkj.common.model.product.ProductCoupon;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.model.user.*;
import com.zbkj.common.model.wechat.WechatPayInfo;
import com.zbkj.common.model.wechat.video.PayComponentProduct;
import com.zbkj.service.service.UnionPayService;
import com.zbkj.service.service.UserBankCardService;
import com.zbkj.common.request.OrderPayRequest;
import com.zbkj.common.request.WechatPaymentQueryRequest;
import com.zbkj.common.response.CashierInfoResponse;
import com.zbkj.common.response.OrderPayResultResponse;
import com.zbkj.common.response.PayConfigResponse;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.result.MemberResultCode;
import com.zbkj.common.result.OrderResultCode;
import com.zbkj.common.result.UserResultCode;
import com.zbkj.common.utils.*;
import com.zbkj.common.vo.*;
import com.zbkj.common.vo.wxvedioshop.ShopOrderAddResultVo;
import com.zbkj.common.vo.wxvedioshop.order.*;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PayServiceImpl 接口实现
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
public class PayServiceImpl implements PayService {

    private static final Logger logger = LoggerFactory.getLogger(PayServiceImpl.class);

    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private UserTokenService userTokenService;
    @Autowired
    private WechatPayInfoService wechatPayInfoService;
    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private MerchantOrderService merchantOrderService;
    @Autowired
    private OrderStatusService orderStatusService;
    @Autowired
    private UserIntegralRecordService userIntegralRecordService;
    @Autowired
    private SystemNotificationService systemNotificationService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProductCouponService productCouponService;
    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;
    @Autowired
    private UserBalanceRecordService userBalanceRecordService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MerchantBillService merchantBillService;
    @Autowired
    private OrderProfitSharingService orderProfitSharingService;
    @Autowired
    private BillService billService;
    @Autowired
    private WechatVideoOrderService wechatVideoOrderService;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private PayComponentOrderService payComponentOrderService;
    @Autowired
    private PayComponentProductService payComponentProductService;
    @Autowired
    private AliPayInfoService aliPayInfoService;
    @Autowired
    private UnionPayService unionPayService;
    @Autowired
    private UserBankCardService userBankCardService;
    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Autowired
    private MerchantBalanceRecordService merchantBalanceRecordService;
    @Autowired
    private MerchantPrintService merchantPrintService;
    @Autowired
    private CrmebConfig crmebConfig;
    @Autowired
    private SystemAdminService systemAdminService;
    @Autowired
    private PaidMemberOrderService paidMemberOrderService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private WechatPayService wechatPayService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberIntegralRuleService memberIntegralRuleService;
    @Autowired
    private com.zbkj.service.util.DataChangeEventPublisher dataChangeEventPublisher;

    /**
     * 获取支付配置
     */
    @Override
    public PayConfigResponse getPayConfig() {
        String payWxOpen = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_PAY_WECHAT_OPEN);
        String yuePayStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_YUE_PAY_STATUS);
        String aliPayStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_ALI_PAY_STATUS);
        String unionPayStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_UNION_PAY_STATUS);
        PayConfigResponse response = new PayConfigResponse();
        response.setYuePayStatus(Constants.CONFIG_FORM_SWITCH_OPEN.equals(yuePayStatus));
        response.setPayWechatOpen(Constants.CONFIG_FORM_SWITCH_OPEN.equals(payWxOpen));
        response.setAliPayStatus(Constants.CONFIG_FORM_SWITCH_OPEN.equals(aliPayStatus));
        response.setUnionPayStatus(Constants.CONFIG_FORM_SWITCH_OPEN.equals(unionPayStatus));
        if (Constants.CONFIG_FORM_SWITCH_OPEN.equals(yuePayStatus)) {
            User user = userService.getInfo();
            response.setUserBalance(user.getNowMoney());
        }
        return response;
    }

    /**
     * 订单支付
     *
     * @param orderPayRequest 订单支付参数
     * @return OrderPayResultResponse
     */
    @Override
    public OrderPayResultResponse payment(OrderPayRequest orderPayRequest) {
        Order order = orderService.getByOrderNo(orderPayRequest.getOrderNo());
        if (order.getCancelStatus() > OrderConstants.ORDER_CANCEL_STATUS_NORMAL) {
            throw new CrmebException(OrderResultCode.ORDER_CANCEL);
        }
        if (order.getPaid()) {
            throw new CrmebException(OrderResultCode.ORDER_PAID);
        }
        if (order.getStatus() > OrderConstants.ORDER_STATUS_WAIT_PAY) {
            throw new CrmebException(OrderResultCode.ORDER_STATUS_ABNORMAL);
        }
        User user = userService.getInfo();
        // 根据支付类型进行校验,更换支付类型
        order.setPayType(orderPayRequest.getPayType());
        order.setPayChannel(orderPayRequest.getPayChannel());
        // 获取过期时间 增加redis缓存的判断 2025-02-12
        DateTime cancelTime;
        if (redisUtil.exists(redisUtil.get(StrUtil.format(RedisConstants.ORDER_EXPIRE_TIME, order.getOrderNo())))) {
            String expireTime = redisUtil.get(StrUtil.format(RedisConstants.ORDER_EXPIRE_TIME, order.getOrderNo()));
            cancelTime = DateUtil.parseDateTime(expireTime);
        } else {
            cancelTime = DateUtil.offset(order.getCreateTime(), DateField.MINUTE, crmebConfig.getOrderCancelTime());
        }
        long between = DateUtil.between(cancelTime, DateUtil.date(), DateUnit.SECOND, false);
        if (between > 0) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "订单已过期");
        }

        // 余额支付
        if (orderPayRequest.getPayType().equals(PayConstants.PAY_TYPE_YUE)) {
            if (user.getNowMoney().compareTo(order.getPayPrice()) < 0) {
                throw new CrmebException(UserResultCode.USER_BALANCE_INSUFFICIENT);
            }
        }

        OrderPayResultResponse response = new OrderPayResultResponse();
        response.setOrderNo(order.getOrderNo());
        response.setPayType(order.getPayType());
        response.setPayChannel(order.getPayChannel());
        if (!order.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_INTEGRAL)) {
            if (order.getPayPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "支付金额不能低于等于0元");
            }
        }
        // 余额支付
        if (order.getPayType().equals(PayConstants.PAY_TYPE_YUE) || order.getPayPrice().compareTo(BigDecimal.ZERO) <= 0) {
            Boolean yueBoolean = yuePay(order, user);
            response.setStatus(yueBoolean);
            return response;
        }

        // 微信视频号下单 需要额外调用支付参数
        if (order.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO)) {
//            WxPayJsResultVo vo = new WxPayJsResultVo();
//            UserToken tokenByUser = userTokenService.getTokenByUserId(user.getId(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
//            List<OrderDetail> orderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
//            //  视频号下单都是单品
//            PayComponentProduct payComponentProduct = payComponentProductService.getById(orderDetailList.get(0).getProductId());
//            //组装商品信息
//            ShopOrderDetailAddVo shopOrderDetailAddVo = new ShopOrderDetailAddVo();
//            List<ShopOrderProductInfoAddVo> shopOrderProductInfoAddVoList = new ArrayList<>();
//            for (OrderDetail orderDetail : orderDetailList) {
//                ShopOrderProductInfoAddVo shopOrderProductInfoAddVo = new ShopOrderProductInfoAddVo();
//                shopOrderProductInfoAddVo.setOutProductId(orderDetail.getProductId().toString());
//                shopOrderProductInfoAddVo.setOutSkuId(orderDetail.getAttrValueId().toString());
//                shopOrderProductInfoAddVo.setProductCnt(orderDetail.getPayNum());
//                shopOrderProductInfoAddVo.setSalePrice(orderDetail.getPrice().multiply(new BigDecimal("100")).longValue());
//                shopOrderProductInfoAddVo.setSkuRealPrice(orderDetail.getPayPrice().multiply(new BigDecimal("100")).longValue());
//                shopOrderProductInfoAddVo.setPath(payComponentProduct.getPath());
//                shopOrderProductInfoAddVo.setTitle(payComponentProduct.getTitle());
//                shopOrderProductInfoAddVo.setHeadImg(payComponentProduct.getHeadImg());
//                shopOrderProductInfoAddVoList.add(shopOrderProductInfoAddVo);
//            }
//            shopOrderDetailAddVo.setProductInfos(shopOrderProductInfoAddVoList);
//
//            // 组装支付方式
//            ShopOrderPayInfoAddVo payInfoAddVo = new ShopOrderPayInfoAddVo();
//            payInfoAddVo.setPayMethodType(0);
//            payInfoAddVo.setPayMethod("微信支付"); // 视频号暂时只有微信支付
//            shopOrderDetailAddVo.setPayInfo(payInfoAddVo);
//
//            // 组装支付详细信息
//            ShopOrderPriceInfoVo shopOrderPriceInfoVo = new ShopOrderPriceInfoVo();
//            shopOrderPriceInfoVo.setOrderPrice(order.getPayPrice().multiply(new BigDecimal("100")).longValue());
//            // 视频号商品暂时面部免运费
//            shopOrderPriceInfoVo.setFreight(0L);
//            shopOrderPriceInfoVo.setDiscountedPrice(0L);
//            shopOrderPriceInfoVo.setAdditionalPrice(0L);
//            shopOrderPriceInfoVo.setAdditional_remarks(null);
//            shopOrderDetailAddVo.setPriceInfo(shopOrderPriceInfoVo);
//
//            // 组装自定义交易组件主体数据
//            ShopOrderAddVo shopOrderAddVo = new ShopOrderAddVo();
//            shopOrderAddVo.setOrderDetail(shopOrderDetailAddVo);
//            shopOrderAddVo.setFund_type(1);
//            shopOrderAddVo.setTrace_id(null);
//            shopOrderAddVo.setOutOrderId(order.getOrderNo());
//            shopOrderAddVo.setOpenid(tokenByUser.getToken());
//            shopOrderAddVo.setOutUserId(user.getId());
//            shopOrderAddVo.setMerId(order.getMerId());
//
//            // 订单路由地址
//            shopOrderAddVo.setPath("/pages/users/order_details/index?orderNo=" + orderPayRequest.getOrderNo());
//
//            // 组装订单信息 视频号都是单品 最多是多量
//            List<MerchantOrder> merchantOrderList = merchantOrderService.getByOrderNo(orderPayRequest.getOrderNo());
//            if (ObjectUtil.isNull(merchantOrderList) || merchantOrderList.size() == 0) {
//                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "未找到视频号商品订单");
//            }
//            MerchantOrder CurrentMerchantOrder = merchantOrderList.get(0);
//            ShopOrderAddressInfoAddVo shopOrderAddressInfoAddVo = new ShopOrderAddressInfoAddVo();
//            shopOrderAddressInfoAddVo.setReceiverName(CurrentMerchantOrder.getRealName());
//            shopOrderAddressInfoAddVo.setDetailedAddress(CurrentMerchantOrder.getUserAddress());
//            shopOrderAddressInfoAddVo.setTelNumber(CurrentMerchantOrder.getUserPhone());
//            shopOrderAddVo.setAddressInfo(shopOrderAddressInfoAddVo);
//
//            // 1: 正常快递, 2: 无需快递, 3: 线下配送, 4: 用户自提，视频号场景目前只支持 1，正常快递
//            ShopOrderDeliveryDetailAddVo shopOrderDeliveryDetailAddVo = new ShopOrderDeliveryDetailAddVo();
//            shopOrderDeliveryDetailAddVo.setDeliveryType(1);
//
//            shopOrderAddVo.setDeliveryDetail(shopOrderDeliveryDetailAddVo);
//            shopOrderAddVo.setExpire_time(WxPayUtil.getCurrentTimestamp().intValue() + 900);
//            shopOrderAddVo.setCreateTime(CrmebDateUtil.nowDateTimeStr());
//            ShopOrderAddResultVo shopOrderAddResultVo = payComponentOrderService.create(shopOrderAddVo);
//            ShopOrderGetPaymentParamsRequestVo videoPaymentRequestVo = new ShopOrderGetPaymentParamsRequestVo(
//                    shopOrderAddResultVo.getOutOrderId(), order.getOrderNo(), tokenByUser.getToken()
//            );
//            ShopOrderGetPaymentParamsRersponseVo shopOrderGetPaymentParamsRersponseVo = wechatVideoOrderService.shopOrderGetPaymentParams(videoPaymentRequestVo);
//            response.setStatus(true);
//            vo.setTimeStamp(WxPayUtil.getCurrentTimestamp() + "");
//            vo.setNonceStr(shopOrderGetPaymentParamsRersponseVo.getNonceStr());
//            vo.setPackages(shopOrderGetPaymentParamsRersponseVo.get_package());
//            vo.setPaySign(shopOrderGetPaymentParamsRersponseVo.getPaySign());
//            vo.setSignType(shopOrderGetPaymentParamsRersponseVo.getSignType());
//            response.setJsConfig(vo);
            return response;
        }
        // 微信支付，调用微信预下单，返回拉起微信支付需要的信息
        if (order.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            WxPayJsResultVo vo = wechatPayment(order);
            order.setUpdateTime(DateUtil.date());
            orderService.updateById(order);
            response.setStatus(true);
            response.setJsConfig(vo);
            return response;
        }
        if (order.getPayType().equals(PayConstants.PAY_TYPE_ALI_PAY)) {
            String result = aliPayment(order);
            order.setOutTradeNo(order.getOrderNo());
            order.setUpdateTime(DateUtil.date());
            orderService.updateById(order);
            response.setStatus(true);
            response.setAlipayRequest(result);
            return response;
        }
        if (order.getPayType().equals(PayConstants.PAY_TYPE_UNION_PAY)) {
            WxPayJsResultVo vo = wechatPayment(order);
            order.setUpdateTime(DateUtil.date());
            orderService.updateById(order);
            response.setStatus(true);
            response.setJsConfig(vo);
            return response;
        }
        throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "未知的支付方式");
    }

    /**
     * 查询支付结果
     *
     * @param orderNo 订单编号
     */
    @Override
    public Boolean queryWechatPayResult(String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (ObjectUtil.isNull(order)) {
            throw new CrmebException(OrderResultCode.ORDER_NOT_EXIST);
        }
        if (order.getCancelStatus() > OrderConstants.ORDER_CANCEL_STATUS_NORMAL) {
            throw new CrmebException(OrderResultCode.ORDER_CANCEL);
        }
        if (!order.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "不是微信支付类型订单");
        }
        if (order.getPaid()) {
            return Boolean.TRUE;
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(order.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "未找到对应微信订单");
        }
        String paySource = getWechatPaySource(order.getPayChannel());
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayOrderQueryResult queryResult = wechatPayService.queryOrder(order.getOutTradeNo(), paySource);
            if (!queryResult.getTradeState().equals("SUCCESS")) {
                return Boolean.FALSE;
            }
            wechatPayInfo.setIsSubscribe(queryResult.getIsSubscribe());
            wechatPayInfo.setTradeState(queryResult.getTradeState());
            wechatPayInfo.setBankType(queryResult.getBankType());
            wechatPayInfo.setCashFee(queryResult.getCashFee());
            wechatPayInfo.setCouponFee(queryResult.getCouponFee());
            wechatPayInfo.setTransactionId(queryResult.getTransactionId());
            wechatPayInfo.setTimeEnd(queryResult.getTimeEnd());
            wechatPayInfo.setTradeStateDesc(queryResult.getTradeStateDesc());
        } else {
            WxPayOrderQueryV3Result queryV3Result = wechatPayService.queryOrderV3(order.getOutTradeNo(), paySource);
            if (!queryV3Result.getTradeState().equals("SUCCESS")) {
                return Boolean.FALSE;
            }
            wechatPayInfo.setTradeState(queryV3Result.getTradeState());
            wechatPayInfo.setBankType(queryV3Result.getBankType());
            wechatPayInfo.setTransactionId(queryV3Result.getTransactionId());
            wechatPayInfo.setTimeEnd(queryV3Result.getSuccessTime());
            wechatPayInfo.setTradeStateDesc(queryV3Result.getTradeStateDesc());
        }
        Boolean updatePaid = transactionTemplate.execute(e -> {
            orderService.updatePaid(orderNo);
            wechatPayInfoService.updateById(wechatPayInfo);
            return Boolean.TRUE;
        });
        if (!updatePaid) {
            throw new CrmebException("支付成功更新订单失败");
        }
        asyncService.orderPaySuccessSplit(order.getOrderNo());
        return Boolean.TRUE;
    }

    private String getWechatPaySource(String payChannel) {
        String paySource;
        if (payChannel.equals(PayConstants.PAY_CHANNEL_H5) || payChannel.equals(PayConstants.PAY_CHANNEL_WECHAT_NATIVE)) {
            String source = systemConfigService.getValueByKey(SysConfigConstants.WECHAT_PAY_SOURCE_H5_PC);
            if (StrUtil.isNotBlank(source) && source.equals(PayConstants.WECHAT_PAY_SOURCE_MINI)) {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_MINI;
            } else {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_PUBLIC;
            }
        } else {
            paySource = payChannel;
        }
        return paySource;
    }

    /**
     * 查询订单支付宝支付结果
     *
     * @param orderNo 订单编号
     */
    @Override
    public Boolean queryAliPayResult(String orderNo) {
        AliPayInfo aliPayInfo = aliPayInfoService.getByOutTradeNo(orderNo);
        if (ObjectUtil.isNull(aliPayInfo)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "支付宝订单信息不存在");
        }
        String passbackParams = aliPayInfo.getPassbackParams();
        if (StrUtil.isBlank(passbackParams)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "未知的支付宝订单类型");
        }
        String decode;
        try {
            decode = URLDecoder.decode(passbackParams, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new CrmebException("ali pay query error : 订单支付类型解码失败==》" + orderNo);
        }

        String[] split = decode.split("=");
        String orderType = split[1];
        if (PayConstants.PAY_SERVICE_TYPE_RECHARGE.equals(orderType)) {// 充值订单
            RechargeOrder rechargeOrder = rechargeOrderService.getByOutTradeNo(orderNo);
            if (ObjectUtil.isNull(rechargeOrder)) {
                throw new CrmebException(StrUtil.format("ali pay query error : 充值订单后置处理，没有找到对应订单，支付服务方订单号：{}", orderNo));
            }
            if (rechargeOrder.getPaid()) {
                return Boolean.TRUE;
            }
            aliPayQuery(orderNo);
            // 支付成功处理
            Boolean rechargePayAfter = rechargeOrderService.paySuccessAfter(rechargeOrder);
            if (!rechargePayAfter) {
                throw new CrmebException(StrUtil.format("ali pay recharge pay after error : 数据保存失败==》" + orderNo));
            }
            return Boolean.TRUE;
        }
        if (PayConstants.PAY_SERVICE_TYPE_SVIP.equals(orderType)) {// 充值订单
            PaidMemberOrder paidMemberOrder = paidMemberOrderService.getByOutTradeNo(orderNo);
            if (ObjectUtil.isNull(paidMemberOrder)) {
                throw new CrmebException(StrUtil.format("ali pay query error : SVIP订单后置处理，没有找到对应订单，支付服务方订单号：{}", orderNo));
            }
            if (paidMemberOrder.getPaid()) {
                return Boolean.TRUE;
            }
            aliPayQuery(orderNo);
            // 支付成功处理
            // 支付成功处理
            Boolean svipPayAfter = paidMemberOrderService.paySuccessAfter(paidMemberOrder);
            if (!svipPayAfter) {
                logger.error("ali pay svip pay after error : 数据保存失败==》" + paidMemberOrder);
                throw new CrmebException(StrUtil.format("ali pay svip pay after error : 数据处理失败==》" + orderNo));
            }
            return Boolean.TRUE;
        }

        Order order = orderService.getByOrderNo(orderNo);
        if (ObjectUtil.isNull(order)) {
            throw new CrmebException(OrderResultCode.ORDER_NOT_EXIST);
        }
        if (order.getCancelStatus() > OrderConstants.ORDER_CANCEL_STATUS_NORMAL) {
            throw new CrmebException(OrderResultCode.ORDER_CANCEL);
        }
        if (!order.getPayType().equals(PayConstants.PAY_TYPE_ALI_PAY)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "不是支付宝支付类型订单");
        }
        if (order.getPaid()) {
            return Boolean.TRUE;
        }
        aliPayQuery(orderNo);
        Boolean execute = transactionTemplate.execute(e -> {
            Boolean updatePaid = orderService.updatePaid(orderNo);
            if (!updatePaid) {
                logger.error("支付宝查询订单状态，商品订单更新支付状态失败，orderNo = {}", orderNo);
                e.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        });

        if (!execute) {
            throw new CrmebException("支付成功更新订单失败");
        }
        asyncService.orderPaySuccessSplit(order.getOrderNo());
        return Boolean.TRUE;
    }

    private AlipayTradeQueryResponse aliPayQuery(String orderNo) {
        // 使用AliPayService中的证书模式查询方法（自动选择证书模式或公钥模式）
        AlipayTradeQueryResponse alipay_response = aliPayService.queryPayResultResponse(orderNo);
        
        if (ObjectUtil.isNull(alipay_response)) {
            logger.error("支付宝支付结果异常,查询结果为空");
            throw new CrmebException("支付宝支付结果异常,查询结果为空");
        }
        if (ObjectUtil.isNull(alipay_response.getTradeStatus()) || !alipay_response.getTradeStatus().equals("TRADE_SUCCESS")) {
            logger.error("支付宝支付结果异常，tradeStatus = " + alipay_response.getTradeStatus());
            throw new CrmebException("支付宝支付结果异常");
        }
        return alipay_response;
    }

    /**
     * 支付成功后置处理(临时)
     *
     * @param orderNo 订单编号
     */
    @Override
    public Boolean payAfterProcessingTemp(String orderNo) {
        Order platOrder = orderService.getByOrderNo(orderNo);
        if (ObjectUtil.isNull(platOrder)) {
            logger.error("OrderTaskServiceImpl.orderPaySuccessAfter | 订单不存在，orderNo: {}", orderNo);
            throw new CrmebException("订单不存在，orderNo: " + orderNo);
        }
        User user = userService.getById(platOrder.getUid());
        // 获取拆单后订单
        List<Order> orderList = orderService.getByPlatOrderNo(platOrder.getOrderNo());
        if (CollUtil.isEmpty(orderList)) {
            logger.error("OrderTaskServiceImpl.orderPaySuccessAfter | 商户订单信息不存在,orderNo: {}", orderNo);
            throw new CrmebException("商户订单信息不存在，orderNo: " + orderNo);
        }
        List<UserIntegralRecord> integralList = CollUtil.newArrayList();
        List<UserBrokerageRecord> brokerageRecordList = CollUtil.newArrayList();
        List<OrderProfitSharing> profitSharingList = CollUtil.newArrayList();
        List<MerchantBill> merchantBillList = CollUtil.newArrayList();
        List<Bill> billList = CollUtil.newArrayList();
        List<MerchantOrder> merchantOrderList = CollUtil.newArrayList();
        List<MerchantOrder> merchantOrderListForPrint = CollUtil.newArrayList();

        List<OrderDetail> orderDetailList = CollUtil.newArrayList();

        for (Order order : orderList) {
            // 拆单后，一个主订单只会对应一个商户订单
            MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(order.getOrderNo());
            // 排除核销订单，核销订单在具体核销步骤再打印小票
            if (merchantOrder.getShippingType().equals(OrderConstants.ORDER_SHIPPING_TYPE_EXPRESS)) {
                merchantOrderListForPrint.add(merchantOrder);
            }
            if (order.getGainIntegral() > 0) {
                // 生成赠送积分记录
                UserIntegralRecord integralRecord = integralRecordGainInit(user.getId(), order.getOrderNo(), order.getGainIntegral());
                integralList.add(integralRecord);
            }
            List<OrderDetail> merOrderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
            // 佣金处理
            List<UserBrokerageRecord> broRecordList = assignCommission(merchantOrder, merOrderDetailList);
            if (CollUtil.isNotEmpty(broRecordList)) {
                brokerageRecordList.addAll(broRecordList);
                merchantOrder.setUpdateTime(DateUtil.date());
                merchantOrderList.add(merchantOrder);
                merOrderDetailList.forEach(od -> od.setUpdateTime(DateUtil.date()));
                orderDetailList.addAll(merOrderDetailList);
            }
            // 商户帐单流水、分账
            if (!order.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_INTEGRAL)) {// 积分订单不参与账单
                OrderProfitSharing orderProfitSharing = initOrderProfitSharing(merchantOrder);
                MerchantBill merchantBill = initPayMerchantBill(merchantOrder, orderProfitSharing.getProfitSharingMerPrice());
                List<Bill> platBillList = initPlatformBill(order, merchantOrder, orderProfitSharing);
                profitSharingList.add(orderProfitSharing);
                merchantBillList.add(merchantBill);
                billList.addAll(platBillList);
            }
            try {
                //支付成功还需要调用聚水潭订单推送接口
                asyncService.sendJushuitanOrder(order);
                logger.info("订单上传聚水潭成功，订单号：{}", order.getOrderNo());
            } catch (Exception ex) {
                logger.warn("订单上传聚水潭失败，订单号：{}，错误：{}", order.getOrderNo(), ex.getMessage());
            }
            try {
                //支付成功给商户推送待办
                asyncService.createOrderTodoForMerchant(order.getMerId(), order.getOrderNo(),user);
                logger.info("支付成功给商户推送待办成功，订单号：{}", order.getOrderNo());
            } catch (Exception ex) {
                logger.warn("支付成功还给商户推送待办失败，订单号：{}，错误：{}", order.getOrderNo(), ex.getMessage());
            }
        }

        // 商户余额记录
        List<MerchantBalanceRecord> merchantBalanceRecordList = profitSharingList.stream().map(sharing -> {
            MerchantBalanceRecord merchantBalanceRecord = new MerchantBalanceRecord();
            merchantBalanceRecord.setMerId(sharing.getMerId());
            merchantBalanceRecord.setLinkNo(sharing.getOrderNo());
            merchantBalanceRecord.setLinkType("order");
            merchantBalanceRecord.setType(1);
            merchantBalanceRecord.setAmount(sharing.getProfitSharingMerPrice().add(sharing.getFreightFee()));
            merchantBalanceRecord.setTitle(StrUtil.format("订单支付，商户预计分账金额{}元", merchantBalanceRecord.getAmount()));
            merchantBalanceRecord.setBalance(BigDecimal.ZERO);
            merchantBalanceRecord.setStatus(1);
            return merchantBalanceRecord;
        }).collect(Collectors.toList());

        // 分销员逻辑
        if (!user.getIsPromoter()) {
            String funcStatus = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_SWITCH);
            if (funcStatus.equals("1")) {
                String broQuota = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_LINE);
                if (!broQuota.equals("-1") && platOrder.getPayPrice().compareTo(new BigDecimal(broQuota)) >= 0) {// -1 不成为分销员
                    user.setIsPromoter(true);
                }
            }
        } else {
            user.setIsPromoter(false);
        }
        Boolean execute = transactionTemplate.execute(e -> {
            // 订单、佣金
            if (CollUtil.isNotEmpty(brokerageRecordList)) {
                merchantOrderService.updateBatchById(merchantOrderList);
                orderDetailService.updateBatchById(orderDetailList);
                userBrokerageRecordService.saveBatch(brokerageRecordList);
            }
            // 订单日志
            orderList.forEach(o -> orderStatusService.createLog(o.getOrderNo(), OrderStatusConstants.ORDER_STATUS_PAY_SPLIT, StrUtil.format(OrderStatusConstants.ORDER_LOG_MESSAGE_PAY_SPLIT, platOrder.getOrderNo())));
            // 用户信息变更
            userService.paySuccessChange(user.getId(), user.getIsPromoter());
            // 积分记录
            if (CollUtil.isNotEmpty(integralList)) {
                userIntegralRecordService.saveBatch(integralList);
            }

            // 会员积分处理
            processMemberIntegral(orderList, user);

            if (CollUtil.isNotEmpty(billList)) {
                billService.saveBatch(billList);
            }
            if (CollUtil.isNotEmpty(merchantBillList)) {
                merchantBillService.saveBatch(merchantBillList);
            }
            if (CollUtil.isNotEmpty(profitSharingList)) {
                orderProfitSharingService.saveBatch(profitSharingList);
            }
            if (CollUtil.isNotEmpty(merchantBalanceRecordList)) {
                merchantBalanceRecordService.saveBatch(merchantBalanceRecordList);
            }
            return Boolean.TRUE;
        });
        if (execute) {
            // 发布订单支付成功事件，触发WebSocket推送  
            for (Order order : orderList) {
                try {
                    dataChangeEventPublisher.publishOrderPaid(order);
                    logger.info("订单支付成功，已触发WebSocket推送: orderNo={}, payPrice={}", order.getOrderNo(), order.getPayPrice());
                } catch (Exception e) {
                    logger.error("发布订单支付事件失败: orderNo={}", order.getOrderNo(), e);
                    // 不影响支付流程，只记录错误
                }
            }
            
            SystemNotification payNotification = systemNotificationService.getByMark(NotifyConstants.PAY_SUCCESS_MARK);
            // 发送短信
            if (StrUtil.isNotBlank(user.getPhone()) && payNotification.getIsSms().equals(1)) {
                try {
                    smsService.sendPaySuccess(user.getPhone(), platOrder.getOrderNo(), platOrder.getPayPrice(),user.getNickname());
                } catch (Exception e) {
                    logger.error("支付成功短信发送异常", e);
                }
            }
            if (payNotification.getIsWechat().equals(1) || payNotification.getIsRoutine().equals(1)) {
                //下发模板通知
                try {
                    pushMessageOrder(platOrder, user, payNotification);
                } catch (Exception e) {
                    logger.error("支付成功发送微信通知失败", e);
                }
            }

            // 购买成功后根据配置送优惠券
            autoSendCoupons(platOrder);
            List<String> orderNoList = orderList.stream().map(Order::getOrderNo).collect(Collectors.toList());
            asyncService.orderPayAfterFreezingOperation(orderNoList);

            SystemNotification merchantPayNotification = systemNotificationService.getByMark(NotifyConstants.MERCHANT_PAY_SUCCESS_REMINDER);
            if (merchantPayNotification.getIsSms().equals(1)) {
                try {
                    orderList.forEach(order -> {
                        List<SystemAdmin> smsAdminList = systemAdminService.findReceiveSmsAdminListByMerId(order.getMerId());
                        smsService.sendPaySuccessToMerchant(smsAdminList, order.getOrderNo());
                    });
                } catch (Exception e) {
                    logger.error("支付成功商户管理员短信发送异常", e);
                }
            }

            try {
                if (!platOrder.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_INTEGRAL)) {
                    for (int i = 0; i < merchantOrderListForPrint.size(); ) {
                        MerchantOrder merchantOrder = merchantOrderListForPrint.get(i);
                        Merchant merchant = merchantService.getByIdException(merchantOrder.getMerId());
                        // 小票打印开关：0关闭，1=手动打印，2=自动打印，3=自动和手动
                        if (merchant.getReceiptPrintingSwitch() == 0) {
                            merchantOrderListForPrint.remove(i);
                            continue;
                        }
                        if (merchant.getReceiptPrintingSwitch() < 2) {
                            merchantOrderListForPrint.remove(i);
                            continue;
                        }
                        i++;
                    }
                    // 打印小票 op=1 为方法调用这里也就是支付后自动打印小票的场景
                    if (CollUtil.isNotEmpty(merchantOrderListForPrint)) {
                        merchantPrintService.batchPrintTicket(merchantOrderListForPrint);
                    }
                }
            } catch (Exception e) {
                logger.error(StrUtil.format("小票打印异常，Exception：{}", e.getMessage()), e);
            }

            orderList.forEach(order -> {
                if (!order.getType().equals(OrderConstants.ORDER_TYPE_PITUAN)) {
                    if (order.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_CLOUD) || order.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_CDKEY)) {
                        orderService.virtualShipment(order);
                    }
                }
            });
        }

        return execute;
    }

    /**
     * 获取收银台信息
     *
     * @param orderNo 订单号
     */
    @Override
    public CashierInfoResponse getCashierIno(String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order.getCancelStatus() > OrderConstants.ORDER_CANCEL_STATUS_NORMAL) {
            throw new CrmebException(OrderResultCode.ORDER_CANCEL);
        }
        if (order.getPaid()) {
            throw new CrmebException(OrderResultCode.ORDER_PAID);
        }
        if (order.getStatus() > OrderConstants.ORDER_STATUS_WAIT_PAY) {
            throw new CrmebException(OrderResultCode.ORDER_STATUS_ABNORMAL);
        }
        MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(orderNo);
        CashierInfoResponse response = new CashierInfoResponse();
        response.setPayPrice(order.getPayPrice());
        response.setTotalNum(order.getTotalNum());
        response.setConsigneeName(merchantOrder.getRealName());
        response.setConsigneePhone(merchantOrder.getUserPhone());
        response.setConsigneeAddress(merchantOrder.getUserAddress());
        DateTime cancelDate = DateUtil.offsetMinute(order.getCreateTime(), crmebConfig.getOrderCancelTime());
        response.setCancelDateTime(cancelDate.getTime());
        return response;
    }

    /**
     * 查询微信支付结果
     */
    @Override
    public Boolean queryWechatPaymentResult(WechatPaymentQueryRequest request) {
        if (request.getOrderType().equals(PayConstants.PAY_SERVICE_TYPE_ORDER)) {
            return queryWechatPayResult(request.getOrderNo());
        }
        if (request.getOrderType().equals(PayConstants.PAY_SERVICE_TYPE_SVIP)) {
            return querySvipWechatPayResult(request.getOrderNo());
        }
        return queryRechargeWechatPayResult(request.getOrderNo());
    }

    private Boolean queryRechargeWechatPayResult(String orderNo) {
        RechargeOrder rechargeOrder = rechargeOrderService.getByOrderNo(orderNo);
        if (ObjectUtil.isNull(rechargeOrder)) {
            throw new CrmebException(OrderResultCode.ORDER_NOT_EXIST);
        }
        if (!rechargeOrder.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "不是微信支付类型订单");
        }
        if (rechargeOrder.getPaid()) {
            return Boolean.TRUE;
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(rechargeOrder.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "未找到对应微信订单");
        }
        String paySource = getWechatPaySource(rechargeOrder.getPayChannel());
        WxPayOrderQueryResult queryResult = wechatPayService.queryOrder(rechargeOrder.getOutTradeNo(), paySource);
        if (!queryResult.getTradeState().equals("SUCCESS")) {
            return Boolean.FALSE;
        }
        wechatPayInfo.setIsSubscribe(queryResult.getIsSubscribe());
        wechatPayInfo.setTradeState(queryResult.getTradeState());
        wechatPayInfo.setBankType(queryResult.getBankType());
        wechatPayInfo.setCashFee(queryResult.getCashFee());
        wechatPayInfo.setCouponFee(queryResult.getCouponFee());
        wechatPayInfo.setTransactionId(queryResult.getTransactionId());
        wechatPayInfo.setTimeEnd(queryResult.getTimeEnd());
        wechatPayInfo.setTradeStateDesc(queryResult.getTradeStateDesc());
        Boolean updatePaid = transactionTemplate.execute(e -> {
            Boolean update = rechargeOrderService.paySuccessAfter(rechargeOrder);
            if (!update) {
                logger.error("充值订单支付处理结果失败，orderNo = {}", rechargeOrder.getOrderNo());
                e.setRollbackOnly();
                return false;
            }
            wechatPayInfoService.updateById(wechatPayInfo);
            return Boolean.TRUE;
        });
        if (!updatePaid) {
            throw new CrmebException("充值支付成功更新订单失败");
        }
        return Boolean.TRUE;
    }

    /**
     * 查询svip订单微信支付结果
     */
    private Boolean querySvipWechatPayResult(String orderNo) {
        PaidMemberOrder order = paidMemberOrderService.getByOrderNo(orderNo);
        if (ObjectUtil.isNull(order)) {
            throw new CrmebException(MemberResultCode.PAID_MEMBER_ORDER_NOT_EXIST);
        }
        if (!order.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "不是微信支付类型订单");
        }
        if (order.getPaid()) {
            return Boolean.TRUE;
        }
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(order.getOutTradeNo());
        if (ObjectUtil.isNull(wechatPayInfo)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "未找到对应微信订单");
        }
        String paySource = getWechatPaySource(order.getPayChannel());
        WxPayOrderQueryResult queryResult = wechatPayService.queryOrder(order.getOutTradeNo(), paySource);
        if (!queryResult.getTradeState().equals("SUCCESS")) {
            return Boolean.FALSE;
        }
        wechatPayInfo.setIsSubscribe(queryResult.getIsSubscribe());
        wechatPayInfo.setTradeState(queryResult.getTradeState());
        wechatPayInfo.setBankType(queryResult.getBankType());
        wechatPayInfo.setCashFee(queryResult.getCashFee());
        wechatPayInfo.setCouponFee(queryResult.getCouponFee());
        wechatPayInfo.setTransactionId(queryResult.getTransactionId());
        wechatPayInfo.setTimeEnd(queryResult.getTimeEnd());
        wechatPayInfo.setTradeStateDesc(queryResult.getTradeStateDesc());
        Boolean updatePaid = transactionTemplate.execute(e -> {
            Boolean update = paidMemberOrderService.paySuccessAfter(order);
            if (!update) {
                logger.error("svip订单支付处理结果失败，orderNo = {}", order.getOrderNo());
                e.setRollbackOnly();
                return false;
            }
            wechatPayInfoService.updateById(wechatPayInfo);
            return Boolean.TRUE;
        });
        if (!updatePaid) {
            throw new CrmebException("svip支付成功更新订单失败");
        }
        return Boolean.TRUE;
    }

    /**
     * 发送消息通知
     * 根据用户类型发送
     * 公众号模板消息
     * 小程序订阅消息
     */
    private void pushMessageOrder(Order order, User user, SystemNotification payNotification) {
        if (order.getPayChannel().equals(PayConstants.PAY_CHANNEL_H5)) {// H5
            return;
        }
        UserToken userToken;
        HashMap<String, String> temMap = new HashMap<>();
        if (!order.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            return;
        }
        List<OrderDetail> orderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
        // 公众号模板消息
        if (order.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC) && payNotification.getIsWechat().equals(1) && user.getIsWechatPublic()) {
            userToken = userTokenService.getTokenByUserId(user.getId(), UserConstants.USER_TOKEN_TYPE_WECHAT);
            if (ObjectUtil.isNull(userToken)) {
                return;
            }
            // 发送微信模板消息
            /**
             * {{first.DATA}}
             * 订单号：{{keyword1.DATA}}
             * 商品名称：{{keyword2.DATA}}
             * 支付金额：{{keyword3.DATA}}
             * 下单人：{{keyword4.DATA}}
             * 订单支付时间：{{keyword5.DATA}}
             * {{remark.DATA}}
             */
            temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "订单支付成功通知！");
            temMap.put("keyword1", order.getOrderNo());
            temMap.put("keyword2", orderDetailList.stream().map(OrderDetail::getProductName).collect(Collectors.joining(",")));
            temMap.put("keyword3", order.getPayPrice().toString());
            temMap.put("keyword4", user.getNickname());
            temMap.put("keyword5", order.getPayTime().toString());
            temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "欢迎下次再来！");
            templateMessageService.pushTemplateMessage(payNotification.getWechatId(), temMap, userToken.getToken());
            return;
        }
        if (order.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_MINI) && payNotification.getIsRoutine().equals(1)) {
            // 小程序发送订阅消息
            userToken = userTokenService.getTokenByUserId(user.getId(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return;
            }
            // 组装数据
//            temMap.put("character_string1", storeOrder.getOrderId());
//            temMap.put("amount2", storeOrder.getPayPrice().toString() + "元");
//            temMap.put("thing7", "您的订单已支付成功");
            temMap.put("character_string3", order.getOrderNo());
            temMap.put("amount9", order.getPayPrice().toString() + "元");
            temMap.put("thing6", "您的订单已支付成功");
            templateMessageService.pushMiniTemplateMessage(payNotification.getRoutineId(), temMap, userToken.getToken());
        }
    }


    /**
     * 佣金处理
     *
     * @param merchantOrder   商户订单部分
     * @param orderDetailList 订单详情列表
     * @return 佣金记录列表
     */
    private List<UserBrokerageRecord> assignCommission(MerchantOrder merchantOrder, List<OrderDetail> orderDetailList) {
        // 秒杀订单不参与分佣
        if (merchantOrder.getType().equals(OrderConstants.ORDER_TYPE_SECKILL)) {
            return CollUtil.newArrayList();
        }
        // 积分订单不参与分佣
        if (merchantOrder.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_INTEGRAL)) {
            return CollUtil.newArrayList();
        }
        // 积分订单不参与分佣
        if (merchantOrder.getType().equals(OrderConstants.ORDER_TYPE_PITUAN)){
            return CollUtil.newArrayList();
        }
        // 检测商城是否开启分销功能
        String isOpen = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_SWITCH);
        if (StrUtil.isBlank(isOpen) || isOpen.equals("0")) {
            return CollUtil.newArrayList();
        }
        long count = orderDetailList.stream().filter(e -> e.getSubBrokerageType() > 0).count();
        if (count == 0L) {
            return CollUtil.newArrayList();
        }
        // 查找订单所属人信息
        User user = userService.getById(merchantOrder.getUid());
        // 当前用户不存在 没有上级 或者 当用用户上级时自己  直接返回
        if (ObjectUtil.isNull(user.getSpreadUid()) || user.getSpreadUid() < 1 || user.getSpreadUid().equals(merchantOrder.getUid())) {
            return CollUtil.newArrayList();
        }
        // 获取参与分佣的人（两级）
        List<MyRecord> spreadRecordList = getSpreadRecordList(user.getSpreadUid());
        if (CollUtil.isEmpty(spreadRecordList)) {
            return CollUtil.newArrayList();
        }
        // 获取佣金冻结期
        String freezingTime = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_BROKERAGE_FREEZING_TIME);
        List<UserBrokerageRecord> brokerageRecordList = new ArrayList<>();
        // 计算两级佣金金额
        if (spreadRecordList.size() == 1) {
            BigDecimal firstBrokerage = BigDecimal.ZERO;
            for (OrderDetail orderDetail : orderDetailList) {
                if (orderDetail.getSubBrokerageType().equals(0)) {
                    continue;
                }
                BigDecimal detailPrice = (orderDetail.getIsSvip() && orderDetail.getIsPaidMemberProduct()) ? orderDetail.getVipPrice() : orderDetail.getPrice();
                BigDecimal brokerage = detailPrice.multiply(new BigDecimal(orderDetail.getPayNum().toString()))
                        .subtract(orderDetail.getMerCouponPrice())
                        .multiply(new BigDecimal(orderDetail.getBrokerage().toString()))
                        .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_DOWN);
                orderDetail.setFirstBrokerageFee(brokerage);
                firstBrokerage = firstBrokerage.add(brokerage);
            }
            merchantOrder.setFirstBrokerage(firstBrokerage);
            UserBrokerageRecord brokerageRecord = new UserBrokerageRecord();
            brokerageRecord.setUid(spreadRecordList.get(0).getInt("spreadUid"));
            brokerageRecord.setSubUid(merchantOrder.getUid());
            brokerageRecord.setLinkNo(merchantOrder.getOrderNo());
            brokerageRecord.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
            brokerageRecord.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
            brokerageRecord.setTitle(BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_ORDER);
            brokerageRecord.setPrice(firstBrokerage);
            brokerageRecord.setMark(StrUtil.format("获得推广佣金，分佣{}", firstBrokerage));
            brokerageRecord.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
            brokerageRecord.setFrozenTime(Integer.valueOf(Optional.ofNullable(freezingTime).orElse("0")));
            brokerageRecord.setCreateTime(CrmebDateUtil.nowDateTime());
            brokerageRecord.setBrokerageLevel(spreadRecordList.get(0).getInt("index"));
            brokerageRecordList.add(brokerageRecord);
            return brokerageRecordList;
        }
        BigDecimal firstBrokerage = BigDecimal.ZERO;
        BigDecimal secondBrokerage = BigDecimal.ZERO;
        for (OrderDetail orderDetail : orderDetailList) {
            if (orderDetail.getSubBrokerageType().equals(0)) {
                continue;
            }
            BigDecimal detailPrice = (orderDetail.getIsSvip() && orderDetail.getIsPaidMemberProduct()) ? orderDetail.getVipPrice() : orderDetail.getPrice();
            BigDecimal brokerage = detailPrice.multiply(new BigDecimal(orderDetail.getPayNum().toString()))
                    .subtract(orderDetail.getMerCouponPrice())
                    .multiply(new BigDecimal(orderDetail.getBrokerage().toString()))
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_DOWN);
            BigDecimal brokerageTwo = detailPrice.multiply(new BigDecimal(orderDetail.getPayNum().toString()))
                    .subtract(orderDetail.getMerCouponPrice())
                    .multiply(new BigDecimal(orderDetail.getBrokerageTwo().toString()))
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_DOWN);

            orderDetail.setFirstBrokerageFee(brokerage);
            orderDetail.setSecondBrokerageFee(brokerageTwo);
            firstBrokerage = firstBrokerage.add(brokerage);
            secondBrokerage = secondBrokerage.add(brokerageTwo);
        }
        merchantOrder.setFirstBrokerage(firstBrokerage);
        merchantOrder.setSecondBrokerage(secondBrokerage);
        // 生成佣金记录
        brokerageRecordList = spreadRecordList.stream().map(record -> {
            UserBrokerageRecord brokerageRecord = new UserBrokerageRecord();
            brokerageRecord.setUid(record.getInt("spreadUid"));
            brokerageRecord.setSubUid(merchantOrder.getUid());
            brokerageRecord.setLinkNo(merchantOrder.getOrderNo());
            brokerageRecord.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
            brokerageRecord.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
            brokerageRecord.setTitle(BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_ORDER);
            BigDecimal price = record.getInt("index") == 1 ? merchantOrder.getFirstBrokerage() : merchantOrder.getSecondBrokerage();
            brokerageRecord.setPrice(price);
            brokerageRecord.setMark(StrUtil.format("获得推广佣金，分佣{}", price));
            brokerageRecord.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
            brokerageRecord.setFrozenTime(Integer.valueOf(Optional.ofNullable(freezingTime).orElse("0")));
            brokerageRecord.setCreateTime(CrmebDateUtil.nowDateTime());
            brokerageRecord.setBrokerageLevel(record.getInt("index"));
            return brokerageRecord;
        }).collect(Collectors.toList());
        return brokerageRecordList;
    }

    /**
     * 获取参与分佣人员（两级）
     *
     * @param spreadUid 一级分佣人Uid
     * @return List<MyRecord>
     */
    private List<MyRecord> getSpreadRecordList(Integer spreadUid) {
        List<MyRecord> recordList = CollUtil.newArrayList();

        // 第一级
        User spreadUser = userService.getById(spreadUid);
        if (ObjectUtil.isNull(spreadUser) || !spreadUser.getIsPromoter()) {
            return recordList;
        }

        MyRecord firstRecord = new MyRecord();
        firstRecord.set("index", 1);
        firstRecord.set("spreadUid", spreadUid);
        recordList.add(firstRecord);

        // 第二级
        User spreadSpreadUser = userService.getById(spreadUser.getSpreadUid());
        if (ObjectUtil.isNull(spreadSpreadUser) || !spreadSpreadUser.getIsPromoter()) {
            return recordList;
        }
        MyRecord secondRecord = new MyRecord();
        secondRecord.set("index", 2);
        secondRecord.set("spreadUid", spreadSpreadUser.getId());
        recordList.add(secondRecord);
        return recordList;
    }

    /**
     * 初始化订单分帐表
     *
     * @param merchantOrder 商户部分订单
     * @return 分账记录
     */
    private OrderProfitSharing initOrderProfitSharing(MerchantOrder merchantOrder) {
        // 获取商户信息
        Merchant merchant = merchantService.getByIdException(merchantOrder.getMerId());
        // 分账计算
        // 商户收入 = 订单应付 - 商户优惠 -平台手续费 - 佣金
        BigDecimal orderPrice = merchantOrder.getPayPrice().add(merchantOrder.getIntegralPrice()).add(merchantOrder.getPlatCouponPrice()).subtract(merchantOrder.getPayPostage());
        // 平台手续费
        BigDecimal platFee = orderPrice.multiply(new BigDecimal(merchant.getHandlingFee())).divide(new BigDecimal(100), 2, BigDecimal.ROUND_UP);
        // 商户收入金额
        BigDecimal merchantFee = orderPrice.subtract(platFee).subtract(merchantOrder.getFirstBrokerage()).subtract(merchantOrder.getSecondBrokerage());
        OrderProfitSharing orderProfitSharing = new OrderProfitSharing();
        orderProfitSharing.setOrderNo(merchantOrder.getOrderNo());
        orderProfitSharing.setMerId(merchantOrder.getMerId());
        orderProfitSharing.setOrderPrice(merchantOrder.getPayPrice());
        orderProfitSharing.setIntegralNum(merchantOrder.getUseIntegral());
        orderProfitSharing.setIntegralPrice(merchantOrder.getIntegralPrice());
        orderProfitSharing.setProfitSharingPlatPrice(platFee);
        orderProfitSharing.setProfitSharingMerPrice(merchantFee);
        orderProfitSharing.setFirstBrokerageFee(merchantOrder.getFirstBrokerage());
        orderProfitSharing.setSecondBrokerageFee(merchantOrder.getSecondBrokerage());
        orderProfitSharing.setPlatCouponPrice(merchantOrder.getPlatCouponPrice());
        orderProfitSharing.setFreightFee(merchantOrder.getPayPostage());
        return orderProfitSharing;
    }

    /**
     * 初始化订单支付商户账单表
     *
     * @param merchantOrder 商户订单部分
     * @param merchantFee   商户分账金额
     */
    private MerchantBill initPayMerchantBill(MerchantOrder merchantOrder, BigDecimal merchantFee) {
        MerchantBill merchantBill = new MerchantBill();
        merchantBill.setMerId(merchantOrder.getMerId());
        merchantBill.setType(BillConstants.BILL_TYPE_PAY_ORDER);
        merchantBill.setOrderNo(merchantOrder.getOrderNo());
        merchantBill.setUid(merchantOrder.getUid());
        merchantBill.setPm(BillConstants.BILL_PM_ADD);
        BigDecimal merFee = merchantOrder.getPayPostage().add(merchantFee);
        merchantBill.setAmount(merFee);
        merchantBill.setMark(StrUtil.format("订单{}支付{}元，商户收入{}元", merchantOrder.getOrderNo(), merchantOrder.getPayPrice(), merFee));
        return merchantBill;
    }

    /**
     * 初始化订单支付平台账单表
     *
     * @param order              订单
     * @param merchantOrder      商户订单部分
     * @param orderProfitSharing 分账数据
     * @return List
     */
    private List<Bill> initPlatformBill(Order order, MerchantOrder merchantOrder, OrderProfitSharing orderProfitSharing) {
        List<Bill> billList = CollUtil.newArrayList();

        Bill payBill = new Bill();
        payBill.setUid(order.getUid());
        payBill.setOrderNo(order.getOrderNo());
        payBill.setAmount(order.getPayPrice());
        if (order.getPayType().equals(PayConstants.PAY_TYPE_YUE)) {
            payBill.setPm(BillConstants.BILL_PM_SUB);
            payBill.setType(BillConstants.BILL_TYPE_YUE_PAY);
            payBill.setMark(StrUtil.format("余额支付成功，扣除用户余额{}元", order.getPayPrice()));
        } else {
            payBill.setPm(BillConstants.BILL_PM_ADD);
            payBill.setType(BillConstants.BILL_TYPE_PAY_ORDER);
            payBill.setMark(StrUtil.format("订单支付成功，支付金额{}元", order.getPayPrice()));
        }
        billList.add(payBill);

        Bill collectBill = new Bill();
        collectBill.setMerId(merchantOrder.getMerId());
        collectBill.setOrderNo(order.getOrderNo());
        collectBill.setAmount(orderProfitSharing.getProfitSharingMerPrice().add(merchantOrder.getPayPostage()));
        collectBill.setPm(BillConstants.BILL_PM_SUB);
        collectBill.setType(BillConstants.BILL_TYPE_MERCHANT_COLLECT);
        collectBill.setMark(StrUtil.format("订单支付成功，商户分账{}元", orderProfitSharing.getProfitSharingMerPrice().add(merchantOrder.getPayPostage())));
        billList.add(collectBill);

        Bill platBill = new Bill();
        platBill.setOrderNo(order.getOrderNo());
        platBill.setAmount(orderProfitSharing.getProfitSharingPlatPrice());
        platBill.setPm(BillConstants.BILL_PM_ADD);
        platBill.setType(BillConstants.BILL_TYPE_PAY_ORDER);
        platBill.setMark(StrUtil.format("订单支付成功，平台手续费{}元", orderProfitSharing.getProfitSharingPlatPrice()));
        billList.add(platBill);

        if (ObjectUtil.isNotNull(orderProfitSharing.getFirstBrokerageFee()) && orderProfitSharing.getFirstBrokerageFee().compareTo(BigDecimal.ZERO) > 0) {
            Bill firstBill = new Bill();
            firstBill.setOrderNo(order.getOrderNo());
            firstBill.setAmount(orderProfitSharing.getFirstBrokerageFee());
            firstBill.setPm(BillConstants.BILL_PM_SUB);
            firstBill.setType(BillConstants.BILL_TYPE_BROKERAGE);
            firstBill.setMark(StrUtil.format("订单支付成功，分配一级佣金{}元", orderProfitSharing.getFirstBrokerageFee()));
            billList.add(firstBill);
            if (orderProfitSharing.getSecondBrokerageFee().compareTo(BigDecimal.ZERO) > 0) {
                Bill secondBill = new Bill();
                secondBill.setOrderNo(order.getOrderNo());
                secondBill.setAmount(orderProfitSharing.getSecondBrokerageFee());
                secondBill.setPm(BillConstants.BILL_PM_SUB);
                secondBill.setType(BillConstants.BILL_TYPE_BROKERAGE);
                secondBill.setMark(StrUtil.format("订单支付成功，分配二级佣金{}元", orderProfitSharing.getSecondBrokerageFee()));
                billList.add(secondBill);
            }
        }

        if (orderProfitSharing.getIntegralNum() > 0) {
            Bill integralBill = new Bill();
            integralBill.setOrderNo(order.getOrderNo());
            integralBill.setAmount(order.getIntegralPrice());
            integralBill.setPm(BillConstants.BILL_PM_SUB);
            integralBill.setType(BillConstants.BILL_TYPE_PAY_ORDER);
            integralBill.setMark(StrUtil.format("订单支付成功，用户使用{}积分抵扣{}元，平台扣除", orderProfitSharing.getIntegralNum(), orderProfitSharing.getIntegralPrice()));
            billList.add(integralBill);
        }
        return billList;
    }

    /**
     * 初始化积分赠送记录
     */
    private UserIntegralRecord integralRecordGainInit(Integer uid, String orderNo, Integer gainIntegral) {
        UserIntegralRecord integralRecord = new UserIntegralRecord();
        integralRecord.setUid(uid);
        integralRecord.setLinkId(orderNo);
        integralRecord.setLinkType(IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_ORDER);
        integralRecord.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
        integralRecord.setTitle(IntegralRecordConstants.INTEGRAL_RECORD_TITLE_ORDER);
        integralRecord.setIntegral(gainIntegral);
//        integralRecord.setBalance(balance);
        integralRecord.setMark(StrUtil.format("订单支付成功奖励{}积分", gainIntegral));
        integralRecord.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_CREATE);
        // 获取积分冻结期
        String freezeTime = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_STORE_INTEGRAL_EXTRACT_TIME);
        integralRecord.setFrozenTime(Integer.valueOf(Optional.ofNullable(freezeTime).orElse("0")));
        integralRecord.setCreateTime(CrmebDateUtil.nowDateTime());
        return integralRecord;
    }

    /**
     * 支付宝支付
     *
     * @param order 订单
     * @return result
     */
    private String aliPayment(Order order) {
        DateTime cancelTime = DateUtil.offset(order.getCreateTime(), DateField.MINUTE, crmebConfig.getOrderCancelTime());
        String cancelStr = cancelTime.toString(DateConstants.DATE_FORMAT);
      //  return aliPayService.pay(order.getOrderNo(), order.getPayPrice(), "order", order.getPayChannel(), cancelStr);
        return aliPayService.qrCodePay(order.getOrderNo(), order.getPayPrice(), "订单支付", "order", cancelStr);
    }

    /**
     * 微信支付
     *
     * @param order 订单
     * @return WxPayJsResultVo
     */
    private WxPayJsResultVo wechatPayment(Order order) {
        // 根据订单支付类型来判断获取公众号openId还是小程序openId
        UserToken userToken = new UserToken();
        userToken.setToken("");
        if (order.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC)) {// 公众号
            userToken = userTokenService.getTokenByUserId(order.getUid(), UserConstants.USER_TOKEN_TYPE_WECHAT);
        }
        if (order.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_MINI)
                || order.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO)) {// 小程序
            userToken = userTokenService.getTokenByUserId(order.getUid(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
        }
        // 从config表中获取H5/PC支付使用的是公众号还是小程序的
        String paySource = order.getPayChannel();
        if (paySource.equals(PayConstants.PAY_CHANNEL_H5) || paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_NATIVE)) {
            String source = systemConfigService.getValueByKey(SysConfigConstants.WECHAT_PAY_SOURCE_H5_PC);
            if (StrUtil.isNotBlank(source) && source.equals(PayConstants.WECHAT_PAY_SOURCE_MINI)) {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_MINI;
            } else {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_PUBLIC;
            }
        }
        // 根据不同的版本调用对应的微信下单接口
        MyRecord myRecord;
        String outTradeNo;
        String apiDomain = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_API_URL);
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayUnifiedOrderRequest unifiedOrderRequest = buildWxPayUnifiedOrderRequest(order);
            outTradeNo = unifiedOrderRequest.getOutTradeNo();
            myRecord = wechatPayService.createOrder(unifiedOrderRequest, userToken.getToken(), order.getPayChannel(), paySource, apiDomain);
        } else {
            WxPayUnifiedOrderV3Request v3Request = buildWxPayUnifiedOrderV3Request(order);
            outTradeNo = v3Request.getOutTradeNo();
            myRecord = wechatPayService.createV3Order(v3Request, userToken.getToken(), order.getPayChannel(), paySource, apiDomain);
        }
        WxPayJsResultVo vo = new WxPayJsResultVo();
        switch (order.getPayChannel()) {
            case PayConstants.PAY_CHANNEL_WECHAT_MINI:
            case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
            case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                vo.setAppId(myRecord.getStr("appId"));
                vo.setNonceStr(myRecord.getStr("nonceStr"));
                vo.setPackages(myRecord.getStr("package"));
                vo.setSignType(myRecord.getStr("signType"));
                vo.setPaySign(myRecord.getStr("paySign"));
                vo.setTimeStamp(myRecord.getStr("timeStamp"));
                break;
            case PayConstants.PAY_CHANNEL_H5:
                vo.setMwebUrl(myRecord.getStr("mwebUrl"));
            case PayConstants.PAY_CHANNEL_WECHAT_NATIVE:
                vo.setMwebUrl(myRecord.getStr("codeUrl"));
            case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
            case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                vo.setAppId(myRecord.getStr("appId"));
                vo.setPartnerid(myRecord.getStr("partnerId"));
                vo.setPrepayid(myRecord.getStr("prepayId"));
                vo.setPackages(myRecord.getStr("package"));
                vo.setNonceStr(myRecord.getStr("nonceStr"));
                vo.setTimeStamp(myRecord.getStr("timeStamp"));
                vo.setPaySign(myRecord.getStr("sign"));
        }
        // 更新商户订单号
        order.setOutTradeNo(outTradeNo);
        return vo;
    }

    /**
     * 余额支付
     *
     * @param order 订单
     * @return Boolean Boolean
     */
    private Boolean yuePay(Order order, User user) {
        // 用户余额扣除
        Boolean execute = transactionTemplate.execute(e -> {
            Boolean update = Boolean.TRUE;
            // 订单修改
            order.setPaid(true);
            order.setPayTime(DateUtil.date());
            order.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
            order.setPayType(PayConstants.PAY_TYPE_YUE);
            order.setPayChannel(PayConstants.PAY_CHANNEL_YUE);
            order.setUpdateTime(DateUtil.date());
            orderService.updateById(order);
            // 这里只扣除金额，账单记录在task中处理
            if (order.getPayPrice().compareTo(BigDecimal.ZERO) > 0) {
                update = userService.updateNowMoney(order.getUid(), order.getPayPrice(), Constants.OPERATION_TYPE_SUBTRACT);
                if (!update) {
                    logger.error("余额支付，扣除用户余额失败，orderNo = {}", order.getOrderNo());
                    e.setRollbackOnly();
                    return update;
                }
                // 用户余额记录
                UserBalanceRecord userBalanceRecord = new UserBalanceRecord();
                userBalanceRecord.setUid(user.getId());
                userBalanceRecord.setLinkId(order.getOrderNo());
                userBalanceRecord.setLinkType(BalanceRecordConstants.BALANCE_RECORD_LINK_TYPE_ORDER);
                userBalanceRecord.setType(BalanceRecordConstants.BALANCE_RECORD_TYPE_SUB);
                userBalanceRecord.setAmount(order.getPayPrice());
                userBalanceRecord.setBalance(user.getNowMoney().subtract(order.getPayPrice()));
                userBalanceRecord.setRemark(StrUtil.format(BalanceRecordConstants.BALANCE_RECORD_REMARK_ORDER, order.getPayPrice()));
                userBalanceRecordService.save(userBalanceRecord);
            }
            return update;
        });
        if (!execute) throw new CrmebException("余额支付订单失败");
        asyncService.orderPaySuccessSplit(order.getOrderNo());
        return true;
    }

    private WxPayUnifiedOrderRequest buildWxPayUnifiedOrderRequest(Order order) {
        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        // 因商品名称在微信侧超长更换为网站名称
        String siteName = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_NAME);
        orderRequest.setBody(siteName);

        AttachVo attachVo = new AttachVo(PayConstants.PAY_SERVICE_TYPE_ORDER, order.getUid());
        orderRequest.setAttach(JSONObject.toJSONString(attachVo));
        orderRequest.setOutTradeNo(CrmebUtil.getOrderNo(OrderConstants.ORDER_PREFIX_WECHAT));
        orderRequest.setTotalFee(order.getPayPrice().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).intValue());
        orderRequest.setSpbillCreateIp(RequestUtil.getClientIp());
        String domain = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_URL);
        CreateOrderH5SceneInfoVo createOrderH5SceneInfoVo = new CreateOrderH5SceneInfoVo(
                new CreateOrderH5SceneInfoDetailVo(
                        domain,
                        systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_NAME)
                )
        );
        orderRequest.setSceneInfo(JSONObject.toJSONString(createOrderH5SceneInfoVo));
        orderRequest.setTimeStart(DateUtil.date().toString(DateConstants.DATE_TIME_FORMAT_NUM));
        DateTime cancelTime = DateUtil.offset(order.getCreateTime(), DateField.MINUTE, crmebConfig.getOrderCancelTime());
        String cancelStr = cancelTime.toString(DateConstants.DATE_TIME_FORMAT_NUM);
        orderRequest.setTimeExpire(cancelStr);

        orderRequest.setProductId(order.getOrderNo());
        return orderRequest;
    }

    private WxPayUnifiedOrderV3Request buildWxPayUnifiedOrderV3Request(Order order) {
        WxPayUnifiedOrderV3Request orderRequest = new WxPayUnifiedOrderV3Request();
        // description【商品描述】不能超过127个字符。
        String siteName = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_NAME);
        orderRequest.setDescription(siteName);
        orderRequest.setOutTradeNo(CrmebUtil.getOrderNo(OrderConstants.ORDER_PREFIX_WECHAT));
        DateTime cancelTime = DateUtil.offset(order.getCreateTime(), DateField.MINUTE, crmebConfig.getOrderCancelTime());
        String cancelStr = cancelTime.toString(DateConstants.DATE_FORMAT_RFC_3339);
        orderRequest.setTimeExpire(cancelStr);
        AttachVo attachVo = new AttachVo(PayConstants.PAY_SERVICE_TYPE_ORDER, order.getUid());
        orderRequest.setAttach(JSONObject.toJSONString(attachVo));
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        amount.setTotal(order.getPayPrice().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).intValue());
        orderRequest.setAmount(amount);
        return orderRequest;
    }

    /**
     * 商品购买后根据配置送券
     */
    private void autoSendCoupons(Order order) {
        List<OrderDetail> orderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
        if (CollUtil.isEmpty(orderDetailList)) {
            return;
        }
        List<CouponUser> couponUserList = CollUtil.newArrayList();
        Map<Integer, Boolean> couponMap = CollUtil.newHashMap();
        List<Integer> proIdList = orderDetailList.stream().map(OrderDetail::getProductId).distinct().collect(Collectors.toList());
        for (Integer proId : proIdList) {
            List<ProductCoupon> couponsForGiveUser = productCouponService.getListByProductId(proId);
            for (int i = 0; i < couponsForGiveUser.size(); ) {
                ProductCoupon productCoupon = couponsForGiveUser.get(i);
                MyRecord record = couponUserService.paySuccessGiveAway(productCoupon.getCouponId(), order.getUid());
                if (record.getStr("status").equals("fail")) {
                    logger.error(StrUtil.format("支付成功领取优惠券失败，失败原因：{}", record.getStr("errMsg")));
                    couponsForGiveUser.remove(i);
                    continue;
                }
                CouponUser couponUser = record.get("couponUser");
                couponUserList.add(couponUser);
                couponMap.put(couponUser.getCouponId(), record.getBoolean("isLimited"));
                i++;
            }
        }

        Boolean execute = transactionTemplate.execute(e -> {
            if (CollUtil.isNotEmpty(couponUserList)) {
                couponUserService.saveBatch(couponUserList);
                couponUserList.forEach(i -> couponService.deduction(i.getCouponId(), 1, couponMap.get(i.getCouponId())));
            }
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error(StrUtil.format("支付成功领取优惠券，更新数据库失败，订单编号：{}", order.getOrderNo()));
        }
    }

    /**
     * 处理会员积分
     * @param orderList 订单列表
     * @param user 用户信息
     */
    private void processMemberIntegral(List<Order> orderList, User user) {
        for (Order order : orderList) {
            try {
                // 获取对应的商户订单
                MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(order.getOrderNo());
                if (merchantOrder == null) {
                    logger.error("商户订单不存在，跳过积分处理，订单号: {}", order.getOrderNo());
                    continue;
                }

                // 先处理店铺会员积分扣减（如果有使用）
                if (merchantOrder.getUsedMemberIntegral() != null && merchantOrder.getUsedMemberIntegral() > 0) {
                    try {
                        Member member = memberService.getByUidAndMerId(user.getId(), order.getMerId());
                        if (member != null && member.getIntegral() >= merchantOrder.getUsedMemberIntegral()) {
                            // 扣减会员积分
                            member.setIntegral(member.getIntegral() - merchantOrder.getUsedMemberIntegral());
                            memberService.updateById(member);

                            logger.info("支付成功，扣减店铺会员积分：用户ID={}, 商户ID={}, 积分={}, 订单号={}",
                                    user.getId(), order.getMerId(), merchantOrder.getUsedMemberIntegral(), order.getOrderNo());
                        } else {
                            logger.warn("扣减店铺会员积分失败，积分不足：用户ID={}, 商户ID={}, 需要积分={}, 实际积分={}, 订单号={}",
                                    user.getId(), order.getMerId(), merchantOrder.getUsedMemberIntegral(),
                                    member != null ? member.getIntegral() : 0, order.getOrderNo());
                        }
                    } catch (Exception e) {
                        logger.error("扣减店铺会员积分异常：用户ID={}, 商户ID={}, 积分={}, 订单号={}, 错误={}",
                                user.getId(), order.getMerId(), merchantOrder.getUsedMemberIntegral(), order.getOrderNo(), e.getMessage());
                    }
                }

                // 获取商户的积分规则
                com.zbkj.common.model.member.MemberIntegralRule integralRule = memberIntegralRuleService.getByMerId(order.getMerId());
                if (integralRule == null || integralRule.getMoneyToIntegral() == null ||
                        integralRule.getMoneyToIntegral().compareTo(BigDecimal.ZERO) <= 0) {
                    logger.debug("商户积分规则未配置或无效，跳过积分处理，商户ID: {}, 订单号: {}", order.getMerId(), order.getOrderNo());
                    continue;
                }

                // 使用商户订单的支付金额计算积分（而不是平台订单的金额）
                BigDecimal orderAmount = merchantOrder.getPayPrice() != null ? merchantOrder.getPayPrice() : BigDecimal.ZERO;
                if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    logger.debug("商户订单金额为0，跳过积分处理，订单号: {}", order.getOrderNo());
                    continue;
                }

                BigDecimal integralDecimal = orderAmount.divide(integralRule.getMoneyToIntegral(), 0, BigDecimal.ROUND_DOWN);
                Integer integral = integralDecimal.intValue();

                if (integral <= 0) {
                    logger.debug("计算得到的积分为0，跳过积分处理，订单号: {}, 商户订单金额: {}, 积分规则: {}",
                            order.getOrderNo(), orderAmount, integralRule.getMoneyToIntegral());
                    continue;
                }

                // 调用会员服务更新积分和等级
                boolean success = memberService.consumeUpdateIntegralAndLevel(
                        user.getId(),
                        order.getMerId(),
                        order.getId(),
                        order.getOrderNo(),
                        integral
                );

                if (success) {
                    logger.info("会员积分处理成功，用户ID: {}, 商户ID: {}, 订单号: {}, 商户订单金额: {}, 获得积分: {}",
                            user.getId(), order.getMerId(), order.getOrderNo(), orderAmount, integral);
                } else {
                    logger.error("会员积分处理失败，用户ID: {}, 商户ID: {}, 订单号: {}",
                            user.getId(), order.getMerId(), order.getOrderNo());
                }

            } catch (Exception e) {
                logger.error("处理会员积分异常，订单号: {}, 错误: {}", order.getOrderNo(), e.getMessage(), e);
                // 积分处理失败不影响主流程，只记录日志
            }
        }
    }

    /**
     * 银联支付
     *
     * @param order 订单信息
     * @param orderPayRequest 支付请求
     * @return 银联支付表单HTML
     */
    private String unionPayment(Order order, OrderPayRequest orderPayRequest) {
        try {
            logger.info("银联支付开始，订单号: {}, 金额: {}", order.getOrderNo(), order.getPayPrice());
            
            // 如果指定了银行卡ID，使用无跳转支付
            if (orderPayRequest.getBankCardId() != null) {
                Integer uid = userService.getUserIdException();
                UserBankCard bankCard = userBankCardService.getBankCardById(uid, orderPayRequest.getBankCardId());
                if (bankCard == null) {
                    throw new CrmebException("指定的银行卡不存在");
                }
                
                logger.info("使用绑定银行卡支付（无跳转），银行卡ID: {}, 卡号后4位: {}", 
                           bankCard.getId(), bankCard.getCardNoLast4());
                
                // 使用银行卡信息进行无跳转支付
                // 测试环境使用固定短信验证码
                String smsCode = "111111";
                return unionPayService.unionPay(order, bankCard, smsCode);
            } else {
                // 没有银行卡信息，使用普通跳转支付
                logger.info("使用银联跳转支付");
                return unionPayService.unionPay(order);
            }
            
        } catch (Exception e) {
            logger.error("银联支付异常，订单号: {}", order.getOrderNo(), e);
            throw new CrmebException("银联支付发起失败：" + e.getMessage());
        }
    }
}


