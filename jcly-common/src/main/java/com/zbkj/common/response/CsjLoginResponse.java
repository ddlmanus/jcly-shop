package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "采食家平台登录响应对象", description = "采食家平台登录响应对象")
public class CsjLoginResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "令牌")
    private String token;

    @ApiModelProperty(value = "用户账号")
    private String account;

    @ApiModelProperty(value = "用户名称")
    private String realName;
}