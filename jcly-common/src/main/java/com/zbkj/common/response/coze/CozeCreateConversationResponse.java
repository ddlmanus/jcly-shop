package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeCreateConversationResponse对象", description = "创建会话响应")
public class CozeCreateConversationResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "会话的基本信息")
    private ConversationData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "ConversationData对象", description = "会话信息")
    public static class ConversationData {
        @ApiModelProperty(value = "会话的唯一标识")
        private String id;
        
        @ApiModelProperty(value = "会话的创建时间")
        @JsonProperty("created_at")
        private Long createdAt;
        
        @ApiModelProperty(value = "智能体的 ID")
        @JsonProperty("bot_id")
        private String botId;
        
        @ApiModelProperty(value = "会话的名称")
        @JsonProperty("name")
        private String name;
    }
}