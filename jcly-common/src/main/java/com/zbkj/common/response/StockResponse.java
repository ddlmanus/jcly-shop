package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存响应对象
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: AI Assistant
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StockResponse对象", description = "库存响应对象")
public class StockResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "库存ID")
    private Integer id;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品图片")
    private String productImage;

    @ApiModelProperty(value = "SKU编码")
    private String sku;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "商户名称")
    private String merName;

    @ApiModelProperty(value = "商品状态：0-下架，1-上架")
    private Boolean isShow;

    @ApiModelProperty(value = "商品售价")
    private BigDecimal price;

    @ApiModelProperty(value = "当前库存（实时剩余可用库存）")
    private Integer stock;

    @ApiModelProperty(value = "总库存（累计入库总量）")
    private Integer totalStock;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 