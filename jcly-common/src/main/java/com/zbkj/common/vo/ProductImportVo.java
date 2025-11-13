package com.zbkj.common.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品导入VO对象
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
@ApiModel(value = "ProductImportVo对象", description = "商品导入VO对象")
public class ProductImportVo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // === 基础信息 ===
    @ExcelProperty(value = "商品类型（0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品）", index = 0)
    @ApiModelProperty(value = "商品类型")
    private String type;
    
    @ExcelProperty(value = "商品名称", index = 1)
    @ApiModelProperty(value = "商品名称")
    private String name;
    
    @ExcelProperty(value = "商户分类名称（多级用>分隔，如：食品>零食>坚果）", index = 2)
    @ApiModelProperty(value = "商户分类名称")
    private String merCategoryName;
    
    @ExcelProperty(value = "平台分类名称", index = 3)
    @ApiModelProperty(value = "平台分类名称")
    private String categoryName;
    
    @ExcelProperty(value = "品牌名称", index = 4)
    @ApiModelProperty(value = "品牌名称")
    private String brandName;
    
    @ExcelProperty(value = "单位名", index = 5)
    @ApiModelProperty(value = "单位名")
    private String unitName;
    
    // === 配送设置 ===
    @ExcelProperty(value = "是否包邮（true/false）", index = 6)
    @ApiModelProperty(value = "是否包邮")
    private String postageSwith;
    
    @ExcelProperty(value = "运费模板名称（不包邮时必填）", index = 7)
    @ApiModelProperty(value = "运费模板名称")
    private String tempName;
    
    @ExcelProperty(value = "商品关键字（多个用逗号分隔）", index = 8)
    @ApiModelProperty(value = "商品关键字")
    private String keyword;
    
    @ExcelProperty(value = "商品简介", index = 9)
    @ApiModelProperty(value = "商品简介")
    private String intro;
    
    // === 图片和媒体 ===
    @ExcelProperty(value = "商品主图", index = 10)
    @ApiModelProperty(value = "商品主图URL")
    private String image;
    
    @ExcelProperty(value = "轮播图（多个用英文逗号分隔）", index = 11)
    @ApiModelProperty(value = "轮播图URL")
    private String sliderImage;
    
    @ExcelProperty(value = "商品详情（HTML内容）", index = 12)
    @ApiModelProperty(value = "商品详情")
    private String content;
    
    // === 佣金设置 ===
    @ExcelProperty(value = "是否单独分佣（true/false）", index = 13)
    @ApiModelProperty(value = "是否单独分佣")
    private String isSub;
    
    // === 配送设置 ===
    @ExcelProperty(value = "配送方式（1-商家配送,2-到店核销,3-快递发货,4-同城配送）", index = 14)
    @ApiModelProperty(value = "配送方式")
    private String deliveryMethod;
    
    @ExcelProperty(value = "是否支持同城配送（true/false）", index = 15)
    @ApiModelProperty(value = "是否支持同城配送")
    private String cityDeliverySwith;
    
//    // === 地址信息 ===
//    @ExcelProperty(value = "所属省", index = 16)
//    @ApiModelProperty(value = "所属省")
//    private String province;
//
//    @ExcelProperty(value = "所属市", index = 17)
//    @ApiModelProperty(value = "所属市")
//    private String city;
//
//    @ExcelProperty(value = "所属区/县", index = 18)
//    @ApiModelProperty(value = "所属区/县")
//    private String area;
//
//    @ExcelProperty(value = "所属街道", index = 19)
//    @ApiModelProperty(value = "所属街道")
//    private String street;
//
//    @ExcelProperty(value = "详细地址", index = 20)
//    @ApiModelProperty(value = "详细地址")
//    private String detail;
    
    // === 商品设置 ===
    @ExcelProperty(value = "是否付费会员商品（true/false）", index = 16)
    @ApiModelProperty(value = "是否付费会员商品")
    private String isPaidMember;
    
    @ExcelProperty(value = "是否支持退款（true/false）", index = 17)
    @ApiModelProperty(value = "是否支持退款")
    private String refundSwitch;
    
    @ExcelProperty(value = "是否限购（true/false）", index = 18)
    @ApiModelProperty(value = "是否限购")
    private String limitSwith;
    
    @ExcelProperty(value = "限购数量（限购时必填）", index = 19)
    @ApiModelProperty(value = "限购数量")
    private String limitNum;
    
    @ExcelProperty(value = "最少购买件数", index = 20)
    @ApiModelProperty(value = "最少购买件数")
    private String minNum;
    
    // === 保障服务 ===
    @ExcelProperty(value = "保障服务（多个用逗号分隔）", index = 21)
    @ApiModelProperty(value = "保障服务")
    private String guaranteeNames;
    
    // === 优惠券 ===
    @ExcelProperty(value = "绑定优惠券名称（多个用逗号分隔）", index = 22)
    @ApiModelProperty(value = "绑定优惠券名称")
    private String couponNames;
    
    // === 系统设置 ===
    @ExcelProperty(value = "是否自动上架（true/false）", index = 23)
    @ApiModelProperty(value = "是否自动上架")
    private String isAutoUp;
    
    @ExcelProperty(value = "是否自动提审（true/false）", index = 24)
    @ApiModelProperty(value = "是否自动提审")
    private String isAutoSubmitAudit;
    
    // === 备注信息 ===
    @ExcelProperty(value = "备注说明", index = 25)
    @ApiModelProperty(value = "备注说明")
    private String remark;
} 