package com.zbkj.common.model.coze.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * Coze Message对象
 * 对应API文档中的Message Object
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "CozeMessageObject", description = "Coze Message对象")
public class CozeMessageObject {
    
    @ApiModelProperty(value = "消息ID")
    private String id;
    
    @ApiModelProperty(value = "会话ID")
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @ApiModelProperty(value = "智能体ID")
    @JsonProperty("bot_id")
    private String botId;
    
    @ApiModelProperty(value = "对话ID")
    @JsonProperty("chat_id")
    private String chatId;
    
    @ApiModelProperty(value = "附加消息")
    @JsonProperty("meta_data")
    private Map<String, String> metaData;
    
    @ApiModelProperty(value = "消息发送角色")
    private String role;
    
    @ApiModelProperty(value = "消息内容")
    private String content;
    
    @ApiModelProperty(value = "消息内容类型")
    @JsonProperty("content_type")
    private String contentType;
    
    @ApiModelProperty(value = "消息创建时间")
    @JsonProperty("created_at")
    private Long createdAt;
    
    @ApiModelProperty(value = "消息更新时间")
    @JsonProperty("updated_at")
    private Long updatedAt;
    
    @ApiModelProperty(value = "消息类型")
    private String type;
    
    @ApiModelProperty(value = "上下文片段ID")
    @JsonProperty("section_id")
    private String sectionId;
    
    @ApiModelProperty(value = "思维链内容")
    @JsonProperty("reasoning_content")
    private String reasoningContent;
    
    /**
     * 角色常量
     */
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    
    /**
     * 消息类型常量
     */
    public static final String TYPE_QUESTION = "question";
    public static final String TYPE_ANSWER = "answer";
    public static final String TYPE_FUNCTION_CALL = "function_call";
    public static final String TYPE_TOOL_RESPONSE = "tool_response";
    public static final String TYPE_FOLLOW_UP = "follow_up";
    public static final String TYPE_VERBOSE = "verbose";
    
    /**
     * 内容类型常量
     */
    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_OBJECT_STRING = "object_string";
    public static final String CONTENT_TYPE_CARD = "card";
    public static final String CONTENT_TYPE_AUDIO = "audio";
}
