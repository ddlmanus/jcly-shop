package com.zbkj.common.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PlatformHomeUserResponse {
    @ApiModelProperty(value = "新增用户数")
    private Integer newUserNum;
    @ApiModelProperty(value = "新增商户数")
    private Integer newMerchantNum;

    @ApiModelProperty(value = "时间")
    private String day;

}
