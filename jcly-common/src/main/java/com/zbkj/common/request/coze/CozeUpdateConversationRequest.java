package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeUpdateConversationRequest对象", description = "更新会话名称请求参数")
public class CozeUpdateConversationRequest {
    
    @ApiModelProperty(value = "会话的唯一标识", required = true)
    @JsonProperty("conversation_id")
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;
    
    @ApiModelProperty(value = "会话的新名称", required = true)
    @JsonProperty("conversation_name")
    @NotBlank(message = "会话名称不能为空")
    private String conversationName;
}
