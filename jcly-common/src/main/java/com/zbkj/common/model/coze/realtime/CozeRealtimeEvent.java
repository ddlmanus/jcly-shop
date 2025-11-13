package com.zbkj.common.model.coze.realtime;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Coze Realtime事件基类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeRealtimeEvent {
    
    /**
     * 事件ID，客户端自行生成的事件ID，方便定位问题
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * 事件类型
     */
    @JsonProperty("event_type")
    private String eventType;
    
    /**
     * 事件数据
     */
    @JsonProperty("data")
    private Map<String, Object> data;
}
