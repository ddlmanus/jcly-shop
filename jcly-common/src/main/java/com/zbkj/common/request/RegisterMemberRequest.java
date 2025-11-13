package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 会员注册请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="RegisterMemberRequest对象", description="会员注册请求对象")
public class RegisterMemberRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @ApiModelProperty(value = "验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    private String code;
}