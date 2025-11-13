package com.zbkj.common.response.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze语音识别响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeVoiceTranscriptionResponse {
    
    /**
     * 状态码，0代表调用成功
     */
    @JsonProperty("code")
    private Long code;
    
    /**
     * 状态信息
     */
    @JsonProperty("msg")
    private String msg;
    
    /**
     * 音频文件对应的文本内容
     */
    @JsonProperty("data")
    private AudioTranscriptionsData data;
    
    /**
     * 包含请求的详细信息的对象
     */
    @JsonProperty("detail")
    private ResponseDetail detail;
    
    /**
     * 语音识别数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioTranscriptionsData {
        
        /**
         * 语音文件对应的文本内容
         */
        @JsonProperty("text")
        private String text;
    }
    
    /**
     * 响应详细信息类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDetail {
        
        /**
         * 本次请求的日志ID
         */
        @JsonProperty("logid")
        private String logid;
    }
}
