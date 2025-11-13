package com.zbkj.service.service.impl;

import com.zbkj.service.service.StaffAssignmentService;
import com.zbkj.service.service.SystemAdminService;
import com.zbkj.service.service.CustomerServiceStaffService;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.service.CustomerServiceStaff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客服分配服务实现
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class StaffAssignmentServiceImpl implements StaffAssignmentService {
    @Autowired
    private SystemAdminService systemAdminService;
    @Autowired
    private CustomerServiceStaffService customerServiceStaffService;

    // 客服在线状态缓存
    private static final Map<Integer, Map<String, Object>> staffStatusCache = new ConcurrentHashMap<>();
    
    // 客服工作负载缓存 - staffId -> Set<sessionId>
    private static final Map<Integer, Set<String>> staffWorkloadCache = new ConcurrentHashMap<>();
    
    // 客服技能缓存
    private static final Map<Integer, List<String>> staffSkillsCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> assignBestStaff(Integer merchantId, Integer userId, String sessionId, 
                                              List<String> requiredSkills, String urgency, Integer preferredStaffId) {
        try {
            log.info("智能分配客服: merchantId={}, userId={}, sessionId={}, skills={}, urgency={}", 
                    merchantId, userId, sessionId, requiredSkills, urgency);

            Map<String, Object> result = new HashMap<>();

            // 1. 首先获取所有在线客服
            List<Map<String, Object>> onlineStaff = getMerchantStaff(merchantId, true);
            
            // 2. 如果没有在线客服，获取所有客服（包括离线）
            List<Map<String, Object>> availableStaff = onlineStaff;
            if (onlineStaff.isEmpty()) {
                log.info("没有在线客服，将尝试分配离线客服: merchantId={}", merchantId);
                availableStaff = getMerchantStaff(merchantId, false);  // 获取所有客服
            }
            
            // 如果完全没有客服，尝试将商户设为默认客服
            if (availableStaff.isEmpty()) {
                Map<String, Object> merchantAsStaff = assignMerchantAsDefaultStaff(merchantId);
                if (merchantAsStaff != null) {
                    result.put("success", true);
                    result.put("staffId", merchantAsStaff.get("staffId"));
                    result.put("staffName", merchantAsStaff.get("staffName"));
                    result.put("assignmentReason", "商户默认客服");
                    return result;
                } else {
                    result.put("success", false);
                    result.put("message", "暂无可用客服，且无法分配默认客服");
                    return result;
                }
            }

            // 2. 如果指定了首选客服，优先检查
            if (preferredStaffId != null) {
                for (Map<String, Object> staff : availableStaff) {
                    Integer staffId = (Integer) staff.get("staffId");
                    if (staffId.equals(preferredStaffId) && canStaffAcceptNewSession(staffId)) {
                        result.put("success", true);
                        result.put("staffId", staffId);
                        result.put("staffName", staff.get("staffName"));
                        result.put("assignmentReason", "首选客服");
                        return result;
                    }
                }
            }

            // 3. 计算每个客服的匹配分数
            Map<Integer, Integer> staffScores = new HashMap<>();
            for (Map<String, Object> staff : availableStaff) {
                Integer staffId = (Integer) staff.get("staffId");
                
                // 移除严格的在线检查，改为分数权重处理
                // if (canStaffAcceptNewSession(staffId)) {
                
                int score = calculateStaffMatchScore(staffId, requiredSkills, urgency, "normal");
                
                // 在线客服额外加分
                Boolean isOnline = (Boolean) staff.get("isOnline");
                if (Boolean.TRUE.equals(isOnline)) {
                    score += 100;  // 在线客服额外得100分
                    log.debug("客服{}在线，额外加分: score={}", staffId, score);
                } else {
                    log.debug("客服{}离线，分配到离线客服: score={}", staffId, score);
                }
                
                // 检查工作负载，避免过载分配
                if (canStaffAcceptNewSession(staffId)) {
                    score += 20;  // 可接受新会话的客服额外加分
                } else {
                    score -= 50;  // 已达负载上限的客服减分，但仍可分配
                }
                
                staffScores.put(staffId, score);
                // }
            }

            // 4. 选择分数最高的客服
            Integer bestStaffId = staffScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (bestStaffId != null) {
                // 找到客服信息
                Map<String, Object> bestStaff = availableStaff.stream()
                        .filter(staff -> bestStaffId.equals(staff.get("staffId")))
                        .findFirst()
                        .orElse(null);

                Boolean isOnline = bestStaff != null ? (Boolean) bestStaff.get("isOnline") : false;
                String staffStatus = Boolean.TRUE.equals(isOnline) ? "在线" : "离线";
                String assignmentReason = Boolean.TRUE.equals(isOnline) ? "智能匹配（在线客服）" : "智能匹配（离线客服）";

                result.put("success", true);
                result.put("staffId", bestStaffId);
                result.put("staffName", bestStaff != null ? bestStaff.get("staffName") : "客服" + bestStaffId);
                result.put("isOnline", isOnline);
                result.put("staffStatus", staffStatus);
                result.put("matchScore", staffScores.get(bestStaffId));
                result.put("assignmentReason", assignmentReason);
                
                log.info("成功分配{}客服: staffId={}, staffName={}, score={}", 
                        staffStatus, bestStaffId, bestStaff != null ? bestStaff.get("staffName") : "客服" + bestStaffId, 
                        staffScores.get(bestStaffId));
            } else {
                result.put("success", false);
                result.put("message", "没有可用的客服");
            }

            return result;

        } catch (Exception e) {
            log.error("智能分配客服失败: merchantId={}, sessionId={}, 错误: {}", merchantId, sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "分配失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> assignByRule(Integer merchantId, String assignmentRule, String sessionId, 
                                           Integer userId, Map<String, Object> context) {
        // 简化实现，使用智能分配
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) context.getOrDefault("skills", Arrays.asList("general"));
        String urgency = (String) context.getOrDefault("urgency", "medium");
        return assignBestStaff(merchantId, userId, sessionId, skills, urgency, null);
    }

    @Override
    public Map<String, Object> getStaffWorkload(Integer staffId) {
        Map<String, Object> workload = new HashMap<>();
        Set<String> sessions = staffWorkloadCache.getOrDefault(staffId, new HashSet<>());
        
        workload.put("staffId", staffId);
        workload.put("activeSessions", sessions.size());
        workload.put("sessionIds", new ArrayList<>(sessions));
        workload.put("maxConcurrentSessions", getStaffMaxSessions(staffId));
        // 移除循环调用，直接计算可用性
        Map<String, Object> status = getStaffOnlineStatus(staffId);
        boolean isOnline = "online".equals(status.get("status"));
        int maxSessions = getStaffMaxSessions(staffId);
        workload.put("isAvailable", isOnline && sessions.size() < maxSessions);
        
        return workload;
    }

    @Override
    public Map<String, Object> getStaffOnlineStatus(Integer staffId) {
        Map<String, Object> status = staffStatusCache.get(staffId);
        if (status == null) {
            status = new HashMap<>();
            status.put("staffId", staffId);
            status.put("status", "offline");
            status.put("lastActiveTime", 0L);
        }
        return new HashMap<>(status);
    }

    @Override
    public List<String> getStaffSkills(Integer staffId) {
        return staffSkillsCache.getOrDefault(staffId, Arrays.asList("general"));
    }

    @Override
    public List<Map<String, Object>> getMerchantStaff(Integer merchantId, boolean onlineOnly) {
        List<Map<String, Object>> staffStats = customerServiceStaffService.getStaffStatsByMerId(merchantId,onlineOnly);
        return staffStats;}

    @Override
    public int calculateStaffMatchScore(Integer staffId, List<String> requiredSkills, 
                                       String urgency, String userLevel) {
        int score = 50; // 基础分数
        
        try {
            // 1. 技能匹配分数 (0-30分)
            List<String> staffSkills = getStaffSkills(staffId);
            int skillMatchCount = 0;
            for (String skill : requiredSkills) {
                if (staffSkills.contains(skill)) {
                    skillMatchCount++;
                }
            }
            score += (skillMatchCount * 30) / Math.max(requiredSkills.size(), 1);
            
            // 2. 工作负载分数 (0-20分)
            Map<String, Object> workload = getStaffWorkload(staffId);
            int activeSessions = (Integer) workload.get("activeSessions");
            int maxSessions = (Integer) workload.get("maxConcurrentSessions");
            
            if (maxSessions > 0) {
                int loadPercentage = (activeSessions * 100) / maxSessions;
                score += Math.max(0, 20 - (loadPercentage / 5)); // 负载越低分数越高
            }
            
            // 3. 紧急程度加分
            if ("high".equals(urgency)) {
                score += 10;
            }
            
            // 4. VIP用户加分
            if ("vip".equals(userLevel)) {
                score += 5;
            }
            
        } catch (Exception e) {
            log.error("计算客服匹配分数失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
        }
        
        return Math.min(score, 100);
    }

    @Override
    public boolean updateStaffStatus(Integer staffId, String status, Integer maxConcurrentSessions) {
        try {
            Map<String, Object> staffStatus = new HashMap<>();
            staffStatus.put("staffId", staffId);
            staffStatus.put("status", status);
            staffStatus.put("maxConcurrentSessions", maxConcurrentSessions != null ? maxConcurrentSessions : 5);
            staffStatus.put("lastActiveTime", System.currentTimeMillis());
            
            staffStatusCache.put(staffId, staffStatus);
            
            log.info("更新客服状态: staffId={}, status={}, maxSessions={}", staffId, status, maxConcurrentSessions);
            return true;
            
        } catch (Exception e) {
            log.error("更新客服状态失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean addStaffWorkload(Integer staffId, String sessionId) {
        try {
            staffWorkloadCache.computeIfAbsent(staffId, k -> new HashSet<>()).add(sessionId);
            
            // 检查是否需要设置为忙碌状态
            checkAndUpdateStaffBusyStatus(staffId);
            
            log.info("增加客服工作负载: staffId={}, sessionId={}", staffId, sessionId);
            return true;
        } catch (Exception e) {
            log.error("增加客服工作负载失败: staffId={}, sessionId={}, 错误: {}", staffId, sessionId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean removeStaffWorkload(Integer staffId, String sessionId) {
        try {
            Set<String> sessions = staffWorkloadCache.get(staffId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    staffWorkloadCache.remove(staffId);
                }
            }
            
            // 检查是否需要从忙碌状态恢复
            checkAndUpdateStaffBusyStatus(staffId);
            
            log.info("减少客服工作负载: staffId={}, sessionId={}", staffId, sessionId);
            return true;
        } catch (Exception e) {
            log.error("减少客服工作负载失败: staffId={}, sessionId={}, 错误: {}", staffId, sessionId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getAssignmentStatistics(Integer merchantId, Long startTime, Long endTime) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAssignments", 0);
        statistics.put("successfulAssignments", 0);
        statistics.put("averageResponseTime", 0);
        statistics.put("staffUtilization", new HashMap<>());
        return statistics;
    }

    @Override
    public Map<String, Object> handleStaffForceOffline(Integer staffId, String reason) {
        try {
            // 1. 更新客服状态为离线
            updateStaffStatus(staffId, "offline", 0);
            
            // 2. 获取客服当前处理的会话
            List<String> activeSessions = getStaffActiveSessions(staffId);
            
            // 3. 重新分配所有会话
            List<String> reassignedSessions = new ArrayList<>();
            List<String> failedSessions = new ArrayList<>();
            
            for (String sessionId : activeSessions) {
                Map<String, Object> result = reassignSession(sessionId, "客服强制下线：" + reason);
                if ((Boolean) result.get("success")) {
                    reassignedSessions.add(sessionId);
                } else {
                    failedSessions.add(sessionId);
                }
            }
            
            // 4. 清空工作负载
            staffWorkloadCache.remove(staffId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("staffId", staffId);
            result.put("reason", reason);
            result.put("affectedSessions", activeSessions.size());
            result.put("reassignedSessions", reassignedSessions);
            result.put("failedSessions", failedSessions);
            
            log.info("客服强制下线处理完成: staffId={}, 影响会话数={}", staffId, activeSessions.size());
            return result;
            
        } catch (Exception e) {
            log.error("客服强制下线处理失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "处理失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> reassignSession(String sessionId, String reason) {
        try {
            log.info("重新分配会话: sessionId={}, reason={}", sessionId, reason);
            
            // 这里应该调用ChatHandoverService来重新分配
            // 暂时返回成功
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("reason", reason);
            
            return result;
            
        } catch (Exception e) {
            log.error("重新分配会话失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "重新分配失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public boolean canStaffAcceptNewSession(Integer staffId) {
        try {
            Map<String, Object> status = getStaffOnlineStatus(staffId);
            String staffStatus = (String) status.get("status");
            
            // 只有在线状态的客服才能接受新会话，忙碌和离线状态都不能接受
            if (!"online".equals(staffStatus)) {
                return false;
            }
            
            Map<String, Object> workload = getStaffWorkload(staffId);
            int activeSessions = (Integer) workload.get("activeSessions");
            int maxSessions = (Integer) workload.get("maxConcurrentSessions");
            
            return activeSessions < maxSessions;
            
        } catch (Exception e) {
            log.error("检查客服是否可接受新会话失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<String> getStaffActiveSessions(Integer staffId) {
        Set<String> sessions = staffWorkloadCache.get(staffId);
        return sessions != null ? new ArrayList<>(sessions) : new ArrayList<>();
    }

    @Override
    public boolean setStaffSkills(Integer staffId, List<String> skills) {
        try {
            staffSkillsCache.put(staffId, new ArrayList<>(skills));
            log.info("设置客服技能: staffId={}, skills={}", staffId, skills);
            return true;
        } catch (Exception e) {
            log.error("设置客服技能失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getPendingAssignmentQueue(Integer merchantId) {
        // 这里可以实现等待队列的逻辑
        return new ArrayList<>();
    }

    /**
     * 私有辅助方法
     */
    
    private int getStaffMaxSessions(Integer staffId) {
        Map<String, Object> status = getStaffOnlineStatus(staffId);
        return (Integer) status.getOrDefault("maxConcurrentSessions", 5);
    }

    /**
     * 将商户分配为默认客服
     */
    private Map<String, Object> assignMerchantAsDefaultStaff(Integer merchantId) {
        try {
            // 1. 通过merchantId查找对应的SystemAdmin（商户管理员）
            // 需要注入相关服务
            if (systemAdminService == null || customerServiceStaffService == null) {
                log.error("缺少必要的服务依赖，无法分配商户为默认客服");
                return null;
            }
            
            // 查找商户对应的管理员账号
            SystemAdmin merchantAdmin = systemAdminService.getByMerId(merchantId);
            if (merchantAdmin == null) {
                log.warn("商户{}没有对应的管理员账号，无法设为默认客服", merchantId);
                return null;
            }
            
            // 2. 检查是否已经存在该商户的默认客服记录
            CustomerServiceStaff existingStaff = customerServiceStaffService.getByEmployeeId(merchantAdmin.getId());
            if (existingStaff != null) {
                // 已存在客服记录，检查是否为默认客服
                if (!Boolean.TRUE.equals(existingStaff.getIsDefault())) {
                    // 设置为默认客服
                    existingStaff.setIsDefault(true);
                    existingStaff.setStatus(CustomerServiceStaff.STATUS_ENABLED);
                    existingStaff.setOnlineStatus(CustomerServiceStaff.ONLINE_STATUS_ONLINE);
                    existingStaff.setMaxConcurrentSessions(10);
                    existingStaff.setUpdateTime(new Date());
                    customerServiceStaffService.updateStaff(existingStaff);
                }
                
                // 更新客服状态为在线
                updateStaffStatus(merchantAdmin.getId(), "online", 10);
                
                Map<String, Object> result = new HashMap<>();
                result.put("staffId", merchantAdmin.getId());
                result.put("staffName", merchantAdmin.getRealName());
                result.put("isDefault", true);
                
                log.info("商户{}的现有客服已设置为默认客服", merchantId);
                return result;
            }
            
            // 3. 创建新的默认客服记录
            CustomerServiceStaff newStaff = new CustomerServiceStaff();
            newStaff.setAdminId(merchantAdmin.getId());
            newStaff.setMerId(merchantId);
            newStaff.setStaffNo("MERCHANT_" + merchantId);
            newStaff.setStaffName(merchantAdmin.getRealName() != null ? merchantAdmin.getRealName() : "商户客服");
            newStaff.setAvatar(merchantAdmin.getHeaderImage());
            newStaff.setServiceLevel(CustomerServiceStaff.SERVICE_LEVEL_STANDARD);
            newStaff.setSkillTags("[\"general\",\"merchant_default\"]"); // JSON格式的技能标签
            newStaff.setMaxConcurrentSessions(10);
            newStaff.setCurrentSessions(0);
            newStaff.setOnlineStatus(CustomerServiceStaff.ONLINE_STATUS_ONLINE);
            newStaff.setLastOnlineTime(new Date());
            newStaff.setTotalServedSessions(0);
            newStaff.setAverageResponseTime(0);
            newStaff.setSatisfactionRating(BigDecimal.ZERO);
            newStaff.setStatus(CustomerServiceStaff.STATUS_ENABLED);
            newStaff.setIsDefault(true);
            newStaff.setCreateTime(new Date());
            newStaff.setUpdateTime(new Date());
            
            customerServiceStaffService.save(newStaff);
            
            // 4. 设置商户默认客服为在线状态
            updateStaffStatus(merchantAdmin.getId(), "online", 10);
            
            Map<String, Object> result = new HashMap<>();
            result.put("staffId", merchantAdmin.getId());
            result.put("staffName", newStaff.getStaffName());
            result.put("isDefault", true);
            
            log.info("已为商户{}创建默认客服记录: staffId={}", merchantId, merchantAdmin.getId());
            return result;
            
        } catch (Exception e) {
            log.error("设置商户默认客服失败: merchantId={}, 错误: {}", merchantId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查并更新客服忙碌状态
     */
    private void checkAndUpdateStaffBusyStatus(Integer staffId) {
        try {
            Map<String, Object> currentStatus = getStaffOnlineStatus(staffId);
            String currentStatusStr = (String) currentStatus.get("status");
            
            // 只有在线状态的客服才会变为忙碌状态
            if (!"online".equals(currentStatusStr)) {
                return;
            }
            
            Set<String> sessions = staffWorkloadCache.get(staffId);
            int currentSessions = sessions != null ? sessions.size() : 0;
            int maxSessions = getStaffMaxSessions(staffId);
            
            if (currentSessions >= maxSessions) {
                // 达到最大会话数，设置为忙碌
                updateStaffStatus(staffId, "busy", maxSessions);
                log.info("客服达到最大会话数，设置为忙碌状态: staffId={}, currentSessions={}, maxSessions={}", 
                        staffId, currentSessions, maxSessions);
            } else if (currentSessions < maxSessions && "busy".equals(currentStatusStr)) {
                // 会话数减少且当前为忙碌状态，恢复为在线状态
                updateStaffStatus(staffId, "online", maxSessions);
                log.info("客服会话数减少，从忙碌状态恢复为在线状态: staffId={}, currentSessions={}, maxSessions={}", 
                        staffId, currentSessions, maxSessions);
            }
            
        } catch (Exception e) {
            log.error("检查并更新客服忙碌状态失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
        }
    }

    // 初始化一些默认数据
    static {
        // 初始化一些客服状态
        for (int merchantId = 1; merchantId <= 3; merchantId++) {
            for (int i = 1; i <= 5; i++) {
                Integer staffId = merchantId * 100 + i;
                
                Map<String, Object> status = new HashMap<>();
                status.put("staffId", staffId);
                status.put("status", i <= 2 ? "online" : "offline"); // 前2个客服在线
                status.put("maxConcurrentSessions", 5);
                status.put("lastActiveTime", System.currentTimeMillis());
                
                staffStatusCache.put(staffId, status);
                
                // 设置技能
                List<String> skills = new ArrayList<>();
                skills.add("general");
                if (i == 1) skills.add("vip");
                if (i == 2) skills.add("technical");
                if (i == 3) skills.add("refund");
                
                staffSkillsCache.put(staffId, skills);
            }
        }
    }
}
