package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 商品添加对象
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
@ApiModel(value = "ProductAddRequest对象", description = "商品添加对象")
public class ProductAddRequest implements Serializable {

    private static final long serialVersionUID = -452373239606480650L;

    @ApiModelProperty(value = "商品id|添加时不填，修改时必填")
    private Integer id;

    @ApiModelProperty(value = "基础类型：0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品", required = true)
    @NotNull(message = "请选择商品类型")
    private Integer type;

    @ApiModelProperty(value = "商品图片", required = true)
    @NotBlank(message = "商品图片不能为空")
    @Length(max = 255, message = "商品图片名称长度不能超过255个字符")
    private String image;

    @ApiModelProperty(value = "展示图")
    @Length(max = 1000, message = "展示图名称长度不能超过1000个字符")
    private String flatPattern;

    @ApiModelProperty(value = "轮播图", required = true)
    @NotBlank(message = "轮播图不能为空")
    @Length(max = 2000, message = "轮播图名称长度不能超过2000个字符")
    private String sliderImage;

    @ApiModelProperty(value = "商品名称", required = true)
    @NotBlank(message = "商品名称不能为空")
    @Length(max = 50, message = "商品名称长度不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "商品简介", required = true)
    @NotBlank(message = "商品简介不能为空")
    @Length(max = 100, message = "商品简介长度不能超过100个字符")
    private String intro;

    @ApiModelProperty(value = "关键字")
//    @Length(max = 255, message = "关键字长度不能超过255个字符")
//    @NotBlank(message = "关键字不能为空")
    private String keyword;

    @ApiModelProperty(value = "商户商品分类id|逗号分隔", required = true)
    @NotBlank(message = "商户商品分类不能为空")
    @Length(max = 64, message = "商品分类组合长度不能超过64个字符")
    private String cateId;

    @ApiModelProperty(value = "品牌id", required = true)
    @NotNull(message = "品牌id不能为空")
    private Integer brandId;

    @ApiModelProperty(value = "平台分类id", required = true)
    @NotNull(message = "平台分类id不能为空")
    private Integer categoryId;

    @ApiModelProperty(value = "保障服务ids(英文逗号拼接)")
    private String guaranteeIds;

    @ApiModelProperty(value = "单位名", required = true)
    @NotBlank(message = "单位名称不能为空")
    @Length(max = 32, message = "单位名长度不能超过32个字符")
    private String unitName;

    @ApiModelProperty(value = "运费模板ID", required = true)
    @NotNull(message = "运费模板ID不能为空")
    private Integer tempId;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "规格 0单 1多", required = true)
    @NotNull(message = "商品规格类型不能为空")
    private Boolean specType;

    @ApiModelProperty(value = "是否单独分佣", required = true)
    @NotNull(message = "请选择是否单独分佣")
    private Boolean isSub;

    @Valid
    @ApiModelProperty(value = "商品属性", required = true)
    @NotEmpty(message = "商品属性不能为空")
    private List<ProductAttrAddRequest> attrList;

    @Valid
    @ApiModelProperty(value = "商品属性详情", required = true)
    @NotEmpty(message = "商品属性详情不能为空")
    private List<ProductAttrValueAddRequest> attrValueList;

    @ApiModelProperty(value = "商品描述")
    private String content;

    @ApiModelProperty(value = "优惠券id集合")
    private List<Integer> couponIds;

    @ApiModelProperty(value = "是否付费会员商品")
    @NotNull(message = "是否付费会员商品不能为空")
    private Boolean isPaidMember;

    @ApiModelProperty(value = "是否自动上架")
    private Boolean isAutoUp = false;

    @ApiModelProperty(value = "是否自动提审")
    private Boolean isAutoSubmitAudit = false;

    @ApiModelProperty(value = "配送方式：1-商家配送，2-到店核销,逗号拼接")
    @NotBlank(message = "请选择配送方式")
    private String deliveryMethod;

    @ApiModelProperty(value = "是否支持退款")
    private Boolean refundSwitch = true;

    @ApiModelProperty(value = "系统表单ID")
    private Integer systemFormId = 0;

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
    @ApiModelProperty(value = "所属门店")
    private String storeId;
    
    // ========== 预约商品相关字段 ==========
    
    @ApiModelProperty(value = "服务模式：shop-到店服务，home-上门服务（逗号分隔）")
    private String serviceMode;
    
    @ApiModelProperty(value = "是否显示剩余可约数量")
    private Boolean showRemaining = true;
    
    @ApiModelProperty(value = "可约日期类型：everyday-每天，custom-自定义")
    private String bookingDates = "everyday";
    
    @ApiModelProperty(value = "预约日期范围（天）")
    private Integer bookingDaysRange = 1;
    
    @ApiModelProperty(value = "提前预约：none-无需提前预约，required-需要提前预约")
    private String advanceBooking = "none";
    
    @ApiModelProperty(value = "提前预约时间（小时）")
    private Integer advanceBookingTime = 1;
    
    @ApiModelProperty(value = "取消订单：notAllowed-不允许取消，allowed-允许取消")
    private String cancelOrder = "allowed";
    
    @ApiModelProperty(value = "取消订单时间（小时）")
    private Integer cancelOrderTime = 1;
    
    @ApiModelProperty(value = "表单提交类型：perBooking-每个预约提交一次，perOrder-每单提交一次")
    private String formSubmitType = "perBooking";
    
    @ApiModelProperty(value = "关联表单ID（预约商品专用）")
    private Integer associatedFormId;
    
    @ApiModelProperty(value = "时段划分类型：auto-自动划分，custom-自定义划分")
    private String timeDivisionType = "auto";
    
    @ApiModelProperty(value = "起始时间")
    private String startTime = "09:00";
    
    @ApiModelProperty(value = "结束时间")
    private String endTime = "18:00";
    
    @ApiModelProperty(value = "间隔时间（分钟）")
    private Integer intervalTime = 60;
    
    @ApiModelProperty(value = "时间段配置（JSON格式）")
    private String timeSlotsConfig;
}
