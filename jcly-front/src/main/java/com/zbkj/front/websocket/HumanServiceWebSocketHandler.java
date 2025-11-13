package com.zbkj.front.websocket;

import com.alibaba.fastjson.JSON;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.request.coze.CozeCreateMessageRequest;
import com.zbkj.common.response.coze.CozeCreateMessageResponse;
import com.zbkj.service.service.HumanServiceWebSocketService;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.UnifiedChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 人工客服WebSocket处理器 - 前端模块
 * 企业级WebSocket实现，支持：
 * - 消息隔离和会话管理
 * - 实时双向通信
 * - 安全认证和权限控制
 * - 心跳检测和断线重连
 * - 异常处理和日志记录
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Component
public class HumanServiceWebSocketHandler implements WebSocketHandler {

    @Autowired
    private HumanServiceWebSocketService humanServiceWebSocketService;

    @Autowired
    private CozeService cozeService;

    @Autowired
    private UnifiedChatService unifiedChatService;


    // 存储WebSocket连接 - sessionId -> WebSocketSession
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 存储用户ID与WebSocket SessionId的映射 - userId -> sessionId
    private static final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    // 存储聊天会话ID与WebSocket SessionId的映射 - chatSessionId -> sessionId
    private static final Map<String, String> chatSessionMap = new ConcurrentHashMap<>();

    // 心跳检测调度器
    private static final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(2);

    // 用户最后活跃时间 - sessionId -> timestamp
    private static final Map<String, Long> lastActiveTime = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();

        // 从握手拦截器获取用户信息
        Integer userId = (Integer) session.getAttributes().get("userId");
        String userName = (String) session.getAttributes().get("userName");
        String userType = (String) session.getAttributes().get("userType");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");

        if (userId == null) {
            log.error("WebSocket连接失败: 用户信息缺失 - sessionId: {}", sessionId);
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // 检查是否已有连接，如果有则关闭旧连接
        String oldSessionId = userSessionMap.get(userId.toString());
        if (oldSessionId != null && sessions.containsKey(oldSessionId)) {
            WebSocketSession oldSession = sessions.get(oldSessionId);
            try {
                oldSession.close(CloseStatus.GOING_AWAY);
                log.info("关闭用户旧连接: userId={}, oldSessionId={}", userId, oldSessionId);
            } catch (Exception e) {
                log.warn("关闭旧连接失败: {}", e.getMessage());
            }
        }

        // 存储新连接
        sessions.put(sessionId, session);
        userSessionMap.put(userId.toString(), sessionId);
        if (chatSessionId != null) {
            chatSessionMap.put(chatSessionId, sessionId);
        }
        lastActiveTime.put(sessionId, System.currentTimeMillis());

        // 小程序用户登录时已为在线状态，无需更新
        log.debug("小程序用户连接: userId={}, type={}", userId, userType);

        // 发送连接成功消息
        Map<String, Object> welcomeData = new HashMap<>();
        welcomeData.put("userId", userId);
        welcomeData.put("userName", userName);
        welcomeData.put("sessionId", chatSessionId != null ? chatSessionId : "");
        welcomeData.put("serverTime", System.currentTimeMillis());
        // 添加心跳配置信息，让小程序了解服务端的心跳要求
        welcomeData.put("heartbeatInterval", 240000); // 建议小程序每4分钟发送一次心跳
        welcomeData.put("sessionTimeout", 300000);    // 会话超时时间5分钟
        Map<String, Object> welcomeMessage = createSuccessMessage("connection_established", welcomeData);
        sendMessage(session, welcomeMessage);

        // 启动心跳检测
        startHeartbeatCheck(sessionId);

        log.info("前端WebSocket连接建立成功: userId={}, userName={}, sessionId={}, chatSessionId={}",
                userId, userName, sessionId, chatSessionId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        Integer userId = (Integer) session.getAttributes().get("userId");

        // 更新最后活跃时间
        lastActiveTime.put(sessionId, System.currentTimeMillis());


        try {
            String payload = message.getPayload().toString();
            log.debug("收到WebSocket消息: sessionId={}, userId={}, payload={}", sessionId, userId, payload);

            Map<String, Object> messageData = JSON.parseObject(payload, Map.class);
            String messageType = (String) messageData.get("type");

            switch (messageType) {
                case "heartbeat":
                    handleHeartbeat(session, messageData);
                    break;
                case "send_message":
                    handleSendMessage(session, messageData);
                    break;
                case "send_image":
                    handleSendImage(session, messageData);
                    break;
                case "send_product":
                    handleSendProduct(session, messageData);
                    break;
                case "send_order":
                    handleSendOrder(session, messageData);
                    break;
                case "quick_reply":
                    handleQuickReply(session, messageData);
                    break;
                case "typing_status":
                    handleTypingStatus(session, messageData);
                    break;
                case "read_message":
                    handleReadMessage(session, messageData);
                    break;
                case "get_quick_replies":
                    handleGetQuickReplies(session, messageData);
                    break;
                case "search_products":
                    handleSearchProducts(session, messageData);
                    break;
                default:
                    log.warn("未知的消息类型: type={}, sessionId={}", messageType, sessionId);
                    sendErrorMessage(session, "未知的消息类型: " + messageType);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}, userId={}, 错误: {}", sessionId, userId, e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        Integer userId = (Integer) session.getAttributes().get("userId");

        log.error("前端WebSocket传输错误: sessionId={}, userId={}, 错误: {}",
                sessionId, userId, exception.getMessage(), exception);

        // 清理连接
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        Integer userId = (Integer) session.getAttributes().get("userId");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");

        log.info("前端WebSocket连接关闭: sessionId={}, userId={}, chatSessionId={}, 状态: {}",
                sessionId, userId, chatSessionId, closeStatus.getCode());

        // 清理连接
        cleanupSession(session);

        // 小程序用户WebSocket断开不代表离线，无需更新状态
        if (userId != null) {
            log.debug("小程序用户WebSocket断开: userId={}", userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");

        // 更新心跳时间
        if (userId != null) {
            try {
                humanServiceWebSocketService.updateHeartbeat(userId);
                log.debug("用户心跳更新成功: userId={}", userId);
            } catch (Exception e) {
                log.warn("更新心跳失败: userId={}, 错误: {}", userId, e.getMessage());
            }
        }

        // 回复心跳响应，包含连接状态信息
        Map<String, Object> heartbeatData = new HashMap<>();
        heartbeatData.put("timestamp", System.currentTimeMillis());
        heartbeatData.put("status", "alive");
        heartbeatData.put("sessionId", session.getId());
        heartbeatData.put("nextHeartbeat", 240000); // 建议下次心跳间隔
        Map<String, Object> response = createSuccessMessage("heartbeat_response", heartbeatData);
        sendMessage(session, response);
        log.debug("心跳响应已发送: sessionId={}", session.getId());
    }

    /**
     * 处理发送消息（人工客服模式）
     */
    private void handleSendMessage(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");

        if (userId == null) {
            sendErrorMessage(session, "用户未认证");
            return;
        }

        try {
            String content = (String) messageData.get("content");
            String contentType = (String) messageData.get("contentType");

            if (content == null || content.trim().isEmpty()) {
                sendErrorMessage(session, "消息内容不能为空");
                return;
            }

            log.info("用户发送消息: userId={}, sessionId={}, content={}", userId, chatSessionId, content);
            
            // 1. 获取会话信息以便正确设置接收者
            UnifiedChatSession chatSession = unifiedChatService.getSession(chatSessionId);
            if (chatSession == null) {
                sendErrorMessage(session, "会话不存在或已过期");
                return;
            }
            
            // 2. 创建统一消息对象并存储到数据库
            UnifiedChatMessage unifiedMessage = new UnifiedChatMessage();
            unifiedMessage.setMessageId(humanServiceWebSocketService.generateMessageId());
            unifiedMessage.setSessionId(chatSessionId);
            unifiedMessage.setSenderId(userId.longValue());
            unifiedMessage.setSenderType("USER");
            unifiedMessage.setContent(content);
            unifiedMessage.setContentType(contentType != null ? contentType : "text");
            unifiedMessage.setMessageType("user");
            unifiedMessage.setRole("user");
            unifiedMessage.setSenderName((String) session.getAttributes().get("userName"));
            unifiedMessage.setCreateTime(new Date());
            unifiedMessage.setIsRead(false);
            
            // 3. 根据会话类型设置接收者信息
            if (chatSession.isAiSession()) {
                // AI服务中，接收者是AI（没有具体的receiverId）
                unifiedMessage.setReceiverType("AI");
                unifiedMessage.setReceiverId(null); // AI没有具体的ID
                log.debug("设置消息接收者为AI: sessionId={}", chatSessionId);
            } else {
                // 人工客服会话中，接收者是分配的客服
                if (chatSession.getStaffId() != null) {
                    unifiedMessage.setReceiverId(chatSession.getStaffId());
                    
                    // 根据会话类型确定接收者类型
                    if (chatSession.getMerId() != null && chatSession.getMerId() == 0) {
                        // merId=0 说明接收者是平台
                        unifiedMessage.setReceiverType("PLATFORM");
                    } else {
                        // 其他情况接收者是商户
                        unifiedMessage.setReceiverType("MERCHANT");
                    }
                    log.debug("设置消息接收者为客服: staffId={}, receiverType={}", 
                            chatSession.getStaffId(), unifiedMessage.getReceiverType());
                } else {
                    // 如果没有分配客服，默认接收者是平台
                    unifiedMessage.setReceiverId(null);
                    unifiedMessage.setReceiverType("PLATFORM");
                    log.debug("设置消息接收者为平台（未分配客服）: sessionId={}", chatSessionId);
                }
            }
            
            // 4. 存储消息到数据库
            humanServiceWebSocketService.saveMessage(unifiedMessage);

            // 2. 确认消息接收
            messageData.put("sessionId", chatSessionId != null ? chatSessionId : "");
            messageData.put("messageId", unifiedMessage.getMessageId());
            messageData.put("content", content);
            messageData.put("contentType", contentType != null ? contentType : "text");
            messageData.put("timestamp", System.currentTimeMillis());
            Map<String, Object> response = createSuccessMessage("message_received", messageData);
            sendMessage(session, response);

            // 5. 检查会话类型并处理AI回复
            if (chatSession.isAiSession()) {
                log.info("检测到AI会话，准备调用Coze API: sessionId={}, cozeBotId={}, cozeConversationId={}", 
                        chatSessionId, chatSession.getCozeBotId(), chatSession.getCozeConversationId());
                
                // 调用Coze API发送消息
                handleAiMessage(chatSession, unifiedMessage, session);
            } else {
                // 6. 通知商户端客服有新消息（仅人工客服会话）
                humanServiceWebSocketService.notifyMerchantOfNewMessage(unifiedMessage);
            }

            log.info("消息已存储: messageId={}, userId={}, sessionId={}, sessionType={}, receiverId={}, receiverType={}", 
                    unifiedMessage.getMessageId(), userId, chatSessionId, 
                    chatSession.getSessionType(), unifiedMessage.getReceiverId(), unifiedMessage.getReceiverType());

        } catch (Exception e) {
            log.error("处理发送消息失败: userId={}, 错误: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 处理AI消息 - 调用Coze API
     */
    private void handleAiMessage(UnifiedChatSession chatSession, UnifiedChatMessage userMessage, WebSocketSession session) {
        try {
            // 检查必要的Coze参数
            if (chatSession.getCozeConversationId() == null || chatSession.getCozeConversationId().trim().isEmpty()) {
                log.error("AI会话缺少Coze conversation ID: sessionId={}", chatSession.getSessionId());
                sendErrorMessage(session, "AI会话配置错误：缺少conversation ID");
                return;
            }

            if (chatSession.getCozeBotId() == null || chatSession.getCozeBotId().trim().isEmpty()) {
                log.error("AI会话缺少Coze bot ID: sessionId={}", chatSession.getSessionId());
                sendErrorMessage(session, "AI会话配置错误：缺少bot ID");
                return;
            }

            log.info("开始调用Coze API: conversationId={}, botId={}, content={}", 
                    chatSession.getCozeConversationId(), chatSession.getCozeBotId(), userMessage.getContent());

            // 构建Coze创建消息请求
            CozeCreateMessageRequest cozeRequest = new CozeCreateMessageRequest();
            cozeRequest.setConversationId(chatSession.getCozeConversationId()); // 使用Coze的conversation ID
            cozeRequest.setRole("user");
            cozeRequest.setContentType("text");
            cozeRequest.setContent(userMessage.getContent());

            // 调用Coze API创建消息
            CozeCreateMessageResponse cozeResponse = cozeService.createMessage(cozeRequest);

            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                log.info("Coze消息创建成功: messageId={}, conversationId={}", 
                        cozeResponse.getData().getId(), cozeResponse.getData().getConversationId());

                // 发送成功响应给前端
                Map<String, Object> aiResponseData = new HashMap<>();
                aiResponseData.put("type", "ai_message_sent");
                aiResponseData.put("cozeMessageId", cozeResponse.getData().getId());
                aiResponseData.put("conversationId", cozeResponse.getData().getConversationId());
                aiResponseData.put("timestamp", System.currentTimeMillis());
                
                Map<String, Object> successResponse = createSuccessMessage("ai_message_sent", aiResponseData);
                sendMessage(session, successResponse);

                // 这里可以进一步处理AI的回复，比如通过流式接口获取AI回复
                // 或者等待Coze的webhook回调
                
            } else {
                String errorMsg = cozeResponse != null ? cozeResponse.getMsg() : "未知错误";
                log.error("Coze消息创建失败: conversationId={}, error={}", 
                        chatSession.getCozeConversationId(), errorMsg);
                sendErrorMessage(session, "AI消息发送失败: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("处理AI消息失败: sessionId={}, conversationId={}, 错误: {}", 
                    chatSession.getSessionId(), chatSession.getCozeConversationId(), e.getMessage(), e);
            sendErrorMessage(session, "AI消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理图片发送
     */
    private void handleSendImage(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");

        if (userId == null) {
            sendErrorMessage(session, "用户未认证");
            return;
        }

        try {
            String imageUrl = (String) messageData.get("imageUrl");
            String thumbnailUrl = (String) messageData.get("thumbnailUrl");
            String fileName = (String) messageData.get("fileName");

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                sendErrorMessage(session, "图片URL不能为空");
                return;
            }

            log.info("用户发送图片: userId={}, sessionId={}, imageUrl={}", userId, chatSessionId, imageUrl);

            // 构建图片消息响应
            Map<String, Object> imageMessage = new HashMap<>();
            imageMessage.put("messageType", "image");
            imageMessage.put("imageUrl", imageUrl);
            imageMessage.put("thumbnailUrl", thumbnailUrl);
            imageMessage.put("fileName", fileName);
            imageMessage.put("sessionId", chatSessionId);
            imageMessage.put("timestamp", System.currentTimeMillis());

            Map<String, Object> response = createSuccessMessage("image_received", imageMessage);
            sendMessage(session, response);

        } catch (Exception e) {
            log.error("处理图片发送失败: userId={}, 错误: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "图片发送失败: " + e.getMessage());
        }
    }

    /**
     * 处理商品发送
     */
    private void handleSendProduct(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");

        if (userId == null) {
            sendErrorMessage(session, "用户未认证");
            return;
        }

        try {
            Integer productId = (Integer) messageData.get("productId");

            if (productId == null) {
                sendErrorMessage(session, "商品ID不能为空");
                return;
            }

            log.info("用户发送商品: userId={}, sessionId={}, productId={}", userId, chatSessionId, productId);

            // 这里可以调用商品服务创建商品卡片
            Map<String, Object> productCard = new HashMap<>();
            productCard.put("messageType", "product_card");
            productCard.put("productId", productId);
            productCard.put("sessionId", chatSessionId);
            productCard.put("timestamp", System.currentTimeMillis());

            Map<String, Object> response = createSuccessMessage("product_sent", productCard);
            sendMessage(session, response);

        } catch (Exception e) {
            log.error("处理商品发送失败: userId={}, 错误: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "商品发送失败: " + e.getMessage());
        }
    }

    /**
     * 处理订单发送
     */
    private void handleSendOrder(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");

        if (userId == null) {
            sendErrorMessage(session, "用户未认证");
            return;
        }

        try {
            String orderNo = (String) messageData.get("orderNo");

            if (orderNo == null || orderNo.trim().isEmpty()) {
                sendErrorMessage(session, "订单号不能为空");
                return;
            }

            log.info("用户发送订单: userId={}, sessionId={}, orderNo={}", userId, chatSessionId, orderNo);

            // 这里可以调用订单服务创建订单卡片
            Map<String, Object> orderCard = new HashMap<>();
            orderCard.put("messageType", "order_card");
            orderCard.put("orderNo", orderNo);
            orderCard.put("sessionId", chatSessionId);
            orderCard.put("timestamp", System.currentTimeMillis());

            Map<String, Object> response = createSuccessMessage("order_sent", orderCard);
            sendMessage(session, response);

        } catch (Exception e) {
            log.error("处理订单发送失败: userId={}, 错误: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "订单发送失败: " + e.getMessage());
        }
    }

    /**
     * 处理快捷回复
     */
    private void handleQuickReply(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");
        String userType = (String) session.getAttributes().get("userType");

        if (userId == null) {
            sendErrorMessage(session, "用户未认证");
            return;
        }

        try {
            Integer replyId = (Integer) messageData.get("replyId");
            String content = (String) messageData.get("content");

            if (content == null || content.trim().isEmpty()) {
                sendErrorMessage(session, "快捷回复内容不能为空");
                return;
            }

            log.info("用户使用快捷回复: userId={}, sessionId={}, replyId={}", userId, chatSessionId, replyId);

            // 记录快捷回复使用
            if (replyId != null) {
                // 这里可以调用快捷回复服务记录使用情况
                log.debug("记录快捷回复使用: replyId={}", replyId);
            }

            // 构建快捷回复响应
            Map<String, Object> quickReplyData = new HashMap<>();
            quickReplyData.put("messageType", "quick_reply");
            quickReplyData.put("content", content);
            quickReplyData.put("replyId", replyId);
            quickReplyData.put("sessionId", chatSessionId);
            quickReplyData.put("timestamp", System.currentTimeMillis());

            Map<String, Object> response = createSuccessMessage("quick_reply_sent", quickReplyData);
            sendMessage(session, response);

        } catch (Exception e) {
            log.error("处理快捷回复失败: userId={}, 错误: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "快捷回复失败: " + e.getMessage());
        }
    }

    /**
     * 处理获取快捷回复列表
     */
    private void handleGetQuickReplies(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        String userType = (String) session.getAttributes().get("userType");

        if (userId == null) {
            sendErrorMessage(session, "用户未认证");
            return;
        }

        try {
            String category = (String) messageData.get("category");

            log.info("获取快捷回复列表: userId={}, userType={}, category={}", userId, userType, category);

            // 这里可以调用快捷回复服务获取列表
            List<Map<String, Object>> quickReplies = new ArrayList<>();

            // 模拟返回一些快捷回复
            if ("USER".equals(userType)) {
                Map<String, Object> reply1 = new HashMap<>();
                reply1.put("id", 1);
                reply1.put("title", "您好");
                reply1.put("content", "您好");
                reply1.put("category", "greeting");
                quickReplies.add(reply1);
            } else {
                Map<String, Object> reply1 = new HashMap<>();
                reply1.put("id", 1);
                reply1.put("title", "欢迎咨询");
                reply1.put("content", "您好，很高兴为您服务！有什么可以帮助您的吗？");
                reply1.put("category", "greeting");
                quickReplies.add(reply1);
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("category", category);
            responseData.put("quickReplies", quickReplies);
            responseData.put("count", quickReplies.size());

            Map<String, Object> response = createSuccessMessage("quick_replies_list", responseData);
            sendMessage(session, response);

        } catch (Exception e) {
            log.error("获取快捷回复列表失败: userId={}, 错误: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "获取快捷回复列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理商品搜索
     */
    private void handleSearchProducts(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");

        if (userId == null) {
            sendErrorMessage(session, "用户未认证");
            return;
        }

        try {
            String keyword = (String) messageData.get("keyword");
            Integer page = (Integer) messageData.get("page");
            Integer limit = (Integer) messageData.get("limit");

            if (keyword == null || keyword.trim().isEmpty()) {
                sendErrorMessage(session, "搜索关键词不能为空");
                return;
            }

            if (page == null) page = 1;
            if (limit == null) limit = 10;

            log.info("搜索商品: userId={}, keyword={}, page={}, limit={}", userId, keyword, page, limit);

            // 这里可以调用商品搜索服务
            List<Map<String, Object>> products = new ArrayList<>();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("keyword", keyword);
            responseData.put("page", page);
            responseData.put("limit", limit);
            responseData.put("products", products);
            responseData.put("total", 0);
            responseData.put("hasMore", false);

            Map<String, Object> response = createSuccessMessage("search_results", responseData);
            sendMessage(session, response);

        } catch (Exception e) {
            log.error("搜索商品失败: userId={}, 错误: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "搜索商品失败: " + e.getMessage());
        }
    }

    /**
     * 处理输入状态
     */
    private void handleTypingStatus(WebSocketSession session, Map<String, Object> messageData) {
        // 暂时忽略，可以后续实现
        log.debug("收到输入状态消息: {}", messageData);
    }

    /**
     * 处理消息已读
     */
    private void handleReadMessage(WebSocketSession session, Map<String, Object> messageData) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        String messageId = (String) messageData.get("messageId");

        if (userId != null && messageId != null) {
            try {
                humanServiceWebSocketService.markMessageAsRead(messageId, userId);

                Map<String, Object> readData = new HashMap<>();
                readData.put("messageId", messageId);
                Map<String, Object> response = createSuccessMessage("message_read", readData);
                sendMessage(session, response);
            } catch (Exception e) {
                log.error("标记消息已读失败: userId={}, messageId={}, 错误: {}", userId, messageId, e.getMessage());
            }
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Integer userId, Map<String, Object> message) {
        try {
            String sessionId = userSessionMap.get(userId.toString());
            if (sessionId != null) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                    log.debug("成功发送消息给用户: userId={}, 消息类型={}", userId, message.get("type"));
                } else {
                    log.debug("用户会话不存在或已关闭: userId={}", userId);
                    // 清理无效映射
                    userSessionMap.remove(userId.toString());
                    if (sessionId != null) {
                        sessions.remove(sessionId);
                        lastActiveTime.remove(sessionId);
                    }
                }
            } else {
                log.debug("用户未在线: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("发送消息给用户失败: userId={}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 发送AI回复给特定聊天会话
     */
    public void sendAiReplyToSession(String chatSessionId, Map<String, Object> message) {
        try {
            String sessionId = chatSessionMap.get(chatSessionId);
            if (sessionId != null) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                    log.info("成功发送AI回复到会话: chatSessionId={}, 消息类型={}", chatSessionId, message.get("type"));
                } else {
                    log.debug("聊天会话连接不存在或已关闭: chatSessionId={}", chatSessionId);
                    // 清理无效映射
                    chatSessionMap.remove(chatSessionId);
                    if (sessionId != null) {
                        sessions.remove(sessionId);
                        lastActiveTime.remove(sessionId);
                    }
                }
            } else {
                log.debug("聊天会话未连接: chatSessionId={}", chatSessionId);
            }
        } catch (Exception e) {
            log.error("发送AI回复到会话失败: chatSessionId={}, 错误: {}", chatSessionId, e.getMessage(), e);
        }
    }

    /**
     * 广播统一聊天消息（AI回复）
     */
    public void broadcastMessageToSession(UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "ai_reply");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("sender", "AI");

            // 发送给特定聊天会话
            sendAiReplyToSession(message.getSessionId(), messageData);

        } catch (Exception e) {
            log.error("广播AI回复失败: sessionId={}, 错误: {}", message.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * 发送消息到WebSocket会话
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                String messageJson = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(messageJson));
                // 发送消息也更新活跃时间，表示连接是活跃的
                lastActiveTime.put(session.getId(), System.currentTimeMillis());
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: sessionId={}, 错误: {}", session.getId(), e.getMessage(), e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        Map<String, Object> errorResponse = createErrorMessage(errorMessage);
        sendMessage(session, errorResponse);
    }

    /**
     * 创建成功响应消息
     */
    private Map<String, Object> createSuccessMessage(String type, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        return response;
    }

    /**
     * 创建错误响应消息
     */
    private Map<String, Object> createErrorMessage(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "error");
        response.put("data", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        response.put("error", true);
        return response;
    }

    /**
     * 清理会话
     */
    private void cleanupSession(WebSocketSession session) {
        String sessionId = session.getId();
        Integer userId = (Integer) session.getAttributes().get("userId");
        String chatSessionId = (String) session.getAttributes().get("chatSessionId");

        // 清理各种映射
        sessions.remove(sessionId);
        lastActiveTime.remove(sessionId);

        if (userId != null) {
            userSessionMap.remove(userId.toString());
        }

        if (chatSessionId != null) {
            chatSessionMap.remove(chatSessionId);
        }

        log.debug("清理WebSocket会话: sessionId={}, userId={}, chatSessionId={}", sessionId, userId, chatSessionId);
    }

    /**
     * 启动心跳检测
     */
    private void startHeartbeatCheck(String sessionId) {
        heartbeatExecutor.schedule(() -> {
            try {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    Long lastActive = lastActiveTime.get(sessionId);
                    long currentTime = System.currentTimeMillis();
                    
                    if (lastActive != null) {
                        long inactiveTime = currentTime - lastActive;
                        
                        // 如果超过4分钟无活动，发送心跳提醒
                        if (inactiveTime > 240000 && inactiveTime <= 300000) {
                            Map<String, Object> reminderData = new HashMap<>();
                            reminderData.put("type", "heartbeat_reminder");
                            reminderData.put("message", "连接即将超时，请发送心跳");
                            reminderData.put("timeRemaining", 300000 - inactiveTime);
                            Map<String, Object> reminder = createSuccessMessage("heartbeat_reminder", reminderData);
                            sendMessage(session, reminder);
                            log.debug("发送心跳提醒: sessionId={}", sessionId);
                        }
                        
                        // 延长超时时间到5分钟，适合小程序的使用场景
                        if (inactiveTime > 300000) { // 5分钟无活动
                            log.warn("WebSocket会话超时，关闭连接: sessionId={}, 无活动时间={}ms", sessionId, inactiveTime);
                            session.close(CloseStatus.SESSION_NOT_RELIABLE);
                            return; // 不再继续检测
                        }
                    }
                    
                    // 继续下一次检测
                    startHeartbeatCheck(sessionId);
                }
            } catch (Exception e) {
                log.error("心跳检测异常: sessionId={}, 错误: {}", sessionId, e.getMessage());
            }
        }, 60, TimeUnit.SECONDS); // 检测间隔改为60秒，减少服务器负载
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessionMap.size();
    }

    /**
     * 获取所有在线用户
     */
    public Map<String, String> getOnlineUsers() {
        return new HashMap<>(userSessionMap);
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastToAllUsers(Map<String, Object> message) {
        if (message == null) {
            log.warn("广播消息为空，跳过广播");
            return;
        }

        int successCount = 0;
        int failedCount = 0;

        for (WebSocketSession session : sessions.values()) {
            try {
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                    successCount++;
                } else {
                    failedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                log.warn("广播消息失败，会话ID: {}, 错误: {}", session != null ? session.getId() : "null", e.getMessage());
            }
        }

        log.debug("广播消息完成: 成功={}, 失败={}, 消息类型={}",
                successCount, failedCount, message.get("type"));
    }

    /**
     * 广播系统通知给所有在线用户
     */
    public void broadcastSystemNotification(String notification, String targetType) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "system_notification");
            messageData.put("notification", notification);
            messageData.put("targetType", targetType);
            messageData.put("timestamp", System.currentTimeMillis());

            broadcastToAllUsers(messageData);
            log.info("系统通知已广播到小程序端: notification={}", notification);
        } catch (Exception e) {
            log.error("广播系统通知失败", e);
        }
    }

    /**
     * 向指定会话的所有参与者广播消息
     */
    public void broadcastToSession(String sessionId, Map<String, Object> message) {
        if (sessionId == null || message == null) {
            log.warn("会话ID或消息为空，跳过会话广播");
            return;
        }

        // 根据聊天会话ID找到对应的WebSocket会话
        String webSocketSessionId = chatSessionMap.get(sessionId);
        if (webSocketSessionId != null) {
            WebSocketSession session = sessions.get(webSocketSessionId);
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, message);
                    log.debug("会话消息已发送: sessionId={}, messageType={}", sessionId, message.get("type"));
                } catch (Exception e) {
                    log.error("发送会话消息失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
                }
            } else {
                log.warn("会话对应的WebSocket连接不存在或已关闭: sessionId={}", sessionId);
            }
        } else {
            log.warn("找不到会话对应的WebSocket连接: sessionId={}", sessionId);
        }
    }
}
