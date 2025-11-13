package com.zbkj.common.request.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Coze语音识别请求类
 * 将音频文件转录为文本
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeVoiceTranscriptionRequest {
    
    /**
     * 音频文件，必选
     * 支持格式：opus、ogg、mp3、wav、m4a、mp4、pcm、raw、spx、aac、amr
     * 文件大小：最大10MB，时长需小于30分钟
     */
    private MultipartFile file;
}
