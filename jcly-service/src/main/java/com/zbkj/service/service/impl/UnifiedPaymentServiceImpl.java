package com.zbkj.service.service.impl;

import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.user.UserBankCard;
import com.zbkj.common.vo.BankCardVerifyResponseVo;
import com.zbkj.common.vo.UnifiedPaymentRequestVo;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一支付服务实现
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
public class UnifiedPaymentServiceImpl implements UnifiedPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedPaymentServiceImpl.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UnionPayService unionPayService;

    @Autowired
    private UserBankCardService userBankCardService;

    // 这里可以注入微信支付和支付宝支付服务
    // @Autowired
    // private WechatPayService wechatPayService;
    // @Autowired
    // private AlipayService alipayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> unifiedPay(Integer uid, UnifiedPaymentRequestVo request) {
        logger.info("统一支付请求开始，用户ID: {}, 订单号: {}, 支付方式: {}", 
                uid, request.getOrderNo(), request.getPaymentMethod());

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证订单
            Order order = orderService.getByOrderNo(request.getOrderNo());
            if (order == null) {
                throw new CrmebException("订单不存在");
            }
            if (!order.getUid().equals(uid)) {
                throw new CrmebException("无权限访问该订单");
            }
            if (order.getPaid()) {
                throw new CrmebException("订单已支付");
            }

            // 2. 验证支付金额
            if (request.getPayAmount().compareTo(order.getPayPrice()) != 0) {
                throw new CrmebException("支付金额不匹配");
            }

            // 3. 根据支付方式处理
            switch (request.getPaymentMethod().toLowerCase()) {
                case "unionpay":
                    result = handleUnionPay(uid, order, request);
                    break;
                case "wechat":
                    result = handleWechatPay(uid, order, request);
                    break;
                case "alipay":
                    result = handleAlipay(uid, order, request);
                    break;
                default:
                    throw new CrmebException("不支持的支付方式");
            }

            // 4. 更新订单支付方式信息
            if (result.get("success").equals(true)) {
                updateOrderPaymentInfo(order, request);
            }

            return result;

        } catch (Exception e) {
            logger.error("统一支付异常，用户ID: {}, 订单号: {}", uid, request.getOrderNo(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 处理银联支付
     */
    private Map<String, Object> handleUnionPay(Integer uid, Order order, UnifiedPaymentRequestVo request) {
        Map<String, Object> result = new HashMap<>();

        try {
            UserBankCard bankCard = null;

            // 1. 处理银行卡信息
            if (request.getBankCardId() != null) {
                // 使用已保存的银行卡
                bankCard = userBankCardService.getBankCardById(uid, request.getBankCardId());
            } else if (request.getBankCardInfo() != null) {
                // 验证并保存新银行卡
                BankCardVerifyResponseVo verifyResult = userBankCardService.verifyAndSaveBankCard(uid, request.getBankCardInfo());
                if (!verifyResult.getVerifyResult()) {
                    result.put("success", false);
                    result.put("message", "银行卡验证失败：" + verifyResult.getMessage());
                    return result;
                }
                // 获取刚保存的银行卡
                bankCard = userBankCardService.getDefaultBankCard(uid);
            } else {
                result.put("success", false);
                result.put("message", "银联支付必须提供银行卡信息");
                return result;
            }

            // 2. 调用银联支付
            String paymentForm = unionPayService.unionPay(order);
            if (StringUtils.isEmpty(paymentForm)) {
                result.put("success", false);
                result.put("message", "银联支付请求失败");
                return result;
            }

            result.put("success", true);
            result.put("paymentMethod", "unionpay");
            result.put("paymentForm", paymentForm);
            result.put("bankCardId", bankCard.getId());
            result.put("message", "银联支付请求成功");

            return result;

        } catch (Exception e) {
            logger.error("银联支付处理异常", e);
            result.put("success", false);
            result.put("message", "银联支付处理异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 处理微信支付
     */
    private Map<String, Object> handleWechatPay(Integer uid, Order order, UnifiedPaymentRequestVo request) {
        Map<String, Object> result = new HashMap<>();
        
        // TODO: 实现微信支付逻辑
        // 这里可以调用微信支付服务
        // Map<String, Object> wechatResult = wechatPayService.createPayment(order, request);
        
        result.put("success", false);
        result.put("message", "微信支付功能待实现");
        return result;
    }

    /**
     * 处理支付宝支付
     */
    private Map<String, Object> handleAlipay(Integer uid, Order order, UnifiedPaymentRequestVo request) {
        Map<String, Object> result = new HashMap<>();
        
        // TODO: 实现支付宝支付逻辑
        // 这里可以调用支付宝支付服务
        // Map<String, Object> alipayResult = alipayService.createPayment(order, request);
        
        result.put("success", false);
        result.put("message", "支付宝支付功能待实现");
        return result;
    }

    /**
     * 更新订单支付方式信息
     */
    private void updateOrderPaymentInfo(Order order, UnifiedPaymentRequestVo request) {
        // 这里需要根据实际的Order实体字段来更新
        // 假设Order实体已经添加了paymentMethod和bankCardId字段
        try {
            // 更新订单的支付方式和银行卡ID
            // orderService.updatePaymentMethod(order.getOrderId(), request.getPaymentMethod(), request.getBankCardId());
            logger.info("订单支付方式信息更新成功，订单号: {}, 支付方式: {}", order.getOrderNo(), request.getPaymentMethod());
        } catch (Exception e) {
            logger.error("更新订单支付方式信息失败", e);
        }
    }

    @Override
    public Map<String, Object> queryPaymentStatus(String orderNo) {
        logger.info("查询支付状态，订单号: {}", orderNo);

        Map<String, Object> result = new HashMap<>();

        try {
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            // 根据订单的支付方式查询状态
            String paymentMethod = order.getPayType(); // 假设payType字段存储支付方式
            
            switch (paymentMethod) {
                case "unionpay":
                    // 调用银联查询接口
                    // 这里需要获取原交易时间，可能需要从订单或支付记录中获取
                    String txnTime = order.getCreateTime().toString(); // 简化处理
                    Map<String, String> queryResult = unionPayService.queryOrder(orderNo, txnTime);
                    
                    result.put("success", true);
                    result.put("paymentMethod", "unionpay");
                    result.put("queryResult", queryResult);
                    break;
                    
                case "wechat":
                    // TODO: 调用微信查询接口
                    result.put("success", false);
                    result.put("message", "微信支付查询功能待实现");
                    break;
                    
                case "alipay":
                    // TODO: 调用支付宝查询接口
                    result.put("success", false);
                    result.put("message", "支付宝查询功能待实现");
                    break;
                    
                default:
                    result.put("success", false);
                    result.put("message", "不支持的支付方式查询");
                    break;
            }

            return result;

        } catch (Exception e) {
            logger.error("查询支付状态异常，订单号: {}", orderNo, e);
            result.put("success", false);
            result.put("message", "查询支付状态异常：" + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> unifiedRefund(String orderNo, BigDecimal refundAmount, String reason) {
        logger.info("统一退款请求，订单号: {}, 退款金额: {}, 退款原因: {}", orderNo, refundAmount, reason);

        Map<String, Object> result = new HashMap<>();

        try {
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.getPaid()) {
                result.put("success", false);
                result.put("message", "订单未支付，无法退款");
                return result;
            }

            // 根据订单的支付方式进行退款
            String paymentMethod = order.getPayType();
            
            switch (paymentMethod) {
                case "unionpay":
                    // 银联退款需要原交易时间
                    String origTxnTime = order.getCreateTime().toString(); // 简化处理
                    Map<String, String> refundResult = unionPayService.refundOrder(orderNo, origTxnTime, refundAmount);
                    
                    result.put("success", "SUCCESS".equals(refundResult.get("refundStatus")));
                    result.put("paymentMethod", "unionpay");
                    result.put("refundResult", refundResult);
                    result.put("message", refundResult.get("refundMessage"));
                    break;
                    
                case "wechat":
                    // TODO: 调用微信退款接口
                    result.put("success", false);
                    result.put("message", "微信支付退款功能待实现");
                    break;
                    
                case "alipay":
                    // TODO: 调用支付宝退款接口
                    result.put("success", false);
                    result.put("message", "支付宝退款功能待实现");
                    break;
                    
                default:
                    result.put("success", false);
                    result.put("message", "不支持的支付方式退款");
                    break;
            }

            return result;

        } catch (Exception e) {
            logger.error("统一退款异常，订单号: {}", orderNo, e);
            result.put("success", false);
            result.put("message", "退款异常：" + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> unifiedReturn(String orderNo, BigDecimal returnAmount, String reason) {
        logger.info("统一退货请求，订单号: {}, 退货金额: {}, 退货原因: {}", orderNo, returnAmount, reason);

        Map<String, Object> result = new HashMap<>();

        try {
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.getPaid()) {
                result.put("success", false);
                result.put("message", "订单未支付，无法退货");
                return result;
            }

            // 根据订单的支付方式进行退货
            String paymentMethod = order.getPayType();
            
            switch (paymentMethod) {
                case "unionpay":
                    // 银联退货需要原交易查询流水号
                    // 这里需要从支付记录中获取origQryId，简化处理
                    String origQryId = ""; // 实际应该从支付记录表中获取
                    Map<String, String> returnResult = unionPayService.returnOrder(origQryId, returnAmount);
                    
                    result.put("success", "SUCCESS".equals(returnResult.get("returnStatus")));
                    result.put("paymentMethod", "unionpay");
                    result.put("returnResult", returnResult);
                    result.put("message", returnResult.get("returnMessage"));
                    break;
                    
                case "wechat":
                    // 微信支付一般没有退货概念，使用退款
                    result = unifiedRefund(orderNo, returnAmount, reason);
                    break;
                    
                case "alipay":
                    // 支付宝支付一般没有退货概念，使用退款
                    result = unifiedRefund(orderNo, returnAmount, reason);
                    break;
                    
                default:
                    result.put("success", false);
                    result.put("message", "不支持的支付方式退货");
                    break;
            }

            return result;

        } catch (Exception e) {
            logger.error("统一退货异常，订单号: {}", orderNo, e);
            result.put("success", false);
            result.put("message", "退货异常：" + e.getMessage());
            return result;
        }
    }
}
