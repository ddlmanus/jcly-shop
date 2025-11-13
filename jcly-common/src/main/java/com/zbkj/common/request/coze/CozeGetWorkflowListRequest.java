package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Coze 查询工作流列表请求类
 * 根据 查询工作流列表.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeGetWorkflowListRequest", description = "Coze 查询工作流列表请求")
public class CozeGetWorkflowListRequest {

    @JsonProperty("workspace_id")
    @ApiModelProperty(value = "工作空间ID", required = true, example = "736163827687053****")
    @NotBlank(message = "工作空间ID不能为空")
    private String workspaceId;

    @JsonProperty("page_num")
    @ApiModelProperty(value = "页码", required = true, example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为1")
    private Integer pageNum;

    @JsonProperty("page_size")
    @ApiModelProperty(value = "每页数据量", example = "20")
    @Min(value = 1, message = "每页数据量最小值为1")
    private Integer pageSize;

    @JsonProperty("workflow_mode")
    @ApiModelProperty(value = "工作流类型", example = "workflow", notes = "workflow:工作流, chatflow:对话流")
    private String workflowMode;

    @JsonProperty("app_id")
    @ApiModelProperty(value = "扣子应用ID", example = "744208683**")
    private String appId;

    @ApiModelProperty(value = "发布状态", example = "all", notes = "all:所有状态, published_online:已发布, unpublished_draft:草稿状态")
    private String publishStatus;
}
