package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 下架智能体请求参数
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
@ApiModel(value = "CozeUnpublishBotRequest", description = "下架智能体请求参数")
public class CozeUnpublishBotRequest {

    @ApiModelProperty(value = "智能体ID", required = true)
    @NotBlank(message = "智能体ID不能为空")
    @JsonProperty("bot_id")
    private String botId;

    @ApiModelProperty(value = "渠道ID", required = true)
    @NotBlank(message = "渠道ID不能为空")
    @JsonProperty("connector_id")
    private String connectorId;

    @ApiModelProperty(value = "下架原因")
    @Size(max = 1024, message = "下架原因长度不能超过1024个字符")
    @JsonProperty("unpublish_reason")
    private String unpublishReason;
}
