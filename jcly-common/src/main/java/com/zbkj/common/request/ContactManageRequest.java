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
 * 联系人管理请求
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ContactManageRequest对象", description = "联系人管理请求")
public class ContactManageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "联系人ID", required = true)
    @NotNull(message = "联系人ID不能为空")
    private Integer contactId;

    @ApiModelProperty(value = "联系人类型：USER-用户，MERCHANT-商户，PLATFORM-平台", required = true)
    @NotBlank(message = "联系人类型不能为空")
    private String contactType;

    @ApiModelProperty(value = "联系人名称", required = true)
    @NotBlank(message = "联系人名称不能为空")
    private String contactName;

    @ApiModelProperty(value = "联系人头像")
    private String contactAvatar;

    @ApiModelProperty(value = "联系人电话")
    private String contactPhone;

    @ApiModelProperty(value = "备注信息")
    private String notes;

    @ApiModelProperty(value = "分组名称")
    private String groupName = "DEFAULT";

    @ApiModelProperty(value = "是否置顶")
    private Boolean isPinned = false;
}
