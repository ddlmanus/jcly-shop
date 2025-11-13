package com.zbkj.common.request.coze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Coze聊天请求参数
 * 用于小程序端与智能体聊天
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@ApiModel(value = "CozeChatRequest", description = "Coze聊天请求参数")
public class CozeChatRequest {

    @ApiModelProperty(value = "智能体ID", required = true)
    @NotBlank(message = "智能体ID不能为空")
    private String botId;

    @ApiModelProperty(value = "消息内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String message;

    @ApiModelProperty(value = "会话ID（可选，用于继续对话）")
    private String conversationId;

    @ApiModelProperty(value = "本地会话ID（可选，用于继续对话）")
    private Integer sessionId;

    @ApiModelProperty(value = "消息类型：text（默认）")
    private String messageType = "text";
}
