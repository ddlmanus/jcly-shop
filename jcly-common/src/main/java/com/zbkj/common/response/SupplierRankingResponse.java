package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 供应商供货排行榜响应
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
@ApiModel(value = "SupplierRankingResponse对象", description = "供应商供货排行榜响应")
public class SupplierRankingResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "排名")
    private Integer rank;

    @ApiModelProperty(value = "供应商ID(商户ID)")
    private Integer supplierId;

    @ApiModelProperty(value = "供应商名称")
    private String supplierName;

    @ApiModelProperty(value = "供应商头像")
    private String supplierAvatar;

    @ApiModelProperty(value = "供货金额")
    private BigDecimal supplyAmount;

    @ApiModelProperty(value = "昨日供货金额")
    private BigDecimal yesterdaySupplyAmount;

    @ApiModelProperty(value = "供货商品数量")
    private Integer productCount;

    @ApiModelProperty(value = "供货订单数")
    private Integer orderCount;

    @ApiModelProperty(value = "增长率")
    private String growthRate;

    @ApiModelProperty(value = "占比")
    private String percentage;
} 