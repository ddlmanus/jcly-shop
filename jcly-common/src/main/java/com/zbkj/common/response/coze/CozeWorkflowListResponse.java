package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Coze工作流列表响应
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
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CozeWorkflowListResponse", description = "Coze工作流列表响应")
public class CozeWorkflowListResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "工作流列表数据")
    private WorkflowListData data;

    @Data
    @ApiModel(value = "WorkflowListData", description = "工作流列表数据")
    public static class WorkflowListData {
        
        @ApiModelProperty(value = "工作流列表")
        private List<WorkflowInfo> workflows;
        
        @ApiModelProperty(value = "总数")
        private Integer total;
        
        @ApiModelProperty(value = "当前页码")
        @JsonProperty("page_num")
        private Integer pageNum;
        
        @ApiModelProperty(value = "每页大小")
        @JsonProperty("page_size")
        private Integer pageSize;
        
        @ApiModelProperty(value = "总页数")
        @JsonProperty("total_pages")
        private Integer totalPages;
        
        @ApiModelProperty(value = "是否有下一页")
        @JsonProperty("has_more")
        private Boolean hasMore;
    }

    @Data
    @ApiModel(value = "WorkflowInfo", description = "工作流信息")
    public static class WorkflowInfo {
        
        @ApiModelProperty(value = "工作流ID")
        @JsonProperty("workflow_id")
        private String workflowId;
        
        @ApiModelProperty(value = "工作流名称")
        private String name;
        
        @ApiModelProperty(value = "工作流描述")
        private String description;
        
        @ApiModelProperty(value = "工作流类型")
        @JsonProperty("workflow_type")
        private String workflowType;
        
        @ApiModelProperty(value = "工作流模式")
        @JsonProperty("workflow_mode")
        private String workflowMode;
        
        @ApiModelProperty(value = "创建时间")
        @JsonProperty("create_time")
        private Long createTime;
        
        @ApiModelProperty(value = "更新时间")
        @JsonProperty("update_time")
        private Long updateTime;
        
        @ApiModelProperty(value = "发布状态")
        @JsonProperty("publish_status")
        private String publishStatus;
        
        @ApiModelProperty(value = "应用ID")
        @JsonProperty("app_id")
        private String appId;
        
        @ApiModelProperty(value = "空间ID")
        @JsonProperty("workspace_id")
        private String workspaceId;
        
        @ApiModelProperty(value = "创建者ID")
        @JsonProperty("creator_id")
        private String creatorId;
    }
}
