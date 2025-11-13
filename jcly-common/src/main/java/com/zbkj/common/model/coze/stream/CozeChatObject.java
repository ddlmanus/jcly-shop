package com.zbkj.common.model.coze.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * Coze Chat对象
 * 对应API文档中的Chat Object
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "CozeChatObject", description = "Coze Chat对象")
public class CozeChatObject {
    
    @ApiModelProperty(value = "对话ID")
    private String id;
    
    @ApiModelProperty(value = "会话ID")
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @ApiModelProperty(value = "智能体ID")
    @JsonProperty("bot_id")
    private String botId;
    
    @ApiModelProperty(value = "对话创建时间")
    @JsonProperty("created_at")
    private Long createdAt;
    
    @ApiModelProperty(value = "对话结束时间")
    @JsonProperty("completed_at")
    private Long completedAt;
    
    @ApiModelProperty(value = "对话失败时间")
    @JsonProperty("failed_at")
    private Long failedAt;
    
    @ApiModelProperty(value = "附加消息")
    @JsonProperty("meta_data")
    private Map<String, String> metaData;
    
    @ApiModelProperty(value = "错误信息")
    @JsonProperty("last_error")
    private LastError lastError;
    
    @ApiModelProperty(value = "对话状态")
    private String status;
    
    @ApiModelProperty(value = "需要的操作")
    @JsonProperty("required_action")
    private RequiredAction requiredAction;
    
    @ApiModelProperty(value = "Token使用情况")
    private Usage usage;
    
    @ApiModelProperty(value = "上下文片段ID")
    @JsonProperty("section_id")
    private String sectionId;
    
    /**
     * 状态常量
     */
    public static final String STATUS_CREATED = "created";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_REQUIRES_ACTION = "requires_action";
    public static final String STATUS_CANCELED = "canceled";
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LastError {
        @ApiModelProperty(value = "错误码")
        private Integer code;
        
        @ApiModelProperty(value = "错误信息")
        private String msg;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequiredAction {
        @ApiModelProperty(value = "操作类型")
        private String type;
        
        @ApiModelProperty(value = "需要提交的工具输出")
        @JsonProperty("submit_tool_outputs")
        private SubmitToolOutputs submitToolOutputs;
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SubmitToolOutputs {
            @ApiModelProperty(value = "工具调用信息")
            @JsonProperty("tool_calls")
            private Object[] toolCalls;
        }
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @ApiModelProperty(value = "总Token数")
        @JsonProperty("token_count")
        private Integer tokenCount;
        
        @ApiModelProperty(value = "输出Token数")
        @JsonProperty("output_count")
        private Integer outputCount;
        
        @ApiModelProperty(value = "输入Token数")
        @JsonProperty("input_count")
        private Integer inputCount;
        
        @ApiModelProperty(value = "输入Token详细信息")
        @JsonProperty("input_tokens_details")
        private InputTokensDetails inputTokensDetails;
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class InputTokensDetails {
            @ApiModelProperty(value = "缓存Token数")
            @JsonProperty("cached_tokens")
            private Integer cachedTokens;
        }
    }
}
