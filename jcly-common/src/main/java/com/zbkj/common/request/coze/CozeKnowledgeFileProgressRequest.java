package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Coze查看知识库文件上传进度请求
 */
@Data
@ApiModel(value = "CozeKnowledgeFileProgressRequest对象", description = "Coze查看知识库文件上传进度请求")
public class CozeKnowledgeFileProgressRequest {

    @ApiModelProperty(value = "知识库ID", required = true)
    @NotBlank(message = "知识库ID不能为空")
    private String datasetId;

    @ApiModelProperty(value = "文件ID列表", required = true)
    @JsonProperty("document_ids")
    @NotEmpty(message = "文件ID列表不能为空")
    private List<String> documentIds;
}
