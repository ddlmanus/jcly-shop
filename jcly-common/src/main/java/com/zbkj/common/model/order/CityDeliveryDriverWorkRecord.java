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
@TableName("eb_city_delivery_driver_work_record")
@ApiModel(value = "CityDeliveryDriverWorkRecord", description = "配送员工作记录表")
public class CityDeliveryDriverWorkRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "工作记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "配送员ID", required = true)
    @NotNull(message = "配送员ID不能为空")
    private Integer driverId;

    @ApiModelProperty(value = "配送员姓名")
    private String driverName;

    @ApiModelProperty(value = "工作日期", required = true)
    @NotNull(message = "工作日期不能为空")
    private Date workDate;

    @ApiModelProperty(value = "签到时间")
    private Date checkInTime;

    @ApiModelProperty(value = "签退时间")
    private Date checkOutTime;

    @ApiModelProperty(value = "工作时长（小时）")
    private BigDecimal workHours;

    @ApiModelProperty(value = "接单总数")
    private Integer totalOrders;

    @ApiModelProperty(value = "完成订单数")
    private Integer completedOrders;

    @ApiModelProperty(value = "取消订单数")
    private Integer cancelledOrders;

    @ApiModelProperty(value = "配送总距离（公里）")
    private BigDecimal totalDistance;

    @ApiModelProperty(value = "总收入")
    private BigDecimal totalIncome;

    @ApiModelProperty(value = "基础收入")
    private BigDecimal baseIncome;

    @ApiModelProperty(value = "佣金收入")
    private BigDecimal commissionIncome;

    @ApiModelProperty(value = "奖励收入")
    private BigDecimal bonusIncome;

    @ApiModelProperty(value = "违规次数")
    private Integer violationCount;

    @ApiModelProperty(value = "客户评分总分")
    private BigDecimal totalRating;

    @ApiModelProperty(value = "评价次数")
    private Integer ratingCount;

    @ApiModelProperty(value = "平均评分")
    private BigDecimal averageRating;

    @ApiModelProperty(value = "在线时长（分钟）")
    private Integer onlineMinutes;

    @ApiModelProperty(value = "配送区域")
    private String workArea;

    @ApiModelProperty(value = "最长配送距离（公里）")
    private BigDecimal maxDeliveryDistance;

    @ApiModelProperty(value = "平均配送时间（分钟）")
    private Integer avgDeliveryTime;

    @ApiModelProperty(value = "配送效率评级（A/B/C/D）")
    private String efficiencyRating;

    @ApiModelProperty(value = "异常次数")
    private Integer exceptionCount;

    @ApiModelProperty(value = "超时次数")
    private Integer timeoutCount;

    @ApiModelProperty(value = "客户投诉次数")
    private Integer complaintCount;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否删除（0：未删除；1：已删除）")
    private Integer isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 