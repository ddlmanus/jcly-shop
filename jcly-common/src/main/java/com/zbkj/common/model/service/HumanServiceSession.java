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
import java.util.Date;

/**
 * 人工客服会话表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_human_service_session")
@ApiModel(value = "HumanServiceSession对象", description = "人工客服会话表")
public class HumanServiceSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "会话唯一标识")
    @TableField("session_id")
    private String sessionId;

    @ApiModelProperty(value = "关联的企业聊天会话ID")
    @TableField("enterprise_session_id")
    private String enterpriseSessionId;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "用户类型：USER-客户，MERCHANT-商户，PLATFORM-平台")
    @TableField("user_type")
    private String userType;

    @ApiModelProperty(value = "分配的客服ID")
    @TableField("staff_id")
    private Long staffId;

    @ApiModelProperty(value = "商户ID")
    @TableField("mer_id")
    private Long merId;

    @ApiModelProperty(value = "会话类型：DIRECT-直接，TRANSFER-转接")
    @TableField("session_type")
    private String sessionType;

    @ApiModelProperty(value = "转接原因")
    @TableField("transfer_reason")
    private String transferReason;

    @ApiModelProperty(value = "会话状态：WAITING-等待，ACTIVE-进行中，ENDED-已结束，CLOSED-已关闭")
    @TableField("session_status")
    private String sessionStatus;

    @ApiModelProperty(value = "优先级：LOW-低，NORMAL-普通，HIGH-高，URGENT-紧急")
    @TableField("priority")
    private String priority;

    @ApiModelProperty(value = "排队位置")
    @TableField("queue_position")
    private Integer queuePosition;

    @ApiModelProperty(value = "等待开始时间")
    @TableField("wait_start_time")
    private Date waitStartTime;

    @ApiModelProperty(value = "服务开始时间")
    @TableField("service_start_time")
    private Date serviceStartTime;

    @ApiModelProperty(value = "服务结束时间")
    @TableField("service_end_time")
    private Date serviceEndTime;

    @ApiModelProperty(value = "总等待时间（秒）")
    @TableField("total_wait_time")
    private Integer totalWaitTime;

    @ApiModelProperty(value = "总服务时间（秒）")
    @TableField("total_service_time")
    private Integer totalServiceTime;

    @ApiModelProperty(value = "用户满意度评分（1-5）")
    @TableField("user_satisfaction")
    private Integer userSatisfaction;

    @ApiModelProperty(value = "用户反馈内容")
    @TableField("feedback_content")
    private String feedbackContent;

    @ApiModelProperty(value = "会话总结")
    @TableField("session_summary")
    private String sessionSummary;

    @ApiModelProperty(value = "会话标签")
    @TableField("tags")
    private String tags;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 用户类型常量
    public static final String USER_TYPE_CUSTOMER = "USER";
    public static final String USER_TYPE_MERCHANT = "MERCHANT";
    public static final String USER_TYPE_PLATFORM = "PLATFORM";

    // 会话类型常量
    public static final String SESSION_TYPE_DIRECT = "DIRECT";
    public static final String SESSION_TYPE_TRANSFER = "TRANSFER";

    // 会话状态常量
    public static final String SESSION_STATUS_WAITING = "WAITING";
    public static final String SESSION_STATUS_ACTIVE = "ACTIVE";
    public static final String SESSION_STATUS_ENDED = "ENDED";
    public static final String SESSION_STATUS_CLOSED = "CLOSED";

    // 优先级常量
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";
}
