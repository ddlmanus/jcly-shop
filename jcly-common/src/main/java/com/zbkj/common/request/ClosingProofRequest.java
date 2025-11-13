package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 结算到账凭证请求对象
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
@ApiModel(value = "ClosingProofRequest对象", description = "结算到账凭证请求对象")
public class ClosingProofRequest implements Serializable {

    private static final long serialVersionUID = 3362714265772774491L;

    @ApiModelProperty(value = "结算单号")
    @NotEmpty(message = "结算单号不能为空")
    private String closingNo;

    @ApiModelProperty(value = "结算凭证")
    @NotEmpty(message = "结算凭证不能为空")
    private String closingProof;

}
