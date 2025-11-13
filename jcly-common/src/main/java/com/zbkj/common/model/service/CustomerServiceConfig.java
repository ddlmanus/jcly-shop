package com.zbkj.common.model.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

/**
 * 人工客服配置表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_customer_service_config")
@ApiModel(value = "CustomerServiceConfig对象", description = "人工客服配置表")
public class CustomerServiceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    @TableField("mer_id")
    private Integer merId;

    @ApiModelProperty(value = "客服名称")
    @TableField("service_name")
    private String serviceName;

    @ApiModelProperty(value = "是否启用自动欢迎语")
    @TableField("auto_welcome_enabled")
    private Boolean autoWelcomeEnabled;

    @ApiModelProperty(value = "欢迎语模板")
    @TableField("welcome_message")
    private String welcomeMessage;

    @ApiModelProperty(value = "工作开始时间")
    @TableField("work_hours_start")
    private Time workHoursStart;

    @ApiModelProperty(value = "工作结束时间")
    @TableField("work_hours_end")
    private Time workHoursEnd;

    @ApiModelProperty(value = "工作日：1-7表示周一到周日")
    @TableField("work_days")
    private String workDays;

    @ApiModelProperty(value = "自动转人工关键词，JSON格式")
    @TableField("auto_transfer_keywords")
    private String autoTransferKeywords;

    @ApiModelProperty(value = "最大并发会话数")
    @TableField("max_concurrent_sessions")
    private Integer maxConcurrentSessions;

    @ApiModelProperty(value = "是否启用提示音")
    @TableField("notification_sound_enabled")
    private Boolean notificationSoundEnabled;

    @ApiModelProperty(value = "提示音文件URL")
    @TableField("notification_sound_url")
    private String notificationSoundUrl;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    @TableField("status")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 状态常量
    public static final Boolean STATUS_DISABLED = false;
    public static final Boolean STATUS_ENABLED = true;
}
