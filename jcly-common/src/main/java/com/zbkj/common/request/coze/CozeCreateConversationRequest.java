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
@ApiModel(value = "CozeCreateConversationRequest对象", description = "创建会话请求参数")
public class CozeCreateConversationRequest {
    
    @ApiModelProperty(value = "智能体的 ID", required = true)
    @JsonProperty("bot_id")
    @NotBlank(message = "智能体ID不能为空")
    private String botId;
    
    @ApiModelProperty(value = "会话的名称")
    @JsonProperty("name")
    private String conversationName;
//
//    @ApiModelProperty(value = "会话的描述")
//    private String description;
    @ApiModelProperty(value = "渠道创建")
    @JsonProperty("connector_id")
    private String connectorId;

}