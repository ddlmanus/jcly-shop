package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 平台收益汇总数据响应
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
@ApiModel(value = "PlatformRevenueResponse对象", description = "平台收益汇总数据响应")
public class PlatformRevenueResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总收益汇总")
    private BigDecimal totalRevenue;

    @ApiModelProperty(value = "分类收益明细")
    private List<RevenueDetail> revenueDetails;

    @ApiModelProperty(value = "月度收益趋势")
    private List<MonthlyRevenue> monthlyTrend;

    @Data
    @ApiModel(value = "收益详细数据")
    public static class RevenueDetail implements Serializable {
        
        @ApiModelProperty(value = "收益类型")
        private String type;
        
        @ApiModelProperty(value = "收益金额")
        private BigDecimal amount;
        
        @ApiModelProperty(value = "占比")
        private String percentage;
    }

    @Data
    @ApiModel(value = "月度收益数据")
    public static class MonthlyRevenue implements Serializable {
        
        @ApiModelProperty(value = "月份")
        private String month;
        
        @ApiModelProperty(value = "提现手续费")
        private BigDecimal withdrawalFee;
        
        @ApiModelProperty(value = "分类明细收益")
        private BigDecimal categoryRevenue;
    }
} 