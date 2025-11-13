package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 商户端首页经营数据响应对象
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
@ApiModel(value = "HomeOperatingMerDataResponse对象", description = "商户端首页经营数据响应对象")
public class HomeOperatingMerDataResponse implements Serializable {

    private static final long serialVersionUID = -1486435421582495511L;

    @ApiModelProperty(value = "待发货订单数量")
    private Integer notShippingOrderNum;

    @ApiModelProperty(value = "待核销订单数量")
    private Integer awaitVerificationOrderNum;

    @ApiModelProperty(value = "待退款订单数量")
    private Integer refundingOrderNum;

    @ApiModelProperty(value = "在售商品数量")
    private Integer onSaleProductNum;

    @ApiModelProperty(value = "待审核商品数量")
    private Integer awaitAuditProductNum;

}
