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
 * 系统配置请求对象
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
@ApiModel(value="SystemConfigAdminRequest对象", description="系统配置请求对象")
public class SystemConfigAdminRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "配置id")
    @NotNull(message = "配置id不能为空")
    private Integer id;

    @ApiModelProperty(value = "字段名称")
    @NotBlank(message = "字段名称不能为空")
    private String name;

    @ApiModelProperty(value = "字段提示文字")
    @NotBlank(message = "字段提示文字不能为空")
    private String title;

    @ApiModelProperty(value = "表单id")
    @NotNull(message = "表单id不能为空")
    private Integer formId;

    @ApiModelProperty(value = "值")
    @NotBlank(message = "值不能为空")
    private String value;

    @ApiModelProperty(value = "是否隐藏")
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
