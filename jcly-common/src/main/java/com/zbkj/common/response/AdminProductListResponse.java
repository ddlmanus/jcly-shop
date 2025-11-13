package com.zbkj.common.response;

import com.zbkj.common.model.product.ProductAttrValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品列表相应对象
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
@ApiModel(value="AdminProductListResponse对象", description="商品列表相应对象")
public class AdminProductListResponse implements Serializable {

    private static final long serialVersionUID = -4011410120937602623L;

    @ApiModelProperty(value = "商品id")
    private Integer id;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "关键字")
    private String keyword;

    @ApiModelProperty(value = "分类ids")
    private String cateId;

    @ApiModelProperty(value = "平台分类id")
    private Integer categoryId;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "会员价格")
    private BigDecimal vipPrice;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "状态（0：未上架，1：上架）")
    private Boolean isShow;

    @ApiModelProperty(value = "审核状态：0-无需审核 1-待审核，2-审核成功，3-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty(value = "是否加入审核，0-正常，1-审核流程中")
    private Boolean isAudit;

    @ApiModelProperty(value = "虚拟销量")
    private Integer ficti;

    @ApiModelProperty(value = "收藏数量")
    private Integer collectCount;

    @ApiModelProperty(value = "拒绝原因")
    private String reason;

    @ApiModelProperty(value = "基础类型：0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品")
    private Integer type;

    @ApiModelProperty(value = "是否付费会员商品")
    private Boolean isPaidMember;

    @ApiModelProperty(value = "规格 0单 1多")
    private Boolean specType;
    @ApiModelProperty(value = "所属商户id")
    private Integer merId;
    @ApiModelProperty(value = "所属商户名称")
    private String merName;
    @ApiModelProperty(value = "商户联系方式")
    private String phone;
    @ApiModelProperty(value = "商品规格")
    private List<ProductAttrValue> productAttrValues;
}
