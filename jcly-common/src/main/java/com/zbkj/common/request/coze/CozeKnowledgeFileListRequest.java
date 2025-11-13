package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

/**
 * Coze知识库文件列表请求
 */
@Data
@ApiModel(value = "CozeKnowledgeFileListRequest对象", description = "Coze知识库文件列表请求")
public class CozeKnowledgeFileListRequest {

    @ApiModelProperty(value = "待查看文件的扣子知识库ID", required = true)
    @JsonProperty("dataset_id")
    @NotBlank(message = "知识库ID不能为空")
    private String datasetId;

    @ApiModelProperty(value = "分页查询时的页码，默认为1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @ApiModelProperty(value = "分页大小，默认为10")
    @Min(value = 1, message = "分页大小必须大于0")
    private Integer size = 10;
}
