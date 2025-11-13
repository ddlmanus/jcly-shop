package com.zbkj.common.request.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze语音合成请求类
 * 将指定文本合成为音频文件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeVoiceSpeechRequest {
    
    /**
     * 合成语音的文本，必选
     * 经由UTF-8编码，长度限制为1024字节
     */
    @JsonProperty("input")
    private String input;
    
    /**
     * 音频文件使用的音色ID，必选
     */
    @JsonProperty("voice_id")
    private String voiceId;
    
    /**
     * 设置多情感音色的情感类型，可选
     * 仅当voice_id为多情感音色时才支持设置情感类型
     * 枚举值：happy(开心)、sad(悲伤)、angry(愤怒)、surprised(惊讶)、
     *        fear(恐惧)、hate(厌恶)、excited(兴奋)、coldness(冷漠)、neutral(中性)
     */
    @JsonProperty("emotion")
    private String emotion;
    
    /**
     * 情感值用于量化情感的强度，可选
     * 数值越高，情感表达越强烈
     * 取值范围：1.0~5.0，默认值：4.0
     */
    @JsonProperty("emotion_scale")
    private Double emotionScale;
    
    /**
     * 音频文件的编码格式，可选
     * wav: 返回二进制wav音频
     * pcm: 返回二进制pcm音频
     * ogg_opus: 返回多段含opus压缩分片音频
     * mp3: (默认)返回二进制mp3音频
     */
    @JsonProperty("response_format")
    private String responseFormat;
    
    /**
     * 语速，可选
     * 大模型音色的取值范围为0.5~2，小模型音色的取值范围为0.2~3
     * 默认为1，表示1倍速
     */
    @JsonProperty("speed")
    private Double speed;
    
    /**
     * 音频采样率，单位为Hz，可选
     * 8000: 8k, 16000: 16k, 22050: 22.05k, 24000: (默认)24k,
     * 32000: 32k, 44100: 44.1k, 48000: 48k
     */
    @JsonProperty("sample_rate")
    private Integer sampleRate;
    
    /**
     * 音频输出音量的增益或衰减比例，可选
     * 以百分比形式表示，取值范围为-50~100，默认值为0
     * 负值表示衰减，正值表示增益
     */
    @JsonProperty("loudness_rate")
    private Integer loudnessRate;
}
