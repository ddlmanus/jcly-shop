package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单商品响应对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderProductResponse对象", description="订单商品响应对象")
public class OrderProductResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单商品ID")
    private Integer id;

    @ApiModelProperty(value = "订单ID")
    private Integer orderId;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品规格")
    private String sku;

    @ApiModelProperty(value = "商品单价")
    private BigDecimal price;

    @ApiModelProperty(value = "购买数量")
    private Integer num;

    @ApiModelProperty(value = "商品总价")
    private BigDecimal totalPrice;
}