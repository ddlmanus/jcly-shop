package com.zbkj.common.response.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze创建房间响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeCreateRoomResponse {
    
    /**
     * 状态码，0代表调用成功
     */
    @JsonProperty("code")
    private Long code;
    
    /**
     * 接口返回的业务数据
     */
    @JsonProperty("data")
    private CreateRoomData data;
    
    /**
     * 状态信息
     */
    @JsonProperty("msg")
    private String msg;
    
    /**
     * 本次请求的详细信息
     */
    @JsonProperty("detail")
    private ResponseDetail detail;
    
    /**
     * 创建房间数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoomData {
        
        /**
         * 进入房间的用户ID
         */
        @JsonProperty("uid")
        private String uid;
        
        /**
         * 房间密钥，用户加入RTC房间时需要通过token进行身份认证和鉴权
         */
        @JsonProperty("token")
        private String token;
        
        /**
         * RTC应用ID
         */
        @JsonProperty("app_id")
        private String appId;
        
        /**
         * 已创建的RTC房间的房间ID
         */
        @JsonProperty("room_id")
        private String roomId;
        
        /**
         * Coze Access Token，用于连接Coze Realtime WebSocket
         */
        @JsonProperty("access_token")
        private String accessToken;
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
