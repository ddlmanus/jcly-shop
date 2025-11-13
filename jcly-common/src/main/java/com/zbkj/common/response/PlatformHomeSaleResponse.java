package com.zbkj.common.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PlatformHomeSaleResponse {
    @ApiModelProperty(value = "销售总金额")
    private Integer saleTotal;

    @ApiModelProperty(value = "时间")
    private String day;
}
