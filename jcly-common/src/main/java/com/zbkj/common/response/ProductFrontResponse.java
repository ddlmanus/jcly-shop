package com.zbkj.common.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductAttribute;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 移动端商品响应对象
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
@ApiModel(value = "ProductFrontResponse对象", description = "移动端商品响应对象")
public class ProductFrontResponse {

    @ApiModelProperty(value = "商品id")
    private Integer id;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "市场价")
    private BigDecimal otPrice;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "虚拟销量")
    private Integer ficti;

    @ApiModelProperty(value = "单位名")
    private String unitName;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "商户id")
    private Integer merId;

    @ApiModelProperty(value = "商户名称")
    private String merName;

    @ApiModelProperty(value = "商户分类ID")
    private Integer merCategoryId;

    @ApiModelProperty(value = "商户类型ID")
    private Integer merTypeId;

    @ApiModelProperty(value = "好评率")
    private String positiveRatio;

    @ApiModelProperty(value = "评论数量")
    private Integer replyNum;

    @ApiModelProperty(value = "活动边框 列表中是边框 详情中是背景图")
    private String activityStyle;

    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    
    @ApiModelProperty(value = "品牌名称")
    private String brandName;

    @ApiModelProperty(value = "平台分类id")
    private Integer categoryId;

    @ApiModelProperty(value = "会员价格")
    private BigDecimal vipPrice;

    @ApiModelProperty(value = "是否付费会员商品")
    private Boolean isPaidMember;
    @ApiModelProperty(value = "营销类型：0=基础商品,1=秒杀,2=拼团")
    private Integer marketingType;

    @ApiModelProperty(value = "基础类型：0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品")
    private Integer type;

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
    @ApiModelProperty(value = "商品标签")
    private ProductTagsFrontResponse productTags;
    @ApiModelProperty(value = "商品属性详情")
    private HashMap<String, ProductAttrValueResponse> productValue;
    @ApiModelProperty(value = "产品属性")
    private List<ProductAttribute> productAttr;
}
