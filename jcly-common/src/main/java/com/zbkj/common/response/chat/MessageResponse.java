package com.zbkj.common.response.chat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 消息响应
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@ApiModel(value = "MessageResponse", description = "消息响应")
public class MessageResponse {

    @ApiModelProperty(value = "消息ID")
    private String messageId;

    @ApiModelProperty(value = "会话ID")
    private String sessionId;

    @ApiModelProperty(value = "发送者ID")
    private Long senderId;

    @ApiModelProperty(value = "发送者类型")
    private String senderType;

    @ApiModelProperty(value = "发送者名称")
    private String senderName;

    @ApiModelProperty(value = "发送者头像")
    private String senderAvatar;
    @ApiModelProperty(value = "消耗的Token数量")
    @TableField("tokens_used")
    private Integer tokensUsed;

    @ApiModelProperty(value = "接收者ID")
    private Long receiverId;

    @ApiModelProperty(value = "接收者类型")
    private String receiverType;

    @ApiModelProperty(value = "消息角色")
    private String role;

    @ApiModelProperty(value = "消息类型")
    private String messageType;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "内容类型")
    private String contentType;

    @ApiModelProperty(value = "附件信息")
    private String attachments;

    @ApiModelProperty(value = "消息状态")
    private String status;

    @ApiModelProperty(value = "是否已读")
    private Boolean isRead;

    @ApiModelProperty(value = "是否系统消息")
    private Boolean isSystemMessage;

    @ApiModelProperty(value = "关联消息ID")
    private String relatedMessageId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 从UnifiedChatMessage转换
     */
    public static MessageResponse fromMessage(UnifiedChatMessage message) {
        MessageResponse response = new MessageResponse();
        response.setMessageId(message.getMessageId());
        response.setSessionId(message.getSessionId());
        response.setSenderId(message.getSenderId());
        response.setSenderType(message.getSenderType());
        response.setSenderName(message.getSenderName());
        response.setSenderAvatar(message.getSenderAvatar());
        response.setReceiverId(message.getReceiverId());
        response.setReceiverType(message.getReceiverType());
        response.setRole(message.getRole());
        response.setMessageType(message.getMessageType());
        response.setContent(message.getContent());
        response.setContentType(message.getContentType());
        response.setAttachments(message.getAttachments());
        response.setStatus(message.getStatus());
        response.setIsRead(message.getIsRead());
        response.setIsSystemMessage(message.getIsSystemMessage());
        response.setRelatedMessageId(message.getRelatedMessageId());
        response.setCreateTime(message.getCreateTime());
        response.setUpdateTime(message.getUpdateTime());
        response.setTokensUsed(message.getTokensUsed());
        return response;
    }

    /**
     * 是否为用户发送的消息
     */
    public boolean isUserMessage() {
        return UnifiedChatMessage.SENDER_TYPE_USER.equals(senderType) || UnifiedChatMessage.ROLE_USER.equals(role);
    }

    /**
     * 是否为AI发送的消息
     */
    public boolean isAiMessage() {
        return UnifiedChatMessage.SENDER_TYPE_AI.equals(senderType) || UnifiedChatMessage.ROLE_ASSISTANT.equals(role);
    }

    /**
     * 是否为客服发送的消息
     */
    public boolean isStaffMessage() {
        return UnifiedChatMessage.SENDER_TYPE_MERCHANT.equals(senderType);
    }

    /**
     * 是否发送成功
     */
    public boolean isSent() {
        return UnifiedChatMessage.STATUS_SENT.equals(status) || 
               UnifiedChatMessage.STATUS_DELIVERED.equals(status) || 
               UnifiedChatMessage.STATUS_READ.equals(status);
    }
}
