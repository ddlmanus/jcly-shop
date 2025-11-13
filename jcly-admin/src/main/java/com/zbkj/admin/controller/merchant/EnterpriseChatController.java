package com.zbkj.admin.controller.merchant;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.EnterpriseChatSessionRequest;
import com.zbkj.common.request.EnterpriseChatMessageRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.model.coze.EnterpriseChatSession;
import com.zbkj.common.model.coze.EnterpriseChatMessage;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.EnterpriseChatService;
import com.zbkj.service.service.UnifiedChatService;
import com.zbkj.common.request.chat.SendMessageRequest;
import com.zbkj.common.response.chat.MessageResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * 企业级AI聊天控制器
 * 提供企业级聊天功能的API接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/enterprise-chat")
@Api(tags = "商户端 - 企业级AI聊天管理")
public class EnterpriseChatController {

    @Autowired
    private EnterpriseChatService enterpriseChatService;
    
    @Autowired
    private com.zbkj.service.service.CozeStreamClient cozeStreamClient;
    
    @Autowired
    private com.zbkj.service.service.CozeBotService cozeBotService;
    
    @Autowired
    private com.zbkj.admin.filter.TokenComponent tokenComponent;

    @Autowired
    private UnifiedChatService unifiedChatService;

    /**
     * 获取聊天会话列表（使用统一聊天服务）
     */
    @ApiOperation(value = "获取聊天会话列表")
   // @PreAuthorize("hasAuthority('merchant:enterprise-chat:session:list')")
    @GetMapping("/sessions")
    public CommonResult<List<com.zbkj.common.model.chat.UnifiedChatSession>> getSessionList(
            @RequestParam(required = false) String cozeBotId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            // 获取当前商户的会话列表
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId().longValue();
            
            List<com.zbkj.common.model.chat.UnifiedChatSession> sessions = 
                unifiedChatService.getUserActiveSessions(merId, "MERCHANT", merId);
            
            // 根据cozeBotId和status过滤
            if (cozeBotId != null || status != null) {
                sessions = sessions.stream()
                    .filter(session -> (cozeBotId == null || cozeBotId.equals(session.getCozeBotId())))
                    .filter(session -> (status == null || status.equals(session.getStatus())))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return CommonResult.success(sessions);
        } catch (Exception e) {
            log.error("获取会话列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建聊天会话（使用统一聊天服务）
     */
    @ApiOperation(value = "创建聊天会话")
 //   @PreAuthorize("hasAuthority('merchant:enterprise-chat:session:create')")
    @PostMapping("/session")
    public CommonResult<com.zbkj.common.model.chat.UnifiedChatSession> createSession(
            @RequestBody @Validated EnterpriseChatSessionRequest request) {
        
        try {
            // 从当前登录用户获取用户ID和商户ID
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();
            
            // 使用统一聊天服务创建会话
            com.zbkj.common.model.chat.UnifiedChatSession session = unifiedChatService.createOrGetSession(
                merId.longValue(), // 商户端用户ID = 商户ID
                "MERCHANT", // 商户用户类型
                merId.longValue(),
                "AI", // AI会话类型
                request.getCozeBotId()
            );
            
            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("创建聊天会话失败: {}", e.getMessage(), e);
            return CommonResult.failed("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话详情（使用统一聊天服务）
     */
    @ApiOperation(value = "获取会话详情")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:session:detail')")
    @GetMapping("/session/{sessionId}")
    public CommonResult<com.zbkj.common.model.chat.UnifiedChatSession> getSessionDetail(@PathVariable String sessionId) {
        try {
            com.zbkj.common.model.chat.UnifiedChatSession session = unifiedChatService.getSession(sessionId);
            if (session == null) {
                return CommonResult.failed("会话不存在");
            }
            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("获取会话详情失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取会话失败：" + e.getMessage());
        }
    }

    /**
     * 更新会话信息
     */
    @ApiOperation(value = "更新会话信息")
   // @PreAuthorize("hasAuthority('merchant:enterprise-chat:session:update')")
    @PutMapping("/session/{sessionId}")
    public CommonResult<EnterpriseChatSession> updateSession(
            @PathVariable String sessionId,
            @RequestBody @Validated EnterpriseChatSessionRequest request) {
        
        EnterpriseChatSession session = enterpriseChatService.updateSession(sessionId, request);
        return CommonResult.success(session);
    }

    /**
     * 删除会话
     */
    @ApiOperation(value = "删除会话")
   // @PreAuthorize("hasAuthority('merchant:enterprise-chat:session:delete')")
    @DeleteMapping("/session/{sessionId}")
    public CommonResult<Void> deleteSession(@PathVariable String sessionId) {
        enterpriseChatService.deleteSession(sessionId);
        return CommonResult.success();
    }

    /**
     * 清空会话历史
     */
    @ApiOperation(value = "清空会话历史")
   // @PreAuthorize("hasAuthority('merchant:enterprise-chat:session:clear')")
    @DeleteMapping("/session/{sessionId}/clear")
    public CommonResult<Void> clearSessionHistory(@PathVariable String sessionId) {
        enterpriseChatService.clearSessionHistory(sessionId);
        return CommonResult.success();
    }

    /**
     * 发送聊天消息（GET方式的流式响应，支持EventSource）
     * 支持EventSource的流式输出
     * 通过URL参数传递token进行身份验证
     */
    @CrossOrigin
    @ApiOperation(value = "发送聊天消息（GET流式响应）")
    @GetMapping(value = "/message/stream-get", produces = "text/event-stream")
    public SseEmitter sendMessageStreamGet(
            @RequestParam String sessionId,
            @RequestParam String content,
            @RequestParam(defaultValue = "text") String contentType,
            @RequestParam(defaultValue = "true") String enableStream,
            @RequestParam(required = false) String token) {
        
        log.info("开始处理企业聊天GET流式消息，会话ID: {}, 内容: {}", sessionId, content);
        
        // 创建SSE发射器 - 即使认证失败也要返回，以确保正确的MIME类型
        SseEmitter emitter = new SseEmitter(3600000L); // 1小时超时
        
        // 验证token并设置认证上下文
        com.zbkj.common.vo.LoginUserVo currentUser = null;
        if (cn.hutool.core.util.StrUtil.isNotBlank(token)) {
            try {
                // 使用TokenComponent验证token
                currentUser = validateTokenFromUrl(token);
                if (currentUser == null) {
                    log.error("Token验证失败，token: {}", token);
                    sendErrorAndComplete(emitter, "认证失败：无效的token");
                    return emitter;
                }
                
                // 设置Spring Security认证上下文
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authenticationToken = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        currentUser, null, currentUser.getAuthorities());
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .setAuthentication(authenticationToken);
                
                log.info("Token验证成功，用户: {}, 商户ID: {}", 
                    currentUser.getUser().getAccount(), currentUser.getUser().getMerId());
                    
            } catch (Exception e) {
                log.error("Token验证过程中发生异常: {}", e.getMessage(), e);
                sendErrorAndComplete(emitter, "认证失败：" + e.getMessage());
                return emitter;
            }
        } else {
            log.error("缺少token参数");
            sendErrorAndComplete(emitter, "认证失败：缺少token参数");
            return emitter;
        }
        
        // 创建请求对象
        EnterpriseChatMessageRequest request = new EnterpriseChatMessageRequest();
        request.setSessionId(sessionId);
        request.setContent(content);
        request.setContentType(contentType);
        request.setEnableStream(Boolean.parseBoolean(enableStream));
        
        // 直接调用流式处理逻辑，不依赖sendMessageStream方法
        // 因为sendMessageStream会重新获取用户上下文，可能导致问题
        return handleStreamMessageDirectly(request, currentUser);
    }
    
    /**
     * 直接处理流式消息（GET方式专用）
     * 使用已验证的用户上下文，避免重复认证问题
     */
    private SseEmitter handleStreamMessageDirectly(EnterpriseChatMessageRequest request, 
                                                  com.zbkj.common.vo.LoginUserVo userContext) {
        
        log.info("开始处理GET方式流式消息，会话ID: {}, 内容: {}", 
                request.getSessionId(), request.getContent());
        
        // 强制启用流式响应
        request.setEnableStream(true);
        
        // 创建SSE发射器，设置超时时间为1小时 (3600秒)
        SseEmitter emitter = new SseEmitter(3600000L);
        
        // 设置SSE连接的错误和超时处理
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，会话ID: {}", request.getSessionId());
            emitter.complete();
        });
        
        emitter.onError((throwable) -> {
            log.error("SSE连接发生错误，会话ID: {}, 错误: {}", 
                     request.getSessionId(), throwable.getMessage());
            emitter.complete();
        });
        
        emitter.onCompletion(() -> {
            log.info("SSE连接已完成，会话ID: {}", request.getSessionId());
        });
        
        // 使用传入的用户上下文
        final com.zbkj.common.vo.LoginUserVo finalUserContext = userContext;
        
        // 立即发送连接建立事件，确保前端知道连接成功
        try {
            emitter.send(SseEmitter.event()
                .name("connection")
                .data("{\"status\": \"connected\", \"sessionId\": \"" + request.getSessionId() + "\"}"));
            log.info("✅ GET方式SSE连接建立事件立即发送成功");
        } catch (Exception e) {
            log.error("❌ 发送连接建立事件失败", e);
            return emitter;
        }

        // 改为同步处理流式响应，避免异步执行时机问题
        log.info("🚀 GET方式同步任务开始执行，会话ID: {}", request.getSessionId());
        
        try {
            // 发送处理开始事件
            emitter.send(SseEmitter.event()
                .name("processing_started")
                .data("{\"status\": \"processing_started\", \"timestamp\": " + System.currentTimeMillis() + "}"));
            log.info("✅ GET方式处理开始事件发送成功");
            
            try {
                
                // 获取或创建会话
                log.info("📋 GET步骤1: 获取或创建会话");
                log.info("🔍 用户上下文检查: finalUserContext = {}", finalUserContext != null ? "存在" : "null");
                if (finalUserContext != null) {
                    log.info("🔍 用户信息: account={}, merId={}", 
                        finalUserContext.getUser() != null ? finalUserContext.getUser().getAccount() : "null",
                        finalUserContext.getUser() != null ? finalUserContext.getUser().getMerId() : "null");
                }
                
                com.zbkj.common.model.coze.EnterpriseChatSession session;
                try {
                    if (finalUserContext != null) {
                        log.info("使用GET验证的用户上下文获取会话: {}", request.getSessionId());
                        session = enterpriseChatService.getSessionDetail(request.getSessionId(), finalUserContext);
                    } else {
                        log.error("❌ 用户上下文为空，无法获取会话");
                        throw new RuntimeException("用户上下文为空");
                    }
                    log.info("✅ 会话获取成功: {}", session.getSessionId());
                } catch (Exception e) {
                    log.warn("⚠️ 会话不存在，尝试创建新会话: {}", e.getMessage());
                    log.warn("⚠️ 会话获取异常详情: ", e);
                    try {
                        session = createTestSessionIfNeeded(request.getSessionId(), finalUserContext);
                        log.info("✅ 新会话创建成功: {}", session.getSessionId());
                    } catch (Exception createEx) {
                        log.error("❌ 创建新会话失败: {}", createEx.getMessage(), createEx);
                        throw createEx; // 重新抛出异常，让外层catch处理
                    }
                }
                
                // 发送会话准备完成事件
                try {
                    emitter.send(SseEmitter.event()
                        .name("session_ready")
                        .data("{\"sessionId\": \"" + session.getSessionId() + "\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                    log.info("✅ 会话准备完成事件发送成功");
                } catch (Exception e) {
                    log.error("❌ 发送会话准备完成事件失败", e);
                    emitter.completeWithError(e);
                    return emitter;
                }
                
                // 使用统一聊天服务保存用户消息
                log.info("📝 GET步骤2: 保存用户消息到统一表");
                SendMessageRequest unifiedRequest = new SendMessageRequest();
                unifiedRequest.setSessionId(request.getSessionId());
                unifiedRequest.setContent(request.getContent());
                unifiedRequest.setMessageType(request.getMessageType() != null ? request.getMessageType() : "text");
                unifiedRequest.setContentType(request.getContentType() != null ? request.getContentType() : "text");
                unifiedRequest.setNeedAiReply(true); // 需要AI回复
                unifiedRequest.setAttachments(request.getAttachments());
                unifiedRequest.setMetaData(request.getMetaData());
                
                MessageResponse messageResponse = unifiedChatService.sendMessage(unifiedRequest);
                log.info("✅ 用户消息保存成功: {}", messageResponse.getMessageId());
                
                // 发送用户消息事件
                log.info("📤 GET步骤3: 发送用户消息事件");
                try {
                    emitter.send(SseEmitter.event()
                        .name("user_message")
                        .data("{\"messageId\": \"" + messageResponse.getMessageId() + "\", \"content\": \"" + messageResponse.getContent() + "\"}"));
                    log.info("✅ 用户消息事件发送成功");
                } catch (Exception e) {
                    log.error("❌ 发送用户消息事件失败", e);
                    emitter.completeWithError(e);
                    return emitter;
                }
                
                // 构建Coze API请求
                log.info("🔧 GET步骤4: 构建Coze API请求");
                java.util.Map<String, Object> cozeRequest = buildCozeStreamRequest(session, request);
                log.info("✅ Coze请求构建完成: {}", cozeRequest);
                
                log.info("🌊 GET步骤5: 开始调用Coze流式客户端");
                
                // 发送处理中事件，防止连接超时
                try {
                    emitter.send(SseEmitter.event()
                        .name("processing")
                        .data("{\"status\": \"processing\", \"message\": \"正在连接Coze API...\"}"));
                    log.info("✅ 处理中事件发送成功");
                } catch (Exception e) {
                    log.error("❌ 发送处理中事件失败", e);
                }
                
                com.zbkj.common.model.coze.stream.CozeStreamResponse streamResponse = 
                    cozeStreamClient.startStreamChat(cozeRequest, (eventData) -> {
                        try {
                            log.info("🔄 收到流式事件，准备转发: {}", eventData);
                            
                            // 简化事件处理，只处理核心事件类型
                            if (eventData.contains("event:") && eventData.contains("data:")) {
                                String[] lines = eventData.split("\n");
                                String eventType = null;
                                StringBuilder dataBuilder = new StringBuilder();
                                
                                for (String line : lines) {
                                    line = line.trim();
                                    if (line.startsWith("event:")) {
                                        eventType = line.substring(6).trim();
                                    } else if (line.startsWith("data:")) {
                                        if (dataBuilder.length() > 0) {
                                            dataBuilder.append("\n");
                                        }
                                        dataBuilder.append(line.substring(5).trim());
                                    }
                                }
                                
                                // 根据Coze官方文档处理核心事件类型
                                if (eventType != null && dataBuilder.length() > 0) {
                                    if ("conversation.message.delta".equals(eventType)) {
                                        // 处理增量消息 - 直接使用官方事件名
                                        emitter.send(SseEmitter.event()
                                            .name("conversation.message.delta")
                                            .data(dataBuilder.toString()));
                                        log.info("✅ 增量消息事件转发成功: conversation.message.delta");
                                    } else if ("conversation.message.completed".equals(eventType)) {
                                        // 处理消息完成事件 - 直接使用官方事件名
                                        emitter.send(SseEmitter.event()
                                            .name("conversation.message.completed")
                                            .data(dataBuilder.toString()));
                                        log.info("✅ 消息完成事件转发成功: conversation.message.completed");
                                    } else if ("conversation.chat.completed".equals(eventType)) {
                                        // 处理对话完成事件 - 直接使用官方事件名
                                        emitter.send(SseEmitter.event()
                                            .name("conversation.chat.completed")
                                            .data(dataBuilder.toString()));
                                        log.info("✅ 对话完成事件转发成功: conversation.chat.completed");
                                    } else if ("conversation.chat.created".equals(eventType)) {
                                        // 处理对话创建事件
                                        emitter.send(SseEmitter.event()
                                            .name("conversation.chat.created")
                                            .data(dataBuilder.toString()));
                                        log.info("✅ 对话创建事件转发成功: conversation.chat.created");
                                    } else if ("conversation.chat.in_progress".equals(eventType)) {
                                        // 处理对话进行中事件
                                        emitter.send(SseEmitter.event()
                                            .name("conversation.chat.in_progress")
                                            .data(dataBuilder.toString()));
                                        log.info("✅ 对话进行中事件转发成功: conversation.chat.in_progress");
                                    } else if ("done".equals(eventType)) {
                                        // 处理完成事件
                                        emitter.send(SseEmitter.event()
                                            .name("done")
                                            .data(dataBuilder.toString()));
                                        log.info("✅ 流式响应完成事件转发成功: done");
                                    } else {
                                        log.debug("忽略事件类型: {}", eventType);
                                    }
                                }
                            } else {
                                // 如果不是嵌套格式，直接转发
                                emitter.send(SseEmitter.event()
                                    .name("ai_stream")
                                    .data(eventData));
                                log.info("✅ 直接流式事件转发成功");
                            }
                        } catch (Exception e) {
                            log.error("❌ 发送流式事件失败: {}", e.getMessage());
                        }
                    });
                log.info("✅ GET步骤5完成: Coze流式客户端调用完成");
                
                // 处理完整的流式响应
                log.info("📋 GET步骤6: 处理完整的流式响应");
                com.zbkj.common.model.coze.EnterpriseChatMessage assistantMessage = 
                    enterpriseChatService.processStreamResponse(
                        request.getSessionId(), streamResponse, messageResponse.getMessageId());
                log.info("✅ 流式响应处理完成");
                
                if (assistantMessage != null) {
                    enterpriseChatService.saveMessage(assistantMessage);
                    
                    try {
                        emitter.send(SseEmitter.event()
                            .name("assistant_message")
                            .data(assistantMessage));
                        log.info("✅ AI回复完成事件发送成功");
                    } catch (Exception e) {
                        log.error("❌ 发送AI回复完成事件失败", e);
                    }
                }
                
                // 发送完成事件
                try {
                    emitter.send(SseEmitter.event()
                        .name("completed")
                        .data("对话完成"));
                    log.info("✅ 对话完成事件发送成功");
                } catch (Exception e) {
                    log.error("❌ 发送对话完成事件失败", e);
                }
                
                try {
                    emitter.complete();
                    log.info("✅ GET方式SSE流正常完成");
                } catch (Exception e) {
                    log.error("❌ SSE流完成时出错", e);
                }
                
                log.info("GET方式企业聊天流式消息处理完成，会话ID: {}", request.getSessionId());
                
            } catch (Exception e) {
                log.error("❌ GET方式企业聊天流式消息处理失败，会话ID: {}, 错误: {}", 
                         request.getSessionId(), e.getMessage(), e);
                log.error("❌ 详细异常堆栈:", e); // 打印完整堆栈信息
                
                try {
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "未知错误";
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\": \"" + errorMessage.replace("\"", "\\\"") + "\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                    log.info("✅ 错误事件发送成功");
                    emitter.complete();
                    log.info("✅ SSE连接因错误而完成");
                } catch (Exception ex) {
                    log.error("❌ 发送错误事件失败，强制完成连接", ex);
                    emitter.completeWithError(ex);
                }
            }
        } catch (Exception outerE) {
            log.error("❌ GET方式处理开始事件发送失败", outerE);
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\": \"处理开始失败: " + outerE.getMessage().replace("\"", "\\\"") + "\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
        
        log.info("返回GET方式SSE emitter，同步处理已完成");
        return emitter;
    }
    
    /**
     * 通过SSE发送错误事件并完成连接（辅助方法）
     */
    private void sendErrorAndComplete(SseEmitter emitter, String errorMessage) {
        try {
            // 发送错误事件
            emitter.send(SseEmitter.event()
                .name("error")
                .data("{\"error\": \"" + errorMessage.replace("\"", "\\\"") + "\", \"timestamp\": " + System.currentTimeMillis() + "}"));
            
            // 完成连接
            emitter.complete();
        } catch (Exception e) {
            log.error("发送错误事件失败", e);
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 从URL参数验证token（辅助方法）
     * 模拟HTTP请求头验证流程
     */
    private com.zbkj.common.vo.LoginUserVo validateTokenFromUrl(String token) {
        try {
            // 创建模拟的HttpServletRequest来使用TokenComponent
            MockHttpServletRequest mockRequest = new MockHttpServletRequest();
            mockRequest.addHeader(com.zbkj.common.constants.Constants.HEADER_AUTHORIZATION_KEY, token);
            
            // 使用注入的TokenComponent验证token
            com.zbkj.common.vo.LoginUserVo loginUser = tokenComponent.getLoginUser(mockRequest);
            
            if (loginUser != null) {
                // 验证token有效期
                tokenComponent.verifyToken(loginUser);
                log.info("Token验证成功，用户: {}", loginUser.getUser().getAccount());
                return loginUser;
            } else {
                log.warn("Token无效或已过期: {}", token);
                return null;
            }
            
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取聊天消息列表（使用统一聊天服务）
     */
    @ApiOperation(value = "获取聊天消息列表")
   // @PreAuthorize("hasAuthority('merchant:enterprise-chat:message:list')")
    @GetMapping("/messages")
    public CommonResult<List<MessageResponse>> getMessageList(
            @RequestParam String sessionId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String messageType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        
        try {
            List<MessageResponse> messages = unifiedChatService.getSessionMessages(sessionId, page, size);
            
            // 根据role和messageType过滤
            if (role != null || messageType != null) {
                messages = messages.stream()
                    .filter(msg -> (role == null || role.equals(msg.getRole())))
                    .filter(msg -> (messageType == null || messageType.equals(msg.getMessageType())))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return CommonResult.success(messages);
        } catch (Exception e) {
            log.error("获取聊天消息列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取消息失败：" + e.getMessage());
        }
    }

    /**
     * 发送聊天消息（使用统一聊天服务）
     * 该接口会：
     * 1. 保存用户消息到统一消息表
     * 2. 调用Coze API发起对话
     * 3. 保存AI回复到统一消息表
     * 4. 返回完整的对话结果
     */
    @ApiOperation(value = "发送聊天消息")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:message:send')")
    @PostMapping("/h5/message")
    public CommonResult<MessageResponse> sendMessage(
            @RequestBody @Validated EnterpriseChatMessageRequest request) {
        
        log.info("开始处理企业聊天消息，会话ID: {}, 内容: {}", 
                request.getSessionId(), request.getContent());
        
        try {
            // 转换为统一聊天请求
            SendMessageRequest unifiedRequest = new SendMessageRequest();
            unifiedRequest.setSessionId(request.getSessionId());
            unifiedRequest.setContent(request.getContent());
            unifiedRequest.setMessageType(request.getMessageType() != null ? request.getMessageType() : "text");
            unifiedRequest.setContentType(request.getContentType() != null ? request.getContentType() : "text");
            unifiedRequest.setNeedAiReply(true); // 企业聊天需要AI回复
            unifiedRequest.setAttachments(request.getAttachments());
            unifiedRequest.setMetaData(request.getMetaData());
            
            // 调用统一聊天服务发送消息
            MessageResponse response = unifiedChatService.sendMessage(unifiedRequest);
            
            log.info("企业聊天消息处理完成，会话ID: {}, 消息ID: {}", 
                    request.getSessionId(), response.getMessageId());
            return CommonResult.success(response);
            
        } catch (Exception e) {
            log.error("企业聊天消息处理失败，会话ID: {}, 错误: {}", 
                     request.getSessionId(), e.getMessage(), e);
            return CommonResult.failed("发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 发送聊天消息（流式响应版本）
     * 支持Server-Sent Events (SSE) 实时流式输出
     * 客户端可以实时接收AI的回复内容，实现打字机效果
     */
    @ApiOperation(value = "发送聊天消息（流式响应）")
    @PostMapping(value = "/message/stream", produces = "text/event-stream")
    public SseEmitter sendMessageStream(
            @RequestBody @Validated EnterpriseChatMessageRequest request) {
        
        log.info("开始处理企业聊天流式消息，会话ID: {}, 内容: {}", 
                request.getSessionId(), request.getContent());
        
        // 强制启用流式响应
        request.setEnableStream(true);
        
        // 创建SSE发射器，设置超时时间为2分钟，确保AI处理不会超时
        SseEmitter emitter = new SseEmitter(3600000L); // 1小时超时
        
        // 设置SSE连接的错误和超时处理
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，会话ID: {}", request.getSessionId());
            emitter.complete();
        });
        
        emitter.onError((throwable) -> {
            log.error("SSE连接发生错误，会话ID: {}, 错误: {}", 
                     request.getSessionId(), throwable.getMessage());
            emitter.complete();
        });
        
        emitter.onCompletion(() -> {
            log.info("SSE连接已完成，会话ID: {}", request.getSessionId());
        });
        
        // 捕获当前用户上下文，用于异步线程中使用
        com.zbkj.common.vo.LoginUserVo currentUser = null;
        try {
            currentUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        } catch (Exception e) {
            log.warn("无法获取当前用户信息，可能未登录: {}", e.getMessage());
        }
        
        final com.zbkj.common.vo.LoginUserVo userContext = currentUser;
        
        // 异步处理流式响应
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            log.info("🚀 异步任务开始执行，会话ID: {}", request.getSessionId());
            
            // 立即发送异步任务开始事件，确认异步执行正常
            try {
                emitter.send(SseEmitter.event()
                    .name("async_started")
                    .data("{\"status\": \"async_task_started\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                log.info("✅ 异步任务开始事件发送成功");
            } catch (Exception e) {
                log.error("❌ 发送异步任务开始事件失败，emitter可能已断开", e);
                return; // 如果连基本的send都失败了，直接返回
            }
            
            try {
                // 保存用户消息到数据库
                log.info("保存用户消息并发起流式对话");
                
                // 获取或创建会话
                log.info("📋 步骤1: 获取或创建会话");
                com.zbkj.common.model.coze.EnterpriseChatSession session;
                try {
                    if (userContext != null) {
                        log.info("使用用户上下文获取会话: {}", request.getSessionId());
                        // 使用统一聊天服务获取会话
                        com.zbkj.common.model.chat.UnifiedChatSession unifiedSession = 
                            unifiedChatService.getSession(request.getSessionId());
                        
                        // 转换为EnterpriseChatSession（临时兼容）
                        session = convertToEnterpriseChatSession(unifiedSession);
                    } else {
                        // 如果没有用户上下文，尝试使用统一服务
                        log.warn("用户上下文为空，尝试使用统一服务获取会话");
                        com.zbkj.common.model.chat.UnifiedChatSession unifiedSession = 
                            unifiedChatService.getSession(request.getSessionId());
                        session = convertToEnterpriseChatSession(unifiedSession);
                    }
                    log.info("✅ 会话获取成功: {}", session.getSessionId());
                } catch (Exception e) {
                    log.warn("会话不存在，尝试创建新会话: {}", e.getMessage());
                    // 如果会话不存在，创建新会话（仅在测试环境中）
                    session = createTestSessionIfNeeded(request.getSessionId(), userContext);
                    log.info("✅ 新会话创建成功: {}", session.getSessionId());
                }
                
                // 使用统一聊天服务保存用户消息
                log.info("📝 步骤2: 保存用户消息到统一表");
                SendMessageRequest unifiedRequest = new SendMessageRequest();
                unifiedRequest.setSessionId(request.getSessionId());
                unifiedRequest.setContent(request.getContent());
                unifiedRequest.setMessageType(request.getMessageType() != null ? request.getMessageType() : "text");
                unifiedRequest.setContentType(request.getContentType() != null ? request.getContentType() : "text");
                unifiedRequest.setNeedAiReply(true); // 需要AI回复
                unifiedRequest.setAttachments(request.getAttachments());
                unifiedRequest.setMetaData(request.getMetaData());
                
                MessageResponse messageResponse = unifiedChatService.sendMessage(unifiedRequest);
                log.info("✅ 用户消息保存成功: {}", messageResponse.getMessageId());
                
                // 发送用户消息事件
                log.info("📤 步骤3: 发送用户消息事件，会话ID: {}", request.getSessionId());
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    String userMessageJson = objectMapper.writeValueAsString(messageResponse);
                    emitter.send(SseEmitter.event()
                        .name("user_message")
                        .data(userMessageJson));
                    log.info("✅ 用户消息事件发送成功");
                } catch (Exception e) {
                    log.error("❌ 发送用户消息事件失败，可能是emitter已断开", e);
                    try {
                        emitter.send(SseEmitter.event()
                            .name("user_message")
                            .data("{\"messageId\": \"" + messageResponse.getMessageId() + "\", \"content\": \"" + messageResponse.getContent() + "\"}"));
                        log.info("✅ 用户消息事件(简化版)发送成功");
                    } catch (Exception e2) {
                        log.error("❌ 连简化的用户消息事件也发送失败，emitter已断开", e2);
                        return; // SSE连接已断开，停止处理
                    }
                }
                
                // 构建Coze API请求
                log.info("🔧 步骤4: 构建Coze API请求");
                java.util.Map<String, Object> cozeRequest = buildCozeStreamRequest(session, request);
                log.info("✅ Coze请求构建完成: {}", cozeRequest);
                
                log.info("🌊 步骤5: 开始调用Coze流式客户端");
                com.zbkj.common.model.coze.stream.CozeStreamResponse streamResponse = 
                    cozeStreamClient.startStreamChat(cozeRequest, (eventData) -> {
                        try {
                            log.info("🔄 收到流式事件，准备转发: {}", eventData);
                            
                            // 解析嵌套的SSE格式并直接发送对应的事件类型
                            if (eventData.contains("event:") && eventData.contains("data:")) {
                                String[] lines = eventData.split("\n");
                                String eventType = null;
                                StringBuilder dataBuilder = new StringBuilder();
                                
                                for (String line : lines) {
                                    line = line.trim();
                                    if (line.startsWith("event:")) {
                                        eventType = line.substring(6).trim();
                                    } else if (line.startsWith("data:")) {
                                        if (dataBuilder.length() > 0) {
                                            dataBuilder.append("\n");
                                        }
                                        dataBuilder.append(line.substring(5).trim());
                                    }
                                }
                                
                                if (eventType != null && dataBuilder.length() > 0) {
                                    // 直接发送对应的事件类型，前端无需解析嵌套格式
                                    emitter.send(SseEmitter.event()
                                        .name(eventType.replace(".", "_")) // 将点号替换为下划线，适配前端事件名
                                        .data(dataBuilder.toString()));
                                    log.info("✅ 流式事件转发成功，类型: {}", eventType);
                                }
                            } else {
                                // 如果不是嵌套格式，直接转发
                                emitter.send(SseEmitter.event()
                                    .name("ai_stream")
                                    .data(eventData));
                                log.info("✅ 直接流式事件转发成功");
                            }
                        } catch (Exception e) {
                            log.error("❌ 发送流式事件失败，emitter可能已断开: {}", e.getMessage());
                            log.error("❌ 事件内容: {}", eventData);
                            // 不return，继续处理其他事件
                        }
                    });
                log.info("✅ 步骤5完成: Coze流式客户端调用完成");
                
                // 处理完整的流式响应
                log.info("📋 步骤6: 处理完整的流式响应");
                com.zbkj.common.model.coze.EnterpriseChatMessage assistantMessage = 
                    enterpriseChatService.processStreamResponse(
                        request.getSessionId(), streamResponse, messageResponse.getMessageId());
                log.info("✅ 流式响应处理完成");
                
                if (assistantMessage != null) {
                    // 保存AI回复消息
                    enterpriseChatService.saveMessage(assistantMessage);
                    
                    // 发送完成事件
                    log.info("📤 步骤7: 准备发送AI回复完成事件");
                    try {
                        emitter.send(SseEmitter.event()
                            .name("assistant_message")
                            .data(assistantMessage));
                        log.info("✅ AI回复完成事件发送成功");
                    } catch (Exception e) {
                        log.error("❌ 发送AI回复完成事件失败，emitter可能已断开", e);
                    }
                }
                
                // 发送完成事件
                log.info("📤 步骤8: 准备发送对话完成事件");
                try {
                    emitter.send(SseEmitter.event()
                        .name("completed")
                        .data("对话完成"));
                    log.info("✅ 对话完成事件发送成功");
                } catch (Exception e) {
                    log.error("❌ 发送对话完成事件失败，emitter可能已断开", e);
                }
                
                try {
                    emitter.complete();
                    log.info("✅ SSE流正常完成");
                } catch (Exception e) {
                    log.error("❌ SSE流完成时出错", e);
                }
                
                log.info("企业聊天流式消息处理完成，会话ID: {}", request.getSessionId());
                
            } catch (Exception e) {
                log.error("企业聊天流式消息处理失败，会话ID: {}, 错误: {}", 
                         request.getSessionId(), e.getMessage(), e);
                
                try {
                    // 发送错误事件给客户端
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                    
                    // 正常完成连接，而不是使用completeWithError
                    emitter.complete();
                } catch (Exception ex) {
                    log.error("发送错误事件失败", ex);
                    // 如果发送错误事件也失败了，才使用completeWithError
                    emitter.completeWithError(ex);
                }
            }
        });
        
        // 立即发送一个连接建立事件，确保SSE连接正常
        try {
            emitter.send(SseEmitter.event()
                .name("connection")
                .data("{\"status\": \"connected\", \"sessionId\": \"" + request.getSessionId() + "\"}"));
            log.info("SSE连接建立事件发送成功，会话ID: {}", request.getSessionId());
        } catch (Exception e) {
            log.error("发送SSE连接建立事件失败", e);
        }
        
        // 确保异步任务立即开始执行
        log.info("返回SSE emitter，异步任务应该正在执行...");
        return emitter;
    }

    /**
     * 创建用户消息（辅助方法）
     */
    private com.zbkj.common.model.coze.EnterpriseChatMessage createUserMessage(EnterpriseChatMessageRequest request) {
        com.zbkj.common.model.coze.EnterpriseChatMessage message = 
            new com.zbkj.common.model.coze.EnterpriseChatMessage();
        message.setMessageId(cn.hutool.core.util.IdUtil.simpleUUID());
        message.setSessionId(request.getSessionId());
        message.setRole(com.zbkj.common.model.coze.EnterpriseChatMessage.ROLE_USER);
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setContentType(request.getContentType());
        message.setStatus(com.zbkj.common.model.coze.EnterpriseChatMessage.STATUS_SENDING);
        message.setParentMessageId(request.getParentMessageId());
        message.setAttachments(request.getAttachments());
        message.setMetaData(request.getMetaData());
        message.setCreateTime(new java.util.Date());
        message.setUpdateTime(new java.util.Date());
        return message;
    }

    /**
     * 构建Coze流式请求（辅助方法）
     */
    private java.util.Map<String, Object> buildCozeStreamRequest(
            com.zbkj.common.model.coze.EnterpriseChatSession session, 
            EnterpriseChatMessageRequest request) {
        
        java.util.Map<String, Object> cozeRequest = new java.util.HashMap<>();
        
        // 设置基本参数
        cozeRequest.put("bot_id", session.getCozeBotId());
        cozeRequest.put("user_id", String.valueOf(session.getMerId()));
        cozeRequest.put("stream", true); // 强制使用流式
        cozeRequest.put("auto_save_history", true);
        
        // 设置会话ID（如果已存在）
        if (cn.hutool.core.util.StrUtil.isNotBlank(session.getCozeConversationId())) {
            cozeRequest.put("conversation_id", session.getCozeConversationId());
        }
        
        // 构建消息数组
        java.util.List<java.util.Map<String, Object>> messages = new java.util.ArrayList<>();
        
        // 添加当前用户消息
        java.util.Map<String, Object> currentMessage = new java.util.HashMap<>();
        currentMessage.put("role", "user");
        currentMessage.put("content", request.getContent());
        currentMessage.put("content_type", request.getContentType());
        messages.add(currentMessage);
        
        cozeRequest.put("additional_messages", messages);
        
        return cozeRequest;
    }

    /**
     * 创建测试会话（当会话不存在时）
     * 自动从数据库中获取商户的默认智能体或第一个可用智能体
     */
    private com.zbkj.common.model.coze.EnterpriseChatSession createTestSessionIfNeeded(
            String sessionId, com.zbkj.common.vo.LoginUserVo userContext) {
        
        log.info("开始创建测试会话，会话ID: {}", sessionId);
        
        if (userContext == null || userContext.getUser() == null) {
            throw new RuntimeException("无法创建会话：用户信息无效");
        }
        
        try {
            // 获取商户的智能体
            Integer merchantId = userContext.getUser().getMerId();
            com.zbkj.common.model.coze.CozeBot defaultBot = cozeBotService.getDefaultBot(merchantId);
            
            String botId = null;
            if (defaultBot != null) {
                botId = defaultBot.getCozeBotId();
                log.info("使用商户默认智能体，Bot ID: {}, 名称: {}", botId, defaultBot.getName());
            } else {
                // 如果没有默认智能体，尝试获取第一个可用的智能体
                java.util.List<com.zbkj.common.model.coze.CozeBot> availableBots = 
                    cozeBotService.getAllByMerchantId(merchantId);
                
                if (availableBots != null && !availableBots.isEmpty()) {
                    // 寻找状态为启用且已发布的智能体
                    com.zbkj.common.model.coze.CozeBot availableBot = availableBots.stream()
                        .filter(bot -> bot.getStatus() == 1 && bot.getPublishStatus() == 1)
                        .findFirst()
                        .orElse(availableBots.get(0)); // 如果没找到，就用第一个
                    
                    botId = availableBot.getCozeBotId();
                    log.info("使用商户第一个可用智能体，Bot ID: {}, 名称: {}", botId, availableBot.getName());
                } else {
                    throw new RuntimeException("商户没有配置任何智能体，无法创建会话");
                }
            }
            
            // 创建会话请求
            com.zbkj.common.request.EnterpriseChatSessionRequest sessionRequest = 
                new com.zbkj.common.request.EnterpriseChatSessionRequest();
            sessionRequest.setSessionId(sessionId); // 设置指定的会话ID
            sessionRequest.setCozeBotId(botId); // 使用从数据库获取的智能体ID
            sessionRequest.setSessionTitle("测试会话 - " + sessionId);
            sessionRequest.setSessionContext("{\"autoCreated\": true, \"testMode\": true}"); // 设置会话上下文
            sessionRequest.setMetaData("{\"sessionId\": \"" + sessionId + "\", \"source\": \"stream-api\"}"); // 设置元数据
            sessionRequest.setAutoGenerateTitle(false);
            
            // 使用统一聊天服务创建会话
            com.zbkj.common.model.chat.UnifiedChatSession unifiedSession = unifiedChatService.createOrGetSession(
                userContext.getUser().getMerId().longValue(),
                "MERCHANT",
                userContext.getUser().getMerId().longValue(),
                "AI",
                botId
            );
            
            return convertToEnterpriseChatSession(unifiedSession);
            
        } catch (Exception e) {
            log.error("创建测试会话失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建测试会话失败: " + e.getMessage());
        }
    }

    /**
     * GET方式流式消息简化测试接口
     * 用于诊断连接断开问题
     */
    @ApiOperation(value = "GET流式消息简化测试")
    @GetMapping(value = "/test/stream-get", produces = "text/event-stream")
    public SseEmitter testStreamGet(
            @RequestParam(defaultValue = "test-session") String sessionId,
            @RequestParam(defaultValue = "测试消息") String content,
            @RequestParam(required = false) String token) {
        
        log.info("🧪 开始GET流式消息简化测试，会话ID: {}, 内容: {}", sessionId, content);
        
        SseEmitter emitter = new SseEmitter(3600000L); // 1小时超时
        
        // 异步任务，模拟完整流程但简化处理
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                log.info("🚀 测试异步任务开始");
                
                // 1. 发送开始事件
                emitter.send(SseEmitter.event()
                    .name("async_started")
                    .data("{\"status\": \"test_async_started\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                log.info("✅ 测试异步开始事件发送成功");
                
                // 2. 模拟步骤1 - 延时1秒
                Thread.sleep(1000);
                emitter.send(SseEmitter.event()
                    .name("user_message")
                    .data("{\"content\": \"" + content + "\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                log.info("✅ 测试用户消息事件发送成功");
                
                // 3. 模拟步骤2 - 延时1秒
                Thread.sleep(1000);
                emitter.send(SseEmitter.event()
                    .name("ai_stream")
                    .data("{\"content\": \"这是测试AI回复\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                log.info("✅ 测试AI流式事件发送成功");
                
                // 4. 模拟步骤3 - 延时1秒
                Thread.sleep(1000);
                emitter.send(SseEmitter.event()
                    .name("assistant_message")
                    .data("{\"content\": \"测试AI回复完成\", \"timestamp\": " + System.currentTimeMillis() + "}"));
                log.info("✅ 测试AI完成事件发送成功");
                
                // 5. 完成事件
                emitter.send(SseEmitter.event()
                    .name("completed")
                    .data("测试完成"));
                log.info("✅ 测试完成事件发送成功");
                
                emitter.complete();
                log.info("✅ 测试SSE流正常完成");
                
            } catch (Exception e) {
                log.error("❌ 测试流式消息失败", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\": \"" + e.getMessage() + "\"}"));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
        
        // 立即发送连接事件
        try {
            emitter.send(SseEmitter.event()
                .name("connection")
                .data("{\"status\": \"test_connected\", \"sessionId\": \"" + sessionId + "\"}"));
            log.info("✅ 测试连接事件发送成功");
        } catch (Exception e) {
            log.error("❌ 发送测试连接事件失败", e);
        }
        
        return emitter;
    }

    /**
     * SSE连接测试接口
     */
    @ApiOperation(value = "SSE连接测试")
    @GetMapping(value = "/test/sse", produces = "text/event-stream")
    public SseEmitter testSse() {
        log.info("开始SSE连接测试");
        SseEmitter emitter = new SseEmitter(3600000L); // 1小时超时
        
        // 立即发送测试事件
        try {
            emitter.send(SseEmitter.event()
                .name("test")
                .data("SSE连接测试成功"));
            log.info("测试事件发送成功");
            
            // 2秒后发送另一个事件
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    emitter.send(SseEmitter.event()
                        .name("test2")
                        .data("延迟测试事件"));
                    log.info("延迟测试事件发送成功");
                    
                    Thread.sleep(1000);
                    emitter.send(SseEmitter.event()
                        .name("completed")
                        .data("测试完成"));
                    emitter.complete();
                    log.info("SSE测试完成");
                } catch (Exception e) {
                    log.error("SSE测试失败", e);
                    emitter.completeWithError(e);
                }
            });
            
        } catch (Exception e) {
            log.error("发送测试事件失败", e);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * 获取消息详情
     */
    @ApiOperation(value = "获取消息详情")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:message:detail')")
    @GetMapping("/message/{messageId}")
    public CommonResult<EnterpriseChatMessage> getMessageDetail(@PathVariable String messageId) {
        EnterpriseChatMessage message = enterpriseChatService.getMessageDetail(messageId);
        return CommonResult.success(message);
    }

    /**
     * 删除消息
     */
    @ApiOperation(value = "删除消息")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:message:delete')")
    @DeleteMapping("/message/{messageId}")
    public CommonResult<Void> deleteMessage(@PathVariable String messageId) {
        enterpriseChatService.deleteMessage(messageId);
        return CommonResult.success();
    }

    /**
     * 重新发送失败的消息
     */
    @ApiOperation(value = "重新发送失败的消息")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:message:resend')")
    @PostMapping("/message/{messageId}/resend")
    public CommonResult<Map<String, Object>> resendMessage(@PathVariable String messageId) {
        Map<String, Object> result = enterpriseChatService.resendMessage(messageId);
        return CommonResult.success(result);
    }

    /**
     * 获取聊天统计信息
     */
    @ApiOperation(value = "获取聊天统计信息")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:statistics:view')")
    @GetMapping("/statistics")
    public CommonResult<Map<String, Object>> getChatStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String cozeBotId) {
        
        Map<String, Object> statistics = enterpriseChatService.getChatStatistics(
            startDate, endDate, cozeBotId);
        return CommonResult.success(statistics);
    }

    /**
     * 获取热门对话主题
     */
    @ApiOperation(value = "获取热门对话主题")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:statistics:view')")
    @GetMapping("/hot-topics")
    public CommonResult<List<Map<String, Object>>> getHotTopics(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<Map<String, Object>> hotTopics = enterpriseChatService.getHotTopics(limit);
        return CommonResult.success(hotTopics);
    }

    /**
     * 导出聊天记录
     */
    @ApiOperation(value = "导出聊天记录")
  //  @PreAuthorize("hasAuthority('merchant:enterprise-chat:export')")
    @GetMapping("/export")
    public CommonResult<String> exportChatHistory(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "json") String format) {
        
        String exportResult = enterpriseChatService.exportChatHistory(sessionId, format);
        return CommonResult.success(exportResult);
    }

    /**
     * 获取聊天配置
     */
    @ApiOperation(value = "获取聊天配置")
   // @PreAuthorize("hasAuthority('merchant:enterprise-chat:config:view')")
    @GetMapping("/config")
    public CommonResult<Map<String, Object>> getChatConfig() {
        Map<String, Object> config = enterpriseChatService.getChatConfig();
        return CommonResult.success(config);
    }

    /**
     * 更新聊天配置
     */
    @ApiOperation(value = "更新聊天配置")
   // @PreAuthorize("hasAuthority('merchant:enterprise-chat:config:update')")
    @PutMapping("/config")
    public CommonResult<Void> updateChatConfig(@RequestBody Map<String, Object> config) {
        enterpriseChatService.updateChatConfig(config);
        return CommonResult.success();
    }

    /**
     * 转换UnifiedChatSession为EnterpriseChatSession（临时兼容方法）
     */
    private com.zbkj.common.model.coze.EnterpriseChatSession convertToEnterpriseChatSession(
            com.zbkj.common.model.chat.UnifiedChatSession unifiedSession) {
        
        if (unifiedSession == null) return null;
        
        com.zbkj.common.model.coze.EnterpriseChatSession enterpriseSession = 
            new com.zbkj.common.model.coze.EnterpriseChatSession();
            
        enterpriseSession.setSessionId(unifiedSession.getSessionId());
        enterpriseSession.setUserId(unifiedSession.getUserId());
        enterpriseSession.setMerId(unifiedSession.getMerId());
        enterpriseSession.setCozeBotId(unifiedSession.getCozeBotId());
        enterpriseSession.setCozeConversationId(unifiedSession.getCozeConversationId());
        enterpriseSession.setSessionTitle(unifiedSession.getSessionTitle());
        
        // 状态转换
        int status = 1; // 默认活跃
        if ("ENDED".equals(unifiedSession.getStatus())) {
            status = 2;
        } else if ("CLOSED".equals(unifiedSession.getStatus())) {
            status = 3;
        }
        enterpriseSession.setStatus(status);
        
        enterpriseSession.setTotalMessages(unifiedSession.getTotalMessages());
        enterpriseSession.setLastMessageTime(unifiedSession.getLastMessageTime());
        enterpriseSession.setLastMessageContent(unifiedSession.getLastMessageContent());
        enterpriseSession.setSessionContext(unifiedSession.getSessionContext());
        enterpriseSession.setMetaData(unifiedSession.getMetaData());
        enterpriseSession.setCreateTime(unifiedSession.getCreateTime());
        enterpriseSession.setUpdateTime(unifiedSession.getUpdateTime());
        
        return enterpriseSession;
    }
}
