package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeSpace;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.coze.CozeGetSpaceListRequest;
import com.zbkj.common.response.coze.CozeGetSpaceListResponse;
import com.zbkj.service.dao.CozeSpaceDao;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.CozeSpaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Coze空间服务实现类
 * @author: auto-generated
 * @date: 2024/01/01
 */
@Slf4j
@Service
public class CozeSpaceServiceImpl extends ServiceImpl<CozeSpaceDao, CozeSpace> implements CozeSpaceService {

    @Autowired
    private CozeService cozeService;

    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return CommonPage
     */
    @Override
    public CommonPage<CozeSpace> getList(PageParamRequest pageParamRequest) {
        Page<CozeSpace> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        LambdaQueryWrapper<CozeSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeSpace::getStatus, 1);
        wrapper.eq(CozeSpace::getMerId, merchantId);
        wrapper.orderByDesc(CozeSpace::getUpdateTime);
        List<CozeSpace> list = this.baseMapper.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    /**
     * 根据商户ID获取空间列表
     * @param merId 商户ID
     * @return 空间列表
     */
    @Override
    public List<CozeSpace> getByMerId(Integer merId) {
        LambdaQueryWrapper<CozeSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeSpace::getMerId, merId);
        wrapper.eq(CozeSpace::getStatus, 1);
        wrapper.orderByDesc(CozeSpace::getUpdateTime);
        return this.baseMapper.selectList(wrapper);
    }

    /**
     * 根据空间ID获取空间详情
     * @param spaceId 空间ID
     * @return 空间详情
     */
    @Override
    public CozeSpace getBySpaceId(String spaceId, Integer merId) {
        LambdaQueryWrapper<CozeSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeSpace::getSpaceId, spaceId);
        wrapper.eq(CozeSpace::getMerId, merId);
        wrapper.eq(CozeSpace::getStatus, 1);
        return this.baseMapper.selectOne(wrapper);
    }

    /**
     * 同步Coze空间到本地数据库
     * @param merId 商户ID
     * @return 是否成功
     */
    @Override
    public Boolean syncCozeSpaces(Integer merId) {
        try {
            // 创建获取空间列表的请求
            CozeGetSpaceListRequest request = new CozeGetSpaceListRequest();
            request.setPageNum(1);
            request.setPageSize(50); // 获取最多50个空间
            
            // 调用Coze API获取空间列表
            CozeGetSpaceListResponse response = cozeService.getSpaceList(request);
            
            if (response != null && response.getData() != null && response.getData().getWorkspaces() != null) {
                // 遍历空间列表，保存或更新到本地数据库
                for (CozeGetSpaceListResponse.OpenSpace workspace : response.getData().getWorkspaces()) {
                    // 检查是否已存在
                    CozeSpace existingSpace = getBySpaceId(workspace.getId(),merId);
                    
                    if (existingSpace != null) {
                        // 更新现有空间
                        existingSpace.setName(workspace.getName());
                        existingSpace.setIconUrl(workspace.getIconUrl());
                        existingSpace.setOwnerUid(workspace.getOwnerUid());
                        existingSpace.setRoleType(workspace.getRoleType());
                        existingSpace.setDescription(workspace.getDescription());
                        existingSpace.setEnterpriseId(workspace.getEnterpriseId());
                        existingSpace.setJoinedStatus(workspace.getJoinedStatus());
                        existingSpace.setWorkspaceType(workspace.getWorkspaceType());
                        existingSpace.setUpdateTime(new Date());
                        
                        this.baseMapper.updateById(existingSpace);
                    } else {
                        // 创建新空间
                        CozeSpace newSpace = new CozeSpace();
                        newSpace.setMerId(merId);
                        newSpace.setSpaceId(workspace.getId());
                        newSpace.setName(workspace.getName());
                        newSpace.setIconUrl(workspace.getIconUrl());
                        newSpace.setOwnerUid(workspace.getOwnerUid());
                        newSpace.setRoleType(workspace.getRoleType());
                        newSpace.setDescription(workspace.getDescription());
                        newSpace.setEnterpriseId(workspace.getEnterpriseId());
                        newSpace.setJoinedStatus(workspace.getJoinedStatus());
                        newSpace.setWorkspaceType(workspace.getWorkspaceType());
                        newSpace.setStatus(1);
                        newSpace.setCreateTime(new Date());
                        newSpace.setUpdateTime(new Date());
                        
                        this.baseMapper.insert(newSpace);
                    }
                }
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("同步Coze空间失败", e);
            return false;
        }
    }

    /**
     * 删除本地空间记录
     * @param id 主键ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteLocalSpace(Integer id) {
        return this.baseMapper.deleteById(id) > 0;
    }
}
