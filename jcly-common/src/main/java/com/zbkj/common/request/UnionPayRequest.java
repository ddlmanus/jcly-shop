package com.zbkj.common.request;

import lombok.Data;

@Data
public class UnionPayRequest {
    private String orderNo;
    private String payPrice;
    private String productName;
}
