package com.zbkj.service.listener;

import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.order.Order;
import com.zbkj.service.service.impl.WebSocketDataPushService;
import com.zbkj.service.util.DataChangeEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据看板事件监听器
 * 监听数据变更事件并触发大屏数据推送
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
public class DashboardDataChangeListener {

    @Autowired
    private WebSocketDataPushService webSocketDataPushService;

    // =================================================================================
    // 订单事件监听
    // =================================================================================

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getYearlySalesData", "getDistributorRanking", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "ORDER_CREATED");
            extraData.put("payPrice", event.getOrder().getPayPrice());
            extraData.put("orderNo", event.getOrder().getOrderNo());
            
            // 推送到WebSocket
            webSocketDataPushService.pushOrderChange("CREATE", event.getOrder().getId(), 
                event.getOrder().getMerId(), affectedMethods, extraData);
                
            log.debug("处理订单创建事件完成: orderId={}", event.getOrder().getId());
        } catch (Exception e) {
            log.error("处理订单创建事件失败: orderId={}", event.getOrder().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleOrderUpdated(OrderUpdatedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getDistributorRanking"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "ORDER_UPDATED");
            extraData.put("changeFields", event.getChangeFields());
            extraData.put("orderNo", event.getOrder().getOrderNo());
            
            webSocketDataPushService.pushOrderChange("UPDATE", event.getOrder().getId(), 
                event.getOrder().getMerId(), affectedMethods, extraData);
                
            log.debug("处理订单更新事件完成: orderId={}", event.getOrder().getId());
        } catch (Exception e) {
            log.error("处理订单更新事件失败: orderId={}", event.getOrder().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleOrderDeleted(OrderDeletedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getYearlySalesData", "getDistributorRanking", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "ORDER_DELETED");
            extraData.put("orderNo", event.getOrderNo());
            extraData.put("deleteTime", event.getDeleteTime());
            
            webSocketDataPushService.pushOrderChange("DELETE", event.getOrderId(), 
                event.getMerchantId(), affectedMethods, extraData);
                
            log.debug("处理订单删除事件完成: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("处理订单删除事件失败: orderId={}", event.getOrderId(), e);
        }
    }

    @Async
    @EventListener
    public void handleOrderPaid(OrderPaidEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getYearlySalesData", "getDistributorRanking", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "ORDER_PAID");
            extraData.put("payPrice", event.getOrder().getPayPrice());
            extraData.put("payTime", event.getOrder().getPayTime());
            extraData.put("orderNo", event.getOrder().getOrderNo());
            
            webSocketDataPushService.pushOrderChange("PAID", event.getOrder().getId(), 
                event.getOrder().getMerId(), affectedMethods, extraData);
                
            log.debug("处理订单支付事件完成: orderId={}", event.getOrder().getId());
        } catch (Exception e) {
            log.error("处理订单支付事件失败: orderId={}", event.getOrder().getId(), e);
        }
    }

    // =================================================================================
    // 商品事件监听
    // =================================================================================

    @Async
    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getCategoryRanking"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "PRODUCT_CREATED");
            extraData.put("categoryId", event.getProduct().getCateId());
            extraData.put("stock", event.getProduct().getStock());
            extraData.put("productName", event.getProduct().getName());
            
            webSocketDataPushService.pushProductChange("CREATE", event.getProduct().getId(), 
                event.getProduct().getMerId(), affectedMethods, extraData);
                
            log.debug("处理商品创建事件完成: productId={}", event.getProduct().getId());
        } catch (Exception e) {
            log.error("处理商品创建事件失败: productId={}", event.getProduct().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getCategoryRanking"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "PRODUCT_UPDATED");
            extraData.put("changeFields", event.getChangeFields());
            extraData.put("categoryId", event.getProduct().getCateId());
            extraData.put("productName", event.getProduct().getName());
            
            webSocketDataPushService.pushProductChange("UPDATE", event.getProduct().getId(), 
                event.getProduct().getMerId(), affectedMethods, extraData);
                
            log.debug("处理商品更新事件完成: productId={}", event.getProduct().getId());
        } catch (Exception e) {
            log.error("处理商品更新事件失败: productId={}", event.getProduct().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleProductDeleted(ProductDeletedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getCategoryRanking"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "PRODUCT_DELETED");
            extraData.put("productName", event.getProductName());
            
            webSocketDataPushService.pushProductChange("DELETE", event.getProductId(), 
                event.getMerchantId(), affectedMethods, extraData);
                
            log.debug("处理商品删除事件完成: productId={}", event.getProductId());
        } catch (Exception e) {
            log.error("处理商品删除事件失败: productId={}", event.getProductId(), e);
        }
    }

    // =================================================================================
    // 用户事件监听
    // =================================================================================

    @Async
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getYearlyUserData", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "USER_CREATED");
            extraData.put("registerType", event.getRegisterType());
            extraData.put("userLevel", event.getUser().getLevel());
            extraData.put("nickname", event.getUser().getNickname());
            
            webSocketDataPushService.pushUserChange("CREATE", event.getUser().getId(), 
                affectedMethods, extraData);
                
            log.debug("处理用户创建事件完成: userId={}", event.getUser().getId());
        } catch (Exception e) {
            log.error("处理用户创建事件失败: userId={}", event.getUser().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleUserUpdated(UserUpdatedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "USER_UPDATED");
            extraData.put("changeFields", event.getChangeFields());
            extraData.put("nickname", event.getUser().getNickname());
            
            webSocketDataPushService.pushUserChange("UPDATE", event.getUser().getId(),
                affectedMethods, extraData);
                
            log.debug("处理用户更新事件完成: userId={}", event.getUser().getId());
        } catch (Exception e) {
            log.error("处理用户更新事件失败: userId={}", event.getUser().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleUserDeleted(UserDeletedEvent event) {
        try {
            String[] affectedMethods = {"getOverviewData", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "USER_DELETED");
            extraData.put("nickname", event.getNickname());
            
            webSocketDataPushService.pushUserChange("DELETE", event.getUserId(), 
                affectedMethods, extraData);
                
            log.debug("处理用户删除事件完成: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("处理用户删除事件失败: userId={}", event.getUserId(), e);
        }
    }

    // =================================================================================
    // 商户事件监听
    // =================================================================================

    @Async
    @EventListener
    public void handleMerchantCreated(MerchantCreatedEvent event) {
        try {
            String[] affectedMethods = {"getDistributorRanking", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "MERCHANT_CREATED");
            extraData.put("merchantName", event.getMerchant().getName());
            extraData.put("categoryId", event.getMerchant().getCategoryId());
            
            webSocketDataPushService.pushMerchantChange("CREATE", event.getMerchant().getId(), 
                affectedMethods, extraData);
                
            log.debug("处理商户创建事件完成: merchantId={}", event.getMerchant().getId());
        } catch (Exception e) {
            log.error("处理商户创建事件失败: merchantId={}", event.getMerchant().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleMerchantUpdated(MerchantUpdatedEvent event) {
        try {
            String[] affectedMethods = {"getDistributorRanking", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "MERCHANT_UPDATED");
            extraData.put("changeFields", event.getChangeFields());
            extraData.put("merchantName", event.getMerchant().getName());
            
            webSocketDataPushService.pushMerchantChange("UPDATE", event.getMerchant().getId(), 
                affectedMethods, extraData);
                
            log.debug("处理商户更新事件完成: merchantId={}", event.getMerchant().getId());
        } catch (Exception e) {
            log.error("处理商户更新事件失败: merchantId={}", event.getMerchant().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleMerchantDeleted(MerchantDeletedEvent event) {
        try {
            String[] affectedMethods = {"getDistributorRanking", "getRegionSalesData"};
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", "MERCHANT_DELETED");
            extraData.put("merchantName", event.getMerchantName());
            
            webSocketDataPushService.pushMerchantChange("DELETE", event.getMerchantId(), 
                affectedMethods, extraData);
                
            log.debug("处理商户删除事件完成: merchantId={}", event.getMerchantId());
        } catch (Exception e) {
            log.error("处理商户删除事件失败: merchantId={}", event.getMerchantId(), e);
        }
    }

    // =================================================================================
    // 事件类定义
    // =================================================================================

    // 订单事件
    public static class OrderCreatedEvent {
        private Order order;
        
        public OrderCreatedEvent(Order order) {
            this.order = order;
        }
        
        public Order getOrder() { return order; }
    }

    public static class OrderUpdatedEvent {
        private Order order;
        private Map<String, Object> changeFields;
        
        public OrderUpdatedEvent(Order order, Map<String, Object> changeFields) {
            this.order = order;
            this.changeFields = changeFields;
        }
        
        public Order getOrder() { return order; }
        public Map<String, Object> getChangeFields() { return changeFields; }
    }

    public static class OrderDeletedEvent {
        private Integer orderId;
        private Integer merchantId;
        private String orderNo;
        private String deleteTime;
        
        public OrderDeletedEvent(Integer orderId, Integer merchantId, String orderNo, String deleteTime) {
            this.orderId = orderId;
            this.merchantId = merchantId;
            this.orderNo = orderNo;
            this.deleteTime = deleteTime;
        }
        
        public Integer getOrderId() { return orderId; }
        public Integer getMerchantId() { return merchantId; }
        public String getOrderNo() { return orderNo; }
        public String getDeleteTime() { return deleteTime; }
    }

    public static class OrderPaidEvent {
        private Order order;
        
        public OrderPaidEvent(Order order) {
            this.order = order;
        }
        
        public Order getOrder() { return order; }
    }

    // 商品事件
    public static class ProductCreatedEvent {
        private Product product;
        
        public ProductCreatedEvent(Product product) {
            this.product = product;
        }
        
        public Product getProduct() { return product; }
    }

    public static class ProductUpdatedEvent {
        private Product product;
        private Map<String, Object> changeFields;
        
        public ProductUpdatedEvent(Product product, Map<String, Object> changeFields) {
            this.product = product;
            this.changeFields = changeFields;
        }
        
        public Product getProduct() { return product; }
        public Map<String, Object> getChangeFields() { return changeFields; }
    }

    public static class ProductDeletedEvent {
        private Integer productId;
        private Integer merchantId;
        private String productName;
        private String deleteTime;
        
        public ProductDeletedEvent(Integer productId, Integer merchantId, String productName) {
            this.productId = productId;
            this.merchantId = merchantId;
            this.productName = productName;
            this.deleteTime = LocalDateTime.now().toString();
        }
        
        public Integer getProductId() { return productId; }
        public Integer getMerchantId() { return merchantId; }
        public String getProductName() { return productName; }
        public String getDeleteTime() { return deleteTime; }
    }

    // 用户事件
    public static class UserCreatedEvent {
        private User user;
        private String registerType;
        
        public UserCreatedEvent(User user, String registerType) {
            this.user = user;
            this.registerType = registerType;
        }
        
        public User getUser() { return user; }
        public String getRegisterType() { return registerType; }
    }

    public static class UserUpdatedEvent {
        private User user;
        private Map<String, Object> changeFields;
        
        public UserUpdatedEvent(User user, Map<String, Object> changeFields) {
            this.user = user;
            this.changeFields = changeFields;
        }
        
        public User getUser() { return user; }
        public Map<String, Object> getChangeFields() { return changeFields; }
    }

    public static class UserDeletedEvent {
        private Integer userId;
        private String nickname;
        private String deleteTime;
        
        public UserDeletedEvent(Integer userId, String nickname) {
            this.userId = userId;
            this.nickname = nickname;
            this.deleteTime = LocalDateTime.now().toString();
        }
        
        public Integer getUserId() { return userId; }
        public String getNickname() { return nickname; }
        public String getDeleteTime() { return deleteTime; }
    }

    // 商户事件
    public static class MerchantCreatedEvent {
        private Merchant merchant;
        
        public MerchantCreatedEvent(Merchant merchant) {
            this.merchant = merchant;
        }
        
        public Merchant getMerchant() { return merchant; }
    }

    public static class MerchantUpdatedEvent {
        private Merchant merchant;
        private Map<String, Object> changeFields;
        
        public MerchantUpdatedEvent(Merchant merchant, Map<String, Object> changeFields) {
            this.merchant = merchant;
            this.changeFields = changeFields;
        }
        
        public Merchant getMerchant() { return merchant; }
        public Map<String, Object> getChangeFields() { return changeFields; }
    }

    public static class MerchantDeletedEvent {
        private Integer merchantId;
        private String merchantName;
        private String deleteTime;
        
        public MerchantDeletedEvent(Integer merchantId, String merchantName) {
            this.merchantId = merchantId;
            this.merchantName = merchantName;
            this.deleteTime = LocalDateTime.now().toString();
        }
        
        public Integer getMerchantId() { return merchantId; }
        public String getMerchantName() { return merchantName; }
        public String getDeleteTime() { return deleteTime; }
    }
}