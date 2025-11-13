package com.zbkj.service.service.impl;

import com.zbkj.common.model.order.Order;
import com.zbkj.service.util.DataChangeEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单Service扩展示例
 * 展示如何在现有Service中集成数据推送功能
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
@Slf4j
@Service
public class OrderServiceExtension {

    @Autowired
    private DataChangeEventPublisher eventPublisher;

    /**
     * 订单创建示例（集成数据推送）
     */
    public void createOrderWithDataPush(Order order) {
        try {
            // 1. 执行原有的订单创建逻辑
            // ... 原有代码 ...
            
            // 2. 发布订单创建事件（触发数据推送）
            eventPublisher.publishOrderCreated(order);
            
            log.info("订单创建完成并触发数据推送: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("订单创建失败", e);
            throw e;
        }
    }

    /**
     * 订单状态更新示例（集成数据推送）
     */
    public void updateOrderStatusWithDataPush(Order order, Integer newStatus) {
        try {
            // 1. 记录变更字段
            Integer oldStatus = order.getStatus();
            Map<String, Object> changeFields = new HashMap<>();
            Map<String, Object> oldMap = new HashMap<>();
            oldMap.put("old", oldStatus);
            changeFields.put("new", newStatus);
            changeFields.put("status", oldMap);
            
            // 2. 执行原有的状态更新逻辑
            order.setStatus(newStatus);
            // ... 原有保存代码 ...
            
            // 3. 发布订单更新事件（触发数据推送）
            eventPublisher.publishOrderUpdated(order, changeFields);
            
            log.info("订单状态更新完成并触发数据推送: orderId={}, oldStatus={}, newStatus={}", 
                    order.getId(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("订单状态更新失败", e);
            throw e;
        }
    }

    /**
     * 订单支付成功示例（集成数据推送）
     */
    public void markOrderPaidWithDataPush(Order order) {
        try {
            // 1. 执行原有的支付成功逻辑
            order.setPaid(true);
            // ... 原有保存代码 ...
            
            // 2. 发布订单支付事件（触发数据推送）
            eventPublisher.publishOrderPaid(order);
            
            log.info("订单支付完成并触发数据推送: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("订单支付处理失败", e);
            throw e;
        }
    }

    /**
     * 订单删除示例（集成数据推送）
     */
    public void deleteOrderWithDataPush(Integer orderId, Integer merchantId, String orderNo) {
        try {
            // 1. 执行原有的删除逻辑
            // ... 原有删除代码 ...
            
            // 2. 发布订单删除事件（触发数据推送）
            eventPublisher.publishOrderDeleted(orderId, merchantId, orderNo, 
                    java.time.LocalDateTime.now().toString());
            
            log.info("订单删除完成并触发数据推送: orderId={}", orderId);
        } catch (Exception e) {
            log.error("订单删除失败", e);
            throw e;
        }
    }
}