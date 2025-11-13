package com.zbkj.front.controller;

import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.PayConstants;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.unionpay.SDKConstants;
import com.zbkj.service.service.OrderService;
import com.zbkj.service.service.UnionPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 银联支付回调控制器
 */
@Slf4j
@RestController
@RequestMapping(PayConstants.UNION_PAY_NOTIFY_API_URI)
@Api(tags = "银联支付回调控制器")
public class UnionPayNotifyController {

    @Autowired
    private UnionPayService unionPayService;
    
    @Autowired
    private OrderService orderService;

    /**
     * 银联支付后台通知
     */
    @ApiOperation(value = "银联支付后台通知")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.GET})
    public void notify(HttpServletRequest request, HttpServletResponse response) {
        log.info("银联支付后台通知开始");
        
        // 获取银联通知参数
        Map<String, String> notifyParams = getAllRequestParam(request);
        log.info("银联支付通知参数：{}", notifyParams);

        // 验证签名
        String validateResult = unionPayService.validate(notifyParams, "UTF-8");
        if (!Constants.SUCCESS.equals(validateResult)) {
            log.error("银联支付通知验签失败");
            writeResponse(response, "fail");
            return;
        }

        // 获取关键参数
        String respCode = notifyParams.get("respCode");
        String orderId = notifyParams.get("orderId");
        String queryId = notifyParams.get("queryId");
        String txnAmt = notifyParams.get("txnAmt");
        String settleAmt = notifyParams.get("settleAmt");
        // String txnTime = notifyParams.get("txnTime"); // 交易时间，暂未使用
        // String traceNo = notifyParams.get("traceNo"); // 系统跟踪号，暂未使用
        
        log.info("银联支付通知 - 订单号：{}，应答码：{}，交易查询流水号：{}，交易金额：{}，清算金额：{}", 
                orderId, respCode, queryId, txnAmt, settleAmt);

        try {
            // 检查订单是否存在
            Order order = orderService.getByOrderNo(orderId);
            if (order == null) {
                log.error("银联支付通知 - 订单不存在：{}", orderId);
                writeResponse(response, "fail");
                return;
            }

            // 检查支付状态
            if ("00".equals(respCode)) {
                // 支付成功
                log.info("银联支付成功 - 订单号：{}", orderId);
                
                // 验证金额（银联返回的金额单位为分）
                Long notifyAmount = Long.parseLong(txnAmt);
                Long orderAmount = order.getPayPrice().multiply(new java.math.BigDecimal(100)).longValue();
                
                if (!notifyAmount.equals(orderAmount)) {
                    log.error("银联支付通知 - 金额不匹配，订单金额：{}分，通知金额：{}分", orderAmount, notifyAmount);
                    writeResponse(response, "fail");
                    return;
                }

                // 更新订单状态
                boolean updateResult = orderService.updatePaid(orderId);
                if (updateResult) {
                    log.info("银联支付成功处理完成 - 订单号：{}", orderId);
                    writeResponse(response, "ok");
                } else {
                    log.error("银联支付成功但订单状态更新失败 - 订单号：{}", orderId);
                    writeResponse(response, "fail");
                }
            } else {
                // 支付失败
                log.warn("银联支付失败 - 订单号：{}，错误码：{}", orderId, respCode);
                writeResponse(response, "ok"); // 即使支付失败也要返回ok，避免银联重复通知
            }
        } catch (Exception e) {
            log.error("银联支付通知处理异常 - 订单号：{}", orderId, e);
            writeResponse(response, "fail");
        }
    }

    /**
     * 银联支付前台回调
     */
    @ApiOperation(value = "银联支付前台回调")
    @RequestMapping(value = "/return", method = {RequestMethod.POST, RequestMethod.GET})
    public void returnNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("银联支付前台回调开始");
        
        // 获取银联前台返回参数
        Map<String, String> returnParams = getAllRequestParam(request);
        log.info("银联支付前台返回参数：{}", returnParams);

        // 验证签名
        String validateResult = unionPayService.validate(returnParams, "UTF-8");
        if (!Constants.SUCCESS.equals(validateResult)) {
            log.error("银联支付前台回调验签失败");
            redirectToErrorPage(response);
            return;
        }

        // 获取订单号和支付结果
        String respCode = returnParams.get("respCode");
        String orderId = returnParams.get("orderId");
        
        log.info("银联支付前台回调 - 订单号：{}，应答码：{}", orderId, respCode);

        try {
            if ("00".equals(respCode)) {
                // 支付成功，跳转到成功页面
                redirectToSuccessPage(response, orderId);
            } else {
                // 支付失败，跳转到失败页面
                redirectToErrorPage(response);
            }
        } catch (Exception e) {
            log.error("银联支付前台回调处理异常 - 订单号：{}", orderId, e);
            redirectToErrorPage(response);
        }
    }

    /**
     * 获取所有请求参数
     */
    private Map<String, String> getAllRequestParam(HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();
        Enumeration<String> temp = request.getParameterNames();
        while (temp.hasMoreElements()) {
            String en = temp.nextElement();
            String value = request.getParameter(en);
            res.put(en, value);
            // 在日志中打印请求参数
            if (SDKConstants.param_signature.equals(en)) {
                log.debug("参数名：{} => 参数值：{}", en, "***");
            } else {
                log.debug("参数名：{} => 参数值：{}", en, value);
            }
        }
        return res;
    }

    /**
     * 写入响应
     */
    private void writeResponse(HttpServletResponse response, String result) {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(result);
            writer.flush();
        } catch (IOException e) {
            log.error("写入银联支付响应失败", e);
        }
    }

    /**
     * 跳转到支付成功页面
     */
    private void redirectToSuccessPage(HttpServletResponse response, String orderId) {
        try {
            // 这里可以根据实际需求跳转到对应的成功页面
            String successUrl = "/pages/order/payment-success?orderId=" + orderId;
            response.sendRedirect(successUrl);
        } catch (IOException e) {
            log.error("跳转到支付成功页面失败", e);
        }
    }

    /**
     * 跳转到支付失败页面
     */
    private void redirectToErrorPage(HttpServletResponse response) {
        try {
            // 这里可以根据实际需求跳转到对应的失败页面
            String errorUrl = "/pages/order/payment-error";
            response.sendRedirect(errorUrl);
        } catch (IOException e) {
            log.error("跳转到支付失败页面失败", e);
        }
    }

    /**
     * 银联退款后台通知处理
     */
    @ApiOperation(value = "银联退款后台通知")
    @RequestMapping(value = "/refund-callback", method = RequestMethod.POST)
    public void refundNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("银联退款后台通知开始");
        
        String orderId = null;
        try {
            String encoding = request.getParameter(SDKConstants.param_encoding);
            if (encoding == null || encoding.isEmpty()) {
                encoding = "UTF-8";
            }
            
            // 获取银联返回信息
            Map<String, String> responseData = getAllRequestParam(request);
            
            log.info("银联退款通知参数: {}", responseData);
            
            // 重要！验证签名，验证成功后在商户端进行业务处理
            if (unionPayService.validate(responseData, encoding).equals(Constants.SUCCESS)) {
                String respCode = responseData.get("respCode");
                orderId = responseData.get("origOrderId"); // 原订单号
                String refundOrderId = responseData.get("orderId"); // 退款订单号
                String queryId = responseData.get("queryId"); // 系统跟踪号
                String txnAmt = responseData.get("txnAmt"); // 退款金额（分）
                
                if ("00".equals(respCode)) {
                    log.info("银联退款成功通知，原订单号: {}, 退款订单号: {}, 退款金额: {}", orderId, refundOrderId, txnAmt);
                    
                    // 检查订单是否存在
                    Order order = orderService.getByOrderNo(orderId);
                    if (order != null) {
                        // 这里可以添加退款成功后的业务逻辑
                        // 例如：更新订单状态、记录退款日志、发送退款成功通知等
                        log.info("退款成功，订单号: {}, 系统跟踪号: {}", orderId, queryId);
                        
                        // TODO: 添加具体的退款成功业务处理逻辑
                        // 比如：更新订单退款状态、记录退款记录、通知用户等
                        
                        writeResponse(response, "ok");
                    } else {
                        log.error("退款通知处理失败：订单不存在，订单号: {}", orderId);
                        writeResponse(response, "fail");
                    }
                } else {
                    log.warn("银联退款失败通知，原订单号: {}, 错误码: {}, 错误信息: {}", 
                            orderId, respCode, responseData.get("respMsg"));
                    
                    // TODO: 添加退款失败的业务处理逻辑
                    
                    writeResponse(response, "ok"); // 即使退款失败也要返回ok，避免银联重复通知
                }
            } else {
                log.error("银联退款通知验签失败");
                writeResponse(response, "fail");
            }
            
        } catch (Exception e) {
            log.error("银联退款通知处理异常 - 订单号：{}", orderId, e);
            writeResponse(response, "fail");
        }
    }

    /**
     * 银联退货后台通知处理
     */
    @ApiOperation(value = "银联退货后台通知")
    @RequestMapping(value = "/return/all/callback", method = RequestMethod.POST)
    public void returnALlNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("银联退货后台通知开始");

        String origQryId = null;
        try {
            String encoding = request.getParameter(SDKConstants.param_encoding);
            if (encoding == null || encoding.isEmpty()) {
                encoding = "UTF-8";
            }

            // 获取银联返回信息
            Map<String, String> responseData = getAllRequestParam(request);

            log.info("银联退货通知参数: {}", responseData);

            // 重要！验证签名，验证成功后在商户端进行业务处理
            if (unionPayService.validate(responseData, encoding).equals(Constants.SUCCESS)) {
                String respCode = responseData.get("respCode");
                origQryId = responseData.get("origQryId");        // 原交易查询流水号
                String returnOrderId = responseData.get("orderId"); // 退货订单号
                String queryId = responseData.get("queryId");     // 系统跟踪号
                String txnAmt = responseData.get("txnAmt");       // 退货金额（分）

                if ("00".equals(respCode)) {
                    log.info("银联退货成功通知，原交易查询流水号: {}, 退货订单号: {}, 退货金额: {}",
                            origQryId, returnOrderId, txnAmt);

                    // 这里可以添加退货成功后的业务逻辑
                    // 例如：更新订单状态、记录退货日志、发送退货成功通知等
                    log.info("退货成功，原交易查询流水号: {}, 系统跟踪号: {}", origQryId, queryId);

                    // TODO: 添加具体的退货成功业务处理逻辑
                    // 比如：更新订单退货状态、记录退货记录、通知用户等

                    writeResponse(response, "ok");
                } else {
                    log.warn("银联退货失败通知，原交易查询流水号: {}, 错误码: {}, 错误信息: {}",
                            origQryId, respCode, responseData.get("respMsg"));

                    // TODO: 添加退货失败的业务处理逻辑

                    writeResponse(response, "ok"); // 即使退货失败也要返回ok，避免银联重复通知
                }
            } else {
                log.error("银联退货通知验签失败");
                writeResponse(response, "fail");
            }

        } catch (Exception e) {
            log.error("银联退货通知处理异常 - 原交易查询流水号：{}", origQryId, e);
            writeResponse(response, "fail");
        }
    }
}
