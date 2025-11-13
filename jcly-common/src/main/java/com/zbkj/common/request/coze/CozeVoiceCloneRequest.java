package com.zbkj.common.request.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

/**
 * Coze复刻音色请求类
 * 复刻指定音频文件中人声的音色
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeVoiceCloneRequest {
    
    /**
     * 此音色的名称，必选
     * 长度限制为128字节
     */
    @JsonProperty("voice_name")
    private String voiceName;
    
    /**
     * 音频文件对应的文案，可选
     * 需要和音频文件中人声朗读的内容大致一致
     * 最大长度为1024字节
     */
    @JsonProperty("text")
    private String text;
    
    /**
     * 音频文件中的语种，可选
     * zh: 中文, en: 英文, ja: 日语, es: 西班牙语, id: 印度尼西亚语, pt: 葡萄牙语
     */
    @JsonProperty("language")
    private String language;
    
    /**
     * 需要训练的音色ID，可选
     * 仅在训练音色时需要指定此参数
     */
    @JsonProperty("voice_id")
    private String voiceId;
    
    /**
     * 预览音频的文案，可选
     * 如果成功复刻音色，扣子平台会根据此文案生成一段新音色的预览音频
     */
    @JsonProperty("preview_text")
    private String previewText;
    
    /**
     * 克隆音色保存的扣子工作空间ID，可选
     * 默认保存在当前账号的个人空间中
     */
    @JsonProperty("space_id")
    private String spaceId;
    
    /**
     * 音频文件，必选
     * 支持格式：wav、mp3、ogg、m4a、aac、pcm
     * 文件大小：最大不超过10MB
     * 音频时长：建议10s~30s
     */
    private MultipartFile file;
}
