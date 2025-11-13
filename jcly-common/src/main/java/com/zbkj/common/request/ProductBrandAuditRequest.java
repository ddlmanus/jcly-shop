package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商品品牌审核请求对象
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
@ApiModel(value="ProductBrandAuditRequest对象", description="商品品牌审核请求对象")
public class ProductBrandAuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "品牌ID")
    @NotNull(message = "品牌ID不能为空")
    private Integer id;

    @ApiModelProperty(value = "审核状态：1-审核通过，2-审核拒绝")
    @NotNull(message = "审核状态不能为空")
    @Range(min = 1, max = 2, message = "未知的审核状态")
    private Integer auditStatus;

    @ApiModelProperty(value = "拒绝原因")
    @Length(max = 200, message = "拒绝原因不能超过200个字符")
    private String rejectReason;
} 