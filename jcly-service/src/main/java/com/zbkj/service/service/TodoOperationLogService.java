package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.TodoOperationLog;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

/**
 * <p>
 * 待办事项操作日志表 服务类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
public interface TodoOperationLogService extends IService<TodoOperationLog> {

    /**
     * 分页获取操作日志列表
     *
     * @param todoItemId       待办事项ID
     * @param operationType    操作类型
     * @param operatorId       操作人ID
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<TodoOperationLog> getPage(Integer todoItemId, String operationType, Integer operatorId, PageParamRequest pageParamRequest);

    /**
     * 创建操作日志
     *
     * @param operationLog 操作日志信息
     * @return Boolean
     */
    Boolean create(TodoOperationLog operationLog);

    /**
     * 根据待办事项ID获取操作日志
     *
     * @param todoItemId 待办事项ID
     * @return List
     */
    List<TodoOperationLog> getByTodoItemId(Integer todoItemId);

    /**
     * 根据操作人ID获取操作日志
     *
     * @param operatorId 操作人ID
     * @param limit      数量限制
     * @return List
     */
    List<TodoOperationLog> getByOperatorId(Integer operatorId, Integer limit);

    /**
     * 批量删除操作日志
     *
     * @param todoItemIds 待办事项ID列表
     * @return Boolean
     */
    Boolean batchDeleteByTodoItemIds(List<Integer> todoItemIds);

    /**
     * 清理过期的操作日志
     *
     * @param days 保留天数
     * @return Boolean
     */
    Boolean cleanExpiredLogs(Integer days);
}