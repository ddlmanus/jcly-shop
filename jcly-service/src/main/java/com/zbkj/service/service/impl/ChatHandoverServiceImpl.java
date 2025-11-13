package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.service.dao.chat.UnifiedChatMessageDao;
import com.zbkj.service.dao.chat.UnifiedChatSessionDao;
import com.zbkj.service.service.ChatHandoverService;
import com.zbkj.service.service.StaffAssignmentService;
import com.zbkj.service.service.HumanServiceWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI转人工客服服务实现
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class ChatHandoverServiceImpl implements ChatHandoverService {

    @Autowired
    private UnifiedChatSessionDao unifiedChatSessionDao;

    @Autowired
    private UnifiedChatMessageDao unifiedChatMessageDao;

    @Autowired
    private StaffAssignmentService staffAssignmentService;

    @Autowired
    private HumanServiceWebSocketService humanServiceWebSocketService;

    // 需要转人工的关键词
    private static final List<String> HANDOVER_KEYWORDS = Arrays.asList(
        "人工", "客服", "转人工", "真人", "投诉", "退款", "不满意", "解决不了", 
        "没用", "不行", "差评", "举报", "维权", "法律", "起诉", "赔偿"
    );

    // 紧急关键词
    private static final List<String> URGENT_KEYWORDS = Arrays.asList(
        "紧急", "急", "马上", "立即", "投诉", "举报", "退款", "赔偿", "法律"
    );

    @Override
    public Map<String, Object> requestHandoverToHuman(String sessionId, Integer userId, String reason, String urgency) {
        try {
            log.info("用户请求转人工客服: sessionId={}, userId={}, reason={}, urgency={}", 
                    sessionId, userId, reason, urgency);

            Map<String, Object> result = new HashMap<>();

            LambdaQueryWrapper<UnifiedChatSession> ne = new LambdaQueryWrapper<>();
            ne.eq(UnifiedChatSession::getSessionId, sessionId);
            UnifiedChatSession session = unifiedChatSessionDao.selectOne(ne);
            // 1. 检查会话状态
           // UnifiedChatSession session = getSessionById(sessionId);
            if (session == null) {
                result.put("success", false);
                result.put("message", "会话不存在");
                return result;
            }

            // 2. 检查是否已经在人工模式
            if ("HUMAN".equals(session.getServiceMode())) {
                result.put("success", false);
                result.put("message", "当前已经是人工客服模式");
                result.put("currentStaffId", session.getStaffId());
                return result;
            }

            // 3. 保存转接请求消息
            saveHandoverRequestMessage(sessionId, userId, reason, urgency);

            // 4. 更新会话状态为转接中
            updateSessionStatus(sessionId, "HANDOVER_PENDING");

            // 5. 分配客服
            Map<String, Object> assignResult = staffAssignmentService.assignBestStaff(
                session.getMerId().intValue(), userId, sessionId,
                extractSkillsFromReason(reason), urgency, null);

            if ((Boolean) assignResult.get("success")) {
                Integer assignedStaffId = (Integer) assignResult.get("staffId");
                Boolean isOnline = (Boolean) assignResult.get("isOnline");
                String staffStatus = (String) assignResult.get("staffStatus");
                String assignmentReason = (String) assignResult.get("assignmentReason");
                
                // 5.1. 立即更新会话的staffId
                updateSessionStaffId(sessionId, assignedStaffId,assignmentReason);
                
                // 6. 发送转接通知给客服
                String notificationMsg = String.format("用户请求转人工客服，原因：%s，客服状态：%s", reason, staffStatus);
                sendHandoverNotification(sessionId, "HANDOVER_REQUEST", assignedStaffId, notificationMsg);

                String userMessage;
                if (Boolean.TRUE.equals(isOnline)) {
                    userMessage = "转接请求已发送，客服在线，请稍候";
                } else {
                    userMessage = "转接请求已发送给离线客服，客服上线后将优先处理您的问题";
                }

                result.put("success", true);
                result.put("message", userMessage);
                result.put("assignedStaffId", assignedStaffId);
                result.put("isOnline", isOnline);
                result.put("staffStatus", staffStatus);
                result.put("assignmentReason", assignmentReason);
                result.put("estimatedWaitTime", calculateWaitTime(urgency, isOnline));

                // 7. 发送等待消息给用户
              //  sendWaitingMessage(sessionId, userId, urgency, isOnline);

            } else {
                result.put("success", false);
                result.put("message", "暂时没有可用客服，请稍后再试");
                
                // 回退到AI模式
                updateSessionStatus(sessionId, "AI");
            }

            return result;

        } catch (Exception e) {
            log.error("请求转人工客服失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "转接请求失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> shouldHandoverToHuman(String userMessage, String sessionId, Integer userId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. 检查关键词匹配
            boolean hasHandoverKeyword = HANDOVER_KEYWORDS.stream()
                .anyMatch(keyword -> userMessage.contains(keyword));
            
            // 2. 检查紧急关键词
            boolean hasUrgentKeyword = URGENT_KEYWORDS.stream()
                .anyMatch(keyword -> userMessage.contains(keyword));
            
            // 3. 检查连续AI回复无效的情况
            boolean hasConsecutiveFailures = checkConsecutiveAiFailures(sessionId);
            
            // 4. 检查用户情绪
            boolean hasNegativeEmotion = checkNegativeEmotion(userMessage);
            
            // 5. 综合判断
            boolean shouldHandover = hasHandoverKeyword || hasConsecutiveFailures || 
                                   (hasNegativeEmotion && hasUrgentKeyword);
            
            result.put("shouldHandover", shouldHandover);
            
            if (shouldHandover) {
                String reason = "";
                String urgency = "medium";
                
                if (hasHandoverKeyword) {
                    reason = "用户明确要求转人工";
                }
                if (hasConsecutiveFailures) {
                    reason += (reason.isEmpty() ? "" : "；") + "AI连续回复无效";
                }
                if (hasNegativeEmotion) {
                    reason += (reason.isEmpty() ? "" : "；") + "用户情绪不佳";
                }
                
                if (hasUrgentKeyword) {
                    urgency = "high";
                }
                
                result.put("reason", reason);
                result.put("urgency", urgency);
                result.put("confidence", calculateHandoverConfidence(hasHandoverKeyword, hasConsecutiveFailures, hasNegativeEmotion));
            }
            
            return result;

        } catch (Exception e) {
            log.error("判断是否需要转人工失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("shouldHandover", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> assignStaff(String sessionId, Integer userId, Integer merchantId, 
                                          List<String> skillTags, String urgency) {
        return staffAssignmentService.assignBestStaff(merchantId, userId, sessionId, skillTags, urgency, null);
    }

    @Override
    public Map<String, Object> acceptHandover(String sessionId, Integer staffId) {
        try {
            log.info("客服接受转接: sessionId={}, staffId={}", sessionId, staffId);

            Map<String, Object> result = new HashMap<>();

            // 1. 检查会话状态
            UnifiedChatSession session = getSessionById(sessionId);
            if (session == null) {
                result.put("success", false);
                result.put("message", "会话不存在");
                return result;
            }

            // 2. 更新会话状态
            updateSessionServiceMode(sessionId, "HUMAN", staffId);
            updateSessionStatus(sessionId, "ACTIVE");

            // 3. 保存接受转接消息
            saveHandoverAcceptMessage(sessionId, staffId);

            // 4. 发送通知给用户
            sendHandoverNotification(sessionId, "STAFF_JOINED", session.getUserId().intValue(), 
                "客服已接入，为您提供人工服务");

            // 5. 增加客服工作负载
            staffAssignmentService.addStaffWorkload(staffId, sessionId);

            result.put("success", true);
            result.put("message", "转接成功");
            result.put("sessionId", sessionId);

            log.info("客服成功接受转接: sessionId={}, staffId={}", sessionId, staffId);
            return result;

        } catch (Exception e) {
            log.error("客服接受转接失败: sessionId={}, staffId={}, 错误: {}", sessionId, staffId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "接受转接失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> rejectHandover(String sessionId, Integer staffId, String reason) {
        try {
            log.info("客服拒绝转接: sessionId={}, staffId={}, reason={}", sessionId, staffId, reason);

            // 重新分配其他客服
            Map<String, Object> result = reassignSession(sessionId, "客服拒绝：" + reason);
            
            if (!(Boolean) result.get("success")) {
                // 如果没有其他客服，回退到AI模式
                updateSessionServiceMode(sessionId, "AI", null);
                updateSessionStatus(sessionId, "AI");
                
                // 通知用户
                UnifiedChatSession session = getSessionById(sessionId);
                if (session != null) {
                    sendHandoverNotification(sessionId, "HANDOVER_FAILED", session.getUserId().intValue(), 
                        "暂时没有可用客服，为您继续提供AI服务");
                }
            }

            return result;

        } catch (Exception e) {
            log.error("客服拒绝转接失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "拒绝转接失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> completeHandover(String sessionId, Integer staffId) {
        try {
            log.info("完成转接: sessionId={}, staffId={}", sessionId, staffId);

            // 保存转接完成消息
            saveHandoverCompleteMessage(sessionId, staffId);

            // 记录转接历史
            recordHandoverHistory(sessionId, "AI", "HUMAN", staffId, "用户转接", null);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "转接完成");
            return result;

        } catch (Exception e) {
            log.error("完成转接失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "完成转接失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> endHumanService(String sessionId, Integer staffId, String summary) {
        try {
            log.info("结束人工客服: sessionId={}, staffId={}, summary={}", sessionId, staffId, summary);

            // 1. 更新会话状态回到AI模式
            updateSessionServiceMode(sessionId, "AI", null);
            updateSessionStatus(sessionId, "AI");

            // 2. 保存结束服务消息
            saveServiceEndMessage(sessionId, staffId, summary);

            // 3. 减少客服工作负载
            staffAssignmentService.removeStaffWorkload(staffId, sessionId);

            // 4. 记录转接历史
            recordHandoverHistory(sessionId, "HUMAN", "AI", staffId, "人工服务结束", null);

            // 5. 发送通知给用户
            UnifiedChatSession session = getSessionById(sessionId);
            if (session != null) {
                sendHandoverNotification(sessionId, "SERVICE_END", session.getUserId().intValue(), 
                    "人工客服服务已结束，为您继续提供AI服务");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "人工客服服务已结束");
            return result;

        } catch (Exception e) {
            log.error("结束人工客服失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "结束服务失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> getHandoverStatus(String sessionId) {
        try {
            UnifiedChatSession session = getSessionById(sessionId);
            Map<String, Object> status = new HashMap<>();
            
            if (session != null) {
                status.put("sessionId", sessionId);
                status.put("serviceMode", session.getServiceMode());
                status.put("status", session.getStatus());
                status.put("staffId", session.getStaffId());
                status.put("createTime", session.getCreateTime());
                status.put("updateTime", session.getUpdateTime());
            } else {
                status.put("exists", false);
            }
            
            return status;

        } catch (Exception e) {
            log.error("获取转接状态失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getStaffWorkload(Integer staffId) {
        return staffAssignmentService.getStaffWorkload(staffId);
    }

    @Override
    public List<Map<String, Object>> getAvailableStaff(Integer merchantId, List<String> skillTags, String urgency) {
        return staffAssignmentService.getMerchantStaff(merchantId, true);
    }

    @Override
    public boolean updateSessionServiceMode(String sessionId, String serviceMode, Integer staffId) {
        try {
            UnifiedChatSession session = getSessionById(sessionId);
            if (session != null) {
                session.setServiceMode(serviceMode);
                session.setStaffId(staffId != null ? staffId.longValue() : null);
                session.setUpdateTime(new Date());
                unifiedChatSessionDao.updateById(session);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新会话服务模式失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void sendHandoverNotification(String sessionId, String notificationType, Integer targetUserId, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", notificationType.toLowerCase());
            notification.put("sessionId", sessionId);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            humanServiceWebSocketService.sendMessageToUser(targetUserId, notification);
            
            log.info("发送转接通知: sessionId={}, type={}, targetUserId={}", 
                    sessionId, notificationType, targetUserId);

        } catch (Exception e) {
            log.error("发送转接通知失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void recordHandoverHistory(String sessionId, String fromMode, String toMode, 
                                    Integer staffId, String reason, Long duration) {
        try {
            // 这里可以记录到专门的转接历史表
            log.info("记录转接历史: sessionId={}, from={}, to={}, staffId={}, reason={}, duration={}", 
                    sessionId, fromMode, toMode, staffId, reason, duration);
        } catch (Exception e) {
            log.error("记录转接历史失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getHandoverStatistics(Integer merchantId, Long startTime, Long endTime) {
        // 这里可以实现具体的统计逻辑
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalHandovers", 0);
        statistics.put("successfulHandovers", 0);
        statistics.put("averageWaitTime", 0);
        statistics.put("customerSatisfaction", 0);
        return statistics;
    }

    /**
     * 私有辅助方法
     */

    private UnifiedChatSession getSessionById(String sessionId) {
        try {
            LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UnifiedChatSession::getSessionId, sessionId);
            return unifiedChatSessionDao.selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("获取会话失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    private void updateSessionStatus(String sessionId, String status) {
        try {
            UnifiedChatSession session = getSessionById(sessionId);
            if (session != null) {
                session.setStatus(status);
                session.setUpdateTime(new Date());
                unifiedChatSessionDao.updateById(session);
            }
        } catch (Exception e) {
            log.error("更新会话状态失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    private void saveHandoverRequestMessage(String sessionId, Integer userId, String reason, String urgency) {
        // 转人工请求消息，接收者为客服，但此时可能还未分配，暂不设置接收者
        saveSystemMessage(sessionId, UnifiedChatMessage.MESSAGE_TYPE_HANDOVER_REQUEST, 
            "用户请求转人工客服，原因：" + reason + "，紧急程度：" + urgency, null);
    }

    private void saveHandoverAcceptMessage(String sessionId, Integer staffId) {
        UnifiedChatSession session = getSessionById(sessionId);
        Long receiverId = session != null ? session.getUserId() : null;
        saveSystemMessage(sessionId, UnifiedChatMessage.MESSAGE_TYPE_STAFF_JOIN, 
            "客服" + staffId + "已接入会话", receiverId);
    }

    private void saveHandoverCompleteMessage(String sessionId, Integer staffId) {
        UnifiedChatSession session = getSessionById(sessionId);
        Long receiverId = session != null ? session.getUserId() : null;
        saveSystemMessage(sessionId, UnifiedChatMessage.MESSAGE_TYPE_HANDOVER_COMPLETE, 
            "转接完成，客服" + staffId + "为您提供人工服务", receiverId);
    }

    private void saveServiceEndMessage(String sessionId, Integer staffId, String summary) {
        UnifiedChatSession session = getSessionById(sessionId);
        Long receiverId = session != null ? session.getUserId() : null;
        saveSystemMessage(sessionId, UnifiedChatMessage.MESSAGE_TYPE_STAFF_LEAVE, 
            "客服" + staffId + "结束服务" + (summary != null ? "，服务总结：" + summary : ""), receiverId);
    }

    private void saveSystemMessage(String sessionId, String messageType, String content) {
        saveSystemMessage(sessionId, messageType, content, null);
    }

    private void saveSystemMessage(String sessionId, String messageType, String content, Long receiverId) {
        try {
            UnifiedChatMessage message = new UnifiedChatMessage();
            message.setMessageId(UUID.randomUUID().toString());
            message.setSessionId(sessionId);
            message.setSenderType(UnifiedChatMessage.SENDER_TYPE_SYSTEM);
            message.setSenderName("系统");
            message.setRole(UnifiedChatMessage.ROLE_SYSTEM);
            
            // 设置接收者信息
            if (receiverId != null) {
                message.setReceiverId(receiverId);
                // 根据消息类型设置接收者类型
                if (messageType.contains("MERCHANT") || messageType.contains("handover")) {
                    message.setReceiverType(UnifiedChatMessage.SENDER_TYPE_STAFF);
                } else {
                    message.setReceiverType(UnifiedChatMessage.SENDER_TYPE_USER);
                }
            }
            
            message.setMessageType(messageType);
            message.setContent(content);
            message.setContentType(UnifiedChatMessage.CONTENT_TYPE_TEXT);
            message.setStatus(UnifiedChatMessage.STATUS_SENT);
            message.setIsSystemMessage(true);
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());

            unifiedChatMessageDao.insert(message);
        } catch (Exception e) {
            log.error("保存系统消息失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    private List<String> extractSkillsFromReason(String reason) {
        List<String> skills = new ArrayList<>();
        
        if (reason.contains("退款") || reason.contains("退货")) {
            skills.add("refund");
        }
        if (reason.contains("投诉") || reason.contains("举报")) {
            skills.add("complaint");
        }
        if (reason.contains("技术") || reason.contains("故障")) {
            skills.add("technical");
        }
        if (reason.contains("商品") || reason.contains("产品")) {
            skills.add("product");
        }
        
        return skills.isEmpty() ? Arrays.asList("general") : skills;
    }

    private int calculateWaitTime(String urgency) {
        return calculateWaitTime(urgency, true); // 默认按在线客服计算
    }

    private int calculateWaitTime(String urgency, Boolean isOnline) {
        int baseTime;
        switch (urgency) {
            case "high": baseTime = 30; break; // 30秒
            case "medium": baseTime = 60; break; // 1分钟  
            case "low": baseTime = 120; break; // 2分钟
            default: baseTime = 60;
        }
        
        // 离线客服等待时间更长
        if (!Boolean.TRUE.equals(isOnline)) {
            baseTime *= 3; // 离线客服等待时间为3倍
        }
        
        return baseTime;
    }

    private void sendWaitingMessage(String sessionId, Integer userId, String urgency) {
        sendWaitingMessage(sessionId, userId, urgency, true); // 默认按在线客服
    }

    private void sendWaitingMessage(String sessionId, Integer userId, String urgency, Boolean isOnline) {
        try {
            String message;
            if (Boolean.TRUE.equals(isOnline)) {
                message = "正在为您分配在线客服，请稍候...";
                if ("high".equals(urgency)) {
                    message = "您的问题已标记为紧急，正在优先为您分配在线客服...";
                }
            } else {
                message = "已为您分配离线客服，客服上线后将优先处理您的问题";
                if ("high".equals(urgency)) {
                    message = "您的问题已标记为紧急，已分配离线客服，客服上线后将立即为您处理";
                }
            }
            
            Map<String, Object> waitingMsg = new HashMap<>();
            waitingMsg.put("type", "system_notice");
            waitingMsg.put("content", message);
            waitingMsg.put("sessionId", sessionId);
            waitingMsg.put("isOnline", isOnline);
            waitingMsg.put("staffStatus", Boolean.TRUE.equals(isOnline) ? "在线" : "离线");
            waitingMsg.put("timestamp", System.currentTimeMillis());

            humanServiceWebSocketService.sendMessageToUser(userId, waitingMsg);
        } catch (Exception e) {
            log.error("发送等待消息失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    private boolean checkConsecutiveAiFailures(String sessionId) {
        // 这里可以检查最近的AI回复是否连续无效
        // 暂时返回false
        return false;
    }

    private boolean checkNegativeEmotion(String userMessage) {
        List<String> negativeWords = Arrays.asList("不行", "垃圾", "差", "烂", "不满意", "生气", "愤怒");
        return negativeWords.stream().anyMatch(userMessage::contains);
    }

    private double calculateHandoverConfidence(boolean hasKeyword, boolean hasFailures, boolean hasEmotion) {
        double confidence = 0.0;
        if (hasKeyword) confidence += 0.8;
        if (hasFailures) confidence += 0.6;
        if (hasEmotion) confidence += 0.4;
        return Math.min(confidence, 1.0);
    }

    private Map<String, Object> reassignSession(String sessionId, String reason) {
        try {
            UnifiedChatSession session = getSessionById(sessionId);
            if (session == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "会话不存在");
                return result;
            }

            return staffAssignmentService.assignBestStaff(
                session.getMerId().intValue(), session.getUserId().intValue(), sessionId,
                Arrays.asList("general"), "medium", null);

        } catch (Exception e) {
            log.error("重新分配会话失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "重新分配失败: " + e.getMessage());
            return result;
        }
    }

    private void updateSessionStaffId(String sessionId, Integer staffId,String assignmentReason) {
        try {
            UnifiedChatSession session = getSessionById(sessionId);
            if (session != null) {
                session.setStaffId(staffId != null ? staffId.longValue() : null);
                session.setUpdateTime(new Date());
                session.setSessionType(UnifiedChatSession.SESSION_TYPE_HUMAN);
                session.setCurrentServiceType(UnifiedChatSession.SERVICE_TYPE_HUMAN);
                session.setStatus(UnifiedChatSession.STATUS_ACTIVE);
                session.setTransferReason(assignmentReason);
                session.setUserType(UnifiedChatSession.USER_TYPE_CUSTOMER);
                session.setPriority(UnifiedChatSession.PRIORITY_NORMAL);
                session.setWaitStartTime(new Date());
                unifiedChatSessionDao.updateById(session);
                log.info("更新会话staffId成功: sessionId={}, staffId={}", sessionId, staffId);
            }
        } catch (Exception e) {
            log.error("更新会话staffId失败: sessionId={}, staffId={}, 错误: {}", sessionId, staffId, e.getMessage(), e);
        }
    }
}
