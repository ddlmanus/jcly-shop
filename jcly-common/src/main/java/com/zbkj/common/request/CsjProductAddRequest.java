package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采食家商品添加对象
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
@ApiModel(value = "CsjProductAddRequest对象", description = "采食家商品添加对象")
public class CsjProductAddRequest implements Serializable {

    private static final long serialVersionUID = -452373239606480650L;

    @ApiModelProperty(value = "商品id|添加时不填，修改时必填")
    private Integer id;

    @ApiModelProperty(value = "商品名称", required = true)
    @NotBlank(message = "商品名称不能为空")
    @Length(max = 100, message = "商品名称长度不能超过100个字符")
    private String name;

    @ApiModelProperty(value = "商品图片", required = true)
    @NotBlank(message = "商品图片不能为空")
    @Length(max = 255, message = "商品图片名称长度不能超过255个字符")
    private String image;

    @ApiModelProperty(value = "轮播图", required = true)
    @NotBlank(message = "轮播图不能为空")
    @Length(max = 2000, message = "轮播图名称长度不能超过2000个字符")
    private String sliderImage;

    @ApiModelProperty(value = "商品简介", required = true)
    @NotBlank(message = "商品简介不能为空")
    @Length(max = 200, message = "商品简介长度不能超过200个字符")
    private String intro;

    @ApiModelProperty(value = "商品分类", required = true)
    @NotBlank(message = "商品分类不能为空")
    @Length(max = 50, message = "商品分类长度不能超过50个字符")
    private String categoryName;

    @ApiModelProperty(value = "商品品牌", required = true)
    @NotBlank(message = "商品品牌不能为空")
    @Length(max = 50, message = "商品品牌长度不能超过50个字符")
    private String brandName;

    @ApiModelProperty(value = "商品销售价格", required = true)
    @NotNull(message = "商品销售价格不能为空")
    private BigDecimal price;

    @ApiModelProperty(value = "商品原价", required = true)
    @NotNull(message = "商品原价不能为空")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "库存", required = true)
    @NotNull(message = "库存不能为空")
    private Integer stock;

    @ApiModelProperty(value = "销量")
    private Integer sales = 0;

    @ApiModelProperty(value = "单位名", required = true)
    @NotBlank(message = "单位名不能为空")
    @Length(max = 20, message = "单位名长度不能超过20个字符")
    private String unitName;

    @ApiModelProperty(value = "商品重量(kg)")
    private BigDecimal weight;

    @ApiModelProperty(value = "商品体积(m³)")
    private BigDecimal volume;

    @ApiModelProperty(value = "商品编码", required = true)
    @NotBlank(message = "商品编码不能为空")
    @Length(max = 50, message = "商品编码长度不能超过50个字符")
    private String productCode;

    @ApiModelProperty(value = "商品详情")
    private String content;

    @ApiModelProperty(value = "上架状态（0：未上架，1：上架）")
    private Boolean isShow = false;

    @ApiModelProperty(value = "跳转URL")
    @Length(max = 500, message = "跳转URL长度不能超过500个字符")
    private String jumpUrl;

    @ApiModelProperty(value = "排序")
    private Integer sort = 0;
}
