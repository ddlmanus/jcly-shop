package com.zbkj.service.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步任务服务接口
 */
public interface AsyncTaskService {

    /**
     * 异步生成商品导入模板
     * @return 任务ID
     */
    String generateProductImportTemplateAsync();

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> getTaskStatus(String taskId);
}