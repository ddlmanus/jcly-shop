package com.zbkj.common.request.merchant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 商户申请查询请求对象
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
@ApiModel(value="MerchantApplySearchRequest对象", description="商户申请查询请求对象")
public class MerchantApplySearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商户分类ID")
    private Integer categoryId;

    @ApiModelProperty(value = "商户类型ID")
    private Integer typeId;

    @ApiModelProperty(value = "商户关键字")
    private String keywords;

    @ApiModelProperty(value = "审核状态：1-待审核，2-审核通过，3-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty(value = "创建时间区间")
    private String dateLimit;

}
