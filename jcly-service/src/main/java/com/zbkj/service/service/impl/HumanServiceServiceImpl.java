package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.*;
import com.zbkj.common.request.chat.SendMessageRequest;
import com.zbkj.common.response.*;
import com.zbkj.common.model.service.*;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.response.chat.MessageResponse;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.vo.FileResultVo;
import com.zbkj.common.vo.LoginFrontUserVo;
import com.zbkj.service.dao.*;
import com.zbkj.service.dao.chat.UnifiedChatMessageDao;
import com.zbkj.service.dao.chat.UnifiedChatSessionDao;
import com.zbkj.service.service.*;
import com.zbkj.service.service.UnifiedMessageRoutingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 人工客服服务实现
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class HumanServiceServiceImpl implements HumanServiceService {

    @Autowired
    private HumanServiceSessionDao humanServiceSessionDao;
    
    // 统一会话表DAO，用于人工客服会话管理
    @Autowired
    private UnifiedChatSessionDao unifiedChatSessionDao;

    @Autowired
    private HumanServiceMessageDao humanServiceMessageDao;

    @Autowired
    private CustomerServiceStaffDao customerServiceStaffDao;

    @Autowired
    private QuickReplyTemplateDao quickReplyTemplateDao;

    @Autowired
    private CustomerServiceConfigDao customerServiceConfigDao;


    @Autowired
    private CustomerServiceStaffService customerServiceStaffService;

    @Autowired
    private UnifiedMessageRoutingService messageRoutingService;
    
    @Autowired
    private HumanServiceWebSocketService humanServiceWebSocketService;

    @Autowired
    private ChatHandoverService chatHandoverService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private EnterpriseChatService enterpriseChatService;

    @Autowired
    private HumanServiceRatingService humanServiceRatingService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MerchantDao merchantDao;

    @Autowired
    private UploadService uploadService;
    @Autowired
    protected CozeBotService cozeBotService;
    @Autowired
    private CozeStreamClient cozeStreamClient;
    @Autowired
    private UnifiedChatService unifiedChatService;
    @Autowired
    private CozeService cozeService;
    @Autowired
    private UnifiedChatMessageDao unifiedChatMessageDao;

    @Autowired
    private StaffAssignmentService staffAssignmentService;

    // 转人工关键词模式
    private static final List<Pattern> TRANSFER_KEYWORDS = Arrays.asList(
        Pattern.compile(".*转人工.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*人工客服.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*联系客服.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*真人服务.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*手动服务.*", Pattern.CASE_INSENSITIVE)
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> transferToHuman(TransferToHumanRequest request) {
        log.info("开始转接人工客服，会话ID: {}", request.getEnterpriseSessionId());

        // 1. 验证统一聊天会话是否存在
        UnifiedChatSession unifiedSession = unifiedChatService.getSession(request.getEnterpriseSessionId());
        if (unifiedSession == null) {
            throw new CrmebException("聊天会话不存在");
        }

        // 2. 检查是否已经转接过 - 使用统一会话表
        LambdaQueryWrapper<UnifiedChatSession> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(UnifiedChatSession::getSessionId, request.getEnterpriseSessionId())
                      .eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);
        UnifiedChatSession existingSession = unifiedChatSessionDao.selectOne(existingWrapper);
        if (existingSession != null && !UnifiedChatSession.STATUS_ENDED.equals(existingSession.getStatus())) {
            throw new CrmebException("该会话已经转接给人工客服");
        }

        // 3. 分配客服
        CustomerServiceStaff assignedStaff = null;
        if (request.getAssignedStaffId() != null) {
            assignedStaff = customerServiceStaffDao.selectByAdminId(request.getAssignedStaffId());
            if (assignedStaff == null || !customerServiceStaffService.isStaffAvailable(request.getAssignedStaffId())) {
                log.warn("指定的客服不可用，将进行自动分配");
                assignedStaff = null;
            }
        }

        if (assignedStaff == null) {
            // 使用新的智能分配服务
            Map<String, Object> assignmentResult = staffAssignmentService.assignBestStaff(
                request.getMerId(), 
                request.getUserId(),
                request.getEnterpriseSessionId(),
                request.getRequiredSkills() != null ? 
                    Arrays.asList(request.getRequiredSkills().split(",")) : Arrays.asList("general"),
                request.getPriority() != null ? request.getPriority() : "NORMAL",
                request.getAssignedStaffId()
            );
            
            if ((Boolean) assignmentResult.get("success")) {
                Integer staffId = (Integer) assignmentResult.get("staffId");
                String assignmentReason = (String) assignmentResult.get("assignmentReason");
                
                // 根据staffId查找对应的CustomerServiceStaff记录
                assignedStaff = customerServiceStaffDao.selectByAdminId(staffId);
                
                log.info("智能分配客服成功: staffId={}, reason={}", staffId, assignmentReason);
            } else {
                String message = (String) assignmentResult.get("message");
                log.warn("智能分配客服失败: {}", message);
            }
        }

        // 4. 创建统一会话或更新现有会话为人工客服模式
        UnifiedChatSession session;
        LambdaQueryWrapper<UnifiedChatSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(UnifiedChatSession::getSessionId, request.getEnterpriseSessionId());
        session = unifiedChatSessionDao.selectOne(sessionWrapper);
        
        if (session == null) {
            // 创建新的统一会话
            session = new UnifiedChatSession();
            session.setSessionId(request.getEnterpriseSessionId());
            session.setUserId(request.getUserId().longValue());
            session.setUserType(request.getUserType());
            session.setMerId(request.getMerId().longValue());
            session.setSessionType(UnifiedChatSession.SESSION_TYPE_MIXED); // 混合模式（从AI转人工）
            session.setCurrentServiceType(UnifiedChatSession.SERVICE_TYPE_HUMAN); // 当前为人工服务
            session.setTransferReason(request.getTransferReason());
            session.setPriority(request.getPriority());
            session.setWaitStartTime(new Date());
            session.setCreateTime(new Date());
        } else {
            // 更新现有会话为人工服务模式
            session.setSessionType(UnifiedChatSession.SESSION_TYPE_MIXED); // 从AI转为混合模式
            session.setCurrentServiceType(UnifiedChatSession.SERVICE_TYPE_HUMAN); // 切换到人工服务
            session.setTransferReason(request.getTransferReason());
            session.setPriority(request.getPriority());
            session.setWaitStartTime(new Date());
        }

        if (assignedStaff != null) {
            session.setStaffId(assignedStaff.getId().longValue());
            session.setStatus(UnifiedChatSession.STATUS_WAITING);
            session.setQueuePosition(0);
        } else {
            session.setStatus(UnifiedChatSession.STATUS_WAITING);
            // 计算排队位置
            LambdaQueryWrapper<UnifiedChatSession> queueWrapper = new LambdaQueryWrapper<>();
            queueWrapper.eq(UnifiedChatSession::getMerId, request.getMerId())
                       .eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_WAITING)
                       .eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);
            Integer queuePosition = Math.toIntExact(unifiedChatSessionDao.selectCount(queueWrapper)) + 1;
            session.setQueuePosition(queuePosition);
        }

        session.setUpdateTime(new Date());

        int result;
        if (session.getId() == null) {
            result = unifiedChatSessionDao.insert(session);
        } else {
            result = unifiedChatSessionDao.updateById(session);
        }
        if (result <= 0) {
            throw new CrmebException("创建或更新统一会话失败");
        }

        // 5. 发送系统消息并处理跨端通知
        String systemMessage;
        if (assignedStaff != null) {
            systemMessage = String.format("您的对话已转接到人工客服，客服工号：%s，请稍等...", assignedStaff.getStaffNo());
            
            // 使用统一消息路由服务处理转接通知
            messageRoutingService.handleTransferToHumanNotification(
                session.getSessionId(), 
                request.getUserId(), 
                assignedStaff.getId(), 
                null
            );
            
            // 更新客服当前会话数
            customerServiceStaffService.updateCurrentSessions(assignedStaff.getId(), 1);
        } else {
            systemMessage = String.format("您的对话已转接到人工客服，当前排队位置：第%d位，请耐心等待...", session.getQueuePosition());
            
            // 使用统一消息路由服务处理排队通知
            messageRoutingService.handleTransferToHumanNotification(
                session.getSessionId(), 
                request.getUserId(), 
                null, 
                session.getQueuePosition()
            );
        }

        sendSystemMessage(session.getSessionId(), systemMessage);

        // 6. 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sessionId", session.getSessionId());
        resultMap.put("status", session.getStatus());
        resultMap.put("queuePosition", session.getQueuePosition());
        resultMap.put("assignedStaff", assignedStaff);
        resultMap.put("systemMessage", systemMessage);

        log.info("人工客服转接完成，会话ID: {}, 分配客服: {}", session.getSessionId(), 
                assignedStaff != null ? assignedStaff.getStaffNo() : "排队中");

        return resultMap;
    }
    @Override
    public HumanServiceSession getSessionDetail(String sessionId) {
        LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        wrapper.eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);

        UnifiedChatSession session = unifiedChatSessionDao.selectOne(wrapper);

        // 会话隔离：验证当前用户是否有权限访问此会话
        if (session != null && !hasUnifiedSessionAccess(session)) {
            throw new CrmebException("无权限访问该会话");
        }

        return session != null ? convertToHumanServiceSession(session) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptSession(String sessionId) {
        LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        wrapper.eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);

        UnifiedChatSession session = unifiedChatSessionDao.selectOne(wrapper);
        if (session == null) {
            throw new CrmebException("会话不存在");
        }

        if (!UnifiedChatSession.STATUS_WAITING.equals(session.getStatus())) {
            throw new CrmebException("会话状态不正确，无法接受");
        }

        // 更新会话状态
        session.setStatus(UnifiedChatSession.STATUS_ACTIVE);
        session.setServiceStartTime(new Date());
        session.setQueuePosition(0);
        
        // 计算等待时间
        if (session.getWaitStartTime() != null) {
            long waitTime = (System.currentTimeMillis() - session.getWaitStartTime().getTime()) / 1000;
            session.setTotalWaitTime((int) waitTime);
        }

        session.setUpdateTime(new Date());
        unifiedChatSessionDao.updateById(session);

        // 发送欢迎消息
        CustomerServiceStaff staff = customerServiceStaffDao.selectById(session.getStaffId());
        if (staff != null) {
            String welcomeMessage = String.format("您好！我是客服工号%s，很高兴为您服务！请问有什么可以帮助您的吗？", staff.getStaffNo());
            sendSystemMessage(sessionId, welcomeMessage);
        }

        log.info("客服接受会话成功，会话ID: {}, 客服ID: {}", sessionId, session.getStaffId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endSession(String sessionId) {
        LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnifiedChatSession::getSessionId, sessionId);
        UnifiedChatSession session = unifiedChatSessionDao.selectOne(wrapper);
        if (session == null) {
            throw new CrmebException("会话不存在");
        }

        // 更新会话状态
        session.setStatus(UnifiedChatSession.STATUS_ENDED);
        session.setServiceEndTime(new Date());
        
        // 计算服务时间
        if (session.getServiceStartTime() != null) {
            long serviceTime = (System.currentTimeMillis() - session.getServiceStartTime().getTime()) / 1000;
            session.setTotalServiceTime((int) serviceTime);
        }

        session.setUpdateTime(new Date());
        unifiedChatSessionDao.updateById(session);

        // 更新客服当前会话数
        if (session.getStaffId() != null) {
            customerServiceStaffService.updateCurrentSessions(session.getStaffId().intValue(), -1);
        }

        // 发送结束消息
        sendSystemMessage(sessionId, "本次客服服务已结束，感谢您的咨询！");

        log.info("会话结束，会话ID: {}", sessionId);
    }

    @Override
    public Map<String, Object> getWorkplaceData() {
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        
        Map<String, Object> workplaceData = new HashMap<>();
        
        if ("MERCHANT".equals(currentUserType)) {
            // 获取客服相关数据
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(currentUserId);
            workplaceData.put("staffInfo", staff);
            
            // 获取当前会话列表
            List<HumanServiceSession> activeSessions = getActiveSessionsByStaff(currentUserId);
            workplaceData.put("activeSessions", activeSessions);
            
            // 获取等待中的会话数
            Integer waitingCount = getWaitingSessionCount();
            workplaceData.put("waitingCount", waitingCount);
            
            // 获取今日统计
            Map<String, Object> todayStats = getTodayStatistics(currentUserId);
            workplaceData.put("todayStats", todayStats);
            
        } else {
            // 普通用户数据
            workplaceData.put("onlineStaffCount", customerServiceStaffService.getOnlineStaffCount());
        }
        
        return workplaceData;
    }

    @Override
    public List<QuickReplyTemplate> getQuickReplyTemplates(String category) {
        LambdaQueryWrapper<QuickReplyTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), QuickReplyTemplate::getCategory, category);
        wrapper.eq(QuickReplyTemplate::getStatus, true);
        wrapper.orderByAsc(QuickReplyTemplate::getSortOrder);
        
        return quickReplyTemplateDao.selectList(wrapper);
    }

    @Override
    public com.github.pagehelper.PageInfo<QuickReplyTemplate> getQuickReplyTemplatesWithPage(String category, String keyword, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100; // 限制最大页面大小

        // 使用 PageHelper 进行分页
        com.github.pagehelper.PageHelper.startPage(page, size);

        LambdaQueryWrapper<QuickReplyTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), QuickReplyTemplate::getCategory, category);
        wrapper.eq(QuickReplyTemplate::getStatus, true);
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(QuickReplyTemplate::getTitle, keyword)
                .or()
                .like(QuickReplyTemplate::getContent, keyword)
            );
        }
        
        wrapper.orderByAsc(QuickReplyTemplate::getSortOrder);
        
        List<QuickReplyTemplate> list = quickReplyTemplateDao.selectList(wrapper);
        return new com.github.pagehelper.PageInfo<>(list);
    }

    @Override
    public Map<String, Object> getServiceStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取会话统计
        List<Map<String, Object>> sessionStats = humanServiceSessionDao.getStaffSessionStatistics(
           getCurrentMerId(), startDate, endDate);
        statistics.put("sessionStats", sessionStats);
        
        // 获取排队统计
        Map<String, Object> queueStats = humanServiceSessionDao.getQueueStatistics(getCurrentMerId());
        statistics.put("queueStats", queueStats);
        
        return statistics;
    }

    @Override
    public CustomerServiceStaff assignStaff(Integer merId, String serviceLevel, String requiredSkills) {
        // 获取可用的客服列表
        LambdaQueryWrapper<CustomerServiceStaff> staffWrapper = new LambdaQueryWrapper<>();
        staffWrapper.eq(CustomerServiceStaff::getMerId, merId);
        staffWrapper.eq(CustomerServiceStaff::getStatus, true);
        staffWrapper.eq(CustomerServiceStaff::getOnlineStatus, CustomerServiceStaff.ONLINE_STATUS_ONLINE);
        if (StringUtils.hasText(serviceLevel)) {
            staffWrapper.eq(CustomerServiceStaff::getServiceLevel, serviceLevel);
        }
        List<CustomerServiceStaff> availableStaff = customerServiceStaffDao.selectList(staffWrapper);
        
        if (availableStaff.isEmpty()) {
            log.warn("没有可用的客服，商户ID: {}, 服务等级: {}", merId, serviceLevel);
            return null;
        }
        
        // 客服分配算法：负载均衡 + 技能匹配
        CustomerServiceStaff bestStaff = availableStaff.stream()
            .filter(staff -> staff.getCurrentSessions() < staff.getMaxConcurrentSessions())
            .filter(staff -> CustomerServiceStaff.ONLINE_STATUS_ONLINE.equals(staff.getOnlineStatus()))
            .min(Comparator.comparing(CustomerServiceStaff::getCurrentSessions))
            .orElse(null);
        
        if (bestStaff != null) {
            log.info("分配客服成功，客服ID: {}, 工号: {}", bestStaff.getId(), bestStaff.getStaffNo());
        }
        
        return bestStaff;
    }

    @Override
    public boolean shouldTransferToHuman(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        
        return TRANSFER_KEYWORDS.stream()
            .anyMatch(pattern -> pattern.matcher(content).matches());
    }

    @Override
    public void sendSystemMessage(String sessionId, String content) {
        HumanServiceMessage systemMessage = new HumanServiceMessage();
        systemMessage.setMessageId(CrmebUtil.getUuid());
        systemMessage.setSessionId(sessionId);
        systemMessage.setSenderId(0L); // 系统消息发送者ID为0
        systemMessage.setSenderType(HumanServiceMessage.SENDER_TYPE_SYSTEM);
        systemMessage.setMessageType(HumanServiceMessage.MESSAGE_TYPE_TEXT);
        systemMessage.setContent(content);
        systemMessage.setContentFormat(HumanServiceMessage.CONTENT_FORMAT_TEXT);
        systemMessage.setIsRead(false);
        systemMessage.setIsSystemMessage(true);
        systemMessage.setStatus(HumanServiceMessage.STATUS_SENT);
        systemMessage.setCreateTime(new Date());
        systemMessage.setUpdateTime(new Date());
        
        humanServiceMessageDao.insert(systemMessage);
        
        log.info("发送系统消息，会话ID: {}, 内容: {}", sessionId, content);
    }

    @Override
    public void updateSessionPriority(String sessionId, String priority) {
        HumanServiceSession session = getSessionDetail(sessionId);
        if (session != null) {
            session.setPriority(priority);
            session.setUpdateTime(new Date());
            humanServiceSessionDao.updateById(session);
        }
    }

    @Override
    public Map<String, Object> getQueueInfo(Integer merId) {
        // 查询等待中的会话 - 使用统一会话表
        LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnifiedChatSession::getMerId, merId);
        wrapper.eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_WAITING);
        wrapper.eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);
        List<UnifiedChatSession> waitingSessions = unifiedChatSessionDao.selectList(wrapper);
        
        Map<String, Object> queueInfo = new HashMap<>();
        queueInfo.put("totalWaiting", waitingSessions.size());
        queueInfo.put("averageWaitTime", calculateAverageWaitTimeFromUnified(waitingSessions));
        queueInfo.put("onlineStaffCount", customerServiceStaffService.getOnlineStaffCount());
        
        return queueInfo;
    }

    // 私有辅助方法
    
    private Integer getCurrentUserId() {
        // 从Spring Security上下文获取当前用户ID
        return SecurityUtil.getLoginUserVo().getUser().getId();
    }
    
    private String getCurrentUserType() {
        // 从Spring Security上下文获取当前用户类型
        return "MERCHANT"; // 商户端默认为客服类型
    }
    
    private List<HumanServiceSession> getActiveSessionsByStaff(Integer staffId) {
        LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnifiedChatSession::getStaffId, staffId);
        wrapper.eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_ACTIVE);
        wrapper.eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);
        
        List<UnifiedChatSession> unifiedSessions = unifiedChatSessionDao.selectList(wrapper);
        return unifiedSessions.stream()
                .map(this::convertToHumanServiceSession)
                .collect(Collectors.toList());
    }
    
    private Integer getWaitingSessionCount() {
        LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_WAITING);
        wrapper.eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);
        return Math.toIntExact(unifiedChatSessionDao.selectCount(wrapper));
    }
    
    private Map<String, Object> getTodayStatistics(Integer staffId) {
        // 实现今日统计逻辑
        Map<String, Object> stats = new HashMap<>();
        stats.put("todaySessionCount", 0);
        stats.put("averageResponseTime", 0);
        stats.put("satisfactionRating", 0.0);
        return stats;
    }
    
    private long calculateAverageWaitTime(List<HumanServiceSession> waitingSessions) {
        if (waitingSessions.isEmpty()) {
            return 0;
        }
        
        long now = System.currentTimeMillis();
        long totalWaitTime = waitingSessions.stream()
            .filter(session -> session.getWaitStartTime() != null)
            .mapToLong(session -> now - session.getWaitStartTime().getTime())
            .sum();
        
        return totalWaitTime / waitingSessions.size() / 1000; // 返回秒数
    }
    
    private long calculateAverageWaitTimeFromUnified(List<UnifiedChatSession> waitingSessions) {
        if (waitingSessions.isEmpty()) {
            return 0;
        }
        
        long now = System.currentTimeMillis();
        long totalWaitTime = waitingSessions.stream()
            .filter(session -> session.getWaitStartTime() != null)
            .mapToLong(session -> now - session.getWaitStartTime().getTime())
            .sum();
        
        return totalWaitTime / waitingSessions.size() / 1000; // 返回秒数
    }
    
    /**
     * 根据用户ID获取客服信息
     */
    private CustomerServiceStaff getStaffByUserId(Integer userId) {
        LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceStaff::getAdminId, userId);
        wrapper.eq(CustomerServiceStaff::getStatus, true);
        return customerServiceStaffDao.selectOne(wrapper);
    }
    
    /**
     * 检查当前用户是否是管理员角色
     */
    private boolean isManagerRole() {
        // 检查当前用户的角色权限
        // 这里可以根据具体的权限设计来实现
        // 比如检查是否有人工客服管理权限
        try {
            String roles = SecurityUtil.getLoginUserVo().getUser().getRoles();
            return roles != null && (roles.contains("MANAGER") || roles.contains("ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查当前用户是否有权限访问指定会话
     */
    private boolean hasSessionAccess(HumanServiceSession session) {
        // 1. 商户隔离：只能访问自己商户的会话
        Integer currentMerId = getCurrentMerId();
        if (!currentMerId.equals(session.getMerId())) {
            return false;
        }
        
        // 2. 如果是管理员，可以访问所有会话
        if (isManagerRole()) {
            return true;
        }
        
        // 3. 普通客服只能访问分配给自己的会话
        Integer currentUserId = getCurrentUserId();
        CustomerServiceStaff currentStaff = getStaffByUserId(currentUserId);
        if (currentStaff != null && session.getStaffId() != null) {
            return currentStaff.getId().equals(session.getStaffId());
        }
        
        return false;
    }

    // ==================== 用户端专用方法实现 ====================

    @Override
    public HumanServiceSessionResponse getUserCurrentSession() {
        try {
            // 获取当前用户ID和类型
            Integer userId = getCurrentUserId();
            String userType = getCurrentUserType();
            
            // 查询用户最近的活跃会话 - 使用统一会话表
            LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UnifiedChatSession::getUserId, userId)
                   .eq(UnifiedChatSession::getUserType, userType)
                   .eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN)
                   .in(UnifiedChatSession::getStatus, UnifiedChatSession.STATUS_WAITING, UnifiedChatSession.STATUS_ACTIVE)
                   .orderByDesc(UnifiedChatSession::getCreateTime)
                   .last("LIMIT 1");
            
            UnifiedChatSession session = unifiedChatSessionDao.selectOne(wrapper);
            
            if (session == null) {
                return null;
            }
            
            return convertToSessionResponse(convertToHumanServiceSession(session));
            
        } catch (Exception e) {
            log.error("获取用户当前会话失败", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getUserSessionDetail(String sessionId) {
        try {
            // 验证会话是否存在 - 使用统一会话表
            LambdaQueryWrapper<UnifiedChatSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UnifiedChatSession::getSessionId, sessionId);
            wrapper.eq(UnifiedChatSession::getCurrentServiceType, UnifiedChatSession.SERVICE_TYPE_HUMAN);
            UnifiedChatSession session = unifiedChatSessionDao.selectOne(wrapper);
            
            if (session == null) {
                throw new CrmebException("会话不存在");
            }
            
            // 验证权限 - 只能查看自己的会话
            Integer currentUserId = getCurrentUserId();
            if (!Objects.equals(currentUserId.longValue(), session.getUserId())) {
                throw new CrmebException("无权访问此会话");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", session.getSessionId());
            result.put("sessionStatus", session.getStatus());
            result.put("userType", session.getUserType());
            result.put("userId", session.getUserId());
            result.put("userName", "用户" + session.getUserId()); // 需要通过userService获取真实用户名
            result.put("merId", session.getMerId());
            result.put("staffId", session.getStaffId());
            result.put("queuePosition", session.getQueuePosition());
            result.put("priority", session.getPriority());
            result.put("createTime", session.getCreateTime());
            result.put("startTime", session.getWaitStartTime());
            result.put("endTime", session.getServiceEndTime());
            
            // 如果有分配的客服，获取客服信息
            if (session.getStaffId() != null) {
                CustomerServiceStaff staff = customerServiceStaffDao.selectById(session.getStaffId());
                if (staff != null) {
                    Map<String, Object> staffInfo = new HashMap<>();
                    staffInfo.put("id", staff.getId());
                    staffInfo.put("staffNo", staff.getStaffNo());
                    staffInfo.put("staffName", staff.getStaffNo()); // 使用工号作为名称
                    staffInfo.put("avatar", ""); // 客服头像字段待实现
                    staffInfo.put("onlineStatus", staff.getOnlineStatus());
                    result.put("staffInfo", staffInfo);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取用户会话详情失败", e);
            throw new CrmebException("获取会话详情失败: " + e.getMessage());
        }
    }

    @Override
    public HumanServiceMessageResponse sendUserMessage(HumanServiceMessageRequest request) {
        try {
            // 验证会话是否存在且属于当前用户
            HumanServiceSession session = humanServiceSessionDao.selectById(request.getSessionId());
            if (session == null) {
                throw new CrmebException("会话不存在");
            }
            
            Integer currentUserId = getCurrentUserId();
            if (!currentUserId.equals(session.getUserId().intValue())) {
                throw new CrmebException("无权访问此会话");
            }
            
            // 检查会话状态
            if (!"ACTIVE".equals(session.getSessionStatus()) && !"WAITING".equals(session.getSessionStatus())) {
                throw new CrmebException("会话已结束，无法发送消息");
            }
            
            // 创建消息记录
            HumanServiceMessage message = new HumanServiceMessage();
            message.setMessageId(CrmebUtil.getUuid());
            message.setSessionId(request.getSessionId());
            message.setSenderId(currentUserId.longValue());
            message.setSenderType("USER");
            message.setReceiverId(session.getStaffId());
            message.setReceiverType("MERCHANT");
            message.setMessageType(request.getMessageType());
            message.setContent(request.getContent());
            message.setContentFormat(request.getContentFormat());
            message.setAttachments(request.getAttachments());
            message.setIsRead(false);
            message.setStatus("SENT");
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());
            
            humanServiceMessageDao.insert(message);
            
            // 更新会话时间
            session.setUpdateTime(new Date());
            humanServiceSessionDao.updateById(session);
            
            // 通过WebSocket发送实时消息
            try {
                humanServiceWebSocketService.sendChatMessage(
                    currentUserId,
                    "USER", 
                    request.getSessionId(),
                    request.getContent(),
                    session.getStaffId().intValue(),
                    "MERCHANT"
                );
            } catch (Exception e) {
                log.warn("WebSocket发送消息失败", e);
            }
            
            return convertToMessageResponse(message);
            
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new CrmebException("发送消息失败: " + e.getMessage());
        }
    }

    @Override
    public CommonPage<HumanServiceMessageResponse> getUserMessageList(Map<String, Object> params) {
        try {
            String sessionId = (String) params.get("sessionId");
            Integer page = (Integer) params.getOrDefault("page", 1);
            Integer limit = (Integer) params.getOrDefault("limit", 20);
            
            // 验证会话权限
            HumanServiceSession session = humanServiceSessionDao.selectByEnterpriseSessionId(sessionId);
            if (session == null) {
                throw new CrmebException("会话不存在");
            }
            
            Integer currentUserId = getCurrentUserId();
            if (!currentUserId.equals(session.getUserId().intValue())) {
                throw new CrmebException("无权访问此会话");
            }
            
            // 分页查询消息
            PageHelper.startPage(page, limit);
            
            LambdaQueryWrapper<HumanServiceMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HumanServiceMessage::getSessionId, sessionId)
                   .orderByAsc(HumanServiceMessage::getCreateTime);
            
            List<HumanServiceMessage> messages = humanServiceMessageDao.selectList(wrapper);
            PageInfo<HumanServiceMessage> pageInfo = new PageInfo<>(messages);
            
            // 转换为响应对象
            List<HumanServiceMessageResponse> responseList = messages.stream()
                    .map(this::convertToMessageResponse)
                    .collect(Collectors.toList());
            
            // 标记消息为已读（用户查看消息时）
            markUserMessagesAsRead(sessionId, currentUserId);
            
            return CommonPage.restPage(new PageInfo<>(responseList));
            
        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            throw new CrmebException("获取消息列表失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadImage(MultipartFile file) {
        try {
            // 使用项目的上传服务
            FileResultVo uploadPath = uploadService.imageUpload(file, "HUMAN_SERVICE", 0);
            
            Map<String, Object> result = new HashMap<>();
            result.put("url", uploadPath.getUrl());
            result.put("fileName", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("type", file.getContentType());
            
            log.info("人工客服图片上传成功: {}", uploadPath.getUrl());
            return result;
            
        } catch (Exception e) {
            log.error("上传图片失败", e);
            throw new CrmebException("上传图片失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadFile(MultipartFile file) {
        try {
            // 使用项目的上传服务
            FileResultVo uploadPath = uploadService.fileUpload(file, "HUMAN_SERVICE", 0);
            
            Map<String, Object> result = new HashMap<>();
            result.put("url", uploadPath.getUrl());
            result.put("fileName", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("type", file.getContentType());
            
            log.info("人工客服文件上传成功: {}", uploadPath.getUrl());
            return result;
            
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new CrmebException("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public HumanServiceMessageResponse sendProductMessage(String sessionId, Integer productId) {
        try {
            // 获取商品信息
            Product product = productService.getById(productId);
            if (product == null) {
                throw new CrmebException("商品不存在");
            }
            
            // 构建商品信息JSON
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("id", product.getId());
            productInfo.put("name", product.getName() != null ? product.getName() : "商品名称");
            productInfo.put("price", product.getPrice());
            productInfo.put("image", product.getImage());
            productInfo.put("stock", product.getStock());
            productInfo.put("url", "/product/" + product.getId());
            
            // 发送商品链接消息
            HumanServiceMessageRequest request = new HumanServiceMessageRequest();
            request.setSessionId(sessionId);
            request.setMessageType("PRODUCT_LINK");
            request.setContent(JSON.toJSONString(productInfo));
            request.setContentFormat("JSON");
            
            // TODO: 使用 UnifiedChatService.sendMessage() 替代
            throw new RuntimeException("此功能需要使用 UnifiedChatService 重新实现");
            // HumanServiceMessage message = sendMessage(request);
            // return convertToMessageResponse(message);
            
        } catch (Exception e) {
            log.error("发送商品消息失败", e);
            throw new CrmebException("发送商品消息失败: " + e.getMessage());
        }
    }

    @Override
    public HumanServiceMessageResponse sendOrderMessage(String sessionId, String orderNo) {
        try {
            // 获取订单信息
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                throw new CrmebException("订单不存在");
            }
            
            // 检查订单权限（只能发送自己的订单）
            Integer currentUserId = getCurrentUserId();
            if (!currentUserId.equals(order.getUid())) {
                throw new CrmebException("无权访问此订单");
            }
            
            // 构建订单信息JSON
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderNo", order.getOrderNo());
            orderInfo.put("amount", order.getPayPrice());
            orderInfo.put("status", order.getStatus());
            orderInfo.put("createTime", order.getCreateTime());
            orderInfo.put("url", "/order/" + order.getId());
            
            // 发送订单链接消息
            HumanServiceMessageRequest request = new HumanServiceMessageRequest();
            request.setSessionId(sessionId);
            request.setMessageType("ORDER_LINK");
            request.setContent(JSON.toJSONString(orderInfo));
            request.setContentFormat("JSON");
            
            // TODO: 使用 UnifiedChatService.sendMessage() 替代
            throw new RuntimeException("此功能需要使用 UnifiedChatService 重新实现");
            // HumanServiceMessage message = sendMessage(request);
            // return convertToMessageResponse(message);
            
        } catch (Exception e) {
            log.error("发送订单消息失败", e);
            throw new CrmebException("发送订单消息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOnlineStatus() {
        try {
            // 获取在线客服数量
            int onlineStaffCount = customerServiceStaffService.getOnlineStaffCount();
            
            // 获取等待队列长度
            long waitingCount = humanServiceSessionDao.selectCount(
                new LambdaQueryWrapper<HumanServiceSession>()
                    .eq(HumanServiceSession::getSessionStatus, "WAITING")
            );
            
            // 计算预估等待时间
            int estimatedWaitTime = calculateEstimatedWaitTime(onlineStaffCount, (int) waitingCount);
            
            Map<String, Object> result = new HashMap<>();
            result.put("onlineStaffCount", onlineStaffCount);
            result.put("waitingCount", waitingCount);
            result.put("estimatedWaitTime", estimatedWaitTime);
            result.put("serviceAvailable", onlineStaffCount > 0);
            
            return result;
            
        } catch (Exception e) {
            log.error("获取在线状态失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("serviceAvailable", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> getUserQueueInfo() {
        try {
            Integer currentUserId = getCurrentUserId();
            
            // 查找用户在队列中的位置
            LambdaQueryWrapper<HumanServiceSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HumanServiceSession::getSessionStatus, "WAITING")
                   .orderByAsc(HumanServiceSession::getCreateTime);
            
            List<HumanServiceSession> waitingSessions = humanServiceSessionDao.selectList(wrapper);
            
            int queuePosition = -1;
            String userSessionId = null;
            
            for (int i = 0; i < waitingSessions.size(); i++) {
                if (currentUserId.equals(waitingSessions.get(i).getUserId().intValue())) {
                    queuePosition = i + 1;
                    userSessionId = waitingSessions.get(i).getSessionId();
                    break;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("inQueue", queuePosition > 0);
            result.put("queuePosition", queuePosition);
            result.put("totalWaiting", waitingSessions.size());
            result.put("sessionId", userSessionId);
            
            if (queuePosition > 0) {
                int estimatedWaitTime = queuePosition * 3; // 假设每个用户平均等待3分钟
                result.put("estimatedWaitTime", estimatedWaitTime);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取队列信息失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    @Override
    public void rateService(String sessionId, Integer rating, String comment) {
        try {
            LambdaQueryWrapper<UnifiedChatSession> sessionQuery = new LambdaQueryWrapper<>();
            sessionQuery.eq(UnifiedChatSession::getSessionId, sessionId);
            UnifiedChatSession session = unifiedChatSessionDao.selectOne(sessionQuery);
            if (session == null) {
                throw new CrmebException("会话不存在");
            }

            Integer currentUserId = userService.getUserId();
            if (!currentUserId.equals(session.getUserId().intValue())) {
                throw new CrmebException("无权评价此会话");
            }
            
            if (!"ENDED".equals(session.getStatus())) {
                throw new CrmebException("只能评价已结束的会话");
            }
            
            // 检查是否已经评价过
            if (humanServiceRatingService.hasUserRatedSession(sessionId, currentUserId.longValue())) {
                throw new CrmebException("您已经评价过该客服服务");
            }
            
            // 保存评价到评价表
            boolean success = humanServiceRatingService.rateService(
                sessionId, 
                currentUserId.longValue(), 
                rating, 
                comment, 
                null, // tags
                null, // detailScores
                false // isAnonymous
            );
            
            if (!success) {
                throw new CrmebException("保存评价失败");
            }
            
            // 更新会话状态
            session.setUpdateTime(new Date());
            unifiedChatSessionDao.updateById(session);
            
            log.info("用户评价服务成功: sessionId={}, rating={}, userId={}", sessionId, rating, currentUserId);
            
        } catch (Exception e) {
            log.error("评价服务失败", e);
            throw new CrmebException("评价服务失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getFAQ() {
        // 这里可以从配置或数据库中获取常见问题
        List<Map<String, Object>> faqList = new ArrayList<>();
        
        Map<String, Object> faq1 = new HashMap<>();
        faq1.put("question", "如何查看订单状态？");
        faq1.put("answer", "您可以在【我的订单】页面查看所有订单的详细状态");
        faq1.put("category", "订单相关");
        
        Map<String, Object> faq2 = new HashMap<>();
        faq2.put("question", "如何申请退款？");
        faq2.put("answer", "在订单详情页面点击【申请退款】按钮，填写退款原因即可");
        faq2.put("category", "售后服务");
        
        Map<String, Object> faq3 = new HashMap<>();
        faq3.put("question", "配送费如何计算？");
        faq3.put("answer", "配送费根据距离和重量计算，具体费用在下单时显示");
        faq3.put("category", "配送相关");
        
        faqList.add(faq1);
        faqList.add(faq2);
        faqList.add(faq3);
        
        Map<String, Object> result = new HashMap<>();
        result.put("faqList", faqList);
        result.put("totalCount", faqList.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getQuickQuestions() {
        // 获取快捷问题模板
        List<String> questions = new ArrayList<>();
        questions.add( "我的订单什么时候发货？");
        questions.add( "如何修改收货地址？");
        questions.add(  "可以开发票吗？");
        questions.add(  "支持哪些支付方式？");
        questions.add(  "如何联系配送员？");
        questions.add( "商品有质量问题怎么办？");
        Map<String, Object> result = new HashMap<>();
        result.put("questions", questions);
        
        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 转换为会话响应对象
     */
    private HumanServiceSessionResponse convertToSessionResponse(HumanServiceSession session) {
        if (session == null) return null;
        
        HumanServiceSessionResponse response = new HumanServiceSessionResponse();
        response.setSessionId(session.getSessionId());
        response.setUserId(session.getUserId().intValue());
        response.setUserType(session.getUserType());
        response.setUserName("用户" + session.getUserId()); // 需要通过userService获取
        response.setUserAvatar(""); // 需要通过userService获取
        response.setMerId(session.getMerId().intValue());
        response.setStaffId(session.getStaffId().intValue());
        response.setSessionStatus(session.getSessionStatus());
        response.setPriority(session.getPriority());
        response.setServiceLevel("STANDARD"); // 默认服务等级
        response.setTags(session.getTags());
        response.setRemarks(""); // 备注字段暂未实现
        response.setLastMessage(""); // 需要查询最后一条消息
        response.setLastMessageTime(session.getUpdateTime());
        response.setUnreadCount(0); // 需要统计未读消息数
        response.setWaitStartTime(session.getWaitStartTime());
        response.setServiceStartTime(session.getServiceStartTime());
        response.setServiceEndTime(session.getServiceEndTime());
        response.setCreateTime(session.getCreateTime());
        response.setRating(null); // 评分从备注中解析
        response.setRatingComment(""); // 评价信息从评价表获取
        
        // 获取客服信息
        if (session.getStaffId() != null) {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(session.getStaffId());
            if (staff != null) {
                response.setStaffName(staff.getStaffNo()); // 使用工号作为名称
                response.setStaffNo(staff.getStaffNo());
                response.setStaffOnline("ONLINE".equals(staff.getOnlineStatus()));
            }
        }
        
        // 计算会话时长
        if (session.getServiceStartTime() != null) {
            Date endTime = session.getServiceEndTime() != null ? session.getServiceEndTime() : new Date();
            long duration = (endTime.getTime() - session.getServiceStartTime().getTime()) / (1000 * 60);
            response.setSessionDuration((int) duration);
        }
        
        return response;
    }

    /**
     * 转换为消息响应对象
     */
    private HumanServiceMessageResponse convertToMessageResponse(HumanServiceMessage message) {
        if (message == null) return null;
        
        HumanServiceMessageResponse response = new HumanServiceMessageResponse();
        response.setMessageId(message.getMessageId());
        response.setSessionId(message.getSessionId());
        response.setSenderId(message.getSenderId().intValue());
        response.setSenderType(message.getSenderType());
        response.setReceiverId(message.getReceiverId().intValue());
        response.setReceiverType(message.getReceiverType());
        response.setMessageType(message.getMessageType());
        response.setContent(message.getContent());
        response.setContentFormat(message.getContentFormat());
        response.setExtraData(message.getAttachments());
        response.setIsRead(message.getIsRead());
        response.setMessageStatus(message.getStatus());
        response.setSendTime(message.getCreateTime());
        response.setDeliveredTime(message.getCreateTime());
        response.setReadTime(message.getReadTime());
        response.setCreateTime(message.getCreateTime());
        response.setReplyToMessageId(message.getRelatedMessageId());
        
        // 设置发送者信息
        if ("USER".equals(message.getSenderType())) {
            // 获取用户信息
            User user = userService.getById(message.getSenderId().intValue());
            if (user != null) {
                response.setSenderName(user.getNickname());
                response.setSenderAvatar(user.getAvatar());
            }
        } else if ("MERCHANT".equals(message.getSenderType())) {
            // 获取客服信息
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(message.getSenderId());
            if (staff != null) {
                response.setSenderName(staff.getStaffNo()); // 使用工号作为名称
            }
        }
        
        // 解析富媒体消息内容
        if ("PRODUCT_LINK".equals(message.getMessageType())) {
            try {
                Map<String, Object> productInfo = JSON.parseObject(message.getContent(), Map.class);
                response.setProductId((Integer) productInfo.get("id"));
                response.setProductName((String) productInfo.get("name"));
                response.setProductPrice((String) productInfo.get("price"));
                response.setProductImage((String) productInfo.get("image"));
                response.setProductStock((Integer) productInfo.get("stock"));
                response.setProductUrl((String) productInfo.get("url"));
            } catch (Exception e) {
                log.warn("解析商品信息失败", e);
            }
        } else if ("ORDER_LINK".equals(message.getMessageType())) {
            try {
                Map<String, Object> orderInfo = JSON.parseObject(message.getContent(), Map.class);
                response.setOrderNo((String) orderInfo.get("orderNo"));
                response.setOrderAmount((String) orderInfo.get("amount"));
                response.setOrderStatus((String) orderInfo.get("status"));
                response.setOrderUrl((String) orderInfo.get("url"));
            } catch (Exception e) {
                log.warn("解析订单信息失败", e);
            }
        } else if ("IMAGE".equals(message.getMessageType())) {
            response.setImageUrl(message.getContent());
        } else if ("FILE".equals(message.getMessageType())) {
            try {
                Map<String, Object> fileInfo = JSON.parseObject(message.getContent(), Map.class);
                response.setFileName((String) fileInfo.get("name"));
                response.setFileSize(((Number) fileInfo.get("size")).longValue());
                response.setFileType((String) fileInfo.get("type"));
                response.setFileUrl((String) fileInfo.get("url"));
            } catch (Exception e) {
                log.warn("解析文件信息失败", e);
            }
        }
        
        return response;
    }

    /**
     * 标记用户消息为已读
     */
    private void markUserMessagesAsRead(String sessionId, Integer userId) {
        try {
            LambdaUpdateWrapper<HumanServiceMessage> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(HumanServiceMessage::getIsRead, true)
                   .set(HumanServiceMessage::getReadTime, new Date())
                   .eq(HumanServiceMessage::getSessionId, sessionId)
                   .eq(HumanServiceMessage::getReceiverId, userId)
                   .eq(HumanServiceMessage::getIsRead, false);
            
            humanServiceMessageDao.update(null, wrapper);
        } catch (Exception e) {
            log.warn("标记消息已读失败", e);
        }
    }

    /**
     * 计算预估等待时间
     */
    private int calculateEstimatedWaitTime(int onlineStaffCount, int waitingCount) {
        if (onlineStaffCount == 0) {
            return -1; // 无客服在线
        }
        
        // 简单算法：假设每个客服同时处理3个用户，每个用户平均服务时间5分钟
        int capacity = onlineStaffCount * 3;
        if (waitingCount <= capacity) {
            return 1; // 立即服务
        }
        
        return ((waitingCount - capacity) / onlineStaffCount) * 5;
    }

    /**
     * 转换商户对象为Map格式
     */
    private Map<String, Object> convertMerchantToMap(Merchant merchant) {
        Map<String, Object> merchantMap = new HashMap<>();
        merchantMap.put("id", merchant.getId());
        merchantMap.put("name", merchant.getName());
        merchantMap.put("realName", merchant.getRealName());
        merchantMap.put("phone", merchant.getPhone());
        merchantMap.put("email", merchant.getEmail());
        merchantMap.put("avatar", merchant.getAvatar());
        merchantMap.put("coverImage", merchant.getCoverImage());
        merchantMap.put("intro", merchant.getIntro());
        merchantMap.put("categoryId", merchant.getCategoryId());
        merchantMap.put("typeId", merchant.getTypeId());
        merchantMap.put("starLevel", merchant.getStarLevel());
        merchantMap.put("isRecommend", merchant.getIsRecommend());
        merchantMap.put("isSelf", merchant.getIsSelf());
        merchantMap.put("isSwitch", merchant.getIsSwitch());
        merchantMap.put("province", merchant.getProvince());
        merchantMap.put("city", merchant.getCity());
        merchantMap.put("district", merchant.getDistrict());
        merchantMap.put("addressDetail", merchant.getAddressDetail());
        merchantMap.put("createTime", merchant.getCreateTime());
        merchantMap.put("updateTime", merchant.getUpdateTime());
        
        // 添加聊天相关信息
        merchantMap.put("chatAvailable", true); // 商户可以聊天
        merchantMap.put("userType", "MERCHANT");
        merchantMap.put("displayName", StringUtils.hasText(merchant.getName()) ? merchant.getName() : merchant.getRealName());
        
        return merchantMap;
    }

    /**
     * 转换用户对象为Map格式
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("account", user.getAccount());
        userMap.put("nickname", user.getNickname());
        userMap.put("realName", user.getRealName());
        userMap.put("phone", user.getPhone());
        userMap.put("avatar", user.getAvatar());
        userMap.put("sex", user.getSex());
        userMap.put("level", user.getLevel());
        userMap.put("integral", user.getIntegral());
        userMap.put("experience", user.getExperience());
        userMap.put("isPromoter", user.getIsPromoter());
        userMap.put("isPaidMember", user.getIsPaidMember());
        userMap.put("payCount", user.getPayCount());
        userMap.put("province", user.getProvince());
        userMap.put("city", user.getCity());
        userMap.put("district", user.getDistrict());
        userMap.put("address", user.getAddress());
        userMap.put("lastLoginTime", user.getLastLoginTime());
        userMap.put("createTime", user.getCreateTime());
        userMap.put("updateTime", user.getUpdateTime());
        userMap.put("status", user.getStatus());
        userMap.put("signature", user.getSignature());
        
        // 添加聊天相关信息
        userMap.put("chatAvailable", true); // 用户可以聊天
        userMap.put("userType", "USER");
        userMap.put("displayName", StringUtils.hasText(user.getNickname()) ? user.getNickname() : 
                                  (StringUtils.hasText(user.getRealName()) ? user.getRealName() : 
                                   (StringUtils.hasText(user.getPhone()) ? user.getPhone() : "用户" + user.getId())));
        
        // 计算用户活跃度 (简单示例：最近登录时间越近越活跃)
        if (user.getLastLoginTime() != null) {
            long daysSinceLastLogin = (System.currentTimeMillis() - user.getLastLoginTime().getTime()) / (1000 * 60 * 60 * 24);
            userMap.put("activityLevel", daysSinceLastLogin <= 7 ? "HIGH" : (daysSinceLastLogin <= 30 ? "MEDIUM" : "LOW"));
        } else {
            userMap.put("activityLevel", "LOW");
        }
        
        return userMap;
    }

    // ==================== 平台端方法实现 ====================

    @Override
    public CommonPage<HumanServiceSession> getPlatformSessionList(String sessionStatus, Integer staffId, String userType, Integer merId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        LambdaQueryWrapper<HumanServiceSession> wrapper = new LambdaQueryWrapper<>();
        
        // 平台端可以查看所有会话
        wrapper.eq(StringUtils.hasText(sessionStatus), HumanServiceSession::getSessionStatus, sessionStatus);
        wrapper.eq(staffId != null, HumanServiceSession::getStaffId, staffId);
        wrapper.eq(StringUtils.hasText(userType), HumanServiceSession::getUserType, userType);
        wrapper.eq(merId != null, HumanServiceSession::getMerId, merId);
        wrapper.orderByDesc(HumanServiceSession::getCreateTime);

        List<HumanServiceSession> list = humanServiceSessionDao.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public void transferSession(String sessionId, Integer targetStaffId, String reason) {
        HumanServiceSession session = humanServiceSessionDao.selectById(sessionId);
        if (session == null) {
            throw new CrmebException("会话不存在");
        }
        
        CustomerServiceStaff targetStaff = customerServiceStaffDao.selectById(targetStaffId);
        if (targetStaff == null || !customerServiceStaffService.isStaffAvailable(targetStaffId)) {
            throw new CrmebException("目标客服不可用");
        }
        
        // 更新会话的客服分配
        session.setStaffId(targetStaffId.longValue());
        session.setTransferReason(reason);
        session.setUpdateTime(new Date());
        
        humanServiceSessionDao.updateById(session);
        
        // 发送系统消息
        sendSystemMessage(sessionId, "您的会话已转接给客服工号：" + targetStaff.getStaffNo());
        
        // 通知新客服
        humanServiceWebSocketService.pushNewSessionNotification(targetStaffId, sessionId);
        
        log.info("会话转接成功: sessionId={}, targetStaffId={}", sessionId, targetStaffId);
    }

    @Override
    public Map<String, Object> getPlatformWorkplaceData() {
        Map<String, Object> workplaceData = new HashMap<>();
        
        // 获取平台整体统计数据
        long totalSessions = humanServiceSessionDao.selectCount(new LambdaQueryWrapper<>());
        long activeSessions = humanServiceSessionDao.selectCount(
            new LambdaQueryWrapper<HumanServiceSession>()
                .eq(HumanServiceSession::getSessionStatus, HumanServiceSession.SESSION_STATUS_ACTIVE)
        );
        long waitingSessions = humanServiceSessionDao.selectCount(
            new LambdaQueryWrapper<HumanServiceSession>()
                .eq(HumanServiceSession::getSessionStatus, HumanServiceSession.SESSION_STATUS_WAITING)
        );
        
        workplaceData.put("totalSessions", totalSessions);
        workplaceData.put("activeSessions", activeSessions);
        workplaceData.put("waitingSessions", waitingSessions);
        workplaceData.put("onlineStaffCount", customerServiceStaffService.getOnlineStaffCount());
        workplaceData.put("timestamp", System.currentTimeMillis());
        
        return workplaceData;
    }

    @Override
    public Map<String, Object> getPlatformServiceStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取平台整体会话统计
        List<Map<String, Object>> sessionStats = humanServiceSessionDao.getStaffSessionStatistics(
            null, startDate, endDate); // null表示查询所有商户
        statistics.put("sessionStats", sessionStats);
        
        // 获取排队统计
        Map<String, Object> queueStats = humanServiceSessionDao.getQueueStatistics(null);
        statistics.put("queueStats", queueStats);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getPlatformQueueInfo() {
        Map<String, Object> queueInfo = new HashMap<>();
        
        // 查询等待中的会话
        long waitingCount = humanServiceSessionDao.selectCount(
            new LambdaQueryWrapper<HumanServiceSession>()
                .eq(HumanServiceSession::getSessionStatus, HumanServiceSession.SESSION_STATUS_WAITING)
        );
        
        // 获取在线客服数量
        Integer onlineStaffCount = customerServiceStaffService.getOnlineStaffCount();
        
        queueInfo.put("waitingCount", waitingCount);
        queueInfo.put("onlineStaffCount", onlineStaffCount);
        // 计算预估等待时间（分钟）
        int estimatedMinutes = 0;
        if (onlineStaffCount > 0) {
            estimatedMinutes = ((int)waitingCount / onlineStaffCount) * 5;
        }
        queueInfo.put("estimatedWaitTime", Math.max(1, estimatedMinutes));
        
        return queueInfo;
    }

    @Override
    public CustomerServiceConfig getPlatformServiceConfig() {
        // 平台配置逻辑
        CustomerServiceConfig config = new CustomerServiceConfig();
        config.setId(0L);
        config.setMerId(0); // 平台配置
        // 设置平台配置的默认值
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        return config;
    }

    @Override
    public void updatePlatformServiceConfig(CustomerServiceConfig config) {
        config.setMerId(0); // 确保是平台配置
        config.setUpdateTime(new Date());
        // 这里应该保存到配置表
        log.info("更新平台客服配置: {}", config);
    }

    @Override
    public List<QuickReplyTemplate> getPlatformQuickReplyTemplates(String category) {
        LambdaQueryWrapper<QuickReplyTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuickReplyTemplate::getMerId, 0L); // 平台模板
        wrapper.eq(StringUtils.hasText(category), QuickReplyTemplate::getCategory, category);
        wrapper.orderByAsc(QuickReplyTemplate::getCreateTime);
        
        return quickReplyTemplateDao.selectList(wrapper);
    }

    @Override
    public void addPlatformQuickReplyTemplate(QuickReplyTemplate template) {
        template.setMerId(0); // 平台模板
        template.setCreateTime(new Date());
        template.setUpdateTime(new Date());
        
        quickReplyTemplateDao.insert(template);
    }

    @Override
    public void updatePlatformQuickReplyTemplate(QuickReplyTemplate template) {
        template.setMerId(0); // 确保是平台模板
        template.setUpdateTime(new Date());
        
        quickReplyTemplateDao.updateById(template);
    }

    @Override
    public void deletePlatformQuickReplyTemplate(Long templateId) {
        QuickReplyTemplate template = quickReplyTemplateDao.selectById(templateId);
        if (template == null || !template.getMerId().equals(0L)) {
            throw new CrmebException("模板不存在或无权限删除");
        }
        
        quickReplyTemplateDao.deleteById(templateId);
    }

    @Override
    public Map<String, Object> startPlatformMerchantChat(Integer merchantId, String message) {
        // 创建平台与商户的聊天会话
        HumanServiceSession session = new HumanServiceSession();
        session.setSessionId(CrmebUtil.getUuid());
        session.setUserId(merchantId.longValue());
        session.setUserType("MERCHANT");
        session.setMerId(0L); // 平台会话
        session.setSessionType(HumanServiceSession.SESSION_TYPE_DIRECT);
        session.setSessionStatus(HumanServiceSession.SESSION_STATUS_ACTIVE);
        session.setPriority("NORMAL");
        session.setWaitStartTime(new Date());
        session.setServiceStartTime(new Date());
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        
        humanServiceSessionDao.insert(session);
        
        // 发送初始消息
        if (StringUtils.hasText(message)) {
            sendSystemMessage(session.getSessionId(), message);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("status", session.getSessionStatus());
        
        return result;
    }

    @Override
    public Map<String, Object> startPlatformUserChat(Integer userId, String message) {
        // 创建平台与用户的聊天会话
        HumanServiceSession session = new HumanServiceSession();
        session.setSessionId(CrmebUtil.getUuid());
        session.setUserId(userId.longValue());
        session.setUserType("USER");
        session.setMerId(0L); // 平台会话
        session.setSessionType(HumanServiceSession.SESSION_TYPE_DIRECT);
        session.setSessionStatus(HumanServiceSession.SESSION_STATUS_ACTIVE);
        session.setPriority("NORMAL");
        session.setWaitStartTime(new Date());
        session.setServiceStartTime(new Date());
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        
        humanServiceSessionDao.insert(session);
        
        // 发送初始消息
        if (StringUtils.hasText(message)) {
            sendSystemMessage(session.getSessionId(), message);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("status", session.getSessionStatus());
        
        return result;
    }

    @Override
    public CommonPage<Map<String, Object>> getChatableMerchants(String keyword, PageParamRequest pageParamRequest) {
        try {
            // 设置分页参数
            PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
            
            // 构建查询条件
            LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
            
            // 只查询已开启且未删除的商户
            wrapper.eq(Merchant::getIsSwitch, true)  // 商户开关开启
                   .eq(Merchant::getIsDel, false);   // 未删除
            
            // 关键词搜索 - 支持商户名称、姓名、手机号搜索
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(Merchant::getName, keyword)
                              .or().like(Merchant::getRealName, keyword)
                              .or().like(Merchant::getPhone, keyword));
            }
            
            // 按创建时间倒序排列，保证新商户优先
            wrapper.orderByDesc(Merchant::getCreateTime);
            
            // 执行查询
            List<Merchant> merchantList = merchantDao.selectList(wrapper);
            PageInfo<Merchant> pageInfo = new PageInfo<>(merchantList);
            
            // 转换为返回格式
            List<Map<String, Object>> resultList = merchantList.stream()
                .map(this::convertMerchantToMap)
                .collect(Collectors.toList());
            
            // 构建分页结果
            PageInfo<Map<String, Object>> result = new PageInfo<>(resultList);
            result.setTotal(pageInfo.getTotal());
            result.setPageNum(pageInfo.getPageNum());
            result.setPageSize(pageInfo.getPageSize());
            result.setPages(pageInfo.getPages());
            
            log.info("获取可聊天商户列表成功，关键词：{}，总数：{}", keyword, result.getTotal());
            return CommonPage.restPage(result);
            
        } catch (Exception e) {
            log.error("获取可聊天商户列表失败", e);
            return CommonPage.restPage(new PageInfo<>(new ArrayList<>()));
        }
    }

    @Override
    public CommonPage<Map<String, Object>> getChatableUsers(String keyword, PageParamRequest pageParamRequest) {
        try {
            // 设置分页参数
            PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
            
            // 构建查询条件
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            
            // 只查询正常状态且未注销的用户
            wrapper.eq(User::getStatus, true)       // 正常状态
                   .eq(User::getIsLogoff, false);   // 未注销
            
            // 关键词搜索 - 支持昵称、真实姓名、手机号搜索
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(User::getNickname, keyword)
                              .or().like(User::getRealName, keyword)
                              .or().like(User::getPhone, keyword)
                              .or().like(User::getAccount, keyword));
            }
            
            // 按最后登录时间倒序排列，保证活跃用户优先
            wrapper.orderByDesc(User::getLastLoginTime)
                   .orderByDesc(User::getCreateTime);
            
            // 执行查询
            List<User> userList = userDao.selectList(wrapper);
            PageInfo<User> pageInfo = new PageInfo<>(userList);
            
            // 转换为返回格式
            List<Map<String, Object>> resultList = userList.stream()
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
            
            // 构建分页结果
            PageInfo<Map<String, Object>> result = new PageInfo<>(resultList);
            result.setTotal(pageInfo.getTotal());
            result.setPageNum(pageInfo.getPageNum());
            result.setPageSize(pageInfo.getPageSize());
            result.setPages(pageInfo.getPages());
            
            log.info("获取可聊天用户列表成功，关键词：{}，总数：{}", keyword, result.getTotal());
            return CommonPage.restPage(result);
            
        } catch (Exception e) {
            log.error("获取可聊天用户列表失败", e);
            return CommonPage.restPage(new PageInfo<>(new ArrayList<>()));
        }
    }

    // ==================== 控制器新增接口方法实现 ====================

    @Override
    public Map<String, Object> getUserProfile(Integer userId, String userType) {
        try {
            Map<String, Object> profile = new HashMap<>();
            
            if ("USER".equals(userType)) {
                // 获取用户信息
                User user = userService.getById(userId);
                if (user != null) {
                    profile.put("id", user.getId());
                    profile.put("nickname", user.getNickname());
                    profile.put("avatar", user.getAvatar());
                    profile.put("phone", user.getPhone());
                    profile.put("level", user.getLevel());
                    profile.put("integral", user.getIntegral());
                    profile.put("experience", user.getExperience());
                    profile.put("createTime", user.getCreateTime());
                    profile.put("lastLoginTime", user.getLastLoginTime());
                }
            } else if ("MERCHANT".equals(userType)) {
                // 获取商户信息
                Merchant merchant = merchantService.getById(userId);
                if (merchant != null) {
                    profile.put("id", merchant.getId());
                    profile.put("name", merchant.getName());
                    profile.put("avatar", merchant.getAvatar());
                    profile.put("phone", merchant.getPhone());
                    profile.put("createTime", merchant.getCreateTime());
                }
            }
            
            return profile;
        } catch (Exception e) {
            log.error("获取用户资料失败: userId={}, userType={}", userId, userType, e);
            throw new CrmebException("获取用户资料失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionSettings(String sessionId, Map<String, Object> settings) {
        try {
            HumanServiceSession session = humanServiceSessionDao.selectById(sessionId);
            if (session == null) {
                throw new CrmebException("会话不存在");
            }

            if (settings.containsKey("priority")) {
                session.setPriority((String)settings.get("priority"));
            }
            if (settings.containsKey("tags")) {
                session.setTags(JSON.toJSONString(settings.get("tags")));
            }
            humanServiceSessionDao.updateById(session);
            log.info("会话设置更新成功: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("更新会话设置失败: sessionId={}", sessionId, e);
            throw new CrmebException("更新会话设置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferSession(String sessionId, Map<String, Object> transferRequest) {
        try {
            Integer targetStaffId = (Integer) transferRequest.get("targetStaffId");
            String reason = (String) transferRequest.get("reason");
            
            if (targetStaffId != null) {
                // 转接给指定客服
                transferSession(sessionId, targetStaffId, reason);
            } else {
                // 如果没有指定客服，自动分配可用客服
                CustomerServiceStaff availableStaff = assignStaff(getCurrentMerId(), "STANDARD", "");
                if (availableStaff != null) {
                    transferSession(sessionId, availableStaff.getId(), reason != null ? reason : "系统自动分配");
                } else {
                    throw new CrmebException("暂无可用客服");
                }
            }
        } catch (Exception e) {
            log.error("转接会话失败: sessionId={}", sessionId, e);
            throw new CrmebException("转接会话失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignStaff(String sessionId, Integer staffId) {
        try {
            HumanServiceSession session = humanServiceSessionDao.selectById(sessionId);
            if (session == null) {
                throw new CrmebException("会话不存在");
            }
            
            CustomerServiceStaff staff = customerServiceStaffService.getByEmployeeId(staffId);
            if (staff == null || !customerServiceStaffService.isStaffAvailable(staffId)) {
                throw new CrmebException("客服不存在或不可用");
            }
            
            LambdaUpdateWrapper<HumanServiceSession> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(HumanServiceSession::getSessionId, sessionId)
                        .set(HumanServiceSession::getStaffId, staffId)
                        .set(HumanServiceSession::getSessionStatus, "ASSIGNED")
                        .set(HumanServiceSession::getUpdateTime, new Date());
            
            humanServiceSessionDao.update(session,updateWrapper);
            
            // 更新客服当前会话数
            customerServiceStaffService.updateCurrentSessions(staffId, 1);
            
            log.info("会话分配成功: sessionId={}, staffId={}", sessionId, staffId);
        } catch (Exception e) {
            log.error("分配客服失败: sessionId={}, staffId={}", sessionId, staffId, e);
            throw new CrmebException("分配客服失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuickReplyTemplate(QuickReplyTemplate template) {
        try {
            template.setMerId(getCurrentMerId());
            template.setCreateTime(new Date());
            template.setUpdateTime(new Date());
            quickReplyTemplateDao.insert(template);
            log.info("快捷回复模板添加成功: title={}", template.getTitle());
        } catch (Exception e) {
            log.error("添加快捷回复模板失败", e);
            throw new CrmebException("添加快捷回复模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuickReplyTemplate(QuickReplyTemplate template) {
        try {
            QuickReplyTemplate existingTemplate = quickReplyTemplateDao.selectById(template.getId());
            if (existingTemplate == null) {
                throw new CrmebException("快捷回复模板不存在");
            }
            
            template.setUpdateTime(new Date());
            quickReplyTemplateDao.updateById(template);
            log.info("快捷回复模板更新成功: id={}", template.getId());
        } catch (Exception e) {
            log.error("更新快捷回复模板失败: id={}", template.getId(), e);
            throw new CrmebException("更新快捷回复模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuickReplyTemplate(Long templateId) {
        try {
            QuickReplyTemplate template = quickReplyTemplateDao.selectById(templateId);
            if (template == null) {
                throw new CrmebException("快捷回复模板不存在");
            }
            
            quickReplyTemplateDao.deleteById(templateId);
            log.info("快捷回复模板删除成功: id={}", templateId);
        } catch (Exception e) {
            log.error("删除快捷回复模板失败: id={}", templateId, e);
            throw new CrmebException("删除快捷回复模板失败: " + e.getMessage());
        }
    }

    @Override
    public HumanServiceMessage sendProductMessage(Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            Integer productId = (Integer) request.get("productId");
            
            // 获取商品信息
            Product product = productService.getById(productId);
            if (product == null) {
                throw new CrmebException("商品不存在");
            }
            
            // 构建商品消息内容
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("productId", product.getId());
            productInfo.put("productName", product.getName());
            productInfo.put("productImage", product.getImage());
            productInfo.put("productPrice", product.getPrice());
            productInfo.put("productStock", product.getStock());
            
            // 发送消息
            HumanServiceMessageRequest messageRequest = new HumanServiceMessageRequest();
            messageRequest.setSessionId(sessionId);
            messageRequest.setMessageType("PRODUCT");
            messageRequest.setContent(JSON.toJSONString(productInfo));
            messageRequest.setSenderId(getCurrentStaffId());
            messageRequest.setSenderType("MERCHANT");
            
            // TODO: 使用 UnifiedChatService.sendMessage() 替代
            throw new RuntimeException("此功能需要使用 UnifiedChatService 重新实现");
            // return sendMessage(messageRequest);
        } catch (Exception e) {
            log.error("发送商品消息失败", e);
            throw new CrmebException("发送商品消息失败: " + e.getMessage());
        }
    }

    @Override
    public HumanServiceMessage sendOrderMessage(Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String orderNo = (String) request.get("orderNo");
            
            // 获取订单信息
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                throw new CrmebException("订单不存在");
            }
            
            // 构建订单消息内容
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderNo", order.getOrderNo());
            orderInfo.put("orderStatus", order.getStatus());
            orderInfo.put("totalPrice", order.getTotalPrice());
            orderInfo.put("payPrice", order.getPayPrice());
            orderInfo.put("createTime", order.getCreateTime());
            
            // 发送消息
            HumanServiceMessageRequest messageRequest = new HumanServiceMessageRequest();
            messageRequest.setSessionId(sessionId);
            messageRequest.setMessageType("ORDER");
            messageRequest.setContent(JSON.toJSONString(orderInfo));
            messageRequest.setSenderId(getCurrentStaffId());
            messageRequest.setSenderType("MERCHANT");
            
            // TODO: 使用 UnifiedChatService.sendMessage() 替代
            throw new RuntimeException("此功能需要使用 UnifiedChatService 重新实现");
            // return sendMessage(messageRequest);
        } catch (Exception e) {
            log.error("发送订单消息失败", e);
            throw new CrmebException("发送订单消息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getQueueInfo() {
        return getQueueInfo(getCurrentMerId());
    }

    @Override
    public CustomerServiceConfig getServiceConfig() {
        try {
            Integer merId = getCurrentMerId();
            CustomerServiceConfig config = customerServiceConfigDao.selectByMerId(merId);
            if (config == null) {
                // 如果没有配置，返回默认配置
                config = new CustomerServiceConfig();
                config.setMerId(merId);
                config.setServiceName("客服中心");
                config.setAutoWelcomeEnabled(true);
                config.setWelcomeMessage("您好！欢迎咨询，我是客服，很高兴为您服务！");
                config.setMaxConcurrentSessions(5);
                config.setWorkDays("1,2,3,4,5");
                config.setStatus(true);
            }
            return config;
        } catch (Exception e) {
            log.error("获取客服配置失败", e);
            throw new CrmebException("获取客服配置失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceConfig(CustomerServiceConfig config) {
        try {
            config.setMerId(getCurrentMerId());
            config.setUpdateTime(new Date());
            
            CustomerServiceConfig existingConfig = customerServiceConfigDao.selectByMerId(config.getMerId());
            if (existingConfig != null) {
                config.setId(existingConfig.getId());
                customerServiceConfigDao.updateById(config);
            } else {
                config.setCreateTime(new Date());
                customerServiceConfigDao.insert(config);
            }
            
            log.info("客服配置更新成功: merId={}", config.getMerId());
        } catch (Exception e) {
            log.error("更新客服配置失败", e);
            throw new CrmebException("更新客服配置失败: " + e.getMessage());
        }
    }

    /**
     * 构建Coze流式请求（辅助方法）
     */
    private java.util.Map<String, Object> buildCozeStreamRequest(
            UnifiedChatSession session,
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
    private Integer getCurrentStaffId() {
        return SecurityUtil.getLoginUserVo().getUser().getId();
    }

    private Integer getCurrentMerId() {
        return SecurityUtil.getLoginUserVo().getUser().getMerId();
    }

    /**
     * 处理小程序消息（非流式，通过WebSocket推送AI回复）
     */
    @Override
    public Map<String, Object> handleMessageForMiniProgram(EnterpriseChatMessageRequest request, 
                                                          LoginFrontUserVo userContext, 
                                                          String cozeBotId) {
        log.info("开始处理小程序消息，会话ID: {}, 内容: {}", request.getSessionId(), request.getContent());
        
        try {
            // 1. 保存用户消息到数据库
            SendMessageRequest messageRequest = new SendMessageRequest();
            messageRequest.setSessionId(request.getSessionId());
            messageRequest.setContent(request.getContent());
            messageRequest.setContentType(request.getContentType());
            messageRequest.setMessageType(request.getMessageType());
            messageRequest.setSenderType("USER");
            messageRequest.setSenderId(userContext.getUser().getId().longValue());
            
            // 获取会话信息以确定接收者
            try {
                com.zbkj.common.model.chat.UnifiedChatSession session = unifiedChatService.getSession(request.getSessionId());
                if (session != null && session.getStaffId() != null) {
                    // 如果会话已分配客服，设置客服为接收者
                    messageRequest.setReceiverId(session.getStaffId());
                    messageRequest.setReceiverType("MERCHANT");
                    log.info("设置消息接收者为客服: staffId={}", session.getStaffId());
                }
            } catch (Exception e) {
                log.warn("获取会话信息失败，将使用默认接收者设置逻辑: {}", e.getMessage());
            }
            
            // 发送消息到统一聊天服务
            MessageResponse userMessage = unifiedChatService.sendMessage(messageRequest);
            log.info("用户消息已保存，消息ID: {}", userMessage.getMessageId());
            
            // 2. 如果需要AI回复，异步处理AI响应
            if (request.getNeedAiReply() != null && request.getNeedAiReply()) {
                log.info("开始处理用户消息，会话ID: {}", request.getSessionId());
                
                // 检查是否需要转人工客服
                Map<String, Object> handoverCheck = chatHandoverService.shouldHandoverToHuman(
                    request.getContent(), request.getSessionId(), userContext.getUser().getId());
                boolean shouldHandover = (Boolean) handoverCheck.getOrDefault("shouldHandover", false);
                
                boolean handoverSuccessful = false;
                
                if (shouldHandover) {
                    // 用户需要转人工客服
                    String reason = (String) handoverCheck.get("reason");
                    String urgency = (String) handoverCheck.get("urgency");
                    
                    log.info("检测到需要转人工客服: sessionId={}, reason={}, urgency={}", 
                            request.getSessionId(), reason, urgency);
                    
                    // 发起转人工请求
                    try {
                        Map<String, Object> handoverResult = chatHandoverService.requestHandoverToHuman(
                            request.getSessionId(), userContext.getUser().getId(), reason, urgency);
                        
                        if ((Boolean) handoverResult.get("success")) {
                            log.info("转人工客服请求成功: sessionId={}, result={}", request.getSessionId(), handoverResult);
                            handoverSuccessful = true;
                            
                            // 推送转人工成功的消息到WebSocket
                            pushHandoverSuccessToWebSocket(request.getSessionId(), reason);
                        } else {
                            log.warn("转人工客服请求失败: sessionId={}, message={}", 
                                    request.getSessionId(), handoverResult.get("message"));
                        }
                    } catch (Exception e) {
                        log.error("转人工客服处理异常: sessionId={}, 错误: {}", request.getSessionId(), e.getMessage(), e);
                    }
                }
                
                // 只有在转人工失败或不需要转人工时才调用AI
                if (!handoverSuccessful) {
                    // 立即推送AI思考中状态
                   // log.info("📤 立即推送AI思考中状态");
                   // pushAiThinkingToWebSocket(request.getSessionId());
                    
                    // 继续AI处理（转人工失败或不需要转人工时）
                    processAiReplyAsync(request, userContext, cozeBotId);
                } else {
                    log.info("✅ 转人工客服成功，跳过AI处理: sessionId={}", request.getSessionId());
                }
            }
            
            // 3. 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("messageId", userMessage.getMessageId());
            response.put("sessionId", request.getSessionId());
            response.put("status", "sent");
            response.put("timestamp", System.currentTimeMillis());
            
            if (request.getNeedAiReply() != null && request.getNeedAiReply()) {
                response.put("aiReplyStatus", "processing");
                response.put("message", "消息已发送，AI正在处理回复");
                response.put("aiReplyMethod", "websocket_only");
                response.put("note", "AI回复将仅通过WebSocket推送，请等待WebSocket消息");
            } else {
                response.put("message", "消息发送成功");
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("处理小程序消息失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "消息处理失败: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return errorResponse;
        }
    }
    
    /**
     * 异步处理AI回复 - 支持流式处理
     */
    private void processAiReplyAsync(EnterpriseChatMessageRequest request, 
                                   LoginFrontUserVo userContext, 
                                   String cozeBotId) {
        // 使用线程池异步处理AI回复，传递用户上下文避免Spring Security上下文丢失
        new Thread(() -> {
            try {
                log.info("开始异步调用AI服务（流式处理），会话ID: {}", request.getSessionId());
                
                // 使用流式AI处理
                processStreamAiReply(request, cozeBotId, userContext);
                
            } catch (Exception e) {
                log.error("异步处理AI回复失败: {}", e.getMessage(), e);
                // 推送错误消息
                pushFinalCozeReplyViaWebSocket(request.getSessionId(), "抱歉，处理您的消息时出现问题，请重试。", userContext.getUser().getId());
            }
        }).start();
    }
    
    /**
     * 流式AI回复处理
     */
    private void processStreamAiReply(EnterpriseChatMessageRequest request, String cozeBotId, LoginFrontUserVo userContext) {
        try {
            log.info("开始流式AI处理，会话ID: {}", request.getSessionId());
            
            // 获取会话信息
            com.zbkj.common.model.chat.UnifiedChatSession session = getSessionById(request.getSessionId());
            if (session == null) {
                log.error("会话不存在: {}", request.getSessionId());
                pushFinalCozeReplyViaWebSocket(request.getSessionId(), "抱歉，会话已失效，请重新开始对话。", userContext.getUser().getId());
                return;
            }
            
            // 构建Coze流式API请求参数（按照官方文档格式）
            Map<String, Object> cozeRequest = new HashMap<>();
            cozeRequest.put("bot_id", cozeBotId);
            cozeRequest.put("user_id", userContext.getUser().getId().toString());
            cozeRequest.put("stream", true); // 启用流式响应
            cozeRequest.put("auto_save_history", true);
            
            // conversation_id需要传递给CozeStreamClient，它会自动处理URL参数
            if (session.getCozeConversationId() != null && !session.getCozeConversationId().isEmpty()) {
                cozeRequest.put("conversation_id", session.getCozeConversationId());
                log.info("使用已有会话ID: {}", session.getCozeConversationId());
            }
            
            // 构建消息内容 - 按照官方文档的additional_messages格式
            List<Map<String, Object>> additionalMessages = new ArrayList<>();
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", request.getContent());
            userMessage.put("content_type", request.getContentType() != null ? request.getContentType() : "text");
            // 根据文档，还可以添加type字段
            userMessage.put("type", "question");
            additionalMessages.add(userMessage);
            cozeRequest.put("additional_messages", additionalMessages);
            
            log.info("构建的Coze流式请求: {}", cozeRequest);
            
            // 验证必要参数
            if (cozeBotId == null || cozeBotId.isEmpty()) {
                log.error("❌ bot_id为空，无法调用Coze API");
                pushFinalCozeReplyViaWebSocket(request.getSessionId(), "配置错误：智能体ID为空", userContext.getUser().getId());
                return;
            }
            
            // 用于累积完整回复内容
            StringBuilder fullResponseBuilder = new StringBuilder();
            AtomicInteger totalTokens = new AtomicInteger();
            // 调用流式Coze API
            try {
                log.info("🚀 准备调用CozeStreamClient，bot_id: {}, user_id: {}", 
                        cozeBotId, userContext.getUser().getId());
                com.zbkj.common.model.coze.stream.CozeStreamResponse streamResponse = 
                    cozeStreamClient.startStreamChat(cozeRequest, (eventData) -> {
                        try {
                            log.info("🔄 收到Coze流式事件: {}", eventData);
                            
                            // 解析事件数据
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
                                
                                // 处理增量消息事件
                                if ("conversation.message.delta".equals(eventType) && dataBuilder.length() > 0) {
                                    try {
                                        // 解析增量数据
                                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                        Map<String, Object> deltaData = objectMapper.readValue(dataBuilder.toString(), Map.class);
                                        
                                        // 根据Coze API文档，增量消息的增量内容在content字段中
                                        String deltaContent = (String) deltaData.get("content");
                                        
                                        if (deltaContent != null && !deltaContent.isEmpty()) {
                                            // 累积内容
                                            fullResponseBuilder.append(deltaContent);
                                            log.info("✅ 收到增量内容: {}", deltaContent);
                                            
                                            // 推送增量消息到WebSocket
                                            pushStreamDeltaToWebSocket(request.getSessionId(), deltaContent);
                                        }
                                    } catch (Exception e) {
                                        log.warn("解析增量消息失败: {}, 原始数据: {}", e.getMessage(), dataBuilder.toString());
                                    }
                                } else if ("conversation.message.completed".equals(eventType)) {
                                    // 消息完成，推送完成事件
                                    log.info("✅ Coze消息完成，最终内容长度: {}", fullResponseBuilder.length());
                                    pushMessageCompletedToWebSocket(request.getSessionId(), fullResponseBuilder.toString());
                                } else if ("conversation.chat.completed".equals(eventType)) {
                                    // 对话完成
                                    log.info("✅ Coze对话完成");
                                    pushChatCompletedToWebSocket(request.getSessionId());
                                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    Map<String, Object> deltaData = objectMapper.readValue(dataBuilder.toString(), Map.class);
                                    Map<String, Object> usage = (Map<String, Object>) deltaData.get("usage");
                                    totalTokens.set((Integer) usage.get("token_count"));
                                    pushStreamDeltaToWebSocket(request.getSessionId(), "消耗的总tokens："+totalTokens);
                                } else if ("conversation.chat.created".equals(eventType)) {
                                    log.info("✅ Coze对话创建成功");
                                } else if ("conversation.chat.in_progress".equals(eventType)) {
                                    log.info("✅ Coze对话处理中");
                                } else {
                                    log.debug("忽略事件类型: {}", eventType);
                                }
                            } else {
                                log.warn("未知的事件格式: {}", eventData);
                            }
                        } catch (Exception e) {
                            log.error("处理流式事件失败: {}, 事件数据: {}", e.getMessage(), eventData, e);
                        }
                    });
                
                log.info("✅ Coze流式API调用完成");
                
                // 等待流式响应完成
                String finalResponse = fullResponseBuilder.toString();
                log.info("📝 最终累积内容长度: {}, 内容预览: {}", 
                        finalResponse.length(), 
                        finalResponse.length() > 100 ? finalResponse.substring(0, 100) + "..." : finalResponse);
                
                if (!finalResponse.isEmpty()) {
                //    pushMessageCompletedToWebSocket(request.getSessionId(), fullResponseBuilder.toString());
                    // 保存最终AI回复消息
                    log.info("💾 保存Coze AI最终回复到数据库");
                    saveAiReplyMessage(request.getSessionId(), finalResponse, userContext.getUser().getId(),totalTokens.get());
                    log.info("✅ 流式AI回复处理完成，最终内容长度: {}", finalResponse.length());
                } else {
                    log.warn("⚠️ 流式AI回复为空，会话ID: {}", request.getSessionId());
                    pushFinalCozeReplyViaWebSocket(request.getSessionId(), "抱歉，我暂时无法回答您的问题，请稍后重试。", userContext.getUser().getId());
                }
                
            } catch (Exception e) {
                log.error("❌ 调用Coze流式API失败: {}", e.getMessage(), e);
                pushFinalCozeReplyViaWebSocket(request.getSessionId(), "抱歉，AI服务暂时不可用，请稍后重试。", userContext.getUser().getId());
            }
            
        } catch (Exception e) {
            log.error("流式AI处理失败: {}", e.getMessage(), e);
            pushFinalCozeReplyViaWebSocket(request.getSessionId(), "抱歉，处理出现问题，请重试。", userContext.getUser().getId());
        }
    }
    
    /**
     * 调用Coze AI服务（带用户上下文）
     */
    private String callCozeAiServiceWithContext(EnterpriseChatMessageRequest request, String cozeBotId, LoginFrontUserVo userContext) {
        try {
            log.info("调用Coze AI服务，botId: {}, 内容: {}", cozeBotId, request.getContent());
            
            // 直接通过会话ID获取会话信息，避免依赖Spring Security上下文
            com.zbkj.common.model.chat.UnifiedChatSession session = getSessionById(request.getSessionId());
            if (session == null) {
                log.error("会话不存在: {}", request.getSessionId());
                return "抱歉，会话已失效，请重新开始对话。";
            }
            
            // 构建Coze API请求参数
            Map<String, Object> cozeRequest = new HashMap<>();
            cozeRequest.put("bot_id", cozeBotId);
            cozeRequest.put("user_id", userContext.getUser().getId().toString());
            cozeRequest.put("stream", false); // 非流式响应
            cozeRequest.put("auto_save_history", true);
            
            // 如果有Coze会话ID，则传递
            if (session.getCozeConversationId() != null && !session.getCozeConversationId().isEmpty()) {
                cozeRequest.put("conversation_id", session.getCozeConversationId());
            }
            
            // 构建消息内容
            List<Map<String, Object>> additionalMessages = new ArrayList<>();
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", request.getContent());
            userMessage.put("content_type", request.getContentType() != null ? request.getContentType() : "text");
            additionalMessages.add(userMessage);
            cozeRequest.put("additional_messages", additionalMessages);
            
            // 调用Coze服务
            Object response = cozeService.startChat(cozeRequest);
            
            if (response != null) {
                // 解析Coze响应
                return parseCozeResponse(response, session);
            } else {
                log.warn("Coze服务返回空响应");
                return "抱歉，AI服务暂时不可用，请稍后重试。";
            }
            
        } catch (Exception e) {
            log.error("调用Coze AI服务失败: {}", e.getMessage(), e);
            return "抱歉，处理您的消息时出现问题，请重试或联系客服。";
        }
    }
    
    /**
     * 直接通过会话ID获取会话（避免Spring Security上下文依赖）
     */
    private com.zbkj.common.model.chat.UnifiedChatSession getSessionById(String sessionId) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zbkj.common.model.chat.UnifiedChatSession> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(com.zbkj.common.model.chat.UnifiedChatSession::getSessionId, sessionId);
            
            return unifiedChatSessionDao.selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("获取会话失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 保存AI回复消息（避免Spring Security上下文依赖）
     */
    private void saveAiReplyMessage(String sessionId, String content, Integer userId,Integer totalTokens) {
        try {
            com.zbkj.common.model.chat.UnifiedChatMessage message = new com.zbkj.common.model.chat.UnifiedChatMessage();
            message.setMessageId("msg_" + CrmebUtil.getUuid());
            message.setSessionId(sessionId);
            message.setSenderId(userId.longValue());
            message.setSenderType("AI");
            message.setSenderName("AI助手");
            message.setReceiverType("USER");
            message.setRole("assistant");
            message.setMessageType("text");
            message.setContent(content);
            message.setTokensUsed(totalTokens);
            message.setContentType("text");
            message.setStatus("sent");
            message.setIsRead(true);
            message.setIsSystemMessage(false);
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());
            
            unifiedChatMessageDao.insert(message);
            log.info("AI回复消息已保存，消息ID: {}", message.getMessageId());
            
        } catch (Exception e) {
            log.error("保存AI回复消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 调用Coze AI服务（原方法保留兼容性）
     */
    private String callCozeAiService(EnterpriseChatMessageRequest request, String cozeBotId) {
        try {
            log.info("调用Coze AI服务，botId: {}, 内容: {}", cozeBotId, request.getContent());
            
            // 获取会话信息
            com.zbkj.common.model.chat.UnifiedChatSession session = unifiedChatService.getSession(request.getSessionId());
            if (session == null) {
                log.error("会话不存在: {}", request.getSessionId());
                return "抱歉，会话已失效，请重新开始对话。";
            }
            
            // 构建Coze API请求参数
            Map<String, Object> cozeRequest = new HashMap<>();
            cozeRequest.put("bot_id", cozeBotId);
            cozeRequest.put("user_id", session.getUserId().toString());
            cozeRequest.put("stream", false); // 非流式响应
            cozeRequest.put("auto_save_history", true);
            
            // 如果有Coze会话ID，则传递
            if (session.getCozeConversationId() != null && !session.getCozeConversationId().isEmpty()) {
                cozeRequest.put("conversation_id", session.getCozeConversationId());
            }
            
            // 构建消息内容
            List<Map<String, Object>> additionalMessages = new ArrayList<>();
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", request.getContent());
            userMessage.put("content_type", request.getContentType() != null ? request.getContentType() : "text");
            additionalMessages.add(userMessage);
            cozeRequest.put("additional_messages", additionalMessages);
            
            // 调用Coze服务
            Object response = cozeService.startChat(cozeRequest);
            
            if (response != null) {
                // 解析Coze响应
                return parseCozeResponse(response, session);
            } else {
                log.warn("Coze服务返回空响应");
                return "抱歉，AI服务暂时不可用，请稍后重试。";
            }
            
        } catch (Exception e) {
            log.error("调用Coze AI服务失败: {}", e.getMessage(), e);
            return "抱歉，处理您的消息时出现问题，请重试或联系客服。";
        }
    }
    
    /**
     * 解析Coze响应（参考EnterpriseChatController的实现）
     */
    private String parseCozeResponse(Object response, com.zbkj.common.model.chat.UnifiedChatSession session) {
        try {
            log.info("开始解析Coze响应: {}", response);
            
            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                
                // 检查是否有错误
                Integer code = (Integer) responseMap.get("code");
                if (code != null && code != 0) {
                    String msg = (String) responseMap.get("msg");
                    log.error("Coze API返回错误，code: {}, msg: {}", code, msg);
                    return "AI服务暂时不可用，请稍后重试：" + (msg != null ? msg : "未知错误");
                }
                
                Object data = responseMap.get("data");
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    
                    // 更新Coze会话ID
                    String conversationId = (String) dataMap.get("conversation_id");
                    if (conversationId != null && !conversationId.equals(session.getCozeConversationId())) {
                        session.setCozeConversationId(conversationId);
                        unifiedChatService.updateSession(session);
                        log.info("更新Coze会话ID: {}", conversationId);
                    }
                    
                    String chatId = (String) dataMap.get("id");
                    String status = (String) dataMap.get("status");
                    
                    log.info("Coze响应状态: {}, ChatID: {}, ConversationID: {}", status, chatId, conversationId);
                    
                    if (chatId != null) {
                        // 根据状态决定处理方式
                        if ("completed".equals(status)) {
                            // 直接获取消息
                            return getCozeMessages(conversationId, chatId);
                        } else {
                            // 需要轮询获取消息
                            return pollCozeMessages(conversationId, chatId);
                        }
                    }
                }
            }
            
            log.warn("无法解析Coze响应格式: {}", response);
            return "AI正在处理您的消息，请稍等...";
            
        } catch (Exception e) {
            log.error("解析Coze响应失败: {}", e.getMessage(), e);
            return "抱歉，处理AI回复时出现问题。";
        }
    }
    
    /**
     * 轮询获取Coze消息
     */
    private String pollCozeMessages(String conversationId, String chatId) {
        try {
            // 等待AI处理完成，最多等待30秒
            int maxRetries = 30;
            int retryCount = 0;
            
            while (retryCount < maxRetries) {
                Thread.sleep(1000); // 等待1秒
                
                // 检查对话状态
                Object chatDetail = cozeService.getChatDetail(conversationId, chatId);
                if (chatDetail instanceof Map) {
                    Map<String, Object> detailMap = (Map<String, Object>) chatDetail;
                    Object data = detailMap.get("data");
                    
                    if (data instanceof Map) {
                        Map<String, Object> dataMap = (Map<String, Object>) data;
                        String status = (String) dataMap.get("status");
                        
                        if ("completed".equals(status)) {
                            // 获取消息列表
                            Object messagesResponse = cozeService.getChatMessages(conversationId, chatId, 1, 20);
                            return extractAiReply(messagesResponse);
                        } else if ("failed".equals(status)) {
                            log.error("Coze对话失败");
                            return "抱歉，AI处理失败，请重试。";
                        }
                    }
                }
                
                retryCount++;
            }
            
            return "AI处理超时，请重试。";
            
        } catch (Exception e) {
            log.error("轮询Coze消息失败: {}", e.getMessage(), e);
            return "获取AI回复失败，请重试。";
        }
    }
    
    /**
     * 直接获取Coze消息（不轮询）
     */
    private String getCozeMessages(String conversationId, String chatId) {
        try {
            log.info("直接获取Coze消息，ConversationID: {}, ChatID: {}", conversationId, chatId);
            
            Object messagesResponse = cozeService.getChatMessages(conversationId, chatId, 1, 20);
            return extractAiReply(messagesResponse);
            
        } catch (Exception e) {
            log.error("直接获取Coze消息失败: {}", e.getMessage(), e);
            return "获取AI回复失败，请重试。";
        }
    }
    
    /**
     * 提取AI回复内容（参考EnterpriseChatController的实现）
     */
    private String extractAiReply(Object messagesResponse) {
        try {
            log.info("开始提取AI回复内容: {}", messagesResponse);
            
            if (messagesResponse instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) messagesResponse;
                
                // 检查API响应状态
                Integer code = (Integer) responseMap.get("code");
                if (code != null && code != 0) {
                    String msg = (String) responseMap.get("msg");
                    log.error("获取消息API返回错误，code: {}, msg: {}", code, msg);
                    return "获取AI回复失败：" + (msg != null ? msg : "未知错误");
                }
                
                Object data = responseMap.get("data");
                if (data instanceof List) {
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) data;
                    
                    log.info("获取到{}条消息", messages.size());
                    
                    // 按照EnterpriseChatController的逻辑，查找最新的assistant answer消息
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        Map<String, Object> message = messages.get(i);
                        String role = (String) message.get("role");
                        String type = (String) message.get("type");
                        String content = (String) message.get("content");
                        
                        log.debug("检查消息: role={}, type={}, content={}", role, type, 
                                content != null ? content.substring(0, Math.min(content.length(), 100)) : "null");
                        
                        // 查找assistant的answer类型消息
                        if ("assistant".equals(role) && "answer".equals(type)) {
                            if (content != null && !content.trim().isEmpty()) {
                                log.info("找到AI回复内容，长度: {}", content.length());
                                return content.trim();
                            }
                        }
                    }
                    
                    // 如果没找到answer消息，检查是否有其他assistant消息
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        Map<String, Object> message = messages.get(i);
                        String role = (String) message.get("role");
                        String content = (String) message.get("content");
                        
                        if ("assistant".equals(role) && content != null && !content.trim().isEmpty()) {
                            log.info("找到assistant消息作为回复，长度: {}", content.length());
                            return content.trim();
                        }
                    }
                }
            }
            
            log.warn("未找到有效的AI回复内容");
            return "AI暂时没有回复，请重试。";
            
        } catch (Exception e) {
            log.error("提取AI回复失败: {}", e.getMessage(), e);
            return "解析AI回复失败。";
        }
    }
    
    /**
     * 通过WebSocket推送AI回复
     */
    private void pushAiReplyViaWebSocket(String sessionId, String content, Integer userId) {
        try {
            log.info("通过WebSocket推送AI回复，会话ID: {}, 用户ID: {}", sessionId, userId);
            
            // 构建消息数据
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "ai_reply");
            messageData.put("sessionId", sessionId);
            messageData.put("content", content);
            messageData.put("messageType", "text");
            messageData.put("contentType", "text");
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("sender", "AI");
            
            // 尝试通过WebSocket发送消息给用户
            try {
                if (humanServiceWebSocketService != null) {
                    humanServiceWebSocketService.sendMessageToUser(userId, messageData);
                    log.info("AI回复已通过管理端WebSocket推送给用户: {}", userId);
                } else {
                    log.warn("管理端WebSocket服务不可用");
                }
            } catch (Exception wsException) {
                log.warn("管理端WebSocket推送失败: {}", wsException.getMessage());
            }
            
            // 同时尝试通过前端WebSocket推送（更精确的会话级推送）
            try {
                // 使用反射调用前端WebSocket控制器
                Class<?> frontControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendAiReplyMethod = frontControllerClass.getMethod("sendAiReplyToSession", String.class, Object.class);
                sendAiReplyMethod.invoke(null, sessionId, messageData);
                log.info("AI回复已通过前端WebSocket推送到会话: {}", sessionId);
            } catch (ClassNotFoundException e) {
                log.debug("前端WebSocket控制器不可用（可能在不同服务中）");
            } catch (Exception e) {
                log.warn("前端WebSocket推送失败: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("通过WebSocket推送AI回复失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 推送AI思考中状态到WebSocket
     */
    private void pushAiThinkingToWebSocket(String sessionId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "ai_thinking");
            messageData.put("sessionId", sessionId);
            messageData.put("content", "AI正在思考中，请稍等..."); // 顶级content字段
            messageData.put("timestamp", System.currentTimeMillis());
            
            // 推送到前端WebSocket
            try {
                Class<?> frontControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendAiReplyMethod = frontControllerClass.getMethod("sendAiReplyToSession", String.class, Object.class);
                sendAiReplyMethod.invoke(null, sessionId, messageData);
                log.info("✅ AI思考状态已推送到WebSocket");
            } catch (Exception e) {
                log.warn("WebSocket AI思考状态推送失败: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("构建AI思考状态失败: {}", e.getMessage());
        }
    }
    
    /**
     * 推送转人工成功消息到WebSocket
     */
    private void pushHandoverSuccessToWebSocket(String sessionId, String reason) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "transfer_success");
            messageData.put("sessionId", sessionId);
            messageData.put("content", "正在为您转接人工客服，请稍候...");
            messageData.put("reason", reason);
            messageData.put("timestamp", System.currentTimeMillis());
            
            // 推送到前端WebSocket
            try {
                Class<?> frontControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendAiReplyMethod = frontControllerClass.getMethod("sendAiReplyToSession", String.class, Object.class);
                sendAiReplyMethod.invoke(null, sessionId, messageData);
                log.info("✅ 转人工成功消息已推送到WebSocket: sessionId={}, reason={}", sessionId, reason);
            } catch (Exception e) {
                log.warn("WebSocket转人工成功消息推送失败: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("构建转人工成功消息失败: {}", e.getMessage());
        }
    }
    
    /**
     * 推送流式增量消息到WebSocket
     */
    private void pushStreamDeltaToWebSocket(String sessionId, String deltaContent) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "conversation.message.delta");
            messageData.put("sessionId", sessionId);
            messageData.put("content", deltaContent); // 顶级content字段供控制器打印
            messageData.put("timestamp", System.currentTimeMillis());
            
            // 同时提供嵌套的data字段，保持与Coze API格式一致
            Map<String, Object> data = new HashMap<>();
            data.put("content", deltaContent);
            data.put("delta", deltaContent); // 备用字段
            messageData.put("data", data);
            
            // 推送到前端WebSocket
            try {
                Class<?> frontControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendAiReplyMethod = frontControllerClass.getMethod("sendAiReplyToSession", String.class, Object.class);
                sendAiReplyMethod.invoke(null, sessionId, messageData);
                log.debug("增量消息已推送到WebSocket: {}", deltaContent.length());
            } catch (Exception e) {
                log.warn("WebSocket增量推送失败: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("构建增量消息失败: {}", e.getMessage());
        }
    }
    
    /**
     * 推送消息完成事件到WebSocket
     */
    private void pushMessageCompletedToWebSocket(String sessionId, String fullContent) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "conversation.message.completed");
            messageData.put("sessionId", sessionId);
            messageData.put("content", fullContent); // 顶级content字段供控制器打印
            messageData.put("timestamp", System.currentTimeMillis());
            
            // 同时提供嵌套的data字段，保持与Coze API格式一致
            Map<String, Object> data = new HashMap<>();
            data.put("content", fullContent);
            messageData.put("data", data);
            
            // 推送到前端WebSocket
            try {
                Class<?> frontControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendAiReplyMethod = frontControllerClass.getMethod("sendAiReplyToSession", String.class, Object.class);
                sendAiReplyMethod.invoke(null, sessionId, messageData);
                log.info("消息完成事件已推送到WebSocket，会话ID: {}", sessionId);
            } catch (Exception e) {
                log.warn("WebSocket消息完成推送失败: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("构建消息完成事件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 推送对话完成事件到WebSocket
     */
    private void pushChatCompletedToWebSocket(String sessionId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "conversation.chat.completed");
            messageData.put("sessionId", sessionId);
            messageData.put("content", "对话完成"); // 顶级content字段供控制器打印
            messageData.put("timestamp", System.currentTimeMillis());
            
            // 推送到前端WebSocket
            try {
                Class<?> frontControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendAiReplyMethod = frontControllerClass.getMethod("sendAiReplyToSession", String.class, Object.class);
                sendAiReplyMethod.invoke(null, sessionId, messageData);
                log.info("对话完成事件已推送到WebSocket，会话ID: {}", sessionId);
            } catch (Exception e) {
                log.warn("WebSocket对话完成推送失败: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("构建对话完成事件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 通过WebSocket推送最终的Coze AI回复（只推送最终结果，不推送中间状态）
     */
    private void pushFinalCozeReplyViaWebSocket(String sessionId, String content, Integer userId) {
        try {
            log.info("推送最终Coze AI回复，会话ID: {}, 用户ID: {}, 内容长度: {}", sessionId, userId, content.length());
            
            // 构建最终AI回复消息数据
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "ai_reply");
            messageData.put("sessionId", sessionId);
            messageData.put("content", content);
            messageData.put("messageType", "text");
            messageData.put("contentType", "text");
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("sender", "AI");
            messageData.put("isFinalReply", true); // 标记为最终回复
            messageData.put("source", "coze"); // 标记来源为Coze
            
            // 优先通过前端WebSocket推送（精确的会话级推送）
            boolean frontendPushSuccess = false;
            try {
                Class<?> frontControllerClass = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController");
                java.lang.reflect.Method sendAiReplyMethod = frontControllerClass.getMethod("sendAiReplyToSession", String.class, Object.class);
                sendAiReplyMethod.invoke(null, sessionId, messageData);
                log.info("最终Coze AI回复已通过前端WebSocket推送到会话: {}", sessionId);
                frontendPushSuccess = true;
            } catch (ClassNotFoundException e) {
                log.debug("前端WebSocket控制器不可用（可能在不同服务中）");
            } catch (Exception e) {
                log.warn("前端WebSocket推送失败: {}", e.getMessage());
            }
            
            // 如果前端推送失败，尝试通过管理端WebSocket推送
            if (!frontendPushSuccess && humanServiceWebSocketService != null) {
                try {
                    humanServiceWebSocketService.sendMessageToUser(userId, messageData);
                    log.info("最终Coze AI回复已通过管理端WebSocket推送给用户: {}", userId);
                } catch (Exception wsException) {
                    log.error("管理端WebSocket推送最终回复失败: {}", wsException.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("推送最终Coze AI回复失败: {}", e.getMessage(), e);
        }
    }
    
    // ==================== 转换和辅助方法 ====================

    /**
     * 转换UnifiedChatSession为HumanServiceSession（保持兼容性）
     */
    private HumanServiceSession convertToHumanServiceSession(UnifiedChatSession unifiedSession) {
        if (unifiedSession == null) return null;
        
        HumanServiceSession session = new HumanServiceSession();
        session.setId(unifiedSession.getId());
        session.setSessionId(unifiedSession.getSessionId());
        session.setEnterpriseSessionId(unifiedSession.getSessionId()); // 使用相同的会话ID
        session.setUserId(unifiedSession.getUserId());
        session.setUserType(unifiedSession.getUserType());
        session.setStaffId(unifiedSession.getStaffId());
        session.setMerId(unifiedSession.getMerId());
        
        // 会话类型转换
        if (UnifiedChatSession.SESSION_TYPE_MIXED.equals(unifiedSession.getSessionType())) {
            session.setSessionType(HumanServiceSession.SESSION_TYPE_TRANSFER);
        } else {
            session.setSessionType(HumanServiceSession.SESSION_TYPE_DIRECT);
        }
        
        session.setTransferReason(unifiedSession.getTransferReason());
        
        // 状态转换
        String sessionStatus = unifiedSession.getStatus();
        switch (sessionStatus) {
            case UnifiedChatSession.STATUS_WAITING:
                session.setSessionStatus(HumanServiceSession.SESSION_STATUS_WAITING);
                break;
            case UnifiedChatSession.STATUS_ACTIVE:
                session.setSessionStatus(HumanServiceSession.SESSION_STATUS_ACTIVE);
                break;
            case UnifiedChatSession.STATUS_ENDED:
                session.setSessionStatus(HumanServiceSession.SESSION_STATUS_ENDED);
                break;
            case UnifiedChatSession.STATUS_CLOSED:
                session.setSessionStatus(HumanServiceSession.SESSION_STATUS_CLOSED);
                break;
            default:
                session.setSessionStatus(HumanServiceSession.SESSION_STATUS_WAITING);
        }
        
        session.setPriority(unifiedSession.getPriority());
        session.setQueuePosition(unifiedSession.getQueuePosition());
        session.setWaitStartTime(unifiedSession.getWaitStartTime());
        session.setServiceStartTime(unifiedSession.getServiceStartTime());
        session.setServiceEndTime(unifiedSession.getServiceEndTime());
        session.setTotalWaitTime(unifiedSession.getTotalWaitTime());
        session.setTotalServiceTime(unifiedSession.getTotalServiceTime());
        session.setUserSatisfaction(unifiedSession.getUserSatisfaction());
        session.setFeedbackContent(unifiedSession.getFeedbackContent());
        session.setSessionSummary(unifiedSession.getSessionSummary());
        session.setTags(unifiedSession.getTags());
        session.setCreateTime(unifiedSession.getCreateTime());
        session.setUpdateTime(unifiedSession.getUpdateTime());
        
        return session;
    }
    
    /**
     * 检查当前用户是否有权限访问指定的统一会话
     */
    private boolean hasUnifiedSessionAccess(UnifiedChatSession session) {
        // 1. 商户隔离：只能访问自己商户的会话
        Integer currentMerId = getCurrentMerId();
        if (!Objects.equals(currentMerId.longValue(), session.getMerId())) {
            return false;
        }
        
        // 2. 如果是管理员，可以访问所有会话
        if (isManagerRole()) {
            return true;
        }
        
        // 3. 普通客服只能访问分配给自己的会话
        Integer currentUserId = getCurrentUserId();
        CustomerServiceStaff currentStaff = getStaffByUserId(currentUserId);
        if (currentStaff != null && session.getStaffId() != null) {
            return Objects.equals(currentStaff.getId().longValue(), session.getStaffId());
        }
        
        return false;
    }

    /**
     * 获取当前用户按商户分组的对话列表
     * @param userId 用户ID
     * @return 按商户分组的对话列表，包含商户信息和对话消息
     */
    @Override
    public List<Map<String, Object>> getUserConversationsGroupedByMerchant(Integer userId) {
        try {
            log.info("开始获取用户对话列表，用户ID: {}", userId);
            
            // 1. 获取用户的所有会话，按商户ID分组
            LambdaQueryWrapper<UnifiedChatSession> sessionWrapper = new LambdaQueryWrapper<>();
            sessionWrapper.eq(UnifiedChatSession::getUserId, userId.longValue())
                          .eq(UnifiedChatSession::getUserType, UnifiedChatSession.USER_TYPE_CUSTOMER)
                          .orderByDesc(UnifiedChatSession::getLastMessageTime);
            
            List<UnifiedChatSession> allSessions = unifiedChatSessionDao.selectList(sessionWrapper);
            log.info("找到用户 {} 的会话数量: {}", userId, allSessions.size());
            
            if (allSessions.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 按商户ID分组
            Map<Long, List<UnifiedChatSession>> sessionsByMerchant = allSessions.stream()
                    .collect(Collectors.groupingBy(UnifiedChatSession::getMerId));
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 3. 为每个商户构建数据
            for (Map.Entry<Long, List<UnifiedChatSession>> entry : sessionsByMerchant.entrySet()) {
                Long merId = entry.getKey();
                List<UnifiedChatSession> merchantSessions = entry.getValue();
                
                log.info("处理商户 {} 的会话，数量: {}", merId, merchantSessions.size());
                
                // 获取商户信息
                Merchant merchant = merchantService.getById(merId.intValue());
                if (merchant == null) {
                    log.warn("商户不存在，跳过商户ID: {}", merId);
                    continue;
                }
                
                Map<String, Object> merchantData = new HashMap<>();
                merchantData.put("merchantId", merchant.getId());
                merchantData.put("merchantName", merchant.getName());
                merchantData.put("merchantAvatar", merchant.getAvatar());
                merchantData.put("merchantIntro", merchant.getIntro());
                
                // 获取该商户下所有会话的消息
                List<Map<String, Object>> conversationsData = new ArrayList<>();
                
                for (UnifiedChatSession session : merchantSessions) {
                    Map<String, Object> conversationData = new HashMap<>();
                    conversationData.put("sessionId", session.getSessionId());
                    conversationData.put("sessionTitle", session.getSessionTitle());
                    conversationData.put("sessionType", session.getSessionType());
                    conversationData.put("currentServiceType", session.getCurrentServiceType());
                    conversationData.put("status", session.getStatus());
                    conversationData.put("createTime", session.getCreateTime());
                    conversationData.put("lastMessageTime", session.getLastMessageTime());
                    conversationData.put("lastMessageContent", session.getLastMessageContent());
                    conversationData.put("totalMessages", session.getTotalMessages());
                    
                    // 获取该会话的消息列表（最近20条）
                    LambdaQueryWrapper<com.zbkj.common.model.chat.UnifiedChatMessage> messageWrapper = 
                            new LambdaQueryWrapper<>();
                    messageWrapper.eq(com.zbkj.common.model.chat.UnifiedChatMessage::getSessionId, session.getSessionId())
                                 .eq(com.zbkj.common.model.chat.UnifiedChatMessage::getIsClear, false)
                                 .orderByDesc(com.zbkj.common.model.chat.UnifiedChatMessage::getCreateTime)
                                 .last("LIMIT 20");
                    
                    List<com.zbkj.common.model.chat.UnifiedChatMessage> messages = 
                            unifiedChatMessageDao.selectList(messageWrapper);
                    
                    // 转换消息格式
                    List<Map<String, Object>> messagesData = messages.stream()
                            .map(this::convertMessageToMap)
                            .collect(Collectors.toList());
                    
                    // 按时间正序排列（最新的在后面）
                    Collections.reverse(messagesData);
                    
                    conversationData.put("messages", messagesData);
                    conversationData.put("messageCount", messages.size());
                    
                    conversationsData.add(conversationData);
                }
                
                // 按最后消息时间排序（最新的在前面）
                conversationsData.sort((a, b) -> {
                    Date timeA = (Date) a.get("lastMessageTime");
                    Date timeB = (Date) b.get("lastMessageTime");
                    if (timeA == null && timeB == null) return 0;
                    if (timeA == null) return 1;
                    if (timeB == null) return -1;
                    return timeB.compareTo(timeA);
                });
                
                merchantData.put("conversations", conversationsData);
                merchantData.put("conversationCount", conversationsData.size());
                
                // 计算总消息数
                int totalMessages = conversationsData.stream()
                        .mapToInt(conv -> (Integer) conv.get("messageCount"))
                        .sum();
                merchantData.put("totalMessages", totalMessages);
                
                result.add(merchantData);
            }
            
            // 按商户的最新消息时间排序
            result.sort((a, b) -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> conversationsA = (List<Map<String, Object>>) a.get("conversations");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> conversationsB = (List<Map<String, Object>>) b.get("conversations");
                
                Date latestA = conversationsA.isEmpty() ? null : 
                        (Date) conversationsA.get(0).get("lastMessageTime");
                Date latestB = conversationsB.isEmpty() ? null : 
                        (Date) conversationsB.get(0).get("lastMessageTime");
                
                if (latestA == null && latestB == null) return 0;
                if (latestA == null) return 1;
                if (latestB == null) return -1;
                return latestB.compareTo(latestA);
            });
            
            log.info("用户 {} 的对话列表构建完成，商户数量: {}", userId, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取用户对话列表失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new CrmebException("获取对话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 将消息对象转换为Map格式
     */
    private Map<String, Object> convertMessageToMap(com.zbkj.common.model.chat.UnifiedChatMessage message) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", message.getMessageId());
        messageData.put("senderId", message.getSenderId());
        messageData.put("senderType", message.getSenderType());
        messageData.put("senderName", message.getSenderName());
        messageData.put("senderAvatar", message.getSenderAvatar());
        messageData.put("receiverId", message.getReceiverId());
        messageData.put("receiverType", message.getReceiverType());
        messageData.put("role", message.getRole());
        messageData.put("messageType", message.getMessageType());
        messageData.put("content", message.getContent());
        messageData.put("contentType", message.getContentType());
        messageData.put("attachments", message.getAttachments());
        messageData.put("status", message.getStatus());
        messageData.put("isRead", message.getIsRead());
        messageData.put("readTime", message.getReadTime());
        messageData.put("isSystemMessage", message.getIsSystemMessage());
        messageData.put("createTime", message.getCreateTime());
        messageData.put("updateTime", message.getUpdateTime());
        return messageData;
    }
}
