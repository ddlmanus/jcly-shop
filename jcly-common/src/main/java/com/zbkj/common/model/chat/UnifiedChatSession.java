package com.zbkj.common.model.chat;

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
 * 统一聊天会话表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_unified_chat_session")
@ApiModel(value = "UnifiedChatSession", description = "统一聊天会话表")
public class UnifiedChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    // 会话类型常量
    public static final String SESSION_TYPE_AI = "AI";
    public static final String SESSION_TYPE_HUMAN = "HUMAN";
    public static final String SESSION_TYPE_MIXED = "MIXED";

    // 当前服务类型常量
    public static final String SERVICE_TYPE_AI = "AI";
    public static final String SERVICE_TYPE_HUMAN = "HUMAN";

    // 用户类型常量
    public static final String USER_TYPE_CUSTOMER = "USER";
    public static final String USER_TYPE_MERCHANT = "MERCHANT";
    public static final String USER_TYPE_PLATFORM = "PLATFORM";

    // 会话状态常量
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_ENDED = "ENDED";
    public static final String STATUS_CLOSED = "CLOSED";

    // 优先级常量
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "会话唯一标识")
    @TableField("session_id")
    private String sessionId;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "用户类型：USER-客户，MERCHANT-商户，PLATFORM-平台")
    @TableField("user_type")
    private String userType;

    @ApiModelProperty(value = "商户ID")
    @TableField("mer_id")
    private Long merId;

    @ApiModelProperty(value = "会话类型：AI-AI对话，HUMAN-人工客服，MIXED-混合模式")
    @TableField("session_type")
    private String sessionType;

    @ApiModelProperty(value = "当前服务类型：AI-AI服务中，HUMAN-人工服务中")
    @TableField("current_service_type")
    private String currentServiceType;

    // AI相关字段
    @ApiModelProperty(value = "Coze智能体ID")
    @TableField("coze_bot_id")
    private String cozeBotId;

    @ApiModelProperty(value = "Coze会话ID")
    @TableField("coze_conversation_id")
    private String cozeConversationId;

    // 人工客服相关字段
    @ApiModelProperty(value = "分配的客服ID")
    @TableField("staff_id")
    private Long staffId;

    @ApiModelProperty(value = "转人工原因")
    @TableField("transfer_reason")
    private String transferReason;

    @ApiModelProperty(value = "优先级：LOW-低，NORMAL-普通，HIGH-高，URGENT-紧急")
    @TableField("priority")
    private String priority;
    @ApiModelProperty(value = "服务模式：HUMAN-人工服务模式，HUMAN_ASSIST-人工转接模式")
    private String serviceMode;

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

    // 通用字段
    @ApiModelProperty(value = "会话标题")
    @TableField("session_title")
    private String sessionTitle;

    @ApiModelProperty(value = "会话状态：ACTIVE-活跃，WAITING-等待，ENDED-已结束，CLOSED-已关闭")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "总消息数量")
    @TableField("total_messages")
    private Integer totalMessages;

    @ApiModelProperty(value = "最后消息时间")
    @TableField("last_message_time")
    private Date lastMessageTime;

    @ApiModelProperty(value = "最后一条消息内容")
    @TableField("last_message_content")
    private String lastMessageContent;

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

    @ApiModelProperty(value = "会话上下文信息（JSON）")
    @TableField("session_context")
    private String sessionContext;

    @ApiModelProperty(value = "扩展元数据（JSON）")
    @TableField("meta_data")
    private String metaData;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    /**
     * 是否为AI会话
     */
    public boolean isAiSession() {
        return SESSION_TYPE_AI.equals(sessionType);
    }

    /**
     * 是否为人工客服会话
     */
    public boolean isHumanSession() {
        return SESSION_TYPE_HUMAN.equals(sessionType);
    }

    /**
     * 是否为混合模式会话
     */
    public boolean isMixedSession() {
        return SESSION_TYPE_MIXED.equals(sessionType);
    }

    /**
     * 是否当前为AI服务
     */
    public boolean isCurrentlyAiService() {
        return SERVICE_TYPE_AI.equals(currentServiceType);
    }

    /**
     * 是否当前为人工服务
     */
    public boolean isCurrentlyHumanService() {
        return SERVICE_TYPE_HUMAN.equals(currentServiceType);
    }

    /**
     * 是否为活跃会话
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    /**
     * 是否为等待状态
     */
    public boolean isWaiting() {
        return STATUS_WAITING.equals(status);
    }

    /**
     * 是否已结束
     */
    public boolean isEnded() {
        return STATUS_ENDED.equals(status) || STATUS_CLOSED.equals(status);
    }
}
