package com.zbkj.common.request.coze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 同步Coze知识库文件请求
 */
@Data
@ApiModel(value = "CozeSyncKnowledgeFileRequest对象", description = "同步Coze知识库文件请求")
public class CozeSyncKnowledgeFileRequest {

    @ApiModelProperty(value = "知识库ID", required = true)
    @NotBlank(message = "知识库ID不能为空")
    private String cozeKnowledgeId;
}
