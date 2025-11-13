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
@ApiModel(value = "CozeUpdateConversationResponse对象", description = "更新会话名称响应")
public class CozeUpdateConversationResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "更新后的会话信息")
    private ConversationUpdateData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "ConversationUpdateData对象", description = "更新会话信息")
    public static class ConversationUpdateData {
        @ApiModelProperty(value = "会话的唯一标识")
        private String id;
        
        @ApiModelProperty(value = "更新后的会话名称")
        @JsonProperty("conversation_name")
        private String conversationName;
        
        @ApiModelProperty(value = "会话的最后修改时间")
        @JsonProperty("updated_at")
        private Long updatedAt;
    }
}
