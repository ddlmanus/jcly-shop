package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售趋势图表响应对象
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
@ApiModel(value="SalesTrendChartResponse对象", description="销售趋势图表响应对象")
public class SalesTrendChartResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "日期列表")
    private List<String> dateList;

    @ApiModelProperty(value = "销售额数据")
    private List<BigDecimal> salesAmountList;

    @ApiModelProperty(value = "订单数量数据")
    private List<Integer> orderCountList;

    @ApiModelProperty(value = "图表标题")
    private String title;

    @ApiModelProperty(value = "时间类型：day/month/year")
    private String timeType;
}
