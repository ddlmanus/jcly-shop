package com.zbkj.common.model.coze.stream;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Coze流式响应结果
 * 用于收集和管理流式响应的完整结果
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@ApiModel(value = "CozeStreamResponse", description = "Coze流式响应结果")
public class CozeStreamResponse {
    
    @ApiModelProperty(value = "对话信息")
    private CozeChatObject chat;
    
    @ApiModelProperty(value = "完整的AI回复消息")
    private CozeMessageObject completeMessage;
    
    @ApiModelProperty(value = "所有增量消息")
    private List<CozeMessageObject> deltaMessages = new ArrayList<>();
    
    @ApiModelProperty(value = "是否完成")
    private boolean completed = false;
    
    @ApiModelProperty(value = "是否失败")
    private boolean failed = false;
    
    @ApiModelProperty(value = "错误信息")
    private String errorMessage;
    
    @ApiModelProperty(value = "最终状态")
    private String finalStatus;
    
    /**
     * 添加增量消息
     */
    public void addDeltaMessage(CozeMessageObject deltaMessage) {
        if (deltaMessage != null) {
            this.deltaMessages.add(deltaMessage);
        }
    }
    
    /**
     * 检查是否为终态
     */
    public boolean isTerminalState() {
        return completed || failed || 
               CozeChatObject.STATUS_COMPLETED.equals(finalStatus) ||
               CozeChatObject.STATUS_FAILED.equals(finalStatus) ||
               CozeChatObject.STATUS_CANCELED.equals(finalStatus);
    }
}
