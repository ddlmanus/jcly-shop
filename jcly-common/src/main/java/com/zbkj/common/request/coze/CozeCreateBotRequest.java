package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * 创建智能体请求参数
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
@ApiModel(value = "CozeCreateBotRequest", description = "创建智能体请求参数")
public class CozeCreateBotRequest {

    @ApiModelProperty(value = "智能体所在的空间ID", required = true)
    @NotBlank(message = "空间ID不能为空")
    @JsonProperty("space_id")
    private String spaceId;

    @ApiModelProperty(value = "智能体的名称", required = true)
    @NotBlank(message = "智能体名称不能为空")
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
    private PromptInfo promptInfo;

    @ApiModelProperty(value = "智能体的开场白相关设置")
    @JsonProperty("onboarding_info")
    private OnboardingInfo onboardingInfo;

    @ApiModelProperty(value = "智能体的插件配置")
    @JsonProperty("plugin_id_list")
    private PluginIdList pluginIdList;

    @ApiModelProperty(value = "智能体绑定的工作流ID列表")
    @JsonProperty("workflow_id_list")
    private WorkflowIdList workflowIdList;

    @ApiModelProperty(value = "智能体的模型配置")
    @JsonProperty("model_info_config")
    private ModelInfoConfig modelInfoConfig;

    @ApiModelProperty(value = "配置智能体回复后是否提供用户问题建议")
    @JsonProperty("suggest_reply_info")
    private SuggestReplyInfo suggestReplyInfo;

    @Data
    @ApiModel(value = "PromptInfo", description = "智能体人设信息")
    public static class PromptInfo {
        @ApiModelProperty(value = "智能体的人设与回复逻辑")
        @Size(max = 20000, message = "智能体人设长度不能超过20000个字符")
        private String prompt;
    }

    @Data
    @ApiModel(value = "OnboardingInfo", description = "智能体开场白信息")
    public static class OnboardingInfo {
        @ApiModelProperty(value = "智能体的开场白")
        @Size(max = 300, message = "开场白长度不能超过300个字符")
        private String prologue;

        @ApiModelProperty(value = "智能体的开场白预置问题")
        @JsonProperty("suggested_questions")
        private List<String> suggestedQuestions;
    }

    @Data
    @ApiModel(value = "PluginIdList", description = "插件列表配置")
    public static class PluginIdList {
        @ApiModelProperty(value = "插件列表")
        @JsonProperty("id_list")
        private List<PluginIdInfo> idList;
    }

    @Data
    @ApiModel(value = "PluginIdInfo", description = "插件信息")
    public static class PluginIdInfo {
        @ApiModelProperty(value = "智能体绑定的插件工具ID", required = true)
        @NotBlank(message = "插件工具ID不能为空")
        @JsonProperty("api_id")
        private String apiId;

        @ApiModelProperty(value = "智能体绑定的插件ID", required = true)
        @NotBlank(message = "插件ID不能为空")
        @JsonProperty("plugin_id")
        private String pluginId;
    }

    @Data
    @ApiModel(value = "WorkflowIdList", description = "工作流列表配置")
    public static class WorkflowIdList {
        @ApiModelProperty(value = "工作流列表")
        private List<WorkflowIdInfo> ids;
    }

    @Data
    @ApiModel(value = "WorkflowIdInfo", description = "工作流信息")
    public static class WorkflowIdInfo {
        @ApiModelProperty(value = "智能体绑定的工作流ID", required = true)
        @NotBlank(message = "工作流ID不能为空")
        private String id;
    }

    @Data
    @ApiModel(value = "ModelInfoConfig", description = "模型配置")
    public static class ModelInfoConfig {
        @ApiModelProperty(value = "智能体绑定的模型ID", required = true)
        @NotBlank(message = "模型ID不能为空")
        @JsonProperty("model_id")
        private String modelId;

        @ApiModelProperty(value = "Top K")
        @JsonProperty("top_k")
        private Integer topK;

        @ApiModelProperty(value = "Top P，即累计概率")
        @JsonProperty("top_p")
        private Double topP;

        @ApiModelProperty(value = "上下文缓存类型")
        @JsonProperty("cache_type")
        private String cacheType;

        @ApiModelProperty(value = "最大回复长度")
        @JsonProperty("max_tokens")
        private Integer maxTokens;

        @ApiModelProperty(value = "模型深度思考相关配置")
        private Map<String, Object> parameters;

        @ApiModelProperty(value = "生成随机性")
        private Double temperature;

        @ApiModelProperty(value = "是否启用SP拼接防泄露指令")
        @JsonProperty("sp_anti_leak")
        private Boolean spAntiLeak;

        @ApiModelProperty(value = "携带上下文轮数")
        @JsonProperty("context_round")
        private Integer contextRound;

        @ApiModelProperty(value = "输出格式")
        @JsonProperty("response_format")
        private String responseFormat;

        @ApiModelProperty(value = "是否在SP中包含当前时间信息")
        @JsonProperty("sp_current_time")
        private Boolean spCurrentTime;

        @ApiModelProperty(value = "重复主题惩罚")
        @JsonProperty("presence_penalty")
        private Double presencePenalty;

        @ApiModelProperty(value = "重复语句惩罚")
        @JsonProperty("frequency_penalty")
        private Double frequencyPenalty;
    }

    @Data
    @ApiModel(value = "SuggestReplyInfo", description = "用户问题建议配置")
    public static class SuggestReplyInfo {
        @ApiModelProperty(value = "回复模式")
        @JsonProperty("reply_mode")
        private String replyMode;

        @ApiModelProperty(value = "自定义提示词")
        @JsonProperty("customized_prompt")
        private String customizedPrompt;
    }
}
