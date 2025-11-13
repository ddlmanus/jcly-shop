package com.zbkj.service.service;

import com.zbkj.common.request.ContactManageRequest;
import com.zbkj.common.model.service.ContactRelationship;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.page.CommonPage;
import java.util.List;
import java.util.Map;

/**
 * 联系人关系服务接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface ContactRelationshipService {

    /**
     * 添加联系人
     */
    ContactRelationship addContact(ContactManageRequest request);

    /**
     * 获取联系人列表
     */
    List<ContactRelationship> getContactList(String contactType, String groupName);

    /**
     * 获取联系人列表（分页）
     */
    com.github.pagehelper.PageInfo<ContactRelationship> getContactListWithPage(String contactType, String groupName, Integer page, Integer size);

    /**
     * 删除联系人
     */
    void deleteContact(Long contactId);

    /**
     * 获取联系人在线状态
     */
    Map<String, Object> getContactOnlineStatus(Integer contactId, String contactType);

    /**
     * 更新联系人信息
     */
    void updateContact(Long contactId, ContactManageRequest request);

    /**
     * 置顶/取消置顶联系人
     */
    void pinContact(Long contactId, Boolean isPinned);

    /**
     * 获取联系人分组列表
     */
    List<String> getContactGroups();

    /**
     * 搜索联系人
     */
    List<ContactRelationship> searchContacts(String keyword);

    /**
     * 搜索联系人（分页）
     */
    com.github.pagehelper.PageInfo<ContactRelationship> searchContactsWithPage(String keyword, Integer page, Integer size);

    /**
     * 搜索可添加的用户
     */
    List<Map<String, Object>> searchAvailableUsers(String userType, String keyword);

    /**
     * 更新最后联系时间
     */
    void updateLastContactTime(Integer ownerId, String ownerType, Integer contactId, String contactType);

    // ==================== 平台端专用方法 ====================

    /**
     * 获取平台联系人列表
     */
    CommonPage<ContactRelationship> getPlatformContactList(String contactType, PageParamRequest pageParamRequest);

    /**
     * 添加平台联系人
     */
    void addPlatformContact(ContactRelationship contact);

    /**
     * 更新联系人信息（平台端）
     */
    void updatePlatformContact(ContactRelationship contact);

    /**
     * 删除联系人（平台端）
     */
    void deletePlatformContact(Long contactId);

    /**
     * 搜索平台可添加的联系人
     */
    List<ContactRelationship> searchPlatformAvailableContacts(String keyword, String contactType);

    /**
     * 获取可聊天的商户列表（平台端）
     */
    List<ContactRelationship> getChatableMerchants();

    /**
     * 获取可聊天的用户列表（平台端）
     */
    List<ContactRelationship> getChatableUsers();

    List<ContactRelationship> getByMerId(Long merId);

    List<ContactRelationship> getByCurrentUserId(Long currentUserId);
}
