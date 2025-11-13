package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.wechat.video.PayComponentOrder;
import com.zbkj.service.dao.PayComponentOrderDao;
import com.zbkj.service.service.PayComponentOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Service
public class PayComponentOrderServiceImpl extends ServiceImpl<PayComponentOrderDao, PayComponentOrder> implements PayComponentOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PayComponentOrderServiceImpl.class);

    @Resource
    private PayComponentOrderDao dao;

//    @Autowired
//    private WechatVideoOrderService wechatVideoOrderService;
//    @Autowired
//    private TransactionTemplate transactionTemplate;
//    @Autowired
//    private PayComponentOrderProductService componentOrderProductService;
//    @Autowired
//    private WechatVideoAftersaleService wechatVideoAftersaleService;

//    /**
//     * 创建组件订单
//     * @param shopOrderAddVo 创建订单参数
//     * @return String
//     */
//    @Override
//    public ShopOrderAddResultVo create(ShopOrderAddVo shopOrderAddVo) {
//        ShopOrderAddResultVo addResultVo = wechatVideoOrderService.shopOrderAdd(shopOrderAddVo);
//        // 生成订单表数据
//        PayComponentOrder order = new PayComponentOrder();
//        order.setOrderNo(shopOrderAddVo.getOutOrderId());
//        order.setComponentOrderId(addResultVo.getOrderId());
//        order.setOpenid(shopOrderAddVo.getOpenid());
//        order.setPath(shopOrderAddVo.getPath());
////        order.setScene(shopOrderAddVo.getScene());
//        order.setOutUserId(shopOrderAddVo.getOutUserId());
//        ShopOrderPayInfoAddVo payInfo = shopOrderAddVo.getOrderDetail().getPayInfo();
//        order.setPayMethod(payInfo.getPayMethod());
//        order.setPayMethodType(payInfo.getPayMethodType());
//        order.setPrepayId(payInfo.getPrepayId());
//        order.setPrepayTime(payInfo.getPrepayTime());
//        ShopOrderPriceInfoVo priceInfo = shopOrderAddVo.getOrderDetail().getPriceInfo();
//        order.setOrderPrice(priceInfo.getOrderPrice());
//        order.setFreight(priceInfo.getFreight());
//        order.setDiscountedPrice(priceInfo.getDiscountedPrice());
//        order.setDeliveryType(shopOrderAddVo.getDeliveryDetail().getDeliveryType());
//        ShopOrderAddressInfoAddVo addressInfo = shopOrderAddVo.getAddressInfo();
//        order.setReceiverName(addressInfo.getReceiverName());
//        order.setTelNumber(addressInfo.getTelNumber());
//        order.setDetailedAddress(addressInfo.getDetailedAddress());
//        order.setTicket(addResultVo.getTicket());
//        order.setTicketExpireTime(addResultVo.getTicketExpireTime());
//        order.setFinalPrice(priceInfo.getOrderPrice() + priceInfo.getFreight());
//        order.setStatus(10);
//        order.setFinishAllDelivery(0);
//        order.setCreateTime(CrmebDateUtil.strToDate(shopOrderAddVo.getCreateTime(), DateConstants.DATE_FORMAT));
//        order.setOutUserId(shopOrderAddVo.getOutUserId());
//        order.setMerId(shopOrderAddVo.getMerId());
//
//        // 订单详情
//        List<ShopOrderProductInfoAddVo> productInfos = shopOrderAddVo.getOrderDetail().getProductInfos();
//        List<PayComponentOrderProduct> orderProductList = productInfos.stream().map(productInfo -> {
//            PayComponentOrderProduct orderProduct = new PayComponentOrderProduct();
//            orderProduct.setOrderNo(order.getOrderNo());
//            orderProduct.setComponentOrderId(order.getComponentOrderId());
////            orderProduct.setProductId(Integer.valueOf());
//            orderProduct.setOutProductId(productInfo.getOutProductId());
////            orderProduct.setSkuId();
//            orderProduct.setOutSkuId(productInfo.getOutSkuId());
//            orderProduct.setProductCnt(productInfo.getProductCnt());
//            orderProduct.setSalePrice(productInfo.getSalePrice());
//            orderProduct.setHeadImg(productInfo.getHeadImg());
//            orderProduct.setTitle(productInfo.getTitle());
//            orderProduct.setPath(productInfo.getPath());
//            orderProduct.setCreateTime(CrmebDateUtil.nowDateTime());
//            return orderProduct;
//        }).collect(Collectors.toList());
//
//        Boolean execute = transactionTemplate.execute(e -> {
//            save(order);
//            componentOrderProductService.saveBatch(orderProductList);
//            return Boolean.TRUE;
//        });
//        if (!execute) {
//            throw new CrmebException("保存组件订单失败");
//        }
//        return addResultVo;
//    }
//
//    /**
//     * 通过订单号获取订单
//     * @param orderNo 订单编号
//     * @return PayComponentOrder
//     */
//    @Override
//    public PayComponentOrder getByOrderNo(String orderNo) {
//        LambdaQueryWrapper<PayComponentOrder> lqw = Wrappers.lambdaQuery();
//        lqw.eq(PayComponentOrder::getOrderNo, orderNo);
//        return dao.selectOne(lqw);
//    }
//
//    /**
//     * 创建售后
//     * @param orderNo 订单编号
//     */
//    @Override
//    public void createAfterSale(String orderNo) {
//        PayComponentOrder order = getByOrderNo(orderNo);
//        if (ObjectUtil.isNull(order)) {
//            log.error("视频号订单创建售后未找到订单，订单编号=" + orderNo);
//            return;
//        }
//        List<PayComponentOrderProduct> orderProductList = componentOrderProductService.getListByOrderNo(orderNo);
//
//        ShopAftersaleAddVo shopAftersaleAddVo = new ShopAftersaleAddVo();
//        shopAftersaleAddVo.setOutOrderId(orderNo);
//        shopAftersaleAddVo.setOutAftersaleId(orderNo);
//        shopAftersaleAddVo.setOpenid(order.getOpenid());
//        shopAftersaleAddVo.setType(1);// 1:退款,2:退款退货,3:换货
//        shopAftersaleAddVo.setCreateTime(CrmebDateUtil.nowDate());
//        shopAftersaleAddVo.setStatus(2);// 2-商家受理中
//        shopAftersaleAddVo.setFinishAllAftersale(0);
//        shopAftersaleAddVo.setPath(StrUtil.format("/pages/order_details/index?order_id={}&type={}", orderNo, "video"));
//        List<AftersaleProductInfoVo> infoVoList = orderProductList.stream().map(product -> {
//            AftersaleProductInfoVo infoVo = new AftersaleProductInfoVo();
//            infoVo.setOutProductId(product.getOutProductId());
//            infoVo.setOutSkuId(product.getOutSkuId());
//            infoVo.setProductCnt(product.getProductCnt());
//            return infoVo;
//        }).collect(Collectors.toList());
//        shopAftersaleAddVo.setProductInfos(infoVoList);
//        Boolean add = wechatVideoAftersaleService.shopAftersaleAdd(shopAftersaleAddVo);
//        if (!add) {// 创建售后失败
//            log.error("视频号订单创建售后失败，orderNo=" + orderNo);
//            throw new CrmebException("视频号订单创建售后失败，orderNo=" + orderNo);
//        }
//        // 创建售后成功,更新售后
//        ShopAftersaleUpdateVo updateVo = new ShopAftersaleUpdateVo();
//        updateVo.setOutOrderId(orderNo);
//        updateVo.setOpenid(order.getOpenid());
//        updateVo.setOutAftersaleId(orderNo);
//        updateVo.setStatus(13);
//        updateVo.setFinishAllAftersale(1);
//        Boolean update = wechatVideoAftersaleService.shopAftersaleUpdate(updateVo);
//        if (!update) {
//            log.error("视频号订单更新售后失败，orderNo=" + orderNo);
//            throw new CrmebException("视频号订单更新售后失败，orderNo=" + orderNo);
//        }
//    }
}

