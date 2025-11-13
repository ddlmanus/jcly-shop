package com.zbkj.common.response.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Coze查看音色列表响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeVoiceListResponse {
    
    /**
     * 状态码，0代表调用成功
     */
    @JsonProperty("code")
    private Long code;
    
    /**
     * 音色的详细信息
     */
    @JsonProperty("data")
    private ListVoiceData data;
    
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
     * 音色列表数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListVoiceData {
        
        /**
         * 标识是否还有未返回的音色数据
         * true: 当前返回的音色列表未包含所有符合条件的音色
         * false: 表示已返回所有符合条件的音色数据
         */
        @JsonProperty("has_more")
        private Boolean hasMore;
        
        /**
         * 音色列表详情
         */
        @JsonProperty("voice_list")
        private List<OpenAPIVoiceData> voiceList;
    }
    
    /**
     * 音色数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAPIVoiceData {
        
        /**
         * 音色的名称
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * 音色克隆状态
         * init: 待克隆
         * cloned: 已克隆
         */
        @JsonProperty("state")
        private String state;
        
        /**
         * 音色的ID
         */
        @JsonProperty("voice_id")
        private String voiceId;
        
        /**
         * 音色模型的类型
         * big: 大模型
         * small: 小模型
         */
        @JsonProperty("model_type")
        private String modelType;
        
        /**
         * 音色的创建时间，格式为11位的Unixtime时间戳
         */
        @JsonProperty("create_time")
        private Integer createTime;
        
        /**
         * 音色的更新时间，格式为11位的Unixtime时间戳
         */
        @JsonProperty("update_time")
        private Integer updateTime;
        
        /**
         * 此音色预览音频对应的文案
         */
        @JsonProperty("preview_text")
        private String previewText;
        
        /**
         * 此音色的语种代号
         */
        @JsonProperty("language_code")
        private String languageCode;
        
        /**
         * 此音色的语种名称
         */
        @JsonProperty("language_name")
        private String languageName;
        
        /**
         * 此音色的预览音频
         * 通常是一个公开可访问的网络地址
         */
        @JsonProperty("preview_audio")
        private String previewAudio;
        
        /**
         * 标识当前音色是否为系统预置音色
         * true: 系统预置音色
         * false: 用户自定义音色
         */
        @JsonProperty("is_system_voice")
        private Boolean isSystemVoice;
        
        /**
         * 音色支持的情感类型列表，仅当音色为多情感音色时返回
         */
        @JsonProperty("support_emotions")
        private List<EmotionInfo> supportEmotions;
        
        /**
         * 当前音色还可训练的次数
         * 包括首次复刻音色在内，每个自定义音色最多被训练10次
         */
        @JsonProperty("available_training_times")
        private Integer availableTrainingTimes;
    }
    
    /**
     * 情感信息类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionInfo {
        
        /**
         * 音色支持的情感类型标识符
         * happy: 开心, sad: 悲伤, angry: 愤怒, surprised: 惊讶,
         * fear: 恐惧, hate: 厌恶, excited: 兴奋, coldness: 冷漠, neutral: 中性
         */
        @JsonProperty("emotion")
        private String emotion;
        
        /**
         * 音色支持的情感类型的中文显示名称
         */
        @JsonProperty("display_name")
        private String displayName;
        
        /**
         * 情感强度的取值范围
         */
        @JsonProperty("emotion_scale_interval")
        private Interval emotionScaleInterval;
    }
    
    /**
     * 区间类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interval {
        
        /**
         * 情感强度的最大值
         */
        @JsonProperty("max")
        private Double max;
        
        /**
         * 情感强度的最小值
         */
        @JsonProperty("min")
        private Double min;
        
        /**
         * 情感强度的默认值
         */
        @JsonProperty("default")
        private Double defaultValue;
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
