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
 * 采食家商品详情响应对象
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
@ApiModel(value="CsjProductInfoResponse对象", description="采食家商品详情响应对象")
public class CsjProductInfoResponse implements Serializable {

    private static final long serialVersionUID = 9215241889318610262L;

    @ApiModelProperty(value = "商品id")
    private Integer id;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "轮播图")
    private String sliderImage;

    @ApiModelProperty(value = "商品简介")
    private String intro;

    @ApiModelProperty(value = "商品分类")
    private String categoryName;

    @ApiModelProperty(value = "商品品牌")
    private String brandName;

    @ApiModelProperty(value = "商品销售价格")
    private BigDecimal price;

    @ApiModelProperty(value = "商品原价")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "单位名")
    private String unitName;

    @ApiModelProperty(value = "商品重量(kg)")
    private BigDecimal weight;

    @ApiModelProperty(value = "商品体积(m³)")
    private BigDecimal volume;

    @ApiModelProperty(value = "商品编码")
    private String productCode;

    @ApiModelProperty(value = "商品详情")
    private String content;

    @ApiModelProperty(value = "上架状态（0：未上架，1：上架）")
    private Boolean isShow;

    @ApiModelProperty(value = "跳转URL")
    private String jumpUrl;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
