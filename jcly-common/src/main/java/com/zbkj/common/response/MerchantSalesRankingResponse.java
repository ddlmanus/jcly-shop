package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 店铺成交金额排行榜响应
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
@ApiModel(value = "MerchantSalesRankingResponse对象", description = "店铺成交金额排行榜响应")
public class MerchantSalesRankingResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "排名")
    private Integer rank;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "店铺头像")
    private String merchantAvatar;

    @ApiModelProperty(value = "成交金额")
    private BigDecimal salesAmount;

    @ApiModelProperty(value = "昨日成交金额")
    private BigDecimal yesterdaySalesAmount;

    @ApiModelProperty(value = "成交订单数")
    private Integer orderCount;

    @ApiModelProperty(value = "增长率")
    private String growthRate;

    @ApiModelProperty(value = "占比")
    private String percentage;
} 