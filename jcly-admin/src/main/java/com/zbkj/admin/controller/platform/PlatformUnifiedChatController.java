package com.zbkj.admin.controller.platform;

import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.request.chat.SendMessageRequest;
import com.zbkj.common.response.chat.MessageResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.UnifiedChatService;
import com.zbkj.service.service.CozeBotService;
import com.zbkj.admin.filter.TokenComponent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 统一聊天控制器 - 平台端
 * 整合AI聊天和人工客服功能
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/unified-chat")
@Api(tags = "平台端 - 统一聊天管理")
public class PlatformUnifiedChatController {

    @Autowired
    private UnifiedChatService unifiedChatService;
    
    // @Autowired
    // private CozeBotService cozeBotService;
    
    // @Autowired
    // private TokenComponent tokenComponent;
    
    @Autowired
    private CozeService cozeService;

    /**
     * 创建或获取聊天会话
     */
    @ApiOperation(value = "创建或获取聊天会话")
    @PostMapping("/session")
    public CommonResult<UnifiedChatSession> createOrGetSession(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "USER") String userType,
            @RequestParam Long merId,
            @RequestParam(defaultValue = "AI") String sessionType,
            @RequestParam(required = false) String cozeBotId) {
        
        try {
            UnifiedChatSession session = unifiedChatService.createOrGetSession(
                userId, userType, merId, sessionType, cozeBotId);
            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("创建或获取会话失败", e);
            return CommonResult.failed("操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话详情
     */
    @ApiOperation(value = "获取会话详情")
    @GetMapping("/session/{sessionId}")
    public CommonResult<UnifiedChatSession> getSession(@PathVariable String sessionId) {
        try {
            UnifiedChatSession session = unifiedChatService.getSession(sessionId);
            if (session == null) {
                return CommonResult.failed("会话不存在");
            }
            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("获取会话详情失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 发送消息（支持AI和人工客服）
     */
    @ApiOperation(value = "发送消息")
    @PostMapping("/message")
    public CommonResult<MessageResponse> sendMessage(
            @RequestBody @Validated SendMessageRequest request) {
        
        try {
            MessageResponse response = unifiedChatService.sendMessage(request);
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return CommonResult.failed("发送失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表
     */
    @ApiOperation(value = "获取会话消息列表")
    @GetMapping("/messages")
    public CommonResult<List<MessageResponse>> getSessionMessages(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            List<MessageResponse> messages = unifiedChatService.getSessionMessages(sessionId, page, size);
            return CommonResult.success(messages);
        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 消息管理页面 - 获取消息列表（支持搜索过滤）
     */
    @ApiOperation(value = "获取消息列表（消息管理页面）")
    @GetMapping("/messages/management")
    public CommonResult<com.github.pagehelper.PageInfo<MessageResponse>> getMessagesForManagement(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String senderType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            com.github.pagehelper.PageInfo<MessageResponse> messages = 
                unifiedChatService.getMessagesForManagement(sessionId, messageType, role, content, senderType, status, page, size);
            return CommonResult.success(messages);
        } catch (Exception e) {
            log.error("获取消息管理列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 转接到人工客服
     */
    @ApiOperation(value = "转接到人工客服")
    @PostMapping("/session/{sessionId}/transfer-human")
    public CommonResult<UnifiedChatSession> transferToHuman(
            @PathVariable String sessionId,
            @RequestParam(required = false) String transferReason,
            @RequestParam(defaultValue = "NORMAL") String priority) {
        
        try {
            UnifiedChatSession session = unifiedChatService.transferToHumanService(
                sessionId, transferReason, priority);
            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("转接人工客服失败", e);
            return CommonResult.failed("转接失败：" + e.getMessage());
        }
    }

    /**
     * 分配客服
     */
    @ApiOperation(value = "分配客服")
    @PostMapping("/session/{sessionId}/assign-staff")
    public CommonResult<UnifiedChatSession> assignStaff(
            @PathVariable String sessionId,
            @RequestParam Long staffId) {
        
        try {
            UnifiedChatSession session = unifiedChatService.assignStaff(sessionId, staffId);
            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("分配客服失败", e);
            return CommonResult.failed("分配失败：" + e.getMessage());
        }
    }

    /**
     * 结束会话
     */
    @ApiOperation(value = "结束会话")
    @PostMapping("/session/{sessionId}/end")
    public CommonResult<Void> endSession(
            @PathVariable String sessionId,
            @RequestParam(required = false) String reason) {
        
        try {
            unifiedChatService.endSession(sessionId, reason);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("结束会话失败", e);
            return CommonResult.failed("结束失败：" + e.getMessage());
        }
    }

    /**
     * 标记消息已读
     */
    @ApiOperation(value = "标记消息已读")
    @PostMapping("/message/{messageId}/read")
    public CommonResult<Void> markMessageRead(
            @PathVariable String messageId,
            @RequestParam Long readerId,
            @RequestParam String readerType) {
        
        try {
            unifiedChatService.markMessageAsRead(messageId, readerId, readerType);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return CommonResult.failed("操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户活跃会话
     */
    @ApiOperation(value = "获取用户活跃会话")
    @GetMapping("/user-sessions")
    public CommonResult<List<UnifiedChatSession>> getUserActiveSessions(
            @RequestParam Long userId,
            @RequestParam String userType,
            @RequestParam Long merId) {
        
        try {
            List<UnifiedChatSession> sessions = unifiedChatService.getUserActiveSessions(userId, userType, merId);
            return CommonResult.success(sessions);
        } catch (Exception e) {
            log.error("获取用户活跃会话失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取客服活跃会话
     */
    @ApiOperation(value = "获取客服活跃会话")
    @GetMapping("/staff-sessions")
    public CommonResult<List<UnifiedChatSession>> getStaffActiveSessions(
            @RequestParam Long staffId,
            @RequestParam Long merId) {
        
        try {
            List<UnifiedChatSession> sessions = unifiedChatService.getStaffActiveSessions(staffId, merId);
            return CommonResult.success(sessions);
        } catch (Exception e) {
            log.error("获取客服活跃会话失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取等待队列
     */
    @ApiOperation(value = "获取等待队列")
    @GetMapping("/waiting-queue")
    public CommonResult<List<UnifiedChatSession>> getWaitingQueue(@RequestParam Long merId) {
        try {
            List<UnifiedChatSession> queue = unifiedChatService.getWaitingQueue(merId);
            return CommonResult.success(queue);
        } catch (Exception e) {
            log.error("获取等待队列失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 流式发送消息（兼容原有企业聊天接口）
     */
    @ApiOperation(value = "流式发送消息")
    @PostMapping("/message/stream")
    public SseEmitter streamMessage(
            @RequestBody @Validated SendMessageRequest request,
            HttpServletRequest httpRequest) {
        
        SseEmitter emitter = new SseEmitter(30000L); // 30秒超时
        
        try {
            // 异步处理消息发送和AI回复
            CompletableFuture.runAsync(() -> {
                try {
                    // 发送用户消息
                    MessageResponse userMessage = unifiedChatService.sendMessage(request);
                    
                    // 发送用户消息事件
                    Map<String, Object> userEvent = new HashMap<>();
                    userEvent.put("type", "user_message");
                    userEvent.put("data", userMessage);
                    emitter.send(userEvent);
                    
                    // 如果需要AI回复
                    if (Boolean.TRUE.equals(request.getNeedAiReply())) {
                        // 获取会话信息
                        UnifiedChatSession session = unifiedChatService.getSession(request.getSessionId());
                        if (session != null && session.isCurrentlyAiService()) {
                            // 发送AI处理中事件
                            Map<String, Object> processingMap = new HashMap<>();
                            processingMap.put("type", "ai_processing");
                            processingMap.put("status", "processing");
                            Map<String, Object> processingEvent = new HashMap<>();
                            processingEvent.put("type", "ai_processing");
                            processingEvent.put("data",processingMap);
                            emitter.send(processingEvent);
                            
                            // 处理AI回复
                            MessageResponse aiReply = unifiedChatService.processAiReply(
                                convertToUnifiedMessage(userMessage));
                            
                            if (aiReply != null) {
                                // 发送AI回复事件
                                Map<String, Object> aiEvent = new HashMap<>();
                                aiEvent.put("type", "ai_reply");
                                aiEvent.put("data", aiReply);
                                emitter.send(aiEvent);
                            }
                        }
                    }
                    
                    // 发送完成事件
                    Map<String, Object> completeEvent = new HashMap<>();
                    completeEvent.put("type", "complete");
                    Map<String, Object> processingMap = new HashMap<>();
                    processingMap.put("status", "success");
                    completeEvent.put("data", processingMap);
                    emitter.send(completeEvent);
                    
                    emitter.complete();
                    
                } catch (Exception e) {
                    log.error("流式消息处理失败", e);
                    try {
                        Map<String, Object> errorEvent = new HashMap<>();
                        errorEvent.put("type", "error");
                        Map<String, Object> processingMap = new HashMap<>();
                        processingMap.put("error", e.getMessage());
                        errorEvent.put("data", processingMap);
                        emitter.send(errorEvent);
                    } catch (Exception sendError) {
                        log.error("发送错误事件失败", sendError);
                    }
                    emitter.completeWithError(e);
                }
            });
            
        } catch (Exception e) {
            log.error("创建流式响应失败", e);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * 更新会话状态
     */
    @ApiOperation(value = "更新会话状态")
    @PutMapping("/session/{sessionId}/status")
    public CommonResult<Void> updateSessionStatus(
            @PathVariable String sessionId,
            @RequestParam String status) {
        
        try {
            unifiedChatService.updateSessionStatus(sessionId, status);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("更新会话状态失败", e);
            return CommonResult.failed("更新失败：" + e.getMessage());
        }
    }

    // 私有辅助方法
    private com.zbkj.common.model.chat.UnifiedChatMessage convertToUnifiedMessage(MessageResponse response) {
        if (response == null) {
            return null;
        }
        
        com.zbkj.common.model.chat.UnifiedChatMessage message = new com.zbkj.common.model.chat.UnifiedChatMessage();
        
        // 基本信息
        message.setMessageId(response.getMessageId());
        message.setSessionId(response.getSessionId());
        
        // 发送者信息
        message.setSenderId(response.getSenderId());
        message.setSenderType(response.getSenderType());
        message.setSenderName(response.getSenderName());
        message.setSenderAvatar(response.getSenderAvatar());
        
        // 接收者信息
        message.setReceiverId(response.getReceiverId());
        message.setReceiverType(response.getReceiverType());
        
        // 消息内容
        message.setRole(response.getRole());
        message.setMessageType(response.getMessageType());
        message.setContent(response.getContent());
        message.setContentType(response.getContentType());
        message.setAttachments(response.getAttachments());
        
        // 消息状态
        message.setStatus(response.getStatus());
        message.setIsRead(response.getIsRead() != null ? response.getIsRead() : false);
        message.setIsSystemMessage(response.getIsSystemMessage() != null ? response.getIsSystemMessage() : false);
        message.setRelatedMessageId(response.getRelatedMessageId());
        
        // 时间信息
        message.setCreateTime(response.getCreateTime());
        message.setUpdateTime(response.getUpdateTime());
        
        // 设置默认值
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        if (message.getIsSystemMessage() == null) {
            message.setIsSystemMessage(false);
        }
        if (message.getStatus() == null) {
            message.setStatus(com.zbkj.common.model.chat.UnifiedChatMessage.STATUS_SENT);
        }
        
        return message;
    }

    /**
     * 获取会话列表（支持多种筛选条件）
     */
    @ApiOperation(value = "获取会话列表")
    @GetMapping("/sessions")
    public CommonResult<com.github.pagehelper.PageInfo<UnifiedChatSession>> getSessionList(
            @RequestParam(required = false) String sessionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String cozeBotId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            // 获取当前平台管理员信息
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            // 调用统一聊天服务获取分页会话列表
            com.github.pagehelper.PageInfo<UnifiedChatSession> sessions = 
                unifiedChatService.getSessionsForManagement(merId, sessionType, status, userId, cozeBotId, sessionId, page, size);
            
            return CommonResult.success(sessions);
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 基于联系人创建或获取会话
     */
    @ApiOperation(value = "基于联系人创建或获取会话")
    @PostMapping("/session/contact")
    public CommonResult<UnifiedChatSession> createOrGetSessionByContact(
            @RequestParam Long contactId,
            @RequestParam String contactType,
            @RequestParam(defaultValue = "HUMAN") String sessionType) {
        
        try {
            // 获取当前登录的管理员信息
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            Long operatorId = loginUser.getUser().getId().longValue(); // 当前管理员ID（操作者）
            
            // 根据联系人信息创建或获取会话
            UnifiedChatSession session = unifiedChatService.createOrGetSessionByContact(
                contactId, contactType, merId, operatorId, sessionType);
            
            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("基于联系人创建会话失败", e);
            return CommonResult.failed("创建失败：" + e.getMessage());
        }
    }

    /**
     * 获取联系人列表
     */
    @ApiOperation(value = "获取联系人列表")
    @GetMapping("/contacts")
    public CommonResult<List<Map<String, Object>>> getContacts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            // 获取当前登录的管理员信息
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            Long currentUserId = loginUser.getUser().getId().longValue(); // 当前管理员ID
            
            // 调用服务获取与当前用户相关的联系人列表（包含最新消息）
            List<Map<String, Object>> contacts = unifiedChatService.getContactListWithMessages(merId, currentUserId, page, size);
            
            return CommonResult.success(contacts);
            
        } catch (Exception e) {
            log.error("获取联系人列表失败", e);
            return CommonResult.failed("获取联系人列表失败: " + e.getMessage());
        }
    }


    /**
     * 向联系人发送消息
     */
    @ApiOperation(value = "向联系人发送消息")
    @PostMapping("/message/contact")
    public CommonResult<MessageResponse> sendMessageToContact(
            @RequestParam Long contactId,
            @RequestParam String contactType,
            @RequestBody SendMessageRequest request) {
        
        try {
            // 基本参数验证
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return CommonResult.failed("消息内容不能为空");
            }
            
            // 获取当前登录的管理员信息
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            Long senderId = loginUser.getUser().getId().longValue(); // 当前管理员ID（发送者）
            
            // 首先创建或获取会话
            UnifiedChatSession session = unifiedChatService.createOrGetSessionByContact(
                contactId, contactType, merId, senderId, "HUMAN");
            
            // 设置会话ID到请求中
            request.setSessionId(session.getSessionId());
            request.setSenderId(senderId);
            request.setSenderType("PLATFORM"); // 发送者是平台管理员
            
            // 设置接收者信息
            request.setReceiverId(contactId);
            request.setReceiverType(contactType);
            
            // 设置默认值
            if (request.getMessageType() == null) {
                request.setMessageType("text");
            }
            if (request.getContentType() == null) {
                request.setContentType("text");
            }
            
            // 设置不需要AI回复
            request.setNeedAiReply(false);
            
            // 发送消息
            MessageResponse response = unifiedChatService.sendMessage(request);
            
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("向联系人发送消息失败", e);
            return CommonResult.failed("发送失败：" + e.getMessage());
        }
    }

    /**
     * 同步Coze会话到统一会话表
     */
    @ApiOperation(value = "同步Coze会话")
    @PostMapping("/sync-coze-conversations")
    public CommonResult<String> syncCozeConversations(
            @RequestParam String botId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        
        try {
            // 构建Coze会话列表请求
            com.zbkj.common.request.coze.CozeGetConversationListRequest request = 
                new com.zbkj.common.request.coze.CozeGetConversationListRequest();
            request.setBotId(botId);
            request.setPageNum(pageNum);
            request.setPageSize(pageSize);
            request.setSortOrder("desc");
            request.setConnectorId("1024"); // API渠道
            
            // 调用Coze服务获取会话列表（会自动同步到统一表）
            com.zbkj.common.response.coze.CozeGetConversationListResponse response = 
                cozeService.getConversationList(request);
            
            if (response != null && response.getCode() == 0) {
                int syncedCount = response.getData() != null && response.getData().getConversations() != null ? 
                    response.getData().getConversations().size() : 0;
                return CommonResult.success("同步成功，共同步 " + syncedCount + " 个会话");
            } else {
                return CommonResult.failed("同步失败：" + (response != null ? response.getMsg() : "未知错误"));
            }
        } catch (Exception e) {
            log.error("同步Coze会话失败", e);
            return CommonResult.failed("同步失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表（统一表）
     */
    @ApiOperation(value = "获取会话消息列表")
    @GetMapping("/session/{sessionId}/messages")
    public CommonResult<List<com.zbkj.common.response.chat.MessageResponse>> getUnifiedSessionMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            List<com.zbkj.common.response.chat.MessageResponse> messages = 
                unifiedChatService.getSessionMessages(sessionId, page, size);
            return CommonResult.success(messages);
        } catch (Exception e) {
            log.error("获取会话消息列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 同步Coze消息到统一消息表
     */
    @ApiOperation(value = "同步Coze消息")
    @PostMapping("/sync-coze-messages")
    public CommonResult<String> syncCozeMessages(
            @RequestParam String conversationId,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String chatId,
            @RequestParam(required = false) String beforeId,
            @RequestParam(required = false) String afterId,
            @RequestParam(defaultValue = "50") Integer limit) {
        
        try {
            // 构建Coze消息列表请求
            com.zbkj.common.request.coze.CozeGetMessageListRequest request = 
                new com.zbkj.common.request.coze.CozeGetMessageListRequest();
            request.setConversationId(conversationId);
            request.setOrder(order);
            request.setChatId(chatId);
            request.setBeforeId(beforeId);
            request.setAfterId(afterId);
            request.setLimit(limit);
            
            // 调用Coze服务获取消息列表（会自动同步到统一表）
            com.zbkj.common.response.coze.CozeGetMessageListResponse response = 
                cozeService.getMessageList(request);
            
            if (response != null && response.getCode() == 0) {
                int syncedCount = response.getData() != null ? response.getData().size() : 0;
                return CommonResult.success("同步成功，共同步 " + syncedCount + " 条消息");
            } else {
                return CommonResult.failed("同步失败：" + (response != null ? response.getMsg() : "未知错误"));
            }
        } catch (Exception e) {
            log.error("同步Coze消息失败", e);
            return CommonResult.failed("同步失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "标记消息为已读")
    @PostMapping("/messages/mark-read")
    public CommonResult<Void> markMessagesAsRead(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String messageId = (String) request.get("messageId");
            
            if (sessionId != null) {
                // 标记整个会话的消息为已读
                unifiedChatService.markSessionMessagesAsRead(sessionId);
            } else if (messageId != null) {
                // 标记单条消息为已读
                unifiedChatService.markMessageAsRead(messageId);
            }
            
            return CommonResult.success();
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return CommonResult.failed("标记消息已读失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取消息已读状态")
    @GetMapping("/messages/{messageId}/read-status")
    public CommonResult<Map<String, Object>> getMessageReadStatus(@PathVariable String messageId) {
        try {
            Map<String, Object> readStatus = unifiedChatService.getMessageReadStatus(messageId);
            return CommonResult.success(readStatus);
        } catch (Exception e) {
            log.error("获取消息已读状态失败", e);
            return CommonResult.failed("获取消息已读状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取可添加的用户列表（根据类型）
     */
    @ApiOperation(value = "获取可添加的用户列表")
    @GetMapping("/available-users")
    public CommonResult<List<Map<String, Object>>> getAvailableUsers(
            @RequestParam String userType,
            @RequestParam(required = false) String keyword) {
        
        try {
            List<Map<String, Object>> users = unifiedChatService.getAvailableUsersByType(userType, keyword);
            return CommonResult.success(users);
        } catch (Exception e) {
            log.error("获取可添加用户列表失败", e);
            return CommonResult.failed("获取用户列表失败: " + e.getMessage());
        }
    }
}
