package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 企业聊天会话请求对象
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "EnterpriseChatSessionRequest对象", description = "企业聊天会话请求")
public class EnterpriseChatSessionRequest {

    @ApiModelProperty(value = "会话ID（可选，不提供则自动生成）")
    private String sessionId;
    @ApiModelProperty(value = "商户ID", required = false)
    private Integer merId;

    @ApiModelProperty(value = "Coze智能体ID", required = true)
    @NotBlank(message = "智能体ID不能为空")
    private String cozeBotId;

    @ApiModelProperty(value = "会话标题")
    @Size(max = 255, message = "会话标题不能超过255个字符")
    private String sessionTitle;

    @ApiModelProperty(value = "会话上下文信息（JSON格式）")
    private String sessionContext;

    @ApiModelProperty(value = "扩展元数据（JSON格式）")
    private String metaData;

    @ApiModelProperty(value = "是否自动生成标题")
    private Boolean autoGenerateTitle = true;
}
