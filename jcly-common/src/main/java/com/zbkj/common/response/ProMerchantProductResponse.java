package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品详情商户商品响应对象
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
@ApiModel(value = "ProMerchantProductResponse对象", description = "商品详情商户商品响应对象")
public class ProMerchantProductResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id")
    private Integer id;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "虚拟销量")
    private Integer ficti;

    @ApiModelProperty(value = "库存")
    private Integer stock;
}
