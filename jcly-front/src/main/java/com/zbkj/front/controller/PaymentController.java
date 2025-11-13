package com.zbkj.front.controller;

import com.zbkj.common.model.user.UserBankCard;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UserService;
import com.zbkj.common.vo.BankCardVerifyRequestVo;
import com.zbkj.common.vo.BankCardVerifyResponseVo;
import com.zbkj.common.vo.UnifiedPaymentRequestVo;
import com.zbkj.service.service.UnifiedPaymentService;
import com.zbkj.service.service.UserBankCardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统一支付控制器
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
@Slf4j
@RestController
@RequestMapping("api/front/payment")
@Api(tags = "统一支付控制器")
public class PaymentController {

    @Autowired
    private UnifiedPaymentService unifiedPaymentService;

    @Autowired
    private UserBankCardService userBankCardService;

    @Autowired
    private UserService userService;

    /**
     * 统一支付接口
     */
    @ApiOperation(value = "统一支付")
    @PostMapping("/unified-pay")
    public CommonResult<Map<String, Object>> unifiedPay(@RequestBody @Validated UnifiedPaymentRequestVo request) {
        try {
            Integer uid = userService.getUserIdException();
            log.info("统一支付请求，用户ID: {}, 订单号: {}, 支付方式: {}", 
                    uid, request.getOrderNo(), request.getPaymentMethod());

            Map<String, Object> result = unifiedPaymentService.unifiedPay(uid, request);
            
            if ((Boolean) result.get("success")) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed((String) result.get("message"));
            }

        } catch (Exception e) {
            log.error("统一支付异常", e);
            return CommonResult.failed("支付请求失败：" + e.getMessage());
        }
    }

    /**
     * 查询支付状态
     */
    @ApiOperation(value = "查询支付状态")
    @GetMapping("/query-status")
    public CommonResult<Map<String, Object>> queryPaymentStatus(
            @ApiParam(value = "订单号", required = true) @RequestParam String orderNo) {
        try {
            log.info("查询支付状态，订单号: {}", orderNo);

            Map<String, Object> result = unifiedPaymentService.queryPaymentStatus(orderNo);
            
            if ((Boolean) result.get("success")) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed((String) result.get("message"));
            }

        } catch (Exception e) {
            log.error("查询支付状态异常", e);
            return CommonResult.failed("查询支付状态失败：" + e.getMessage());
        }
    }

    /**
     * 统一退款接口
     */
    @ApiOperation(value = "统一退款")
    @PostMapping("/unified-refund")
    public CommonResult<Map<String, Object>> unifiedRefund(
            @ApiParam(value = "订单号", required = true) @RequestParam String orderNo,
            @ApiParam(value = "退款金额", required = true) @RequestParam BigDecimal refundAmount,
            @ApiParam(value = "退款原因") @RequestParam(required = false) String reason) {
        try {
            log.info("统一退款请求，订单号: {}, 退款金额: {}, 退款原因: {}", orderNo, refundAmount, reason);

            Map<String, Object> result = unifiedPaymentService.unifiedRefund(orderNo, refundAmount, reason);
            
            if ((Boolean) result.get("success")) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed((String) result.get("message"));
            }

        } catch (Exception e) {
            log.error("统一退款异常", e);
            return CommonResult.failed("退款请求失败：" + e.getMessage());
        }
    }

    /**
     * 统一退货接口
     */
    @ApiOperation(value = "统一退货")
    @PostMapping("/unified-return")
    public CommonResult<Map<String, Object>> unifiedReturn(
            @ApiParam(value = "订单号", required = true) @RequestParam String orderNo,
            @ApiParam(value = "退货金额", required = true) @RequestParam BigDecimal returnAmount,
            @ApiParam(value = "退货原因") @RequestParam(required = false) String reason) {
        try {
            log.info("统一退货请求，订单号: {}, 退货金额: {}, 退货原因: {}", orderNo, returnAmount, reason);

            Map<String, Object> result = unifiedPaymentService.unifiedReturn(orderNo, returnAmount, reason);
            
            if ((Boolean) result.get("success")) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed((String) result.get("message"));
            }

        } catch (Exception e) {
            log.error("统一退货异常", e);
            return CommonResult.failed("退货请求失败：" + e.getMessage());
        }
    }

    // ==================== 银行卡管理相关接口 ====================

    /**
     * 验证并保存银行卡
     */
    @ApiOperation(value = "验证并保存银行卡")
    @PostMapping("/verify-and-save-bankcard")
    public CommonResult<BankCardVerifyResponseVo> verifyAndSaveBankCard(@RequestBody @Validated BankCardVerifyRequestVo request) {
        try {
            Integer uid = userService.getUserIdException();
            log.info("验证并保存银行卡，用户ID: {}, 验证类型: {}", uid, request.getVerifyType());

            BankCardVerifyResponseVo result = userBankCardService.verifyAndSaveBankCard(uid, request);
            
            if (result.getVerifyResult()) {
                return CommonResult.success(result);
            } else {
                return CommonResult.success(result); // 验证失败也返回success，让前端根据verifyResult判断
            }

        } catch (Exception e) {
            log.error("验证并保存银行卡异常", e);
            return CommonResult.failed("银行卡验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户银行卡列表
     */
    @ApiOperation(value = "获取用户银行卡列表")
    @GetMapping("/bank-cards")
    public CommonResult<List<UserBankCard>> getUserBankCards() {
        try {
            Integer uid = userService.getUserIdException();
            log.info("获取用户银行卡列表，用户ID: {}", uid);

            List<UserBankCard> bankCards = userBankCardService.getUserBankCards(uid);
            return CommonResult.success(bankCards);

        } catch (Exception e) {
            log.error("获取用户银行卡列表异常", e);
            return CommonResult.failed("获取银行卡列表失败：" + e.getMessage());
        }
    }

    /**
     * 设置默认银行卡
     */
    @ApiOperation(value = "设置默认银行卡")
    @PostMapping("/set-default-bankcard")
    public CommonResult<Boolean> setDefaultBankCard(
            @ApiParam(value = "银行卡ID", required = true) @RequestParam Integer cardId) {
        try {
            Integer uid = userService.getUserIdException();
            log.info("设置默认银行卡，用户ID: {}, 银行卡ID: {}", uid, cardId);

            Boolean result = userBankCardService.setDefaultBankCard(uid, cardId);
            
            if (result) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("设置默认银行卡失败");
            }

        } catch (Exception e) {
            log.error("设置默认银行卡异常", e);
            return CommonResult.failed("设置默认银行卡失败：" + e.getMessage());
        }
    }

    /**
     * 删除银行卡
     */
    @ApiOperation(value = "删除银行卡")
    @PostMapping("/delete-bankcard")
    public CommonResult<Boolean> deleteBankCard(
            @ApiParam(value = "银行卡ID", required = true) @RequestParam Integer cardId) {
        try {
            Integer uid = userService.getUserIdException();
            log.info("删除银行卡，用户ID: {}, 银行卡ID: {}", uid, cardId);

            Boolean result = userBankCardService.deleteBankCard(uid, cardId);
            
            if (result) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed("删除银行卡失败");
            }

        } catch (Exception e) {
            log.error("删除银行卡异常", e);
            return CommonResult.failed("删除银行卡失败：" + e.getMessage());
        }
    }
}
