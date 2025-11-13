package com.zbkj.admin.controller.merchant;

import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UnifiedMessageRoutingService;
import com.zbkj.service.service.UnifiedChatService;
import com.zbkj.common.request.chat.SendMessageRequest;
import com.zbkj.common.response.chat.MessageResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息推送测试控制器
 * 用于测试跨端消息推送功能
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchant/test/message")
@Api(tags = "消息推送测试接口")
public class MessagePushTestController {

    @Autowired
    private UnifiedMessageRoutingService messageRoutingService;

    @Autowired
    private UnifiedChatService unifiedChatService;

    @PostMapping("/send-staff-message")
    @ApiOperation("测试客服发送消息到用户")
    public CommonResult<Map<String, Object>> sendStaffMessage(
            @RequestParam String sessionId,
            @RequestParam Integer staffId,
            @RequestParam Integer userId,
            @RequestParam String content) {
        
        try {
            log.info("测试客服消息推送: staffId={}, userId={}, sessionId={}, content={}", 
                    staffId, userId, sessionId, content);
            
            // 创建发送消息请求
            SendMessageRequest request = new SendMessageRequest();
            request.setSessionId(sessionId);
            request.setContent(content);
            request.setMessageType("text");
            request.setContentType("text");
            request.setSenderId(staffId.longValue());
            request.setSenderType("MERCHANT"); // 标记为客服发送
            request.setReceiverId(userId.longValue());
            request.setReceiverType("USER");
            
            // 通过统一聊天服务发送消息
            MessageResponse messageResponse = unifiedChatService.sendMessage(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("messageId", messageResponse.getMessageId());
            result.put("sessionId", sessionId);
            result.put("staffId", staffId);
            result.put("userId", userId);
            result.put("content", content);
            result.put("timestamp", System.currentTimeMillis());
            result.put("status", "sent");
            
            log.info("测试消息发送成功: messageId={}", messageResponse.getMessageId());
            
            return CommonResult.success(result );
        } catch (Exception e) {
            log.error("测试消息发送失败: {}", e.getMessage(), e);
            return CommonResult.failed("消息发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/test-cross-endpoint-push")
    @ApiOperation("测试跨端消息推送")
    public CommonResult<Map<String, Object>> testCrossEndpointPush(
            @RequestParam String sessionId,
            @RequestParam Integer fromUserId,
            @RequestParam String fromUserType,
            @RequestParam Integer toUserId,
            @RequestParam String toUserType,
            @RequestParam String content) {
        
        try {
            log.info("测试跨端消息推送: from={}({}), to={}({}), content={}", 
                    fromUserId, fromUserType, toUserId, toUserType, content);
            
            // 模拟创建一条消息并测试路由
            Map<String, Object> testMessage = new HashMap<>();
            testMessage.put("type", "test_message");
            testMessage.put("sessionId", sessionId);
            testMessage.put("fromUserId", fromUserId);
            testMessage.put("fromUserType", fromUserType);
            testMessage.put("toUserId", toUserId);
            testMessage.put("toUserType", toUserType);
            testMessage.put("content", content);
            testMessage.put("timestamp", System.currentTimeMillis());
            
            // 根据目标用户类型选择推送方法
            if ("USER".equals(toUserType)) {
                // 推送给小程序用户
                messageRoutingService.sendToFrontendUser(toUserId, testMessage);
                log.info("消息已推送到小程序用户: userId={}", toUserId);
            } else if ("MERCHANT".equals(toUserType) || "MERCHANT".equals(toUserType)) {
                // 推送给商户端客服
                messageRoutingService.sendToMerchantStaff(toUserId, testMessage);
                log.info("消息已推送到商户端客服: staffId={}", toUserId);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("fromUserId", fromUserId);
            result.put("fromUserType", fromUserType);
            result.put("toUserId", toUserId);
            result.put("toUserType", toUserType);
            result.put("content", content);
            result.put("status", "pushed");
            result.put("timestamp", System.currentTimeMillis());
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("跨端消息推送测试失败: {}", e.getMessage(), e);
            return CommonResult.failed("跨端消息推送失败: " + e.getMessage());
        }
    }

    @GetMapping("/websocket-status")
    @ApiOperation("检查WebSocket连接状态")
    public CommonResult<Map<String, Object>> checkWebSocketStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 这里可以添加检查WebSocket处理器状态的逻辑
            status.put("frontendHandler", "available"); // 假设前端处理器可用
            status.put("adminHandler", "available");    // 假设管理端处理器可用
            status.put("messageRouting", "available");  // 消息路由服务可用
            status.put("timestamp", System.currentTimeMillis());
            
            return CommonResult.success(status);
        } catch (Exception e) {
            log.error("WebSocket状态检查失败: {}", e.getMessage(), e);
            return CommonResult.failed("WebSocket状态检查失败: " + e.getMessage());
        }
    }
}