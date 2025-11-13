package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.service.CustomerServiceStaff;
import com.zbkj.common.token.FrontTokenComponent;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.request.ContactManageRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.model.service.ContactRelationship;
import com.zbkj.service.dao.ContactRelationshipDao;
import com.zbkj.service.service.*;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.admin.SystemAdmin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 联系人关系服务实现
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class ContactRelationshipServiceImpl implements ContactRelationshipService {

    @Autowired
    private ContactRelationshipDao contactRelationshipDao;

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private SystemAdminService systemAdminService;
    @Autowired
    private FrontTokenComponent frontTokenComponent;
    @Autowired
    private CustomerServiceStaffService customerServiceStaffService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactRelationship addContact(ContactManageRequest request) {
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();

        // 验证联系人是否真实存在
        validateContactExists(request.getContactId(), request.getContactType());

        // 检查联系人关系是否已存在
        LambdaQueryWrapper<ContactRelationship> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactRelationship::getOwnerId, currentUserId)
            .eq(ContactRelationship::getOwnerType, currentUserType)
            .eq(ContactRelationship::getContactId, request.getContactId())
            .eq(ContactRelationship::getContactType, request.getContactType())
            .eq(ContactRelationship::getStatus, ContactRelationship.STATUS_NORMAL);
        ContactRelationship existingContact = contactRelationshipDao.selectOne(queryWrapper);
        
        if (existingContact != null) {
            throw new CrmebException("该联系人已存在，无需重复添加");
        }

        // 创建新的联系人关系
        ContactRelationship contact = new ContactRelationship();
        contact.setOwnerId(currentUserId);
        contact.setOwnerType(currentUserType);
        contact.setContactId(request.getContactId());
        contact.setContactType(request.getContactType());
        contact.setContactName(request.getContactName());
        contact.setContactAvatar(request.getContactAvatar());
        contact.setContactPhone(request.getContactPhone());
        contact.setNotes(request.getNotes());
        contact.setGroupName(StringUtils.hasText(request.getGroupName()) ? request.getGroupName() : ContactRelationship.DEFAULT_GROUP);
        contact.setIsPinned(request.getIsPinned() != null ? request.getIsPinned() : false);
        contact.setLastContactTime(new Date());
        contact.setStatus(ContactRelationship.STATUS_NORMAL);
        contact.setCreateTime(new Date());
        contact.setUpdateTime(new Date());

        contactRelationshipDao.insert(contact);
        
        log.info("添加联系人成功，拥有者ID: {}, 联系人ID: {}, 联系人类型: {}", 
                currentUserId, request.getContactId(), request.getContactType());
        
        return contact;
    }

    /**
     * 验证联系人是否真实存在
     */
    private void validateContactExists(Integer contactId, String contactType) {
        boolean exists = false;
        
        try {
            switch (contactType) {
                case "USER":
                    User user = userService.getById(contactId);
                    exists = user != null && user.getStatus();
                    break;
                case "MERCHANT":
                    SystemAdmin merAdmin = systemAdminService.getById(contactId);
                    exists = merAdmin != null && merAdmin.getStatus();
                    break;
                case "PLATFORM":
                    SystemAdmin admin = systemAdminService.getById(contactId);
                    exists = admin != null && admin.getStatus();
                    break;
                default:
                    throw new CrmebException("不支持的联系人类型: " + contactType);
            }
        } catch (Exception e) {
            log.error("验证联系人存在性失败", e);
            throw new CrmebException("验证联系人失败: " + e.getMessage());
        }
        
        if (!exists) {
            throw new CrmebException("联系人不存在或已被禁用，无法添加");
        }
    }

    @Override
    public List<ContactRelationship> getContactList(String contactType, String groupName) {
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        LambdaQueryWrapper<ContactRelationship> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(groupName)){
            queryWrapper.eq(ContactRelationship::getGroupName, groupName);
        }
        if(StringUtils.hasText(contactType)){
            queryWrapper.eq(ContactRelationship::getContactType, contactType);
        }
        queryWrapper.eq(ContactRelationship::getOwnerId, currentUserId)
            .eq(ContactRelationship::getOwnerType, currentUserType)
            .eq(ContactRelationship::getStatus, ContactRelationship.STATUS_NORMAL);
            return contactRelationshipDao.selectList(queryWrapper);
    }

    @Override
    public com.github.pagehelper.PageInfo<ContactRelationship> getContactListWithPage(String contactType, String groupName, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100; // 限制最大页面大小

        // 使用 PageHelper 进行分页
        PageHelper.startPage(page, size);

        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        LambdaQueryWrapper<ContactRelationship> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(groupName)){
            queryWrapper.eq(ContactRelationship::getGroupName, groupName);
        }
        if(StringUtils.hasText(contactType)){
            queryWrapper.eq(ContactRelationship::getContactType, contactType);
        }
        queryWrapper.eq(ContactRelationship::getOwnerId, currentUserId)
            .eq(ContactRelationship::getOwnerType, currentUserType)
            .eq(ContactRelationship::getStatus, ContactRelationship.STATUS_NORMAL);
        
        List<ContactRelationship> list = contactRelationshipDao.selectList(queryWrapper);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContact(Long contactId) {
        ContactRelationship contact = contactRelationshipDao.selectById(contactId);
        if (contact == null) {
            throw new CrmebException("联系人不存在");
        }

        // 验证权限（确保是当前用户的联系人）
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        
        if (!contact.getOwnerId().equals(currentUserId) || !contact.getOwnerType().equals(currentUserType)) {
            throw new CrmebException("无权限删除该联系人");
        }

        // 软删除
        contact.setStatus(false);
        contact.setUpdateTime(new Date());
        contactRelationshipDao.updateById(contact);

        log.info("删除联系人，联系人ID: {}, 拥有者: {}", contactId, currentUserId);
    }

    @Override
    public Map<String, Object> getContactOnlineStatus(Integer contactId, String contactType) {
        Map<String, Object> status = new HashMap<>();
        
        // 根据联系人类型获取在线状态
        boolean isOnline = false;
        String lastOnlineTime = null;
        
        switch (contactType) {
            case ContactRelationship.CONTACT_TYPE_USER:
                // 查询用户在线状态
                isOnline = isUserOnline(contactId);
                break;
            case ContactRelationship.CONTACT_TYPE_MERCHANT:
                // 查询商户在线状态
                isOnline = isMerchantOnline(contactId);
                break;
            case ContactRelationship.CONTACT_TYPE_PLATFORM:
                // 查询平台管理员在线状态
                isOnline = isPlatformAdminOnline(contactId);
                break;
        }
        
        status.put("contactId", contactId);
        status.put("contactType", contactType);
        status.put("isOnline", isOnline);
        status.put("lastOnlineTime", lastOnlineTime);
        
        return status;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContact(Long contactId, ContactManageRequest request) {
        ContactRelationship contact = contactRelationshipDao.selectById(contactId);
        if (contact == null) {
            throw new CrmebException("联系人不存在");
        }

        // 验证权限
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        
        if (!contact.getOwnerId().equals(currentUserId) || !contact.getOwnerType().equals(currentUserType)) {
            throw new CrmebException("无权限修改该联系人");
        }

        // 更新联系人信息
        contact.setContactName(request.getContactName());
        contact.setContactAvatar(request.getContactAvatar());
        contact.setContactPhone(request.getContactPhone());
        contact.setNotes(request.getNotes());
        contact.setGroupName(request.getGroupName());
        contact.setUpdateTime(new Date());

        contactRelationshipDao.updateById(contact);
        log.info("更新联系人信息，联系人ID: {}", contactId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pinContact(Long contactId, Boolean isPinned) {
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        
        LambdaQueryWrapper<ContactRelationship> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactRelationship::getContactId, contactId)
                   .eq(ContactRelationship::getOwnerId, currentUserId)
                   .eq(ContactRelationship::getOwnerType, currentUserType)
                   .eq(ContactRelationship::getStatus, ContactRelationship.STATUS_NORMAL);
        
        ContactRelationship contact = contactRelationshipDao.selectOne(queryWrapper);
        
        if (contact == null) {
            // 联系人不存在，需要先创建联系人关系
            log.info("联系人关系不存在，创建联系人关系，contactId: {}, ownerId: {}", contactId, currentUserId);
            
            // 确定联系人类型
            String contactType = determineContactTypeById(contactId);
            
            // 验证联系人是否真实存在
            validateContactExists(contactId.intValue(), contactType);
            
            // 获取联系人信息
            Map<String, String> contactInfo = getContactInfoById(contactId, contactType);
            
            // 创建新的联系人关系
            contact = new ContactRelationship();
            contact.setOwnerId(currentUserId);
            contact.setOwnerType(currentUserType);
            contact.setContactId(contactId.intValue());
            contact.setContactType(contactType);
            contact.setContactName(contactInfo.get("name"));
            contact.setContactAvatar(contactInfo.get("avatar"));
            contact.setContactPhone(contactInfo.get("phone"));
            contact.setNotes("");
            contact.setGroupName(ContactRelationship.DEFAULT_GROUP);
            contact.setIsPinned(isPinned);
            contact.setLastContactTime(new Date());
            contact.setStatus(ContactRelationship.STATUS_NORMAL);
            contact.setCreateTime(new Date());
            contact.setUpdateTime(new Date());
            
            contactRelationshipDao.insert(contact);
        } else {
            // 联系人存在，更新置顶状态
            contact.setIsPinned(isPinned);
            contact.setUpdateTime(new Date());
            contactRelationshipDao.updateById(contact);
        }

        log.info("更新联系人置顶状态，联系人ID: {}, 置顶: {}", contactId, isPinned);
    }
    
    /**
     * 确定联系人类型
     */
    private String determineContactTypeById(Long contactId) {
        try {
            // 1. 先检查是否是系统管理员
            SystemAdmin admin = systemAdminService.getById(contactId.intValue());
            if (admin != null) {
                // 商户管理员
                if (admin.getMerId() != null && admin.getMerId() > 0) {
                    return ContactRelationship.CONTACT_TYPE_MERCHANT;
                }
                // 平台管理员
                else {
                    return ContactRelationship.CONTACT_TYPE_PLATFORM;
                }
            }
            
            // 2. 检查是否是普通用户
            User user = userService.getById(contactId.intValue());
            if (user != null) {
                return ContactRelationship.CONTACT_TYPE_USER;
            }
            
            // 3. 默认返回MERCHANT
            log.warn("无法确定联系人类型，ID: {}，默认为MERCHANT", contactId);
            return ContactRelationship.CONTACT_TYPE_MERCHANT;
        } catch (Exception e) {
            log.error("确定联系人类型失败，ID: {}, 错误: {}", contactId, e.getMessage());
            return ContactRelationship.CONTACT_TYPE_MERCHANT; // 默认类型
        }
    }
    
    /**
     * 根据ID和类型获取联系人信息
     */
    private Map<String, String> getContactInfoById(Long contactId, String contactType) {
        Map<String, String> contactInfo = new HashMap<>();
        
        try {
            switch (contactType) {
                case ContactRelationship.CONTACT_TYPE_USER:
                    User user = userService.getById(contactId.intValue());
                    if (user != null) {
                        contactInfo.put("name", StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname());
                        contactInfo.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
                        contactInfo.put("phone", user.getPhone() != null ? user.getPhone() : "");
                    }
                    break;
                case ContactRelationship.CONTACT_TYPE_MERCHANT:
                    SystemAdmin merchantAdmin = systemAdminService.getById(contactId.intValue());
                    if (merchantAdmin != null) {
                        contactInfo.put("name", StringUtils.hasText(merchantAdmin.getRealName()) ? merchantAdmin.getRealName() : merchantAdmin.getAccount());
                        contactInfo.put("avatar", merchantAdmin.getHeaderImage() != null ? merchantAdmin.getHeaderImage() : "");
                        contactInfo.put("phone", merchantAdmin.getPhone() != null ? merchantAdmin.getPhone() : "");
                    }
                    break;
                case ContactRelationship.CONTACT_TYPE_PLATFORM:
                    SystemAdmin platformAdmin = systemAdminService.getById(contactId.intValue());
                    if (platformAdmin != null) {
                        contactInfo.put("name", StringUtils.hasText(platformAdmin.getRealName()) ? platformAdmin.getRealName() : platformAdmin.getAccount());
                        contactInfo.put("avatar", platformAdmin.getHeaderImage() != null ? platformAdmin.getHeaderImage() : "");
                        contactInfo.put("phone", platformAdmin.getPhone() != null ? platformAdmin.getPhone() : "");
                    }
                    break;
            }
        } catch (Exception e) {
            log.warn("获取联系人信息失败: contactId={}, contactType={}, 错误: {}", contactId, contactType, e.getMessage());
        }
        
        // 设置默认值
        if (!contactInfo.containsKey("name")) {
            contactInfo.put("name", contactType + contactId);
        }
        if (!contactInfo.containsKey("avatar")) {
            contactInfo.put("avatar", "");
        }
        if (!contactInfo.containsKey("phone")) {
            contactInfo.put("phone", "");
        }
        
        return contactInfo;
    }

    @Override
    public List<String> getContactGroups() {
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();
        
        return contactRelationshipDao.selectGroupsByOwner(currentUserId, currentUserType);
    }

    @Override
    public List<ContactRelationship> searchContacts(String keyword) {
        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();

        LambdaQueryWrapper<ContactRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactRelationship::getOwnerId, currentUserId);
        wrapper.eq(ContactRelationship::getOwnerType, currentUserType);
        wrapper.eq(ContactRelationship::getStatus, true);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(ContactRelationship::getContactName, keyword)
                .or()
                .like(ContactRelationship::getContactPhone, keyword)
                .or()
                .like(ContactRelationship::getNotes, keyword)
            );
        }
        
        wrapper.orderByDesc(ContactRelationship::getIsPinned);
        wrapper.orderByDesc(ContactRelationship::getLastContactTime);
        
        return contactRelationshipDao.selectList(wrapper);
    }

    @Override
    public com.github.pagehelper.PageInfo<ContactRelationship> searchContactsWithPage(String keyword, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // 限制最大页面大小

        // 使用 PageHelper 进行分页
        PageHelper.startPage(page, size);

        Integer currentUserId = getCurrentUserId();
        String currentUserType = getCurrentUserType();

        LambdaQueryWrapper<ContactRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactRelationship::getOwnerId, currentUserId);
        wrapper.eq(ContactRelationship::getOwnerType, currentUserType);
        wrapper.eq(ContactRelationship::getStatus, true);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(ContactRelationship::getContactName, keyword)
                .or()
                .like(ContactRelationship::getContactPhone, keyword)
                .or()
                .like(ContactRelationship::getNotes, keyword)
            );
        }
        
        wrapper.orderByDesc(ContactRelationship::getIsPinned);
        wrapper.orderByDesc(ContactRelationship::getLastContactTime);
        
        List<ContactRelationship> list = contactRelationshipDao.selectList(wrapper);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastContactTime(Integer ownerId, String ownerType, Integer contactId, String contactType) {
        ContactRelationship contact = contactRelationshipDao.selectByOwnerAndContact(
            ownerId, ownerType, contactId, contactType);
        
        if (contact != null) {
            contact.setLastContactTime(new Date());
            contact.setUpdateTime(new Date());
            contactRelationshipDao.updateById(contact);
        }
    }

    // 私有辅助方法

    private Integer getCurrentUserId() {
        // 从Spring Security上下文获取当前用户ID
        return SecurityUtil.getLoginUserVo().getUser().getId();
    }

    private String getCurrentUserType() {
        // 商户端登录默认为商户类型
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        if(merId != null && merId > 0){
            return ContactRelationship.OWNER_TYPE_MERCHANT;
        }
        if(merId!= null&&merId==0){
            return ContactRelationship.OWNER_TYPE_PLATFORM;
        }else {
            return ContactRelationship.CONTACT_TYPE_USER;
        }
    }

    private boolean isUserOnline(Integer contactId) {
        Integer userId = frontTokenComponent.getUserId();
        if (userId == null) {
            return false;
        }
        if(userId.equals(contactId)){
            return true;
        }
        return false;
    }

    private boolean isMerchantOnline(Integer contactId) {
        CustomerServiceStaff customerServiceStaff = customerServiceStaffService.getByEmployeeId(contactId);
        return customerServiceStaff != null && customerServiceStaff.getOnlineStatus().equals(CustomerServiceStaff.ONLINE_STATUS_ONLINE);

    }

    private boolean isPlatformAdminOnline(Integer adminId) {
        CustomerServiceStaff customerServiceStaff = customerServiceStaffService.getByEmployeeId(adminId);
        return customerServiceStaff != null && customerServiceStaff.getOnlineStatus().equals(CustomerServiceStaff.ONLINE_STATUS_ONLINE);
    }

    // ==================== 平台端专用方法 ====================

    @Override
    public CommonPage<ContactRelationship> getPlatformContactList(String contactType, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        // 获取平台管理员ID
        Integer platformUserId = SecurityUtil.getLoginUserVo().getUser().getId();
        
        LambdaQueryWrapper<ContactRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactRelationship::getOwnerId, platformUserId);
        wrapper.eq(ContactRelationship::getOwnerType, ContactRelationship.OWNER_TYPE_PLATFORM);
        wrapper.eq(ContactRelationship::getStatus, true);
        wrapper.eq(StringUtils.hasText(contactType), ContactRelationship::getContactType, contactType);
        
        wrapper.orderByDesc(ContactRelationship::getIsPinned);
        wrapper.orderByDesc(ContactRelationship::getLastContactTime);
        
        List<ContactRelationship> list = contactRelationshipDao.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPlatformContact(ContactRelationship contact) {
        Integer platformUserId = SecurityUtil.getLoginUserVo().getUser().getId();
        
        contact.setOwnerId(platformUserId);
        contact.setOwnerType(ContactRelationship.OWNER_TYPE_PLATFORM);
        contact.setCreateTime(new Date());
        contact.setUpdateTime(new Date());
        contact.setStatus(true);
        contact.setIsPinned(false);
        
        contactRelationshipDao.insert(contact);
        log.info("平台添加联系人成功，联系人ID: {}", contact.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlatformContact(ContactRelationship contact) {
        ContactRelationship existingContact = contactRelationshipDao.selectById(contact.getId());
        if (existingContact == null) {
            throw new CrmebException("联系人不存在");
        }

        // 验证是否是平台的联系人
        Integer platformUserId = SecurityUtil.getLoginUserVo().getUser().getId();
        if (!existingContact.getOwnerId().equals(platformUserId) || 
            !ContactRelationship.OWNER_TYPE_PLATFORM.equals(existingContact.getOwnerType())) {
            throw new CrmebException("无权限修改该联系人");
        }

        contact.setUpdateTime(new Date());
        contactRelationshipDao.updateById(contact);
        log.info("更新平台联系人信息，联系人ID: {}", contact.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlatformContact(Long contactId) {
        ContactRelationship contact = contactRelationshipDao.selectById(contactId);
        if (contact == null) {
            throw new CrmebException("联系人不存在");
        }

        // 验证是否是平台的联系人
        Integer platformUserId = SecurityUtil.getLoginUserVo().getUser().getId();
        if (!contact.getOwnerId().equals(platformUserId) || 
            !ContactRelationship.OWNER_TYPE_PLATFORM.equals(contact.getOwnerType())) {
            throw new CrmebException("无权限删除该联系人");
        }

        contactRelationshipDao.deleteById(contactId);
        log.info("删除平台联系人成功，联系人ID: {}", contactId);
    }

    @Override
    public List<ContactRelationship> searchPlatformAvailableContacts(String keyword, String contactType) {
        // 根据联系人类型搜索可添加的联系人
        // 这里需要根据具体业务逻辑实现
        return new ArrayList<>(); // 临时返回空列表
    }

    @Override
    public List<ContactRelationship> getChatableMerchants() {
        // 获取可聊天的商户列表
        Integer platformUserId = SecurityUtil.getLoginUserVo().getUser().getId();
        
        LambdaQueryWrapper<ContactRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactRelationship::getOwnerId, platformUserId);
        wrapper.eq(ContactRelationship::getOwnerType, ContactRelationship.OWNER_TYPE_PLATFORM);
        wrapper.eq(ContactRelationship::getContactType, ContactRelationship.CONTACT_TYPE_MERCHANT);
        wrapper.eq(ContactRelationship::getStatus, true);
        wrapper.orderByDesc(ContactRelationship::getLastContactTime);
        
        return contactRelationshipDao.selectList(wrapper);
    }

    @Override
    public List<ContactRelationship> getChatableUsers() {
        // 获取可聊天的用户列表
        Integer platformUserId = SecurityUtil.getLoginUserVo().getUser().getId();
        
        LambdaQueryWrapper<ContactRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactRelationship::getOwnerId, platformUserId);
        wrapper.eq(ContactRelationship::getOwnerType, ContactRelationship.OWNER_TYPE_PLATFORM);
        wrapper.eq(ContactRelationship::getContactType, ContactRelationship.CONTACT_TYPE_USER);
        wrapper.eq(ContactRelationship::getStatus, true);
        wrapper.orderByDesc(ContactRelationship::getLastContactTime);
        
        return contactRelationshipDao.selectList(wrapper);
    }

    @Override
    public List<ContactRelationship> getByMerId(Long merId) {
        LambdaQueryWrapper<ContactRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactRelationship::getOwnerId, merId);
        wrapper.eq(ContactRelationship::getOwnerType, ContactRelationship.OWNER_TYPE_MERCHANT);
        wrapper.eq(ContactRelationship::getStatus, true);
        wrapper.orderByDesc(ContactRelationship::getLastContactTime);
        return contactRelationshipDao.selectList(wrapper);
    }

    @Override
    public List<ContactRelationship> getByCurrentUserId(Long currentUserId) {
        LambdaQueryWrapper<ContactRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactRelationship::getOwnerId, currentUserId);
        wrapper.eq(ContactRelationship::getStatus, 1);
        wrapper.orderByDesc(ContactRelationship::getLastContactTime);
        return contactRelationshipDao.selectList(wrapper);
    }

    @Override
    public List<Map<String, Object>> searchAvailableUsers(String userType, String keyword) {
        List<Map<String, Object>> users = new ArrayList<>();
        
        try {
            switch (userType) {
                case "USER":
                    // 从eb_user表搜索用户
                    users = searchUsersFromUserTable(keyword);
                    break;
                case "MERCHANT":
                    // 从eb_merchant表搜索商户
                    users = searchUsersFromMerchantTable(keyword);
                    break;
                case "PLATFORM":
                    // 从eb_system_admin表搜索平台管理员
                    users = searchUsersFromAdminTable(keyword);
                    break;
                default:
                    throw new CrmebException("不支持的用户类型: " + userType);
            }
        } catch (Exception e) {
            log.error("搜索可添加用户失败", e);
            throw new CrmebException("搜索用户失败: " + e.getMessage());
        }
        
        return users;
    }

    /**
     * 从用户表搜索
     */
    private List<Map<String, Object>> searchUsersFromUserTable(String keyword) {
        List<Map<String, Object>> users = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getStatus, true); // 只查询状态正常的用户
            
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(User::getNickname, keyword)
                                 .or().like(User::getPhone, keyword)
                                 .or().like(User::getRealName, keyword));
            }
            
            wrapper.orderByDesc(User::getCreateTime);
            wrapper.last("LIMIT 50"); // 限制返回数量
            
            List<User> userList = userService.list(wrapper);
            
            for (User user : userList) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname());
                userMap.put("avatar", user.getAvatar());
                userMap.put("phone", user.getPhone());
                userMap.put("type", "USER");
                users.add(userMap);
            }
            
        } catch (Exception e) {
            log.error("从用户表搜索失败", e);
        }
        
        return users;
    }

    /**
     * 从商户表搜索
     */
    private List<Map<String, Object>> searchUsersFromMerchantTable(String keyword) {
        List<Map<String, Object>> merchants = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Merchant::getIsDel, false); // 只查询未删除的商户
            wrapper.eq(Merchant::getIsSwitch, true); // 只查询启用的商户
            
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(Merchant::getName, keyword)
                                 .or().like(Merchant::getPhone, keyword)
                                 .or().like(Merchant::getRealName, keyword)
                                 .or().like(Merchant::getName, keyword));
            }
            
            wrapper.orderByDesc(Merchant::getCreateTime);
            wrapper.last("LIMIT 50"); // 限制返回数量
            
            List<Merchant> merchantList = merchantService.list(wrapper);
            
            for (Merchant merchant : merchantList) {
                Map<String, Object> merchantMap = new HashMap<>();
                merchantMap.put("id", merchant.getId());
                merchantMap.put("name", StringUtils.hasText(merchant.getRealName()) ? merchant.getRealName() : merchant.getName());
                merchantMap.put("avatar", merchant.getAvatar());
                merchantMap.put("phone", merchant.getPhone());
                merchantMap.put("type", "MERCHANT");
                merchantMap.put("companyName", merchant.getName());
                merchants.add(merchantMap);
            }
            
        } catch (Exception e) {
            log.error("从商户表搜索失败", e);
        }
        
        return merchants;
    }

    /**
     * 从管理员表搜索
     */
    private List<Map<String, Object>> searchUsersFromAdminTable(String keyword) {
        List<Map<String, Object>> admins = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<SystemAdmin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemAdmin::getStatus, true); // 只查询启用的管理员
            
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(SystemAdmin::getRealName, keyword)
                                 .or().like(SystemAdmin::getAccount, keyword)
                                 .or().like(SystemAdmin::getPhone, keyword));
            }
            
            wrapper.orderByDesc(SystemAdmin::getCreateTime);
            wrapper.last("LIMIT 50"); // 限制返回数量
            
            List<SystemAdmin> adminList = systemAdminService.list(wrapper);
            
            for (SystemAdmin admin : adminList) {
                Map<String, Object> adminMap = new HashMap<>();
                adminMap.put("id", admin.getId());
                adminMap.put("name", StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getAccount());
                adminMap.put("avatar", admin.getHeaderImage());
                adminMap.put("phone", admin.getPhone());
                adminMap.put("type", "PLATFORM");
                adminMap.put("account", admin.getAccount());
                admins.add(adminMap);
            }
            
        } catch (Exception e) {
            log.error("从管理员表搜索失败", e);
        }
        
        return admins;
    }
}
