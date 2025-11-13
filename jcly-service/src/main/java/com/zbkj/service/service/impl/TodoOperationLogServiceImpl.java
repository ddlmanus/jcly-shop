package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.TodoOperationLog;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.TodoOperationLogDao;
import com.zbkj.service.service.TodoOperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 待办事项操作日志表 服务实现类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Service
public class TodoOperationLogServiceImpl extends ServiceImpl<TodoOperationLogDao, TodoOperationLog> implements TodoOperationLogService {

    @Resource
    private TodoOperationLogDao dao;

    private final Logger logger = LoggerFactory.getLogger(TodoOperationLogServiceImpl.class);

    /**
     * 分页获取操作日志列表
     *
     * @param todoItemId       待办事项ID
     * @param operationType    操作类型
     * @param operatorId       操作人ID
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<TodoOperationLog> getPage(Integer todoItemId, String operationType, Integer operatorId, PageParamRequest pageParamRequest) {
        Page<TodoOperationLog> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<TodoOperationLog> lqw = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(todoItemId)) {
            lqw.eq(TodoOperationLog::getTodoItemId, todoItemId);
        }
        if (StrUtil.isNotBlank(operationType)) {
            lqw.eq(TodoOperationLog::getOperationType, operationType);
        }
        if (ObjectUtil.isNotNull(operatorId)) {
            lqw.eq(TodoOperationLog::getOperatorId, operatorId);
        }
        lqw.orderByDesc(TodoOperationLog::getCreateTime);
        List<TodoOperationLog> list = dao.selectList(lqw);
        return CommonPage.copyPageInfo(page, list);
    }

    /**
     * 创建操作日志
     *
     * @param operationLog 操作日志信息
     * @return Boolean
     */
    @Override
    public Boolean create(TodoOperationLog operationLog) {
        operationLog.setCreateTime(new Date());
        return save(operationLog);
    }

    /**
     * 根据待办事项ID获取操作日志
     *
     * @param todoItemId 待办事项ID
     * @return List
     */
    @Override
    public List<TodoOperationLog> getByTodoItemId(Integer todoItemId) {
        LambdaQueryWrapper<TodoOperationLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(TodoOperationLog::getTodoItemId, todoItemId);
        lqw.orderByDesc(TodoOperationLog::getCreateTime);
        return dao.selectList(lqw);
    }

    /**
     * 根据操作人ID获取操作日志
     *
     * @param operatorId 操作人ID
     * @param limit      数量限制
     * @return List
     */
    @Override
    public List<TodoOperationLog> getByOperatorId(Integer operatorId, Integer limit) {
        LambdaQueryWrapper<TodoOperationLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(TodoOperationLog::getOperatorId, operatorId);
        lqw.orderByDesc(TodoOperationLog::getCreateTime);
        lqw.last("LIMIT " + limit);
        return dao.selectList(lqw);
    }

    /**
     * 批量删除操作日志
     *
     * @param todoItemIds 待办事项ID列表
     * @return Boolean
     */
    @Override
    public Boolean batchDeleteByTodoItemIds(List<Integer> todoItemIds) {
        if (CollUtil.isEmpty(todoItemIds)) {
            return true;
        }
        LambdaQueryWrapper<TodoOperationLog> lqw = Wrappers.lambdaQuery();
        lqw.in(TodoOperationLog::getTodoItemId, todoItemIds);
        return remove(lqw);
    }

    /**
     * 清理过期的操作日志
     *
     * @param days 保留天数
     * @return Boolean
     */
    @Override
    public Boolean cleanExpiredLogs(Integer days) {
        Date expiredDate = DateUtil.offsetDay(new Date(), -days);
        LambdaQueryWrapper<TodoOperationLog> lqw = Wrappers.lambdaQuery();
        lqw.lt(TodoOperationLog::getCreateTime, expiredDate);
        int deletedCount = dao.delete(lqw);
        logger.info("清理过期操作日志完成，删除记录数: {}", deletedCount);
        return true;
    }
}