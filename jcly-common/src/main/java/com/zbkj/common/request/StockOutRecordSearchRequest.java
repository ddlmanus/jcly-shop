package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 出库记录搜索请求对象
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: AI Assistant
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StockOutRecordSearchRequest对象", description = "出库记录搜索请求对象")
public class StockOutRecordSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;
    
    @ApiModelProperty(value = "商品名称")
    private String productName;
    
    @ApiModelProperty(value = "出库单号")
    private String recordNo;
    
    @ApiModelProperty(value = "操作员姓名")
    private String operatorName;
    
    @ApiModelProperty(value = "出库类型：1=销售出库，2=损耗出库，3=调拨出库，4=其他出库")
    private Integer outType;
    
    @ApiModelProperty(value = "关联订单号")
    private String orderNo;
    
    @ApiModelProperty(value = "开始日期")
    private String startDate;
    
    @ApiModelProperty(value = "结束日期")
    private String endDate;
}

