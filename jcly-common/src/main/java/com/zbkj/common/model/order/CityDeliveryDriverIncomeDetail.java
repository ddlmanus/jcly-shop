package com.zbkj.common.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 配送员收入明细表
 * @author 荆楚粮油
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_city_delivery_driver_income_detail")
@ApiModel(value="CityDeliveryDriverIncomeDetail对象", description="配送员收入明细表")
public class CityDeliveryDriverIncomeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "收入明细ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "配送员ID")
    private Integer driverId;

    @ApiModelProperty(value = "配送员姓名")
    private String driverName;

    @ApiModelProperty(value = "配送订单号")
    private String deliveryOrderNo;

    @ApiModelProperty(value = "原订单号")
    private String orderNo;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "商户名称")
    private String merName;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "配送区域ID")
    private Integer areaId;

    @ApiModelProperty(value = "配送区域名称")
    private String areaName;

    @ApiModelProperty(value = "费用规则ID")
    private Integer feeRuleId;

    @ApiModelProperty(value = "费用规则名称")
    private String feeRuleName;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "配送费用")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "配送距离（公里）")
    private BigDecimal deliveryDistance;

    @ApiModelProperty(value = "配送时长（分钟）")
    private Integer deliveryDuration;

    @ApiModelProperty(value = "基础配送费")
    private BigDecimal baseFee;

    @ApiModelProperty(value = "距离费用")
    private BigDecimal distanceFee;

    @ApiModelProperty(value = "时间费用（夜间、节假日等）")
    private BigDecimal timeFee;

    @ApiModelProperty(value = "加急费用")
    private BigDecimal urgentFee;

    @ApiModelProperty(value = "其他费用")
    private BigDecimal otherFee;

    @ApiModelProperty(value = "配送员收入")
    private BigDecimal driverIncome;

    @ApiModelProperty(value = "基础收入")
    private BigDecimal baseIncome;

    @ApiModelProperty(value = "佣金收入")
    private BigDecimal commissionIncome;

    @ApiModelProperty(value = "距离奖励")
    private BigDecimal distanceBonus;

    @ApiModelProperty(value = "时效奖励")
    private BigDecimal timeBonus;

    @ApiModelProperty(value = "评分奖励")
    private BigDecimal ratingBonus;

    @ApiModelProperty(value = "其他奖励")
    private BigDecimal otherBonus;

    @ApiModelProperty(value = "佣金比例（%）")
    private BigDecimal commissionRate;

    @ApiModelProperty(value = "平台收入")
    private BigDecimal platformIncome;

    @ApiModelProperty(value = "商户承担费用")
    private BigDecimal merchantCost;

    @ApiModelProperty(value = "用户支付费用")
    private BigDecimal userPayment;

    @ApiModelProperty(value = "补贴金额")
    private BigDecimal subsidyAmount;

    @ApiModelProperty(value = "扣除金额（违规等）")
    private BigDecimal deductionAmount;

    @ApiModelProperty(value = "扣除原因")
    private String deductionReason;

    @ApiModelProperty(value = "配送类型：1-即时配送，2-预约配送")
    private Integer deliveryType;

    @ApiModelProperty(value = "配送状态：0-待接单，1-已接单，2-取件中，3-配送中，4-已送达，5-配送失败，9-已取消")
    private Integer deliveryStatus;

    @ApiModelProperty(value = "结算状态：0-未结算，1-已结算，2-已提现")
    private Integer settlementStatus;

    @ApiModelProperty(value = "订单完成时间")
    private Date completedTime;

    @ApiModelProperty(value = "结算时间")
    private Date settlementTime;

    @ApiModelProperty(value = "客户评分")
    private BigDecimal customerRating;

    @ApiModelProperty(value = "配送开始时间")
    private Date startTime;

    @ApiModelProperty(value = "配送结束时间")
    private Date endTime;

    @ApiModelProperty(value = "取件地址")
    private String pickupAddress;

    @ApiModelProperty(value = "收货地址")
    private String deliveryAddress;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否删除（0：未删除；1：已删除）")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 