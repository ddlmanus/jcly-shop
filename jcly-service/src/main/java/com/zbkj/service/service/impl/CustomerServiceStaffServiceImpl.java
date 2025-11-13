package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.admin.SystemRole;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.model.service.CustomerServiceStaff;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.page.CommonPage;
import com.zbkj.service.dao.CustomerServiceStaffDao;
import com.zbkj.service.service.CustomerServiceStaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 人工客服员工服务实现
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class CustomerServiceStaffServiceImpl extends ServiceImpl<CustomerServiceStaffDao,CustomerServiceStaff>implements CustomerServiceStaffService {

    @Autowired
    private CustomerServiceStaffDao customerServiceStaffDao;

    @Override
    public List<CustomerServiceStaff> getStaffList(String onlineStatus) {
        try {
            Integer merId = getCurrentMerId();
            
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getAdminId, getCurrentUserId()); // 按管理员ID过滤
            wrapper.eq(StringUtils.hasText(onlineStatus), CustomerServiceStaff::getOnlineStatus, onlineStatus);
            wrapper.eq(CustomerServiceStaff::getStatus, true); // 只显示启用的客服
            wrapper.orderByDesc(CustomerServiceStaff::getCreateTime);
            
            List<CustomerServiceStaff> staffList = customerServiceStaffDao.selectList(wrapper);
            log.info("获取客服列表成功，管理员IDID: {}, 数量: {}", getCurrentUserId(), staffList.size());
            return staffList;
        } catch (Exception e) {
            log.error("获取客服列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOnlineStatus(Integer adminId, String onlineStatus) {
      //  CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
        CustomerServiceStaff customerServiceStaff = customerServiceStaffDao.selectByAdminId(adminId);
        if (customerServiceStaff == null) {
            throw new CrmebException("客服不存在");
        }

        String oldStatus = customerServiceStaff.getOnlineStatus();
        customerServiceStaff.setOnlineStatus(onlineStatus);
        customerServiceStaff.setLastOnlineTime(new Date());
        customerServiceStaff.setUpdateTime(new Date());

        int result = customerServiceStaffDao.updateById(customerServiceStaff);
        if (result <= 0) {
            throw new CrmebException("更新客服状态失败");
        }

        log.info("更新客服在线状态，客服ID: {}, 状态: {} -> {}", customerServiceStaff, oldStatus, onlineStatus);
    }

    @Override
    public CustomerServiceStaff getByEmployeeId(Integer employeeId) {
        return customerServiceStaffDao.selectByAdminId(employeeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerServiceStaff createOrUpdateStaff(Integer employeeId, String staffNo, String serviceLevel) {
        CustomerServiceStaff existingStaff = getByEmployeeId(employeeId);
        
        if (existingStaff != null) {
            // 更新现有客服信息
            existingStaff.setServiceLevel(serviceLevel);
            existingStaff.setUpdateTime(new Date());
            customerServiceStaffDao.updateById(existingStaff);
            return existingStaff;
        } else {
            // 创建新的客服记录
            CustomerServiceStaff newStaff = new CustomerServiceStaff();
            newStaff.setAdminId(employeeId);
            newStaff.setStaffNo(staffNo);
            newStaff.setServiceLevel(serviceLevel);
            newStaff.setMaxConcurrentSessions(3);
            newStaff.setCurrentSessions(0);
            newStaff.setOnlineStatus(CustomerServiceStaff.ONLINE_STATUS_OFFLINE);
            newStaff.setTotalServedSessions(0);
            newStaff.setAverageResponseTime(0);
            newStaff.setSatisfactionRating(BigDecimal.ZERO);
            newStaff.setStatus(true);
            newStaff.setCreateTime(new Date());
            newStaff.setUpdateTime(new Date());
            
            customerServiceStaffDao.insert(newStaff);
            log.info("创建新客服记录，员工ID: {}, 工号: {}", employeeId, staffNo);
            return newStaff;
        }
    }

    @Override
    public List<CustomerServiceStaff> getAvailableStaff(String serviceLevel) {
        LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceStaff::getMerId, getCurrentMerId());
        wrapper.eq(CustomerServiceStaff::getStatus, true);
        wrapper.eq(CustomerServiceStaff::getOnlineStatus, CustomerServiceStaff.ONLINE_STATUS_ONLINE);
        if (StringUtils.hasText(serviceLevel)) {
            wrapper.eq(CustomerServiceStaff::getServiceLevel, serviceLevel);
        }
        return customerServiceStaffDao.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentSessions(Integer staffId, Integer increment) {
        CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
        if (staff != null) {
            int newCurrentSessions = Math.max(0, staff.getCurrentSessions() + increment);
            staff.setCurrentSessions(newCurrentSessions);
            staff.setUpdateTime(new Date());
            customerServiceStaffDao.updateById(staff);
            
            log.info("更新客服当前会话数，客服ID: {}, 变化: {}, 当前: {}", 
                    staffId, increment, newCurrentSessions);
        }
    }

    @Override
    public Map<String, Object> getStaffStatistics(Integer staffId) {
        CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
        if (staff == null) {
            throw new CrmebException("客服不存在");
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("staffInfo", staff);
        statistics.put("totalServedSessions", staff.getTotalServedSessions());
        statistics.put("averageResponseTime", staff.getAverageResponseTime());
        statistics.put("satisfactionRating", staff.getSatisfactionRating());
        statistics.put("currentSessions", staff.getCurrentSessions());
        statistics.put("maxConcurrentSessions", staff.getMaxConcurrentSessions());
        statistics.put("onlineStatus", staff.getOnlineStatus());
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkillTags(Integer staffId, String skillTags) {
        CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
        if (staff == null) {
            throw new CrmebException("客服不存在");
        }

        staff.setSkillTags(skillTags);
        staff.setUpdateTime(new Date());
        customerServiceStaffDao.updateById(staff);
        
        log.info("更新客服技能标签，客服ID: {}, 技能标签: {}", staffId, skillTags);
    }

    @Override
    public Integer getOnlineStaffCount() {
        LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceStaff::getMerId, getCurrentMerId());
        wrapper.eq(CustomerServiceStaff::getStatus, true);
        wrapper.eq(CustomerServiceStaff::getOnlineStatus, CustomerServiceStaff.ONLINE_STATUS_ONLINE);
        return customerServiceStaffDao.selectCount(wrapper);
    }

    @Override
    public boolean isStaffAvailable(Integer staffId) {
        LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceStaff::getAdminId, staffId);
       // wrapper.eq(CustomerServiceStaff::getStatus, true);
        CustomerServiceStaff staff = customerServiceStaffDao.selectOne(wrapper);
       // CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
        if (staff == null || !staff.getStatus()) {
            return false;
        }

        // 检查在线状态
        if (!CustomerServiceStaff.ONLINE_STATUS_ONLINE.equals(staff.getOnlineStatus())) {
            return false;
        }

        // 检查是否达到最大并发会话数
        if (staff.getCurrentSessions() >= staff.getMaxConcurrentSessions()) {
            return false;
        }

        return true;
    }

    // ==================== 平台端专用方法 ====================

    @Override
    public CommonPage<CustomerServiceStaff> getPlatformStaffList(String status, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
        // 平台查询不限制商户ID
        wrapper.eq(StringUtils.hasText(status), CustomerServiceStaff::getStatus, status);
        wrapper.orderByDesc(CustomerServiceStaff::getCreateTime);
        
        List<CustomerServiceStaff> list = customerServiceStaffDao.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPlatformStaff(CustomerServiceStaff staff) {
        // 平台管理员添加客服，使用平台用户ID
        Integer platformUserId = SecurityUtil.getLoginUserVo().getUser().getId();
        
        staff.setAdminId(platformUserId);
        staff.setCreateTime(new Date());
        staff.setUpdateTime(new Date());
        staff.setOnlineStatus(CustomerServiceStaff.ONLINE_STATUS_OFFLINE);
        staff.setCurrentSessions(0);
        
        if (staff.getMaxConcurrentSessions() == null) {
            staff.setMaxConcurrentSessions(5); // 默认最大并发会话数
        }
        
        customerServiceStaffDao.insert(staff);
        log.info("平台添加客服成功，员工ID: {}", staff.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStaff(CustomerServiceStaff staff) {
        CustomerServiceStaff existingStaff = customerServiceStaffDao.selectById(staff.getId());
        if (existingStaff == null) {
            throw new CrmebException("客服不存在");
        }

        staff.setUpdateTime(new Date());
        customerServiceStaffDao.updateById(staff);
        log.info("更新客服信息成功，客服ID: {}", staff.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStaff(Integer staffId) {
        CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
        if (staff == null) {
            throw new CrmebException("客服不存在");
        }

        customerServiceStaffDao.deleteById(staffId);
        log.info("删除客服成功，客服ID: {}", staffId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStaffStatus(Integer staffId, String status) {
        CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
        if (staff == null) {
            throw new CrmebException("客服不存在");
        }

        staff.setOnlineStatus(status);
        staff.setUpdateTime(new Date());
        customerServiceStaffDao.updateById(staff);
        
        log.info("更新客服状态成功，客服ID: {}, 状态: {}", staffId, status);
    }

    // ==================== 控制器新增接口方法实现 ====================

    @Override
    public List<Map<String, Object>> getAvailableStaff() {
        try {
            List<CustomerServiceStaff> staffList = getAvailableStaff("STANDARD");
            
            return staffList.stream().map(staff -> {
                Map<String, Object> staffMap = new HashMap<>();
                staffMap.put("id", staff.getId());
                staffMap.put("staffNo", staff.getStaffNo());
                staffMap.put("staffName", staff.getStaffName());
                staffMap.put("name", staff.getStaffName()); // 兼容前端字段
                staffMap.put("avatar", staff.getAvatar());
                staffMap.put("serviceLevel", staff.getServiceLevel());
                staffMap.put("onlineStatus", staff.getOnlineStatus());
                staffMap.put("isOnline", "ONLINE".equals(staff.getOnlineStatus())); // 兼容前端字段
                staffMap.put("currentSessions", staff.getCurrentSessions());
                staffMap.put("maxConcurrentSessions", staff.getMaxConcurrentSessions());
                staffMap.put("skillTags", staff.getSkillTags());
                staffMap.put("createTime", staff.getCreateTime());
                staffMap.put("updateTime", staff.getUpdateTime());
                return staffMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取可用客服列表失败", e);
            return new ArrayList<>();
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStaff(CustomerServiceStaff staff) {
        try {
            staff.setMerId(getCurrentMerId());
            
            // 如果没有提供工号，自动生成
            if (staff.getStaffNo() == null || staff.getStaffNo().trim().isEmpty()) {
                staff.setStaffNo(generateStaffNo());
            }
            
            staff.setCurrentSessions(0);
            staff.setOnlineStatus("OFFLINE");
            staff.setCreateTime(new Date());
            staff.setUpdateTime(new Date());
            
            // 设置默认值
            if (staff.getMaxConcurrentSessions() == null) {
                staff.setMaxConcurrentSessions(5);
            }
            if (staff.getServiceLevel() == null) {
                staff.setServiceLevel("STANDARD");
            }
            if (staff.getStatus() == null) {
                staff.setStatus(true);
            }
            
            customerServiceStaffDao.insert(staff);
            log.info("客服添加成功: staffNo={}, staffName={}", staff.getStaffNo(), staff.getStaffName());
        } catch (Exception e) {
            log.error("添加客服失败", e);
            throw new CrmebException("添加客服失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getStaffStats(Integer staffId) {
        try {
            Map<String, Object> stats = getStaffStatistics(staffId);
            return stats;
        } catch (Exception e) {
            log.error("获取客服统计失败: staffId={}", staffId, e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String,Object>> getStaffStatsByMerId(Integer merchantId,boolean onlineOnly) {
        LambdaQueryWrapper<CustomerServiceStaff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerServiceStaff::getMerId, merchantId);
        queryWrapper.eq(CustomerServiceStaff::getOnlineStatus,"ONLINE");
        List<CustomerServiceStaff> staffList = customerServiceStaffDao.selectList(queryWrapper);
        return staffList.stream().map(staff -> {
            Map<String, Object> staffMap = new HashMap<>();
            staffMap.put("id", staff.getId());
            staffMap.put("staffId", staff.getAdminId());
            staffMap.put("staffNo", staff.getStaffNo());
            staffMap.put("staffName", staff.getStaffName());
            staffMap.put("name", staff.getStaffName()); // 兼容前端字段
            staffMap.put("avatar", staff.getAvatar());
            staffMap.put("serviceLevel", staff.getServiceLevel());
            staffMap.put("onlineStatus", staff.getOnlineStatus());
            staffMap.put("isOnline", "ONLINE".equals(staff.getOnlineStatus())); // 兼容前端字段
            staffMap.put("currentSessions", staff.getCurrentSessions());
            staffMap.put("maxConcurrentSessions", staff.getMaxConcurrentSessions());
            staffMap.put("skillTags", staff.getSkillTags());
            staffMap.put("createTime", staff.getCreateTime());
            staffMap.put("updateTime", staff.getUpdateTime());
            return staffMap;
        }).collect(Collectors.toList());
    }

    /**
     * 获取当前商户ID
     */
    private Integer getCurrentMerId() {
        // 从当前登录用户上下文获取商户ID
        return SecurityUtil.getLoginUserVo().getUser().getMerId();
    }
    /**
     * 获取当前商户ID
     */
    private Integer getCurrentUserId() {
        // 从当前登录用户上下文获取商户ID
        return SecurityUtil.getLoginUserVo().getUser().getId();
    }

    /**
     * 生成客服工号：KF + 6位顺序号
     */
    private String generateStaffNo() {
        try {
            Integer merId = getCurrentMerId();

            // 查询当前商户下最大的工号
            LambdaQueryWrapper<CustomerServiceStaff> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CustomerServiceStaff::getMerId, merId)
                    .like(CustomerServiceStaff::getStaffNo, "KF")
                    .orderByDesc(CustomerServiceStaff::getStaffNo)
                    .last("LIMIT 1");

            CustomerServiceStaff lastStaff = customerServiceStaffDao.selectOne(queryWrapper);

            int nextNumber = 1;
            if (lastStaff != null && lastStaff.getStaffNo() != null) {
                String lastStaffNo = lastStaff.getStaffNo();
                if (lastStaffNo.startsWith("KF") && lastStaffNo.length() == 8) {
                    try {
                        String numberPart = lastStaffNo.substring(2);
                        nextNumber = Integer.parseInt(numberPart) + 1;
                    } catch (NumberFormatException e) {
                        log.warn("解析最后工号失败: {}", lastStaffNo);
                        nextNumber = 1;
                    }
                }
            }

            // 生成6位数字，前面补0
            String staffNo = String.format("KF%06d", nextNumber);
            log.info("生成新的客服工号: {}", staffNo);
            return staffNo;

        } catch (Exception e) {
            log.error("生成客服工号失败", e);
            // 如果生成失败，使用时间戳作为后备方案
            long timestamp = System.currentTimeMillis() % 1000000;
            return String.format("KF%06d", timestamp);
        }
    }

    // ==================== 客服登录与管理新增方法实现 ====================

    @Autowired
    private com.zbkj.service.service.SystemAdminService systemAdminService;

    @Autowired
    private com.zbkj.service.service.SystemRoleService systemRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.zbkj.common.response.CustomerServiceLoginResponse login(
            com.zbkj.common.request.CustomerServiceLoginRequest request) {
        try {
            log.info("【客服登录】开始处理登录请求: account={}", request.getAccount());

            // 1. 验证账号是否存在
            SystemAdmin admin = systemAdminService.getByAccount(request.getAccount());
            if (admin == null) {
                log.warn("【客服登录】账号不存在: account={}", request.getAccount());
                throw new CrmebException("账号不存在");
            }

            // 2. 验证密码
            String encryptPassword = com.zbkj.common.utils.CrmebUtil.encryptPassword(
                request.getPassword(), request.getAccount());
            if (!encryptPassword.equals(admin.getPwd())) {
                log.warn("【客服登录】密码错误: account={}", request.getAccount());
                throw new CrmebException("密码错误");
            }

            // 3. 验证账号状态
            if (!admin.getStatus()) {
                log.warn("【客服登录】账号已被禁用: account={}", request.getAccount());
                throw new CrmebException("账号已被禁用");
            }

            // 4. 获取或创建客服记录
            CustomerServiceStaff staff = getByAdminId(admin.getId());
            if (staff == null) {
                log.info("【客服登录】首次登录，创建客服记录: adminId={}", admin.getId());
                staff = createStaffFromAdmin(admin);
            }

            // 5. 更新在线状态
            updateOnlineStatus(admin.getId(), "ONLINE");
            log.info("【客服登录】更新在线状态: adminId={}, status=ONLINE", admin.getId());

            // 6. 生成Token（使用简化版本，返回adminId作为token的一部分）
            // 注意：前端和WebSocket需要使用adminId来标识客服
            String tokenValue = java.util.UUID.randomUUID().toString().replace("-", "");
            String token = "customer_service_" + tokenValue;

            log.info("【客服登录】生成Token成功: adminId={}, token={}", admin.getId(), token.substring(0, 20) + "...");

            // 7. 构造响应
            com.zbkj.common.response.CustomerServiceLoginResponse response =
                new com.zbkj.common.response.CustomerServiceLoginResponse();
            response.setToken(token);
            response.setAdminId(admin.getId());  // 重要：返回adminId，用于后续所有操作
            response.setStaffId(staff.getId());   // staffId仅用于显示
            response.setAccount(admin.getAccount());
            response.setStaffName(staff.getStaffName());
            response.setAvatar(staff.getAvatar());
            response.setUserType("CUSTOMER_SERVICE");
            response.setMerId(admin.getMerId());
            response.setServiceLevel(staff.getServiceLevel());
            response.setMaxConcurrentSessions(staff.getMaxConcurrentSessions());
            response.setCurrentSessions(staff.getCurrentSessions());
            response.setExpiresIn(86400L); // 24小时

            log.info("【客服登录】登录成功 ✅: adminId={}, staffId={}, staffName={}, merId={}",
                    admin.getId(), staff.getId(), staff.getStaffName(), admin.getMerId());
            return response;

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            log.error("【客服登录】登录失败: {}", e.getMessage(), e);
            throw new CrmebException("登录失败: " + e.getMessage());
        }
    }

    @Override
    public CustomerServiceStaff getByAdminId(Integer adminId) {
        try {
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getAdminId, adminId);
            return customerServiceStaffDao.selectOne(wrapper);
        } catch (Exception e) {
            log.error("根据管理员ID获取客服失败: adminId={}", adminId, e);
            return null;
        }
    }

    /**
     * 从管理员信息创建客服记录
     */
    private CustomerServiceStaff createStaffFromAdmin(com.zbkj.common.model.admin.SystemAdmin admin) {
        CustomerServiceStaff staff = new CustomerServiceStaff();
        staff.setAdminId(admin.getId());
        staff.setMerId(admin.getMerId());
        staff.setAccount(admin.getAccount());
        staff.setStaffName(admin.getRealName());
        staff.setStaffNo(generateStaffNo(admin.getMerId()));
        staff.setServiceLevel("STANDARD");
        staff.setMaxConcurrentSessions(10);
        staff.setCurrentSessions(0);
        staff.setOnlineStatus("OFFLINE");
        staff.setTotalServedSessions(0);
        staff.setAverageResponseTime(0);
        staff.setSatisfactionRating(BigDecimal.valueOf(5.0));
        staff.setStatus(true);
        staff.setIsDefault(false);
        staff.setCreateTime(new Date());
        staff.setUpdateTime(new Date());

        customerServiceStaffDao.insert(staff);
        log.info("创建客服记录: adminId={}, staffId={}", admin.getId(), staff.getId());
        return staff;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerServiceStaff createCustomerService(
            com.zbkj.common.request.CustomerServiceRequest request, Integer merId) {
        try {
            log.info("创建客服账号: account={}, merId={}", request.getAccount(), merId);

            // 1. 验证账号是否已存在
            com.zbkj.common.model.admin.SystemAdmin existingAdmin =
                systemAdminService.getByAccount(request.getAccount());
            if (existingAdmin != null) {
                throw new CrmebException("账号已存在");
            }

            // 2. 创建system_admin记录
            com.zbkj.common.model.admin.SystemAdmin admin =
                new com.zbkj.common.model.admin.SystemAdmin();
            admin.setAccount(request.getAccount());
            admin.setPwd(com.zbkj.common.utils.CrmebUtil.encryptPassword(
                request.getPassword(), request.getAccount()));
            admin.setRealName(request.getStaffName());
            admin.setMerId(merId);
            admin.setLevel(1);
            admin.setStatus(request.getStatus() != null ? request.getStatus() : true);
            admin.setIsDel(false);
            admin.setCreateTime(new Date());

            // 获取客服角色ID
           SystemRole customerServiceRole =
                systemRoleService.getByRoleName("客服人员");
            if (customerServiceRole != null) {
                admin.setRoles(customerServiceRole.getId().toString());
            }

            systemAdminService.save(admin);

            // 3. 创建customer_service_staff记录
            CustomerServiceStaff staff = new CustomerServiceStaff();
            staff.setAdminId(admin.getId());
            staff.setMerId(merId);
            staff.setAccount(request.getAccount());
            staff.setStaffName(request.getStaffName());
            staff.setPhone(request.getPhone());
            staff.setEmail(request.getEmail());
            staff.setAvatar(request.getAvatar());
            staff.setServiceLevel(request.getServiceLevel() != null ?
                request.getServiceLevel() : "STANDARD");
            staff.setSkillTags(request.getSkillTags());
            staff.setMaxConcurrentSessions(request.getMaxConcurrentSessions());
            staff.setCurrentSessions(0);
            staff.setOnlineStatus("OFFLINE");
            staff.setTotalServedSessions(0);
            staff.setAverageResponseTime(0);
            staff.setSatisfactionRating(BigDecimal.valueOf(5.0));
            staff.setStatus(request.getStatus() != null ? request.getStatus() : true);
            staff.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
            staff.setStaffNo(generateStaffNo(merId));
            staff.setCreateTime(new Date());
            staff.setUpdateTime(new Date());

            customerServiceStaffDao.insert(staff);

            log.info("创建客服成功: staffId={}, account={}", staff.getId(), request.getAccount());
            return staff;

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建客服失败", e);
            throw new CrmebException("创建客服失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerService(com.zbkj.common.request.CustomerServiceRequest request) {
        try {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(request.getStaffId());
            if (staff == null) {
                throw new CrmebException("客服不存在");
            }

            // 更新客服信息
            if (request.getStaffName() != null) {
                staff.setStaffName(request.getStaffName());
            }
            if (request.getPhone() != null) {
                staff.setPhone(request.getPhone());
            }
            if (request.getEmail() != null) {
                staff.setEmail(request.getEmail());
            }
            if (request.getAvatar() != null) {
                staff.setAvatar(request.getAvatar());
            }
            if (request.getServiceLevel() != null) {
                staff.setServiceLevel(request.getServiceLevel());
            }
            if (request.getSkillTags() != null) {
                staff.setSkillTags(request.getSkillTags());
            }
            if (request.getMaxConcurrentSessions() != null) {
                staff.setMaxConcurrentSessions(request.getMaxConcurrentSessions());
            }
            if (request.getStatus() != null) {
                staff.setStatus(request.getStatus());
            }
            if (request.getIsDefault() != null) {
                staff.setIsDefault(request.getIsDefault());
            }

            staff.setUpdateTime(new Date());
            customerServiceStaffDao.updateById(staff);

            // 如果修改了密码，同步更新system_admin
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                com.zbkj.common.model.admin.SystemAdmin admin =
                    systemAdminService.getById(staff.getAdminId());
                if (admin != null) {
                    admin.setPwd(com.zbkj.common.utils.CrmebUtil.encryptPassword(
                        request.getPassword(), admin.getAccount()));
                    systemAdminService.updateById(admin);
                }
            }

            log.info("更新客服成功: staffId={}", request.getStaffId());

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新客服失败", e);
            throw new CrmebException("更新客服失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomerService(Integer staffId) {
        try {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
            if (staff == null) {
                throw new CrmebException("客服不存在");
            }

            // 删除客服记录
            customerServiceStaffDao.deleteById(staffId);

            // 同时删除或禁用对应的system_admin记录
            com.zbkj.common.model.admin.SystemAdmin admin =
                systemAdminService.getById(staff.getAdminId());
            if (admin != null) {
                admin.setIsDel(true);
                systemAdminService.updateById(admin);
            }

            log.info("删除客服成功: staffId={}", staffId);

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除客服失败", e);
            throw new CrmebException("删除客服失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer staffId, Boolean status) {
        try {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
            if (staff == null) {
                throw new CrmebException("客服不存在");
            }

            staff.setStatus(status);
            staff.setUpdateTime(new Date());
            customerServiceStaffDao.updateById(staff);

            // 同步更新system_admin状态
            com.zbkj.common.model.admin.SystemAdmin admin =
                systemAdminService.getById(staff.getAdminId());
            if (admin != null) {
                admin.setStatus(status);
                systemAdminService.updateById(admin);
            }

            log.info("更新客服状态: staffId={}, status={}", staffId, status);

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新客服状态失败", e);
            throw new CrmebException("更新客服状态失败: " + e.getMessage());
        }
    }

    @Override
    public CommonPage<CustomerServiceStaff> getPageList(Integer merId, String keyword,
                                                         String onlineStatus, Boolean status,
                                                         PageParamRequest pageParamRequest) {
        try {
            PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getMerId, merId);

            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w
                    .like(CustomerServiceStaff::getStaffName, keyword)
                    .or().like(CustomerServiceStaff::getAccount, keyword)
                    .or().like(CustomerServiceStaff::getPhone, keyword)
                );
            }

            if (StringUtils.hasText(onlineStatus)) {
                wrapper.eq(CustomerServiceStaff::getOnlineStatus, onlineStatus);
            }

            if (status != null) {
                wrapper.eq(CustomerServiceStaff::getStatus, status);
            }

            wrapper.orderByDesc(CustomerServiceStaff::getCreateTime);

            List<CustomerServiceStaff> list = customerServiceStaffDao.selectList(wrapper);
            PageInfo<CustomerServiceStaff> pageInfo = new PageInfo<>(list);

            return CommonPage.restPage(pageInfo);

        } catch (Exception e) {
            log.error("获取客服列表失败", e);
            throw new CrmebException("获取客服列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Integer staffId, String newPassword) {
        try {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
            if (staff == null) {
                throw new CrmebException("客服不存在");
            }

            // 重置system_admin的密码
            com.zbkj.common.model.admin.SystemAdmin admin =
                systemAdminService.getById(staff.getAdminId());
            if (admin != null) {
                admin.setPwd(com.zbkj.common.utils.CrmebUtil.encryptPassword(
                    newPassword, admin.getAccount()));
                systemAdminService.updateById(admin);
            }

            log.info("重置客服密码成功: staffId={}", staffId);

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            log.error("重置客服密码失败", e);
            throw new CrmebException("重置客服密码失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultStaff(Integer staffId, Integer merId) {
        try {
            // 1. 取消当前默认客服
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getMerId, merId)
                   .eq(CustomerServiceStaff::getIsDefault, true);
            List<CustomerServiceStaff> defaultStaffList = customerServiceStaffDao.selectList(wrapper);
            for (CustomerServiceStaff staff : defaultStaffList) {
                staff.setIsDefault(false);
                staff.setUpdateTime(new Date());
                customerServiceStaffDao.updateById(staff);
            }

            // 2. 设置新的默认客服
            CustomerServiceStaff newDefaultStaff = customerServiceStaffDao.selectById(staffId);
            if (newDefaultStaff == null) {
                throw new CrmebException("客服不存在");
            }

            newDefaultStaff.setIsDefault(true);
            newDefaultStaff.setUpdateTime(new Date());
            customerServiceStaffDao.updateById(newDefaultStaff);

            log.info("设置默认客服成功: staffId={}, merId={}", staffId, merId);

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            log.error("设置默认客服失败", e);
            throw new CrmebException("设置默认客服失败: " + e.getMessage());
        }
    }

    @Override
    public String generateStaffNo(Integer merId) {
        try {
            // 查询商户下最大的工号
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getMerId, merId)
                   .likeRight(CustomerServiceStaff::getStaffNo, "CS" + merId)
                   .orderByDesc(CustomerServiceStaff::getStaffNo)
                   .last("LIMIT 1");

            CustomerServiceStaff lastStaff = customerServiceStaffDao.selectOne(wrapper);

            int nextNumber = 1;
            if (lastStaff != null && lastStaff.getStaffNo() != null) {
                String lastStaffNo = lastStaff.getStaffNo();
                String prefix = "CS" + merId;
                if (lastStaffNo.startsWith(prefix)) {
                    try {
                        String numberPart = lastStaffNo.substring(prefix.length());
                        nextNumber = Integer.parseInt(numberPart) + 1;
                    } catch (NumberFormatException e) {
                        log.warn("解析工号失败: {}", lastStaffNo);
                    }
                }
            }

            String staffNo = String.format("CS%d%03d", merId, nextNumber);
            log.info("生成客服工号: {}", staffNo);
            return staffNo;

        } catch (Exception e) {
            log.error("生成客服工号失败", e);
            return String.format("CS%d%03d", merId, (int)(Math.random() * 1000));
        }
    }

    @Override
    public List<CustomerServiceStaff> getOnlineStaff(Integer merId) {
        try {
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getMerId, merId)
                   .eq(CustomerServiceStaff::getStatus, true)
                   .eq(CustomerServiceStaff::getOnlineStatus, "ONLINE")
                   .orderByAsc(CustomerServiceStaff::getCurrentSessions);

            return customerServiceStaffDao.selectList(wrapper);

        } catch (Exception e) {
            log.error("获取在线客服列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<CustomerServiceStaff> getAvailableStaff(Long merId) {
        try {
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getMerId, merId.intValue())
                   .eq(CustomerServiceStaff::getStatus, true)
                   .eq(CustomerServiceStaff::getOnlineStatus, "ONLINE")
                   .orderByAsc(CustomerServiceStaff::getCurrentSessions);

            List<CustomerServiceStaff> allOnlineStaff = customerServiceStaffDao.selectList(wrapper);

            // 过滤出未达到最大会话数的客服
            return allOnlineStaff.stream()
                .filter(staff -> staff.getCurrentSessions() < staff.getMaxConcurrentSessions())
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取可用客服列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCurrentSessions(Integer staffId) {
        try {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
            if (staff != null) {
                int newSessions = staff.getCurrentSessions() + 1;
                staff.setCurrentSessions(newSessions);
                staff.setUpdateTime(new Date());
                customerServiceStaffDao.updateById(staff);

                log.info("增加客服会话数: staffId={}, currentSessions={}", staffId, newSessions);
            }
        } catch (Exception e) {
            log.error("增加客服会话数失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementCurrentSessions(Integer staffId) {
        try {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
            if (staff != null) {
                int newSessions = Math.max(0, staff.getCurrentSessions() - 1);
                staff.setCurrentSessions(newSessions);
                staff.setUpdateTime(new Date());
                customerServiceStaffDao.updateById(staff);

                log.info("减少客服会话数: staffId={}, currentSessions={}", staffId, newSessions);
            }
        } catch (Exception e) {
            log.error("减少客服会话数失败", e);
        }
    }

    @Override
    public CustomerServiceStaff getDefaultStaff(Long merId) {
        try {
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getMerId, merId.intValue())
                   .eq(CustomerServiceStaff::getIsDefault, true)
                   .eq(CustomerServiceStaff::getStatus, true)
                   .last("LIMIT 1");

            return customerServiceStaffDao.selectOne(wrapper);

        } catch (Exception e) {
            log.error("获取默认客服失败", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getStaffStatistics(Integer staffId, String startDate, String endDate) {
        try {
            CustomerServiceStaff staff = customerServiceStaffDao.selectById(staffId);
            if (staff == null) {
                throw new CrmebException("客服不存在");
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("staffId", staffId);
            statistics.put("staffName", staff.getStaffName());
            statistics.put("totalServedSessions", staff.getTotalServedSessions());
            statistics.put("averageResponseTime", staff.getAverageResponseTime());
            statistics.put("satisfactionRating", staff.getSatisfactionRating());
            statistics.put("currentSessions", staff.getCurrentSessions());
            statistics.put("maxConcurrentSessions", staff.getMaxConcurrentSessions());
            statistics.put("onlineStatus", staff.getOnlineStatus());
            statistics.put("lastOnlineTime", staff.getLastOnlineTime());

            // 这里可以扩展：根据日期范围查询更详细的统计数据
            // 例如：从 eb_customer_service_work_record 表查询

            return statistics;

        } catch (Exception e) {
            log.error("获取客服统计失败", e);
            throw new CrmebException("获取客服统计失败: " + e.getMessage());
        }
    }

    @Override
    public Integer getPendingSessionCount(Integer staffId) {
        try {
            // 这里需要查询统一聊天会话表
            // 暂时返回0，实际使用时需要注入UnifiedChatSessionService并查询
            return 0;

        } catch (Exception e) {
            log.error("获取待处理会话数失败", e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerServiceStaff createOrGetDefaultStaffForMerchant(Long merId) {
        try {
            // 1. 先尝试获取已存在的默认客服
            CustomerServiceStaff existingStaff = getDefaultStaff(merId);
            if (existingStaff != null) {
                log.info("【自动创建客服】商户已有默认客服: merId={}, staffId={}, staffName={}",
                        merId, existingStaff.getId(), existingStaff.getStaffName());
                return existingStaff;
            }

            // 2. 获取商户的超级管理员
            com.zbkj.common.model.admin.SystemAdmin superAdmin =
                systemAdminService.getSuperAdminByPlat(merId.intValue());

            if (superAdmin == null) {
                log.error("【自动创建客服】未找到商户超级管理员: merId={}", merId);
                return null;
            }

            // 3. 检查该管理员是否已经有客服账号
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getAdminId, superAdmin.getId())
                   .eq(CustomerServiceStaff::getMerId, merId.intValue());
            CustomerServiceStaff existingStaffByAdmin = customerServiceStaffDao.selectOne(wrapper);

            if (existingStaffByAdmin != null) {
                // 如果已存在，设置为默认客服
                existingStaffByAdmin.setIsDefault(true);
                existingStaffByAdmin.setStatus(true);
                customerServiceStaffDao.updateById(existingStaffByAdmin);
                log.info("【自动创建客服】将现有客服设置为默认: merId={}, adminId={}, staffId={}",
                        merId, superAdmin.getId(), existingStaffByAdmin.getId());
                return existingStaffByAdmin;
            }

            // 4. 为超级管理员创建默认客服账号
            CustomerServiceStaff newStaff = new CustomerServiceStaff();
            newStaff.setAdminId(superAdmin.getId());
            newStaff.setMerId(merId.intValue());
            newStaff.setStaffNo(generateStaffNo(merId.intValue()));
            newStaff.setStaffName(superAdmin.getRealName() != null ? superAdmin.getRealName() : superAdmin.getAccount());
            newStaff.setAccount(superAdmin.getAccount());
            newStaff.setPhone(superAdmin.getPhone() != null ? superAdmin.getPhone() : "");
            newStaff.setEmail(""); // 管理员表没有email字段
            newStaff.setAvatar(superAdmin.getHeaderImage() != null ? superAdmin.getHeaderImage() : "");
            newStaff.setServiceLevel("STANDARD");
            newStaff.setSkillTags("[]");
            newStaff.setMaxConcurrentSessions(10);
            newStaff.setCurrentSessions(0);
            newStaff.setOnlineStatus(CustomerServiceStaff.ONLINE_STATUS_OFFLINE);
            newStaff.setTotalServedSessions(0);
            newStaff.setAverageResponseTime(0);
            newStaff.setSatisfactionRating(new BigDecimal("5.0"));
            newStaff.setIsDefault(true); // 设置为默认客服
            newStaff.setStatus(true);
            newStaff.setCreateTime(new Date());
            newStaff.setUpdateTime(new Date());

            int result = customerServiceStaffDao.insert(newStaff);
            if (result > 0) {
                log.info("【自动创建客服】成功为商户超级管理员创建默认客服: merId={}, adminId={}, staffId={}, staffName={}",
                        merId, superAdmin.getId(), newStaff.getId(), newStaff.getStaffName());
                return newStaff;
            } else {
                log.error("【自动创建客服】创建客服失败: merId={}, adminId={}", merId, superAdmin.getId());
                return null;
            }

        } catch (Exception e) {
            log.error("【自动创建客服】为商户创建默认客服失败: merId={}, 错误: {}", merId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerServiceStaff createOrGetDefaultStaffForPlatform() {
        try {
            // 平台ID固定为0
            Long platformMerId = 0L;

            // 1. 先尝试获取已存在的默认客服
            CustomerServiceStaff existingStaff = getDefaultStaff(platformMerId);
            if (existingStaff != null) {
                log.info("【自动创建客服】平台已有默认客服: staffId={}, staffName={}",
                        existingStaff.getId(), existingStaff.getStaffName());
                return existingStaff;
            }

            // 2. 获取平台超级管理员（type=1的超管）
            LambdaQueryWrapper<com.zbkj.common.model.admin.SystemAdmin> adminWrapper =
                new LambdaQueryWrapper<>();
            adminWrapper.eq(com.zbkj.common.model.admin.SystemAdmin::getType,
                           com.zbkj.common.enums.RoleEnum.SUPER_ADMIN.getValue()) // type=1
                       .eq(com.zbkj.common.model.admin.SystemAdmin::getIsDel, false)
                       .last("LIMIT 1");

            com.zbkj.common.model.admin.SystemAdmin superAdmin =
                systemAdminService.getOne(adminWrapper);

            if (superAdmin == null) {
                log.error("【自动创建客服】未找到平台超级管理员");
                return null;
            }

            // 3. 检查该管理员是否已经有客服账号
            LambdaQueryWrapper<CustomerServiceStaff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceStaff::getAdminId, superAdmin.getId())
                   .eq(CustomerServiceStaff::getMerId, 0);
            CustomerServiceStaff existingStaffByAdmin = customerServiceStaffDao.selectOne(wrapper);

            if (existingStaffByAdmin != null) {
                // 如果已存在，设置为默认客服
                existingStaffByAdmin.setIsDefault(true);
                existingStaffByAdmin.setStatus(true);
                customerServiceStaffDao.updateById(existingStaffByAdmin);
                log.info("【自动创建客服】将平台现有客服设置为默认: adminId={}, staffId={}",
                        superAdmin.getId(), existingStaffByAdmin.getId());
                return existingStaffByAdmin;
            }

            // 4. 为平台超级管理员创建默认客服账号
            CustomerServiceStaff newStaff = new CustomerServiceStaff();
            newStaff.setAdminId(superAdmin.getId());
            newStaff.setMerId(0); // 平台客服merId=0
            newStaff.setStaffNo(generateStaffNo(0));
            newStaff.setStaffName(superAdmin.getRealName() != null ? superAdmin.getRealName() : superAdmin.getAccount());
            newStaff.setAccount(superAdmin.getAccount());
            newStaff.setPhone(superAdmin.getPhone() != null ? superAdmin.getPhone() : "");
            newStaff.setEmail(""); // 管理员表没有email字段
            newStaff.setAvatar(superAdmin.getHeaderImage() != null ? superAdmin.getHeaderImage() : "");
            newStaff.setServiceLevel("EXPERT"); // 平台客服默认为专家级
            newStaff.setSkillTags("[]");
            newStaff.setMaxConcurrentSessions(20); // 平台客服可以处理更多会话
            newStaff.setCurrentSessions(0);
            newStaff.setOnlineStatus(CustomerServiceStaff.ONLINE_STATUS_OFFLINE);
            newStaff.setTotalServedSessions(0);
            newStaff.setAverageResponseTime(0);
            newStaff.setSatisfactionRating(new BigDecimal("5.0"));
            newStaff.setIsDefault(true); // 设置为默认客服
            newStaff.setStatus(true);
            newStaff.setCreateTime(new Date());
            newStaff.setUpdateTime(new Date());

            int result = customerServiceStaffDao.insert(newStaff);
            if (result > 0) {
                log.info("【自动创建客服】成功为平台超级管理员创建默认客服: adminId={}, staffId={}, staffName={}",
                        superAdmin.getId(), newStaff.getId(), newStaff.getStaffName());
                return newStaff;
            } else {
                log.error("【自动创建客服】创建平台客服失败: adminId={}", superAdmin.getId());
                return null;
            }

        } catch (Exception e) {
            log.error("【自动创建客服】为平台创建默认客服失败，错误: {}", e.getMessage(), e);
            return null;
        }
    }
}
