package com.zbkj.common.request;

/**
 * 聚水潭订单拆分信息
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
public class JustuitanOrderSplitInfo {
    private Integer orderDetailId; // 订单详情ID
    private Integer splitQuantity; // 拆分数量

    public JustuitanOrderSplitInfo() {}

    public JustuitanOrderSplitInfo(Integer orderDetailId, Integer splitQuantity) {
        this.orderDetailId = orderDetailId;
        this.splitQuantity = splitQuantity;
    }

    public Integer getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(Integer orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public Integer getSplitQuantity() {
        return splitQuantity;
    }

    public void setSplitQuantity(Integer splitQuantity) {
        this.splitQuantity = splitQuantity;
    }
}