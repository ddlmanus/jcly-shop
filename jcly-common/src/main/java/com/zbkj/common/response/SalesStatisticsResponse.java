package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售统计响应对象
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
@ApiModel(value="SalesStatisticsResponse对象", description="销售统计响应对象")
public class SalesStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总销售额")
    private BigDecimal totalSalesAmount;

    @ApiModelProperty(value = "今日销售额")
    private BigDecimal todaySalesAmount;

    @ApiModelProperty(value = "昨日销售额")
    private BigDecimal yesterdaySalesAmount;

    @ApiModelProperty(value = "本月销售额")
    private BigDecimal currentMonthSalesAmount;

    @ApiModelProperty(value = "上月销售额")
    private BigDecimal lastMonthSalesAmount;

    @ApiModelProperty(value = "本年销售额")
    private BigDecimal currentYearSalesAmount;

    @ApiModelProperty(value = "上年销售额")
    private BigDecimal lastYearSalesAmount;

    @ApiModelProperty(value = "总订单数")
    private Integer totalOrderCount;

    @ApiModelProperty(value = "今日订单数")
    private Integer todayOrderCount;

    @ApiModelProperty(value = "昨日订单数")
    private Integer yesterdayOrderCount;

    @ApiModelProperty(value = "本月订单数")
    private Integer currentMonthOrderCount;

    @ApiModelProperty(value = "上月订单数")
    private Integer lastMonthOrderCount;

    @ApiModelProperty(value = "本年订单数")
    private Integer currentYearOrderCount;

    @ApiModelProperty(value = "上年订单数")
    private Integer lastYearOrderCount;

    @ApiModelProperty(value = "平均客单价")
    private BigDecimal averageOrderValue;

    @ApiModelProperty(value = "昨日平均客单价")
    private BigDecimal yesterdayAverageOrderValue;

    @ApiModelProperty(value = "本月平均客单价")
    private BigDecimal currentMonthAverageOrderValue;

    @ApiModelProperty(value = "上月平均客单价")
    private BigDecimal lastMonthAverageOrderValue;
}
