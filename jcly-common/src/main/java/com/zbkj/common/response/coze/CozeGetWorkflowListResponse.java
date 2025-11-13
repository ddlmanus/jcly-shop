package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Coze 查询工作流列表响应类
 * 根据 查询工作流列表.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeGetWorkflowListResponse", description = "Coze 查询工作流列表响应")
public class CozeGetWorkflowListResponse {

    @ApiModelProperty(value = "状态码", example = "0")
    private Long code;

    @ApiModelProperty(value = "状态信息", example = "Success")
    private String msg;

    @ApiModelProperty(value = "工作流列表数据")
    private WorkflowListData data;

    @ApiModelProperty(value = "响应详情")
    private ResponseDetail detail;

    /**
     * 工作流列表数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowListData {
        
        @ApiModelProperty(value = "工作流列表")
        private List<WorkflowBasic> items;

        @JsonProperty("has_more")
        @ApiModelProperty(value = "是否还有更多数据", example = "false")
        private Boolean hasMore;
    }

    /**
     * 工作流基础信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowBasic {
        
        @JsonProperty("app_id")
        @ApiModelProperty(value = "工作流关联的应用ID", example = "744208683**")
        private String appId;

        @ApiModelProperty(value = "工作流创建者信息")
        private UserInfo creator;

        @JsonProperty("icon_url")
        @ApiModelProperty(value = "工作流图标URL", example = "https://example.com/icon/workflow_123.png")
        private String iconUrl;

        @JsonProperty("created_at")
        @ApiModelProperty(value = "创建时间，Unix时间戳", example = "1752060786")
        private String createdAt;

        @JsonProperty("updated_at")
        @ApiModelProperty(value = "更新时间，Unix时间戳", example = "1752060827")
        private String updatedAt;

        @ApiModelProperty(value = "工作流描述", example = "工作流测试")
        private String description;

        @JsonProperty("workflow_id")
        @ApiModelProperty(value = "工作流ID", example = "73505836754923***")
        private String workflowId;

        @JsonProperty("workflow_name")
        @ApiModelProperty(value = "工作流名称", example = "workflow_example")
        private String workflowName;
    }

    /**
     * 用户信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        
        @ApiModelProperty(value = "扣子用户ID", example = "2478774393***")
        private String id;

        @ApiModelProperty(value = "扣子用户名", example = "user41833***")
        private String name;
    }

    /**
     * 响应详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDetail {
        
        @ApiModelProperty(value = "日志ID", example = "20241210152726467C48D89D6DB2****")
        private String logid;
    }
}
