package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Coze 执行工作流请求类
 * 根据 执行工作流.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeExecuteWorkflowRequest", description = "Coze 执行工作流请求")
public class CozeExecuteWorkflowRequest {

    @JsonProperty("workflow_id")
    @ApiModelProperty(value = "工作流ID", required = true, example = "73664689170551*****")
    @NotBlank(message = "工作流ID不能为空")
    private String workflowId;

    @JsonProperty("parameters")
    @ApiModelProperty(value = "工作流开始节点的输入参数，JSON序列化字符串形式", example = "{\"user_id\":\"12345\",\"user_name\":\"George\"}")
    private Map<String, Object> parameters;

    @JsonProperty("bot_id")
    @ApiModelProperty(value = "需要关联的智能体ID", example = "73428668*****")
    private String botId;

    @JsonProperty("app_id")
    @ApiModelProperty(value = "该工作流关联的扣子应用的ID", example = "744208683**")
    private String appId;

    @JsonProperty("ext")
    @ApiModelProperty(value = "额外字段，如经纬度等", example = "{\"latitude\":\"39.9042\",\"longitude\":\"116.4074\"}")
    private Map<String, String> ext;

    @JsonProperty("is_async")
    @ApiModelProperty(value = "是否异步运行", example = "false")
    private Boolean isAsync;

    @JsonProperty("workflow_version")
    @ApiModelProperty(value = "工作流版本号，仅资源库工作流有效", example = "v0.0.5")
    private String workflowVersion;
}
