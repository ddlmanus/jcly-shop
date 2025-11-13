package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.service.ContactRelationship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 联系人关系Mapper接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface ContactRelationshipDao extends BaseMapper<ContactRelationship> {

    /**
     * 获取用户联系人列表
     */
    List<ContactRelationship> selectContactsByOwner(@Param("ownerId") Integer ownerId, 
                                                   @Param("ownerType") String ownerType,
                                                   @Param("contactType") String contactType,
                                                   @Param("groupName") String groupName);

    /**
     * 检查联系人关系是否存在
     */
    ContactRelationship selectByOwnerAndContact(@Param("ownerId") Integer ownerId,
                                               @Param("ownerType") String ownerType,
                                               @Param("contactId") Integer contactId,
                                               @Param("contactType") String contactType);

    /**
     * 获取联系人分组列表
     */
    List<String> selectGroupsByOwner(@Param("ownerId") Integer ownerId, @Param("ownerType") String ownerType);
}
