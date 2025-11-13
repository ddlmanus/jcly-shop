package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.service.CustomerServiceStaff;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import java.util.List;
import java.util.Map;

/**
 * 人工客服员工服务接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface CustomerServiceStaffService extends IService<CustomerServiceStaff> {

    /**
     * 获取客服列表
     */
    List<CustomerServiceStaff> getStaffList(String onlineStatus);

    /**
     * 更新客服在线状态
     */
    void updateOnlineStatus(Integer staffId, String onlineStatus);

    /**
     * 根据员工ID获取客服信息
     */
    CustomerServiceStaff getByEmployeeId(Integer employeeId);

    /**
     * 创建或更新客服信息
     */
    CustomerServiceStaff createOrUpdateStaff(Integer employeeId, String staffNo, String serviceLevel);

    /**
     * 获取可分配的客服列表
     */
    List<CustomerServiceStaff> getAvailableStaff(String serviceLevel);

    /**
     * 更新客服当前会话数
     */
    void updateCurrentSessions(Integer staffId, Integer increment);

    /**
     * 获取客服统计信息
     */
    Map<String, Object> getStaffStatistics(Integer staffId);

    /**
     * 更新客服技能标签
     */
    void updateSkillTags(Integer staffId, String skillTags);

    /**
     * 获取在线客服数量
     */
    Integer getOnlineStaffCount();

    /**
     * 检查客服是否在线且可用
     */
    boolean isStaffAvailable(Integer staffId);

    // ==================== 平台端专用方法 ====================

    /**
     * 获取平台客服人员列表
     */
    CommonPage<CustomerServiceStaff> getPlatformStaffList(String status, PageParamRequest pageParamRequest);

    /**
     * 添加平台客服人员
     */
    void addPlatformStaff(CustomerServiceStaff staff);

    /**
     * 更新客服人员信息
     */
    void updateStaff(CustomerServiceStaff staff);

    /**
     * 删除客服人员
     */
    void deleteStaff(Integer staffId);

    /**
     * 更新客服状态
     */
    void updateStaffStatus(Integer staffId, String status);

    // ==================== 控制器新增接口所需方法 ====================

    /**
     * 获取可用客服列表（Map格式）
     */
    List<Map<String, Object>> getAvailableStaff();



    /**
     * 添加客服
     */
    void addStaff(CustomerServiceStaff staff);

    /**
     * 获取客服统计信息（重载方法）
     */
    Map<String, Object> getStaffStats(Integer staffId);

    List<Map<String,Object>> getStaffStatsByMerId(Integer merchantId,boolean onlineOnly);

    // ==================== 客服登录与管理新增方法 ====================

    /**
     * 客服登录
     * @param request 登录请求
     * @return 登录响应（包含Token、客服信息）
     */
    com.zbkj.common.response.CustomerServiceLoginResponse login(com.zbkj.common.request.CustomerServiceLoginRequest request);

    /**
     * 根据管理员ID获取客服
     * @param adminId 管理员ID
     * @return 客服信息
     */
    CustomerServiceStaff getByAdminId(Integer adminId);

    /**
     * 创建客服账号
     * @param request 客服信息
     * @param merId 商户ID
     * @return 客服信息
     */
    CustomerServiceStaff createCustomerService(com.zbkj.common.request.CustomerServiceRequest request, Integer merId);

    /**
     * 更新客服信息
     * @param request 客服信息
     */
    void updateCustomerService(com.zbkj.common.request.CustomerServiceRequest request);

    /**
     * 删除客服
     * @param staffId 客服ID
     */
    void deleteCustomerService(Integer staffId);

    /**
     * 更新客服启用状态
     * @param staffId 客服ID
     * @param status 状态：true-启用，false-禁用
     */
    void updateStatus(Integer staffId, Boolean status);

    /**
     * 获取客服分页列表
     * @param merId 商户ID
     * @param keyword 搜索关键词（姓名、账号、手机号）
     * @param onlineStatus 在线状态：ONLINE, BUSY, AWAY, OFFLINE
     * @param status 启用状态：true-启用，false-禁用
     * @param pageParamRequest 分页参数
     * @return 客服分页列表
     */
    CommonPage<CustomerServiceStaff> getPageList(Integer merId, String keyword,
                                                  String onlineStatus, Boolean status,
                                                  PageParamRequest pageParamRequest);

    /**
     * 重置客服密码
     * @param staffId 客服ID
     * @param newPassword 新密码（明文）
     */
    void resetPassword(Integer staffId, String newPassword);

    /**
     * 设置默认客服
     * @param staffId 客服ID
     * @param merId 商户ID
     */
    void setDefaultStaff(Integer staffId, Integer merId);

    /**
     * 生成客服工号
     * @param merId 商户ID
     * @return 客服工号，格式：CS{商户ID}{序号}，如CS1001
     */
    String generateStaffNo(Integer merId);

    /**
     * 获取在线客服列表
     * @param merId 商户ID
     * @return 在线客服列表
     */
    List<CustomerServiceStaff> getOnlineStaff(Integer merId);

    /**
     * 获取可用客服列表（在线且未达最大会话数）
     * @param merId 商户ID
     * @return 可用客服列表
     */
    List<CustomerServiceStaff> getAvailableStaff(Long merId);

    /**
     * 增加当前会话数
     * @param staffId 客服ID
     */
    void incrementCurrentSessions(Integer staffId);

    /**
     * 减少当前会话数
     * @param staffId 客服ID
     */
    void decrementCurrentSessions(Integer staffId);

    /**
     * 获取默认客服
     * @param merId 商户ID
     * @return 默认客服
     */
    CustomerServiceStaff getDefaultStaff(Long merId);

    /**
     * 获取客服工作统计
     * @param staffId 客服ID
     * @param startDate 开始日期，格式：yyyy-MM-dd
     * @param endDate 结束日期，格式：yyyy-MM-dd
     * @return 统计数据
     */
    Map<String, Object> getStaffStatistics(Integer staffId, String startDate, String endDate);

    /**
     * 获取待处理会话数量
     * @param staffId 客服ID
     * @return 待处理会话数量
     */
    Integer getPendingSessionCount(Integer staffId);

    /**
     * 为商户创建或获取默认客服
     * 如果客服表为空，自动为商户超级管理员创建默认客服账号
     * @param merId 商户ID
     * @return 默认客服
     */
    CustomerServiceStaff createOrGetDefaultStaffForMerchant(Long merId);

    /**
     * 为平台创建或获取默认客服
     * 如果客服表为空，自动为平台超级管理员创建默认客服账号
     * @return 默认客服
     */
    CustomerServiceStaff createOrGetDefaultStaffForPlatform();
}
