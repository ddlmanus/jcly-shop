package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.response.coze.CozeBaseResponse;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.model.service.ContactRelationship;
import com.zbkj.common.model.user.User;
import com.zbkj.common.request.chat.SendMessageRequest;
import com.zbkj.common.request.TransferToHumanRequest;
import com.zbkj.common.response.chat.MessageResponse;
// import com.zbkj.admin.websocket.HumanServiceWebSocketHandler;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginFrontUserVo;
import com.zbkj.service.dao.chat.UnifiedChatMessageDao;
import com.zbkj.service.dao.chat.UnifiedChatSessionDao;
import com.zbkj.service.service.*;
import com.zbkj.service.service.UnifiedMessageRoutingService;
import com.zbkj.common.model.merchant.Merchant;
// import com.zbkj.common.model.system.SystemAdmin; // 暂时注释避免导入错误
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 统一聊天服务实现
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class UnifiedChatServiceImpl implements UnifiedChatService {

    @Autowired
    private UnifiedChatSessionDao sessionDao;

    @Autowired
    private UnifiedChatMessageDao messageDao;

    @Autowired
    private UserService userService;

    @Autowired
    private CozeService cozeService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private SystemAdminService systemAdminService;

    @Autowired
    private HumanServiceWebSocketService humanServiceWebSocketService;
    
    @Autowired
    private UnifiedMessageRoutingService messageRoutingService;
    
    @Autowired
    private HumanServiceService humanServiceService;
    @Autowired
    private UnifiedChatMessageDao unifiedChatMessageDao;
    @Autowired
    private ContactRelationshipService contactRelationshipService;

    // 暂时注释，避免循环依赖，实际使用时需要通过ApplicationContext获取
    // @Autowired
    // private HumanServiceWebSocketHandler webSocketHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatSession createOrGetSession(Long userId, String userType, Long merId, String sessionType, String cozeBotId) {
        // 查找是否有活跃的会话
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getUserId, userId)
                .eq(UnifiedChatSession::getUserType, userType)
                .eq(UnifiedChatSession::getMerId, merId)
                .eq(UnifiedChatSession::getSessionType, sessionType)
                .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE);
        
        if (StringUtils.hasText(cozeBotId)) {
            queryWrapper.eq(UnifiedChatSession::getCozeBotId, cozeBotId);
        }
        
        UnifiedChatSession existingSession = sessionDao.selectOne(queryWrapper);
        if (existingSession != null) {
            return existingSession;
        }

        // 创建新会话
        UnifiedChatSession session = new UnifiedChatSession();
        session.setSessionId(generateSessionId());
        session.setUserId(userId);
        session.setUserType(userType);
        session.setMerId(merId);
        session.setSessionType(sessionType);
        session.setCurrentServiceType(sessionType.equals(UnifiedChatSession.SESSION_TYPE_AI) ? 
            UnifiedChatSession.SERVICE_TYPE_AI : UnifiedChatSession.SERVICE_TYPE_HUMAN);
        session.setCozeBotId(cozeBotId);
        session.setStatus(UnifiedChatSession.STATUS_ACTIVE);
        session.setTotalMessages(0);
        session.setPriority(UnifiedChatSession.PRIORITY_NORMAL);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());

        sessionDao.insert(session);
        
        log.info("创建新会话，用户ID: {}, 会话ID: {}, 类型: {}", userId, session.getSessionId(), sessionType);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageResponse sendMessage(SendMessageRequest request) {
        // 验证会话
        UnifiedChatSession session = getSessionInternal(request.getSessionId());
        if (session == null) {
            throw new CrmebException("会话不存在");
        }

        // 🔒 权限验证：只能向自己的会话发送消息
        Long currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        
        // 如果请求中指定了发送者信息，使用指定的发送者进行权限验证
        Long senderId = request.getSenderId() != null ? request.getSenderId() : currentUserId;
        String senderType = request.getSenderType() != null ? request.getSenderType() : currentUserType;
        
        if (!hasSessionAccess(session, senderId, senderType)) {
            throw new CrmebException("无权向此会话发送消息");
        }

        // 创建消息
        UnifiedChatMessage message = new UnifiedChatMessage();
        message.setMessageId(generateMessageId());
        message.setSessionId(request.getSessionId());
        
        // 优先使用请求中的发送者信息（如果提供的话）
        if (request.getSenderId() != null && request.getSenderType() != null) {
            message.setSenderId(request.getSenderId());
            message.setSenderType(request.getSenderType());
        } else {
            message.setSenderId(currentUserId);
            message.setSenderType(getSenderTypeFromUserType(currentUserType));
        }
        
        // 根据发送者类型设置角色
        if ("MERCHANT".equals(message.getSenderType())) {
            message.setRole(UnifiedChatMessage.ROLE_USER);
        }else if ("STAFF".equals(message.getSenderType())) {
            message.setRole(UnifiedChatMessage.ROLE_USER);
        }
        else if ("AI".equals(message.getSenderType())) {
            message.setRole(UnifiedChatMessage.ROLE_ASSISTANT);
        } else {
            message.setRole(UnifiedChatMessage.ROLE_USER);
        }
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setContentType(request.getContentType());
        message.setAttachments(request.getAttachments());
        message.setRelatedMessageId(request.getRelatedMessageId());
        message.setMetaData(request.getMetaData());
        message.setStatus(UnifiedChatMessage.STATUS_SENT);
        message.setIsRead(false);
        message.setIsSystemMessage(false);
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());

        // 处理转人工请求（在设置接收者信息之前）
        if (humanServiceService.shouldTransferToHuman(request.getContent())) {
            log.info("检测到转人工关键词，开始转接: sessionId={}", request.getSessionId());
            
            try {
                // 使用HumanServiceService的transferToHuman方法，这会分配具体的客服
                TransferToHumanRequest transferRequest = new TransferToHumanRequest();
                transferRequest.setEnterpriseSessionId(request.getSessionId());
                transferRequest.setUserId(request.getSenderId().intValue());
                transferRequest.setUserType("CUSTOMER");
                transferRequest.setMerId(session.getMerId().intValue());
                transferRequest.setTransferReason(request.getTransferReason() != null ? request.getTransferReason() : "用户主动转人工");
                transferRequest.setPriority(UnifiedChatSession.PRIORITY_NORMAL);
                
                Map<String, Object> transferResult = humanServiceService.transferToHuman(transferRequest);
                log.info("转接结果: {}", transferResult);
                
                // 转接后重新获取最新的会话信息
                session = getSessionInternal(request.getSessionId());
                log.info("转接完成，重新获取会话信息: sessionId={}, staffId={}", session.getSessionId(), session.getStaffId());
            } catch (Exception e) {
                log.error("转接人工客服失败: sessionId={}, 错误: {}", request.getSessionId(), e.getMessage(), e);
                // 如果转接失败，仍然使用原有的简单转接方法
                transferToHumanService(request.getSessionId(), request.getTransferReason(), UnifiedChatSession.PRIORITY_NORMAL);
                session = getSessionInternal(request.getSessionId());
            }
        }

        // 设置接收者信息
        if (request.getReceiverId() != null && StringUtils.hasText(request.getReceiverType())) {
            message.setReceiverId(request.getReceiverId());
            message.setReceiverType(request.getReceiverType());
        } else {
            // 根据会话类型和发送者信息设置默认接收者
            if (session.isCurrentlyAiService()) {
                // AI服务中，接收者是AI（没有具体的receiverId）
                message.setReceiverType(UnifiedChatMessage.SENDER_TYPE_AI);
                message.setReceiverId(null); // AI没有具体的ID
            } else {
                // 人工客服会话中，根据发送者确定接收者
                if (UnifiedChatMessage.SENDER_TYPE_MERCHANT.equals(message.getSenderType()) ||
                        UnifiedChatMessage.SENDER_TYPE_PLATFORM.equals(message.getSenderType())) {
                    // 客服或平台发送的消息，接收者是会话中的用户
                    message.setReceiverId(session.getUserId());
                    message.setReceiverType(session.getUserType());
                    log.debug("设置消息接收者为用户: userId={}, userType={}", session.getUserId(), session.getUserType());
                } else if (session.getStaffId() != null) {
                    // 用户发送的消息，接收者是分配的客服/商户
                    message.setReceiverId(session.getStaffId());
                    
                    // 根据会话类型确定接收者类型
                    if ("PLATFORM".equals(session.getUserType())) {
                        // 如果会话的用户类型是PLATFORM，说明这是平台与商户的会话，接收者是商户
                        message.setReceiverType(UnifiedChatMessage.SENDER_TYPE_MERCHANT);
                    } else {
                        // 其他情况，根据merId判断接收者类型
                        if (session.getMerId() != null && session.getMerId() == 0) {
                            // merId=0 说明接收者是平台
                            message.setReceiverType(UnifiedChatMessage.SENDER_TYPE_PLATFORM);
                        } else {
                            // 其他情况接收者是商户
                            message.setReceiverType(UnifiedChatMessage.SENDER_TYPE_MERCHANT);
                        }
                    }
                    log.debug("设置消息接收者为客服: staffId={}, receiverType={}", session.getStaffId(), message.getReceiverType());
                } else {
                    // 如果没有分配客服，但是会话已经是人工模式，记录警告
                    message.setReceiverId(session.getStaffId());
                    message.setReceiverType(UnifiedChatMessage.SENDER_TYPE_PLATFORM);
                    log.debug("设置消息接收者为平台: staffId={}", session.getStaffId());
                }
            }
        }

        // 设置发送者信息 - 使用实际的发送者ID和类型
        setSenderInfo(message, message.getSenderId(), message.getSenderType());

        // 保存消息
        messageDao.insert(message);

        // 更新会话信息
        updateSessionLastMessage(session, message);

        // 广播消息
        broadcastMessage(message);

        // 转人工处理已在消息保存前完成，此处无需重复处理

        // 如果是AI会话且需要AI回复，则处理AI回复
        if (session.isCurrentlyAiService() && Boolean.TRUE.equals(request.getNeedAiReply())) {
            log.info("AI回复将通过流式接口处理，统一聊天服务跳过重复调用");
        }

        log.info("发送消息成功，会话ID: {}, 消息ID: {}, 发送者: {}", 
                request.getSessionId(), message.getMessageId(), currentUserId);

        return MessageResponse.fromMessage(message);
    }

    @Override
    public List<MessageResponse> getSessionMessages(String sessionId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // 限制最大页面大小

        // 🔒 安全检查：验证会话权限
        UnifiedChatSession session = getSessionInternal(sessionId);
        if (session == null) {
            throw new CrmebException("会话不存在");
        }
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatMessage::getSessionId, sessionId)
                .eq(UnifiedChatMessage::getIsClear,false)
                .orderByAsc(UnifiedChatMessage::getCreateTime);

        Page<UnifiedChatMessage> messagePage = new Page<>(page, size);
        Page<UnifiedChatMessage> result = messageDao.selectPage(messagePage, queryWrapper);

        return result.getRecords().stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
    }

    @Override
    public UnifiedChatSession getSession(String sessionId) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        return sessionDao.selectOne(queryWrapper);
    }

    /**
     * 获取会话（内部使用，不做权限检查）
     */
    private UnifiedChatSession getSessionInternal(String sessionId) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        return sessionDao.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionStatus(String sessionId, String status) {
        UnifiedChatSession session = getSession(sessionId);
        if (session == null) {
            throw new CrmebException("会话不存在");
        }

        session.setStatus(status);
        session.setUpdateTime(new Date());
        
        if (UnifiedChatSession.STATUS_ENDED.equals(status) || UnifiedChatSession.STATUS_CLOSED.equals(status)) {
            session.setServiceEndTime(new Date());
            if (session.getServiceStartTime() != null) {
                long serviceTime = (session.getServiceEndTime().getTime() - session.getServiceStartTime().getTime()) / 1000;
                session.setTotalServiceTime((int) serviceTime);
            }
        }

        sessionDao.updateById(session);
        log.info("更新会话状态，会话ID: {}, 新状态: {}", sessionId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatSession transferToHumanService(String sessionId, String transferReason, String priority) {
        UnifiedChatSession session = getSession(sessionId);
        if (session == null) {
            throw new CrmebException("会话不存在");
        }

        // 更新会话为混合模式，当前服务类型为人工
        session.setSessionType(UnifiedChatSession.SESSION_TYPE_MIXED);
        session.setCurrentServiceType(UnifiedChatSession.SERVICE_TYPE_HUMAN);
        session.setStatus(UnifiedChatSession.STATUS_WAITING);
        session.setTransferReason(transferReason);
        session.setPriority(priority != null ? priority : UnifiedChatSession.PRIORITY_NORMAL);
        session.setWaitStartTime(new Date());
        session.setUpdateTime(new Date());

        // 设置排队位置
        int queuePosition = getQueuePosition(session.getMerId());
        session.setQueuePosition(queuePosition);

        sessionDao.updateById(session);

        // 发送系统消息通知转人工
        sendSystemMessage(sessionId, "您的咨询已转接到人工客服，当前排队位置：" + queuePosition + "，请稍候...");

        log.info("会话转接到人工客服，会话ID: {}, 原因: {}, 排队位置: {}", sessionId, transferReason, queuePosition);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatSession assignStaff(String sessionId, Long staffId) {
        UnifiedChatSession session = getSession(sessionId);
        if (session == null) {
            throw new CrmebException("会话不存在");
        }

        session.setStaffId(staffId);
        session.setStatus(UnifiedChatSession.STATUS_ACTIVE);
        session.setServiceStartTime(new Date());
        session.setQueuePosition(0);
        session.setUpdateTime(new Date());

        if (session.getWaitStartTime() != null) {
            long waitTime = (new Date().getTime() - session.getWaitStartTime().getTime()) / 1000;
            session.setTotalWaitTime((int) waitTime);
        }

        sessionDao.updateById(session);

        // 发送系统消息通知客服已接入
        sendSystemMessage(sessionId, "客服已为您接入服务，有什么可以帮助您的吗？");

        log.info("分配客服到会话，会话ID: {}, 客服ID: {}", sessionId, staffId);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endSession(String sessionId, String reason) {
        try {
            // 获取会话信息
            UnifiedChatSession session = getSession(sessionId);
            if (session == null) {
                throw new CrmebException("会话不存在: " + sessionId);
            }
            
            // 如果是AI会话且有Coze会话ID，先删除Coze会话
            if (session.getCozeBotId() != null && session.getCozeConversationId() != null) {
                try {
                    log.info("正在删除Coze会话，会话ID: {}, Coze会话ID: {}", sessionId, session.getCozeConversationId());
                    CozeBaseResponse cozeResponse = cozeService.deleteConversation(session.getCozeConversationId());
                    if (cozeResponse != null && cozeResponse.getCode() == 0) {
                        log.info("Coze会话删除成功，会话ID: {}", sessionId);
                    } else {
                        log.warn("Coze会话删除失败，会话ID: {}, 响应: {}", sessionId, cozeResponse);
                    }
                } catch (Exception e) {
                    log.error("删除Coze会话失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
                    // 不抛出异常，继续结束本地会话
                }
            }
            
            // 更新本地会话状态
            updateSessionStatus(sessionId, UnifiedChatSession.STATUS_ENDED);
            sendSystemMessage(sessionId, "会话已结束，感谢您的咨询。原因：" + (reason != null ? reason : "正常结束"));
            log.info("结束会话，会话ID: {}, 原因: {}", sessionId, reason);
            
        } catch (Exception e) {
            log.error("结束会话失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessageAsRead(String messageId, Long readerId, String readerType) {
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatMessage::getMessageId, messageId);
        
        UnifiedChatMessage message = messageDao.selectOne(queryWrapper);
        if (message != null) {
            // 🔒 权限验证：验证会话权限
            UnifiedChatSession session = getSessionInternal(message.getSessionId());
            if (session != null) {
                Long currentUserId = getCurrentUserId();
                String currentUserType = getCurrentUserType();
                
                if (!hasSessionAccess(session, currentUserId, currentUserType)) {
                    throw new CrmebException("无权标记此消息为已读");
                }
            }
            
            message.setIsRead(true);
            message.setReadTime(LocalDateTime.now());
            message.setStatus(UnifiedChatMessage.STATUS_READ);
            message.setUpdateTime(new Date());
            messageDao.updateById(message);
            
            // 广播已读状态更新
            broadcastReadStatusUpdate(message, readerId, readerType);
            
            log.debug("标记消息已读，消息ID: {}, 阅读者: {}", messageId, readerId);
        }
    }
    
    /**
     * 广播已读状态更新
     */
    private void broadcastReadStatusUpdate(UnifiedChatMessage message, Long readerId, String readerType) {
        try {
            // 获取会话信息
            UnifiedChatSession session = getSessionInternal(message.getSessionId());
            if (session == null) {
                return;
            }
            
            // 创建已读状态更新消息
            Map<String, Object> readUpdate = new HashMap<>();
            readUpdate.put("type", "message_read");
            readUpdate.put("messageId", message.getMessageId());
            readUpdate.put("sessionId", message.getSessionId());
            readUpdate.put("readerId", readerId);
            readUpdate.put("readerType", readerType);
            readUpdate.put("readTime", System.currentTimeMillis());
            
            // 广播给会话参与者（除了读者本人）
            Set<Long> participants = getSessionParticipants(session);
            for (Long participantId : participants) {
                if (!Objects.equals(participantId, readerId)) {
                    if (humanServiceWebSocketService != null) {
                        humanServiceWebSocketService.sendMessageToUser(participantId.intValue(), readUpdate);
                    }
                }
            }
            
            log.debug("已读状态已广播: 消息ID={}, 读者={}", message.getMessageId(), readerId);
        } catch (Exception e) {
            log.error("广播已读状态失败", e);
        }
    }

    @Override
    public List<UnifiedChatSession> getUserActiveSessions(Long userId, String userType, Long merId) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getUserId, userId)
                .eq(UnifiedChatSession::getUserType, userType)
                .eq(UnifiedChatSession::getMerId, merId)
                .in(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE, UnifiedChatSession.STATUS_WAITING)
                .orderByDesc(UnifiedChatSession::getUpdateTime);
        
        return sessionDao.selectList(queryWrapper);
    }

    @Override
    public List<UnifiedChatSession> getStaffActiveSessions(Long staffId, Long merId) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getStaffId, staffId)
                .eq(UnifiedChatSession::getMerId, merId)
                .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE)
                .orderByDesc(UnifiedChatSession::getUpdateTime);
        
        return sessionDao.selectList(queryWrapper);
    }

    @Override
    public List<UnifiedChatSession> getWaitingQueue(Long merId) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getMerId, merId)
                .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_WAITING)
                .orderByAsc(UnifiedChatSession::getQueuePosition)
                .orderByAsc(UnifiedChatSession::getWaitStartTime);
        
        return sessionDao.selectList(queryWrapper);
    }

    @Override
    public MessageResponse processAiReply(UnifiedChatMessage message) {
        try {
            // 获取会话信息
            UnifiedChatSession session = getSession(message.getSessionId());
            if (session == null || !StringUtils.hasText(session.getCozeBotId())) {
                log.warn("会话不存在或未配置AI机器人，跳过AI回复");
                return null;
            }

            // 构建AI请求参数
            Map<String, Object> cozeRequest = new HashMap<>();
            cozeRequest.put("bot_id", session.getCozeBotId());
            cozeRequest.put("user_id", message.getSenderId().toString());
            cozeRequest.put("query", message.getContent());
            if (StringUtils.hasText(session.getCozeConversationId())) {
                cozeRequest.put("conversation_id", session.getCozeConversationId());
            }
            cozeRequest.put("stream", true);

            // 调用Coze服务获取AI回复
            Object cozeResponse = cozeService.startChat(cozeRequest);
            
            // 创建AI回复消息
            UnifiedChatMessage aiReply = createAiReplyMessage(message, cozeResponse);
            
            if (aiReply != null) {
                messageDao.insert(aiReply);

                // 更新会话信息
                updateSessionLastMessage(session, aiReply);

                // 广播AI回复
                broadcastMessage(aiReply);

                log.info("AI回复处理成功，会话ID: {}, 消息ID: {}", message.getSessionId(), aiReply.getMessageId());
                return MessageResponse.fromMessage(aiReply);
            }

            return null;
        } catch (Exception e) {
            log.error("AI回复处理失败，会话ID: {}, 消息ID: {}", message.getSessionId(), message.getMessageId(), e);
            
            // 创建错误回复
            UnifiedChatMessage errorReply = createErrorReply(message.getSessionId(), "抱歉，AI服务暂时不可用，请稍后再试或转接人工客服。");
            if (errorReply != null) {
                messageDao.insert(errorReply);
                broadcastMessage(errorReply);
                return MessageResponse.fromMessage(errorReply);
            }
            
            return null;
        }
    }

    // 私有辅助方法

    @Override
    public UnifiedChatSession getSessionByCozeConversationId(String cozeConversationId) {
        if (!StringUtils.hasText(cozeConversationId)) {
            return null;
        }
        
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getCozeConversationId, cozeConversationId);
        queryWrapper.orderByDesc(UnifiedChatSession::getCreateTime);
        queryWrapper.last("LIMIT 1");
        
        return sessionDao.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatSession saveSession(UnifiedChatSession session) {
        if (session == null) {
            throw new IllegalArgumentException("会话信息不能为空");
        }
        
        if (!StringUtils.hasText(session.getSessionId())) {
            session.setSessionId(generateSessionId());
        }
        
        if (session.getCreateTime() == null) {
            session.setCreateTime(new Date());
        }
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(new Date());
        }
        
        sessionDao.insert(session);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatSession updateSession(UnifiedChatSession session) {
        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException("会话信息或ID不能为空");
        }
        
        session.setUpdateTime(new Date());
        sessionDao.updateById(session);
        return session;
    }

    @Override
    public String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成商户到商户的确定性会话ID
     * 确保A→B和B→A使用相同的会话ID
     * @param adminId1 商户管理员ID1
     * @param adminId2 商户管理员ID2
     * @return 确定性的会话ID
     */
    private String generateMerchantToMerchantSessionId(Long adminId1, Long adminId2) {
        // 使用较小和较大的ID排序，确保双向会话使用相同ID
        Long smallerId = Math.min(adminId1, adminId2);
        Long largerId = Math.max(adminId1, adminId2);
        return "session_merchant_" + smallerId + "_" + largerId;
    }

    @Override
    public String generateMessageId() {
        return "msg_" + UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public UnifiedChatMessage getMessageByCozeMessageId(String cozeMessageId) {
        if (!StringUtils.hasText(cozeMessageId)) {
            return null;
        }

        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatMessage::getCozeMessageId, cozeMessageId);
        queryWrapper.orderByDesc(UnifiedChatMessage::getCreateTime);
        queryWrapper.last("LIMIT 1");

        return messageDao.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatMessage saveMessage(UnifiedChatMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("消息信息不能为空");
        }
        
        if (!StringUtils.hasText(message.getMessageId())) {
            message.setMessageId(generateMessageId());
        }
        
        if (message.getCreateTime() == null) {
            message.setCreateTime(new Date());
        }
        if (message.getUpdateTime() == null) {
            message.setUpdateTime(new Date());
        }
        
        messageDao.insert(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatMessage updateMessage(UnifiedChatMessage message) {
        if (message == null || message.getId() == null) {
            throw new IllegalArgumentException("消息信息或ID不能为空");
        }
        
        message.setUpdateTime(new Date());
        messageDao.updateById(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionLastMessage(UnifiedChatSession session, UnifiedChatMessage message) {
        if (session == null || message == null) {
            return;
        }
        
        session.setLastMessageTime(message.getCreateTime());
        session.setLastMessageContent(message.getContent());
        session.setTotalMessages(session.getTotalMessages() + 1);
        session.setUpdateTime(new Date());
        
        sessionDao.updateById(session);
    }

    private Long getCurrentUserId() {
         try {
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            if (loginUser != null && loginUser.getUser() != null) {
                // 商户端返回商户ID
                return loginUser.getUser().getId().longValue();
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }
         return 1L;
    }

    private String getCurrentUserType() {
        try {
            if(userService.getUserId()!=null){
               return UnifiedChatSession.USER_TYPE_CUSTOMER;
            }else{
                com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
                if (loginUser != null && loginUser.getUser() != null) {
                    // 商户端用户都视为客服（STAFF）
                    if (loginUser.getUser().getMerId() != 0) {
                        return "MERCHANT"; // 商户用户视为商户
                    }else{
                        // 平台用户也视为客服
                        return UnifiedChatSession.USER_TYPE_PLATFORM;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户类型失败: {}", e.getMessage());
        }
        return UnifiedChatSession.USER_TYPE_CUSTOMER; // 默认返回
    }

    private String getSenderTypeFromUserType(String userType) {
        switch (userType) {
            case UnifiedChatSession.USER_TYPE_CUSTOMER:
                return UnifiedChatMessage.SENDER_TYPE_USER;
            case UnifiedChatSession.USER_TYPE_MERCHANT:
                return UnifiedChatMessage.SENDER_TYPE_MERCHANT;
            case UnifiedChatSession.USER_TYPE_PLATFORM:
                return UnifiedChatMessage.SENDER_TYPE_SYSTEM;
            default:
                return UnifiedChatMessage.SENDER_TYPE_USER;
        }
    }

    private void setSenderInfo(UnifiedChatMessage message, Long userId, String senderType) {
        try {
            switch (senderType) {
                case "USER":
                    User user = userService.getById(userId.intValue());
                    if (user != null) {
                        message.setSenderName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname());
                        message.setSenderAvatar(user.getAvatar());
                    }
                    break;
                case "MERCHANT":
                    // 商户管理员：商户客服头像从eb_merchant表获取
                    SystemAdmin admin = systemAdminService.getById(userId.intValue());
                    if (admin != null) {
                        message.setSenderName(StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getAccount());
                        
                        // 如果是商户管理员（merId != 0），头像从merchant表获取
                        if (admin.getMerId() != null && admin.getMerId() > 0) {
                            try {
                                Merchant merchant = merchantService.getById(admin.getMerId());
                                if (merchant != null) {
                                    message.setSenderAvatar(merchant.getAvatar());
                                } else {
                                    message.setSenderAvatar(admin.getHeaderImage()); // 兜底使用admin头像
                                }
                            } catch (Exception e) {
                                log.warn("获取商户头像失败，使用管理员头像: userId={}", userId, e);
                                message.setSenderAvatar(admin.getHeaderImage());
                            }
                        } else {
                            // 平台管理员使用admin的头像
                            message.setSenderAvatar(admin.getHeaderImage());
                        }
                    } else {
                        message.setSenderName("商户管理员" + userId);
                    }
                    break;

                case "PLATFORM":
                    SystemAdmin platformAdmin = systemAdminService.getById(userId.intValue());
                    if (platformAdmin != null) {
                        message.setSenderName(StringUtils.hasText(platformAdmin.getRealName()) ? platformAdmin.getRealName() : platformAdmin.getAccount());
                        message.setSenderAvatar(platformAdmin.getHeaderImage());
                    } else {
                        message.setSenderName("平台管理员" + userId);
                    }
                    break;
                case "AI":
                    message.setSenderName("AI助手");
                    message.setSenderAvatar("");
                    break;
                case "SYSTEM":
                    message.setSenderName("系统");
                    message.setSenderAvatar("");
                    break;
                default:
                    log.warn("未知发送者类型: {}", senderType);
                    message.setSenderName("用户" + userId);
                    break;
            }
        } catch (Exception e) {
            log.error("设置发送者信息失败，用户ID: {}, 发送者类型: {}", userId, senderType, e);
            message.setSenderName("用户" + userId);
        }
    }



    private int getQueuePosition(Long merId) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getMerId, merId)
                .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_WAITING);
        
        return Math.toIntExact(sessionDao.selectCount(queryWrapper)) + 1;
    }

    private void sendSystemMessage(String sessionId, String content) {
        UnifiedChatMessage systemMessage = new UnifiedChatMessage();
        systemMessage.setMessageId(generateMessageId());
        systemMessage.setSessionId(sessionId);
        systemMessage.setSenderType(UnifiedChatMessage.SENDER_TYPE_SYSTEM);
        systemMessage.setRole(UnifiedChatMessage.ROLE_SYSTEM);
        systemMessage.setMessageType(UnifiedChatMessage.MESSAGE_TYPE_TEXT);
        systemMessage.setContent(content);
        systemMessage.setContentType(UnifiedChatMessage.CONTENT_TYPE_TEXT);
        systemMessage.setStatus(UnifiedChatMessage.STATUS_SENT);
        systemMessage.setIsRead(false);
        systemMessage.setIsSystemMessage(true);
        systemMessage.setCreateTime(new Date());
        systemMessage.setUpdateTime(new Date());

        messageDao.insert(systemMessage);
        broadcastMessage(systemMessage);
    }

    /**
     * 创建AI回复消息
     */
    private UnifiedChatMessage createAiReplyMessage(UnifiedChatMessage userMessage, Object cozeResponse) {
        try {
            UnifiedChatMessage aiReply = new UnifiedChatMessage();
            aiReply.setMessageId(generateMessageId());
            aiReply.setSessionId(userMessage.getSessionId());
            aiReply.setSenderType(UnifiedChatMessage.SENDER_TYPE_AI);
            aiReply.setRole(UnifiedChatMessage.ROLE_ASSISTANT);
            aiReply.setMessageType(UnifiedChatMessage.MESSAGE_TYPE_TEXT);
            aiReply.setContentType(UnifiedChatMessage.CONTENT_TYPE_TEXT);
            aiReply.setStatus(UnifiedChatMessage.STATUS_SENT);
            aiReply.setIsRead(false);
            aiReply.setIsSystemMessage(false);
            aiReply.setParentMessageId(userMessage.getMessageId());
            aiReply.setCreateTime(new Date());
            aiReply.setUpdateTime(new Date());

            // 处理Coze响应
            String content = "AI正在思考中..."; // 默认内容
            if (cozeResponse != null) {
                try {
                    // 这里可以根据实际的Coze响应格式来解析
                    content = "AI: 收到您的消息「" + userMessage.getContent() + "」，正在为您处理...";
                } catch (Exception e) {
                    log.warn("解析Coze响应失败，使用默认回复", e);
                    content = "抱歉，我现在有点忙，请稍后再试或转接人工客服。";
                }
            }
            
            aiReply.setContent(content);
            if (cozeResponse != null) {
                aiReply.setRawContent(cozeResponse.toString());
            }

            return aiReply;
        } catch (Exception e) {
            log.error("创建AI回复消息失败", e);
            return null;
        }
    }

    /**
     * 创建错误回复消息
     */
    private UnifiedChatMessage createErrorReply(String sessionId, String errorContent) {
        try {
            UnifiedChatMessage errorReply = new UnifiedChatMessage();
            errorReply.setMessageId(generateMessageId());
            errorReply.setSessionId(sessionId);
            errorReply.setSenderType(UnifiedChatMessage.SENDER_TYPE_AI);
            errorReply.setRole(UnifiedChatMessage.ROLE_ASSISTANT);
            errorReply.setMessageType(UnifiedChatMessage.MESSAGE_TYPE_TEXT);
            errorReply.setContent(errorContent);
            errorReply.setContentType(UnifiedChatMessage.CONTENT_TYPE_TEXT);
            errorReply.setStatus(UnifiedChatMessage.STATUS_FAILED);
            errorReply.setIsRead(false);
            errorReply.setIsSystemMessage(false);
            errorReply.setCreateTime(new Date());
            errorReply.setUpdateTime(new Date());

            return errorReply;
        } catch (Exception e) {
            log.error("创建错误回复失败", e);
            return null;
        }
    }

    /**
     * 检查用户是否有权访问指定会话
     * 
     * @param session 会话对象
     * @param userId 用户ID
     * @param userType 用户类型
     * @return true 如果有权限，false 否则
     */
    private boolean hasSessionAccess(UnifiedChatSession session, Long userId, String userType) {
        if (session == null || userId == null || userType == null) {
            return false;
        }

        // 检查是否是会话的用户
        if (Objects.equals(session.getUserId(), userId) && 
            Objects.equals(session.getUserType(), userType)) {
            return true;
        }

        // 检查是否是被分配的客服人员
        if (session.getStaffId() != null && 
            Objects.equals(session.getStaffId(), userId) && 
            ("MERCHANT".equals(userType) || UnifiedChatSession.USER_TYPE_PLATFORM.equals(userType)|| UnifiedChatSession.USER_TYPE_CUSTOMER.equals(userType))) {
            return true;
        }

        // 如果是商户类型的会话，检查是否是同一商户的管理员或客服
        if (UnifiedChatSession.USER_TYPE_MERCHANT.equals(session.getUserType()) && 
            (UnifiedChatSession.USER_TYPE_MERCHANT.equals(userType) || "MERCHANT".equals(userType))) {
            // 获取会话所属商户ID
            Long sessionMerId = session.getMerId();
            // 获取当前用户所属商户ID
            try {
                com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
                if (loginUser != null && loginUser.getUser() != null) {
                    Integer currentMerId = loginUser.getUser().getMerId();
                    if (Objects.equals(sessionMerId, currentMerId != null ? currentMerId.longValue() : null)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.warn("检查商户权限时出错: {}", e.getMessage());
            }
        }

        // 平台管理员或客服可以访问所有会话
        if (UnifiedChatSession.USER_TYPE_PLATFORM.equals(userType) || "MERCHANT".equals(userType)) {
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedChatSession createOrGetSessionByContact(Long contactId, String contactType, Long merId, Long operatorId, String sessionType) {
        // 【修复】商户到商户的会话：使用确定性会话ID，确保双向使用同一个会话
        boolean isMerchantToMerchant = "MERCHANT".equals(contactType) && merId != 0;
        String determinedSessionId = null;

        if (isMerchantToMerchant) {
            // 生成确定性会话ID（A→B和B→A使用相同ID）
            determinedSessionId = generateMerchantToMerchantSessionId(operatorId, contactId);

            // 先尝试根据sessionId直接查找
            LambdaQueryWrapper<UnifiedChatSession> directQuery = new LambdaQueryWrapper<>();
            directQuery.eq(UnifiedChatSession::getSessionId, determinedSessionId)
                       .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE);

            UnifiedChatSession existingSession = sessionDao.selectOne(directQuery);
            if (existingSession != null) {
                log.info("【商户到商户】找到现有会话，会话ID: {}, 操作者: {}, 联系人: {}",
                    existingSession.getSessionId(), operatorId, contactId);
                return existingSession;
            }
        }

        // 查找是否存在现有会话 - 修复查询逻辑，避免创建重复session
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();

        // 对于平台端与商户端的会话，需要特殊处理
        if ((merId == 0 && !"PLATFORM".equals(contactType)) || (merId != 0 && "PLATFORM".equals(contactType))) {
            // 平台端(merId=0)与商户端(merId≠0)的跨merId会话
            // 使用更简单的查询条件：只要两个用户ID匹配且会话活跃即可
            queryWrapper.eq(UnifiedChatSession::getSessionType, sessionType)
                    .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE)
                    .and(wrapper -> wrapper
                        // 情况1：操作者->联系人
                        .or(w1 -> w1.eq(UnifiedChatSession::getUserId, operatorId)
                                  .eq(UnifiedChatSession::getStaffId, contactId))
                        // 情况2：联系人->操作者
                        .or(w2 -> w2.eq(UnifiedChatSession::getUserId, contactId)
                                  .eq(UnifiedChatSession::getStaffId, operatorId))
                    );
        } else if (!isMerchantToMerchant) {
            // 同一商户内的会话（非商户到商户）
            queryWrapper.eq(UnifiedChatSession::getMerId, merId)
                    .eq(UnifiedChatSession::getSessionType, sessionType)
                    .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE)
                    .and(wrapper -> wrapper
                        .or(w1 -> w1.eq(UnifiedChatSession::getUserId, operatorId)
                                  .eq(UnifiedChatSession::getStaffId, contactId))
                        .or(w2 -> w2.eq(UnifiedChatSession::getUserId, contactId)
                                  .eq(UnifiedChatSession::getStaffId, operatorId))
                    );
        }

        // 如果不是商户到商户，或者商户到商户但没有找到，继续查找
        if (!isMerchantToMerchant) {
            queryWrapper.orderByDesc(UnifiedChatSession::getUpdateTime).last("LIMIT 1");

            UnifiedChatSession existingSession = sessionDao.selectOne(queryWrapper);
            if (existingSession != null) {
                log.info("找到现有会话，会话ID: {}, 操作者: {}, 联系人: {}, merId: {}",
                    existingSession.getSessionId(), operatorId, contactId, existingSession.getMerId());
                return existingSession;
            }
        }

        // 创建新会话：以操作者为主体，联系人为对话对象
        UnifiedChatSession session = new UnifiedChatSession();
        // 【修复】商户到商户使用确定性会话ID
        session.setSessionId(determinedSessionId != null ? determinedSessionId : generateSessionId());
        
        // 根据联系人类型和merId确定会话结构
        if ("PLATFORM".equals(contactType)) {
            // 与平台管理员的会话：平台端(merId=0) vs 商户端(merId≠0)
            if (merId == 0) {
                // 平台端发起：平台管理员 vs 商户管理员
                session.setUserId(operatorId);
                session.setUserType("PLATFORM");
                session.setStaffId(contactId);
                session.setMerId(0L); // 使用平台的merId
            } else {
                // 商户端发起：商户管理员 vs 平台管理员
                session.setUserId(contactId);
                session.setUserType("PLATFORM");
                session.setStaffId(operatorId);
                session.setMerId(0L); // 平台与商户的会话统一使用平台merId=0
            }
        } else if ("MERCHANT".equals(contactType)) {
            // 【修复】商户到商户的会话：使用一致的会话结构
            // 使用较小的adminId作为userId，较大的adminId作为staffId，确保双向一致
            Long smallerAdminId = Math.min(operatorId, contactId);
            Long largerAdminId = Math.max(operatorId, contactId);

            session.setUserId(smallerAdminId);
            session.setUserType("MERCHANT");
            session.setStaffId(largerAdminId);
            // 对于商户到商户的会话，使用发起方的merId（保持向后兼容）
            session.setMerId(merId);

            log.info("【商户到商户】创建新会话，会话ID: {}, userId(较小): {}, staffId(较大): {}, merId: {}",
                    session.getSessionId(), smallerAdminId, largerAdminId, merId);
        } else if ("USER".equals(contactType)) {
            // 商户管理员对用户：用户是USER，管理员是MERCHANT
            session.setUserId(contactId);
            session.setUserType("USER");
            session.setStaffId(operatorId);
            session.setMerId(merId);
        } else {
            // 默认情况：操作者是MERCHANT
            session.setUserId(operatorId);
            session.setUserType("MERCHANT");
            session.setStaffId(contactId);
            session.setMerId(merId);
        }
        session.setSessionType(sessionType);
        session.setCurrentServiceType(sessionType.equals(UnifiedChatSession.SESSION_TYPE_AI) ? 
            UnifiedChatSession.SERVICE_TYPE_AI : UnifiedChatSession.SERVICE_TYPE_HUMAN);
        session.setStatus(UnifiedChatSession.STATUS_ACTIVE);
        session.setTotalMessages(0);
        session.setPriority(UnifiedChatSession.PRIORITY_NORMAL);
        
        // 根据联系人类型设置会话标题
        String sessionTitle = "与 " + getContactDisplayName(contactId, contactType) + " 的对话";
        session.setSessionTitle(sessionTitle);
        
        Date now = new Date();
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setLastMessageTime(now);

        sessionDao.insert(session);
        
        log.info("基于联系人创建新会话，操作者ID: {}, 联系人ID: {}, 类型: {}, 会话ID: {}", 
            operatorId, contactId, contactType, session.getSessionId());
        return session;
    }
    
    /**
     * 生成会话的唯一标识键
     */
    private String generateSessionKey(Long userId1, Long userId2, Long merId) {
        // 确保键的唯一性，不受用户顺序影响
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        return String.format("%d_%d_%d", smallerId, largerId, merId);
    }

    /**
     * 根据ID确定联系人类型
     */
    private String determineContactTypeById(Long contactId) {
        try {
            // 1. 先检查是否是系统管理员
            SystemAdmin admin = systemAdminService.getById(contactId.intValue());
            if (admin != null) {
                // 商户管理员
                if (admin.getMerId() != null && admin.getMerId() > 0) {
                    return "MERCHANT";
                }
                // 平台管理员
                else {
                    return "PLATFORM";
                }
            }
            
            // 2. 检查是否是普通用户
            User user = userService.getById(contactId.intValue());
            if (user != null) {
                return "USER";
            }
            
            // 3. 默认返回STAFF（管理员）
            log.warn("无法确定联系人类型，ID: {}，默认为MERCHANT", contactId);
            return "MERCHANT";
        } catch (Exception e) {
            log.error("确定联系人类型失败，ID: {}, 错误: {}", contactId, e.getMessage());
            return "MERCHANT"; // 默认类型
        }
    }
    
    /**
     * 获取联系人完整信息（包括头像）
     */
    private Map<String, String> getContactInfo(Long contactId, String contactType) {
        Map<String, String> contactInfo = new HashMap<>();
        
        try {
            switch (contactType) {
                case "USER":
                    User user = userService.getById(contactId.intValue());
                    if (user != null) {
                        contactInfo.put("name", StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname());
                        contactInfo.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
                    } else {
                        contactInfo.put("name", "用户" + contactId);
                        contactInfo.put("avatar", "");
                    }
                    break;
                case "MERCHANT":
                    SystemAdmin staff = systemAdminService.getById(contactId.intValue());
                    if (staff != null) {
                        contactInfo.put("name", StringUtils.hasText(staff.getRealName()) ? staff.getRealName() : staff.getAccount());
                        
                        // 商户客服头像从eb_merchant表获取
                        if (staff.getMerId() != null && staff.getMerId() > 0) {
                            try {
                                Merchant merchant = merchantService.getById(staff.getMerId());
                                contactInfo.put("avatar", merchant != null ? (merchant.getAvatar() != null ? merchant.getAvatar() : "") : "");
                            } catch (Exception e) {
                                log.warn("获取商户头像失败，使用管理员头像: contactId={}", contactId, e);
                                contactInfo.put("avatar", staff.getHeaderImage() != null ? staff.getHeaderImage() : "");
                            }
                        } else {
                            contactInfo.put("avatar", staff.getHeaderImage() != null ? staff.getHeaderImage() : "");
                        }
                    } else {
                        contactInfo.put("name", "商户管理员" + contactId);
                        contactInfo.put("avatar", "");
                    }
                    break;
                case "PLATFORM":
                    SystemAdmin admin = systemAdminService.getById(contactId.intValue());
                    if (admin != null) {
                        contactInfo.put("name", StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getAccount());
                        contactInfo.put("avatar", admin.getHeaderImage() != null ? admin.getHeaderImage() : "");
                    } else {
                        contactInfo.put("name", "平台管理员" + contactId);
                        contactInfo.put("avatar", "");
                    }
                    break;
                default:
                    contactInfo.put("name", contactType + contactId);
                    contactInfo.put("avatar", "");
                    break;
            }
        } catch (Exception e) {
            log.warn("获取联系人信息失败: contactId={}, contactType={}, 错误: {}", contactId, contactType, e.getMessage());
            contactInfo.put("name", contactType + contactId);
            contactInfo.put("avatar", "");
        }
        
        return contactInfo;
    }

    /**
     * 获取联系人显示名称
     */
    private String getContactDisplayName(Long contactId, String contactType) {
        try {
            switch (contactType) {
                case "USER":
                    User user = userService.getById(contactId.intValue());
                    return user != null ? user.getNickname() : "用户" + contactId;
                case "MERCHANT":
                    SystemAdmin staff = systemAdminService.getById(contactId.intValue());
                    return staff != null ? (staff.getRealName() != null ? staff.getRealName() : staff.getAccount()) : "商户管理员" + contactId;

                case "PLATFORM":
                    SystemAdmin admin = systemAdminService.getById(contactId.intValue());
                    return admin != null ? (admin.getRealName() != null ? admin.getRealName() : admin.getAccount()) : "管理员" + contactId;
                default:
                    return contactType + contactId;
            }
        } catch (Exception e) {
            log.warn("获取联系人显示名称失败: {}", e.getMessage());
            return contactType + contactId;
        }
    }

    /**
     * 广播消息到WebSocket
     */
    private void broadcastMessage(UnifiedChatMessage message) {
        try {
            // 获取会话信息以确定广播范围
            UnifiedChatSession session = getSessionInternal(message.getSessionId());
            if (session == null) {
                log.warn("会话不存在，无法广播消息: {}", message.getSessionId());
                return;
            }
            
            // 对于用户发送的消息，需要根据会话状态判断是否广播
            if ("USER".equals(message.getSenderType()) && "user".equals(message.getRole())) {
                if (session.isCurrentlyAiService()) {
                    // AI服务模式下，用户消息不需要广播到商户端（前端已经显示）
                    log.debug("AI服务模式，跳过广播用户消息: {}", message.getMessageId());
                    return;
                } else {
                    // 人工客服模式下，用户消息需要广播到商户端
                    log.debug("人工客服模式，广播用户消息到商户端: {}", message.getMessageId());
                }
            }
            
            // 使用统一消息路由服务处理消息广播
            // 根据消息发送者类型确定源端点
            String sourceEndpoint = null;
            if ("AI".equals(message.getSenderType())) {
                // AI消息不区分来源端点，需要广播给所有相关端
                messageRoutingService.routeMessage(message, null);
            } else if ("MERCHANT".equals(message.getSenderType())) {
                // 商户客服消息来自商户端，路由给用户端和平台端
                messageRoutingService.routeMessage(message, "ADMIN");
            } else if ("PLATFORM".equals(message.getSenderType())) {
                // 平台消息来自平台端，路由给商户端和用户端
                messageRoutingService.routeMessage(message, "MERCHANT");
            } else if ("USER".equals(message.getSenderType())) {
                // 用户消息来自前端，路由给商户端和平台端
                messageRoutingService.routeMessage(message, "FRONT");
            } else if ("SYSTEM".equals(message.getSenderType())) {
                // 系统消息广播到所有端
                messageRoutingService.routeMessage(message, null);
            } else {
                log.warn("未知的发送者类型，使用默认路由: {}", message.getSenderType());
                messageRoutingService.routeMessage(message, null);
            }
            
            log.debug("消息已通过统一路由服务广播: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 创建WebSocket消息数据
     */
    private Map<String, Object> createWebSocketMessage(UnifiedChatMessage message) {
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("type", "unified_message");
        wsMessage.put("messageId", message.getMessageId());
        wsMessage.put("sessionId", message.getSessionId());
        wsMessage.put("senderId", message.getSenderId());
        wsMessage.put("senderType", message.getSenderType());
        wsMessage.put("senderName", message.getSenderName());
        wsMessage.put("senderAvatar", message.getSenderAvatar());
        wsMessage.put("receiverId", message.getReceiverId());
        wsMessage.put("receiverType", message.getReceiverType());
        wsMessage.put("role", message.getRole());
        wsMessage.put("messageType", message.getMessageType());
        wsMessage.put("content", message.getContent());
        wsMessage.put("contentType", message.getContentType());
        wsMessage.put("attachments", message.getAttachments());
        wsMessage.put("status", message.getStatus());
        wsMessage.put("isRead", message.getIsRead());
        wsMessage.put("isSystemMessage", message.getIsSystemMessage());
        wsMessage.put("createTime", message.getCreateTime());
        wsMessage.put("timestamp", System.currentTimeMillis());
        return wsMessage;
    }
    
    /**
     * 向会话参与者广播消息
     */
    private void broadcastToSessionParticipants(UnifiedChatSession session, UnifiedChatMessage message, Map<String, Object> wsMessage) {
        // 获取会话的所有参与者
        Set<Long> participantIds = getSessionParticipants(session);
        
        for (Long participantId : participantIds) {
            try {
                // 确定参与者类型
                String participantType = determineParticipantType(session, participantId);
                
                // 为不同参与者定制消息内容
                Map<String, Object> customizedMessage = customizeMessageForParticipant(wsMessage, participantId, participantType);
                
                // 发送给具体用户
                if (humanServiceWebSocketService != null) {
                    humanServiceWebSocketService.sendMessageToUser(participantId.intValue(), customizedMessage);
                }
                
                log.debug("消息已发送给参与者: ID={}, 类型={}", participantId, participantType);
            } catch (Exception e) {
                log.error("发送消息给参与者失败: ID={}, 错误={}", participantId, e.getMessage());
            }
        }
    }
    
    /**
     * 获取会话参与者
     */
    private Set<Long> getSessionParticipants(UnifiedChatSession session) {
        Set<Long> participants = new HashSet<>();
        
        // 添加会话的用户
        if (session.getUserId() != null) {
            participants.add(session.getUserId());
        }
        
        // 添加会话的客服/商户
        if (session.getStaffId() != null) {
            participants.add(session.getStaffId());
        }
        
        return participants;
    }
    
    /**
     * 确定参与者类型
     */
    private String determineParticipantType(UnifiedChatSession session, Long participantId) {
        if (Objects.equals(session.getUserId(), participantId)) {
            return session.getUserType(); // "CUSTOMER", "MERCHANT", 等
        } else if (Objects.equals(session.getStaffId(), participantId)) {
            return "MERCHANT"; // 商户管理员类型
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * 为不同参与者定制消息内容
     */
    private Map<String, Object> customizeMessageForParticipant(Map<String, Object> baseMessage, Long participantId, String participantType) {
        Map<String, Object> customizedMessage = new HashMap<>(baseMessage);
        
        // 添加接收者特定信息
        customizedMessage.put("recipientId", participantId);
        customizedMessage.put("recipientType", participantType);
        
        // 判断这条消息对当前参与者来说是发送的还是接收的
        Long senderId = (Long) baseMessage.get("senderId");
        boolean isOwnMessage = Objects.equals(senderId, participantId);
        customizedMessage.put("isOwnMessage", isOwnMessage);
        
        return customizedMessage;
    }
    @Override
    public List<Map<String, Object>> getContactListWithMessages(Long merId, Long currentUserId, Integer page, Integer size) {
        log.info("获取联系人列表，商户ID: {}, 当前用户ID: {}, 页码: {}, 大小: {}", merId, currentUserId, page, size);

        try {
            // 使用Map存储联系人，key为"contactType_contactId"，避免重复
            Map<String, Map<String, Object>> contactMap = new HashMap<>();

            // 1. 首先获取所有联系人关系（包括没有会话的联系人）
            List<ContactRelationship> allRelationships = contactRelationshipService.getByCurrentUserId(currentUserId);
            log.info("找到联系人关系数量: {}", allRelationships.size());

            // 将所有联系人先添加到 contactMap（作为基础信息）
            for (ContactRelationship relationship : allRelationships) {
                if (relationship.getStatus() == null || !relationship.getStatus()) {
                    // 跳过已删除的联系人
                    continue;
                }

                String contactKey = relationship.getContactType() + "_" + relationship.getContactId();
                Map<String, Object> contact = new HashMap<>();
                contact.put("contactId", relationship.getContactId().longValue());
                contact.put("contactType", relationship.getContactType());
                contact.put("contactName", relationship.getContactName());
                contact.put("contactAvatar", relationship.getContactAvatar() != null ? relationship.getContactAvatar() : "");
                contact.put("sessionId", null); // 暂时没有会话
                contact.put("lastMessage", "");
                contact.put("lastMessageTime", relationship.getLastContactTime() != null ? relationship.getLastContactTime() : relationship.getCreateTime());
                contact.put("lastMessageType", "text");
                contact.put("notes", relationship.getNotes() != null ? relationship.getNotes() : "");
                contact.put("isPinned", relationship.getIsPinned() != null ? relationship.getIsPinned() : false);
                contact.put("unreadCount", 0);
                contact.put("hasSession", false); // 标记是否有会话

                contactMap.put(contactKey, contact);
            }
            log.info("初始化联系人基础信息完成，联系人数量: {}", contactMap.size());

            // 2. 获取当前用户参与的所有会话
            LambdaQueryWrapper<UnifiedChatSession> sessionWrapper = new LambdaQueryWrapper<>();
            sessionWrapper.eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE)
                    .and(wrapper -> wrapper
                        // 情况1：当前用户是会话的userId
                        .eq(UnifiedChatSession::getUserId, currentUserId)
                        // 情况2：当前用户是会话的staffId
                        .or().eq(UnifiedChatSession::getStaffId, currentUserId)
                    )
                    .orderByDesc(UnifiedChatSession::getLastMessageTime);

            List<UnifiedChatSession> userSessions = sessionDao.selectList(sessionWrapper);
            log.info("找到用户参与的会话数量: {}", userSessions.size());

            // 3. 为每个会话更新联系人的会话信息（更新有会话的联系人）

            for (UnifiedChatSession session : userSessions) {
                // 获取会话的最新消息
                LambdaQueryWrapper<UnifiedChatMessage> messageWrapper = new LambdaQueryWrapper<>();
                messageWrapper.eq(UnifiedChatMessage::getSessionId, session.getSessionId())
                        .orderByDesc(UnifiedChatMessage::getCreateTime)
                        .last("LIMIT 1");

                UnifiedChatMessage latestMessage = messageDao.selectOne(messageWrapper);

                // 确定联系人ID和类型（排除当前用户）
                Long contactId = null;
                String contactType = null;
                String contactName = null;
                String contactAvatar = null;

                // 根据会话结构确定联系人
                if (Objects.equals(session.getUserId(), currentUserId)) {
                    // 当前用户是userId，联系人是staffId
                    contactId = session.getStaffId();
                    // 修复：根据实际业务逻辑确定联系人类型
                    // staffId可能是商户管理员(STAFF)或其他类型，需要查询数据库确定
                    contactType = determineContactTypeById(contactId);
                } else if (Objects.equals(session.getStaffId(), currentUserId)) {
                    // 当前用户是staffId，联系人是userId
                    contactId = session.getUserId();
                    contactType = session.getUserType();
                }

                // 跳过无效的联系人或找不到联系人的会话
                if (contactId == null || Objects.equals(contactId, currentUserId)) {
                    log.warn("会话联系人信息异常，跳过。会话ID: {}, 当前用户: {}, 会话userId: {}, 会话staffId: {}",
                        session.getSessionId(), currentUserId, session.getUserId(), session.getStaffId());
                    continue;
                }

                // 获取联系人显示名称和头像
                Map<String, String> contactInfo = getContactInfo(contactId, contactType);
                contactName = contactInfo.get("name");
                contactAvatar = contactInfo.get("avatar");

                // 生成联系人的唯一标识（避免重复）
                String contactKey = contactType + "_" + contactId;

                // 如果联系人已经在 contactMap 中（来自联系人关系表），更新其会话信息
                if (contactMap.containsKey(contactKey)) {
                    Map<String, Object> existingContact = contactMap.get(contactKey);
                    Date existingTime = (Date) existingContact.get("lastMessageTime");
                    Date currentTime = latestMessage != null ? latestMessage.getCreateTime() : session.getLastMessageTime();
                    Boolean hasSession = (Boolean) existingContact.getOrDefault("hasSession", false);

                    // 如果还没有会话信息，或者当前会话的消息更新，则更新联系人信息
                    if (!hasSession || (currentTime != null && (existingTime == null || currentTime.after(existingTime)))) {
                        // 更新会话相关信息
                        updateContactInfo(contactMap, contactKey, session, latestMessage, contactId, contactType, contactName, contactAvatar, currentUserId);
                    }
                } else {
                    // 如果联系人不在 contactMap 中（说明有会话但没有建立联系人关系），也添加进去
                    // 这种情况可能是历史遗留数据
                    log.warn("发现有会话但无联系人关系的情况，会话ID: {}, 联系人ID: {}, 联系人类型: {}",
                        session.getSessionId(), contactId, contactType);
                    updateContactInfo(contactMap, contactKey, session, latestMessage, contactId, contactType, contactName, contactAvatar, currentUserId);
                }
            }
            
            // 3. 计算未读消息数量
            for (Map<String, Object> contact : contactMap.values()) {
                String sessionId = (String) contact.get("sessionId");
                if (sessionId != null) {
                    // 计算未读消息数量（当前用户作为接收者且未读的消息）
                    LambdaQueryWrapper<UnifiedChatMessage> unreadWrapper = new LambdaQueryWrapper<>();
                    unreadWrapper.eq(UnifiedChatMessage::getSessionId, sessionId)
                            .eq(UnifiedChatMessage::getReceiverId, currentUserId)
                            .eq(UnifiedChatMessage::getIsRead, false);
                    
                    int unreadCount = messageDao.selectCount(unreadWrapper).intValue();
                    contact.put("unreadCount", unreadCount);
                }
            }
            
            // 4. 转换为List并排序
            List<Map<String, Object>> contactList = new ArrayList<>(contactMap.values());
            
            // 排序：置顶的在前，然后按最后消息时间倒序
            contactList.sort((a, b) -> {
                Boolean aPinned = (Boolean) a.getOrDefault("isPinned", false);
                Boolean bPinned = (Boolean) b.getOrDefault("isPinned", false);
                
                if (!aPinned.equals(bPinned)) {
                    return bPinned.compareTo(aPinned); // 置顶的在前
                }
                
                Date aTime = (Date) a.get("lastMessageTime");
                Date bTime = (Date) b.get("lastMessageTime");
                
                if (aTime == null && bTime == null) return 0;
                if (aTime == null) return 1;
                if (bTime == null) return -1;
                
                return bTime.compareTo(aTime); // 时间倒序
            });
            
            // 5. 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, contactList.size());
            
            if (start >= contactList.size()) {
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> pagedContacts = contactList.subList(start, end);
            
            log.info("获取联系人列表成功，总数: {}, 返回数量: {}", contactList.size(), pagedContacts.size());
            return pagedContacts;
            
        } catch (Exception e) {
            log.error("获取联系人列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 更新联系人信息
     */
    private void updateContactInfo(Map<String, Map<String, Object>> contactMap, String contactKey,
                                 UnifiedChatSession session, UnifiedChatMessage latestMessage,
                                 Long contactId, String contactType, String contactName, String contactAvatar, Long currentUserId) {
        // 获取或创建联系人信息（保留已有的基础信息）
        Map<String, Object> contact = contactMap.getOrDefault(contactKey, new HashMap<>());

        // 更新基本信息
        contact.put("contactId", contactId);
        contact.put("contactType", contactType);
        contact.put("contactName", contactName);
        contact.put("contactAvatar", contactAvatar != null ? contactAvatar : "");
        contact.put("sessionId", session.getSessionId());
        contact.put("hasSession", true); // 标记该联系人有会话

        // 更新消息信息
        if (latestMessage != null) {
            contact.put("lastMessage", latestMessage.getContent());
            contact.put("lastMessageTime", latestMessage.getCreateTime());
            contact.put("lastMessageType", latestMessage.getMessageType());
        } else {
            contact.put("lastMessage", "");
            contact.put("lastMessageTime", session.getCreateTime());
            contact.put("lastMessageType", "text");
        }

        // 如果还没有 notes 和 isPinned 信息，从联系人关系表获取
        if (!contact.containsKey("notes") || !contact.containsKey("isPinned")) {
            try {
                List<ContactRelationship> relationships = contactRelationshipService.getByCurrentUserId(currentUserId);
                ContactRelationship relationship = relationships.stream()
                        .filter(r -> r.getContactId().equals(contactId.intValue()) && contactType.equals(r.getContactType()) && r.getStatus())
                        .findFirst()
                        .orElse(null);

                if (relationship != null) {
                    contact.put("isPinned", relationship.getIsPinned() != null ? relationship.getIsPinned() : false);
                    contact.put("notes", relationship.getNotes() != null ? relationship.getNotes() : "");
                } else {
                    if (!contact.containsKey("isPinned")) {
                        contact.put("isPinned", false);
                    }
                    if (!contact.containsKey("notes")) {
                        contact.put("notes", "");
                    }
                }
            } catch (Exception e) {
                log.warn("获取联系人置顶状态失败，contactId: {}, contactType: {}", contactId, contactType, e);
                if (!contact.containsKey("isPinned")) {
                    contact.put("isPinned", false);
                }
                if (!contact.containsKey("notes")) {
                    contact.put("notes", "");
                }
            }
        }

        // 初始化未读数为 0，后续会重新计算
        contact.put("unreadCount", 0);

        contactMap.put(contactKey, contact);
    }
    
    /**
     * 从联系人关系表获取联系人列表
     */
    private List<Map<String, Object>> getContactListFromRelationships(Long merId, Long currentUserId, Integer page, Integer size) {
        try {
            log.info("从联系人关系表获取联系人列表，商户ID: {}, 当前用户ID: {}", merId, currentUserId);
            
            // 查询联系人关系表
            List<ContactRelationship> relationships = contactRelationshipService.getByCurrentUserId(currentUserId);
            
            if (relationships.isEmpty()) {
                log.info("没有找到联系人关系记录，返回空列表");
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> contactList = new ArrayList<>();
            
            for (ContactRelationship relationship : relationships) {
                Map<String, Object> contact = new HashMap<>();
                contact.put("contactId", relationship.getContactId());
                contact.put("contactType", relationship.getContactType());
                contact.put("contactName", relationship.getContactName());
                contact.put("contactAvatar", relationship.getContactAvatar() != null ? relationship.getContactAvatar() : "");
                contact.put("sessionId", ""); // 没有会话时为空
                contact.put("lastMessage", ""); // 没有消息时为空
                contact.put("lastMessageTime", relationship.getLastContactTime());
                contact.put("lastMessageType", "text");
                contact.put("unreadCount", 0); // 没有消息时未读数为0
                contact.put("isPinned", relationship.getIsPinned() != null ? relationship.getIsPinned() : false);
                contact.put("notes", relationship.getNotes() != null ? relationship.getNotes() : "");
                
                contactList.add(contact);
            }
            
            // 排序：置顶的在前，然后按最后联系时间倒序
            contactList.sort((a, b) -> {
                Boolean aPinned = (Boolean) a.getOrDefault("isPinned", false);
                Boolean bPinned = (Boolean) b.getOrDefault("isPinned", false);
                
                if (!aPinned.equals(bPinned)) {
                    return bPinned.compareTo(aPinned); // 置顶的在前
                }
                
                Date aTime = (Date) a.get("lastMessageTime");
                Date bTime = (Date) b.get("lastMessageTime");
                
                if (aTime == null && bTime == null) return 0;
                if (aTime == null) return 1;
                if (bTime == null) return -1;
                
                return bTime.compareTo(aTime); // 时间倒序
            });
            
            // 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, contactList.size());
            
            if (start >= contactList.size()) {
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> pagedContacts = contactList.subList(start, end);
            
            log.info("从联系人关系表获取联系人成功，总数: {}, 返回数量: {}", contactList.size(), pagedContacts.size());
            return pagedContacts;
            
        } catch (Exception e) {
            log.error("从联系人关系表获取联系人失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取商户的所有会话ID
     */
    private List<String> getAllMerchantSessionIds(Long merId) {
        try {
            LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UnifiedChatSession::getMerId, merId)
                    .select(UnifiedChatSession::getSessionId);
            
            List<UnifiedChatSession> sessions = sessionDao.selectList(queryWrapper);
            return sessions.stream()
                    .map(UnifiedChatSession::getSessionId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取商户会话ID列表失败，商户ID: {}", merId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成联系人显示名称
     */
    private String generateContactDisplayName(String userType, Long userId) {
        try {
            // 根据用户类型从对应的服务获取用户信息
            if ("CUSTOMER".equals(userType) || "USER".equals(userType)) {
                // 从用户服务获取用户信息
                if (userService != null) {
                    User user = userService.getById(userId);
                    if (user != null && user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                        return user.getNickname();
                    }
                }
                return "用户" + userId;
            } else if ("MERCHANT".equals(userType)) {
                // 从商户服务获取商户信息
                if (merchantService != null) {
                    Merchant merchant = merchantService.getById(userId);
                    if (merchant != null && merchant.getName() != null && !merchant.getName().trim().isEmpty()) {
                        return merchant.getName();
                    }
                }
                return "商户" + userId;
            } else if ("PLATFORM".equals(userType)) {
                // 从系统管理员服务获取管理员信息
                 if (systemAdminService != null) {
                     SystemAdmin admin = systemAdminService.getById(userId);
                     if (admin != null && admin.getRealName() != null && !admin.getRealName().trim().isEmpty()) {
                         return admin.getRealName();
                     }
                 }
                return "管理员" + userId;
            }
        } catch (Exception e) {
            log.warn("获取联系人显示名称失败: userType={}, userId={}, error={}", userType, userId, e.getMessage());
        }
        
        // 默认显示名称
        return "联系人" + userId;
    }

    @Override
    public void markSessionMessagesAsRead(String sessionId) {
        try {
            // 获取当前管理员ID（不是商户ID）
            Long currentUserId = SecurityUtil.getLoginUserVo().getUser().getId().longValue();
            
            // 更新会话中所有未读消息为已读
            LambdaUpdateWrapper<UnifiedChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UnifiedChatMessage::getSessionId, sessionId)
                        .ne(UnifiedChatMessage::getSenderId, currentUserId) // 不是自己发送的消息
                        .isNull(UnifiedChatMessage::getReadTime); // 未读的消息
            
            UnifiedChatMessage updateMessage = new UnifiedChatMessage();
            updateMessage.setReadTime(LocalDateTime.now());
            updateMessage.setIsRead(true);
            this.unifiedChatMessageDao.update(updateMessage, updateWrapper);
            log.info("标记会话消息为已读: sessionId={}, userId={}", sessionId, currentUserId);
            
        } catch (Exception e) {
            log.error("标记会话消息为已读失败: sessionId={}", sessionId, e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    @Override
    public void markSingleMessageAsRead(String messageId) {
        try {
            // 获取当前管理员ID（不是商户ID）
            Long currentUserId = SecurityUtil.getLoginUserVo().getUser().getId().longValue();
            
            // 查询消息
            UnifiedChatMessage message = messageDao.selectById(messageId);
            if (message == null) {
                throw new RuntimeException("消息不存在");
            }
            
            // 只有接收者才能标记消息为已读
            if (message.getSenderId().equals(currentUserId)) {
                log.warn("不能标记自己发送的消息为已读: messageId={}", messageId);
                return;
            }
            
            // 更新消息已读状态
            message.setReadTime(LocalDateTime.now());
            message.setIsRead(true);
            messageDao.updateById(message);
            
            log.info("标记消息为已读: messageId={}, userId={}", messageId, currentUserId);
            
        } catch (Exception e) {
            log.error("标记消息为已读失败: messageId={}", messageId, e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    @Override
    public Map<String, Object> getMessageReadStatus(String messageId) {
        try {
            UnifiedChatMessage message = unifiedChatMessageDao.selectById(messageId);
            if (message == null) {
                throw new RuntimeException("消息不存在");
            }
            
            Map<String, Object> readStatus = new HashMap<>();
            readStatus.put("messageId", messageId);
            readStatus.put("readTime", message.getReadTime());
            readStatus.put("isRead", message.getIsRead());
            
            return readStatus;
            
        } catch (Exception e) {
            log.error("获取消息已读状态失败: messageId={}", messageId, e);
            throw new RuntimeException("获取消息已读状态失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableUsersByType(String userType, String keyword) {
        List<Map<String, Object>> users = new ArrayList<>();
        
        try {
            switch (userType.toUpperCase()) {
                case "USER":
                case "CUSTOMER":
                    // 从 eb_user 表获取用户列表
                    LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                    userWrapper.eq(User::getStatus, true); // 只获取状态正常的用户
                    if (StringUtils.hasText(keyword)) {
                        userWrapper.and(wrapper -> 
                            wrapper.like(User::getNickname, keyword)
                                .or()
                                .like(User::getPhone, keyword)
                                .or()
                                .like(User::getRealName, keyword)
                        );
                    }
                    userWrapper.orderByDesc(User::getCreateTime);
                    userWrapper.last("LIMIT 50"); // 限制返回数量，没有关键词时显示最近的50个
                    
                    List<User> userList = userService.list(userWrapper);
                    for (User user : userList) {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("name", StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname());
                        userInfo.put("displayName", userInfo.get("name"));
                        userInfo.put("phone", user.getPhone());
                        userInfo.put("avatar", user.getAvatar());
                        userInfo.put("type", "USER");
                        users.add(userInfo);
                    }
                    break;
//
//                case "MERCHANT":
//                    // 从 eb_merchant 表获取商户列表
//                    LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
//                    merchantWrapper.eq(Merchant::getIsDel, false); // 只获取未删除的商户
//                    if (StringUtils.hasText(keyword)) {
//                        merchantWrapper.and(wrapper ->
//                            wrapper.like(Merchant::getName, keyword)
//                                .or()
//                                .like(Merchant::getPhone, keyword)
//                                .or()
//                                .like(Merchant::getRealName, keyword)
//                        );
//                    }
//                    merchantWrapper.orderByDesc(Merchant::getCreateTime);
//                    merchantWrapper.last("LIMIT 50"); // 限制返回数量，没有关键词时显示最近的50个
//
//                    List<Merchant> merchantList = merchantService.list(merchantWrapper);
//                    for (Merchant merchant : merchantList) {
//                        Map<String, Object> merchantInfo = new HashMap<>();
//                        merchantInfo.put("id", merchant.getId());
//                        merchantInfo.put("name", StringUtils.hasText(merchant.getRealName()) ? merchant.getRealName() : merchant.getName());
//                        merchantInfo.put("displayName", merchantInfo.get("name"));
//                        merchantInfo.put("phone", merchant.getPhone());
//                        merchantInfo.put("avatar", merchant.getAvatar());
//                        merchantInfo.put("type", "MERCHANT");
//                        merchantInfo.put("storeName", merchant.getName()); // 店铺名称
//                        users.add(merchantInfo);
//                    }
//                    break;
                case "MERCHANT":
                    // 从 eb_system_admin 表获取商户管理员列表（mer_id != 0 的是平台管理员）
                    LambdaQueryWrapper<SystemAdmin> merchantWrapper = new LambdaQueryWrapper<>();
                    merchantWrapper.ne(SystemAdmin::getMerId, 0) // 商户管理员的商户ID不等于0
                            .eq(SystemAdmin::getStatus, true); // 只获取状态正常的管理员
                    if (StringUtils.hasText(keyword)) {
                        merchantWrapper.and(wrapper ->
                                wrapper.like(SystemAdmin::getRealName, keyword)
                                        .or()
                                        .like(SystemAdmin::getPhone, keyword)
                                        .or()
                                        .like(SystemAdmin::getAccount, keyword)
                        );
                    }
                    merchantWrapper.orderByDesc(SystemAdmin::getCreateTime);
                    merchantWrapper.last("LIMIT 50"); // 限制返回数量，没有关键词时显示最近的50个

                    List<SystemAdmin> merchantAdminList = systemAdminService.list(merchantWrapper);
                    for (SystemAdmin admin : merchantAdminList) {
                        Map<String, Object> adminInfo = new HashMap<>();
                        adminInfo.put("id", admin.getId());
                        adminInfo.put("name", StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getAccount());
                        adminInfo.put("displayName", adminInfo.get("name"));
                        adminInfo.put("phone", admin.getPhone());
                        adminInfo.put("avatar", "");
                        adminInfo.put("type", "PLATFORM");
                        adminInfo.put("account", admin.getAccount()); // 账号
                        users.add(adminInfo);
                    }
                    break;
                case "PLATFORM":
                    // 从 eb_system_admin 表获取平台管理员列表（mer_id = 0 的是平台管理员）
                    LambdaQueryWrapper<SystemAdmin> platformWrapper = new LambdaQueryWrapper<>();
                    platformWrapper.eq(SystemAdmin::getMerId, 0) // 平台管理员的商户ID为0
                                   .eq(SystemAdmin::getStatus, true); // 只获取状态正常的管理员
                    if (StringUtils.hasText(keyword)) {
                        platformWrapper.and(wrapper -> 
                            wrapper.like(SystemAdmin::getRealName, keyword)
                                .or()
                                .like(SystemAdmin::getPhone, keyword)
                                .or()
                                .like(SystemAdmin::getAccount, keyword)
                        );
                    }
                    platformWrapper.orderByDesc(SystemAdmin::getCreateTime);
                    platformWrapper.last("LIMIT 50"); // 限制返回数量，没有关键词时显示最近的50个
                    
                    List<SystemAdmin> platformAdminList = systemAdminService.list(platformWrapper);
                    for (SystemAdmin admin : platformAdminList) {
                        Map<String, Object> adminInfo = new HashMap<>();
                        adminInfo.put("id", admin.getId());
                        adminInfo.put("name", StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getAccount());
                        adminInfo.put("displayName", adminInfo.get("name"));
                        adminInfo.put("phone", admin.getPhone());
                        adminInfo.put("avatar", "");
                        adminInfo.put("type", "PLATFORM");
                        adminInfo.put("account", admin.getAccount()); // 账号
                        users.add(adminInfo);
                    }
                    break;
                    
                default:
                    log.warn("不支持的用户类型: {}", userType);
                    break;
            }
            
            log.info("获取{}类型用户列表，关键词: {}, 结果数量: {}", userType, keyword, users.size());
            
        } catch (Exception e) {
            log.error("获取{}类型用户列表失败", userType, e);
        }
        
        return users;
    }
    @Override
    public UnifiedChatSession getOrCreateSession(String sessionId, LoginFrontUserVo finalUserContext) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        queryWrapper.eq(UnifiedChatSession::getUserId, finalUserContext.getUser().getId());
        return sessionDao.selectOne(queryWrapper);

    }

    @Override
    public UnifiedChatSession createOrGetUserSession(long userId,Integer merId, String cozeBotId) {
        // 查找是否有活跃的会话
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getUserId, userId)
                .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE);

        if (StringUtils.hasText(cozeBotId)) {
            queryWrapper.eq(UnifiedChatSession::getCozeBotId, cozeBotId);
        }

        UnifiedChatSession existingSession = sessionDao.selectOne(queryWrapper);
        if (existingSession != null) {
            return existingSession;
        }

        // 创建新会话 - 同时调用Coze API创建会话
        UnifiedChatSession session = new UnifiedChatSession();
        session.setSessionId(generateSessionId());
        session.setUserId(userId);
        session.setUserType(UnifiedChatSession.USER_TYPE_CUSTOMER);
        session.setMerId(merId.longValue());
        session.setSessionType(UnifiedChatSession.SESSION_TYPE_AI);
        session.setCurrentServiceType(UnifiedChatSession.SESSION_TYPE_AI);
        session.setCozeBotId(cozeBotId);
        session.setStatus(UnifiedChatSession.STATUS_ACTIVE);
        session.setTotalMessages(0);
        session.setPriority(UnifiedChatSession.PRIORITY_NORMAL);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());

        // 如果指定了Coze Bot ID，调用Coze API创建会话
        if (StringUtils.hasText(cozeBotId)) {
            try {
                // 创建Coze会话请求
                com.zbkj.common.request.coze.CozeCreateConversationRequest cozeRequest = 
                    new com.zbkj.common.request.coze.CozeCreateConversationRequest();
                cozeRequest.setBotId(cozeBotId);
                cozeRequest.setConversationName("用户" + userId + "的聊天会话");
              //  cozeRequest.setDescription("系统自动创建的用户聊天会话");
                cozeRequest.setConnectorId("1024");
                // 调用Coze API创建会话
                com.zbkj.common.response.coze.CozeCreateConversationResponse cozeResponse = 
                    cozeService.createConversation(cozeRequest);

                if (cozeResponse != null && cozeResponse.getCode() == 0 && cozeResponse.getData() != null) {
                    // 设置Coze会话ID
                    session.setCozeConversationId(cozeResponse.getData().getId());
                    log.info("成功创建Coze会话，用户ID: {}, 本地会话ID: {}, Coze会话ID: {}", 
                            userId, session.getSessionId(), cozeResponse.getData().getId());
                } else {
                    log.error("创建Coze会话失败，用户ID: {}, 响应: {}", userId, cozeResponse);
                    throw new RuntimeException("创建Coze会话失败: " + (cozeResponse != null ? cozeResponse.getMsg() : "未知错误"));
                }
            } catch (Exception e) {
                log.error("调用Coze API创建会话失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
                throw new RuntimeException("创建Coze会话失败: " + e.getMessage());
            }
        }

        sessionDao.insert(session);

        log.info("创建新会话，用户ID: {}, 会话ID: {}, 类型: {}, Coze会话ID: {}", 
                userId, session.getSessionId(), UnifiedChatSession.SESSION_TYPE_AI, session.getCozeConversationId());
        return session;
    }

    @Override
    public com.github.pagehelper.PageInfo<MessageResponse> getMessagesForManagement(
            String sessionId, String messageType, String role, String content, 
            String senderType, String status, Integer page, Integer size) {
        
        log.info("获取消息管理列表，参数：sessionId={}, messageType={}, role={}, content={}, senderType={}, status={}, page={}, size={}", 
                sessionId, messageType, role, content, senderType, status, page, size);
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // 限制最大页面大小

        // 启动分页
        com.github.pagehelper.Page<UnifiedChatMessage> startPage = PageHelper.startPage(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        
        // 默认只查询AI助手的回复消息
        queryWrapper.eq(UnifiedChatMessage::getRole, "assistant")
                   .eq(UnifiedChatMessage::getSenderType, "AI");
        
        // 会话ID过滤
        if (StringUtils.hasText(sessionId)) {
            queryWrapper.eq(UnifiedChatMessage::getSessionId, sessionId);
        }
        
        // 消息类型过滤
        if (StringUtils.hasText(messageType)) {
            queryWrapper.eq(UnifiedChatMessage::getMessageType, messageType);
        }
        
        // 角色过滤（如果用户指定了角色，则覆盖默认的assistant过滤）
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(UnifiedChatMessage::getRole, role);
        }
        
        // 内容关键词搜索
        if (StringUtils.hasText(content)) {
            queryWrapper.like(UnifiedChatMessage::getContent, content);
        }
        
        // 发送者类型过滤（如果用户指定了发送者类型，则覆盖默认的AI过滤）
        if (StringUtils.hasText(senderType)) {
            queryWrapper.eq(UnifiedChatMessage::getSenderType, senderType);
        }
        
        // 消息状态过滤
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(UnifiedChatMessage::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(UnifiedChatMessage::getCreateTime);

        // 查询消息列表
        List<UnifiedChatMessage> messageList = messageDao.selectList(queryWrapper);
        
        // 转换为MessageResponse
        List<MessageResponse> responseList = messageList.stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        
        // 使用项目标准的分页工具类
        com.github.pagehelper.PageInfo<MessageResponse> result = CommonPage.copyPageInfo(startPage, responseList);
        
        log.info("获取消息管理列表成功，总数: {}, 当前页: {}, 页大小: {}", result.getTotal(), result.getPageNum(), result.getPageSize());
        
        return result;
    }

    @Override
    public com.github.pagehelper.PageInfo<UnifiedChatSession> getSessionsForManagement(
            Long merId, String sessionType, String status, Long userId, 
            String cozeBotId, String sessionId, Integer page, Integer size) {
        
        log.info("获取会话管理列表，参数：merId={}, sessionType={}, status={}, userId={}, cozeBotId={}, sessionId={}, page={}, size={}", 
                merId, sessionType, status, userId, cozeBotId, sessionId, page, size);
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // 限制最大页面大小

        // 启动分页
        com.github.pagehelper.Page<UnifiedChatSession> startPage = PageHelper.startPage(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        
        // 商户ID过滤（必须条件）
        queryWrapper.eq(UnifiedChatSession::getMerId, merId);
        
        // 会话类型过滤
        if (StringUtils.hasText(sessionType)) {
            queryWrapper.eq(UnifiedChatSession::getSessionType, sessionType);
        }
        
        // 会话状态过滤
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(UnifiedChatSession::getStatus, status);
        }
        
        // 用户ID过滤
        if (userId != null) {
            queryWrapper.eq(UnifiedChatSession::getUserId, userId);
        }
        
        // Coze智能体ID过滤
        if (StringUtils.hasText(cozeBotId)) {
            queryWrapper.eq(UnifiedChatSession::getCozeBotId, cozeBotId);
        }
        
        // 会话ID过滤
        if (StringUtils.hasText(sessionId)) {
            queryWrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        }
        
        // 按更新时间倒序排列
        queryWrapper.orderByDesc(UnifiedChatSession::getUpdateTime);

        // 查询会话列表
        List<UnifiedChatSession> sessionList = sessionDao.selectList(queryWrapper);
        
        // 使用项目标准的分页工具类
        com.github.pagehelper.PageInfo<UnifiedChatSession> result = CommonPage.copyPageInfo(startPage, sessionList);
        
        log.info("获取会话管理列表成功，总数: {}, 当前页: {}, 页大小: {}", result.getTotal(), result.getPageNum(), result.getPageSize());
        
        return result;
    }

    @Override
    public PageInfo<MessageResponse> getHumanServiceMessages(
            Long merId, String sessionId, String messageType, String role, String content, 
            String senderType, String status, Integer page, Integer size) {
        
        log.info("获取人工客服消息列表，参数：merId={}, sessionId={}, messageType={}, role={}, content={}, senderType={}, status={}, page={}, size={}", 
                merId, sessionId, messageType, role, content, senderType, status, page, size);
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100; // 限制最大页面大小

        // 构建查询条件
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        
        // 排除AI助手消息：不是assistant角色或不是AI发送者类型的消息
        queryWrapper.and(wrapper -> wrapper
            .ne(UnifiedChatMessage::getRole, "assistant")
            .or()
            .ne(UnifiedChatMessage::getSenderType, "AI")
        );
        
        // 通过会话获取商户ID过滤（需要关联查询会话表）
        if (merId != null) {
            // 先获取该商户的所有会话ID
            LambdaQueryWrapper<UnifiedChatSession> sessionQueryWrapper = new LambdaQueryWrapper<>();
            sessionQueryWrapper.eq(UnifiedChatSession::getMerId, merId);
            List<UnifiedChatSession> merchantSessions = sessionDao.selectList(sessionQueryWrapper);
            
            if (!merchantSessions.isEmpty()) {
                List<String> sessionIds = merchantSessions.stream()
                        .map(UnifiedChatSession::getSessionId)
                        .collect(Collectors.toList());
                queryWrapper.in(UnifiedChatMessage::getSessionId, sessionIds);
            } else {
                // 如果该商户没有会话，则返回空结果
                PageInfo<MessageResponse> emptyResult = new PageInfo<>();
                emptyResult.setTotal(0);
                emptyResult.setPageNum(page);
                emptyResult.setPageSize(size);
                emptyResult.setPages(0);
                emptyResult.setList(new ArrayList<>());
                return emptyResult;
            }
        }
        
        // 会话ID过滤
        if (StringUtils.hasText(sessionId)) {
            queryWrapper.eq(UnifiedChatMessage::getSessionId, sessionId);
        }
        
        // 消息类型过滤
        if (StringUtils.hasText(messageType)) {
            queryWrapper.eq(UnifiedChatMessage::getMessageType, messageType);
        }
        
        // 角色过滤
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(UnifiedChatMessage::getRole, role);
        }
        
        // 内容关键词搜索
        if (StringUtils.hasText(content)) {
            queryWrapper.like(UnifiedChatMessage::getContent, content);
        }
        
        // 发送者类型过滤
        if (StringUtils.hasText(senderType)) {
            queryWrapper.eq(UnifiedChatMessage::getSenderType, senderType);
        }
        
        // 消息状态过滤
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(UnifiedChatMessage::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(UnifiedChatMessage::getCreateTime);

        // 启动分页并查询消息列表
        PageHelper.startPage(page, size);
        List<UnifiedChatMessage> messageList = messageDao.selectList(queryWrapper);
        PageInfo<UnifiedChatMessage> pageInfo = new PageInfo<>(messageList);
        
        log.info("分页查询结果 - 返回记录数: {}, 总记录数: {}", messageList.size(), pageInfo.getTotal());
        
        // 转换为MessageResponse
        List<MessageResponse> responseList = messageList.stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        
        // 创建返回结果
        PageInfo<MessageResponse> result = new PageInfo<>();
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setPages(pageInfo.getPages());
        result.setList(responseList);
        result.setHasPreviousPage(pageInfo.isHasPreviousPage());
        result.setHasNextPage(pageInfo.isHasNextPage());
        result.setIsFirstPage(pageInfo.isIsFirstPage());
        result.setIsLastPage(pageInfo.isIsLastPage());
        result.setPrePage(pageInfo.getPrePage());
        result.setNextPage(pageInfo.getNextPage());
        result.setNavigatePages(pageInfo.getNavigatePages());
        result.setNavigatepageNums(pageInfo.getNavigatepageNums());
        result.setNavigateFirstPage(pageInfo.getNavigateFirstPage());
        result.setNavigateLastPage(pageInfo.getNavigateLastPage());
        
        log.info("获取人工客服消息列表成功，总数: {}, 当前页: {}, 页大小: {}", result.getTotal(), result.getPageNum(), result.getPageSize());
        
        return result;
    }

    @Override
    public UnifiedChatMessage getMessageById(String messageId) {
        log.info("根据消息ID获取消息详情，messageId: {}", messageId);
        
        if (!StringUtils.hasText(messageId)) {
            log.warn("消息ID为空");
            return null;
        }
        
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatMessage::getMessageId, messageId);
        
        UnifiedChatMessage message = messageDao.selectOne(queryWrapper);
        
        if (message != null) {
            log.info("找到消息详情，messageId: {}, sessionId: {}", messageId, message.getSessionId());
        } else {
            log.warn("未找到消息，messageId: {}", messageId);
        }
        
        return message;
    }

    @Override
    public boolean markMessageAsRead(String messageId) {
        log.info("标记消息为已读，messageId: {}", messageId);
        
        if (!StringUtils.hasText(messageId)) {
            log.warn("消息ID为空");
            return false;
        }
        
        try {
            LambdaUpdateWrapper<UnifiedChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UnifiedChatMessage::getMessageId, messageId);
            updateWrapper.set(UnifiedChatMessage::getIsRead, true);
            updateWrapper.set(UnifiedChatMessage::getReadTime, LocalDateTime.now());
            
            int result = messageDao.update(null, updateWrapper);
            
            if (result > 0) {
                log.info("标记消息已读成功，messageId: {}", messageId);
                return true;
            } else {
                log.warn("标记消息已读失败，消息不存在，messageId: {}", messageId);
                return false;
            }
        } catch (Exception e) {
            log.error("标记消息已读异常，messageId: {}", messageId, e);
            return false;
        }
    }

    @Override
    public boolean batchDeleteMessages(List<String> messageIds) {
        log.info("批量删除消息，messageIds: {}", messageIds);
        
        if (CollectionUtils.isEmpty(messageIds)) {
            log.warn("消息ID列表为空");
            return false;
        }
        
        try {
            LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(UnifiedChatMessage::getMessageId, messageIds);
            
            int result = messageDao.delete(queryWrapper);
            
            if (result > 0) {
                log.info("批量删除消息成功，删除数量: {}", result);
                return true;
            } else {
                log.warn("批量删除消息失败，没有找到匹配的消息");
                return false;
            }
        } catch (Exception e) {
            log.error("批量删除消息异常", e);
            return false;
        }
    }

    @Override
    public com.github.pagehelper.PageInfo<UnifiedChatSession> getHumanServiceSessions(
            Long merId, String sessionType, String status, Long userId, String sessionId, Integer page, Integer size) {
        
        log.info("获取人工客服会话列表，参数：merId={}, sessionType={}, status={}, userId={}, sessionId={}, page={}, size={}", 
                merId, sessionType, status, userId, sessionId, page, size);
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // 限制最大页面大小

        // 启动分页
        com.github.pagehelper.Page<UnifiedChatSession> startPage = PageHelper.startPage(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        
        // 商户ID过滤（必须条件）
        if (merId != null) {
            queryWrapper.eq(UnifiedChatSession::getMerId, merId);
        }
        
        // 会话类型过滤
        if (StringUtils.hasText(sessionType)) {
            queryWrapper.eq(UnifiedChatSession::getSessionType, sessionType);
        }
        
        // 会话状态过滤
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(UnifiedChatSession::getStatus, status);
        }
        
        // 用户ID过滤
        if (userId != null) {
            queryWrapper.eq(UnifiedChatSession::getUserId, userId);
        }
        
        // 会话ID过滤
        if (StringUtils.hasText(sessionId)) {
            queryWrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        }
        
        // 按更新时间倒序排列
        queryWrapper.orderByDesc(UnifiedChatSession::getUpdateTime);

        // 查询会话列表
        List<UnifiedChatSession> sessionList = sessionDao.selectList(queryWrapper);
        
        // 使用项目标准的分页工具类
        com.github.pagehelper.PageInfo<UnifiedChatSession> result = CommonPage.copyPageInfo(startPage, sessionList);
        
        log.info("获取人工客服会话列表成功，总数: {}, 当前页: {}, 页大小: {}", result.getTotal(), result.getPageNum(), result.getPageSize());
        
        return result;
    }

    @Override
    public com.github.pagehelper.PageInfo<MessageResponse> getSessionMessages(
            Long merId, String sessionId, String messageType, String role, String senderType, Integer page, Integer size) {
        
        log.info("获取指定会话的消息列表，参数：merId={}, sessionId={}, messageType={}, role={}, senderType={}, page={}, size={}", 
                merId, sessionId, messageType, role, senderType, page, size);
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // 限制最大页面大小

        // 会话ID过滤（必须条件）
        if (!StringUtils.hasText(sessionId)) {
            log.warn("会话ID为空，返回空结果");
            return new com.github.pagehelper.PageInfo<>();
        }
        
        // 验证会话是否属于该商户
        if (merId != null) {
            UnifiedChatSession session = getSession(sessionId);
            if (session == null || !merId.equals(session.getMerId())) {
                log.warn("会话不存在或不属于该商户，merId: {}, sessionId: {}", merId, sessionId);
                return new com.github.pagehelper.PageInfo<>();
            }
        }

        // 启动分页
        com.github.pagehelper.Page<UnifiedChatMessage> startPage = PageHelper.startPage(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        
        // 会话ID过滤
        queryWrapper.eq(UnifiedChatMessage::getSessionId, sessionId);
        
        // 消息类型过滤
        if (StringUtils.hasText(messageType)) {
            queryWrapper.eq(UnifiedChatMessage::getMessageType, messageType);
        }
        
        // 角色过滤
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(UnifiedChatMessage::getRole, role);
        }
        
        // 发送者类型过滤
        if (StringUtils.hasText(senderType)) {
            queryWrapper.eq(UnifiedChatMessage::getSenderType, senderType);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(UnifiedChatMessage::getCreateTime);

        // 查询消息列表
        List<UnifiedChatMessage> messageList = messageDao.selectList(queryWrapper);
        
        // 转换为MessageResponse
        List<MessageResponse> responseList = messageList.stream()
                .map(MessageResponse::fromMessage)
                .collect(Collectors.toList());
        
        // 使用项目标准的分页工具类
        com.github.pagehelper.PageInfo<MessageResponse> result = CommonPage.copyPageInfo(startPage, responseList);
        
        log.info("获取指定会话的消息列表成功，总数: {}, 当前页: {}, 页大小: {}", result.getTotal(), result.getPageNum(), result.getPageSize());
        
        return result;
    }

    @Override
    public boolean clearMessages(String sessionId) {
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatMessage::getSessionId, sessionId);
        List<UnifiedChatMessage> unifiedChatMessages = messageDao.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(unifiedChatMessages)){
            unifiedChatMessages.stream().forEach(message -> {{
                message.setIsClear(true);
                messageDao.updateById(message);
            }});
        }
        return true;
    }


    /**
     * 统计平台客服未读消息数
     * @param merId
     * @return
     */
    @Override
    public int getUnreadMessageCount(Long merId) {
        try {
            // 1. 获取所有商户的活跃会话
            LambdaQueryWrapper<UnifiedChatSession> sessionWrapper = new LambdaQueryWrapper<>();
            sessionWrapper.eq(UnifiedChatSession::getMerId, merId)
                    .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE);
            
            List<UnifiedChatSession> sessions = sessionDao.selectList(sessionWrapper);
            
            if (sessions.isEmpty()) {
                return 0;
            }
            
            // 2. 获取所有会话ID
            List<String> sessionIds = sessions.stream()
                    .map(UnifiedChatSession::getSessionId)
                    .collect(Collectors.toList());
            
            // 3. 统计未读消息数量（平台客服作为接收者且未读的消息）
            LambdaQueryWrapper<UnifiedChatMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.in(UnifiedChatMessage::getSessionId, sessionIds)
                    .eq(UnifiedChatMessage::getIsRead, false)
                    .ne(UnifiedChatMessage::getSenderType, "PLATFORM"); // 排除平台自己发送的消息
            
            int unreadCount = Math.toIntExact(messageDao.selectCount(messageWrapper));
            
            log.info("统计平台客服未读消息数，商户ID: {}, 活跃会话数: {}, 未读消息数: {}", 
                    merId, sessions.size(), unreadCount);
            
            return unreadCount;
            
        } catch (Exception e) {
            log.error("统计平台客服未读消息数失败，商户ID: {}", merId, e);
            return 0;
        }
    }

    @Override
    public int getUserUnreadMessageCount(Long userId) {
        try {
            // 1. 获取所有商户的活跃会话
            LambdaQueryWrapper<UnifiedChatSession> sessionWrapper = new LambdaQueryWrapper<>();
            sessionWrapper.eq(UnifiedChatSession::getStaffId, userId)
                    .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE);

            List<UnifiedChatSession> sessions = sessionDao.selectList(sessionWrapper);

            if (sessions.isEmpty()) {
                return 0;
            }

            // 2. 获取所有会话ID
            List<String> sessionIds = sessions.stream()
                    .map(UnifiedChatSession::getSessionId)
                    .collect(Collectors.toList());

            // 3. 统计未读消息数量（平台客服作为接收者且未读的消息）
            LambdaQueryWrapper<UnifiedChatMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.in(UnifiedChatMessage::getSessionId, sessionIds)
                    .eq(UnifiedChatMessage::getIsRead, false)
                    .ne(UnifiedChatMessage::getSenderType, "USER"); // 排除平台自己发送的消息

            int unreadCount = Math.toIntExact(messageDao.selectCount(messageWrapper));

            log.info("统计用户未读消息数，用户ID: {}, 活跃会话数: {}, 未读消息数: {}",
                    userId, sessions.size(), unreadCount);

            return unreadCount;

        } catch (Exception e) {
            log.error("统计用户客服未读消息数失败，用户ID: {}", userId, e);
            return 0;
        }
    }
}
