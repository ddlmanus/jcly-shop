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
import java.util.Date;

/**
 * 配送通知记录表
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
@TableName("eb_city_delivery_notification")
@ApiModel(value = "CityDeliveryNotification", description = "配送通知记录实体类")
public class CityDeliveryNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "通知ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "配送订单号", example = "D202512010001")
    private String deliveryOrderNo;

    @NotNull(message = "接收者类型不能为空")
    @ApiModelProperty(value = "接收者类型：1-用户，2-商户，3-配送员", required = true, example = "1")
    private Integer recipientType;

    @NotNull(message = "接收者ID不能为空")
    @ApiModelProperty(value = "接收者ID", required = true, example = "1")
    private Integer recipientId;

    @NotNull(message = "通知类型不能为空")
    @ApiModelProperty(value = "通知类型：1-派单，2-接单，3-取件，4-配送中，5-已送达，6-异常", required = true, example = "1")
    private Integer notificationType;

    @NotBlank(message = "通知标题不能为空")
    @ApiModelProperty(value = "通知标题", required = true, example = "配送员已接单")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @ApiModelProperty(value = "通知内容", required = true, example = "您的订单已被配送员接单，正在前往取件")
    private String content;

    @ApiModelProperty(value = "推送方式：1-短信，2-APP推送，3-微信推送", example = "2")
    private Integer pushMethod;

    @ApiModelProperty(value = "推送状态：0-未发送，1-发送中，2-发送成功，3-发送失败", example = "2")
    private Integer pushStatus;

    @ApiModelProperty(value = "推送时间", example = "2024-01-01 10:00:00")
    private Date pushTime;

    @ApiModelProperty(value = "推送响应", example = "success")
    private String pushResponse;

    @ApiModelProperty(value = "阅读状态：0-未读，1-已读", example = "0")
    private Integer readStatus;

    @ApiModelProperty(value = "阅读时间", example = "2024-01-01 10:05:00")
    private Date readTime;

    @ApiModelProperty(value = "额外数据（JSON格式）", example = "{\"driverName\":\"张三\",\"driverPhone\":\"13800138000\"}")
    private String extraData;

    @NotNull(message = "创建时间不能为空")
    @ApiModelProperty(value = "创建时间", required = true, example = "2024-01-01 10:00:00")
    private Date createTime;

    @NotNull(message = "更新时间不能为空")
    @ApiModelProperty(value = "更新时间", required = true, example = "2024-01-01 10:00:00")
    private Date updateTime;
} 