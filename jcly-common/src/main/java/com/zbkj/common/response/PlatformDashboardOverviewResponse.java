package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 平台仪表板概览数据响应
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
@ApiModel(value = "PlatformDashboardOverviewResponse对象", description = "平台仪表板概览数据响应")
public class PlatformDashboardOverviewResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品总数")
    private Integer productTotal;

    @ApiModelProperty(value = "上架商品数量")
    private Integer productOnSale;

    @ApiModelProperty(value = "下架商品数量")
    private Integer productOffSale;

    @ApiModelProperty(value = "待审核商品数量")
    private Integer productPending;

    @ApiModelProperty(value = "商品业绩(今日销售额)")
    private BigDecimal productSales;

    @ApiModelProperty(value = "商品业绩增长率")
    private String productSalesGrowthRate;

    @ApiModelProperty(value = "当月销售额")
    private BigDecimal monthSales;

    @ApiModelProperty(value = "累计销售额")
    private BigDecimal totalSales;

    @ApiModelProperty(value = "开通店铺总数")
    private Integer merchantTotal;

    @ApiModelProperty(value = "今日新增店铺")
    private Integer merchantToday;

    @ApiModelProperty(value = "本月新增店铺")
    private Integer merchantThisMonth;

    @ApiModelProperty(value = "店铺增长率")
    private String merchantGrowthRate;

    @ApiModelProperty(value = "入驻供应商总数")
    private Integer supplierTotal;

    @ApiModelProperty(value = "今日新增供应商")
    private Integer supplierToday;

    @ApiModelProperty(value = "本月新增供应商")
    private Integer supplierThisMonth;

    @ApiModelProperty(value = "供应商增长率")
    private String supplierGrowthRate;
} 