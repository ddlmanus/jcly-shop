package com.zbkj.common.request.coze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 同步Coze知识库请求
 */
@Data
@ApiModel(value = "CozeSyncKnowledgeRequest对象", description = "同步Coze知识库请求")
public class CozeSyncKnowledgeRequest {

    @ApiModelProperty(value = "空间ID", required = true)
    @NotBlank(message = "空间ID不能为空")
    private String spaceId;

    @ApiModelProperty(value = "知识库名称(可选)")
    private String name;

    @ApiModelProperty(value = "知识库类型(可选)")
    private Integer formatType;
}
