package com.zbkj.common.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 同城配送订单表
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
@TableName("eb_city_delivery_order")
@ApiModel(value = "CityDeliveryOrder", description = "同城配送订单表")
public class CityDeliveryOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配送订单ID", required = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "配送订单号", required = true, example = "D202512010001")
    @NotBlank(message = "配送订单号不能为空")
    private String deliveryOrderNo;

    @ApiModelProperty(value = "原订单号", required = true, example = "2025120100001")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
    @ApiModelProperty(value = "订单金额", required = true)
    @NotNull(message = "订单金额不能为空")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "商户ID", required = true)
    @NotNull(message = "商户ID不能为空")
    private Integer merId;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Integer uid;

    @ApiModelProperty(value = "用户ID（备用字段）")
    private Integer userId;

    @ApiModelProperty(value = "配送员ID")
    private Integer driverId;

    @ApiModelProperty(value = "配送员姓名")
    private String driverName;

    @ApiModelProperty(value = "配送员手机号")
    private String driverPhone;

    @ApiModelProperty(value = "配送区域ID")
    private Integer areaId;

    @ApiModelProperty(value = "费用规则ID")
    private Integer feeRuleId;

    @ApiModelProperty(value = "取件地址", required = true)
    @NotBlank(message = "取件地址不能为空")
    private String pickupAddress;

    @ApiModelProperty(value = "取件地址经度")
    private BigDecimal pickupLongitude;

    @ApiModelProperty(value = "取件地址纬度")
    private BigDecimal pickupLatitude;

    @ApiModelProperty(value = "取件联系人")
    private String pickupContact;

    @ApiModelProperty(value = "取件联系电话")
    private String pickupPhone;

    @ApiModelProperty(value = "送货地址", required = true)
    @NotBlank(message = "送货地址不能为空")
    private String deliveryAddress;

    @ApiModelProperty(value = "送货地址经度")
    private BigDecimal deliveryLongitude;

    @ApiModelProperty(value = "送货地址纬度")
    private BigDecimal deliveryLatitude;

    @ApiModelProperty(value = "收货联系人")
    private String deliveryContact;

    @ApiModelProperty(value = "收货联系电话")
    private String deliveryContactPhone;

    @ApiModelProperty(value = "收货联系电话（备用字段）")
    private String deliveryPhone;

    @ApiModelProperty(value = "配送距离（公里）", example = "5.5")
    private BigDecimal distance;

    @ApiModelProperty(value = "配送距离（公里）备用字段", example = "5.5")
    private BigDecimal deliveryDistance;

    @ApiModelProperty(value = "配送费用（元）", required = true, example = "15.00")
    @NotNull(message = "配送费用不能为空")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "实际费用（元）", example = "15.00")
    private BigDecimal actualFee;

    @ApiModelProperty(value = "配送员佣金（元）", example = "10.00")
    private BigDecimal driverCommission;

    @ApiModelProperty(value = "平台抽佣（元）", example = "3.00")
    private BigDecimal platformCommission;

    @ApiModelProperty(value = "配送类型：1-即时配送，2-预约配送", example = "1")
    private Integer deliveryType;

    @ApiModelProperty(value = "配送状态：0-待派单，1-已派单，2-已接单，3-取件中，4-配送中，5-已完成，6-已取消，7-配送失败，8-异常", required = true, example = "0")
    @NotNull(message = "配送状态不能为空")
    private Integer deliveryStatus;

    @ApiModelProperty(value = "紧急程度：1-普通，2-加急，3-特急", example = "1")
    private Integer urgencyLevel;

    @ApiModelProperty(value = "预计送达时间（分钟）", example = "30")
    private String estimatedTime;

    @ApiModelProperty(value = "预计取件时间")
    private Date estimatedPickupTime;

    @ApiModelProperty(value = "预计送达时间")
    private Date estimatedDeliveryTime;

    @ApiModelProperty(value = "实际取件时间")
    private Date actualPickupTime;

    @ApiModelProperty(value = "实际送达时间")
    private Date actualDeliveryTime;

    @ApiModelProperty(value = "取件时间")
    private Date pickupTime;

    @ApiModelProperty(value = "开始配送时间")
    private Date startDeliveryTime;

    @ApiModelProperty(value = "完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "取消时间")
    private Date cancelTime;

    @ApiModelProperty(value = "异常时间")
    private Date exceptionTime;

    @ApiModelProperty(value = "异常原因")
    private String exceptionReason;

    @ApiModelProperty(value = "取件码", example = "123456")
    private String pickupCode;

    @ApiModelProperty(value = "收货码", example = "789012")
    private String deliveryCode;

    @ApiModelProperty(value = "货物信息（JSON格式）")
    private String goodsInfo;

    @ApiModelProperty(value = "特殊要求")
    private String specialRequirements;

    @ApiModelProperty(value = "配送备注")
    private String deliveryRemark;

    @ApiModelProperty(value = "配送轨迹")
    private String deliveryTrack;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "异常信息")
    private String exceptionInfo;

    @ApiModelProperty(value = "客户评分（1-5分）", example = "5")
    private Integer customerRating;

    @ApiModelProperty(value = "客户反馈")
    private String customerFeedback;

    @ApiModelProperty(value = "配送员评分（1-5分）", example = "5")
    private Integer driverRating;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间", required = true)
    @NotNull(message = "创建时间不能为空")
    private Date createTime;

    @ApiModelProperty(value = "更新时间", required = true)
    @NotNull(message = "更新时间不能为空")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除：0-正常，1-删除", example = "0")
    private Integer isDel;
} 