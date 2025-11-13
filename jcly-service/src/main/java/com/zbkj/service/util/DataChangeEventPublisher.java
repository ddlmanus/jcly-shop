package com.zbkj.service.util;

import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.service.listener.DashboardDataChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据变更事件发布器
 * 用于在Service层发布数据变更事件，触发数据推送
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
@Component
public class DataChangeEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // =================================================================================
    // 订单事件发布
    // =================================================================================

    /**
     * 发布订单创建事件
     */
    public void publishOrderCreated(Order order) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.OrderCreatedEvent(order));
            log.debug("发布订单创建事件: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("发布订单创建事件失败", e);
        }
    }

    /**
     * 发布订单更新事件
     */
    public void publishOrderUpdated(Order order, Map<String, Object> changeFields) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.OrderUpdatedEvent(order, changeFields));
            log.debug("发布订单更新事件: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("发布订单更新事件失败", e);
        }
    }

    /**
     * 发布订单删除事件
     */
    public void publishOrderDeleted(Integer orderId, Integer merchantId, String orderNo, String deleteTime) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.OrderDeletedEvent(orderId, merchantId, orderNo, deleteTime));
            log.debug("发布订单删除事件: orderId={}", orderId);
        } catch (Exception e) {
            log.error("发布订单删除事件失败", e);
        }
    }

    /**
     * 发布订单支付事件
     */
    public void publishOrderPaid(Order order) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.OrderPaidEvent(order));
            log.debug("发布订单支付事件: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("发布订单支付事件失败", e);
        }
    }

    // =================================================================================
    // 商品事件发布
    // =================================================================================

    /**
     * 发布商品创建事件
     */
    public void publishProductCreated(Product product) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.ProductCreatedEvent(product));
            log.debug("发布商品创建事件: productId={}", product.getId());
        } catch (Exception e) {
            log.error("发布商品创建事件失败", e);
        }
    }

    /**
     * 发布商品更新事件
     */
    public void publishProductUpdated(Product product, Map<String, Object> changeFields) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.ProductUpdatedEvent(product, changeFields));
            log.debug("发布商品更新事件: productId={}", product.getId());
        } catch (Exception e) {
            log.error("发布商品更新事件失败", e);
        }
    }

    /**
     * 发布商品删除事件
     */
    public void publishProductDeleted(Integer productId, Integer merchantId, String productName) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.ProductDeletedEvent(productId, merchantId, productName));
            log.debug("发布商品删除事件: productId={}", productId);
        } catch (Exception e) {
            log.error("发布商品删除事件失败", e);
        }
    }

    // =================================================================================
    // 用户事件发布
    // =================================================================================

    /**
     * 发布用户创建事件
     */
    public void publishUserCreated(User user, String registerType) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.UserCreatedEvent(user, registerType));
            log.debug("发布用户创建事件: userId={}", user.getId());
        } catch (Exception e) {
            log.error("发布用户创建事件失败", e);
        }
    }

    /**
     * 发布用户更新事件
     */
    public void publishUserUpdated(User user, Map<String, Object> changeFields) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.UserUpdatedEvent(user, changeFields));
            log.debug("发布用户更新事件: userId={}", user.getId());
        } catch (Exception e) {
            log.error("发布用户更新事件失败", e);
        }
    }

    /**
     * 发布用户删除事件
     */
    public void publishUserDeleted(Integer userId, String nickname) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.UserDeletedEvent(userId, nickname));
            log.debug("发布用户删除事件: userId={}", userId);
        } catch (Exception e) {
            log.error("发布用户删除事件失败", e);
        }
    }

    // =================================================================================
    // 商户事件发布
    // =================================================================================

    /**
     * 发布商户创建事件
     */
    public void publishMerchantCreated(Merchant merchant) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.MerchantCreatedEvent(merchant));
            log.debug("发布商户创建事件: merchantId={}", merchant.getId());
        } catch (Exception e) {
            log.error("发布商户创建事件失败", e);
        }
    }

    /**
     * 发布商户更新事件
     */
    public void publishMerchantUpdated(Merchant merchant, Map<String, Object> changeFields) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.MerchantUpdatedEvent(merchant, changeFields));
            log.debug("发布商户更新事件: merchantId={}", merchant.getId());
        } catch (Exception e) {
            log.error("发布商户更新事件失败", e);
        }
    }

    /**
     * 发布商户删除事件
     */
    public void publishMerchantDeleted(Integer merchantId, String merchantName) {
        try {
            eventPublisher.publishEvent(new DashboardDataChangeListener.MerchantDeletedEvent(merchantId, merchantName));
            log.debug("发布商户删除事件: merchantId={}", merchantId);
        } catch (Exception e) {
            log.error("发布商户删除事件失败", e);
        }
    }

    // =================================================================================
    // 批量事件发布
    // =================================================================================

    /**
     * 批量发布订单事件（用于批量操作）
     */
    public void publishBatchOrderEvents(java.util.List<Order> orders, String action) {
        for (Order order : orders) {
            switch (action.toUpperCase()) {
                case "CREATE":
                    publishOrderCreated(order);
                    break;
                case "UPDATE":
                    publishOrderUpdated(order, null);
                    break;
                case "PAID":
                    publishOrderPaid(order);
                    break;
            }
        }
    }

    /**
     * 批量发布商品事件（用于批量操作）
     */
    public void publishBatchProductEvents(java.util.List<Product> products, String action) {
        for (Product product : products) {
            switch (action.toUpperCase()) {
                case "CREATE":
                    publishProductCreated(product);
                    break;
                case "UPDATE":
                    publishProductUpdated(product, null);
                    break;
            }
        }
    }

    /**
     * 批量发布用户事件（用于批量操作）
     */
    public void publishBatchUserEvents(java.util.List<User> users, String action) {
        for (User user : users) {
            switch (action.toUpperCase()) {
                case "CREATE":
                    publishUserCreated(user, "BATCH");
                    break;
                case "UPDATE":
                    publishUserUpdated(user, null);
                    break;
            }
        }
    }
}