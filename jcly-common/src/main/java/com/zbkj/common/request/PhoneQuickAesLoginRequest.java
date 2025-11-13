package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="PhoneQuickLoginRequest对象", description="手机号快速注册登录请求对象")
public class PhoneQuickAesLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private String phone;
}