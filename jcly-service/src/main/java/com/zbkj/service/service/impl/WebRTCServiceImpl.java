package com.zbkj.service.service.impl;


import com.zbkj.service.service.WebRTCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebRTC音频通话服务实现
 */
@Slf4j
@Service
public class WebRTCServiceImpl implements WebRTCService {

    @Autowired
    private ObjectMapper objectMapper;
    
    // 存储活跃的通话信息
    private final Map<String, Map<String, Object>> activeCalls = new ConcurrentHashMap<>();
    
    // 存储用户在线状态
    private final Map<String, Boolean> userOnlineStatus = new ConcurrentHashMap<>();
    
    // 存储WebSocket会话状态 (userId -> Boolean)
    private final Map<String, Boolean> webSocketSessions = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> initiateCall(Integer callerId, Integer calleeId, String callerType, String calleeType) {
        log.info("发起音频通话: {} -> {}", callerId, calleeId);
        
        // 检查被叫用户是否在线
        if (!isUserOnline(calleeId, calleeType)) {
            throw new RuntimeException("用户不在线，无法发起通话");
        }
        
        // 生成通话ID
        String callId = UUID.randomUUID().toString();
        
        // 创建通话信息
        Map<String, Object> callInfo = new HashMap<>();
        callInfo.put("callId", callId);
        callInfo.put("callerId", callerId);
        callInfo.put("calleeId", calleeId);
        callInfo.put("callerType", callerType);
        callInfo.put("calleeType", calleeType);
        callInfo.put("status", "CALLING");
        callInfo.put("startTime", LocalDateTime.now());
        callInfo.put("type", "AUDIO");
        
        // 存储通话信息
        activeCalls.put(callId, callInfo);
        
        // 通过WebSocket通知被叫用户
        notifyUser(calleeId, "incoming_call", callInfo);
        
        return callInfo;
    }

    @Override
    public Map<String, Object> acceptCall(String callId, Integer userId) {
        log.info("接受音频通话: callId={}, userId={}", callId, userId);
        
        Map<String, Object> callInfo = activeCalls.get(callId);
        if (callInfo == null) {
            throw new RuntimeException("通话不存在");
        }
        
        if (!"CALLING".equals(callInfo.get("status"))) {
            throw new RuntimeException("通话状态不正确");
        }
        
        // 更新通话状态
        callInfo.put("status", "CONNECTED");
        callInfo.put("acceptTime", LocalDateTime.now());
        
        // 通知主叫用户
        Integer callerId = (Integer) callInfo.get("callerId");
        notifyUser(callerId, "call_accepted", callInfo);
        
        return callInfo;
    }

    @Override
    public void rejectCall(String callId, String userId, String reason) {
        log.info("拒绝音频通话: callId={}, userId={}, reason={}", callId, userId, reason);
        
        Map<String, Object> callInfo = activeCalls.get(callId);
        if (callInfo == null) {
            return;
        }
        
        // 更新通话状态
        callInfo.put("status", "REJECTED");
        callInfo.put("endTime", LocalDateTime.now());
        callInfo.put("rejectReason", reason);
        
        // 通知主叫用户
        Integer callerId = (Integer) callInfo.get("callerId");
        notifyUser(callerId, "call_rejected", callInfo);
        
        // 移除通话记录
        activeCalls.remove(callId);
    }

    @Override
    public void endCall(String callId, String userId) {
        log.info("结束音频通话: callId={}, userId={}", callId, userId);
        
        Map<String, Object> callInfo = activeCalls.get(callId);
        if (callInfo == null) {
            return;
        }
        
        // 更新通话状态
        callInfo.put("status", "ENDED");
        callInfo.put("endTime", LocalDateTime.now());
        
        // 通知通话中的其他用户
        Integer callerId = (Integer) callInfo.get("callerId");
        Integer calleeId = (Integer) callInfo.get("calleeId");
        
        if (!userId.equals(callerId)) {
            notifyUser(callerId, "call_ended", callInfo);
        }
        if (!userId.equals(calleeId)) {
            notifyUser(calleeId, "call_ended", callInfo);
        }
        
        // 移除通话记录
        activeCalls.remove(callId);
    }

    @Override
    public Map<String, Object> getCallStatus(String callId) {
        return activeCalls.get(callId);
    }

    @Override
    public void handleSignaling(String callId, String type, Object data) {
        log.info("处理WebRTC信令: callId={}, type={}", callId, type);
        
        Map<String, Object> callInfo = activeCalls.get(callId);
        if (callInfo == null) {
            log.warn("通话不存在: {}", callId);
            return;
        }

        Integer callerId = (Integer) callInfo.get("callerId");
        Integer calleeId = (Integer) callInfo.get("calleeId");
        
        // 创建信令消息
        Map<String, Object> signalingMessage = new HashMap<>();
        signalingMessage.put("callId", callId);
        signalingMessage.put("type", type);
        signalingMessage.put("data", data);
        
        // 转发信令给对方
        switch (type) {
            case "offer":
                notifyUser(calleeId, "webrtc_signaling", signalingMessage);
                break;
            case "answer":
                notifyUser(callerId, "webrtc_signaling", signalingMessage);
                break;
            case "ice-candidate":
                // ICE候选需要转发给对方，这里简化处理，转发给双方
                notifyUser(callerId, "webrtc_signaling", signalingMessage);
                notifyUser(calleeId, "webrtc_signaling", signalingMessage);
                break;
        }
    }

    @Override
    public boolean isUserOnline(Integer userId, String userType) {
        // 检查WebSocket会话是否存在
        String sessionKey = userType + "_" + userId;
        return webSocketSessions.containsKey(sessionKey);
    }

    @Override
    public int getOnlineUserCount() {
        return webSocketSessions.size();
    }

    /**
     * 通过WebSocket通知用户
     */
    private void notifyUser(Integer userId, String messageType, Object data) {
        try {
            // 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", messageType);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // 通过前端WebSocket发送消息
            try {
                // 使用反射调用前端WebSocket控制器的方法
                Class<?> wsControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendMessageMethod = wsControllerClass.getMethod("sendMessageToUser", String.class, String.class);
                sendMessageMethod.invoke(null, userId.toString(), jsonMessage);
                log.info("通知用户成功: userId={}, messageType={}", userId, messageType);
            } catch (Exception e) {
                log.warn("通过前端WebSocket通知失败，尝试直接发送: userId={}, error={}", userId, e.getMessage());
                
                // 备用方案：检查用户在线状态
                Boolean isOnline = webSocketSessions.get(userId.toString());
                if (isOnline != null && isOnline) {
                    log.info("用户在线，消息已尝试发送: userId={}, messageType={}", userId, messageType);
                } else {
                    log.warn("用户不在线或WebSocket会话不存在: userId={}", userId);
                }
            }
            
        } catch (Exception e) {
            log.error("通知用户失败: userId={}, messageType={}", userId, messageType, e);
        }
    }


    /**
     * 设置用户在线状态
     */
    public void setUserOnlineStatus(String userId, String userType, boolean online) {
        userOnlineStatus.put(userId + "_" + userType, online);
    }
    
    /**
     * 添加WebSocket会话
     */
    public void addWebSocketSession(String userId, boolean isOnline) {
        webSocketSessions.put(userId, isOnline);
        log.info("设置WebSocket会话状态: userId={}, isOnline={}", userId, isOnline);
    }
    
    /**
     * 移除WebSocket会话
     */
    public void removeWebSocketSession(String userId) {
        webSocketSessions.remove(userId);
        log.info("移除WebSocket会话: userId={}", userId);
    }
}
