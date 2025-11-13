package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改知识库信息请求参数
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Data
@ApiModel(value = "CozeUpdateKnowledgeRequest", description = "修改知识库信息请求参数")
public class CozeUpdateKnowledgeRequest {

    @ApiModelProperty(value = "知识库ID", required = true)
    @NotBlank(message = "知识库ID不能为空")
    @JsonProperty("dataset_id")
    private String datasetId;

    @ApiModelProperty(value = "知识库名称", required = true)
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 100, message = "知识库名称长度不能超过100个字符")
    private String name;

    @ApiModelProperty(value = "知识库图标")
    @JsonProperty("file_id")
    private String fileId;

    @ApiModelProperty(value = "知识库描述信息")
    private String description;
}
