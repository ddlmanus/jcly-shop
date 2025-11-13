package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeCreateMessageResponse对象", description = "创建消息响应")
public class CozeCreateMessageResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "消息的详情")
    private MessageData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "MessageData对象", description = "消息信息")
    public static class MessageData {
        @ApiModelProperty(value = "消息的唯一标识")
        private String id;
        
        @ApiModelProperty(value = "此消息所在的会话 ID")
        @JsonProperty("conversation_id")
        private String conversationId;
        
        @ApiModelProperty(value = "编写此消息的智能体 ID")
        @JsonProperty("bot_id")
        private String botId;
        
        @ApiModelProperty(value = "Chat ID")
        @JsonProperty("chat_id")
        private String chatId;
        
        @ApiModelProperty(value = "创建消息时的附加信息")
        @JsonProperty("meta_data")
        private Map<String, String> metaData;
        
        @ApiModelProperty(value = "发送这条消息的实体")
        private String role;
        
        @ApiModelProperty(value = "消息的内容")
        private String content;
        
        @ApiModelProperty(value = "消息内容的类型")
        @JsonProperty("content_type")
        private String contentType;
        
        @ApiModelProperty(value = "消息的创建时间，格式为 10 位的 Unixtime 时间戳")
        @JsonProperty("created_at")
        private Long createdAt;
        
        @ApiModelProperty(value = "消息的更新时间，格式为 10 位的 Unixtime 时间戳")
        @JsonProperty("updated_at")
        private Long updatedAt;
        
        @ApiModelProperty(value = "消息类型")
        private String type;
        
        @ApiModelProperty(value = "上下文片段 ID")
        @JsonProperty("section_id")
        private String sectionId;
        
        @ApiModelProperty(value = "模型的思维链（CoT）")
        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }
}
