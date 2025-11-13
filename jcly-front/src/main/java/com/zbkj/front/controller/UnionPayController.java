package com.zbkj.front.controller;

import com.zbkj.common.model.order.Order;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.BankCardVerifyRequestVo;
import com.zbkj.common.vo.BankCardVerifyResponseVo;
import org.apache.commons.lang3.StringUtils;
import com.zbkj.service.service.OrderService;
import com.zbkj.service.service.UnionPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/front/unionpay")
@Api(tags = "银联支付控制器")
public class UnionPayController {

    @Autowired
    private UnionPayService unionpayService;
    
    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "银联支付")
    @GetMapping(value = "/pay")
    public void pay(HttpServletResponse response, 
                   @ApiParam(value = "订单号", required = false) @RequestParam(value = "orderNo", required = false) String orderNo,
                   @ApiParam(value = "支付金额", required = false) @RequestParam(value = "amount", required = false) BigDecimal amount) throws Exception{
        response.setHeader("content-type", "text/html;charset=UTF-8");
        
        Order order;
        if (orderNo != null && !orderNo.isEmpty()) {
            // 使用真实订单
            order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                response.getWriter().write("<h3>订单不存在：" + orderNo + "</h3>");
                return;
            }
        } else {
            // 创建测试订单
            order = new Order();
            order.setPayPrice(amount != null ? amount : new BigDecimal("1.00")); // 默认1元测试
            order.setPayType("unionpay");
            order.setOrderNo("TEST" + System.currentTimeMillis()); // 生成测试订单号
            log.info("创建测试银联支付订单：{}, 金额：{}", order.getOrderNo(), order.getPayPrice());
        }
        
        try {
            String payForm = unionpayService.unionPay(order);
            response.getWriter().write(payForm);
            response.getWriter().flush();
        } catch (Exception e) {
            log.error("银联支付发起失败", e);
            response.getWriter().write("<h3>支付发起失败：" + e.getMessage() + "</h3>");
        } finally {
            response.getWriter().close();
        }
    }

    @ApiOperation(value = "查询银联支付订单状态")
    @GetMapping(value = "/query")
    public CommonResult<Map<String, String>> queryOrder(
            @ApiParam(value = "商户订单号", required = true) @RequestParam String orderId,
            @ApiParam(value = "原交易时间", required = true) @RequestParam String txnTime) {
        
        try {
            Map<String, String> result = unionpayService.queryOrder(orderId, txnTime);
            if (result != null) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("查询失败");
            }
        } catch (Exception e) {
            log.error("银联支付订单查询异常", e);
            return CommonResult.failed("查询异常：" + e.getMessage());
        }
    }

    /**
     * 银联支付退款（消费撤销）
     */
    @ApiOperation(value = "银联支付退款")
    @PostMapping(value = "/refund")
    public CommonResult<Map<String, String>> refundOrder(
            @ApiParam(value = "商户订单号", required = true) @RequestParam String orderNo,
            @ApiParam(value = "原交易时间", required = true) @RequestParam String origTxnTime,
            @ApiParam(value = "退款金额", required = true) @RequestParam BigDecimal refundAmount) {
        try {
            log.info("银联支付退款请求，订单号: {}, 原交易时间: {}, 退款金额: {}", orderNo, origTxnTime, refundAmount);
            
            // 参数校验
            if (StringUtils.isBlank(orderNo)) {
                return CommonResult.failed("订单号不能为空");
            }
            if (StringUtils.isBlank(origTxnTime)) {
                return CommonResult.failed("原交易时间不能为空");
            }
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return CommonResult.failed("退款金额必须大于0");
            }
            
            // 检查订单是否存在
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                return CommonResult.failed("订单不存在");
            }
            
            // 检查订单状态是否允许退款
            if (!order.getPaid()) {
                return CommonResult.failed("订单未支付，无法退款");
            }
            
            // 检查退款金额是否超过订单金额
            if (refundAmount.compareTo(order.getPayPrice()) > 0) {
                return CommonResult.failed("退款金额不能超过订单金额");
            }
            
            // 调用银联退款接口
            Map<String, String> result = unionpayService.refundOrder(orderNo, origTxnTime, refundAmount);
            
            if (result != null) {
                String refundStatus = result.get("refundStatus");
                if ("SUCCESS".equals(refundStatus)) {
                    log.info("银联退款成功，订单号: {}, 退款金额: {}", orderNo, refundAmount);
                    return CommonResult.success(result);
                } else {
                    log.warn("银联退款失败，订单号: {}, 失败原因: {}", orderNo, result.get("refundMessage"));
                    return CommonResult.failed("退款失败：" + result.get("refundMessage"));
                }
            } else {
                return CommonResult.failed("退款请求失败");
            }
            
        } catch (Exception e) {
            log.error("银联支付退款异常", e);
            return CommonResult.failed("退款异常：" + e.getMessage());
        }
    }

    /**
     * 银联支付退货
     */
    @ApiOperation(value = "银联支付退货")
    @PostMapping(value = "/return")
    public CommonResult<Map<String, String>> returnOrder(
            @ApiParam(value = "原交易查询流水号", required = true) @RequestParam String origQryId,
            @ApiParam(value = "退货金额", required = true) @RequestParam BigDecimal returnAmount) {
        try {
            log.info("银联支付退货请求，原交易查询流水号: {}, 退货金额: {}", origQryId, returnAmount);
            
            // 参数校验
            if (StringUtils.isBlank(origQryId)) {
                return CommonResult.failed("原交易查询流水号不能为空");
            }
            if (returnAmount == null || returnAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return CommonResult.failed("退货金额必须大于0");
            }
            
            // 调用银联退货接口
            Map<String, String> result = unionpayService.returnOrder(origQryId, returnAmount);
            
            if (result != null) {
                String returnStatus = result.get("returnStatus");
                if ("SUCCESS".equals(returnStatus)) {
                    log.info("银联退货成功，原交易查询流水号: {}, 退货金额: {}", origQryId, returnAmount);
                    return CommonResult.success(result);
                } else if ("PROCESSING".equals(returnStatus)) {
                    log.info("银联退货处理中，原交易查询流水号: {}, 退货金额: {}", origQryId, returnAmount);
                    return CommonResult.success(result);
                } else {
                    log.warn("银联退货失败，原交易查询流水号: {}, 失败原因: {}", origQryId, result.get("returnMessage"));
                    return CommonResult.failed("退货失败：" + result.get("returnMessage"));
                }
            } else {
                return CommonResult.failed("退货请求失败");
            }
            
        } catch (Exception e) {
            log.error("银联支付退货异常", e);
            return CommonResult.failed("退货异常：" + e.getMessage());
        }
    }

    /**
     * 银行卡信息验证
     */
    @ApiOperation(value = "银行卡信息验证")
    @PostMapping(value = "/verify-bankcard")
    public CommonResult<BankCardVerifyResponseVo> verifyBankCard(@RequestBody BankCardVerifyRequestVo request) {
        try {
            log.info("银行卡验证请求，验证类型: {}", request.getVerifyType());
            
            // 调用银行卡验证服务
            BankCardVerifyResponseVo result = unionpayService.verifyBankCard(request);
            
            if (result != null) {
                if (result.getVerifyResult()) {
                    log.info("银行卡验证成功，响应码: {}", result.getResponseCode());
                    return CommonResult.success(result);
                } else {
                    log.warn("银行卡验证失败，响应码: {}, 失败原因: {}", 
                            result.getResponseCode(), result.getMessage());
                    return CommonResult.success(result); // 验证失败也返回success，让前端根据verifyResult判断
                }
            } else {
                return CommonResult.failed("银行卡验证请求失败");
            }
            
        } catch (Exception e) {
            log.error("银行卡验证异常", e);
            return CommonResult.failed("银行卡验证异常：" + e.getMessage());
        }
    }
}
