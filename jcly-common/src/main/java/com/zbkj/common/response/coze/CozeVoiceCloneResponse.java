package com.zbkj.common.response.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze复刻音色响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeVoiceCloneResponse {
    
    /**
     * 状态码，0代表调用成功
     */
    @JsonProperty("code")
    private Long code;
    
    /**
     * 新音色的详细信息
     */
    @JsonProperty("data")
    private CloneVoiceData data;
    
    /**
     * 状态信息
     */
    @JsonProperty("msg")
    private String msg;
    
    /**
     * 包含请求的详细信息的对象
     */
    @JsonProperty("detail")
    private ResponseDetail detail;
    
    /**
     * 复刻音色数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CloneVoiceData {
        
        /**
         * 复刻后的音色ID
         * 后续语音生成或重新克隆音色时需要传入该音色ID
         */
        @JsonProperty("voice_id")
        private String voiceId;
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
