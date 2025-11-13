package com.zbkj.common.request.merchant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 商户地址保存请求对象
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
@ApiModel(value = "MerchantAddressSaveRequest对象", description = "商户地址保存请求对象")
public class MerchantAddressSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户地址id,新增时不传，编辑时必传")
    private Integer id;

    @ApiModelProperty(value = "收货人姓名")
    private String receiverName;

    @ApiModelProperty(value = "收货人电话")
    private String receiverPhone;

    @ApiModelProperty(value = "收货人所在省")
    private String province;

    @ApiModelProperty(value = "省份ID")
    private Integer provinceId;

    @ApiModelProperty(value = "收货人所在市")
    private String city;

    @ApiModelProperty(value = "城市id")
    private Integer cityId;

    @ApiModelProperty(value = "收货人所在区/县")
    private String district;

    @ApiModelProperty(value = "区/县id")
    private Integer districtId;

    @ApiModelProperty(value = "收货人所在街道")
    private String street;

    @ApiModelProperty(value = "收货人详细地址")
    private String detail;

    @ApiModelProperty(value = "经度")
    private String longitude;

    @ApiModelProperty(value = "纬度")
    private String latitude;

    @ApiModelProperty(value = "是否默认")
    private Boolean isDefault;

    @ApiModelProperty(value = "是否开启")
    private Boolean isShow;


}
