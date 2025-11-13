package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 等级更改显示状态请求对象
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
@ApiModel(value="SystemUserLevelUpdateShowRequest对象", description="等级更改显示状态请求对象")
public class SystemUserLevelUpdateShowRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "等级id")
    @NotNull(message = "等级id不能为空")
    private Integer id;

    @ApiModelProperty(value = "是否显示 true=显示,false=隐藏")
    @NotNull(message = "是否显示不能为空")
    private Boolean isShow;

}
