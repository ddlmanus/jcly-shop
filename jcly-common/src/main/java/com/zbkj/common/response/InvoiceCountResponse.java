package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 发票状态数量统计响应类
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
@ApiModel(value = "InvoiceCountResponse对象", description = "发票状态数量统计响应")
public class InvoiceCountResponse {

    @ApiModelProperty(value = "全部发票数量")
    private Integer allCount;

    @ApiModelProperty(value = "待付款订单发票数量")
    private Integer unpaidCount;

    @ApiModelProperty(value = "待审核发票数量") 
    private Integer pendingAuditCount;

    @ApiModelProperty(value = "待发货订单发票数量")
    private Integer pendingShipmentCount;

    @ApiModelProperty(value = "交易完成发票数量")
    private Integer completedCount;

    @ApiModelProperty(value = "已退款订单发票数量")
    private Integer refundedCount;

    @ApiModelProperty(value = "已删除发票数量")
    private Integer deletedCount;
} 