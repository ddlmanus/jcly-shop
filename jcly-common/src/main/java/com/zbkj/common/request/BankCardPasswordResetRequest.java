package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 银行卡支付密码重置请求
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "BankCardPasswordResetRequest对象", description = "银行卡支付密码重置请求")
public class BankCardPasswordResetRequest {

    @ApiModelProperty(value = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @ApiModelProperty(value = "短信验证码", required = true, example = "123456")
    @NotBlank(message = "短信验证码不能为空")
    @Size(min = 4, max = 6, message = "短信验证码长度为4-6位")
    private String smsCode;

    @ApiModelProperty(value = "新支付密码", required = true, example = "123456")
    @NotBlank(message = "新支付密码不能为空")
    @Size(min = 6, max = 20, message = "新支付密码长度为6-20位")
    private String newPassword;

    @ApiModelProperty(value = "确认新支付密码", required = true, example = "123456")
    @NotBlank(message = "确认新支付密码不能为空")
    @Size(min = 6, max = 20, message = "确认新支付密码长度为6-20位")
    private String confirmPassword;

    @ApiModelProperty(value = "银行卡号后四位", required = true, example = "1234")
    @NotBlank(message = "银行卡号后四位不能为空")
    @Size(min = 4, max = 4, message = "银行卡号后四位长度为4位")
    private String cardNoSuffix;

    @ApiModelProperty(value = "身份证号后四位", required = true, example = "123X")
    @NotBlank(message = "身份证号后四位不能为空")
    @Size(min = 4, max = 4, message = "身份证号后四位长度为4位")
    private String idCardSuffix;
} 