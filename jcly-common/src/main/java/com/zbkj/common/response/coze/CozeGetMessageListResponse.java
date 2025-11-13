package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeGetMessageListResponse对象", description = "查看消息列表响应")
public class CozeGetMessageListResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "消息详情")
    private List<CozeCreateMessageResponse.MessageData> data;
    
    @ApiModelProperty(value = "是否已返回全部消息")
    @JsonProperty("has_more")
    private Boolean hasMore;
    
    @ApiModelProperty(value = "返回的消息列表中，第一条消息的 Message ID")
    @JsonProperty("first_id")
    private String firstId;
    
    @ApiModelProperty(value = "返回的消息列表中，最后一条消息的 Message ID")
    @JsonProperty("last_id")
    private String lastId;
}
