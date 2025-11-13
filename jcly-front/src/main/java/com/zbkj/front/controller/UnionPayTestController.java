package com.zbkj.front.controller;

import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.user.UserBankCard;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.BankCardVerifyRequestVo;
import com.zbkj.common.vo.BankCardVerifyResponseVo;
import com.zbkj.service.service.UnionPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/front/unionpay/test")
@Api(tags = "银联支付测试控制器")
public class UnionPayTestController {

    @Autowired
    private UnionPayService unionPayService;

    /**
     * 测试1：消费交易（必测）- 借记卡
     */
    @ApiOperation(value = "测试消费交易-借记卡")
    @GetMapping(value = "/consume/debit")
    public void testConsumeDebit(HttpServletResponse response,
                                @ApiParam(value = "支付金额", required = false) @RequestParam(value = "amount", required = false) BigDecimal amount) throws Exception {
        response.setHeader("content-type", "text/html;charset=UTF-8");
        
        try {
            // 创建测试订单 - 借记卡消费
            Order order = createTestOrder("DEBIT", amount != null ? amount : new BigDecimal("1.00"));
            order.setPayType("unionpay"); // PC支付
            
            log.info("开始测试借记卡消费交易，订单号：{}, 金额：{}", order.getOrderNo(), order.getPayPrice());
            
            String payForm = unionPayService.unionPay(order);
            response.getWriter().write(payForm);
            response.getWriter().flush();
        } catch (Exception e) {
            log.error("借记卡消费测试失败", e);
            response.getWriter().write("<h3>借记卡消费测试失败：" + e.getMessage() + "</h3>");
        } finally {
            response.getWriter().close();
        }
    }

    /**
     * 测试1：消费交易（必测）- 贷记卡
     */
    @ApiOperation(value = "测试消费交易-贷记卡")
    @GetMapping(value = "/consume/credit")
    public void testConsumeCredit(HttpServletResponse response,
                                 @ApiParam(value = "支付金额", required = false) @RequestParam(value = "amount", required = false) BigDecimal amount) throws Exception {
        response.setHeader("content-type", "text/html;charset=UTF-8");
        
        try {
            // 创建测试订单 - 贷记卡消费
            Order order = createTestOrder("CREDIT", amount != null ? amount : new BigDecimal("1.00"));
            order.setPayType("unionpay"); // PC支付
            
            log.info("开始测试贷记卡消费交易，订单号：{}, 金额：{}", order.getOrderNo(), order.getPayPrice());
            
            String payForm = unionPayService.unionPay(order);
            response.getWriter().write(payForm);
            response.getWriter().flush();
        } catch (Exception e) {
            log.error("贷记卡消费测试失败", e);
            response.getWriter().write("<h3>贷记卡消费测试失败：" + e.getMessage() + "</h3>");
        } finally {
            response.getWriter().close();
        }
    }

    /**
     * 测试1：消费交易（必测）- 手机WAP支付
     */
    @ApiOperation(value = "测试手机WAP消费交易")
    @GetMapping(value = "/consume/mobile")
    public void testConsumeMobile(HttpServletResponse response,
                                 @ApiParam(value = "支付金额", required = false) @RequestParam(value = "amount", required = false) BigDecimal amount) throws Exception {
        response.setHeader("content-type", "text/html;charset=UTF-8");
        
        try {
            // 创建测试订单 - 手机支付
            Order order = createTestOrder("MOBILE", amount != null ? amount : new BigDecimal("1.00"));
            order.setPayType("unionpay_mobile"); // 手机支付
            
            log.info("开始测试手机WAP消费交易，订单号：{}, 金额：{}", order.getOrderNo(), order.getPayPrice());
            
            String payForm = unionPayService.unionPay(order);
            response.getWriter().write(payForm);
            response.getWriter().flush();
        } catch (Exception e) {
            log.error("手机WAP消费测试失败", e);
            response.getWriter().write("<h3>手机WAP消费测试失败：" + e.getMessage() + "</h3>");
        } finally {
            response.getWriter().close();
        }
    }

    /**
     * 测试1：消费交易（必测）- 无跳转支付（认证支付2.0）
     */
    @ApiOperation(value = "测试无跳转消费交易")
    @PostMapping(value = "/consume/no-redirect")
    public void testConsumeNoRedirect(HttpServletResponse response,
                                     @ApiParam(value = "支付金额", required = false) @RequestParam(value = "amount", required = false) BigDecimal amount,
                                     @ApiParam(value = "银行卡号", required = false) @RequestParam(value = "cardNo", required = false) String cardNo,
                                     @ApiParam(value = "短信验证码", required = false) @RequestParam(value = "smsCode", required = false) String smsCode) throws Exception {
        response.setHeader("content-type", "text/html;charset=UTF-8");
        
        try {
            // 创建测试订单
            Order order = createTestOrder("NO_REDIRECT", amount != null ? amount : new BigDecimal("1.00"));
            
            // 创建测试银行卡信息
            UserBankCard bankCard = createTestBankCard(cardNo);
            String testSmsCode = smsCode != null ? smsCode : "111111";
            
            log.info("开始测试无跳转消费交易，订单号：{}, 金额：{}, 卡号：{}", 
                    order.getOrderNo(), order.getPayPrice(), maskCardNo(bankCard.getCardNo()));
            
            String payForm = unionPayService.unionPay(order, bankCard, testSmsCode);
            response.getWriter().write(payForm);
            response.getWriter().flush();
        } catch (Exception e) {
            log.error("无跳转消费测试失败", e);
            response.getWriter().write("<h3>无跳转消费测试失败：" + e.getMessage() + "</h3>");
        } finally {
            response.getWriter().close();
        }
    }

    /**
     * 测试2：交易状态查询（选测）
     */
    @ApiOperation(value = "测试交易状态查询")
    @GetMapping(value = "/query")
    public CommonResult<Map<String, String>> testQueryOrder(
            @ApiParam(value = "商户订单号", required = true) @RequestParam String orderId,
            @ApiParam(value = "原交易时间(格式：YYYYMMDDhhmmss)", required = false) @RequestParam(required = false) String txnTime) {
        
        try {
            // 如果没有提供交易时间，使用当前时间的格式
            if (txnTime == null || txnTime.isEmpty()) {
                txnTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
            
            log.info("开始测试交易状态查询，订单号：{}, 交易时间：{}", orderId, txnTime);
            
            Map<String, String> result = unionPayService.queryOrder(orderId, txnTime);
            
            if (result != null) {
                log.info("交易查询成功，订单号：{}, 应答码：{}", orderId, result.get("respCode"));
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("查询失败，可能是订单不存在或网络异常");
            }
        } catch (Exception e) {
            log.error("交易查询测试异常", e);
            return CommonResult.failed("查询异常：" + e.getMessage());
        }
    }

    /**
     * 测试3：退货交易（选测）
     */
    @ApiOperation(value = "测试退货交易")
    @PostMapping(value = "/return")
    public CommonResult<Map<String, String>> testReturnOrder(
            @ApiParam(value = "原交易查询流水号", required = true) @RequestParam String origQryId,
            @ApiParam(value = "退货金额", required = true) @RequestParam BigDecimal returnAmount) {
        
        try {
            log.info("开始测试退货交易，原交易查询流水号：{}, 退货金额：{}", origQryId, returnAmount);
            
            // 参数校验
            if (returnAmount == null || returnAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return CommonResult.failed("退货金额必须大于0");
            }
            
            Map<String, String> result = unionPayService.returnOrder(origQryId, returnAmount);
            
            if (result != null) {
                String returnStatus = result.get("returnStatus");
                log.info("退货测试完成，状态：{}, 消息：{}", returnStatus, result.get("returnMessage"));
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("退货请求失败");
            }
        } catch (Exception e) {
            log.error("退货测试异常", e);
            return CommonResult.failed("退货异常：" + e.getMessage());
        }
    }

    /**
     * 银行卡验证测试 - 2要素验证
     */
    @ApiOperation(value = "测试银行卡2要素验证")
    @PostMapping(value = "/verify-bankcard/2-factor")
    public CommonResult<BankCardVerifyResponseVo> testVerifyBankCard2Factor(
            @ApiParam(value = "银行卡号", required = false) @RequestParam(required = false) String cardNo,
            @ApiParam(value = "持卡人姓名", required = false) @RequestParam(required = false) String cardholderName) {
        
        try {
            BankCardVerifyRequestVo request = new BankCardVerifyRequestVo();
            request.setCardNo(cardNo != null ? cardNo : "6216261000000000018"); // 默认使用测试借记卡
            request.setCardholderName(cardholderName != null ? cardholderName : "全渠道"); // 默认测试姓名
            request.setIdCard("341126197709218366"); // 测试身份证号
            request.setVerifyType("2"); // 2要素验证
            
            log.info("开始测试银行卡2要素验证，卡号：{}", maskCardNo(request.getCardNo()));
            
            BankCardVerifyResponseVo result = unionPayService.verifyBankCard(request);
            
            if (result != null) {
                log.info("银行卡2要素验证完成，结果：{}, 响应码：{}", 
                        result.getVerifyResult(), result.getResponseCode());
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("银行卡验证请求失败");
            }
        } catch (Exception e) {
            log.error("银行卡2要素验证测试异常", e);
            return CommonResult.failed("验证异常：" + e.getMessage());
        }
    }

    /**
     * 银行卡验证测试 - 4要素验证
     */
    @ApiOperation(value = "测试银行卡4要素验证")
    @PostMapping(value = "/verify-bankcard/4-factor")
    public CommonResult<BankCardVerifyResponseVo> testVerifyBankCard4Factor(
            @ApiParam(value = "银行卡号", required = false) @RequestParam(required = false) String cardNo,
            @ApiParam(value = "持卡人姓名", required = false) @RequestParam(required = false) String cardholderName,
            @ApiParam(value = "手机号", required = false) @RequestParam(required = false) String mobile) {
        
        try {
            BankCardVerifyRequestVo request = new BankCardVerifyRequestVo();
            request.setCardNo(cardNo != null ? cardNo : "6216261000000000018"); // 默认使用测试借记卡
            request.setCardholderName(cardholderName != null ? cardholderName : "全渠道");
            request.setIdCard("341126197709218366");
            request.setMobile(mobile != null ? mobile : "13552535506"); // 测试手机号
            request.setVerifyType("4"); // 4要素验证
            
            log.info("开始测试银行卡4要素验证，卡号：{}", maskCardNo(request.getCardNo()));
            
            BankCardVerifyResponseVo result = unionPayService.verifyBankCard(request);
            
            if (result != null) {
                log.info("银行卡4要素验证完成，结果：{}, 响应码：{}", 
                        result.getVerifyResult(), result.getResponseCode());
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("银行卡验证请求失败");
            }
        } catch (Exception e) {
            log.error("银行卡4要素验证测试异常", e);
            return CommonResult.failed("验证异常：" + e.getMessage());
        }
    }

    /**
     * 银行卡验证测试 - 6要素验证
     */
    @ApiOperation(value = "测试银行卡6要素验证")
    @PostMapping(value = "/verify-bankcard/6-factor")
    public CommonResult<BankCardVerifyResponseVo> testVerifyBankCard6Factor(
            @ApiParam(value = "银行卡号", required = false) @RequestParam(required = false) String cardNo,
            @ApiParam(value = "持卡人姓名", required = false) @RequestParam(required = false) String cardholderName,
            @ApiParam(value = "手机号", required = false) @RequestParam(required = false) String mobile,
            @ApiParam(value = "CVN2码", required = false) @RequestParam(required = false) String cvn2,
            @ApiParam(value = "有效期(YYMM)", required = false) @RequestParam(required = false) String expired) {
        
        try {
            BankCardVerifyRequestVo request = new BankCardVerifyRequestVo();
            request.setCardNo(cardNo != null ? cardNo : "6221558812340000"); // 默认使用测试贷记卡
            request.setCardholderName(cardholderName != null ? cardholderName : "互联网");
            request.setIdCard("341126197709218366");
            request.setMobile(mobile != null ? mobile : "13552535506");
            request.setCvn2(cvn2 != null ? cvn2 : "123"); // 测试CVN2
            request.setExpired(expired != null ? expired : "2311"); // 测试有效期
            request.setVerifyType("6"); // 6要素验证
            
            log.info("开始测试银行卡6要素验证，卡号：{}", maskCardNo(request.getCardNo()));
            
            BankCardVerifyResponseVo result = unionPayService.verifyBankCard(request);
            
            if (result != null) {
                log.info("银行卡6要素验证完成，结果：{}, 响应码：{}", 
                        result.getVerifyResult(), result.getResponseCode());
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("银行卡验证请求失败");
            }
        } catch (Exception e) {
            log.error("银行卡6要素验证测试异常", e);
            return CommonResult.failed("验证异常：" + e.getMessage());
        }
    }

    /**
     * 获取测试数据信息
     */
    @ApiOperation(value = "获取银联测试数据")
    @GetMapping(value = "/data")
    public CommonResult<Map<String, Object>> getTestData() {
        Map<String, Object> testData = new HashMap<>();
        
        // 测试商户号
        testData.put("merchantId", "777290058110048");
        
        // 测试银行卡信息
        Map<String, Object> testCards = new HashMap<>();
        
        Map<String, Object> debitCard = new HashMap<>();
        debitCard.put("cardNo", "6216261000000000018");
        debitCard.put("cardType", "借记卡");
        debitCard.put("bankName", "平安银行");
        debitCard.put("mobile", "13552535506");
        debitCard.put("password", "123456");
        debitCard.put("idCard", "341126197709218366");
        debitCard.put("cardholderName", "全渠道");
        testCards.put("debitCard", debitCard);
        
        Map<String, Object> creditCard = new HashMap<>();
        creditCard.put("cardNo", "6221558812340000");
        creditCard.put("cardType", "贷记卡");
        creditCard.put("bankName", "平安银行");
        creditCard.put("mobile", "13552535506");
        creditCard.put("password", "123456");
        creditCard.put("cvn2", "123");
        creditCard.put("expired", "2311");
        creditCard.put("idCard", "341126197709218366");
        creditCard.put("cardholderName", "互联网");
        testCards.put("creditCard", creditCard);
        
        testData.put("testCards", testCards);
        
        // 短信验证码
        Map<String, String> smsCodes = new HashMap<>();
        smsCodes.put("gateway_wap", "111111");
        smsCodes.put("widget", "123456");
        testData.put("smsCodes", smsCodes);
        
        // 测试地址
        Map<String, String> testUrls = new HashMap<>();
        testUrls.put("frontTransReq", "https://gateway.test.95516.com/gateway/api/frontTransReq.do");
        testUrls.put("appTransReq", "https://gateway.test.95516.com/gateway/api/appTransReq.do");
        testUrls.put("backTransReq", "https://gateway.test.95516.com/gateway/api/backTransReq.do");
        testUrls.put("cardTransReq", "https://gateway.test.95516.com/gateway/api/cardTransReq.do");
        testUrls.put("queryTrans", "https://gateway.test.95516.com/gateway/api/queryTrans.do");
        testData.put("testUrls", testUrls);
        
        return CommonResult.success(testData);
    }

    /**
     * 创建测试订单
     */
    private Order createTestOrder(String testType, BigDecimal amount) {
        Order order = new Order();
        // 银联要求：订单号8-40位数字字母，不能含"-"或"_"
        // 使用时间戳 + 类型代码的格式
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String typeCode = getTestTypeCode(testType);
        order.setOrderNo(timestamp + typeCode);
        order.setPayPrice(amount);
        order.setPayType("unionpay");
        return order;
    }
    
    /**
     * 获取测试类型代码
     */
    private String getTestTypeCode(String testType) {
        switch (testType) {
            case "DEBIT":
                return "01";
            case "CREDIT":
                return "02";
            case "MOBILE":
                return "03";
            case "NO_REDIRECT":
                return "04";
            default:
                return "00";
        }
    }

    /**
     * 创建测试银行卡信息
     */
    private UserBankCard createTestBankCard(String cardNo) {
        UserBankCard bankCard = new UserBankCard();
        
        if (cardNo == null || cardNo.isEmpty()) {
            // 默认使用借记卡
            bankCard.setCardNo("6216261000000000018");
            bankCard.setCardholderName("全渠道");
            bankCard.setMobile("13552535506");
        } else if (cardNo.startsWith("6216261")) {
            // 借记卡
            bankCard.setCardNo(cardNo);
            bankCard.setCardholderName("全渠道");
            bankCard.setMobile("13552535506");
        } else if (cardNo.startsWith("6221558")) {
            // 贷记卡
            bankCard.setCardNo(cardNo);
            bankCard.setCardholderName("互联网");
            bankCard.setMobile("13552535506");
            bankCard.setCvn2("123");
            bankCard.setExpired("2311");
            bankCard.setVerifyType("6");
        } else {
            // 其他卡号，使用默认借记卡信息
            bankCard.setCardNo(cardNo);
            bankCard.setCardholderName("全渠道");
            bankCard.setMobile("13552535506");
        }
        
        bankCard.setIdCard("341126197709218366");
        return bankCard;
    }

    /**
     * 脱敏银行卡号
     */
    private String maskCardNo(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) {
            return cardNo;
        }
        return cardNo.substring(0, 4) + "****" + cardNo.substring(cardNo.length() - 4);
    }
}
