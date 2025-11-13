package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeChatSession;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CozeChatSessionDao;
import com.zbkj.service.service.CozeChatSessionService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Coze聊天会话服务实现类
 * @author: auto-generated
 * @date: 2024/01/01
 */
@Service
public class CozeChatSessionServiceImpl extends ServiceImpl<CozeChatSessionDao, CozeChatSession> implements CozeChatSessionService {



    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return CommonPage
     */
    @Override
    public CommonPage<CozeChatSession> getList(PageParamRequest pageParamRequest) {
        Page<CozeChatSession> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeChatSession::getStatus, 1);
        wrapper.orderByDesc(CozeChatSession::getUpdateTime);
        List<CozeChatSession> list = this.baseMapper.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    /**
     * 根据用户ID获取聊天会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    @Override
    public List<CozeChatSession> getByUserId(Integer userId) {
        LambdaQueryWrapper<CozeChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeChatSession::getUserId, userId);
        wrapper.eq(CozeChatSession::getStatus, 1);
        wrapper.orderByDesc(CozeChatSession::getUpdateTime);
        return this.baseMapper.selectList(wrapper);
    }

    /**
     * 根据用户ID和Coze会话ID获取会话
     * @param userId 用户ID
     * @param cozeSessionId Coze会话ID
     * @return 会话信息
     */
    @Override
    public CozeChatSession getByUserIdAndCozeSessionId(Integer userId, String cozeSessionId) {
        LambdaQueryWrapper<CozeChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeChatSession::getUserId, userId);
        wrapper.eq(CozeChatSession::getCozeSessionId, cozeSessionId);
        wrapper.eq(CozeChatSession::getStatus, 1);
        return this.baseMapper.selectOne(wrapper);
    }

    /**
     * 创建新的聊天会话
     * @param userId 用户ID
     * @param cozeBotId Coze智能体ID
     * @param cozeSessionId Coze会话ID
     * @param sessionName 会话名称
     * @return 创建的会话
     */
    @Override
    public CozeChatSession createSession(Integer userId, String cozeBotId, String cozeSessionId, String sessionName) {
        CozeChatSession session = new CozeChatSession();
        session.setUserId(userId);
        session.setCozeBotId(cozeBotId);
        session.setCozeSessionId(cozeSessionId);
        session.setSessionName(sessionName);
        session.setStatus(1);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        
        this.baseMapper.insert(session);
        return session;
    }

    /**
     * 更新会话名称
     * @param sessionId 会话ID
     * @param sessionName 新的会话名称
     * @return 是否成功
     */
    @Override
    public Boolean updateSessionName(Integer sessionId, String sessionName) {
        LambdaUpdateWrapper<CozeChatSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CozeChatSession::getId, sessionId);
        wrapper.set(CozeChatSession::getSessionName, sessionName);
        wrapper.set(CozeChatSession::getUpdateTime, new Date());
        return this.baseMapper.update(null, wrapper) > 0;
    }

    /**
     * 删除会话
     * @param sessionId 会话ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteSession(Integer sessionId) {
        LambdaUpdateWrapper<CozeChatSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CozeChatSession::getId, sessionId);
        wrapper.set(CozeChatSession::getStatus, 0);
        wrapper.set(CozeChatSession::getUpdateTime, new Date());
        return this.baseMapper.update(null, wrapper) > 0;
    }

    /**
     * 根据用户ID删除所有会话
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteByUserId(Integer userId) {
        LambdaUpdateWrapper<CozeChatSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CozeChatSession::getUserId, userId);
        wrapper.set(CozeChatSession::getStatus, 0);
        wrapper.set(CozeChatSession::getUpdateTime, new Date());
        return this.baseMapper.update(null, wrapper) > 0;
    }
}
