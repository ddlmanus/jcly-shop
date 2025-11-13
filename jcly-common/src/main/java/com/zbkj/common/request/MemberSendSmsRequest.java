package com.zbkj.common.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class MemberSendSmsRequest {
    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;


    @ApiModelProperty(value = "商户ID", required = true)
    private Integer merId;
}
