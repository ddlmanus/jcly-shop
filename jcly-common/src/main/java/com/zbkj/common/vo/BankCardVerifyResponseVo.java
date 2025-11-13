package com.zbkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡验证响应VO
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
@ApiModel(value = "BankCardVerifyResponseVo", description = "银行卡验证响应")
public class BankCardVerifyResponseVo {

    @ApiModelProperty(value = "应答码：00-信息一致，01-信息不一致，99-验证失败")
    private String responseCode;

    @ApiModelProperty(value = "应答消息")
    private String message;

    @ApiModelProperty(value = "验证结果：true-验证通过，false-验证失败")
    private Boolean verifyResult;

    @ApiModelProperty(value = "银行名称（如果验证通过）")
    private String bankName;

    @ApiModelProperty(value = "卡类型（如果验证通过）：1-借记卡，2-信用卡")
    private String cardType;

    @ApiModelProperty(value = "详细信息")
    private String detail;

    /**
     * 根据响应码判断验证是否成功
     */
    public void setVerifyResultByCode() {
        if ("00".equals(this.responseCode)) {
            this.verifyResult = true;
            if (this.message == null || this.message.isEmpty()) {
                this.message = "验证通过";
            }
        } else {
            this.verifyResult = false;
            if (this.message == null || this.message.isEmpty()) {
                if ("01".equals(this.responseCode)) {
                    this.message = "信息不一致";
                } else if ("99".equals(this.responseCode)) {
                    this.message = "验证失败";
                } else {
                    this.message = "未知错误";
                }
            }
        }
    }
}
