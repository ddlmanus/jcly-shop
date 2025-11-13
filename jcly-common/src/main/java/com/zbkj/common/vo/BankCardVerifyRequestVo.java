package com.zbkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 银行卡验证请求VO
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
@ApiModel(value = "BankCardVerifyRequestVo", description = "银行卡验证请求")
public class BankCardVerifyRequestVo {

    @ApiModelProperty(value = "银行卡号", required = true)
    @NotBlank(message = "银行卡号不能为空")
    @Pattern(regexp = "^\\d{15,19}$", message = "银行卡号格式不正确")
    private String cardNo;

    @ApiModelProperty(value = "持卡人姓名", required = true)
    @NotBlank(message = "持卡人姓名不能为空")
    private String cardholderName;

    @ApiModelProperty(value = "身份证号", required = true)
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", 
             message = "身份证号格式不正确")
    private String idCard;

    @ApiModelProperty(value = "手机号（4要素、6要素验证时必填）")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @ApiModelProperty(value = "CVN2码（6要素验证时必填）")
    @Pattern(regexp = "^\\d{3}$", message = "CVN2码必须为3位数字")
    private String cvn2;

    @ApiModelProperty(value = "有效期（6要素验证时必填，格式：MMYY）")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\d{2}$", message = "有效期格式不正确，应为MMYY格式")
    private String expired;

    @ApiModelProperty(value = "验证类型：3-三要素验证，4-四要素验证，6-六要素验证", required = true)
    @NotBlank(message = "验证类型不能为空")
    @Pattern(regexp = "^[346]$", message = "验证类型只能为3、4或6")
    private String verifyType;
}
