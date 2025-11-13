package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 取消进行中的对话请求参数
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Data
@ApiModel(value = "CozeCancelChatRequest", description = "取消进行中的对话请求参数")
public class CozeCancelChatRequest {

    @ApiModelProperty(value = "对话ID", required = true)
    @NotBlank(message = "对话ID不能为空")
    @JsonProperty("chat_id")
    private String chatId;

    @ApiModelProperty(value = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    @JsonProperty("conversation_id")
    private String conversationId;
}
