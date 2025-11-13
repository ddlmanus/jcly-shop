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
@ApiModel(value = "CozeCreateMessageRequest对象", description = "创建消息请求参数")
public class CozeCreateMessageRequest {
    
    @ApiModelProperty(value = "会话ID", required = true)
    @JsonProperty("conversation_id")
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;
    
    @ApiModelProperty(value = "发送这条消息的实体。取值：user：代表该条消息内容是用户发送的。assistant：代表该条消息内容是 Bot 发送的", required = true)
    @NotBlank(message = "角色不能为空")
    private String role;
    
    @ApiModelProperty(value = "消息的内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    @ApiModelProperty(value = "消息内容的类型。text：文本，object_string：多模态内容", required = true)
    @JsonProperty("content_type")
    @NotBlank(message = "消息内容类型不能为空")
    private String contentType;
    
    @ApiModelProperty(value = "创建消息时的附加信息，用于封装业务相关的自定义键值对")
    @JsonProperty("meta_data")
    private Map<String, String> metaData;
}