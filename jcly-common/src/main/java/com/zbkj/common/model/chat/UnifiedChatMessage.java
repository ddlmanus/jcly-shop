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
import org.joda.time.DateTime;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 统一聊天消息表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_unified_chat_message")
@ApiModel(value = "UnifiedChatMessage", description = "统一聊天消息表")
public class UnifiedChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // 发送者类型常量
    public static final String SENDER_TYPE_USER = "USER";
    public static final String SENDER_TYPE_STAFF = "MERCHANT";
    public static final String SENDER_TYPE_MERCHANT = "MERCHANT";
    public static final String SENDER_TYPE_PLATFORM = "PLATFORM";
    public static final String SENDER_TYPE_AI = "AI";
    public static final String SENDER_TYPE_SYSTEM = "SYSTEM";

    // 消息角色常量
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";

    // 消息类型常量
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";
    public static final String MESSAGE_TYPE_FILE = "file";
    public static final String MESSAGE_TYPE_AUDIO = "audio";
    public static final String MESSAGE_TYPE_VIDEO = "video";
    public static final String MESSAGE_TYPE_VOICE = "voice";
    public static final String MESSAGE_TYPE_CARD = "card";
    public static final String MESSAGE_TYPE_FUNCTION_CALL = "function_call";
    public static final String MESSAGE_TYPE_TOOL_RESPONSE = "tool_response";
    
    // 企业级消息类型
    public static final String MESSAGE_TYPE_PRODUCT_CARD = "product_card";
    public static final String MESSAGE_TYPE_ORDER_CARD = "order_card";
    public static final String MESSAGE_TYPE_COUPON_CARD = "coupon_card";
    public static final String MESSAGE_TYPE_ACTIVITY_CARD = "activity_card";
    public static final String MESSAGE_TYPE_QUICK_REPLY = "quick_reply";
    public static final String MESSAGE_TYPE_AI_REPLY = "ai_reply";
    public static final String MESSAGE_TYPE_AI_THINKING = "ai_thinking";
    public static final String MESSAGE_TYPE_AI_SUGGESTION = "ai_suggestion";
    public static final String MESSAGE_TYPE_SYSTEM_NOTICE = "system_notice";
    public static final String MESSAGE_TYPE_TYPING_STATUS = "typing_status";
    public static final String MESSAGE_TYPE_READ_STATUS = "read_status";
    public static final String MESSAGE_TYPE_LOCATION = "location";
    public static final String MESSAGE_TYPE_CONTACT = "contact";
    public static final String MESSAGE_TYPE_LINK = "link";
    public static final String MESSAGE_TYPE_RICH_TEXT = "rich_text";
    
    // 客服转接相关
    public static final String MESSAGE_TYPE_HANDOVER_REQUEST = "handover_request";
    public static final String MESSAGE_TYPE_HANDOVER_ACCEPT = "handover_accept";
    public static final String MESSAGE_TYPE_HANDOVER_COMPLETE = "handover_complete";
    public static final String MESSAGE_TYPE_STAFF_JOIN = "staff_join";
    public static final String MESSAGE_TYPE_STAFF_LEAVE = "staff_leave";

    // 内容类型常量
    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_OBJECT_STRING = "object_string";
    public static final String CONTENT_TYPE_CARD = "card";
    public static final String CONTENT_TYPE_AUDIO = "audio";
    public static final String CONTENT_TYPE_MARKDOWN = "markdown";
    public static final String CONTENT_TYPE_HTML = "html";

    // 消息状态常量
    public static final String STATUS_SENDING = "sending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_READ = "read";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DELETED = "deleted";

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "消息唯一标识")
    @TableField("message_id")
    private String messageId;

    @ApiModelProperty(value = "会话ID")
    @TableField("session_id")
    private String sessionId;

    // 发送者信息
    @ApiModelProperty(value = "发送者ID（用户ID或客服ID）")
    @TableField("sender_id")
    private Long senderId;

    @ApiModelProperty(value = "发送者类型：USER-用户，STAFF-客服，AI-AI助手，SYSTEM-系统")
    @TableField("sender_type")
    private String senderType;

    @ApiModelProperty(value = "发送者名称")
    @TableField("sender_name")
    private String senderName;

    @ApiModelProperty(value = "发送者头像")
    @TableField("sender_avatar")
    private String senderAvatar;

    // 接收者信息
    @ApiModelProperty(value = "接收者ID")
    @TableField("receiver_id")
    private Long receiverId;

    @ApiModelProperty(value = "接收者类型：USER-用户，STAFF-客服，AI-AI助手，SYSTEM-系统")
    @TableField("receiver_type")
    private String receiverType;

    // AI相关字段
    @ApiModelProperty(value = "Coze消息ID")
    @TableField("coze_message_id")
    private String cozeMessageId;

    @ApiModelProperty(value = "Coze对话ID")
    @TableField("coze_chat_id")
    private String cozeChatId;

    @ApiModelProperty(value = "Coze创建时间（时间戳）")
    @TableField("coze_created_at")
    private Long cozeCreatedAt;

    @ApiModelProperty(value = "Coze更新时间（时间戳）")
    @TableField("coze_updated_at")
    private Long cozeUpdatedAt;

    @ApiModelProperty(value = "消耗的Token数量")
    @TableField("tokens_used")
    private Integer tokensUsed;

    @ApiModelProperty(value = "处理时间（毫秒）")
    @TableField("processing_time")
    private Integer processingTime;

    // 消息内容
    @ApiModelProperty(value = "父消息ID（用于消息链）")
    @TableField("parent_message_id")
    private String parentMessageId;

    @ApiModelProperty(value = "消息角色：user-用户，assistant-助手，system-系统")
    @TableField("role")
    private String role;

    @ApiModelProperty(value = "消息类型")
    @TableField("message_type")
    private String messageType;

    @ApiModelProperty(value = "消息内容")
    @TableField("content")
    private String content;

    @ApiModelProperty(value = "内容类型")
    @TableField("content_type")
    private String contentType;

    @ApiModelProperty(value = "原始内容（Coze返回的完整内容）")
    @TableField("raw_content")
    private String rawContent;

    @ApiModelProperty(value = "附件信息（JSON）")
    @TableField("attachments")
    private String attachments;

    // 消息状态
    @ApiModelProperty(value = "消息状态")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "错误信息（发送失败时）")
    @TableField("error_message")
    private String errorMessage;

    @ApiModelProperty(value = "是否已读")
    @TableField("is_read")
    private Boolean isRead;

    @ApiModelProperty(value = "阅读时间")
    @TableField("read_time")
    private LocalDateTime readTime;

    @ApiModelProperty(value = "是否系统消息")
    @TableField("is_system_message")
    private Boolean isSystemMessage;

    @ApiModelProperty(value = "关联消息ID（如回复某条消息）")
    @TableField("related_message_id")
    private String relatedMessageId;

    // 扩展字段
    @ApiModelProperty(value = "扩展元数据（JSON）")
    @TableField("meta_data")
    private String metaData;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;
    @ApiModelProperty(value = "用户是否清空聊天")
    @TableField("is_clear")
    private Boolean isClear;

    /**
     * 是否为用户发送的消息
     */
    public boolean isUserMessage() {
        return SENDER_TYPE_USER.equals(senderType) || ROLE_USER.equals(role);
    }

    /**
     * 是否为AI发送的消息
     */
    public boolean isAiMessage() {
        return SENDER_TYPE_AI.equals(senderType) || ROLE_ASSISTANT.equals(role);
    }

    /**
     * 是否为客服发送的消息
     */
    public boolean isStaffMessage() {
        return SENDER_TYPE_MERCHANT.equals(senderType);
    }

    /**
     * 是否为系统消息
     */
    public boolean isSystemMessage() {
        return SENDER_TYPE_SYSTEM.equals(senderType) || ROLE_SYSTEM.equals(role) || Boolean.TRUE.equals(isSystemMessage);
    }

    /**
     * 是否为文本消息
     */
    public boolean isTextMessage() {
        return MESSAGE_TYPE_TEXT.equals(messageType);
    }

    /**
     * 是否为图片消息
     */
    public boolean isImageMessage() {
        return MESSAGE_TYPE_IMAGE.equals(messageType);
    }

    /**
     * 是否为文件消息
     */
    public boolean isFileMessage() {
        return MESSAGE_TYPE_FILE.equals(messageType);
    }

    /**
     * 是否发送成功
     */
    public boolean isSent() {
        return STATUS_SENT.equals(status) || STATUS_DELIVERED.equals(status) || STATUS_READ.equals(status);
    }

    /**
     * 是否发送失败
     */
    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    /**
     * 是否正在发送
     */
    public boolean isSending() {
        return STATUS_SENDING.equals(status);
    }

    /**
     * 是否已读
     */
    public boolean isMessageRead() {
        return STATUS_READ.equals(status) || Boolean.TRUE.equals(isRead);
    }

    /**
     * 是否为语音消息
     */
    public boolean isVoiceMessage() {
        return MESSAGE_TYPE_VOICE.equals(messageType) || MESSAGE_TYPE_AUDIO.equals(messageType);
    }

    /**
     * 是否为视频消息
     */
    public boolean isVideoMessage() {
        return MESSAGE_TYPE_VIDEO.equals(messageType);
    }

    /**
     * 是否为商品卡片消息
     */
    public boolean isProductCardMessage() {
        return MESSAGE_TYPE_PRODUCT_CARD.equals(messageType);
    }

    /**
     * 是否为订单卡片消息
     */
    public boolean isOrderCardMessage() {
        return MESSAGE_TYPE_ORDER_CARD.equals(messageType);
    }

    /**
     * 是否为快捷回复消息
     */
    public boolean isQuickReplyMessage() {
        return MESSAGE_TYPE_QUICK_REPLY.equals(messageType);
    }

    /**
     * 是否为AI回复消息
     */
    public boolean isAiReplyMessage() {
        return MESSAGE_TYPE_AI_REPLY.equals(messageType) || isAiMessage();
    }

    /**
     * 是否为转人工相关消息
     */
    public boolean isHandoverMessage() {
        return MESSAGE_TYPE_HANDOVER_REQUEST.equals(messageType) ||
               MESSAGE_TYPE_HANDOVER_ACCEPT.equals(messageType) ||
               MESSAGE_TYPE_HANDOVER_COMPLETE.equals(messageType) ||
               MESSAGE_TYPE_STAFF_JOIN.equals(messageType) ||
               MESSAGE_TYPE_STAFF_LEAVE.equals(messageType);
    }

    /**
     * 是否为富媒体消息（图片、视频、音频、文件等）
     */
    public boolean isRichMediaMessage() {
        return isImageMessage() || isVideoMessage() || isVoiceMessage() || isFileMessage();
    }

    /**
     * 是否为卡片类消息
     */
    public boolean isCardMessage() {
        return MESSAGE_TYPE_CARD.equals(messageType) ||
               MESSAGE_TYPE_PRODUCT_CARD.equals(messageType) ||
               MESSAGE_TYPE_ORDER_CARD.equals(messageType) ||
               MESSAGE_TYPE_COUPON_CARD.equals(messageType) ||
               MESSAGE_TYPE_ACTIVITY_CARD.equals(messageType);
    }

    /**
     * 是否需要客服处理的消息
     */
    public boolean needsStaffAttention() {
        return isHandoverMessage() || 
               (isUserMessage() && !isAiReplyMessage()) ||
               MESSAGE_TYPE_SYSTEM_NOTICE.equals(messageType);
    }
}
