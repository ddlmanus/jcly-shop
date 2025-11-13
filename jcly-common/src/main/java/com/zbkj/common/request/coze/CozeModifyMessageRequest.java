package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeModifyMessageRequest对象", description = "修改消息请求参数")
public class CozeModifyMessageRequest {
    
    @ApiModelProperty(value = "会话ID", required = true)
    @JsonProperty("conversation_id")
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;
    
    @ApiModelProperty(value = "消息ID", required = true)
    @JsonProperty("message_id")
    @NotBlank(message = "消息ID不能为空")
    private String messageId;
    
    @ApiModelProperty(value = "创建消息时的附加信息")
    @JsonProperty("meta_data")
    private Map<String, String> metaData;
    
    @ApiModelProperty(value = "消息的内容")
    private String content;
    
    @ApiModelProperty(value = "消息内容的类型。text：文本，object_string：多模态内容")
    @JsonProperty("content_type")
    private String contentType;
}