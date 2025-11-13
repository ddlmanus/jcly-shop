package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单统计响应类
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
@ApiModel(value = "OrderStatisticsResponse对象", description = "订单统计响应")
public class OrderStatisticsResponse {

    @ApiModelProperty(value = "今日订单数量")
    private Integer todayOrderCount;

    @ApiModelProperty(value = "今日订单金额")
    private BigDecimal todayOrderAmount;

    @ApiModelProperty(value = "今日支付订单数量")
    private Integer todayPaidOrderCount;

    @ApiModelProperty(value = "今日支付金额")
    private BigDecimal todayPaidAmount;

    @ApiModelProperty(value = "昨日订单数量")
    private Integer yesterdayOrderCount;

    @ApiModelProperty(value = "昨日订单金额")
    private BigDecimal yesterdayOrderAmount;

    @ApiModelProperty(value = "昨日支付订单数量")
    private Integer yesterdayPaidOrderCount;

    @ApiModelProperty(value = "昨日支付金额")
    private BigDecimal yesterdayPaidAmount;

    @ApiModelProperty(value = "本月订单数量")
    private Integer monthOrderCount;

    @ApiModelProperty(value = "本月订单金额")
    private BigDecimal monthOrderAmount;

    @ApiModelProperty(value = "本月支付订单数量")
    private Integer monthPaidOrderCount;

    @ApiModelProperty(value = "本月支付金额")
    private BigDecimal monthPaidAmount;

    @ApiModelProperty(value = "订单趋势数据")
    private List<OrderTrendData> orderTrend;

    @ApiModelProperty(value = "支付金额趋势数据")
    private List<OrderTrendData> amountTrend;

    @Data
    @ApiModel(value = "OrderTrendData对象", description = "订单趋势数据")
    public static class OrderTrendData {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "数值")
        private BigDecimal value;

        @ApiModelProperty(value = "数量")
        private Integer count;
    }
} 