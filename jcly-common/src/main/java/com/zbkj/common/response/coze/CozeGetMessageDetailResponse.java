package com.zbkj.common.response.coze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeGetMessageDetailResponse对象", description = "查看消息详情响应")
public class CozeGetMessageDetailResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "消息详情")
    private CozeCreateMessageResponse.MessageData data;
}
