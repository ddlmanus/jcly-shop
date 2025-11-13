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
@ApiModel(value = "CozeModifyMessageResponse对象", description = "修改消息响应")
public class CozeModifyMessageResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "修改后的消息详细信息")
    private CozeCreateMessageResponse.MessageData message;
}
