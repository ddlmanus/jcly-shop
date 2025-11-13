package com.zbkj.common.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderTop10Response {

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 销售总数量
     */
    private int orderTotalNum;

    /**
     * 单位
     */
    private String unit;

    /**
     * 支付金额
     */
    private BigDecimal payNum;

    private Integer productId;
}
