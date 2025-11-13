package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 人工客服消息请求
 * @author AI Assistant  
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "HumanServiceMessageRequest对象", description = "人工客服消息请求")
public class HumanServiceMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    @ApiModelProperty(value = "发送者ID", required = true)
    @NotNull(message = "发送者ID不能为空")
    private Integer senderId;
    @ApiModelProperty(value = "发送者类型", required = true)
    @NotBlank(message = "发送者类型不能为空")
    private String senderType;

    @ApiModelProperty(value = "接收者ID", required = true)
    @NotNull(message = "接收者ID不能为空")
    private Integer receiverId;

    @ApiModelProperty(value = "接收者类型", required = true)
    @NotBlank(message = "接收者类型不能为空")
    private String receiverType;

    @ApiModelProperty(value = "消息类型：TEXT-文本，IMAGE-图片，FILE-文件，AUDIO-语音，VIDEO-视频")
    private String messageType = "TEXT";

    @ApiModelProperty(value = "消息内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @ApiModelProperty(value = "内容格式：TEXT-纯文本，MARKDOWN-Markdown，HTML-HTML")
    private String contentFormat = "TEXT";

    @ApiModelProperty(value = "附件信息，JSON格式")
    private String attachments;

    @ApiModelProperty(value = "关联消息ID（如回复某条消息）")
    private String relatedMessageId;
}
