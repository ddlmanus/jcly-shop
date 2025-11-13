package com.zbkj.common.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserIntegralReponse {
    @ApiModelProperty(value = "转换的总积分")
    private Integer integralCount;
    @ApiModelProperty(value = "新增积分 ")
    private Integer integralNewCount;
}
