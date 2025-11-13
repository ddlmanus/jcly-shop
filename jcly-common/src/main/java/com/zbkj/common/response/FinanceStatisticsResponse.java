package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 财务统计响应类
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
@ApiModel(value = "FinanceStatisticsResponse对象", description = "财务统计响应")
public class FinanceStatisticsResponse {

    @ApiModelProperty(value = "今日收入")
    private BigDecimal todayIncome;

    @ApiModelProperty(value = "今日支出")
    private BigDecimal todayExpenditure;

    @ApiModelProperty(value = "今日结余")
    private BigDecimal todayBalance;

    @ApiModelProperty(value = "昨日收入")
    private BigDecimal yesterdayIncome;

    @ApiModelProperty(value = "昨日支出")
    private BigDecimal yesterdayExpenditure;

    @ApiModelProperty(value = "昨日结余")
    private BigDecimal yesterdayBalance;

    @ApiModelProperty(value = "本月收入")
    private BigDecimal monthIncome;

    @ApiModelProperty(value = "本月支出")
    private BigDecimal monthExpenditure;

    @ApiModelProperty(value = "本月结余")
    private BigDecimal monthBalance;

    @ApiModelProperty(value = "当前余额")
    private BigDecimal currentBalance;

    @ApiModelProperty(value = "可提现金额")
    private BigDecimal withdrawableAmount;

    @ApiModelProperty(value = "冻结金额")
    private BigDecimal frozenAmount;

    @ApiModelProperty(value = "收入趋势数据")
    private List<FinanceTrendData> incomeTrend;

    @ApiModelProperty(value = "支出趋势数据")
    private List<FinanceTrendData> expenditureTrend;

    @ApiModelProperty(value = "结余趋势数据")
    private List<FinanceTrendData> balanceTrend;

    @Data
    @ApiModel(value = "FinanceTrendData对象", description = "财务趋势数据")
    public static class FinanceTrendData {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "收入金额")
        private BigDecimal income;

        @ApiModelProperty(value = "支出金额")
        private BigDecimal expenditure;

        @ApiModelProperty(value = "结余金额")
        private BigDecimal balance;
    }
} 