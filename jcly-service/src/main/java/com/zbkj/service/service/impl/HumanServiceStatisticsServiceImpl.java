package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.model.humanservice.HumanServiceRating;
import com.zbkj.common.response.humanservice.HumanServiceStatisticsResponse;
import com.zbkj.service.dao.chat.UnifiedChatMessageDao;
import com.zbkj.service.dao.chat.UnifiedChatSessionDao;
import com.zbkj.service.dao.humanservice.HumanServiceRatingDao;
import com.zbkj.service.service.HumanServiceStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人工客服数据统计服务实现
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class HumanServiceStatisticsServiceImpl implements HumanServiceStatisticsService {

    @Autowired
    private UnifiedChatMessageDao messageDao;

    @Autowired
    private UnifiedChatSessionDao sessionDao;

    @Autowired
    private HumanServiceRatingDao ratingDao;

    @Override
    public HumanServiceStatisticsResponse getComprehensiveStatistics(Long merId, Integer days) {
        log.info("获取人工客服综合统计数据，商户ID: {}, 统计天数: {}", merId, days);
        
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        HumanServiceStatisticsResponse response = new HumanServiceStatisticsResponse();
        
        // 获取各类统计数据
        response.setOverall(getOverallStatistics(merId, days));
        response.setStaffReplyStats(getStaffReplyStatistics(merId, days));
        response.setDailyMessageStats(getDailyMessageStatistics(merId, days));
        response.setMessageTypeStats(getMessageTypeStatistics(merId, days));
        response.setStaffRatingStats(getStaffRatingStatistics(merId, days));
        response.setPopularQuestions(getPopularQuestionStatistics(merId, days, 10));
        
        return response;
    }

    @Override
    public List<HumanServiceStatisticsResponse.StaffReplyStatistics> getStaffReplyStatistics(Long merId, Integer days) {
        log.info("获取客服回复统计数据，商户ID: {}, 统计天数: {}", merId, days);
        
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        Date todayStart = new Date(System.currentTimeMillis() - System.currentTimeMillis() % (24 * 60 * 60 * 1000L));
        Date weekStart = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
        Date monthStart = new Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L);
        
        // 获取该商户的所有会话
        List<String> sessionIds = getSessionIdsByMerId(merId);
        if (sessionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询客服回复的消息
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UnifiedChatMessage::getSessionId, sessionIds)
                   .eq(UnifiedChatMessage::getSenderType, "STAFF")
                   .ge(UnifiedChatMessage::getCreateTime, startTime)
                   .orderByDesc(UnifiedChatMessage::getCreateTime);
        
        List<UnifiedChatMessage> staffMessages = messageDao.selectList(queryWrapper);
        
        // 按客服ID分组统计
        Map<Long, List<UnifiedChatMessage>> staffMessageMap = staffMessages.stream()
                .filter(msg -> msg.getSenderId() != null)
                .collect(Collectors.groupingBy(UnifiedChatMessage::getSenderId));
        
        List<HumanServiceStatisticsResponse.StaffReplyStatistics> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<UnifiedChatMessage>> entry : staffMessageMap.entrySet()) {
            Long staffId = entry.getKey();
            List<UnifiedChatMessage> messages = entry.getValue();
            
            HumanServiceStatisticsResponse.StaffReplyStatistics stats = 
                new HumanServiceStatisticsResponse.StaffReplyStatistics();
            
            stats.setStaffId(staffId);
            stats.setStaffName(getStaffName(messages.get(0).getSenderName()));
            stats.setStaffAvatar(""); // 可以从用户表获取
            
            // 统计各时间段的回复数
            stats.setTodayReplies(messages.stream()
                    .filter(msg -> msg.getCreateTime().after(todayStart))
                    .count());
            
            stats.setWeekReplies(messages.stream()
                    .filter(msg -> msg.getCreateTime().after(weekStart))
                    .count());
            
            stats.setMonthReplies(messages.stream()
                    .filter(msg -> msg.getCreateTime().after(monthStart))
                    .count());
            
            // 计算平均响应时间（基于实际消息时间差）
            BigDecimal avgResponseTime = calculateStaffResponseTimeForStats(staffId, sessionIds, startTime);
            stats.setAvgResponseTime(avgResponseTime);
            
            // 统计处理的会话数
            Set<String> handledSessionIds = messages.stream()
                    .map(UnifiedChatMessage::getSessionId)
                    .collect(Collectors.toSet());
            stats.setHandledSessions((long) handledSessionIds.size());
            
            // 客户满意度（从评价表获取真实数据）
            BigDecimal satisfactionScore = calculateStaffSatisfactionForStats(merId, staffId, days);
            stats.setSatisfactionScore(satisfactionScore);
            
            // 在线时长（可以后续从工作记录表获取，暂时使用固定值）
            stats.setOnlineMinutes(calculateStaffOnlineTime(staffId, days));
            
            result.add(stats);
        }
        
        // 按回复数排序
        result.sort((a, b) -> Long.compare(b.getTodayReplies(), a.getTodayReplies()));
        
        return result;
    }

    @Override
    public List<HumanServiceStatisticsResponse.DailyMessageStatistics> getDailyMessageStatistics(Long merId, Integer days) {
        log.info("获取每日消息统计数据，商户ID: {}, 统计天数: {}", merId, days);
        
        List<String> sessionIds = getSessionIdsByMerId(merId);
        if (sessionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UnifiedChatMessage::getSessionId, sessionIds)
                   .ge(UnifiedChatMessage::getCreateTime, startTime)
                   .orderByAsc(UnifiedChatMessage::getCreateTime);
        
        List<UnifiedChatMessage> messages = messageDao.selectList(queryWrapper);
        
        // 按日期分组统计
        Map<String, List<UnifiedChatMessage>> dailyMessageMap = messages.stream()
                .collect(Collectors.groupingBy(msg -> {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(msg.getCreateTime());
                }));
        
        List<HumanServiceStatisticsResponse.DailyMessageStatistics> result = new ArrayList<>();
        
        // 生成每一天的数据（包括没有消息的日期）
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<UnifiedChatMessage> dayMessages = dailyMessageMap.getOrDefault(date, new ArrayList<>());
            
            HumanServiceStatisticsResponse.DailyMessageStatistics stats = 
                new HumanServiceStatisticsResponse.DailyMessageStatistics();
            
            stats.setDate(date);
            
            // 分类统计
            long userMessages = dayMessages.stream()
                    .filter(msg -> "USER".equals(msg.getSenderType()))
                    .count();
            
            long staffReplies = dayMessages.stream()
                    .filter(msg -> "STAFF".equals(msg.getSenderType()))
                    .count();
            
            long aiReplies = dayMessages.stream()
                    .filter(msg -> "AI".equals(msg.getSenderType()))
                    .count();
            
            stats.setUserMessages(userMessages);
            stats.setStaffReplies(staffReplies);
            stats.setAiReplies(aiReplies);
            stats.setTotalMessages((long) dayMessages.size());
            
            result.add(stats);
        }
        
        return result;
    }

    @Override
    public List<HumanServiceStatisticsResponse.MessageTypeStatistics> getMessageTypeStatistics(Long merId, Integer days) {
        log.info("获取消息类型分布统计，商户ID: {}, 统计天数: {}", merId, days);
        
        List<String> sessionIds = getSessionIdsByMerId(merId);
        if (sessionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UnifiedChatMessage::getSessionId, sessionIds)
                   .ge(UnifiedChatMessage::getCreateTime, startTime);
        
        List<UnifiedChatMessage> messages = messageDao.selectList(queryWrapper);
        
        // 按消息类型分组统计
        Map<String, Long> typeCountMap = messages.stream()
                .collect(Collectors.groupingBy(
                    msg -> msg.getMessageType() != null ? msg.getMessageType() : "text",
                    Collectors.counting()));
        
        long totalCount = messages.size();
        List<HumanServiceStatisticsResponse.MessageTypeStatistics> result = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : typeCountMap.entrySet()) {
            HumanServiceStatisticsResponse.MessageTypeStatistics stats = 
                new HumanServiceStatisticsResponse.MessageTypeStatistics();
            
            String messageType = entry.getKey();
            Long count = entry.getValue();
            
            stats.setMessageType(messageType);
            stats.setMessageTypeName(getMessageTypeName(messageType));
            stats.setCount(count);
            
            if (totalCount > 0) {
                BigDecimal percentage = new BigDecimal(count * 100.0 / totalCount)
                        .setScale(2, RoundingMode.HALF_UP);
                stats.setPercentage(percentage);
            } else {
                stats.setPercentage(BigDecimal.ZERO);
            }
            
            result.add(stats);
        }
        
        // 按数量排序
        result.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        
        return result;
    }

    @Override
    public List<HumanServiceStatisticsResponse.StaffRatingStatistics> getStaffRatingStatistics(Long merId, Integer days) {
        log.info("获取客服评价统计数据，商户ID: {}, 统计天数: {}", merId, days);
        
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        // 计算查询时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        // 查询指定商户和时间范围内的评价数据
        LambdaQueryWrapper<HumanServiceRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HumanServiceRating::getMerId, merId)
                   .ge(HumanServiceRating::getCreateTime, startTime)
                   .eq(HumanServiceRating::getStatus, HumanServiceRating.STATUS_APPROVED); // 只统计已通过的评价
        
        List<HumanServiceRating> ratings = ratingDao.selectList(queryWrapper);
        
        if (ratings.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按客服ID分组统计
        Map<Long, List<HumanServiceRating>> staffRatingMap = ratings.stream()
                .filter(rating -> rating.getStaffId() != null)
                .collect(Collectors.groupingBy(HumanServiceRating::getStaffId));
        
        List<HumanServiceStatisticsResponse.StaffRatingStatistics> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<HumanServiceRating>> entry : staffRatingMap.entrySet()) {
            Long staffId = entry.getKey();
            List<HumanServiceRating> staffRatings = entry.getValue();
            
            HumanServiceStatisticsResponse.StaffRatingStatistics stats = 
                new HumanServiceStatisticsResponse.StaffRatingStatistics();
            
            stats.setStaffId(staffId);
            // 使用第一个评价记录中的客服姓名，如果为空则显示客服ID
            String staffName = staffRatings.get(0).getStaffName();
            stats.setStaffName(StringUtils.hasText(staffName) ? staffName : "客服" + staffId);
            stats.setStaffAvatar(""); // 头像字段可以后续从用户表获取
            
            // 统计各类评价数量
            long totalRatings = staffRatings.size();
            long goodRatings = staffRatings.stream()
                    .filter(rating -> rating.getRatingType() != null && rating.getRatingType().equals(HumanServiceRating.RATING_TYPE_GOOD))
                    .count();
            long averageRatings = staffRatings.stream()
                    .filter(rating -> rating.getRatingType() != null && rating.getRatingType().equals(HumanServiceRating.RATING_TYPE_AVERAGE))
                    .count();
            long badRatings = staffRatings.stream()
                    .filter(rating -> rating.getRatingType() != null && rating.getRatingType().equals(HumanServiceRating.RATING_TYPE_BAD))
                    .count();
            
            stats.setTotalRatings(totalRatings);
            stats.setGoodRatings(goodRatings);
            stats.setAverageRatings(averageRatings);
            stats.setBadRatings(badRatings);
            
            // 计算好评率
            BigDecimal goodRatingRate = BigDecimal.ZERO;
            if (totalRatings > 0) {
                goodRatingRate = new BigDecimal(goodRatings * 100.0 / totalRatings)
                        .setScale(2, RoundingMode.HALF_UP);
            }
            stats.setGoodRatingRate(goodRatingRate);
            
            // 计算平均评分
            BigDecimal avgScore = staffRatings.stream()
                    .filter(rating -> rating.getScore() != null)
                    .map(HumanServiceRating::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(staffRatings.size()), 2, RoundingMode.HALF_UP);
            stats.setAvgScore(avgScore);
            
            result.add(stats);
        }
        
        // 按好评率降序排序
        result.sort((a, b) -> b.getGoodRatingRate().compareTo(a.getGoodRatingRate()));
        
        // 设置排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        
        return result;
    }

    @Override
    public List<HumanServiceStatisticsResponse.PopularQuestionStatistics> getPopularQuestionStatistics(Long merId, Integer days, Integer limit) {
        log.info("获取热门问题统计，商户ID: {}, 统计天数: {}, 限制数量: {}", merId, days, limit);
        
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        if (limit == null || limit <= 0) {
            limit = 10; // 默认前10个
        }
        
        // 获取该商户的所有会话ID
        List<String> sessionIds = getSessionIdsByMerId(merId);
        if (sessionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 计算查询时间范围
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        
        // 查询用户发送的文本消息
        LambdaQueryWrapper<UnifiedChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UnifiedChatMessage::getSessionId, sessionIds)
                   .eq(UnifiedChatMessage::getSenderType, UnifiedChatMessage.SENDER_TYPE_USER)
                   .eq(UnifiedChatMessage::getMessageType, UnifiedChatMessage.MESSAGE_TYPE_TEXT)
                   .ge(UnifiedChatMessage::getCreateTime, startTime)
                   .isNotNull(UnifiedChatMessage::getContent)
                   .ne(UnifiedChatMessage::getContent, "")
                   .select(UnifiedChatMessage::getContent, UnifiedChatMessage::getCreateTime, UnifiedChatMessage::getSessionId);
        
        List<UnifiedChatMessage> userMessages = messageDao.selectList(queryWrapper);
        
        if (userMessages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 定义常见的问题关键词
        String[] keywords = {
            "订单", "退款", "物流", "配送", "发货", "售后", "优惠券", "账户", "支付", "商品", 
            "发票", "客服", "价格", "库存", "规格", "尺寸", "颜色", "质量", "保修", "维修",
            "取消", "修改", "换货", "退货", "投诉", "建议", "咨询", "帮助", "登录", "注册"
        };
        
        // 统计关键词出现次数和相关信息
        Map<String, KeywordStats> keywordStatsMap = new HashMap<>();
        
        for (UnifiedChatMessage message : userMessages) {
            String content = message.getContent();
            if (content == null || content.trim().isEmpty()) {
                continue;
            }
            
            // 清理内容，去除特殊字符，保留中文、英文和数字
            String cleanContent = content.replaceAll("[^\\u4e00-\\u9fa5\\w\\s]", "").toLowerCase();
            
            for (String keyword : keywords) {
                if (content.contains(keyword)) {
                    KeywordStats stats = keywordStatsMap.computeIfAbsent(keyword, k -> new KeywordStats());
                    stats.count++;
                    stats.sessionIds.add(message.getSessionId());
                    
                    // 记录消息创建时间用于计算平均解决时间
                    stats.messageTimes.add(message.getCreateTime());
                }
            }
        }
        
        // 计算每个关键词的平均解决时间（简化版本：使用会话的后续消息来估算）
        for (Map.Entry<String, KeywordStats> entry : keywordStatsMap.entrySet()) {
            KeywordStats stats = entry.getValue();
            if (!stats.sessionIds.isEmpty()) {
                // 简单计算平均解决时间（假设为会话中消息之间的平均间隔）
                stats.avgSolutionTime = calculateAvgSolutionTime(stats.sessionIds);
            }
        }
        
        // 转换为结果列表并排序
        List<HumanServiceStatisticsResponse.PopularQuestionStatistics> result = new ArrayList<>();
        long totalMessages = userMessages.size();
        
        List<Map.Entry<String, KeywordStats>> sortedEntries = keywordStatsMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().count, a.getValue().count))
                .limit(limit)
                .collect(Collectors.toList());
        
        for (Map.Entry<String, KeywordStats> entry : sortedEntries) {
            String keyword = entry.getKey();
            KeywordStats stats = entry.getValue();
            
            HumanServiceStatisticsResponse.PopularQuestionStatistics questionStats = 
                new HumanServiceStatisticsResponse.PopularQuestionStatistics();
            
            questionStats.setKeyword(keyword);
            questionStats.setCount(stats.count);
            
            // 计算百分比
            BigDecimal percentage = totalMessages > 0 ? 
                new BigDecimal(stats.count * 100.0 / totalMessages).setScale(2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            questionStats.setPercentage(percentage);
            
            questionStats.setAvgSolutionTime(stats.avgSolutionTime);
            
            result.add(questionStats);
        }
        
        return result;
    }
    
    /**
     * 内部类：关键词统计信息
     */
    private static class KeywordStats {
        long count = 0;
        Set<String> sessionIds = new HashSet<>();
        List<Date> messageTimes = new ArrayList<>();
        BigDecimal avgSolutionTime = BigDecimal.ZERO;
    }
    
    /**
     * 计算平均解决时间（基于真实会话数据）
     */
    private BigDecimal calculateAvgSolutionTime(Set<String> sessionIds) {
        if (sessionIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 查询这些会话的服务时间数据
        LambdaQueryWrapper<UnifiedChatSession> query = new LambdaQueryWrapper<>();
        query.in(UnifiedChatSession::getSessionId, sessionIds)
             .isNotNull(UnifiedChatSession::getTotalServiceTime)
             .gt(UnifiedChatSession::getTotalServiceTime, 0);
        
        List<UnifiedChatSession> sessions = sessionDao.selectList(query);
        
        if (sessions.isEmpty()) {
            // 如果没有服务时间数据，基于会话的消息时间来估算
            return calculateSolutionTimeByMessages(sessionIds);
        }
        
        // 计算平均服务时间（秒转为分钟）
        double avgServiceTimeSeconds = sessions.stream()
                .mapToInt(UnifiedChatSession::getTotalServiceTime)
                .average()
                .orElse(0.0);
        
        double avgServiceTimeMinutes = avgServiceTimeSeconds / 60.0;
        return new BigDecimal(avgServiceTimeMinutes).setScale(1, RoundingMode.HALF_UP);
    }
    
    /**
     * 基于消息时间估算解决时间
     */
    private BigDecimal calculateSolutionTimeByMessages(Set<String> sessionIds) {
        // 查询每个会话的第一条和最后一条消息
        LambdaQueryWrapper<UnifiedChatMessage> query = new LambdaQueryWrapper<>();
        query.in(UnifiedChatMessage::getSessionId, sessionIds)
             .orderByAsc(UnifiedChatMessage::getSessionId, UnifiedChatMessage::getCreateTime);
        
        List<UnifiedChatMessage> messages = messageDao.selectList(query);
        
        if (messages.isEmpty()) {
            return new BigDecimal("5.0"); // 默认5分钟
        }
        
        // 按会话ID分组，计算每个会话的持续时间
        Map<String, List<UnifiedChatMessage>> sessionMessagesMap = messages.stream()
                .collect(Collectors.groupingBy(UnifiedChatMessage::getSessionId));
        
        List<Long> durations = new ArrayList<>();
        for (List<UnifiedChatMessage> sessionMessages : sessionMessagesMap.values()) {
            if (sessionMessages.size() >= 2) {
                UnifiedChatMessage first = sessionMessages.get(0);
                UnifiedChatMessage last = sessionMessages.get(sessionMessages.size() - 1);
                long duration = last.getCreateTime().getTime() - first.getCreateTime().getTime();
                durations.add(duration / 1000 / 60); // 转换为分钟
            }
        }
        
        if (durations.isEmpty()) {
            return new BigDecimal("5.0"); // 默认5分钟
        }
        
        double avgMinutes = durations.stream().mapToLong(Long::longValue).average().orElse(5.0);
        return new BigDecimal(avgMinutes).setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public Map<String, Object> getStaffEfficiencyStatistics(Long merId, Long staffId, Integer days) {
        log.info("获取客服工作效率统计，商户ID: {}, 客服ID: {}, 统计天数: {}", merId, staffId, days);
        
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取该商户的所有会话ID
        List<String> sessionIds = getSessionIdsByMerId(merId);
        if (sessionIds.isEmpty()) {
            // 返回默认值
            result.put("avgResponseTime", 0);
            result.put("avgSessionDuration", 0);
            result.put("resolutionRate", 0.0);
            result.put("customerSatisfaction", 0.0);
            result.put("messagesPerHour", 0);
            result.put("activeSessions", 0);
            return result;
        }
        
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        
        // 1. 计算平均响应时间
        double avgResponseTime = calculateStaffAvgResponseTime(sessionIds, staffId, startTime);
        
        // 2. 计算平均会话时长
        double avgSessionDuration = calculateStaffAvgSessionDuration(sessionIds, staffId, startTime);
        
        // 3. 计算问题解决率
        double resolutionRate = calculateStaffResolutionRate(merId, staffId, days);
        
        // 4. 计算客户满意度
        double customerSatisfaction = calculateStaffSatisfaction(merId, staffId, days);
        
        // 5. 计算每小时消息数
        double messagesPerHour = calculateStaffMessagesPerHour(sessionIds, staffId, startTime, days);
        
        // 6. 计算活跃会话数
        int activeSessions = calculateStaffActiveSessions(sessionIds, staffId);
        
        result.put("avgResponseTime", Math.round(avgResponseTime)); // 平均响应时间（秒）
        result.put("avgSessionDuration", Math.round(avgSessionDuration)); // 平均会话时长（秒）
        result.put("resolutionRate", Math.round(resolutionRate * 100.0) / 100.0); // 问题解决率（%）
        result.put("customerSatisfaction", Math.round(customerSatisfaction * 100.0) / 100.0); // 客户满意度
        result.put("messagesPerHour", Math.round(messagesPerHour)); // 每小时消息数
        result.put("activeSessions", activeSessions); // 活跃会话数
        
        return result;
    }
    
    /**
     * 计算客服平均响应时间
     */
    private double calculateStaffAvgResponseTime(List<String> sessionIds, Long staffId, Date startTime) {
        // 查询该客服参与的消息
        LambdaQueryWrapper<UnifiedChatMessage> query = new LambdaQueryWrapper<>();
        query.in(UnifiedChatMessage::getSessionId, sessionIds)
             .in(UnifiedChatMessage::getSenderType, Arrays.asList(UnifiedChatMessage.SENDER_TYPE_USER, "STAFF"))
             .ge(UnifiedChatMessage::getCreateTime, startTime)
             .orderByAsc(UnifiedChatMessage::getSessionId, UnifiedChatMessage::getCreateTime);
        
        List<UnifiedChatMessage> messages = messageDao.selectList(query);
        
        List<Long> responseTimes = new ArrayList<>();
        for (int i = 0; i < messages.size() - 1; i++) {
            UnifiedChatMessage current = messages.get(i);
            UnifiedChatMessage next = messages.get(i + 1);
            
            if (UnifiedChatMessage.SENDER_TYPE_USER.equals(current.getSenderType()) &&
                "STAFF".equals(next.getSenderType()) &&
                current.getSessionId().equals(next.getSessionId()) &&
                staffId.equals(next.getSenderId())) {
                
                long responseTime = next.getCreateTime().getTime() - current.getCreateTime().getTime();
                responseTimes.add(responseTime / 1000); // 转换为秒
            }
        }
        
        return responseTimes.isEmpty() ? 180.0 : 
               responseTimes.stream().mapToLong(Long::longValue).average().orElse(180.0);
    }
    
    /**
     * 计算客服平均会话时长
     */
    private double calculateStaffAvgSessionDuration(List<String> sessionIds, Long staffId, Date startTime) {
        // 查询该客服参与的会话
        LambdaQueryWrapper<UnifiedChatSession> query = new LambdaQueryWrapper<>();
        query.in(UnifiedChatSession::getSessionId, sessionIds)
             .eq(UnifiedChatSession::getStaffId, staffId)
             .ge(UnifiedChatSession::getCreateTime, startTime)
             .isNotNull(UnifiedChatSession::getTotalServiceTime);
        
        List<UnifiedChatSession> sessions = sessionDao.selectList(query);
        
        if (sessions.isEmpty()) {
            return 0.0;
        }
        
        double avgDuration = sessions.stream()
                .filter(session -> session.getTotalServiceTime() != null)
                .mapToInt(UnifiedChatSession::getTotalServiceTime)
                .average()
                .orElse(0.0);
        
        return avgDuration;
    }
    
    /**
     * 计算客服问题解决率
     */
    private double calculateStaffResolutionRate(Long merId, Long staffId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        // 查询该客服的评价数据
        LambdaQueryWrapper<HumanServiceRating> ratingQuery = new LambdaQueryWrapper<>();
        ratingQuery.eq(HumanServiceRating::getMerId, merId)
                  .eq(HumanServiceRating::getStaffId, staffId)
                  .ge(HumanServiceRating::getCreateTime, startTime)
                  .eq(HumanServiceRating::getStatus, HumanServiceRating.STATUS_APPROVED);
        
        List<HumanServiceRating> ratings = ratingDao.selectList(ratingQuery);
        
        if (ratings.isEmpty()) {
            return 0.0;
        }
        
        long goodRatings = ratings.stream()
                .filter(rating -> rating.getScore() != null && rating.getScore().compareTo(new BigDecimal("4")) >= 0)
                .count();
        
        return (double) goodRatings / ratings.size() * 100;
    }
    
    /**
     * 计算客服客户满意度
     */
    private double calculateStaffSatisfaction(Long merId, Long staffId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        // 查询该客服的评价数据
        LambdaQueryWrapper<HumanServiceRating> ratingQuery = new LambdaQueryWrapper<>();
        ratingQuery.eq(HumanServiceRating::getMerId, merId)
                  .eq(HumanServiceRating::getStaffId, staffId)
                  .ge(HumanServiceRating::getCreateTime, startTime)
                  .eq(HumanServiceRating::getStatus, HumanServiceRating.STATUS_APPROVED)
                  .isNotNull(HumanServiceRating::getScore);
        
        List<HumanServiceRating> ratings = ratingDao.selectList(ratingQuery);
        
        if (ratings.isEmpty()) {
            return 0.0;
        }
        
        return ratings.stream()
                .mapToDouble(rating -> rating.getScore().doubleValue())
                .average()
                .orElse(0.0);
    }
    
    /**
     * 计算客服每小时消息数
     */
    private double calculateStaffMessagesPerHour(List<String> sessionIds, Long staffId, Date startTime, Integer days) {
        // 查询该客服的消息数
        LambdaQueryWrapper<UnifiedChatMessage> query = new LambdaQueryWrapper<>();
        query.in(UnifiedChatMessage::getSessionId, sessionIds)
             .eq(UnifiedChatMessage::getSenderId, staffId)
             .eq(UnifiedChatMessage::getSenderType, "STAFF")
             .ge(UnifiedChatMessage::getCreateTime, startTime);
        
        int messageCount = messageDao.selectCount(query);
        
        // 计算总小时数
        double totalHours = days * 24.0;
        
        return totalHours > 0 ? messageCount / totalHours : 0.0;
    }
    
    /**
     * 计算客服活跃会话数
     */
    private int calculateStaffActiveSessions(List<String> sessionIds, Long staffId) {
        // 查询该客服当前活跃的会话数
        LambdaQueryWrapper<UnifiedChatSession> query = new LambdaQueryWrapper<>();
        query.in(UnifiedChatSession::getSessionId, sessionIds)
             .eq(UnifiedChatSession::getStaffId, staffId)
             .in(UnifiedChatSession::getStatus, Arrays.asList(
                 UnifiedChatSession.STATUS_ACTIVE,
                 UnifiedChatSession.STATUS_WAITING));
        
        return sessionDao.selectCount(query);
    }

    /**
     * 计算客服响应时间（用于统计页面）
     */
    private BigDecimal calculateStaffResponseTimeForStats(Long staffId, List<String> sessionIds, Date startTime) {
        double avgSeconds = calculateStaffAvgResponseTime(sessionIds, staffId, startTime);
        return new BigDecimal(avgSeconds).setScale(1, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算客服满意度评分（用于统计页面）
     */
    private BigDecimal calculateStaffSatisfactionForStats(Long merId, Long staffId, Integer days) {
        double satisfaction = calculateStaffSatisfaction(merId, staffId, days);
        return new BigDecimal(satisfaction).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算客服在线时长（简化版本，基于消息活跃度估算）
     */
    private Long calculateStaffOnlineTime(Long staffId, Integer days) {
        // 简化计算：基于客服发送消息的时间分布来估算在线时长
        // 实际项目中可以从工作记录表或登录日志表获取准确数据
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        
        LambdaQueryWrapper<UnifiedChatMessage> query = new LambdaQueryWrapper<>();
        query.eq(UnifiedChatMessage::getSenderId, staffId)
             .eq(UnifiedChatMessage::getSenderType, "STAFF")
             .ge(UnifiedChatMessage::getCreateTime, startTime);
        
        int messageCount = messageDao.selectCount(query);
        
        // 简单估算：假设每小时发送10条消息，每天工作8小时
        if (messageCount == 0) {
            return 0L;
        }
        
        // 估算在线时长（分钟）
        long estimatedMinutes = (long) (messageCount * 6); // 每条消息平均6分钟的活跃时间
        long maxDailyMinutes = days * 8 * 60; // 每天最多8小时
        
        return Math.min(estimatedMinutes, maxDailyMinutes);
    }

    @Override
    public List<Map<String, Object>> getSatisfactionTrend(Long merId, Integer days) {
        log.info("获取客户满意度趋势，商户ID: {}, 统计天数: {}", merId, days);
        
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 计算查询时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        // 查询评价数据按日期分组
        LambdaQueryWrapper<HumanServiceRating> ratingQuery = new LambdaQueryWrapper<>();
        ratingQuery.eq(HumanServiceRating::getMerId, merId)
                  .ge(HumanServiceRating::getCreateTime, startTime)
                  .eq(HumanServiceRating::getStatus, HumanServiceRating.STATUS_APPROVED)
                  .isNotNull(HumanServiceRating::getScore)
                  .orderByAsc(HumanServiceRating::getCreateTime);
        
        List<HumanServiceRating> ratings = ratingDao.selectList(ratingQuery);
        
        // 按日期分组评价数据
        Map<String, List<HumanServiceRating>> dailyRatingsMap = ratings.stream()
                .collect(Collectors.groupingBy(rating -> {
                    LocalDate date = rating.getCreateTime().toLocalDate();
                    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }));
        
        // 查询会话满意度数据
        List<String> sessionIds = getSessionIdsByMerId(merId);
        Map<String, List<Integer>> dailySessionSatisfactionMap = new HashMap<>();
        
        if (!sessionIds.isEmpty()) {
            LambdaQueryWrapper<UnifiedChatSession> sessionQuery = new LambdaQueryWrapper<>();
            sessionQuery.in(UnifiedChatSession::getSessionId, sessionIds)
                       .ge(UnifiedChatSession::getCreateTime, Date.from(startTime.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                       .isNotNull(UnifiedChatSession::getUserSatisfaction)
                       .gt(UnifiedChatSession::getUserSatisfaction, 0)
                       .orderByAsc(UnifiedChatSession::getCreateTime);
            
            List<UnifiedChatSession> sessions = sessionDao.selectList(sessionQuery);
            
            // 按日期分组会话满意度数据
            dailySessionSatisfactionMap = sessions.stream()
                    .collect(Collectors.groupingBy(
                        session -> {
                            LocalDate date = session.getCreateTime().toInstant()
                                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        },
                        Collectors.mapping(UnifiedChatSession::getUserSatisfaction, Collectors.toList())
                    ));
        }
        
        // 生成每一天的满意度趋势数据
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String displayDate = date.format(DateTimeFormatter.ofPattern("MM-dd"));
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", displayDate);
            
            // 计算当天的评价满意度
            List<HumanServiceRating> dayRatings = dailyRatingsMap.getOrDefault(dateStr, new ArrayList<>());
            List<Integer> daySessionSatisfactions = dailySessionSatisfactionMap.getOrDefault(dateStr, new ArrayList<>());
            
            double satisfaction = 0.0;
            int ratingCount = 0;
            
            if (!dayRatings.isEmpty()) {
                // 使用评价表的评分数据
                double totalScore = dayRatings.stream()
                        .filter(rating -> rating.getScore() != null)
                        .mapToDouble(rating -> rating.getScore().doubleValue())
                        .sum();
                satisfaction = totalScore / dayRatings.size();
                ratingCount = dayRatings.size();
            } else if (!daySessionSatisfactions.isEmpty()) {
                // 如果没有评价数据，使用会话满意度数据
                double totalScore = daySessionSatisfactions.stream()
                        .mapToDouble(Integer::doubleValue)
                        .sum();
                satisfaction = totalScore / daySessionSatisfactions.size();
                ratingCount = daySessionSatisfactions.size();
            }
            
            dayData.put("satisfaction", Math.round(satisfaction * 100.0) / 100.0); // 保留2位小数
            dayData.put("ratingCount", ratingCount);
            
            result.add(dayData);
        }
        
        return result;
    }

    /**
     * 获取总体统计数据
     */
    private HumanServiceStatisticsResponse.OverallStatistics getOverallStatistics(Long merId, Integer days) {
        List<String> sessionIds = getSessionIdsByMerId(merId);
        
        HumanServiceStatisticsResponse.OverallStatistics stats = 
            new HumanServiceStatisticsResponse.OverallStatistics();
        
        if (sessionIds.isEmpty()) {
            // 返回空数据
            stats.setTotalMessages(0L)
                 .setTodayMessages(0L)
                 .setActiveStaffCount(0L)
                 .setAvgResponseTime(BigDecimal.ZERO)
                 .setAvgSatisfactionScore(BigDecimal.ZERO)
                 .setResolutionRate(BigDecimal.ZERO);
            return stats;
        }
        
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        Date todayStart = new Date(System.currentTimeMillis() - System.currentTimeMillis() % (24 * 60 * 60 * 1000L));
        
        // 查询总消息数
        LambdaQueryWrapper<UnifiedChatMessage> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.in(UnifiedChatMessage::getSessionId, sessionIds)
                  .ge(UnifiedChatMessage::getCreateTime, startTime);
        Integer totalMessages = messageDao.selectCount(totalQuery);
        
        // 查询今日消息数
        LambdaQueryWrapper<UnifiedChatMessage> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.in(UnifiedChatMessage::getSessionId, sessionIds)
                  .ge(UnifiedChatMessage::getCreateTime, todayStart);
        Integer todayMessages = messageDao.selectCount(todayQuery);
        
        // 查询活跃客服数（今日有回复的客服）
        LambdaQueryWrapper<UnifiedChatMessage> staffQuery = new LambdaQueryWrapper<>();
        staffQuery.in(UnifiedChatMessage::getSessionId, sessionIds)
                  .eq(UnifiedChatMessage::getSenderType, "STAFF")
                  .ge(UnifiedChatMessage::getCreateTime, todayStart)
                  .select(UnifiedChatMessage::getSenderId)
                  .groupBy(UnifiedChatMessage::getSenderId);
        List<UnifiedChatMessage> staffMessages = messageDao.selectList(staffQuery);
        
        // 计算平均响应时间（基于实际数据）
        BigDecimal avgResponseTime = calculateRealAvgResponseTime(sessionIds, days);
        
        // 计算平均满意度评分（基于评价表真实数据）
        BigDecimal avgSatisfactionScore = calculateRealSatisfactionScore(merId, days);
        
        // 计算问题解决率（基于会话状态和评价数据）
        BigDecimal resolutionRate = calculateRealResolutionRate(sessionIds, merId, days);
        
        stats.setTotalMessages(totalMessages.longValue())
             .setTodayMessages(todayMessages.longValue())
             .setActiveStaffCount((long) staffMessages.size())
             .setAvgResponseTime(avgResponseTime)
             .setAvgSatisfactionScore(avgSatisfactionScore)
             .setResolutionRate(resolutionRate);
        
        return stats;
    }
    
    /**
     * 计算真实的平均响应时间
     */
    private BigDecimal calculateRealAvgResponseTime(List<String> sessionIds, Integer days) {
        if (sessionIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        
        // 查询用户消息和客服回复的消息对
        LambdaQueryWrapper<UnifiedChatMessage> query = new LambdaQueryWrapper<>();
        query.in(UnifiedChatMessage::getSessionId, sessionIds)
             .in(UnifiedChatMessage::getSenderType, Arrays.asList(UnifiedChatMessage.SENDER_TYPE_USER, "STAFF"))
             .ge(UnifiedChatMessage::getCreateTime, startTime)
             .orderByAsc(UnifiedChatMessage::getSessionId, UnifiedChatMessage::getCreateTime);
        
        List<UnifiedChatMessage> messages = messageDao.selectList(query);
        
        if (messages.size() < 2) {
            return new BigDecimal("180"); // 默认3分钟
        }
        
        // 简单计算：找到用户消息后第一个客服回复的时间差
        List<Long> responseTimes = new ArrayList<>();
        for (int i = 0; i < messages.size() - 1; i++) {
            UnifiedChatMessage current = messages.get(i);
            UnifiedChatMessage next = messages.get(i + 1);
            
            if (UnifiedChatMessage.SENDER_TYPE_USER.equals(current.getSenderType()) &&
                "STAFF".equals(next.getSenderType()) &&
                current.getSessionId().equals(next.getSessionId())) {
                
                long responseTime = next.getCreateTime().getTime() - current.getCreateTime().getTime();
                responseTimes.add(responseTime / 1000); // 转换为秒
            }
        }
        
        if (responseTimes.isEmpty()) {
            return new BigDecimal("180"); // 默认3分钟
        }
        
        double avgSeconds = responseTimes.stream().mapToLong(Long::longValue).average().orElse(180.0);
        return new BigDecimal(avgSeconds).setScale(1, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算真实的平均满意度评分
     */
    private BigDecimal calculateRealSatisfactionScore(Long merId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        // 查询评价表的真实数据
        LambdaQueryWrapper<HumanServiceRating> ratingQuery = new LambdaQueryWrapper<>();
        ratingQuery.eq(HumanServiceRating::getMerId, merId)
                  .ge(HumanServiceRating::getCreateTime, startTime)
                  .eq(HumanServiceRating::getStatus, HumanServiceRating.STATUS_APPROVED)
                  .isNotNull(HumanServiceRating::getScore);
        
        List<HumanServiceRating> ratings = ratingDao.selectList(ratingQuery);
        
        if (ratings.isEmpty()) {
            // 如果没有评价数据，查询会话满意度数据
            List<String> sessionIds = getSessionIdsByMerId(merId);
            if (!sessionIds.isEmpty()) {
                LambdaQueryWrapper<UnifiedChatSession> sessionQuery = new LambdaQueryWrapper<>();
                sessionQuery.in(UnifiedChatSession::getSessionId, sessionIds)
                           .ge(UnifiedChatSession::getCreateTime, Date.from(startTime.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                           .isNotNull(UnifiedChatSession::getUserSatisfaction)
                           .gt(UnifiedChatSession::getUserSatisfaction, 0);
                
                List<UnifiedChatSession> sessions = sessionDao.selectList(sessionQuery);
                if (!sessions.isEmpty()) {
                    double avgSatisfaction = sessions.stream()
                            .mapToInt(UnifiedChatSession::getUserSatisfaction)
                            .average()
                            .orElse(0.0);
                    return new BigDecimal(avgSatisfaction).setScale(2, RoundingMode.HALF_UP);
                }
            }
            return BigDecimal.ZERO;
        }
        
        // 计算平均评分
        double avgScore = ratings.stream()
                .filter(rating -> rating.getScore() != null)
                .mapToDouble(rating -> rating.getScore().doubleValue())
                .average()
                .orElse(0.0);
        
        return new BigDecimal(avgScore).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算真实的问题解决率
     */
    private BigDecimal calculateRealResolutionRate(List<String> sessionIds, Long merId, Integer days) {
        if (sessionIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Date startTime = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L);
        
        // 查询所有会话
        LambdaQueryWrapper<UnifiedChatSession> allSessionQuery = new LambdaQueryWrapper<>();
        allSessionQuery.in(UnifiedChatSession::getSessionId, sessionIds)
                      .ge(UnifiedChatSession::getCreateTime, startTime);
        
        int totalSessions = sessionDao.selectCount(allSessionQuery);
        
        if (totalSessions == 0) {
            return BigDecimal.ZERO;
        }
        
        // 计算已解决的会话数量：已结束的会话 + 有好评的会话
        LambdaQueryWrapper<UnifiedChatSession> resolvedSessionQuery = new LambdaQueryWrapper<>();
        resolvedSessionQuery.in(UnifiedChatSession::getSessionId, sessionIds)
                           .ge(UnifiedChatSession::getCreateTime, startTime)
                           .in(UnifiedChatSession::getStatus, Arrays.asList(
                               UnifiedChatSession.STATUS_ENDED, 
                               UnifiedChatSession.STATUS_CLOSED));
        
        int resolvedSessions = sessionDao.selectCount(resolvedSessionQuery);
        
        // 加上有好评的会话（评分>=4分的认为是解决了问题）
        LocalDateTime localStartTime = startTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        LambdaQueryWrapper<HumanServiceRating> goodRatingQuery = new LambdaQueryWrapper<>();
        goodRatingQuery.eq(HumanServiceRating::getMerId, merId)
                      .ge(HumanServiceRating::getCreateTime, localStartTime)
                      .eq(HumanServiceRating::getStatus, HumanServiceRating.STATUS_APPROVED)
                      .ge(HumanServiceRating::getScore, new BigDecimal("4"));
        
        int goodRatingsCount = ratingDao.selectCount(goodRatingQuery);
        
        // 避免重复计算，取较大值
        int finalResolved = Math.max(resolvedSessions, goodRatingsCount);
        
        double resolutionRate = (double) finalResolved / totalSessions * 100;
        return new BigDecimal(resolutionRate).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 根据商户ID获取会话ID列表
     */
    private List<String> getSessionIdsByMerId(Long merId) {
        LambdaQueryWrapper<UnifiedChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedChatSession::getMerId, merId)
                   .select(UnifiedChatSession::getSessionId);
        
        List<UnifiedChatSession> sessions = sessionDao.selectList(queryWrapper);
        return sessions.stream()
                .map(UnifiedChatSession::getSessionId)
                .collect(Collectors.toList());
    }

    /**
     * 获取客服姓名
     */
    private String getStaffName(String senderName) {
        return StringUtils.hasText(senderName) ? senderName : "未知客服";
    }

    /**
     * 获取消息类型名称
     */
    private String getMessageTypeName(String messageType) {
        switch (messageType) {
            case "text": return "文本消息";
            case "image": return "图片消息";
            case "file": return "文件消息";
            case "audio": return "语音消息";
            case "video": return "视频消息";
            case "card": return "卡片消息";
            case "system": return "系统消息";
            default: return "其他消息";
        }
    }
}
