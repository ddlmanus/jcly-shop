package com.zbkj.common.model.coze.stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Coze流式响应事件
 * 根据Coze API文档定义的事件类型
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@ApiModel(value = "CozeStreamEvent", description = "Coze流式响应事件")
public class CozeStreamEvent {
    
    @ApiModelProperty(value = "事件类型")
    private String event;
    
    @ApiModelProperty(value = "事件数据")
    private Object data;
    
    /**
     * 事件类型常量
     */
    public static final String EVENT_CHAT_CREATED = "conversation.chat.created";
    public static final String EVENT_CHAT_IN_PROGRESS = "conversation.chat.in_progress";
    public static final String EVENT_MESSAGE_DELTA = "conversation.message.delta";
    public static final String EVENT_AUDIO_DELTA = "conversation.audio.delta";
    public static final String EVENT_MESSAGE_COMPLETED = "conversation.message.completed";
    public static final String EVENT_CHAT_COMPLETED = "conversation.chat.completed";
    public static final String EVENT_CHAT_FAILED = "conversation.chat.failed";
    public static final String EVENT_CHAT_REQUIRES_ACTION = "conversation.chat.requires_action";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_DONE = "done";
}
