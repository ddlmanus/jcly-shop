package com.zbkj.service.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.delivery.GetDeliveryListResponse;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.member.PaidMemberOrder;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.model.order.RechargeOrder;
import com.zbkj.common.model.user.UserToken;
import com.zbkj.common.model.wechat.WechatPayInfo;
import com.zbkj.common.response.OrderInvoiceResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信订单发货管理服务实现类
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/11/27
 */
@Service
public class WechatOrderShippingServiceImpl implements WechatOrderShippingService {

    private static final Logger logger = LoggerFactory.getLogger(WechatOrderShippingServiceImpl.class);

    @Autowired
    private WechatPayInfoService wechatPayInfoService;
    @Autowired
    private UserTokenService userTokenService;
    @Autowired
    private OrderInvoiceService orderInvoiceService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MerchantOrderService merchantOrderService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private PaidMemberOrderService paidMemberOrderService;
    @Autowired
    private WxMaService wxMaService;

    /**
     * 录入发货信息
     */
    @Override
    public void uploadShippingInfo(String orderNo) {
        uploadShippingInfo(orderNo, "normal");
    }

    /**
     * 录入发货信息
     * @param type normal-正常订单，verify-核销订单，fictitious-虚拟订单
     */
    @Override
    public void uploadShippingInfo(String orderNo, String type) {
        Order order = orderService.getByOrderNo(orderNo);

        if (order.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_CLOUD)
                || order.getSecondType().equals(OrderConstants.ORDER_SECOND_TYPE_CDKEY)) {
            type = "fictitious";
        }
        int logisticsType = 0;
        int deliveryMode = 1;
        boolean isAllDelivered = true;
        switch (type) {
            case "fictitious":
                logisticsType = 3;
                break;
            case "verify":
                logisticsType = 4;
                break;
            case "normal":
                logisticsType = 1;
                deliveryMode = 2;
                isAllDelivered = false;
                break;
        }

        OrderKeyBean orderKeyBean = getOrderKeyBean(order.getOutTradeNo());
        PayerBean payerBean = getPayerBean(order.getUid());
        List<ShippingListBean> shippingListBeanList = getShippingListBeanList(orderNo, type);
        WxMaOrderShippingInfoUploadRequest uploadRequest = new WxMaOrderShippingInfoUploadRequest();
        uploadRequest.setOrderKey(orderKeyBean);
        uploadRequest.setLogisticsType(logisticsType);
        uploadRequest.setDeliveryMode(deliveryMode);
        uploadRequest.setIsAllDelivered(isAllDelivered);
        uploadRequest.setUploadTime(CrmebDateUtil.dateToStr(new Date(), DateConstants.DATE_FORMAT_RFC_3339));
        uploadRequest.setPayer(payerBean);
        uploadRequest.setShippingList(shippingListBeanList);
        if (type.equals("normal")) {
            // 判断订单是否全部发货
            if (orderService.isAllSendGoods(order.getPlatOrderNo())) {
                uploadRequest.setIsAllDelivered(true);
            }
        }
        uploadShippingInfo(uploadRequest);
    }

    private void uploadShippingInfo(WxMaOrderShippingInfoUploadRequest uploadRequest) {
        try {
            wxMaService.getWxMaOrderShippingService().upload(uploadRequest);
        } catch (Exception e) {
            logger.error("微信小程序上传发货管理，失败", e);
        }
    }

    /**
     * @param type normal-正常订单，verify-核销订单，fictitious-虚拟订单
     */
    private List<ShippingListBean> getShippingListBeanList(String orderNo, String type) {
        List<ShippingListBean> shippingListBeanList = new ArrayList<>();
        if (type.equals("fictitious") || type.equals("verify")) {
            ShippingListBean shippingListBean = new ShippingListBean();
            List<OrderDetail> orderDetailList = orderDetailService.getByOrderNo(orderNo);
            String collect = orderDetailList.stream().map(d -> StrUtil.format("{}*{}件", d.getProductName(), d.getPayNum())).collect(Collectors.joining(","));
            if (collect.length() > 120) {
                collect = collect.substring(0, 119);
            }
            shippingListBean.setItemDesc(collect);
            shippingListBeanList.add(shippingListBean);
        }
        if (type.equals("normal")) {
            List<OrderInvoiceResponse> invoiceResponseList = orderInvoiceService.findByOrderNo(orderNo);
            if (CollUtil.isEmpty(invoiceResponseList)) {
                throw new CrmebException("未找到发货单");
            }
            shippingListBeanList = invoiceResponseList.stream().map(e -> {
                ShippingListBean shippingListBean = new ShippingListBean();
                shippingListBean.setTrackingNo(e.getTrackingNumber());
                String expressCompany = getWechatDeliveryIdByOrderExpressName(e.getExpressName());
                shippingListBean.setExpressCompany(expressCompany);
                String collect = e.getDetailList().stream().map(d -> StrUtil.format("{}*{}件", d.getProductName(), d.getNum())).collect(Collectors.joining(","));
                if (collect.length() > 120) {
                    collect = collect.substring(0, 119);
                }
                shippingListBean.setItemDesc(collect);
                if (expressCompany.equals("SF")) {
                    MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(orderNo);
                    ContactBean contactDto = new ContactBean();
                    contactDto.setReceiverContact(CrmebUtil.maskMobile(merchantOrder.getUserPhone()));
                    shippingListBean.setContact(contactDto);
                }
                return shippingListBean;
            }).collect(Collectors.toList());
        }

        return shippingListBeanList;
    }

    private List<ShippingListBean> getShippingListBeanList(BigDecimal price, BigDecimal givePrice, String type) {
        List<ShippingListBean> shippingListBeanList = new ArrayList<>();
        ShippingListBean shippingListBean = new ShippingListBean();
        if (type.equals("recharge")) {
            shippingListBean.setItemDesc(StrUtil.format("用户充值{}余额,赠送{}", price, givePrice));
            shippingListBeanList.add(shippingListBean);
        }
        if (type.equals("svip")) {
            shippingListBean.setItemDesc(StrUtil.format("用户花费{}购买SVIP会员,赠送{}余额", price, givePrice));
            shippingListBeanList.add(shippingListBean);
        }
        return shippingListBeanList;
    }

    private PayerBean getPayerBean(Integer uid) {
        UserToken userToken = userTokenService.getTokenByUserId(uid, UserConstants.USER_TOKEN_TYPE_ROUTINE);
        if (ObjectUtil.isNull(userToken)) {
            throw new CrmebException("未找到用户对应的小程序openID");
        }
        PayerBean payerDto = new PayerBean();
        payerDto.setOpenid(userToken.getToken());
        return payerDto;
    }

    private OrderKeyBean getOrderKeyBean(String outTradeNo) {
        WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(outTradeNo);
        if (ObjectUtil.isNull(wechatPayInfo)) {
            throw new CrmebException("未找到对应微信订单");
        }
        OrderKeyBean orderKeyBean = new OrderKeyBean();
        orderKeyBean.setOrderNumberType(2);
        orderKeyBean.setMchId(wechatPayInfo.getMchId());
        orderKeyBean.setTransactionId(wechatPayInfo.getTransactionId());
        orderKeyBean.setOutTradeNo(outTradeNo);
        return orderKeyBean;
    }

    /**
     * 批量录入充值订单发货
     */
    @Override
    public void batchUploadRechargeOrderShipping() {
        String shippingSwitch = systemConfigService.getValueByKey(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
        if (StrUtil.isBlank(shippingSwitch) || shippingSwitch.equals("0")) {
            return;
        }
        List<RechargeOrder> rechargeOrderList = rechargeOrderService.findAwaitUploadWechatList();
        if (CollUtil.isEmpty(rechargeOrderList)) {
            return;
        }
        for (RechargeOrder rechargeOrder : rechargeOrderList) {
            OrderKeyBean orderKeyBean;
            PayerBean payerBean;
            try {
                orderKeyBean = getOrderKeyBean(rechargeOrder.getOutTradeNo());
                payerBean = getPayerBean(rechargeOrder.getUid());
            } catch (Exception e) {
                logger.error("充值订单微信小程序发货，获取对应微信订单信息错误，充值单号={}", rechargeOrder.getOrderNo());
                logger.error("充值订单微信小程序发货，获取对应微信订单信息错误，异常信息", e);
                continue;
            }
            List<ShippingListBean> shippingListBeanList = getShippingListBeanList(rechargeOrder.getPrice(), rechargeOrder.getGivePrice(), "recharge");

            WxMaOrderShippingInfoUploadRequest uploadRequest = new WxMaOrderShippingInfoUploadRequest();
            uploadRequest.setOrderKey(orderKeyBean);
            uploadRequest.setLogisticsType(3);
            uploadRequest.setDeliveryMode(1);
            uploadRequest.setIsAllDelivered(true);
            uploadRequest.setUploadTime(CrmebDateUtil.dateToStr(new Date(), DateConstants.DATE_FORMAT_RFC_3339));
            uploadRequest.setPayer(payerBean);
            uploadRequest.setShippingList(shippingListBeanList);

            uploadShippingInfo(uploadRequest);
        }
    }

    /**
     * 批量录入SVIP订单发货
     */
    @Override
    public void batchUploadSvipOrderShipping() {
        String shippingSwitch = systemConfigService.getValueByKey(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
        if (StrUtil.isBlank(shippingSwitch) || shippingSwitch.equals("0")) {
            return;
        }
        List<PaidMemberOrder> orderList = paidMemberOrderService.findAwaitUploadWechatList();
        if (CollUtil.isEmpty(orderList)) {
            return;
        }

        for (PaidMemberOrder order : orderList) {
            OrderKeyBean orderKeyBean;
            PayerBean payerBean;
            try {
                orderKeyBean = getOrderKeyBean(order.getOutTradeNo());
                payerBean = getPayerBean(order.getUid());
            } catch (Exception e) {
                logger.error("SVIP订单微信小程序发货，获取对应微信订单信息错误，充值单号={}", order.getOrderNo());
                logger.error("SVIP订单微信小程序发货，获取对应微信订单信息错误，异常信息", e);
                continue;
            }
            List<ShippingListBean> shippingListBeanList = getShippingListBeanList(order.getPrice(), order.getGiftBalance(), "svip");

            WxMaOrderShippingInfoUploadRequest uploadRequest = new WxMaOrderShippingInfoUploadRequest();
            uploadRequest.setOrderKey(orderKeyBean);
            uploadRequest.setLogisticsType(3);
            uploadRequest.setDeliveryMode(1);
            uploadRequest.setIsAllDelivered(true);
            uploadRequest.setUploadTime(CrmebDateUtil.dateToStr(new Date(), DateConstants.DATE_FORMAT_RFC_3339));
            uploadRequest.setPayer(payerBean);
            uploadRequest.setShippingList(shippingListBeanList);

            uploadShippingInfo(uploadRequest);
        }
    }

    /**
     * 获取运力公司名称通过订单发货公司
     *
     * @param expressName 订单快递公司名称
     * @return 运力公司编码
     */
    private String getWechatDeliveryIdByOrderExpressName(String expressName) {
        if (!redisUtil.exists(RedisConstants.WECHAT_MINI_DELIVERY_KEY)) {
            wechatGetDeliveryList();
        } else {
            Long hashSize = redisUtil.getHashSize(RedisConstants.WECHAT_MINI_DELIVERY_KEY);
            if (hashSize > 0) {
                wechatGetDeliveryList();
            }
        }
        if (!redisUtil.hHasKey(RedisConstants.WECHAT_MINI_DELIVERY_KEY, expressName)) {
            // 未从redis中找到对应物流公司，返回固定值,韵达物流
            return "YD";
        }
        Object object = redisUtil.hget(RedisConstants.WECHAT_MINI_DELIVERY_KEY, expressName);
        return object.toString();
    }

    /**
     * 获取微信运力ID列表
     * 例：
     * {
     *     "errcode": 0,
     *     "delivery_list": [
     *       {
     *           "delivery_id": "(AU)",
     *           "delivery_name": "Interparcel"
     *       },
     *       {
     *           "delivery_id": "BDT",
     *           "delivery_name": "八达通"
     *       },
     *       {
     *           "delivery_id": "YD",
     *           "delivery_name": "韵达速递"
     *       },
     *       ...
     *     ],
     *     "count": 1379
     * }
     */
    private void wechatGetDeliveryList() {
        try {
            GetDeliveryListResponse deliveryListResponse = wxMaService.getWxMaImmediateDeliveryService().getDeliveryList();
            List<GetDeliveryListResponse.DeliveryList> deliveryList = deliveryListResponse.getDeliveryList();
            for (int i = 0; i < deliveryList.size(); i++) {
                GetDeliveryListResponse.DeliveryList delivery = deliveryList.get(i);
                redisUtil.hset(RedisConstants.WECHAT_MINI_DELIVERY_KEY, delivery.getDeliveryName(), delivery.getDeliveryId());
            }
        } catch (Exception e) {
            logger.error("微信小程序获取运力列表失败", e);
            throw new CrmebException("微信小程序获取运力列表失败");
        }
    }
}
