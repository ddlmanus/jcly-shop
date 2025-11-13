package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单报表响应类
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
@ApiModel(value = "OrderReportResponse", description = "订单报表响应")
public class OrderReportResponse {

    @ApiModelProperty(value = "待付款订单数")
    private Integer pendingPaymentOrders;

    @ApiModelProperty(value = "待发货订单数")
    private Integer pendingShipmentOrders;

    @ApiModelProperty(value = "待收货订单数")
    private Integer pendingReceiptOrders;

    @ApiModelProperty(value = "待评价订单数")
    private Integer pendingReviewOrders;

    @ApiModelProperty(value = "处理中订单数")
    private Integer processingOrders;

    @ApiModelProperty(value = "实时订单数统计")
    private OrderCountStatistics orderCountStats;

    @ApiModelProperty(value = "订单金额统计")
    private OrderAmountStatistics orderAmountStats;

    @ApiModelProperty(value = "退款金额统计")
    private RefundAmountStatistics refundAmountStats;

    @ApiModelProperty(value = "付款/退款订单统计趋势")
    private List<OrderTrendData> orderTrend;

    @ApiModelProperty(value = "订单总数统计趋势")
    private List<OrderCountTrendData> orderCountTrend;

    @Data
    @ApiModel(value = "OrderCountStatistics", description = "订单数量统计")
    public static class OrderCountStatistics {
        @ApiModelProperty(value = "昨日订单数")
        private Integer yesterdayOrders;

        @ApiModelProperty(value = "本月订单数")
        private Integer monthOrders;

        @ApiModelProperty(value = "累计订单数")
        private Integer totalOrders;

        @ApiModelProperty(value = "增长率")
        private String growthRate;

        @ApiModelProperty(value = "日订单趋势")
        private List<DailyOrderData> dailyTrend;
    }

    @Data
    @ApiModel(value = "OrderAmountStatistics", description = "订单金额统计")
    public static class OrderAmountStatistics {
        @ApiModelProperty(value = "昨日订单金额")
        private BigDecimal yesterdayAmount;

        @ApiModelProperty(value = "本月订单金额")
        private BigDecimal monthAmount;

        @ApiModelProperty(value = "累计订单金额")
        private BigDecimal totalAmount;

        @ApiModelProperty(value = "增长率")
        private String growthRate;

        @ApiModelProperty(value = "日金额趋势")
        private List<DailyAmountData> dailyTrend;
    }

    @Data
    @ApiModel(value = "RefundAmountStatistics", description = "退款金额统计")
    public static class RefundAmountStatistics {
        @ApiModelProperty(value = "昨日退款金额")
        private BigDecimal yesterdayRefund;

        @ApiModelProperty(value = "本月退款金额")
        private BigDecimal monthRefund;

        @ApiModelProperty(value = "累计退款金额")
        private BigDecimal totalRefund;

        @ApiModelProperty(value = "增长率")
        private String growthRate;

        @ApiModelProperty(value = "日退款趋势")
        private List<DailyRefundData> dailyTrend;
    }

    @Data
    @ApiModel(value = "OrderTrendData", description = "订单趋势数据")
    public static class OrderTrendData {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "付款订单数")
        private Integer paidOrders;

        @ApiModelProperty(value = "退款订单数")
        private Integer refundOrders;
    }

    @Data
    @ApiModel(value = "OrderCountTrendData", description = "订单总数趋势数据")
    public static class OrderCountTrendData {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "订单总数")
        private Integer orderCount;
    }

    @Data
    @ApiModel(value = "DailyOrderData", description = "日订单数据")
    public static class DailyOrderData {
        @ApiModelProperty(value = "时间点")
        private String time;

        @ApiModelProperty(value = "订单数量")
        private Integer count;
    }

    @Data
    @ApiModel(value = "DailyAmountData", description = "日金额数据")
    public static class DailyAmountData {
        @ApiModelProperty(value = "时间点")
        private String time;

        @ApiModelProperty(value = "金额")
        private BigDecimal amount;
    }

    @Data
    @ApiModel(value = "DailyRefundData", description = "日退款数据")
    public static class DailyRefundData {
        @ApiModelProperty(value = "时间点")
        private String time;

        @ApiModelProperty(value = "退款金额")
        private BigDecimal refundAmount;
    }
} 