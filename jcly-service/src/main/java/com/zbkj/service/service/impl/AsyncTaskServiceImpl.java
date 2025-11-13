package com.zbkj.service.service.impl;

import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.service.service.AsyncTaskService;
import com.zbkj.service.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步任务服务实现类
 */
@Slf4j
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    @Autowired
    private ExportService exportService;

    // 存储任务状态和结果的Map
    private static final ConcurrentHashMap<String, Map<String, Object>> TASK_STATUS_MAP = new ConcurrentHashMap<>();

    @Override
    public String generateProductImportTemplateAsync() {
        // 生成唯一任务ID
        String taskId = UUID.randomUUID().toString();
        
        // 初始化任务状态
        Map<String, Object> statusMap = new ConcurrentHashMap<>();
        statusMap.put("status", "processing");
        statusMap.put("progress", 0);
        statusMap.put("fileUrl", "");
        statusMap.put("message", "任务正在处理中");
        
        // 确保taskId和statusMap都不为null再放入Map
        if (taskId != null && statusMap != null) {
            TASK_STATUS_MAP.put(taskId, statusMap);
        } else {
            throw new CrmebException(CommonResultCode.ERROR, "任务初始化失败");
        }
        
        // 异步执行任务
        executeTemplateGenerationTask(taskId);
        
        return taskId;
    }

    @Override
    public Map<String, Object> getTaskStatus(String taskId) {
        // 检查taskId是否为null
        if (taskId == null || taskId.isEmpty()) {
            Map<String, Object> notFoundStatus = new ConcurrentHashMap<>();
            notFoundStatus.put("status", "not_found");
            notFoundStatus.put("message", "任务ID不能为空");
            return notFoundStatus;
        }
        
        Map<String, Object> status = TASK_STATUS_MAP.get(taskId);
        if (status == null) {
            Map<String, Object> notFoundStatus = new ConcurrentHashMap<>();
            notFoundStatus.put("status", "not_found");
            notFoundStatus.put("message", "任务不存在或已过期");
            return notFoundStatus;
        }
        return new HashMap<>(status); // 返回副本避免外部修改
    }

    @Async("taskExecutor")
    protected void executeTemplateGenerationTask(String taskId) {
        // 检查taskId是否为null
        if (taskId == null || taskId.isEmpty()) {
            log.error("任务ID为空，无法执行模板生成任务");
            return;
        }
        
        Map<String, Object> statusMap = TASK_STATUS_MAP.get(taskId);
        // 检查statusMap是否为null
        if (statusMap == null) {
            log.error("未找到任务状态信息，任务ID: {}", taskId);
            return;
        }
        
        try {
            // 更新进度
            statusMap.put("progress", 30);
            statusMap.put("message", "正在生成模板...");
            
            // 调用导出服务生成模板
            String fileUrl = exportService.generateProductImportTemplate();
            
            // 更新任务状态为完成
            statusMap.put("status", "completed");
            statusMap.put("progress", 100);
            statusMap.put("fileUrl", fileUrl != null ? fileUrl : "");
            statusMap.put("message", "模板生成成功");
            
            log.info("商品导入模板生成成功，任务ID: {}, 文件URL: {}", taskId, fileUrl);
        } catch (Exception e) {
            // 更新任务状态为失败
            statusMap.put("status", "failed");
            statusMap.put("message", "模板生成失败: " + e.getMessage());
            log.error("商品导入模板生成失败，任务ID: " + taskId, e);
        }
    }
}