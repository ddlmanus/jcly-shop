package com.zbkj.common.response;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 商品公共响应对象
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
@ApiModel(value = "ProductCommonResponse对象", description = "商品公共响应对象")
public class ProductCommonResponse {

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

    @ApiModelProperty(value = "是否自营：0-非自营，1-自营")
    private Boolean isSelf;

    @ApiModelProperty(value = "好评率")
    private String positiveRatio;

    @ApiModelProperty(value = "评论数量")
    private Integer replyNum;

    @ApiModelProperty(value = "会员价格")
    private BigDecimal vipPrice;

    @ApiModelProperty(value = "是否付费会员商品")
    private Boolean isPaidMember;

    @ApiModelProperty(value = "活动边框 列表中是边框 详情中是背景图")
    private String activityStyle;

    @ApiModelProperty(value = "商品标签")
    private ProductTagsFrontResponse productTags;
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
