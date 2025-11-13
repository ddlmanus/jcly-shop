package com.zbkj.common.request.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze创建房间请求类
 * 用于创建RTC房间并将智能体加入房间
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeCreateRoomRequest {
    
    /**
     * 智能体ID，必选
     */
    @JsonProperty("bot_id")
    private String botId;
    
    /**
     * 会话ID，可选
     * 后续调用发起对话API产生的消息记录都会保存在此对话中
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 智能体使用的音色ID，可选
     * 默认为柔美女友音色
     */
    @JsonProperty("voice_id")
    private String voiceId;
    
    /**
     * 房间内的音视频参数配置，可选
     */
    @JsonProperty("config")
    private RoomConfig config;
    
    /**
     * 标识当前与智能体对话的用户，可选
     * 由使用方自行定义、生成与维护
     */
    @JsonProperty("uid")
    private String uid;
    
    /**
     * 房间配置类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomConfig {
        
        /**
         * 房间模式，默认值为default
         * default: 对话模式
         * s2s: 端到端语音模式
         * podcast: 播客模式
         */
        @JsonProperty("room_mode")
        private String roomMode;
        
        /**
         * 房间音频配置
         */
        @JsonProperty("audio_config")
        private AudioConfig audioConfig;
        
        /**
         * 房间视频配置
         */
        @JsonProperty("video_config")
        private VideoConfig videoConfig;
        
        /**
         * 语音检测配置
         */
        @JsonProperty("turn_detection")
        private TurnDetectionConfig turnDetection;
        
        /**
         * 自定义开场白
         */
        @JsonProperty("prologue_content")
        private String prologueContent;
        
        /**
         * 在进房后等待多长时间播放开场白，单位：ms
         * 默认为500ms，取值范围为0~500
         */
        @JsonProperty("prologue_delay_duration_ms")
        private Integer prologueDelayDurationMs;
    }
    
    /**
     * 音频配置类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioConfig {
        
        /**
         * 房间音频编码格式
         * AACLC: AAC-LC编码格式
         * G711A: G711A编码格式
         * OPUS: (默认)Opus编码格式
         * G722: G.722编码格式
         */
        @JsonProperty("codec")
        private String codec;
    }
    
    /**
     * 视频配置类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoConfig {
        
        /**
         * 房间视频编码格式
         * H264: (默认)H264编码格式
         * BYTEVC1: 火山引擎自研的视频编码格式
         */
        @JsonProperty("codec")
        private String codec;
        
        /**
         * 每秒抽帧数，默认值为1，取值范围为1~24
         */
        @JsonProperty("video_frame_rate")
        private Integer videoFrameRate;
        
        /**
         * 房间视频流类型
         * main: 主流
         * screen: 屏幕流
         */
        @JsonProperty("stream_video_type")
        private String streamVideoType;
        
        /**
         * 用户开始说话前，抽取多少秒画面
         * 单位为秒，默认值为1，取值范围为1~10
         */
        @JsonProperty("video_frame_expire_duration")
        private Integer videoFrameExpireDuration;
    }
    
    /**
     * 语音检测配置类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TurnDetectionConfig {
        
        /**
         * 语音检测类型，默认值为server_vad
         * server_vad: 自由对话模式
         * client_interrupt: 按键说话模式
         */
        @JsonProperty("type")
        private String type;
    }
}
