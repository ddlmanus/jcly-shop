package com.zbkj.common.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚水潭订单拆分结果
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
public class JustuitanOrderSplitResult {
    private boolean success;
    private String message;
    private List<String> newOrderIds; // 新生成的订单ID列表

    public JustuitanOrderSplitResult() {
        this.newOrderIds = new ArrayList<>();
    }

    public JustuitanOrderSplitResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.newOrderIds = new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getNewOrderIds() {
        return newOrderIds;
    }

    public void setNewOrderIds(List<String> newOrderIds) {
        this.newOrderIds = newOrderIds;
    }
}