package com.zbkj.common.request.coze;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze查看音色列表请求类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeVoiceListRequest {
    
    /**
     * 查看音色列表时是否过滤掉系统音色，可选
     * true: 过滤系统音色
     * false: (默认)不过滤系统音色
     */
    @JsonProperty("filter_system_voice")
    private Boolean filterSystemVoice;
    
    /**
     * 音色模型的类型，可选
     * big: 大模型
     * small: 小模型
     */
    @JsonProperty("model_type")
    private String modelType;
    
    /**
     * 音色克隆状态，可选
     * init: 待克隆
     * cloned: (默认值)已克隆
     * all: 全部
     */
    @JsonProperty("voice_state")
    private String voiceState;
    
    /**
     * 查询结果分页展示时，此参数用于设置查看的页码，可选
     * 最小值为1，默认为1
     */
    @JsonProperty("page_num")
    private Integer pageNum;
    
    /**
     * 查询结果分页展示时，此参数用于设置每页返回的数据量，可选
     * 取值范围为1~100，默认为100
     */
    @JsonProperty("page_size")
    private Integer pageSize;
}
