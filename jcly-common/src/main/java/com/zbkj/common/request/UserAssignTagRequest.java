package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户分配标签请求对象
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
@Accessors(chain = true)
@ApiModel(value = "UserAssignTagRequest对象", description = "用户分配标签请求对象")
public class UserAssignTagRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id，英文逗号分隔")
    @NotBlank(message = "用户id不能为空")
    private String ids;

    @ApiModelProperty(value = "用户标签IDs,英文逗号分隔")
    @NotBlank(message = "用户标签ID不能为空")
    private String tagIds;

}
