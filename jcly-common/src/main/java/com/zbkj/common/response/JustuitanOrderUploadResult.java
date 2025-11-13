package com.zbkj.common.response;

/**
 * 聚水潭订单上传结果
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
public class JustuitanOrderUploadResult {
    private boolean success;
    private String message;
    private String jstOrderId; // 聚水潭订单ID
    private String orderNo; // 本地订单号

    public JustuitanOrderUploadResult() {}

    public JustuitanOrderUploadResult(boolean success, String message) {
        this.success = success;
        this.message = message;
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

    public String getJstOrderId() {
        return jstOrderId;
    }

    public void setJstOrderId(String jstOrderId) {
        this.jstOrderId = jstOrderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}