package com.zbkj.admin.websocket;

import com.alibaba.fastjson.JSON;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.service.service.HumanServiceWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人工客服WebSocket处理器
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
    private ApplicationContext applicationContext;

    // 存储WebSocket连接
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // 存储用户ID与SessionId的映射
    private static final Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    
    // 存储客服ID与SessionId的映射（专门用于客服）
    private static final Map<String, String> staffSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        log.info("【商户端WebSocket】连接建立: sessionId={}, 当前连接数={}", sessionId, sessions.size());
        log.info("【商户端WebSocket】连接来源: {}", session.getRemoteAddress());
        log.info("【商户端WebSocket】连接URI: {}", session.getUri());
        
        // 从URL参数获取token并进行预验证
        String token = getTokenFromSession(session);
        if (token != null) {
            log.info("【商户端WebSocket】连接建立，SessionId: {}, Token: {}", sessionId, token.substring(0, Math.min(token.length(), 10)) + "...");
        } else {
            log.warn("【商户端WebSocket】连接建立，SessionId: {}, 但未提供token", sessionId);
        }
    }
    
    /**
     * 从WebSocket会话中获取token
     */
    private String getTokenFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("token=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        return java.net.URLDecoder.decode(param.substring(6), "UTF-8");
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取token失败", e);
        }
        return null;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            log.info("收到WebSocket消息: {}", payload);
            
            Map<String, Object> messageData = JSON.parseObject(payload, Map.class);
            String messageType = (String) messageData.get("type");
            
            switch (messageType) {
                case "login":
                    handleLogin(session, messageData);
                    break;
                case "heartbeat":
                    handleHeartbeat(session, messageData);
                    break;
                case "message":
                    handleChatMessage(session, messageData);
                    break;
                case "typing":
                    handleTyping(session, messageData);
                    break;
                case "read_message":
                    handleReadMessage(session, messageData);
                    break;
                case "status_change":
                    handleStatusChange(session, messageData);
                    break;
                default:
                    log.warn("未知的消息类型: {}", messageType);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误，SessionId: {}, 错误: {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        String userId = getUserIdBySessionId(sessionId);
        String userType = (String) session.getAttributes().get("userType");
        
        sessions.remove(sessionId);
        if (userId != null) {
            userSessionMap.remove(userId);
            
            // 如果是客服，移除客服映射
            String staffId = (String) session.getAttributes().get("staffId");
            if (staffId != null) {
                staffSessionMap.remove(staffId);
                log.info("【商户端WebSocket】客服映射已移除: staffId={}", staffId);
            }
            
            // 更新用户离线状态
            try {
                humanServiceWebSocketService.updateUserOfflineStatus(Integer.valueOf(userId));
                
                // 如果是客服，同时更新客服状态为离线
                if ("MERCHANT".equals(userType) && staffId != null) {
                    com.zbkj.service.service.CustomerServiceStaffService staffService = 
                        applicationContext.getBean(com.zbkj.service.service.CustomerServiceStaffService.class);
                    staffService.updateOnlineStatus(Integer.valueOf(staffId), "OFFLINE");
                    log.info("【商户端WebSocket】客服连接断开，设置为离线状态: staffId={}", staffId);
                }
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userId);
            }
        }
        
        log.info("WebSocket连接关闭，SessionId: {}, UserId: {}, UserType: {}, 状态: {}", 
                sessionId, userId, userType, closeStatus.getCode());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理用户登录
     */
    private void handleLogin(WebSocketSession session, Map<String, Object> messageData) {
        try {
            String token = (String) messageData.get("token");
            String userType = (String) messageData.get("userType"); // STAFF, USER, MERCHANT

            log.info("【商户端WebSocket】登录请求: userType={}, token前10位={}", userType,
                    token != null ? token.substring(0, Math.min(10, token.length())) : "null");

            // 验证token并获取用户信息
            Map<String, Object> userInfo = humanServiceWebSocketService.validateTokenAndGetUser(token, userType);
            String userId = userInfo.get("userId").toString();  // 这里的userId实际是admin_id
            String userName = userInfo.get("userName").toString();

            log.info("【商户端WebSocket】登录验证成功: adminId={}, userName={}", userId, userName);

            // 绑定用户和session（userId这里实际是admin_id）
            userSessionMap.put(userId, session.getId());
            session.getAttributes().put("userId", userId);  // 存储admin_id
            session.getAttributes().put("userType", userType);
            session.getAttributes().put("userName", userName);

            // ✅ 如果是客服，同时建立客服ID映射（统一使用admin_id）
            if ("MERCHANT".equals(userType) || "PLATFORM".equals(userType)) {
                try {
                    log.info("【商户端WebSocket】开始建立客服映射: adminId={}, userType={}", userId, userType);

                    // ✅ 直接使用admin_id作为客服标识
                    String staffAdminId = userId;  // userId本身就是admin_id

                    // 通过admin_id获取客服信息（用于验证）
                    com.zbkj.service.service.CustomerServiceStaffService staffService =
                        applicationContext.getBean(com.zbkj.service.service.CustomerServiceStaffService.class);

                    log.info("【商户端WebSocket】查询客服信息: adminId={}", staffAdminId);
                    com.zbkj.common.model.service.CustomerServiceStaff staff =
                        staffService.getByEmployeeId(Integer.valueOf(staffAdminId));

                    if (staff != null) {
                        // ✅ 使用admin_id建立映射
                        staffSessionMap.put(staffAdminId, session.getId());
                        session.getAttributes().put("staffId", staffAdminId);  // 存储admin_id
                        session.getAttributes().put("staffName", staff.getStaffName());

                        log.info("【商户端WebSocket】客服映射建立成功 ✅: adminId={}, staffName={}, sessionId={}",
                                staffAdminId, staff.getStaffName(), session.getId());

                        // 发送登录成功消息
                        Map<String, Object> response = new HashMap<>();
                        response.put("type", "login_success");
                        response.put("adminId", staffAdminId);
                        response.put("staffId", staff.getId());  // 主键ID仅用于显示
                        response.put("staffName", staff.getStaffName());
                        response.put("userType", userType);
                        response.put("timestamp", System.currentTimeMillis());
                        sendMessage(session, response);
                    } else {
                        // 【修复】未找到客服记录，自动创建默认客服
                        log.warn("【商户端WebSocket】未找到客服记录，尝试自动创建: adminId={}", staffAdminId);

                        try {
                            // 获取 SystemAdminService
                            com.zbkj.service.service.SystemAdminService systemAdminService =
                                applicationContext.getBean(com.zbkj.service.service.SystemAdminService.class);

                            // 通过 adminId 获取管理员信息
                            com.zbkj.common.model.admin.SystemAdmin admin =
                                systemAdminService.getById(Integer.valueOf(staffAdminId));

                            if (admin != null && admin.getMerId() != null) {
                                Long merId = admin.getMerId().longValue();
                                log.info("【商户端WebSocket】获取到管理员信息: adminId={}, merId={}", staffAdminId, merId);

                                // 根据 merId 判断是平台还是商户，自动创建客服
                                com.zbkj.common.model.service.CustomerServiceStaff createdStaff;
                                if (merId == 0L) {
                                    log.info("【商户端WebSocket】创建平台默认客服");
                                    createdStaff = staffService.createOrGetDefaultStaffForPlatform();
                                } else {
                                    log.info("【商户端WebSocket】创建商户默认客服: merId={}", merId);
                                    createdStaff = staffService.createOrGetDefaultStaffForMerchant(merId);
                                }

                                if (createdStaff != null) {
                                    log.info("【商户端WebSocket】自动创建客服成功 ✅: adminId={}, staffName={}, merId={}",
                                            createdStaff.getAdminId(), createdStaff.getStaffName(), merId);

                                    // 建立映射
                                    staffSessionMap.put(staffAdminId, session.getId());
                                    session.getAttributes().put("staffId", staffAdminId);
                                    session.getAttributes().put("staffName", createdStaff.getStaffName());

                                    // 发送登录成功消息
                                    Map<String, Object> response = new HashMap<>();
                                    response.put("type", "login_success");
                                    response.put("adminId", staffAdminId);
                                    response.put("staffId", createdStaff.getId());
                                    response.put("staffName", createdStaff.getStaffName());
                                    response.put("userType", userType);
                                    response.put("timestamp", System.currentTimeMillis());
                                    response.put("autoCreated", true);  // 标记为自动创建
                                    sendMessage(session, response);
                                } else {
                                    log.error("【商户端WebSocket】自动创建客服失败: adminId={}, merId={}", staffAdminId, merId);
                                    sendErrorMessage(session, "自动创建客服失败");
                                }
                            } else {
                                log.error("【商户端WebSocket】无法获取管理员信息: adminId={}", staffAdminId);
                                sendErrorMessage(session, "无法获取管理员信息");
                            }
                        } catch (Exception createEx) {
                            log.error("【商户端WebSocket】自动创建客服异常: adminId={}, 错误: {}",
                                    staffAdminId, createEx.getMessage(), createEx);
                            sendErrorMessage(session, "创建客服失败: " + createEx.getMessage());
                        }
                    }

                } catch (Exception e) {
                    log.error("【商户端WebSocket】建立客服映射失败: adminId={}, 错误: {}",
                            userId, e.getMessage(), e);
                    sendErrorMessage(session, "建立客服连接失败");
                }
            }

        } catch (Exception e) {
            log.error("【商户端WebSocket】登录处理失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(WebSocketSession session, Map<String, Object> messageData) {
        String userId = (String) session.getAttributes().get("userId");
        String userType = (String) session.getAttributes().get("userType");
        
        if (userId != null) {
            try {
                // 更新心跳时间，维持在线状态
                humanServiceWebSocketService.updateHeartbeat(Integer.valueOf(userId));
                
                // 如果是客服，同时更新客服状态为在线，并确保映射存在
                if ("MERCHANT".equals(userType)) {
                    String staffId = (String) session.getAttributes().get("staffId");
                    if (staffId != null) {
                        try {
                            // 确保客服映射存在（防止映射丢失）
                            if (!staffSessionMap.containsKey(staffId)) {
                                staffSessionMap.put(staffId, session.getId());
                                log.info("【商户端WebSocket】心跳时重建客服映射: staffId={} -> sessionId={}", staffId, session.getId());
                            }
                            
                            // 通过CustomerServiceStaffService更新客服在线状态
                            com.zbkj.service.service.CustomerServiceStaffService staffService = 
                                applicationContext.getBean(com.zbkj.service.service.CustomerServiceStaffService.class);
                            
                            // 检查客服是否可用，如果不可用则设为在线
                            if (!staffService.isStaffAvailable(Integer.valueOf(staffId))) {
                                staffService.updateOnlineStatus(Integer.valueOf(staffId), "ONLINE");
                                log.info("【商户端WebSocket】客服心跳更新，设置为在线状态: staffId={}", staffId);
                            }
                        } catch (Exception e) {
                            log.warn("【商户端WebSocket】更新客服在线状态失败: staffId={}, 错误: {}", staffId, e.getMessage());
                        }
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userId);
            }
        }
        
        // 发送心跳响应，包含当前连接状态信息
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("onlineUsers", userSessionMap.size());
        response.put("onlineStaff", staffSessionMap.size());
        response.put("activeConnections", sessions.size());
        sendMessage(session, createSuccessMessage("heartbeat_response", response));
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(WebSocketSession session, Map<String, Object> messageData) {
        String userIdStr = (String) session.getAttributes().get("userId");
        String userType = (String) session.getAttributes().get("userType");
        
        if (userIdStr == null) {
            sendErrorMessage(session, "用户未登录");
            return;
        }
        
        try {
            String sessionId = (String) messageData.get("sessionId");
            String content = (String) messageData.get("content");
            Object receiverIdObj = messageData.get("receiverId");
            String receiverType = (String) messageData.get("receiverType");
            
            if (sessionId == null || content == null || receiverIdObj == null) {
                sendErrorMessage(session, "消息参数不完整");
                return;
            }
            
            // 处理receiverId可能是字符串或整数的情况
            Integer senderId = Integer.valueOf(userIdStr);
            Integer receiverId = null;
            if (receiverIdObj instanceof String) {
                receiverId = Integer.valueOf((String) receiverIdObj);
            } else if (receiverIdObj instanceof Integer) {
                receiverId = (Integer) receiverIdObj;
            }
            
            if (receiverId == null) {
                sendErrorMessage(session, "接收者ID格式错误");
                return;
            }
            
            log.info("商户端发送消息: 发送者={}, 接收者={}, 会话={}, 内容={}", 
                    senderId, receiverId, sessionId, content);
            
            // 使用统一聊天服务发送消息（这会自动处理消息路由）
            try {
                com.zbkj.common.request.chat.SendMessageRequest unifiedRequest = 
                    new com.zbkj.common.request.chat.SendMessageRequest();
                unifiedRequest.setSessionId(sessionId);
                unifiedRequest.setContent(content);
                unifiedRequest.setMessageType("text");
                unifiedRequest.setContentType("text");
                unifiedRequest.setSenderId(senderId.longValue());
                unifiedRequest.setSenderType("MERCHANT".equals(userType) ? "MERCHANT" : "PLATFORM"); // 根据用户类型标记发送者
                unifiedRequest.setReceiverId(receiverId.longValue());
                unifiedRequest.setReceiverType(receiverType != null ? receiverType : "USER");
                
                // 通过统一聊天服务发送消息，这会自动路由到小程序端
                com.zbkj.service.service.UnifiedChatService unifiedChatService = 
                    applicationContext.getBean(com.zbkj.service.service.UnifiedChatService.class);
                com.zbkj.common.response.chat.MessageResponse messageResponse = 
                    unifiedChatService.sendMessage(unifiedRequest);
                
                log.info("统一聊天服务发送成功: 消息ID={}", messageResponse.getMessageId());
                
                // 确认消息发送成功给商户端
                Map<String, Object> result = new HashMap<>();
                result.put("messageId", messageResponse.getMessageId());
                result.put("sessionId", sessionId);
                result.put("content", content);
                result.put("timestamp", System.currentTimeMillis());
                result.put("senderId", senderId);
                result.put("receiverId", receiverId);
                
                sendMessage(session, createSuccessMessage("message_sent", result));
                
            } catch (Exception e) {
                log.error("通过统一服务发送消息失败，回退到原方式: {}", e.getMessage());
                
                // 回退到原来的方式
                Map<String, Object> result = humanServiceWebSocketService.sendChatMessage(
                    senderId, userType, sessionId, content, receiverId, receiverType);
                
                // 发送消息到接收方（这里需要确保能路由到小程序端）
                sendMessageToUser(receiverId, createSuccessMessage("new_message", result));
                
                // 确认消息发送成功
                sendMessage(session, createSuccessMessage("message_sent", result));
            }
            
        } catch (Exception e) {
            log.error("处理聊天消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 处理正在输入状态
     */
    private void handleTyping(WebSocketSession session, Map<String, Object> messageData) {
        String userIdStr = (String) session.getAttributes().get("userId");
        String sessionId = (String) messageData.get("sessionId");
        Object receiverIdObj = messageData.get("receiverId");
        Boolean isTyping = (Boolean) messageData.get("isTyping");
        
        if (userIdStr != null && receiverIdObj != null) {
            try {
                Integer userId = Integer.valueOf(userIdStr);
                Integer receiverId = null;
                
                // 处理receiverId可能是字符串或整数的情况
                if (receiverIdObj instanceof String) {
                    receiverId = Integer.valueOf((String) receiverIdObj);
                } else if (receiverIdObj instanceof Integer) {
                    receiverId = (Integer) receiverIdObj;
                }
                
                if (receiverId != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sessionId", sessionId);
                    map.put("userId", userId);
                    map.put("isTyping", isTyping);
                    sendMessageToUser(receiverId, createSuccessMessage("typing_status", map));
                }
            } catch (NumberFormatException e) {
                log.warn("处理typing消息时ID格式错误: userId={}, receiverId={}", userIdStr, receiverIdObj);
            }
        }
    }

    /**
     * 处理消息已读
     */
    private void handleReadMessage(WebSocketSession session, Map<String, Object> messageData) {
        String userId = (String) session.getAttributes().get("userId");
        String messageId = (String) messageData.get("messageId");
        
        if (userId != null && messageId != null) {
            try {
                humanServiceWebSocketService.markMessageAsRead(messageId, Integer.valueOf(userId));
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userId);
            }
            Map<String, Object> map=new HashMap<>();
            map.put("messageId", messageId);
            sendMessage(session, createSuccessMessage("message_read", map));
        }
    }

    /**
     * 处理状态变更
     */
    private void handleStatusChange(WebSocketSession session, Map<String, Object> messageData) {
        String userId = (String) session.getAttributes().get("userId");
        String userType = (String) session.getAttributes().get("userType");
        String newStatus = (String) messageData.get("status");
        
        if (userId != null && ("MERCHANT".equals(userType) || "PLATFORM".equals(userType))) {
            String staffId = (String) session.getAttributes().get("staffId");
            if (staffId != null) {
                try {
                    // 使用CustomerServiceStaffService更新客服状态
                    com.zbkj.service.service.CustomerServiceStaffService staffService = 
                        applicationContext.getBean(com.zbkj.service.service.CustomerServiceStaffService.class);
                    
                    // 转换状态格式
                    String staffStatus = "online".equals(newStatus) ? "ONLINE" : "OFFLINE";
                    staffService.updateOnlineStatus(Integer.valueOf(staffId), staffStatus);
                    
                    log.info("【商户端WebSocket】客服状态已更新: staffId={}, status={}", staffId, staffStatus);
                } catch (Exception e) {
                    log.error("【商户端WebSocket】更新客服状态失败: staffId={}, 错误: {}", staffId, e.getMessage());
                }

                // 构建响应数据
                Map<String, Object> map = new HashMap<>();
                map.put("staffId", staffId);  // 使用真实的客服ID
                map.put("userId", userId);    // 保留用户ID用于兼容
                map.put("status", newStatus);
                
                // 广播状态变更
                broadcastToAllUsers(createSuccessMessage("staff_status_change", map));
            } else {
                log.warn("【商户端WebSocket】客服ID不存在，无法更新状态: userId={}", userId);
            }
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Integer userId, Map<String, Object> message) {
        log.info("【商户端WebSocket】开始发送消息给用户: userId={}, 消息类型={}, 在线用户数={}", 
                userId, message.get("type"), userSessionMap.size());
        log.info("【商户端WebSocket】当前在线用户列表: {}", userSessionMap.keySet());
        
        try {
            String sessionId = userSessionMap.get(userId.toString()); // 修复：转换为String
            log.info("【商户端WebSocket】用户{}对应的sessionId={}", userId, sessionId);
            
            if (sessionId != null) {
                WebSocketSession session = sessions.get(sessionId);
                log.info("【商户端WebSocket】获取到的WebSocketSession: {}, isOpen={}", 
                        session != null, session != null ? session.isOpen() : false);
                
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                    log.info("【商户端WebSocket】成功发送消息给用户: userId={}, 消息类型={}", userId, message.get("type"));
                } else {
                    log.warn("【商户端WebSocket】用户会话不存在或已关闭: userId={}, session={}", userId, session);
                }
            } else {
                log.warn("【商户端WebSocket】用户未在线或映射不存在: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("【商户端WebSocket】发送消息给用户失败: userId={}, 错误: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送消息给指定客服（根据客服ID）
     */
    public void sendMessageToStaff(Integer staffId, Map<String, Object> message) {
        log.info("【商户端WebSocket】开始发送消息给客服: staffId={}, 消息类型={}, 在线客服数={}", 
                staffId, message.get("type"), staffSessionMap.size());
        log.info("【商户端WebSocket】当前在线客服列表: {}", staffSessionMap.keySet());
        log.info("【商户端WebSocket】当前所有用户映射: {}", userSessionMap);
        log.info("【商户端WebSocket】当前所有客服映射: {}", staffSessionMap);
        log.info("【商户端WebSocket】当前活跃WebSocket连接数: {}", sessions.size());
        log.info("【商户端WebSocket】所有活跃连接的sessionId: {}", sessions.keySet());
        
        // 如果客服映射为空，尝试重建映射
        if (staffSessionMap.isEmpty() && !sessions.isEmpty()) {
            log.warn("【商户端WebSocket】客服映射为空但有活跃连接，尝试重建映射");
            rebuildStaffMappings();
        }
        
        try {
            String sessionId = staffSessionMap.get(staffId.toString());
            log.info("【商户端WebSocket】客服{}对应的sessionId={}", staffId, sessionId);
            
            if (sessionId != null) {
                WebSocketSession session = sessions.get(sessionId);
                log.info("【商户端WebSocket】获取到的WebSocketSession: {}, isOpen={}", 
                        session != null, session != null ? session.isOpen() : false);
                
                if (session != null && session.isOpen()) {
                    // 构造标准的WebSocket消息格式
                    Map<String, Object> webSocketMessage = createSuccessMessage("user_message", message);
                    sendMessage(session, webSocketMessage);
                    log.info("【商户端WebSocket】成功发送消息给客服: staffId={}, 消息类型={}", staffId, message.get("type"));
                } else {
                    log.warn("【商户端WebSocket】客服会话不存在或已关闭: staffId={}, session={}", staffId, session);
                    // 清理无效映射
                    staffSessionMap.remove(staffId.toString());
                }
            } else {
                log.warn("【商户端WebSocket】客服未在线或映射不存在: staffId={}", staffId);
                
                // 检查客服是否真的在线（从数据库查询）
                try {
                    com.zbkj.service.service.CustomerServiceStaffService staffService = 
                        applicationContext.getBean(com.zbkj.service.service.CustomerServiceStaffService.class);
                    boolean isOnline = staffService.isStaffAvailable(staffId);
                    log.info("【商户端WebSocket】客服{}数据库在线状态: {}", staffId, isOnline);
                } catch (Exception e) {
                    log.warn("【商户端WebSocket】查询客服在线状态失败: staffId={}, 错误: {}", staffId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("【商户端WebSocket】发送消息给客服失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastToAllUsers(Map<String, Object> message) {
        log.info("【商户端WebSocket】开始广播消息给所有用户: 消息类型={}, 在线会话数={}", 
                message.get("type"), sessions.size());
        
        int successCount = 0;
        int failedCount = 0;
        
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    sendMessage(session, message);
                    successCount++;
                } else {
                    failedCount++;
                    log.warn("【商户端WebSocket】会话已关闭，跳过: sessionId={}", session.getId());
                }
            } catch (Exception e) {
                failedCount++;
                log.error("【商户端WebSocket】广播消息失败: sessionId={}, 错误: {}", session.getId(), e.getMessage());
            }
        }
        
        log.info("【商户端WebSocket】广播消息完成: 成功={}, 失败={}, 消息类型={}", 
                successCount, failedCount, message.get("type"));
    }

    /**
     * 发送消息到WebSocket会话
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(JSON.toJSONString(message)));
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        sendMessage(session, createErrorMessage(errorMessage));
    }

    /**
     * 创建成功响应消息
     */
    private Map<String, Object> createSuccessMessage(String type, Object data) {
        log.info("创建成功响应消息: {}", type);
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        log.info("创建成功响应消息成功: {}", response);
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
     * 根据SessionId获取UserId
     */
    private String getUserIdBySessionId(String sessionId) {
        return userSessionMap.entrySet().stream()
            .filter(entry -> sessionId.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取所有在线用户
     */
    public Map<String, String> getOnlineUsers() {
        return new ConcurrentHashMap<>(userSessionMap);
    }
    
    /**
     * 获取所有在线客服
     */
    public Map<String, String> getOnlineStaff() {
        return new ConcurrentHashMap<>(staffSessionMap);
    }
    
    /**
     * 检查并恢复客服映射
     */
    public void checkAndRestoreStaffMappings() {
        log.info("【商户端WebSocket】主动检查客服映射状态");
        log.info("【商户端WebSocket】当前客服映射数: {}, 活跃连接数: {}", staffSessionMap.size(), sessions.size());
        
        if (staffSessionMap.isEmpty() && !sessions.isEmpty()) {
            log.warn("【商户端WebSocket】发现映射丢失，开始恢复");
            rebuildStaffMappings();
        }
        
        // 清理无效连接
        cleanupInvalidConnections();
    }
    
    /**
     * 清理无效连接
     */
    private void cleanupInvalidConnections() {
        // 清理无效的客服映射
        staffSessionMap.entrySet().removeIf(entry -> {
            String sessionId = entry.getValue();
            WebSocketSession session = sessions.get(sessionId);
            boolean invalid = session == null || !session.isOpen();
            if (invalid) {
                log.info("【商户端WebSocket】清理无效客服映射: staffId={}, sessionId={}", entry.getKey(), sessionId);
            }
            return invalid;
        });
        
        // 清理无效的用户映射
        userSessionMap.entrySet().removeIf(entry -> {
            String sessionId = entry.getValue();
            WebSocketSession session = sessions.get(sessionId);
            boolean invalid = session == null || !session.isOpen();
            if (invalid) {
                log.info("【商户端WebSocket】清理无效用户映射: userId={}, sessionId={}", entry.getKey(), sessionId);
            }
            return invalid;
        });
        
        // 清理无效的会话
        sessions.entrySet().removeIf(entry -> {
            WebSocketSession session = entry.getValue();
            boolean invalid = session == null || !session.isOpen();
            if (invalid) {
                log.info("【商户端WebSocket】清理无效会话: sessionId={}", entry.getKey());
            }
            return invalid;
        });
    }

    /**
     * 广播消息到指定会话的所有参与者
     */
    public void broadcastMessageToSession(UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "unified_message");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            messageData.put("senderId", message.getSenderId());
            messageData.put("senderType", message.getSenderType());
            messageData.put("senderName", message.getSenderName());
            messageData.put("receiverId", message.getReceiverId());
            messageData.put("receiverType", message.getReceiverType());
            messageData.put("role", message.getRole());
            messageData.put("messageType", message.getMessageType());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("attachments", message.getAttachments());
            messageData.put("status", message.getStatus());
            messageData.put("isSystemMessage", message.getIsSystemMessage());
            messageData.put("createTime", message.getCreateTime());
            messageData.put("timestamp", System.currentTimeMillis());

            String messageJson = JSON.toJSONString(messageData);
            TextMessage textMessage = new TextMessage(messageJson);

            // 广播到所有活跃连接
            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    log.error("发送统一聊天消息失败到session: {}", session.getId(), e);
                }
            });
            
            log.info("广播统一聊天消息，会话ID: {}, 消息ID: {}, 发送者: {} ({})", 
                    message.getSessionId(), message.getMessageId(), 
                    message.getSenderName(), message.getSenderType());
        } catch (Exception e) {
            log.error("广播统一聊天消息失败", e);
        }
    }

    /**
     * 发送消息给特定用户
     */
    public void sendMessageToUser(String userId, UnifiedChatMessage message) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId != null) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("type", "new_message");
                    messageData.put("message", message);
                    messageData.put("timestamp", System.currentTimeMillis());

                    String messageJson = JSON.toJSONString(messageData);
                    session.sendMessage(new TextMessage(messageJson));
                    
                    log.info("发送消息给用户: {}, 消息ID: {}", userId, message.getMessageId());
                } catch (IOException e) {
                    log.error("发送消息给用户失败: {}", userId, e);
                }
            }
        }
    }

    /**
     * 广播系统通知
     */
    public void broadcastSystemNotification(String notification, String targetType) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "system_notification");
            messageData.put("notification", notification);
            messageData.put("targetType", targetType);
            messageData.put("timestamp", System.currentTimeMillis());

            String messageJson = JSON.toJSONString(messageData);
            TextMessage textMessage = new TextMessage(messageJson);

            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    log.error("发送系统通知失败到session: {}", session.getId(), e);
                }
            });

            log.info("广播系统通知: {}", notification);
        } catch (Exception e) {
            log.error("广播系统通知失败", e);
        }
    }

    /**
     * 重建客服映射
     */
    private void rebuildStaffMappings() {
        try {
            log.info("【商户端WebSocket】开始重建客服映射，当前活跃连接数: {}", sessions.size());
            
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession session = entry.getValue();
                String sessionId = entry.getKey();
                
                if (session != null && session.isOpen()) {
                    String userType = (String) session.getAttributes().get("userType");
                    String userId = (String) session.getAttributes().get("userId");
                    String staffId = (String) session.getAttributes().get("staffId");
                    
                    if ("MERCHANT".equals(userType) && staffId != null) {
                        // 重建客服映射
                        staffSessionMap.put(staffId, sessionId);
                        log.info("【商户端WebSocket】重建客服映射: staffId={} -> sessionId={}", staffId, sessionId);
                        
                        // 确保用户映射也存在
                        if (userId != null) {
                            userSessionMap.put(userId, sessionId);
                        }
                    }
                }
            }
            
            log.info("【商户端WebSocket】映射重建完成，客服映射数: {}, 用户映射数: {}", 
                    staffSessionMap.size(), userSessionMap.size());
            log.info("【商户端WebSocket】重建后的客服列表: {}", staffSessionMap.keySet());
            
        } catch (Exception e) {
            log.error("【商户端WebSocket】重建客服映射失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 广播消息数据到会话参与者
     */
    public void broadcastMessageToSession(String sessionId, Map<String, Object> messageData) {
        log.info("【商户端WebSocket】开始广播消息到会话: sessionId={}, 消息类型={}, 在线会话数={}", 
                sessionId, messageData.get("type"), sessions.size());
        
        try {
            String messageJson = JSON.toJSONString(messageData);
            TextMessage textMessage = new TextMessage(messageJson);
            
            int successCount = 0;
            int failedCount = 0;

            // 广播到所有活跃连接（此处可以后续优化为只推送给会话参与者）
            for (WebSocketSession session : sessions.values()) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                        successCount++;
                        log.debug("【商户端WebSocket】成功发送到会话: webSocketSessionId={}", session.getId());
                    } else {
                        failedCount++;
                        log.warn("【商户端WebSocket】会话已关闭，跳过: webSocketSessionId={}", session.getId());
                    }
                } catch (IOException e) {
                    failedCount++;
                    log.error("【商户端WebSocket】发送消息到WebSocket会话失败: webSocketSessionId={}, 错误: {}", 
                            session.getId(), e.getMessage());
                }
            }

            log.info("【商户端WebSocket】消息数据已广播到会话: sessionId={}, type={}, 成功={}, 失败={}", 
                    sessionId, messageData.get("type"), successCount, failedCount);
        } catch (Exception e) {
            log.error("【商户端WebSocket】广播消息数据到会话失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }


}
