package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 商户端合并开票请求类
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
@ApiModel(value = "MerchantInvoiceMergeRequest对象", description = "商户端合并开票请求")
public class MerchantInvoiceMergeRequest {

    @ApiModelProperty(value = "发票ID列表", required = true)
    @NotEmpty(message = "发票ID列表不能为空")
    private List<Integer> invoiceIds;

    @ApiModelProperty(value = "发票号码")
    private String invoiceCode;

    @ApiModelProperty(value = "发票备注")
    private String remark;
} 