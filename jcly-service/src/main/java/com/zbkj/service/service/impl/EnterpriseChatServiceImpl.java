package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.coze.EnterpriseChatMessage;
import com.zbkj.common.model.coze.EnterpriseChatSession;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.EnterpriseChatMessageRequest;
import com.zbkj.common.request.EnterpriseChatSessionRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.vo.LoginFrontUserVo;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.dao.EnterpriseChatMessageDao;
import com.zbkj.service.dao.EnterpriseChatSessionDao;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.EnterpriseChatService;
import com.zbkj.service.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业聊天服务实现类
 * 
 * 注意：核心消息和会话管理已迁移到UnifiedChatService
 * 此类保留向后兼容的接口，并提供格式转换功能
 */
@Slf4j
@Service
public class EnterpriseChatServiceImpl implements EnterpriseChatService {

    @Autowired
    private com.zbkj.service.service.UnifiedChatService unifiedChatService;

    // ===================== 前端实际使用的方法 =====================

    /**
     * 获取会话列表 - 前端使用：getEnterpriseChatSessionsApi
     */
    @Override
    public CommonPage<EnterpriseChatSession> getSessionList(String cozeBotId, Integer status, PageParamRequest pageParamRequest) {
        try {
            // 获取当前商户信息
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId().longValue();
            
            // 从统一聊天服务获取会话列表
            List<com.zbkj.common.model.chat.UnifiedChatSession> unifiedSessions = 
                unifiedChatService.getUserActiveSessions(merId, "MERCHANT", merId);
            
            // 根据条件过滤
            if (cozeBotId != null || status != null) {
                unifiedSessions = unifiedSessions.stream()
                    .filter(session -> (cozeBotId == null || cozeBotId.equals(session.getCozeBotId())))
                    .filter(session -> (status == null || status.equals(session.getStatus())))
                    .collect(Collectors.toList());
            }
            
            // 转换为 EnterpriseChatSession 格式
            List<EnterpriseChatSession> enterpriseSessions = unifiedSessions.stream()
                .map(this::convertToEnterpriseChatSession)
                .collect(Collectors.toList());
            
            // 手动分页
            int start = (pageParamRequest.getPage() - 1) * pageParamRequest.getLimit();
            int end = Math.min(start + pageParamRequest.getLimit(), enterpriseSessions.size());
            List<EnterpriseChatSession> pagedSessions = enterpriseSessions.subList(start, end);
            
            return CommonPage.restPage(pagedSessions);
        } catch (Exception e) {
            log.error("获取企业聊天会话列表失败: {}", e.getMessage(), e);
            throw new CrmebException("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建会话 - 前端使用：createEnterpriseChatSessionApi
     */
    @Override
    public EnterpriseChatSession createSession(EnterpriseChatSessionRequest request) {
        try {
            // 获取当前商户信息
            com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();
            
            // 使用统一聊天服务创建会话
            com.zbkj.common.model.chat.UnifiedChatSession unifiedSession = unifiedChatService.createOrGetSession(
                merId.longValue(), // 商户端用户ID = 商户ID
                "MERCHANT", // 商户用户类型
                merId.longValue(),
                "AI", // AI会话类型
                request.getCozeBotId()
            );
            
            // 转换为 EnterpriseChatSession 格式
            return convertToEnterpriseChatSession(unifiedSession);
        } catch (Exception e) {
            log.error("创建企业聊天会话失败: {}", e.getMessage(), e);
            throw new CrmebException("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 清空会话历史 - 前端使用：clearChatHistoryApi
     */
    @Override
    public void clearSessionHistory(String sessionId) {
        try {
            // 实际上是结束当前会话，用户需要创建新会话来继续聊天
            unifiedChatService.endSession(sessionId, "用户清空历史");
            log.info("会话历史已清空，会话ID: {}", sessionId);
        } catch (Exception e) {
            log.error("清空会话历史失败: {}", e.getMessage(), e);
            throw new CrmebException("清空会话历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话详情
     */
    @Override
    public EnterpriseChatSession getSessionDetail(String sessionId) {
        try {
            com.zbkj.common.model.chat.UnifiedChatSession unifiedSession = unifiedChatService.getSession(sessionId);
            if (unifiedSession == null) {
                throw new CrmebException("会话不存在");
            }
            return convertToEnterpriseChatSession(unifiedSession);
        } catch (Exception e) {
            log.error("获取会话详情失败: {}", e.getMessage(), e);
            throw new CrmebException("获取会话详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新会话状态
     */
    @Override
    public EnterpriseChatSession updateSession(String sessionId, EnterpriseChatSessionRequest request) {
        try {
            // EnterpriseChatSessionRequest 没有status字段，这里只是更新基本信息
            // 状态更新需要通过其他方法
            log.info("更新会话信息，会话ID: {}", sessionId);
            
            // 返回当前会话（实际上没有更新操作）
            return getSessionDetail(sessionId);
        } catch (Exception e) {
            log.error("更新会话失败: {}", e.getMessage(), e);
            throw new CrmebException("更新会话失败：" + e.getMessage());
        }
    }

    /**
     * 删除会话（实际是结束会话）
     */
    @Override
    public void deleteSession(String sessionId) {
        try {
            unifiedChatService.endSession(sessionId, "用户删除");
        } catch (Exception e) {
            log.error("删除会话失败: {}", e.getMessage(), e);
            throw new CrmebException("删除会话失败：" + e.getMessage());
        }
    }

    // ===================== 提供默认配置的方法 =====================

    /**
     * 获取聊天配置
     */
    @Override
    public Map<String, Object> getChatConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxMessageLength", 2000);
        config.put("enableFileUpload", true);
        config.put("enableImageUpload", true);
        config.put("autoReply", false);
        config.put("streamResponse", true);
        return config;
    }

    /**
     * 获取聊天统计
     */
    @Override
    public Map<String, Object> getChatStatistics(String startDate, String endDate, String dimension) {
        // 统计功能暂不实现，返回空数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", 0);
        stats.put("totalMessages", 0);
        stats.put("avgResponseTime", 0);
        stats.put("period", startDate + " ~ " + endDate);
        return stats;
    }

    /**
     * 获取热门话题
     */
    @Override
    public List<Map<String, Object>> getHotTopics(Integer limit) {
        // 热门话题功能暂不实现
        return new java.util.ArrayList<>();
    }

    // ===================== 废弃的方法（标记@Deprecated） =====================

    /**
     * @deprecated 此方法已废弃，请使用 UnifiedChatService.getSessionMessages()
     */
    @Override
    @Deprecated
    public CommonPage<EnterpriseChatMessage> getMessageList(String sessionId, String startTime, String endTime, PageParamRequest pageParamRequest) {
        throw new RuntimeException("此方法已废弃，请使用 UnifiedChatService.getSessionMessages()");
    }

    /**
     * @deprecated 此方法已废弃，请使用 UnifiedChatService.sendMessage()
     */
    @Override
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sendMessage(EnterpriseChatMessageRequest request) {
        throw new RuntimeException("此方法已废弃，请使用 UnifiedChatService.sendMessage()");
    }

    // ===================== 不需要的方法（直接抛出异常或返回空） =====================

    @Override
    public EnterpriseChatSession createSession(EnterpriseChatSessionRequest request, LoginUserVo user) {
        // 与上面方法功能相同
        return createSession(request);
    }

    @Override
    public EnterpriseChatSession getSessionDetail(String sessionId, LoginUserVo user) {
        // 与上面方法功能相同
        return getSessionDetail(sessionId);
    }

    @Override
    public EnterpriseChatSession getFrontSessionDetail(String sessionId, LoginFrontUserVo user) {
        // 与上面方法功能相同
        return getSessionDetail(sessionId);
    }

    @Override
    public EnterpriseChatMessage getMessageDetail(String messageId) {
        log.warn("getMessageDetail方法暂未实现，建议通过getSessionMessages获取消息");
        return null;
    }

    @Override
    public void deleteMessage(String messageId) {
        log.warn("deleteMessage方法不支持，建议保持消息完整性");
    }

    @Override
    public Map<String, Object> resendMessage(String messageId) {
        log.warn("resendMessage方法已废弃");
        return new HashMap<>();
    }

    @Override
    public EnterpriseChatMessage saveMessage(EnterpriseChatMessage message) {
        log.warn("saveMessage方法已废弃，消息保存应该通过sendMessage完成");
        return message;
    }

    @Override
    public List<Map<String, Object>> buildContextMessages(String sessionId, Integer limit) {
        log.warn("buildContextMessages方法已废弃");
        return new java.util.ArrayList<>();
    }

    @Override
    public EnterpriseChatMessage processCozeResponse(String sessionId, Object response, String messageId) {
        log.warn("processCozeResponse方法已废弃");
        return null;
    }

    @Override
    public EnterpriseChatMessage processStreamResponse(String sessionId, com.zbkj.common.model.coze.stream.CozeStreamResponse response, String messageId) {
        log.warn("processStreamResponse方法已废弃");
        return null;
    }

    @Override
    public EnterpriseChatSession getOrCreateUserSession(String cozeBotId) {
        // 这个方法实际上和createSession功能类似
        EnterpriseChatSessionRequest request = new EnterpriseChatSessionRequest();
        request.setCozeBotId(cozeBotId);
        return createSession(request);
    }

    @Override
    public void updateChatConfig(Map<String, Object> config) {
        log.warn("updateChatConfig方法暂不实现");
    }

    @Override
    public String exportChatHistory(String sessionId, String format) {
        throw new RuntimeException("导出聊天记录功能暂未实现");
    }

    // ===================== 格式转换方法 =====================

    /**
     * 转换UnifiedChatSession为EnterpriseChatSession
     */
    private EnterpriseChatSession convertToEnterpriseChatSession(com.zbkj.common.model.chat.UnifiedChatSession unifiedSession) {
        EnterpriseChatSession session = new EnterpriseChatSession();
        session.setSessionId(unifiedSession.getSessionId());
        session.setMerId(unifiedSession.getMerId());
        session.setUserId(unifiedSession.getUserId());
        session.setCozeBotId(unifiedSession.getCozeBotId());
        session.setSessionTitle(unifiedSession.getSessionTitle());
        
        // 状态转换：UnifiedChatSession使用String，EnterpriseChatSession使用Integer
        if ("ACTIVE".equals(unifiedSession.getStatus())) {
            session.setStatus(EnterpriseChatSession.STATUS_ACTIVE);
        } else if ("ENDED".equals(unifiedSession.getStatus())) {
            session.setStatus(EnterpriseChatSession.STATUS_ENDED);
        } else {
            session.setStatus(EnterpriseChatSession.STATUS_ACTIVE); // 默认值
        }
        
        session.setTotalMessages(unifiedSession.getTotalMessages());
        session.setCreateTime(unifiedSession.getCreateTime());
        session.setUpdateTime(unifiedSession.getUpdateTime());
        session.setLastMessageTime(unifiedSession.getLastMessageTime());
        session.setLastMessageContent(unifiedSession.getLastMessageContent());
        
        return session;
    }
}