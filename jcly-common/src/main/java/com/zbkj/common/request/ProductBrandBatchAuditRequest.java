package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 商品品牌批量审核请求对象
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
@ApiModel(value="ProductBrandBatchAuditRequest对象", description="商品品牌批量审核请求对象")
public class ProductBrandBatchAuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "品牌ID集合", required = true)
    @NotEmpty(message = "品牌ID集合不能为空")
    private List<Integer> ids;
    
    @ApiModelProperty(value = "审核状态：1-审核通过，2-审核拒绝", required = true)
    @NotNull(message = "审核状态不能为空")
    @Range(min = 1, max = 2, message = "审核状态只能是1或2")
    private Integer auditStatus;
    
    @ApiModelProperty(value = "拒绝原因")
    private String rejectReason;
} 