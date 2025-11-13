package com.zbkj.common.request.coze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

/**
 * Coze获取空间成员列表请求参数
 * @author: auto-generated
 * @date: 2024/01/01
 */
@Data
@ApiModel(value = "CozeGetSpaceMembersRequest", description = "Coze获取空间成员列表请求参数")
public class CozeGetSpaceMembersRequest {

    @ApiModelProperty(value = "空间ID", required = true)
    @NotBlank(message = "空间ID不能为空")
    private String workspaceId;

    @ApiModelProperty(value = "页码，默认为1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页数量，默认为20，取值范围1~50")
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer pageSize = 20;
}
