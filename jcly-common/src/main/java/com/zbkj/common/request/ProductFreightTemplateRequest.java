package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 商品设置运费模板请求对象
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
@ApiModel(value = "ProductFreightTemplateRequest", description = "商品设置运费模板请求对象")
public class ProductFreightTemplateRequest {

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品ID 不能为空")
    private Integer id;

    @ApiModelProperty(value = "运费模板ID", required = true)
    @NotNull(message = "运费模板ID 不能为空")
    private Integer templateId;

}
