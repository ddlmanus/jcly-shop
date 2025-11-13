package com.zbkj.common.model.coze;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zbkj.common.response.coze.CozeGetWorkflowListResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Coze 工作流表
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_workflow")
@ApiModel(value = "CozeWorkflow对象", description = "Coze 工作流表")
public class CozeWorkflow implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "Coze工作流ID")
    private String cozeWorkflowId;

    @ApiModelProperty(value = "工作流名称")
    private String name;

    @ApiModelProperty(value = "工作流描述")
    private String description;

    @ApiModelProperty(value = "Coze空间ID")
    private String spaceId;

    @ApiModelProperty(value = "工作流配置(JSON格式)")
    private String config;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @JsonProperty("app_id")
    @ApiModelProperty(value = "工作流关联的应用ID", example = "744208683**")
    private String appId;
    @ApiModelProperty(value = "工作流类型", example = "workflow", notes = "workflow:工作流, chatflow:对话流")
    private String workflowMode;
    @JsonProperty("publish_status")
    @ApiModelProperty(value = "发布状态", example = "all", notes = "all:所有状态, published_online:已发布, unpublished_draft:草稿状态")
    private Integer publishStatus;
    @ApiModelProperty(value = "工作流创建者信息")
    private String  creatorId;
    @ApiModelProperty(value = "工作流创建者信息")
    private String creatorName;

    @ApiModelProperty(value = "工作流图标URL", example = "https://example.com/icon/workflow_123.png")
    private String iconUrl;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
