package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 同城配送订单请求参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CityDeliveryOrderRequest", description = "同城配送订单请求参数")
public class CityDeliveryOrderRequest {

    @ApiModelProperty(value = "原订单号", required = true)
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @ApiModelProperty(value = "配送类型：1-即时配送，2-预约配送", required = true)
    @NotNull(message = "配送类型不能为空")
    private Integer deliveryType;

    @ApiModelProperty(value = "预约时间（预约配送时必填）")
    private String scheduledTime;

    @ApiModelProperty(value = "取件地址", required = true)
    @NotBlank(message = "取件地址不能为空")
    private String pickupAddress;

    @ApiModelProperty(value = "取件地址经度")
    private BigDecimal pickupLongitude;

    @ApiModelProperty(value = "取件地址纬度")
    private BigDecimal pickupLatitude;

    @ApiModelProperty(value = "取件联系人", required = true)
    @NotBlank(message = "取件联系人不能为空")
    private String pickupContact;

    @ApiModelProperty(value = "取件联系电话", required = true)
    @NotBlank(message = "取件联系电话不能为空")
    private String pickupPhone;

    @ApiModelProperty(value = "收货地址")
    private String deliveryAddress;

    @ApiModelProperty(value = "收货地址经度")
    private BigDecimal deliveryLongitude;

    @ApiModelProperty(value = "收货地址纬度")
    private BigDecimal deliveryLatitude;

    @ApiModelProperty(value = "收货联系人")
    private String deliveryContact;

    @ApiModelProperty(value = "收货联系电话")
    private String deliveryPhone;

    @ApiModelProperty(value = "指定配送员ID（可选）")
    private Integer driverId;

    @ApiModelProperty(value = "配送备注")
    private String deliveryRemark;

    @ApiModelProperty(value = "特殊要求")
    private String specialRequirements;

    @ApiModelProperty(value = "货物信息JSON字符串")
    private String goodsInfo;

    @ApiModelProperty(value = "紧急程度：1-普通，2-加急，3-特急")
    private Integer urgencyLevel;

    @ApiModelProperty(value = "配送距离（公里）")
    private BigDecimal distance;

    @ApiModelProperty(value = "配送费用")
    private BigDecimal deliveryFee;
} 