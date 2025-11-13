package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.MerchantTodoItem;
import com.zbkj.common.model.merchant.TodoOperationLog;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.service.dao.MerchantTodoItemDao;
import com.zbkj.service.service.MerchantTodoItemService;
import com.zbkj.service.service.TodoOperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户待办事项表 服务实现类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Service
public class MerchantTodoItemServiceImpl extends ServiceImpl<MerchantTodoItemDao, MerchantTodoItem> implements MerchantTodoItemService {

    @Resource
    private MerchantTodoItemDao dao;

    private final Logger logger = LoggerFactory.getLogger(MerchantTodoItemServiceImpl.class);

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TodoOperationLogService todoOperationLogService;

    /**
     * 分页获取待办事项列表
     *
     * @param merId            商户ID
     * @param type             事项类型
     * @param priority         优先级
     * @param status           状态
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<MerchantTodoItem> getPage(Integer merId, String type, Integer priority, Integer status, PageParamRequest pageParamRequest) {
        Page<MerchantTodoItem> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<MerchantTodoItem> lqw = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(merId)) {
            lqw.eq(MerchantTodoItem::getMerId, merId);
        }
        if (StrUtil.isNotBlank(type)) {
            lqw.eq(MerchantTodoItem::getType, type);
        }
        if (ObjectUtil.isNotNull(priority)) {
            lqw.eq(MerchantTodoItem::getPriority, priority);
        }
        if (ObjectUtil.isNotNull(status)) {
            lqw.eq(MerchantTodoItem::getStatus, status);
        }
        lqw.eq(MerchantTodoItem::getIsDelete, false);
        lqw.orderByDesc(MerchantTodoItem::getPriority);
        lqw.orderByDesc(MerchantTodoItem::getCreateTime);
        List<MerchantTodoItem> list = dao.selectList(lqw);
        return CommonPage.copyPageInfo(page, list);
    }

    /**
     * 创建待办事项
     *
     * @param todoItem 待办事项信息
     * @return Boolean
     */
    @Override
    public Boolean create(MerchantTodoItem todoItem) {
        todoItem.setCreateTime(new Date());
        todoItem.setUpdateTime(new Date());
        todoItem.setStatus(0); // 待处理
        todoItem.setIsDelete(false);
        
        return transactionTemplate.execute(e -> {
            boolean saveResult = save(todoItem);
            if (!saveResult) {
                logger.error("创建待办事项失败");
                e.setRollbackOnly();
                return false;
            }
            
            // 创建操作日志
            TodoOperationLog operationLog = new TodoOperationLog();
            operationLog.setTodoItemId(todoItem.getId());
            operationLog.setOperationType("create");
            operationLog.setOperatorId(todoItem.getMerId());
            operationLog.setOperatorType("merchant");
            operationLog.setRemark("创建待办事项");
            operationLog.setCreateTime(new Date());
            
            boolean logResult = todoOperationLogService.save(operationLog);
            if (!logResult) {
                logger.error("创建待办事项操作日志失败");
                e.setRollbackOnly();
                return false;
            }
            
            return true;
        });
    }

    /**
     * 标记事项为已完成
     *
     * @param id         事项ID
     * @param operatorId 操作人ID
     * @return Boolean
     */
    @Override
    public Boolean complete(Integer id, Integer operatorId) {
        MerchantTodoItem todoItem = getById(id);
        if (ObjectUtil.isNull(todoItem)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("待办事项不存在"));
        }
        if (todoItem.getStatus().equals(1)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("事项已完成"));
        }
        
        return transactionTemplate.execute(e -> {
            // 更新事项状态
            UpdateWrapper<MerchantTodoItem> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            updateWrapper.set("status", 1);
            updateWrapper.set("complete_time", new Date());
            updateWrapper.set("update_time", new Date());
            boolean updateResult = update(updateWrapper);
            
            if (!updateResult) {
                logger.error("更新待办事项状态失败，事项ID: {}", id);
                e.setRollbackOnly();
                return false;
            }
            
            // 创建操作日志
            TodoOperationLog operationLog = new TodoOperationLog();
            operationLog.setTodoItemId(id);
            operationLog.setOperationType("complete");
            operationLog.setOperatorId(operatorId);
            operationLog.setOperatorType("merchant");
            operationLog.setRemark("标记事项为已完成");
            operationLog.setCreateTime(new Date());
            
            boolean logResult = todoOperationLogService.save(operationLog);
            if (!logResult) {
                logger.error("创建待办事项操作日志失败");
                e.setRollbackOnly();
                return false;
            }
            
            return true;
        });
    }

    /**
     * 批量标记事项为已完成
     *
     * @param ids        事项ID列表
     * @param operatorId 操作人ID
     * @return Boolean
     */
    @Override
    public Boolean batchComplete(List<Integer> ids, Integer operatorId) {
        if (CollUtil.isEmpty(ids)) {
            return true;
        }
        
        return transactionTemplate.execute(e -> {
            // 批量更新事项状态
            UpdateWrapper<MerchantTodoItem> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", ids);
            updateWrapper.eq("status", 0); // 只更新待处理的事项
            updateWrapper.set("status", 1);
            updateWrapper.set("complete_time", new Date());
            updateWrapper.set("update_time", new Date());
            boolean updateResult = update(updateWrapper);
            
            if (!updateResult) {
                logger.error("批量更新待办事项状态失败");
                e.setRollbackOnly();
                return false;
            }
            
            // 为每个事项创建操作日志
            for (Integer id : ids) {
                TodoOperationLog operationLog = new TodoOperationLog();
                operationLog.setTodoItemId(id);
                operationLog.setOperationType("complete");
                operationLog.setOperatorId(operatorId);
                operationLog.setOperatorType("merchant");
                operationLog.setRemark("批量标记事项为已完成");
                operationLog.setCreateTime(new Date());
                
                boolean logResult = todoOperationLogService.save(operationLog);
                if (!logResult) {
                    logger.error("创建待办事项操作日志失败，事项ID: {}", id);
                    e.setRollbackOnly();
                    return false;
                }
            }
            
            return true;
        });
    }

    /**
     * 获取商户待办事项统计
     *
     * @param merId 商户ID
     * @return Map
     */
    @Override
    public Map<String, Object> getTodoStatistics(Integer merId) {
        Map<String, Object> result = new HashMap<>();
        
        // 总事项数
        LambdaQueryWrapper<MerchantTodoItem> totalLqw = Wrappers.lambdaQuery();
        totalLqw.eq(MerchantTodoItem::getMerId, merId);
        totalLqw.eq(MerchantTodoItem::getIsDelete, false);
        Integer totalCount = dao.selectCount(totalLqw);
        
        // 待处理事项数
        LambdaQueryWrapper<MerchantTodoItem> pendingLqw = Wrappers.lambdaQuery();
        pendingLqw.eq(MerchantTodoItem::getMerId, merId);
        pendingLqw.eq(MerchantTodoItem::getStatus, 0);
        pendingLqw.eq(MerchantTodoItem::getIsDelete, false);
        Integer pendingCount = dao.selectCount(pendingLqw);
        
        // 已完成事项数
        LambdaQueryWrapper<MerchantTodoItem> completedLqw = Wrappers.lambdaQuery();
        completedLqw.eq(MerchantTodoItem::getMerId, merId);
        completedLqw.eq(MerchantTodoItem::getStatus, 1);
        completedLqw.eq(MerchantTodoItem::getIsDelete, false);
        Integer completedCount = dao.selectCount(completedLqw);
        
        // 高优先级待处理事项数
        LambdaQueryWrapper<MerchantTodoItem> highPriorityLqw = Wrappers.lambdaQuery();
        highPriorityLqw.eq(MerchantTodoItem::getMerId, merId);
        highPriorityLqw.eq(MerchantTodoItem::getStatus, 0);
        highPriorityLqw.eq(MerchantTodoItem::getPriority, 3);
        highPriorityLqw.eq(MerchantTodoItem::getIsDelete, false);
        Integer highPriorityCount = dao.selectCount(highPriorityLqw);
        
        result.put("totalCount", totalCount);
        result.put("pendingCount", pendingCount);
        result.put("completedCount", completedCount);
        result.put("highPriorityCount", highPriorityCount);
        
        return result;
    }

    /**
     * 获取商户今日已办事项数量
     *
     * @param merId 商户ID
     * @return Integer
     */
    @Override
    public Integer getTodayCompletedCount(Integer merId) {
        String today = DateUtil.today();
        LambdaQueryWrapper<MerchantTodoItem> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantTodoItem::getMerId, merId);
        lqw.eq(MerchantTodoItem::getStatus, 1);
        lqw.eq(MerchantTodoItem::getIsDelete, false);
        lqw.apply("DATE(complete_time) = {0}", today);
        return dao.selectCount(lqw);
    }

    /**
     * 根据类型获取待办事项数量
     *
     * @param merId 商户ID
     * @param type  事项类型
     * @return Integer
     */
    @Override
    public Integer getCountByType(Integer merId, String type) {
        LambdaQueryWrapper<MerchantTodoItem> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantTodoItem::getMerId, merId);
        lqw.eq(MerchantTodoItem::getType, type);
        lqw.eq(MerchantTodoItem::getStatus, 0);
        lqw.eq(MerchantTodoItem::getIsDelete, false);
        return dao.selectCount(lqw);
    }

    /**
     * 根据ID获取待办事项详情
     *
     * @param id 事项ID
     * @return MerchantTodoItem
     */
    @Override
    public MerchantTodoItem getDetailById(Integer id) {
        MerchantTodoItem todoItem = getById(id);
        if (ObjectUtil.isNull(todoItem) || todoItem.getIsDelete()) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("待办事项不存在"));
        }
        return todoItem;
    }

    /**
     * 自动创建订单相关待办事项
     *
     * @param merId   商户ID
     * @param orderId 订单ID
     * @param type    事项类型
     * @return Boolean
     */
    @Override
    public Boolean createOrderTodoItem(Integer merId, String orderId, String type) {
        MerchantTodoItem todoItem = new MerchantTodoItem();
        todoItem.setMerId(merId);
        todoItem.setType(type);
        todoItem.setRelatedId(orderId);
        todoItem.setPriority(2); // 中等优先级
        
        switch (type) {
            case "order":
                todoItem.setTitle("新订单待处理");
                todoItem.setContent("订单号: " + orderId + " 需要处理");
                break;
            case "refund":
                todoItem.setTitle("退款申请待处理");
                todoItem.setContent("订单号: " + orderId + " 申请退款，需要处理");
                todoItem.setPriority(3); // 高优先级
                break;
            default:
                todoItem.setTitle("订单相关事项");
                todoItem.setContent("订单号: " + orderId + " 相关事项需要处理");
                break;
        }
        
        return create(todoItem);
    }

    /**
     * 自动创建商品相关待办事项
     *
     * @param merId     商户ID
     * @param productId 商品ID
     * @param type      事项类型
     * @return Boolean
     */
    @Override
    public Boolean createProductTodoItem(Integer merId, Integer productId, String type) {
        MerchantTodoItem todoItem = new MerchantTodoItem();
        todoItem.setMerId(merId);
        todoItem.setType(type);
        todoItem.setRelatedId(productId.toString());
        todoItem.setPriority(1); // 低优先级
        
        switch (type) {
            case "product":
                todoItem.setTitle("商品审核待处理");
                todoItem.setContent("商品ID: " + productId + " 需要审核");
                todoItem.setPriority(2); // 中等优先级
                break;
            default:
                todoItem.setTitle("商品相关事项");
                todoItem.setContent("商品ID: " + productId + " 相关事项需要处理");
                break;
        }
        
        return create(todoItem);
    }
}