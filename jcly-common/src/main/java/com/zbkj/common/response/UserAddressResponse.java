package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户地址响应对象
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
@ApiModel(value = "UserAddressResponse对象", description = "用户地址响应对象")
public class UserAddressResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "地址ID")
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "收货人姓名")
    private String realName;

    @ApiModelProperty(value = "收货人电话")
    private String phone;

    @ApiModelProperty(value = "省份ID")
    private Integer provinceId;

    @ApiModelProperty(value = "省份名称")
    private String province;

    @ApiModelProperty(value = "城市ID")
    private Integer cityId;

    @ApiModelProperty(value = "城市名称")
    private String city;

    @ApiModelProperty(value = "区/县ID")
    private Integer districtId;

    @ApiModelProperty(value = "区/县名称")
    private String district;

    @ApiModelProperty(value = "详细地址")
    private String detail;

    @ApiModelProperty(value = "邮编")
    private String postCode;

    @ApiModelProperty(value = "经度")
    private String longitude;

    @ApiModelProperty(value = "纬度")
    private String latitude;

    @ApiModelProperty(value = "是否默认地址")
    private Boolean isDefault;
}
