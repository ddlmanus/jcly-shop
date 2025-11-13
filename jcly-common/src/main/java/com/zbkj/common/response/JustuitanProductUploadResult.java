package com.zbkj.common.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚水潭商品上传结果
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
public class JustuitanProductUploadResult {
    private boolean success;
    private String message;
    private String itemId; // 聚水潭商品ID
    private List<JustuitanSkuUploadResult> skuResults;

    public JustuitanProductUploadResult() {
        this.skuResults = new ArrayList<>();
    }

    public JustuitanProductUploadResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.skuResults = new ArrayList<>();
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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public List<JustuitanSkuUploadResult> getSkuResults() {
        return skuResults;
    }

    public void setSkuResults(List<JustuitanSkuUploadResult> skuResults) {
        this.skuResults = skuResults;
    }
}