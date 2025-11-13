package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商户申请平台商品分类请求对象
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
@ApiModel(value="MerchantApplyProductCategoryRequest对象", description="商户申请平台商品分类请求对象")
public class MerchantApplyProductCategoryRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "父级ID，level=1的时候传0，level !=1 的时候传父级id")
    @NotNull(message = "父级ID不能为空")
    private Integer pid;

    @ApiModelProperty(value = "名称")
    @NotEmpty(message = "分类名称不能为空")
    @Length(max = 100, message = "分类名称不能超过100个字符")
    private String name;

    @ApiModelProperty(value = "icon")
    private String icon;

    @ApiModelProperty(value = "级别:1，2，3")
    @NotNull(message = "分类级别不能为空")
    @Range(min = 1, max = 3, message = "未知的分类级别")
    private Integer level;

    @ApiModelProperty(value = "排序")
    @NotNull(message = "排序不能为空")
    @Range(min = 0, max = 9999, message = "排序最范围为0-9999")
    private Integer sort;

    @ApiModelProperty(value = "申请理由")
    @Length(max = 500, message = "申请理由不能超过500个字符")
    private String applyReason;
} 