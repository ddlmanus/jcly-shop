package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 消息响应参数
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
@ApiModel(value = "CozeMessageResponse", description = "消息响应参数")
public class CozeMessageResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "消息的详情")
    private MessageData data;

    @Data
    @ApiModel(value = "MessageData", description = "消息数据")
    public static class MessageData {
        @ApiModelProperty(value = "消息的唯一标识")
        private String id;

        @ApiModelProperty(value = "此消息所在的会话ID")
        @JsonProperty("conversation_id")
        private String conversationId;

        @ApiModelProperty(value = "编写此消息的智能体ID")
        @JsonProperty("bot_id")
        private String botId;

        @ApiModelProperty(value = "Chat ID")
        @JsonProperty("chat_id")
        private String chatId;

        @ApiModelProperty(value = "创建消息时的附加信息")
        @JsonProperty("meta_data")
        private Map<String, String> metaData;

        @ApiModelProperty(value = "发送这条消息的实体")
        private String role;

        @ApiModelProperty(value = "消息的内容")
        private String content;

        @ApiModelProperty(value = "消息内容的类型")
        @JsonProperty("content_type")
        private String contentType;

        @ApiModelProperty(value = "消息的创建时间")
        @JsonProperty("created_at")
        private Long createdAt;

        @ApiModelProperty(value = "消息的更新时间")
        @JsonProperty("updated_at")
        private Long updatedAt;

        @ApiModelProperty(value = "消息类型")
        private String type;

        @ApiModelProperty(value = "上下文片段ID")
        @JsonProperty("section_id")
        private String sectionId;

        @ApiModelProperty(value = "模型的思维链")
        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }
}
