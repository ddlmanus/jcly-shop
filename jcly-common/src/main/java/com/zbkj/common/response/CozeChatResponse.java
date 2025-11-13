package com.zbkj.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

/**
 * Coze 对话响应（Chat Object）
 */
@Data
public class CozeChatResponse {
    
    /**
     * 对话ID，即对话的唯一标识
     */
    private String id;
    
    /**
     * 会话ID，即会话的唯一标识
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 要进行会话聊天的智能体ID
     */
    @JsonProperty("bot_id")
    private String botId;
    
    /**
     * 对话创建的时间，格式为10位的Unixtime时间戳，单位为秒
     */
    @JsonProperty("created_at")
    private Long createdAt;
    
    /**
     * 对话结束的时间，格式为10位的Unixtime时间戳，单位为秒
     */
    @JsonProperty("completed_at")
    private Long completedAt;
    
    /**
     * 对话失败的时间，格式为10位的Unixtime时间戳，单位为秒
     */
    @JsonProperty("failed_at")
    private Long failedAt;
    
    /**
     * 发起对话时的附加消息
     */
    @JsonProperty("meta_data")
    private Map<String, String> metaData;
    
    /**
     * 对话运行异常时的错误信息
     */
    @JsonProperty("last_error")
    private LastError lastError;
    
    /**
     * 对话的运行状态
     */
    private String status;
    
    /**
     * 需要运行的信息详情
     */
    @JsonProperty("required_action")
    private RequiredAction requiredAction;
    
    /**
     * Token消耗的详细信息
     */
    private Usage usage;
    
    @Data
    public static class LastError {
        /**
         * 错误码
         */
        private Integer code;
        
        /**
         * 错误信息
         */
        private String msg;
    }
    
    @Data
    public static class RequiredAction {
        /**
         * 额外操作的类型
         */
        private String type;
        
        /**
         * 需要提交的结果详情
         */
        @JsonProperty("submit_tool_outputs")
        private SubmitToolOutputs submitToolOutputs;
        
        @Data
        public static class SubmitToolOutputs {
            /**
             * 具体上报信息详情
             */
            @JsonProperty("tool_calls")
            private java.util.List<ToolCall> toolCalls;
            
            @Data
            public static class ToolCall {
                /**
                 * 上报运行结果的ID
                 */
                private String id;
                
                /**
                 * 工具类型
                 */
                private String type;
                
                /**
                 * 执行方法function的定义
                 */
                private Function function;
                
                @Data
                public static class Function {
                    /**
                     * 方法名
                     */
                    private String name;
                    
                    /**
                     * 方法参数
                     */
                    private String arguments;
                }
            }
        }
    }
    
    @Data
    public static class Usage {
        /**
         * 本次对话消耗的Token总数
         */
        @JsonProperty("token_count")
        private Integer tokenCount;
        
        /**
         * output部分消耗的Token总数
         */
        @JsonProperty("output_count")
        private Integer outputCount;
        
        /**
         * input部分消耗的Token总数
         */
        @JsonProperty("input_count")
        private Integer inputCount;
    }
}
