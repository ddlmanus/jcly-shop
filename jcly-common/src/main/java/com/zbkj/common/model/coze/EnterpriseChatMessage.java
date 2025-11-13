package com.zbkj.common.model.coze;

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
 * 企业聊天消息表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_enterprise_chat_message")
@ApiModel(value = "EnterpriseChatMessage对象", description = "企业聊天消息表")
public class EnterpriseChatMessage implements Serializable {

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

    @ApiModelProperty(value = "Coze消息ID")
    @TableField("coze_message_id")
    private String cozeMessageId;

    @ApiModelProperty(value = "Coze对话ID")
    @TableField("coze_chat_id")
    private String cozeChatId;

    @ApiModelProperty(value = "父消息ID（用于消息链）")
    @TableField("parent_message_id")
    private String parentMessageId;

    @ApiModelProperty(value = "消息角色：user-用户 assistant-助手 system-系统")
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

    @ApiModelProperty(value = "消息状态")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "错误信息（发送失败时）")
    @TableField("error_message")
    private String errorMessage;

    @ApiModelProperty(value = "消耗的Token数量")
    @TableField("tokens_used")
    private Integer tokensUsed;

    @ApiModelProperty(value = "处理时间（毫秒）")
    @TableField("processing_time")
    private Integer processingTime;

    @ApiModelProperty(value = "附件信息（JSON）")
    @TableField("attachments")
    private String attachments;

    @ApiModelProperty(value = "扩展元数据（JSON）")
    @TableField("meta_data")
    private String metaData;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 消息角色常量
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";

    // 消息类型常量
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";
    public static final String MESSAGE_TYPE_FILE = "file";
    public static final String MESSAGE_TYPE_AUDIO = "audio";
    public static final String MESSAGE_TYPE_CARD = "card";
    public static final String MESSAGE_TYPE_FUNCTION_CALL = "function_call";
    public static final String MESSAGE_TYPE_TOOL_RESPONSE = "tool_response";
    public static final String MESSAGE_TYPE_VERBOSE = "verbose";

    // 内容类型常量
    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_OBJECT_STRING = "object_string";
    public static final String CONTENT_TYPE_CARD = "card";
    public static final String CONTENT_TYPE_AUDIO = "audio";

    // 消息状态常量
    public static final String STATUS_SENDING = "sending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DELETED = "deleted";
}
