package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 转人工客服请求
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TransferToHumanRequest对象", description = "转人工客服请求")
public class TransferToHumanRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "企业聊天会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String enterpriseSessionId;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @ApiModelProperty(value = "用户类型：CUSTOMER-客户，MERCHANT-商户，PLATFORM-平台", required = true)
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    @ApiModelProperty(value = "商户ID", required = true)
    @NotNull(message = "商户ID不能为空")
    private Integer merId;

    @ApiModelProperty(value = "转接原因")
    private String transferReason;

    @ApiModelProperty(value = "优先级：LOW-低，NORMAL-普通，HIGH-高，URGENT-紧急")
    private String priority = "NORMAL";

    @ApiModelProperty(value = "指定客服ID（可选）这里的客服ID是指adminId")
    private Integer assignedStaffId;

    @ApiModelProperty(value = "技能要求（JSON格式）")
    private String requiredSkills;
}
