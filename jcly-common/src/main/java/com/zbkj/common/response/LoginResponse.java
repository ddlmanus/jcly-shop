package com.zbkj.common.response;

import com.zbkj.common.model.coupon.Coupon;
import com.zbkj.common.response.employee.FrontMerchantEmployeeResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Login Response
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="LoginResponse", description="用户登录返回数据")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "用户登录密钥")
    private String token;

    @ApiModelProperty(value = "状态:login-登录，register-注册,start-注册起始页")
    private String type;

    @ApiModelProperty(value = "注册key")
    private String key;

    @ApiModelProperty(value = "登录用户Uid")
    private Integer id;

    @ApiModelProperty(value = "登录用户昵称")
    private String nickname;

    @ApiModelProperty(value = "登录用户手机号")
    private String phone;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "是否新人")
    private Boolean isNew = false;

    @ApiModelProperty(value = "新人礼优惠券列表")
    private List<Coupon> newPeopleCouponList;

    @ApiModelProperty(value = "PC公众号用户是否同意登录")
    public Boolean wechatPublicUserAgreeStatus = Boolean.FALSE;

}
