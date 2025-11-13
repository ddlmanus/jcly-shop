package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发票说明更新请求类
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
@ApiModel(value = "InvoiceDescriptionRequest对象", description = "发票说明更新请求")
public class InvoiceDescriptionRequest {

    @ApiModelProperty(value = "ID")
    private Integer id;

    @ApiModelProperty(value = "标题", required = true)
    @NotBlank(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "说明内容（富文本）", required = true)
    @NotBlank(message = "说明内容不能为空")
    private String content;

    @ApiModelProperty(value = "是否显示：true-是，false-否")
    @NotNull(message = "是否显示不能为空")
    private Boolean isShow;

    @ApiModelProperty(value = "排序值")
    private Integer sort;
} 