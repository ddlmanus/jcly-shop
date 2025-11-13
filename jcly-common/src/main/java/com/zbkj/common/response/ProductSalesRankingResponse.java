package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品销量排行榜响应
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
@ApiModel(value = "ProductSalesRankingResponse对象", description = "商品销量排行榜响应")
public class ProductSalesRankingResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品图片")
    private String productImage;

    @ApiModelProperty(value = "销量")
    private Integer salesCount;

    @ApiModelProperty(value = "销售额")
    private BigDecimal salesAmount;

    @ApiModelProperty(value = "排名")
    private Integer rank;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "占比百分比")
    private String percentage;
} 