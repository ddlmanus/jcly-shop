package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更新智能体请求参数
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
@ApiModel(value = "CozeUpdateBotRequest", description = "更新智能体请求参数")
public class CozeUpdateBotRequest {

    @ApiModelProperty(value = "智能体ID", required = true)
    @NotBlank(message = "智能体ID不能为空")
    @JsonProperty("bot_id")
    private String botId;

    // 注意：根据Coze API文档，更新智能体接口不需要space_id参数
    // @ApiModelProperty(value = "智能体所在的空间ID")
    // @JsonProperty("space_id")
    // private String spaceId;

    @ApiModelProperty(value = "智能体的名称")
    @Size(min = 1, max = 20, message = "智能体名称长度必须在1-20个字符之间")
    private String name;

    @ApiModelProperty(value = "智能体的描述信息")
    @Size(max = 500, message = "智能体描述长度不能超过500个字符")
    private String description;

    @ApiModelProperty(value = "作为智能体头像的文件ID")
    @JsonProperty("icon_file_id")
    private String iconFileId;

    @ApiModelProperty(value = "智能体的人设与回复逻辑")
    @JsonProperty("prompt_info")
    private CozeCreateBotRequest.PromptInfo promptInfo;

    @ApiModelProperty(value = "智能体的开场白相关设置")
    @JsonProperty("onboarding_info")
    private CozeCreateBotRequest.OnboardingInfo onboardingInfo;

    @ApiModelProperty(value = "智能体的插件配置")
    @JsonProperty("plugin_id_list")
    private CozeCreateBotRequest.PluginIdList pluginIdList;

    @ApiModelProperty(value = "智能体绑定的工作流ID列表")
    @JsonProperty("workflow_id_list")
    private CozeCreateBotRequest.WorkflowIdList workflowIdList;

    @ApiModelProperty(value = "智能体的模型配置")
    @JsonProperty("model_info_config")
    private CozeCreateBotRequest.ModelInfoConfig modelInfoConfig;

    @ApiModelProperty(value = "配置智能体回复后是否提供用户问题建议")
    @JsonProperty("suggest_reply_info")
    private CozeCreateBotRequest.SuggestReplyInfo suggestReplyInfo;

    @ApiModelProperty(value = "智能体绑定的知识库配置")
    private Knowledge knowledge;

    @Data
    @ApiModel(value = "Knowledge", description = "知识库配置")
    public static class Knowledge {
        @ApiModelProperty(value = "智能体绑定的知识库ID数组")
        @JsonProperty("dataset_ids")
        private java.util.List<String> datasetIds;

        @ApiModelProperty(value = "是否自动调用知识库")
        @JsonProperty("auto_call")
        private Boolean autoCall;

        @ApiModelProperty(value = "搜索策略")
        @JsonProperty("search_strategy")
        private Integer searchStrategy;
    }
}
