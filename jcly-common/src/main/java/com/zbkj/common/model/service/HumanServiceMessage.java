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
 * 人工客服消息表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_human_service_message")
@ApiModel(value = "HumanServiceMessage对象", description = "人工客服消息表")
public class HumanServiceMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "消息唯一标识")
    @TableField("message_id")
    private String messageId;

    @ApiModelProperty(value = "会话ID")
    @TableField("session_id")
    private String sessionId;

    @ApiModelProperty(value = "发送者ID")
    @TableField("sender_id")
    private Long senderId;

    @ApiModelProperty(value = "发送者类型：USER-用户，MERCHANT-客服，SYSTEM-系统")
    @TableField("sender_type")
    private String senderType;

    @ApiModelProperty(value = "接收者ID")
    @TableField("receiver_id")
    private Long receiverId;

    @ApiModelProperty(value = "接收者类型")
    @TableField("receiver_type")
    private String receiverType;

    @ApiModelProperty(value = "消息类型：TEXT-文本，IMAGE-图片，FILE-文件，AUDIO-语音，VIDEO-视频")
    @TableField("message_type")
    private String messageType;

    @ApiModelProperty(value = "消息内容")
    @TableField("content")
    private String content;

    @ApiModelProperty(value = "内容格式：TEXT-纯文本，MARKDOWN-Markdown，HTML-HTML")
    @TableField("content_format")
    private String contentFormat;

    @ApiModelProperty(value = "附件信息，JSON格式")
    @TableField("attachments")
    private String attachments;

    @ApiModelProperty(value = "是否已读")
    @TableField("is_read")
    private Boolean isRead;

    @ApiModelProperty(value = "阅读时间")
    @TableField("read_time")
    private Date readTime;

    @ApiModelProperty(value = "是否系统消息")
    @TableField("is_system_message")
    private Boolean isSystemMessage;

    @ApiModelProperty(value = "关联消息ID（如回复某条消息）")
    @TableField("related_message_id")
    private String relatedMessageId;

    @ApiModelProperty(value = "消息状态：SENDING-发送中，SENT-已发送，DELIVERED-已送达，FAILED-发送失败")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 发送者类型常量
    public static final String SENDER_TYPE_USER = "USER";
    public static final String SENDER_TYPE_MERCHANT = "MERCHANT";
    public static final String SENDER_TYPE_SYSTEM = "SYSTEM";

    // 消息类型常量
    public static final String MESSAGE_TYPE_TEXT = "TEXT";
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";
    public static final String MESSAGE_TYPE_FILE = "FILE";
    public static final String MESSAGE_TYPE_AUDIO = "AUDIO";
    public static final String MESSAGE_TYPE_VIDEO = "VIDEO";

    // 内容格式常量
    public static final String CONTENT_FORMAT_TEXT = "TEXT";
    public static final String CONTENT_FORMAT_MARKDOWN = "MARKDOWN";
    public static final String CONTENT_FORMAT_HTML = "HTML";

    // 消息状态常量
    public static final String STATUS_SENDING = "SENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_FAILED = "FAILED";
}
