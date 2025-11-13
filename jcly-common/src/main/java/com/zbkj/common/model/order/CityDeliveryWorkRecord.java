package com.zbkj.common.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 配送员工作记录表
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
@TableName("eb_city_delivery_work_record")
@ApiModel(value = "CityDeliveryWorkRecord", description = "配送员工作记录实体类")
public class CityDeliveryWorkRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "记录ID", example = "1")
    private Integer id;

    @NotNull(message = "配送员ID不能为空")
    @ApiModelProperty(value = "配送员ID", required = true, example = "1")
    private Integer driverId;

    @NotNull(message = "工作日期不能为空")
    @ApiModelProperty(value = "工作日期", required = true, example = "2024-01-01")
    private Date workDate;

    @ApiModelProperty(value = "上线时间", example = "2024-01-01 09:00:00")
    private Date onlineTime;

    @ApiModelProperty(value = "下线时间", example = "2024-01-01 18:00:00")
    private Date offlineTime;

    @ApiModelProperty(value = "工作时长（小时）", example = "8.5")
    private BigDecimal workHours;

    @ApiModelProperty(value = "配送次数", example = "15")
    private Integer deliveryCount;

    @ApiModelProperty(value = "完成次数", example = "14")
    private Integer completedCount;

    @ApiModelProperty(value = "失败次数", example = "1")
    private Integer failedCount;

    @ApiModelProperty(value = "取消次数", example = "0")
    private Integer cancelledCount;

    @ApiModelProperty(value = "总行驶距离（公里）", example = "125.5")
    private BigDecimal totalDistance;

    @ApiModelProperty(value = "总收入", example = "280.50")
    private BigDecimal totalIncome;

    @ApiModelProperty(value = "佣金收入", example = "250.00")
    private BigDecimal commissionIncome;

    @ApiModelProperty(value = "奖金收入", example = "30.50")
    private BigDecimal bonusIncome;

    @ApiModelProperty(value = "罚款金额", example = "0.00")
    private BigDecimal penaltyAmount;

    @ApiModelProperty(value = "平均评分", example = "4.8")
    private BigDecimal avgRating;

    @ApiModelProperty(value = "最低评分", example = "4.0")
    private BigDecimal minRating;

    @ApiModelProperty(value = "最高评分", example = "5.0")
    private BigDecimal maxRating;

    @ApiModelProperty(value = "异常次数", example = "0")
    private Integer exceptionCount;

    @ApiModelProperty(value = "迟到次数", example = "0")
    private Integer lateCount;

    @ApiModelProperty(value = "平均配送时间（分钟）", example = "35.5")
    private BigDecimal avgDeliveryTime;

    @NotNull(message = "创建时间不能为空")
    @ApiModelProperty(value = "创建时间", required = true, example = "2024-01-01 00:00:00")
    private Date createTime;

    @NotNull(message = "更新时间不能为空")
    @ApiModelProperty(value = "更新时间", required = true, example = "2024-01-01 00:00:00")
    private Date updateTime;
} 