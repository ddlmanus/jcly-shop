package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeSpaceMember;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.coze.CozeGetSpaceMembersResponse;
import com.zbkj.service.dao.CozeSpaceMemberDao;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.CozeSpaceMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * CozeSpaceMemberServiceImpl 实现类
 */
@Slf4j
@Service
public class CozeSpaceMemberServiceImpl extends ServiceImpl<CozeSpaceMemberDao, CozeSpaceMember> implements CozeSpaceMemberService {

    @Autowired
    private CozeService cozeService;

    @Override
    public PageInfo<CozeSpaceMember> getList(Integer merchantId, String spaceId, PageParamRequest pageParamRequest) {
        Page<CozeSpaceMember> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        LambdaQueryWrapper<CozeSpaceMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeSpaceMember::getMerchantId, merchantId);
        if (spaceId != null && !spaceId.trim().isEmpty()) {
            wrapper.eq(CozeSpaceMember::getSpaceId, spaceId);
        }
        wrapper.eq(CozeSpaceMember::getStatus, 1);
        wrapper.orderByDesc(CozeSpaceMember::getCreateTime);
        
        List<CozeSpaceMember> list = list(wrapper);
        return CommonPage.copyPageInfo(page, list);
    }

    @Override
    public List<CozeSpaceMember> getBySpaceId(Integer merchantId, String spaceId) {
        LambdaQueryWrapper<CozeSpaceMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeSpaceMember::getMerchantId, merchantId);
        wrapper.eq(CozeSpaceMember::getSpaceId, spaceId);
        wrapper.eq(CozeSpaceMember::getStatus, 1);
        wrapper.orderByDesc(CozeSpaceMember::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncSpaceMembersFromCoze(Integer merchantId, String spaceId) {
        try {
            log.info("开始同步空间成员列表，商户ID：{}，空间ID：{}", merchantId, spaceId);
            
            // 获取Coze平台的空间成员列表（分页获取所有数据）
            int pageNum = 1;
            int pageSize = 50;
            boolean hasMore = true;
            
            while (hasMore) {
                CozeGetSpaceMembersResponse response = cozeService.getSpaceMembersTyped(spaceId, pageNum, pageSize);
                
                if (response != null && response.getData() != null && !CollectionUtils.isEmpty(response.getData().getItems())) {
                    // 保存成员数据到本地数据库
                    saveSpaceMembersFromCoze(merchantId, spaceId, response.getData().getItems());
                    
                    // 检查是否还有更多数据
                    hasMore = response.getData().getItems().size() >= pageSize;
                    pageNum++;
                } else {
                    hasMore = false;
                }
            }
            
            log.info("空间成员同步完成，商户ID：{}，空间ID：{}", merchantId, spaceId);
            return true;
        } catch (Exception e) {
            log.error("同步空间成员失败，商户ID：{}，空间ID：{}", merchantId, spaceId, e);
            throw new RuntimeException("同步空间成员失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSpaceMembersFromCoze(Integer merchantId, String spaceId, List<CozeGetSpaceMembersResponse.OpenSpaceMember> members) {
        try {
            Date now = new Date();
            
            for (CozeGetSpaceMembersResponse.OpenSpaceMember member : members) {
                // 检查成员是否已存在
                LambdaQueryWrapper<CozeSpaceMember> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(CozeSpaceMember::getMerchantId, merchantId);
                wrapper.eq(CozeSpaceMember::getSpaceId, spaceId);
                wrapper.eq(CozeSpaceMember::getUserId, member.getUserId());
                
                CozeSpaceMember existingMember = getOne(wrapper);
                
                if (existingMember != null) {
                    // 更新现有成员信息
                    existingMember.setRoleType(member.getRoleType());
                    existingMember.setUserNickname(member.getUserNickname());
                    existingMember.setUserUniqueName(member.getUserUniqueName());
                    existingMember.setAvatarUrl(member.getAvatarUrl());
                    existingMember.setUpdateTime(now);
                    updateById(existingMember);
                } else {
                    // 创建新成员
                    CozeSpaceMember newMember = new CozeSpaceMember();
                    newMember.setMerchantId(merchantId);
                    newMember.setSpaceId(spaceId);
                    newMember.setUserId(member.getUserId());
                    newMember.setRoleType(member.getRoleType());
                    newMember.setUserNickname(member.getUserNickname());
                    newMember.setUserUniqueName(member.getUserUniqueName());
                    newMember.setAvatarUrl(member.getAvatarUrl());
                    newMember.setStatus(1);
                    newMember.setCreateTime(now);
                    newMember.setUpdateTime(now);
                    save(newMember);
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("保存空间成员失败", e);
            throw new RuntimeException("保存空间成员失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean deleteById(Integer id, Integer merchantId) {
        LambdaQueryWrapper<CozeSpaceMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeSpaceMember::getId, id);
        wrapper.eq(CozeSpaceMember::getMerchantId, merchantId);
        
        CozeSpaceMember member = getOne(wrapper);
        if (member == null) {
            throw new RuntimeException("空间成员不存在或无权限操作");
        }
        
        return removeById(id);
    }
}
