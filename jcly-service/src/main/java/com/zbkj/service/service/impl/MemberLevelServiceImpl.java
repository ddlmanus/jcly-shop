package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.member.MemberLevel;
import com.zbkj.common.request.MemberLevelRequest;
import com.zbkj.common.request.MemberLevelSearchRequest;
import com.zbkj.common.request.MemberLevelStatusRequest;
import com.zbkj.service.dao.MemberDao;
import com.zbkj.service.dao.MemberLevelDao;
import com.zbkj.service.service.MemberLevelService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 会员等级服务实现类
 */
@Service
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelDao, MemberLevel> implements MemberLevelService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Autowired
    private MemberDao memberDao;

    /**
     * 会员等级列表
     * @param request 请求参数
     * @return 会员等级列表
     */
    @Override
    public List<MemberLevel> getList(MemberLevelSearchRequest request) {
        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getIsDel, false);
        
        // 店铺ID
        if (request.getMerId() != null) {
            queryWrapper.eq(MemberLevel::getMerId, request.getMerId());
        }
        
        // 等级名称
        if (StringUtils.isNotBlank(request.getLevelName())) {
            queryWrapper.like(MemberLevel::getLevelName, request.getLevelName());
        }
        
        // 状态
        if (request.getStatus() != null) {
            queryWrapper.eq(MemberLevel::getStatus, request.getStatus());
        }
        
        // 排序
        queryWrapper.orderByAsc(MemberLevel::getMinIntegral);
        
        List<MemberLevel> levelList = memberLevelDao.selectList(queryWrapper);
        
        // 设置前端兼容字段
        for (MemberLevel level : levelList) {
            level.setLevel_name(level.getLevelName());
        }
        
        return levelList;
    }

    /**
     * 会员等级详情
     * @param id 等级ID
     * @return 会员等级详情
     */
    @Override
    public MemberLevel getDetail(Integer id) {
        return memberLevelDao.selectById(id);
    }

    /**
     * 新增会员等级
     * @param request 请求参数
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(MemberLevelRequest request) {
        // 验证等级名称是否重复
        checkLevelNameUnique(request.getMerId(), request.getLevelName(), null);
        
        // 验证积分是否重复
        checkIntegralUnique(request.getMerId(), request.getMinIntegral(), null);
        
        // 创建会员等级
        MemberLevel level = new MemberLevel();
        BeanUtils.copyProperties(request, level);
        level.setMemberCount(0);
        level.setIsDel(false);
        level.setCreateTime(new Date());
        level.setUpdateTime(new Date());
        
        return memberLevelDao.insert(level) > 0;
    }

    /**
     * 编辑会员等级
     * @param request 请求参数
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(MemberLevelRequest request) {
        // 验证等级是否存在
        MemberLevel level = memberLevelDao.selectById(request.getId());
        if (level == null) {
            throw new RuntimeException("会员等级不存在");
        }
        
        // 验证等级名称是否重复
        checkLevelNameUnique(request.getMerId(), request.getLevelName(), request.getId());
        
        // 验证积分是否重复
        checkIntegralUnique(request.getMerId(), request.getMinIntegral(), request.getId());
        
        // 更新会员等级
        BeanUtils.copyProperties(request, level);
        level.setUpdateTime(new Date());
        
        return memberLevelDao.updateById(level) > 0;
    }

    /**
     * 删除会员等级
     * @param id 等级ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        // 验证等级是否存在
        MemberLevel level = memberLevelDao.selectById(id);
        if (level == null) {
            throw new RuntimeException("会员等级不存在");
        }
        
        // 验证是否有会员使用该等级
        if (level.getMemberCount() > 0) {
            throw new RuntimeException("该等级下有会员，无法删除");
        }
        
        // 逻辑删除
        level.setIsDel(true);
        level.setUpdateTime(new Date());
        
        return memberLevelDao.updateById(level) > 0;
    }

    /**
     * 修改会员等级状态
     * @param request 请求参数
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(MemberLevelStatusRequest request) {
        // 验证等级是否存在
        MemberLevel level = memberLevelDao.selectById(request.getId());
        if (level == null) {
            throw new RuntimeException("会员等级不存在");
        }
        
        // 更新状态
        level.setStatus(request.getStatus());
        level.setUpdateTime(new Date());
        
        return memberLevelDao.updateById(level) > 0;
    }

    /**
     * 验证等级名称是否唯一
     * @param storeId 店铺ID
     * @param levelName 等级名称
     * @param id 等级ID（编辑时使用）
     */
    private void checkLevelNameUnique(Integer storeId, String levelName, Integer id) {
        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getMerId, storeId);
        queryWrapper.eq(MemberLevel::getLevelName, levelName);
        queryWrapper.eq(MemberLevel::getIsDel, false);
        
        if (id != null) {
            queryWrapper.ne(MemberLevel::getId, id);
        }
        
        if (memberLevelDao.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("等级名称已存在");
        }
    }

    /**
     * 验证积分是否唯一
     * @param storeId 店铺ID
     * @param minIntegral 所需积分
     * @param id 等级ID（编辑时使用）
     */
    private void checkIntegralUnique(Integer storeId, Integer minIntegral, Integer id) {
        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getMerId, storeId);
        queryWrapper.eq(MemberLevel::getMinIntegral, minIntegral);
        queryWrapper.eq(MemberLevel::getIsDel, false);
        
        if (id != null) {
            queryWrapper.ne(MemberLevel::getId, id);
        }
        
        if (memberLevelDao.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("该积分值已被其他等级使用");
        }
    }

    /**
     * 更新会员等级统计
     * @param storeId 店铺ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLevelStatistics(Integer storeId) {
        // 获取店铺所有等级
        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getMerId, storeId);
        queryWrapper.eq(MemberLevel::getIsDel, false);
        List<MemberLevel> levels = memberLevelDao.selectList(queryWrapper);
        
        for (MemberLevel level : levels) {
            // 统计该等级的会员数量
            int count = memberDao.countByStoreIdAndLevelId(storeId, level.getId());
            
            // 更新等级会员数量
            level.setMemberCount(count);
            level.setUpdateTime(new Date());
            memberLevelDao.updateById(level);
        }
        
        return true;
    }

    /**
     * 根据积分更新会员等级
     * @param member 会员信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberLevel(Member member) {
        // 获取会员当前积分对应的最高等级
        MemberLevel highestLevel = memberLevelDao.getHighestLevelByIntegral(member.getIntegral(), member.getMerId());
        if (highestLevel == null) {
            return false;
        }
        
        // 如果等级没变，不需要更新
        if (member.getLevelId() != null && member.getLevelId().equals(highestLevel.getId())) {
            return false;
        }
        
        // 更新会员等级
        Integer oldLevelId = member.getLevelId();
        member.setLevelId(highestLevel.getId());
        member.setLevelName(highestLevel.getLevelName());
        member.setUpdateTime(new Date());
        memberDao.updateById(member);
        
        // 更新等级会员数量统计
        if (oldLevelId != null) {
            MemberLevel oldLevel = memberLevelDao.selectById(oldLevelId);
            if (oldLevel != null) {
                oldLevel.setMemberCount(oldLevel.getMemberCount() - 1);
                oldLevel.setUpdateTime(new Date());
                memberLevelDao.updateById(oldLevel);
            }
        }
        
        highestLevel.setMemberCount(highestLevel.getMemberCount() + 1);
        highestLevel.setUpdateTime(new Date());
        memberLevelDao.updateById(highestLevel);
        
        return true;
    }
}