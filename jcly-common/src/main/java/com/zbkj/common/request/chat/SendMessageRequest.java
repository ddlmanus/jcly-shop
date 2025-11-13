package com.zbkj.common.request.chat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送消息请求
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@ApiModel(value = "SendMessageRequest", description = "发送消息请求")
public class SendMessageRequest {

    @ApiModelProperty(value = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    @ApiModelProperty(value = "发送者ID", required = true)
    @NotNull(message = "发送者ID不能为空")
    private Long senderId;
    @ApiModelProperty(value = "发送者类型：USER-用户，STAFF-客服，AI-AI助手")
    @NotBlank(message = "发送者类型不能为空")
    private String senderType;

    @ApiModelProperty(value = "消息内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @ApiModelProperty(value = "消息类型：text-文本，image-图片，file-文件，audio-语音，video-视频", required = true)
    @NotBlank(message = "消息类型不能为空")
    private String messageType = "text";

    @ApiModelProperty(value = "内容类型：text-纯文本，markdown-Markdown，html-HTML")
    private String contentType = "text";

    @ApiModelProperty(value = "接收者ID（用于人工客服）")
    private Long receiverId;

    @ApiModelProperty(value = "接收者类型：USER-用户，MERCHANT-客服，AI-AI助手")
    private String receiverType;

    @ApiModelProperty(value = "附件信息（JSON格式）")
    private String attachments;

    @ApiModelProperty(value = "关联消息ID（回复消息时使用）")
    private String relatedMessageId;

    @ApiModelProperty(value = "扩展元数据（JSON格式）")
    private String metaData;

    @ApiModelProperty(value = "是否需要AI回复", notes = "仅在AI会话中有效")
    private Boolean needAiReply = true;

    @ApiModelProperty(value = "是否转人工", notes = "当前为AI服务时，可以申请转人工")
    private Boolean requestHumanService = false;

    @ApiModelProperty(value = "转人工原因")
    private String transferReason;

    /**
     * 是否为文本消息
     */
    public boolean isTextMessage() {
        return "text".equals(messageType);
    }

    /**
     * 是否为图片消息
     */
    public boolean isImageMessage() {
        return "image".equals(messageType);
    }

    /**
     * 是否为文件消息
     */
    public boolean isFileMessage() {
        return "file".equals(messageType);
    }

    /**
     * 是否需要转人工
     */
    public boolean needTransferToHuman() {
        return Boolean.TRUE.equals(requestHumanService);
    }
}
