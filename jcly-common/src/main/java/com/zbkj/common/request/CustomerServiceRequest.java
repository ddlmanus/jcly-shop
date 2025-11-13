package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 客服创建/编辑请求
 * @author AI Assistant
 * @since 2025-10-27
 */
@Data
@ApiModel(value = "CustomerServiceRequest对象", description = "客服创建/编辑请求")
public class CustomerServiceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "客服ID（编辑时必填）")
    private Integer staffId;

    @ApiModelProperty(value = "登录账号", required = true)
    @NotBlank(message = "登录账号不能为空")
    private String account;

    @ApiModelProperty(value = "登录密码（创建时必填，编辑时选填）")
    private String password;

    @ApiModelProperty(value = "客服姓名", required = true)
    @NotBlank(message = "客服姓名不能为空")
    private String staffName;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "客服头像")
    private String avatar;

    @ApiModelProperty(value = "服务等级：JUNIOR-初级，STANDARD-标准，SENIOR-高级，EXPERT-专家")
    private String serviceLevel;

    @ApiModelProperty(value = "技能标签JSON：[\"售前咨询\", \"售后服务\"]")
    private String skillTags;

    @ApiModelProperty(value = "最大并发会话数", required = true)
    @NotNull(message = "最大并发会话数不能为空")
    private Integer maxConcurrentSessions;

    @ApiModelProperty(value = "是否默认客服：0-否，1-是")
    private Boolean isDefault;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Boolean status;
}
