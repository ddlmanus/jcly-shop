package com.zbkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.DataDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket数据推送服务
 * 专门负责向大屏系统推送数据变更通知
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
public class WebSocketDataPushService {

    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private DataDashboardService dataDashboardService;
    
    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("cozeRestTemplate")
    private RestTemplate restTemplate;

    // WebSocket客户端连接
    private DashboardWebSocketClient webSocketClient;
    
    // 重连定时器
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
    
    // 默认WebSocket地址
    private static final String DEFAULT_WS_URL = "wss://shop.jclyyun.com/websocket/human-service";
    private static final String WS_URL_CONFIG_KEY = "dashboard_websocket_url";
    private static final String WS_ENABLED_CONFIG_KEY = "dashboard_websocket_enabled";
    
    // 外部推送接口地址
    private static final String EXTERNAL_PUSH_URL = "external_push_url";
    // 外部推送接口方法路径
    private static final String EXTERNAL_PUSH_METHOD_PATH = "external_push_method_path";


    @PostConstruct
    public void init() {
        try {
            // 延迟初始化，确保系统配置服务已经启动
            reconnectScheduler.schedule(this::initWebSocketConnection, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("WebSocket推送服务初始化失败", e);
        }
    }

    /**
     * 初始化WebSocket连接
     */
    private void initWebSocketConnection() {
        try {
            if (!isWebSocketEnabled()) {
                log.info("WebSocket推送功能已禁用");
                return;
            }

            String wsUrl = getWebSocketUrl();
            connectToWebSocket(wsUrl);
            
            // 启动定时检查重连任务
            startReconnectTask();
            
        } catch (Exception e) {
            log.error("初始化WebSocket连接失败，将在下次定时检查时重试", e);
        }
    }

    /**
     * 连接到WebSocket服务器
     */
    private void connectToWebSocket(String wsUrl) {
        try {
            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.close();
            }

            URI serverURI = new URI(wsUrl);
            webSocketClient = new DashboardWebSocketClient(serverURI);
            
            boolean connected = webSocketClient.connectBlocking(10, TimeUnit.SECONDS);
            
            if (connected) {
                log.info("WebSocket连接成功: {}", wsUrl);
            } else {
                log.error("WebSocket连接失败: {}", wsUrl);
            }
            
        } catch (Exception e) {
            log.error("连接WebSocket失败: {}", wsUrl, e);
        }
    }

    /**
     * 启动重连任务
     */
    private void startReconnectTask() {
        reconnectScheduler.scheduleWithFixedDelay(() -> {
            try {
                if (!isWebSocketEnabled()) {
                    return;
                }
                
                if (webSocketClient == null || webSocketClient.isClosed()) {
                    log.info("检测到WebSocket连接断开，尝试重连...");
                    String wsUrl = getWebSocketUrl();
                    connectToWebSocket(wsUrl);
                }
            } catch (Exception e) {
                log.error("WebSocket重连检查失败", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 推送订单变更数据
     */
    @Async
    public void pushOrderChange(String action, Integer orderId, Integer merchantId, String[] affectedMethods, Map<String, Object> extraData) {
        log.info("推送订单变更数据: {},{},推送数据", action, orderId, extraData);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "ORDER");
//        data.put("action", action);
//        data.put("orderId", orderId);
//        data.put("merchantId", merchantId);
//        data.put("affectedMethods", affectedMethods);
//        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//
        if (extraData != null) {
            data.putAll(extraData);
        }

        pushData(data);
    }

    /**
     * 推送商品变更数据
     */
    @Async
    public void pushProductChange(String action, Integer productId, Integer merchantId, String[] affectedMethods, Map<String, Object> extraData) {
        log.info("推送商品变更数据: {},{},推送数据", action, productId, extraData);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "PRODUCT");
//        data.put("action", action);
//        data.put("productId", productId);
//        data.put("merchantId", merchantId);
//        data.put("affectedMethods", affectedMethods);
//        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//
        if (extraData != null) {
            data.putAll(extraData);
        }

        pushData(data);
    }

    /**
     * 推送用户变更数据
     */
    @Async
    public void pushUserChange(String action, Integer userId, String[] affectedMethods, Map<String, Object> extraData) {
        log.info("推送用户变更数据: {},{},推送数据", action, userId, extraData);
        Map<String, Object> data = new HashMap<>();
//        data.put("type", "USER");
//        data.put("action", action);
//        data.put("userId", userId);
//        data.put("affectedMethods", affectedMethods);
//        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//
        if (extraData != null) {
            data.putAll(extraData);
        }

        pushData(data);
    }

    /**
     * 推送商户变更数据
     */
    @Async
    public void pushMerchantChange(String action, Integer merchantId, String[] affectedMethods, Map<String, Object> extraData) {
        log.info("推送商户变更数据: {},{},推送数据", action, merchantId, extraData);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "MERCHANT");
//        data.put("action", action);
//        data.put("merchantId", merchantId);
//        data.put("affectedMethods", affectedMethods);
//        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//
        if (extraData != null) {
            data.putAll(extraData);
        }

        pushData(data);
    }

    /**
     * 核心推送方法 - 改为HTTP调用外部接口
     */
    private void pushData(Map<String, Object> data) {
        try {
            log.info("核心推送方法: {}", data);
            if (!isWebSocketEnabled()) {
                log.debug("推送功能已禁用");
                return;
            }

            // 根据数据类型调用对应的DataDashboardService接口
            String dataType = (String) data.get("type");
            com.alibaba.fastjson.JSONObject dashboardData = callDataDashboardService(dataType, data);
            
            if (dashboardData == null) {
                log.warn("未获取到仪表板数据，跳过推送: type={}", dataType);
                return;
            }

            // 组装最终推送的大JSON对象
            com.alibaba.fastjson.JSONObject pushPayload = new com.alibaba.fastjson.JSONObject();
//            pushPayload.put("sourceUrl", getSourceUrl());
//            pushPayload.put("sourceSystem", "jcly-shop");
//            pushPayload.put("sourceMethod", getSourceMethod(dataType));
//            pushPayload.put("sourceData", data);
            pushPayload.put("data", dashboardData);
            pushPayload.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // HTTP调用外部接口
            pushToExternalSystem(pushPayload);

            log.info("HTTP推送成功: type={}, action={}", data.get("type"), data.get("action"));

        } catch (Exception e) {
            log.error("推送失败: {}", data, e);
        }
    }
    
    /**
     * 根据数据类型调用对应的DataDashboardService接口
     */
    private com.alibaba.fastjson.JSONObject callDataDashboardService(String dataType, Map<String, Object> data) {
        try {
            int currentYear = LocalDateTime.now().getYear();
            int currentMonth = LocalDateTime.now().getMonthValue();
            
            switch (dataType) {
                case "ORDER":
                    // 订单相关数据变更，获取概览数据和年度销售数据
                    com.alibaba.fastjson.JSONObject orderResult = new com.alibaba.fastjson.JSONObject();
                    orderResult.put("shopBasicData", dataDashboardService.getOverviewData(currentYear, currentMonth));
                    orderResult.put("shopRevenue", dataDashboardService.getYearlySalesData(currentYear, "sales"));
                    orderResult.put("shopRevenue", dataDashboardService.getYearlySalesData(currentYear, "orders"));
                    orderResult.put("shopStoreRank", dataDashboardService.getDistributorRanking(currentYear, currentMonth, 20, "sales"));
                    orderResult.put("shopMap", dataDashboardService.getRegionSalesData(currentYear, currentMonth, null, "sales"));
                    return orderResult;
                    
                case "PRODUCT":
                    // 商品相关数据变更，获取分类排行和概览数据
                    com.alibaba.fastjson.JSONObject productResult = new com.alibaba.fastjson.JSONObject();
                    productResult.put("shopBasicData", dataDashboardService.getOverviewData(currentYear, currentMonth));
                    productResult.put("shopBrandRank", dataDashboardService.getCategoryRanking(currentYear, 20));
                    return productResult;
                    
                case "USER":
                    // 用户相关数据变更，获取用户数据和地区数据
                    com.alibaba.fastjson.JSONObject userResult = new com.alibaba.fastjson.JSONObject();
                    userResult.put("shopBasicData", dataDashboardService.getOverviewData(currentYear, currentMonth));
                    userResult.put("shopAddUser", dataDashboardService.getYearlyUserData(currentYear));
                    userResult.put("shopMap", dataDashboardService.getRegionSalesData(currentYear, currentMonth, null, "users"));
                    return userResult;
                    
                case "MERCHANT":
                    // 商户相关数据变更，获取分销商排行和地区数据
                    com.alibaba.fastjson.JSONObject merchantResult = new com.alibaba.fastjson.JSONObject();
                    merchantResult.put("shopStoreRank", dataDashboardService.getDistributorRanking(currentYear, currentMonth, 20, "sales"));
                    merchantResult.put("shopMap", dataDashboardService.getRegionSalesData(currentYear, currentMonth, null, "merchants"));
                    return merchantResult;
                    
                default:
                    log.warn("未知的数据类型: {}", dataType);
                    return null;
            }
        } catch (Exception e) {
            log.error("调用DataDashboardService失败: dataType={}", dataType, e);
            return null;
        }
    }
    
    /**
     * HTTP推送到外部系统
     */
    private void pushToExternalSystem(com.alibaba.fastjson.JSONObject payload) {
        String apiUrl= systemConfigService.getValueByKeyException(EXTERNAL_PUSH_URL);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(payload.toJSONString(), headers);
            String method = systemConfigService.getValueByKeyException(EXTERNAL_PUSH_METHOD_PATH);
            //请求方法路径
            apiUrl= apiUrl+method;
            log.info("HTTP调用外部系统，URL: {}, 请求参数: {}", apiUrl, payload);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("外部系统推送成功: {}", apiUrl);
            } else {
                log.error("外部系统推送失败: status={}, body={}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("HTTP推送到外部系统失败: url={}", apiUrl, e);
        }
    }
    
    /**
     * 获取来源URL
     */
    private String getSourceUrl() {
        // 可以从配置中获取，这里写死
        return "https://shop.jclyyun.com/admin";
    }
    
    /**
     * 根据数据类型获取来源方法
     */
    private String getSourceMethod(String dataType) {
        switch (dataType) {
            case "ORDER":
                return "DataDashboardService.getOverviewData,getYearlySalesData,getDistributorRanking,getRegionSalesData";
            case "PRODUCT":
                return "DataDashboardService.getOverviewData,getCategoryRanking";
            case "USER":
                return "DataDashboardService.getOverviewData,getYearlyUserData,getRegionSalesData";
            case "MERCHANT":
                return "DataDashboardService.getDistributorRanking,getRegionSalesData";
            default:
                return "DataDashboardService.unknown";
        }
    }

    /**
     * 获取WebSocket地址
     */
    private String getWebSocketUrl() {
        try {
            String wsUrl = systemConfigService.getValueByKey(WS_URL_CONFIG_KEY);
            if (StrUtil.isBlank(wsUrl)) {
                wsUrl = DEFAULT_WS_URL;
                // 保存默认配置
                systemConfigService.updateValueByKey(WS_URL_CONFIG_KEY, wsUrl);
            }
            return wsUrl;
        } catch (Exception e) {
            log.error("获取WebSocket地址失败，使用默认地址", e);
            return DEFAULT_WS_URL;
        }
    }

    /**
     * 检查WebSocket推送是否启用
     */
    private boolean isWebSocketEnabled() {
        try {
            String enabled = systemConfigService.getValueByKey(WS_ENABLED_CONFIG_KEY);
            if (StrUtil.isBlank(enabled)) {
                // 默认启用
                systemConfigService.updateValueByKey(WS_ENABLED_CONFIG_KEY, "1");
                return true;
            }
            return "1".equals(enabled) || "true".equalsIgnoreCase(enabled);
        } catch (Exception e) {
            log.error("检查WebSocket启用状态失败", e);
            return true; // 默认启用
        }
    }

    /**
     * 手动重连WebSocket
     */
    public boolean reconnectWebSocket() {
        try {
            String wsUrl = getWebSocketUrl();
            connectToWebSocket(wsUrl);
            return webSocketClient != null && webSocketClient.isOpen();
        } catch (Exception e) {
            log.error("手动重连WebSocket失败", e);
            return false;
        }
    }

    /**
     * 获取WebSocket连接状态
     */
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean isEnabled = isWebSocketEnabled();
            String wsUrl = getWebSocketUrl();
            boolean isConnected = webSocketClient != null && webSocketClient.isOpen();
            
            status.put("enabled", isEnabled);
            status.put("websocketUrl", wsUrl);
            status.put("connected", isConnected);
            status.put("lastCheckTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            if (webSocketClient != null) {
                status.put("clientStatus", webSocketClient.isClosed() ? "CLOSED" : 
                    (webSocketClient.isOpen() ? "OPEN" : "CONNECTING"));
            } else {
                status.put("clientStatus", "NOT_INITIALIZED");
            }
            
        } catch (Exception e) {
            status.put("error", e.getMessage());
            status.put("connected", false);
            log.error("获取WebSocket连接状态失败", e);
        }
        
        return status;
    }

    /**
     * 推送自定义数据
     */
    @Async
    public void pushCustomData(String dataType, String action, String[] affectedMethods, Map<String, Object> extraData) {
        log.info("推送自定义数据: {},{},推送数据", dataType, action, extraData);
        Map<String, Object> data = new HashMap<>();
        data.put("type", dataType);
//        data.put("action", action);
//        data.put("affectedMethods", affectedMethods);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        if (extraData != null) {
            data.putAll(extraData);
        }

        pushData(data);
    }

    @PreDestroy
    public void destroy() {
        try {
            if (webSocketClient != null) {
                webSocketClient.close();
            }
            reconnectScheduler.shutdown();
            log.info("WebSocket推送服务已关闭");
        } catch (Exception e) {
            log.error("关闭WebSocket推送服务失败", e);
        }
    }

    /**
     * WebSocket客户端实现类
     */
    private class DashboardWebSocketClient extends WebSocketClient {

        public DashboardWebSocketClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            log.info("WebSocket连接已打开: status={}", handshake.getHttpStatus());
        }

        @Override
        public void onMessage(String message) {
            log.debug("收到WebSocket消息: {}", message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.warn("WebSocket连接已关闭: code={}, reason={}, remote={}", code, reason, remote);
        }

        @Override
        public void onError(Exception ex) {
            log.error("WebSocket连接出错", ex);
        }
    }
}