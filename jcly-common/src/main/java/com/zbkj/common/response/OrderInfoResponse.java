package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单信息响应对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderInfoResponse对象", description="订单信息响应对象")
public class OrderInfoResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单ID")
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "店铺ID")
    private Integer storeId;

    @ApiModelProperty(value = "店铺名称")
    private String storeName;

    @ApiModelProperty(value = "订单类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单状态")
    private Integer status;

    @ApiModelProperty(value = "支付状态")
    private Integer payStatus;

    @ApiModelProperty(value = "支付方式")
    private Integer payType;

    @ApiModelProperty(value = "支付时间")
    private Date payTime;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal totalPrice;

    @ApiModelProperty(value = "实际支付金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal couponPrice;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "订单商品列表")
    private List<OrderProductResponse> productList;
}