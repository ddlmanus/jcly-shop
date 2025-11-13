package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单概览统计响应对象
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
@ApiModel(value = "OrderOverviewStatisticsResponse对象", description = "订单概览统计响应对象")
public class OrderOverviewStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单总数量")
    private Integer totalOrders;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "已付款数量")
    private Integer paidOrders;

    @ApiModelProperty(value = "已付款金额")
    private BigDecimal paidAmount;

    @ApiModelProperty(value = "退款总金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "佣金总金额")
    private BigDecimal commissionAmount;

    @ApiModelProperty(value = "平台手续费")
    private BigDecimal platformFee;

    @ApiModelProperty(value = "待发货订单数")
    private Integer pendingShipmentOrders;

    @ApiModelProperty(value = "已完成订单数")
    private Integer completedOrders;

    @ApiModelProperty(value = "已取消订单数")
    private Integer cancelledOrders;
} 