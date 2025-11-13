package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 企业聊天消息请求对象
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "EnterpriseChatMessageRequest对象", description = "企业聊天消息请求")
public class EnterpriseChatMessageRequest {

    @ApiModelProperty(value = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @ApiModelProperty(value = "消息内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    private String content;

    @ApiModelProperty(value = "内容类型", example = "text")
    private String contentType = "text";

    @ApiModelProperty(value = "消息类型", example = "text")
    private String messageType = "text";

    @ApiModelProperty(value = "父消息ID（用于消息链）")
    private String parentMessageId;

    @ApiModelProperty(value = "附件信息（JSON格式）")
    private String attachments;

    @ApiModelProperty(value = "扩展元数据（JSON格式）")
    private String metaData;

    @ApiModelProperty(value = "是否启用上下文记忆")
    private Boolean enableContextMemory = true;

    @ApiModelProperty(value = "是否启用流式响应")
    private Boolean enableStream = true;
    @ApiModelProperty(value = "是否启用历史消息")
    private Boolean  needAiReply ;
    @ApiModelProperty(value = "智能体ID")
    private String cozeBotId;
}
