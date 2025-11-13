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
 * 发起对话请求参数
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
@ApiModel(value = "CozeStartChatRequest", description = "发起对话请求参数")
public class CozeStartChatRequest {

    @ApiModelProperty(value = "会话ID")
    @JsonProperty("conversation_id")
    private String conversationId;

    @ApiModelProperty(value = "要进行会话聊天的智能体ID", required = true)
    @NotBlank(message = "智能体ID不能为空")
    @JsonProperty("bot_id")
    private String botId;

    @ApiModelProperty(value = "标识当前与智能体对话的用户", required = true)
    @NotBlank(message = "用户ID不能为空")
    @JsonProperty("user_id")
    private String userId;

    @ApiModelProperty(value = "对话的附加信息")
    @JsonProperty("additional_messages")
    @Size(max = 100, message = "附加消息数量不能超过100条")
    private List<EnterMessage> additionalMessages;

    @ApiModelProperty(value = "是否启用流式返回")
    private Boolean stream;

    @ApiModelProperty(value = "智能体提示词中定义的变量")
    @JsonProperty("custom_variables")
    private Map<String, String> customVariables;

    @ApiModelProperty(value = "是否保存本次对话记录")
    @JsonProperty("auto_save_history")
    private Boolean autoSaveHistory;

    @ApiModelProperty(value = "附加信息")
    @JsonProperty("meta_data")
    private Map<String, String> metaData;

    @ApiModelProperty(value = "附加参数")
    @JsonProperty("extra_params")
    private Map<String, String> extraParams;

    @ApiModelProperty(value = "快捷指令信息")
    @JsonProperty("shortcut_command")
    private ShortcutCommandDetail shortcutCommand;

    @ApiModelProperty(value = "给自定义参数赋值并传给对话流")
    private Map<String, Object> parameters;

    @ApiModelProperty(value = "设置问答节点返回的内容是否为卡片形式")
    @JsonProperty("enable_card")
    private Boolean enableCard;

    @Data
    @ApiModel(value = "EnterMessage", description = "消息信息")
    public static class EnterMessage {
        @ApiModelProperty(value = "发送这条消息的实体", required = true)
        @NotBlank(message = "消息角色不能为空")
        private String role;

        @ApiModelProperty(value = "消息类型")
        private String type;

        @ApiModelProperty(value = "消息的内容")
        private String content;

        @ApiModelProperty(value = "消息内容的类型")
        @JsonProperty("content_type")
        private String contentType;

        @ApiModelProperty(value = "创建消息时的附加消息")
        @JsonProperty("meta_data")
        private Map<String, String> metaData;
    }

    @Data
    @ApiModel(value = "ShortcutCommandDetail", description = "快捷指令详情")
    public static class ShortcutCommandDetail {
        @ApiModelProperty(value = "对话要执行的快捷指令ID", required = true)
        @NotBlank(message = "快捷指令ID不能为空")
        @JsonProperty("command_id")
        private String commandId;

        @ApiModelProperty(value = "用户输入的快捷指令组件参数信息")
        private Map<String, String> parameters;
    }
}
