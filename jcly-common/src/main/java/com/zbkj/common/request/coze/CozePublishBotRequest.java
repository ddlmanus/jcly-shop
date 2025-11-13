package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发布智能体请求参数
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
@ApiModel(value = "CozePublishBotRequest", description = "发布智能体请求参数")
public class CozePublishBotRequest {

    @ApiModelProperty(value = "智能体ID", required = true)
    @NotBlank(message = "智能体ID不能为空")
    @JsonProperty("bot_id")
    private String botId;

    @ApiModelProperty(value = "智能体的发布渠道ID列表", required = true)
    @JsonProperty("connector_ids")
    private java.util.List<String> connectorIds;

    @ApiModelProperty(value = "智能体发布自定义渠道时的自定义参数")
    private java.util.Map<String, Object> connectors;
}
