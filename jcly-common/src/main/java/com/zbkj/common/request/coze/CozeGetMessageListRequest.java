package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeGetMessageListRequest对象", description = "查看消息列表请求参数")
public class CozeGetMessageListRequest {
    
    @ApiModelProperty(value = "会话ID", required = true)
    @JsonProperty("conversation_id")
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;
    
    @ApiModelProperty(value = "消息列表的排序方式。desc：按创建时间降序排序，asc：按创建时间升序排序")
    private String order;
    
    @ApiModelProperty(value = "筛选指定 Chat ID 中的消息列表")
    @JsonProperty("chat_id")
    private String chatId;
    
    @ApiModelProperty(value = "查看指定位置之前的消息")
    @JsonProperty("before_id")
    private String beforeId;
    
    @ApiModelProperty(value = "查看指定位置之后的消息")
    @JsonProperty("after_id")
    private String afterId;
    
    @ApiModelProperty(value = "每次查询返回的数据量。默认为 50，取值范围为 1~50")
    private Integer limit;
}
