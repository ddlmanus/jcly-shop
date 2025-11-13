package com.zbkj.admin.controller.merchant;

import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.WebRTCService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * WebRTC音频通话控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchant/webrtc")
@Api(tags = "WebRTC音频通话管理")
public class WebRTCController {

    @Autowired
    private WebRTCService webRTCService;

    @ApiOperation(value = "发起音频通话")
    @PostMapping("/call/initiate")
    public CommonResult<Map<String, Object>> initiateCall(@RequestBody Map<String, Object> callRequest) {
        try {
            Integer callerId = (Integer) callRequest.get("callerId");
            Integer calleeId = (Integer) callRequest.get("calleeId");
            String callerType = (String) callRequest.get("callerType");
            String calleeType = (String) callRequest.get("calleeType");

            Map<String, Object> result = webRTCService.initiateCall(callerId, calleeId, callerType, calleeType);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("发起音频通话失败", e);
            return CommonResult.failed("发起通话失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "接受音频通话")
    @PostMapping("/call/accept")
    public CommonResult<Map<String, Object>> acceptCall(@RequestBody Map<String, Object> acceptRequest) {
        try {
            String callId = (String) acceptRequest.get("callId");
            Integer userId = (Integer) acceptRequest.get("userId");

            Map<String, Object> result = webRTCService.acceptCall(callId, userId);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("接受音频通话失败", e);
            return CommonResult.failed("接受通话失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "拒绝音频通话")
    @PostMapping("/call/reject")
    public CommonResult<Void> rejectCall(@RequestBody Map<String, Object> rejectRequest) {
        try {
            String callId = (String) rejectRequest.get("callId");
            String userId = (String) rejectRequest.get("userId");
            String reason = (String) rejectRequest.get("reason");

            webRTCService.rejectCall(callId, userId, reason);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("拒绝音频通话失败", e);
            return CommonResult.failed("拒绝通话失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "结束音频通话")
    @PostMapping("/call/end")
    public CommonResult<Void> endCall(@RequestBody Map<String, Object> endRequest) {
        try {
            String callId = (String) endRequest.get("callId");
            String userId = (String) endRequest.get("userId");

            webRTCService.endCall(callId, userId);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("结束音频通话失败", e);
            return CommonResult.failed("结束通话失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取通话状态")
    @GetMapping("/call/status/{callId}")
    public CommonResult<Map<String, Object>> getCallStatus(@PathVariable String callId) {
        try {
            Map<String, Object> status = webRTCService.getCallStatus(callId);
            return CommonResult.success(status);
        } catch (Exception e) {
            log.error("获取通话状态失败", e);
            return CommonResult.failed("获取状态失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "处理WebRTC信令")
    @PostMapping("/signaling")
    public CommonResult<Void> handleSignaling(@RequestBody Map<String, Object> signalingData) {
        try {
            String callId = (String) signalingData.get("callId");
            String type = (String) signalingData.get("type");
            Object data = signalingData.get("data");

            webRTCService.handleSignaling(callId, type, data);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("处理WebRTC信令失败", e);
            return CommonResult.failed("信令处理失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "客服主动呼叫用户")
    @PostMapping("/call/staff-to-user")
    public CommonResult<Map<String, Object>> staffCallUser(@RequestBody Map<String, Object> callRequest) {
        try {
            Integer staffId = (Integer) callRequest.get("staffId");
            Integer userId = (Integer) callRequest.get("userId");
            String sessionId = (String) callRequest.get("sessionId");

            // 客服呼叫用户
            Map<String, Object> result = webRTCService.initiateCall(staffId, userId, "MERCHANT", "USER");
            result.put("sessionId", sessionId);
            
            log.info("客服{}呼叫用户{}: callId={}", staffId, userId, result.get("callId"));
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("客服呼叫用户失败", e);
            return CommonResult.failed("呼叫失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取在线用户列表")
    @GetMapping("/online-users")
    public CommonResult<Map<String, Object>> getOnlineUsers() {
        try {
            // 获取当前在线的用户列表
            Map<String, Object> result = new HashMap<>();
            result.put("onlineUsers", webRTCService.getOnlineUserCount());
            result.put("timestamp", System.currentTimeMillis());
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取在线用户失败", e);
            return CommonResult.failed("获取失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "检查用户在线状态")
    @GetMapping("/user-status/{userId}")
    public CommonResult<Map<String, Object>> checkUserStatus(@PathVariable Integer userId) {
        try {
            boolean isOnline = webRTCService.isUserOnline(userId, "USER");
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("isOnline", isOnline);
            result.put("timestamp", System.currentTimeMillis());
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("检查用户状态失败", e);
            return CommonResult.failed("检查失败: " + e.getMessage());
        }
    }
}
