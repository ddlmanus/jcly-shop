package com.zbkj.common.request;

import com.anji.captcha.model.vo.CaptchaVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "采食家平台登录请求对象", description = "采食家平台登录请求对象")
public class CsjLoginRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "账户", required = true)
    @NotEmpty(message = "账户不能为空")
    private String account;

    @ApiModelProperty(value = "密码", required = true) 
    @NotEmpty(message = "密码不能为空")
    private String pwd;

    @ApiModelProperty(value = "验证码对象")
    private CaptchaVO captchaVO;
}