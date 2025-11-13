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
@ApiModel(value = "CozeGetConversationListRequest对象", description = "查看会话列表请求参数")
public class CozeGetConversationListRequest {
    
    @ApiModelProperty(value = "智能体的 ID", required = true)
    @JsonProperty("bot_id")
    @NotBlank(message = "智能体ID不能为空")
    private String botId;
    
    @ApiModelProperty(value = "分页页码，从 1 开始")
    @JsonProperty("page_num")
    private Integer pageNum;
    
    @ApiModelProperty(value = "分页大小")
    @JsonProperty("page_size")
    private Integer pageSize;
    
    @ApiModelProperty(value = "会话列表的排序方式：ASC（按创建时间升序），DESC（按创建时间降序）")
    @JsonProperty("sort_order")
    private String sortOrder;
    
    @ApiModelProperty(value = "发布渠道 ID，用于筛选指定渠道的会话")
    @JsonProperty("connector_id")
    private String connectorId;
}
