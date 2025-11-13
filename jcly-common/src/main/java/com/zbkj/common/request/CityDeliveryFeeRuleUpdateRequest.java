package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 更新配送费用规则请求参数
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
@ApiModel(value = "CityDeliveryFeeRuleUpdateRequest", description = "更新配送费用规则请求参数")
public class CityDeliveryFeeRuleUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "费用规则ID不能为空")
    @ApiModelProperty(value = "费用规则ID", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "规则名称", example = "默认距离费用规则")
    private String ruleName;

    @ApiModelProperty(value = "基础费用", example = "5.00")
    private BigDecimal baseFee;

    @ApiModelProperty(value = "基础距离（公里）", example = "3.00")
    private BigDecimal baseDistance;

    @ApiModelProperty(value = "每公里费用", example = "2.00")
    private BigDecimal additionalFeePerKm;

    @ApiModelProperty(value = "额外距离费用", example = "0.00")
    private BigDecimal extraDistanceFee;

    @ApiModelProperty(value = "最低费用", example = "5.00")
    private BigDecimal minFee;

    @ApiModelProperty(value = "最高费用", example = "100.00")
    private BigDecimal maxFee;

    @ApiModelProperty(value = "即时配送费用", example = "0.00")
    private BigDecimal instantDeliveryFee;

    @ApiModelProperty(value = "夜间配送费用", example = "2.00")
    private BigDecimal nightDeliveryFee;

    @ApiModelProperty(value = "节假日配送费用", example = "3.00")
    private BigDecimal holidayDeliveryFee;

    @ApiModelProperty(value = "雨天配送费用", example = "2.00")
    private BigDecimal rainyDayFee;

    @ApiModelProperty(value = "重量费用规则（JSON格式）", example = "[{\"weight\":5,\"fee\":2.00}]")
    private String weightFeeRule;

    @ApiModelProperty(value = "体积费用规则（JSON格式）", example = "[{\"volume\":0.1,\"fee\":3.00}]")
    private String volumeFeeRule;

    @ApiModelProperty(value = "时段费用规则（JSON格式）", example = "[{\"timeSlot\":\"09:00-18:00\",\"fee\":1.00}]")
    private String timeSlotFeeRule;

    @ApiModelProperty(value = "距离费用阶梯（JSON格式）", example = "[{\"distance\":10,\"fee\":1.50}]")
    private String distanceFeeSteps;

    @ApiModelProperty(value = "等待费用（每分钟）", example = "0.50")
    private BigDecimal waitingFeePerMinute;

    @ApiModelProperty(value = "免费等待时间（分钟）", example = "5")
    private Integer freeWaitingTime;

    @ApiModelProperty(value = "取消费用规则（JSON格式）", example = "[{\"timeBeforePickup\":30,\"fee\":5.00}]")
    private String cancelFeeRule;

    @ApiModelProperty(value = "平台佣金比例(%)", example = "10.00")
    private BigDecimal platformCommissionRate;

    @ApiModelProperty(value = "配送员佣金比例(%)", example = "80.00")
    private BigDecimal driverCommissionRate;

    @ApiModelProperty(value = "重量基础费用", example = "0.00")
    private BigDecimal weightBaseFee;

    @ApiModelProperty(value = "体积基础费用", example = "0.00")
    private BigDecimal volumeBaseFee;

    @ApiModelProperty(value = "时间倍数", example = "1.00")
    private BigDecimal timeMultiplier;

    @ApiModelProperty(value = "天气倍数", example = "1.00")
    private BigDecimal weatherMultiplier;

    @ApiModelProperty(value = "节假日倍数", example = "1.50")
    private BigDecimal holidayMultiplier;

    @ApiModelProperty(value = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "配送类型：1-即时配送，2-预约配送", example = "1")
    private Integer deliveryType;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "适用区域（JSON格式）", example = "[\"001\",\"002\"]")
    private String applicableAreas;

    @ApiModelProperty(value = "生效时间", example = "2024-01-01 00:00:00")
    private Date effectiveTime;

    @ApiModelProperty(value = "过期时间", example = "2024-12-31 23:59:59")
    private Date expireTime;

    @ApiModelProperty(value = "备注", example = "更新费用规则")
    private String remark;
} 