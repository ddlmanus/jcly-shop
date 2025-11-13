package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品简易响应对象
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
@ApiModel(value = "ProductSimpleResponse对象", description = "商品简易响应对象")
public class ProductSimpleResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id")
    private Integer productId;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;
    @ApiModelProperty(value = "商品品牌")
    private String brandName;
    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "库存")
    private Integer stock;
    @ApiModelProperty(value = "是否限购")
    private Boolean limitSwith=false;
    @ApiModelProperty(value = "限购数量")
    private Integer limitNum;
    @ApiModelProperty(value = "最少购买件数")
    private Integer minNum;

    @ApiModelProperty(value = "是否包邮")
    private Boolean postageSwith=false;
    @ApiModelProperty(value = "是否支持同城配送")
    private Boolean cityDeliverySwith= false;
    @ApiModelProperty(value = "所属省")
    private String province;
    @ApiModelProperty(value = "所属省code")
    private Integer provinceCode;
    @ApiModelProperty(value = "所属市")
    private String city;
    @ApiModelProperty(value = "所属市code")
    private Integer cityCode;
    @ApiModelProperty(value = "商品所在区域")
    private String area;
    @ApiModelProperty(value = "商品所在区域code")
    private Integer areaCode;
    @ApiModelProperty(value = "所属街道")
    private String street;
    @ApiModelProperty(value = "所属街道code")
    private Integer streetCode;
    @ApiModelProperty(value = "详细地址")
    private String detail;
}
