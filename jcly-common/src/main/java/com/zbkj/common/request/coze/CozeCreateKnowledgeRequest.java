package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 创建知识库请求参数
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
@ApiModel(value = "CozeCreateKnowledgeRequest", description = "创建知识库请求参数")
public class CozeCreateKnowledgeRequest {

    @ApiModelProperty(value = "知识库名称", required = true)
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 100, message = "知识库名称长度不能超过100个字符")
    private String name;

    @ApiModelProperty(value = "知识库所在的空间的Space ID", required = true)
    @NotBlank(message = "空间ID不能为空")
    @JsonProperty("space_id")
    private String spaceId;

    @ApiModelProperty(value = "知识库类型", required = true)
    @NotNull(message = "知识库类型不能为空")
    @JsonProperty("format_type")
    private Integer formatType;

    @ApiModelProperty(value = "知识库描述信息")
    private String description;

    @ApiModelProperty(value = "知识库图标")
    @JsonProperty("file_id")
    private String fileId;
}
