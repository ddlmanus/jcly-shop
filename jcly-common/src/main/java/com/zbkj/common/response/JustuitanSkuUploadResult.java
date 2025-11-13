package com.zbkj.common.response;

/**
 * 聚水潭SKU上传结果
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
public class JustuitanSkuUploadResult {
    private boolean success;
    private String skuId;
    private String itemId;
    private String message;
    private Integer localSkuId; // 本地SKU ID，用于更新数据库

    public JustuitanSkuUploadResult() {}

    public JustuitanSkuUploadResult(boolean success, String skuId, String itemId, String message, Integer localSkuId) {
        this.success = success;
        this.skuId = skuId;
        this.itemId = itemId;
        this.message = message;
        this.localSkuId = localSkuId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getLocalSkuId() {
        return localSkuId;
    }

    public void setLocalSkuId(Integer localSkuId) {
        this.localSkuId = localSkuId;
    }
}