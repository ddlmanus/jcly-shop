package com.zbkj.common.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlatformHomeAreaResponse {
    private int orderTotal;
    private int userTotal;
    private BigDecimal orderPriceTotal;
    private String regionName;
}
