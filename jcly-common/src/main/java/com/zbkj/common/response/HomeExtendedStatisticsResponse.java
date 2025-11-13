package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 首页扩展统计数据响应对象
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
@ApiModel(value = "HomeExtendedStatisticsResponse对象", description = "首页扩展统计数据响应对象")
public class HomeExtendedStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单转化率")
    private BigDecimal orderConversionRate;

    @ApiModelProperty(value = "昨日订单转化率")
    private BigDecimal yesterdayOrderConversionRate;

    @ApiModelProperty(value = "会员总数")
    private Integer memberCount;

    @ApiModelProperty(value = "昨日会员总数")
    private Integer yesterdayMemberCount;

    @ApiModelProperty(value = "今日新增会员数")
    private Integer todayNewMemberCount;

    @ApiModelProperty(value = "昨日新增会员数")
    private Integer yesterdayNewMemberCount;

    @ApiModelProperty(value = "库存周转率")
    private BigDecimal inventoryTurnoverRate;

    @ApiModelProperty(value = "昨日库存周转率")
    private BigDecimal yesterdayInventoryTurnoverRate;

    @ApiModelProperty(value = "商品评价总数")
    private Integer productReviewCount;

    @ApiModelProperty(value = "昨日商品评价总数")
    private Integer yesterdayProductReviewCount;

    @ApiModelProperty(value = "今日新增商品评价数")
    private Integer todayNewProductReviewCount;

    @ApiModelProperty(value = "昨日新增商品评价数")
    private Integer yesterdayNewProductReviewCount;

    @ApiModelProperty(value = "平均评分")
    private BigDecimal averageRating;

    @ApiModelProperty(value = "昨日平均评分")
    private BigDecimal yesterdayAverageRating;

    @ApiModelProperty(value = "今日访客数（用于计算转化率）")
    private Integer todayVisitors;

    @ApiModelProperty(value = "昨日访客数（用于计算转化率）")
    private Integer yesterdayVisitors;

    @ApiModelProperty(value = "今日订单数（用于计算转化率）")
    private Integer todayOrders;

    @ApiModelProperty(value = "昨日订单数（用于计算转化率）")
    private Integer yesterdayOrders;

    @ApiModelProperty(value = "库存总值")
    private BigDecimal totalInventoryValue;

    @ApiModelProperty(value = "昨日库存总值")
    private BigDecimal yesterdayTotalInventoryValue;

    @ApiModelProperty(value = "销售成本")
    private BigDecimal costOfGoodsSold;

    @ApiModelProperty(value = "昨日销售成本")
    private BigDecimal yesterdayCostOfGoodsSold;
}
