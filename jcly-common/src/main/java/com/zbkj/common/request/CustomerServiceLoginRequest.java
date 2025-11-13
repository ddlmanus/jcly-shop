package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 客服登录请求
 * @author AI Assistant
 * @since 2025-10-27
 */
@Data
@ApiModel(value = "CustomerServiceLoginRequest对象", description = "客服登录请求")
public class CustomerServiceLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "登录账号", required = true)
    @NotBlank(message = "登录账号不能为空")
    private String account;

    @ApiModelProperty(value = "登录密码", required = true)
    @NotBlank(message = "登录密码不能为空")
    private String password;

    @ApiModelProperty(value = "验证码")
    private String captcha;

    @ApiModelProperty(value = "验证码key")
    private String captchaKey;
}
