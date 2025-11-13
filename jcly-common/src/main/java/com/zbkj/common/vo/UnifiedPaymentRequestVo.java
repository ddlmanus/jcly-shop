package com.zbkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 统一支付请求VO
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
@Data
@ApiModel(value = "UnifiedPaymentRequestVo", description = "统一支付请求")
public class UnifiedPaymentRequestVo {

    @ApiModelProperty(value = "订单号", required = true)
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @ApiModelProperty(value = "支付方式：wechat-微信，alipay-支付宝，unionpay-银联", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @ApiModelProperty(value = "支付金额", required = true)
    @NotNull(message = "支付金额不能为空")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "银行卡ID（银联支付时必填）")
    private Integer bankCardId;

    @ApiModelProperty(value = "银行卡验证信息（银联支付且银行卡不存在时必填）")
    private BankCardVerifyRequestVo bankCardInfo;

    @ApiModelProperty(value = "支付场景：miniprogram-小程序，h5-H5，app-APP")
    private String payScene = "miniprogram";

    @ApiModelProperty(value = "用户openId（微信支付时需要）")
    private String openId;

    @ApiModelProperty(value = "备注")
    private String remark;
}
