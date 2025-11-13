package com.zbkj.common.request.coze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更新会话名称请求参数
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
@ApiModel(value = "CozeUpdateConversationNameRequest", description = "更新会话名称请求参数")
public class CozeUpdateConversationNameRequest {

    @ApiModelProperty(value = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    @ApiModelProperty(value = "新的会话名称", required = true)
    @NotBlank(message = "会话名称不能为空")
    @Size(max = 100, message = "会话名称长度不能超过100个字符")
    private String name;
}
