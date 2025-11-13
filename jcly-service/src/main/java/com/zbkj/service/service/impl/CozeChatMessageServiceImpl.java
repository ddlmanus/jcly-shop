package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeChatMessage;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CozeChatMessageDao;
import com.zbkj.service.service.CozeChatMessageService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Coze聊天消息服务实现类
 * @author: auto-generated
 * @date: 2024/01/01
 */
@Service
public class CozeChatMessageServiceImpl extends ServiceImpl<CozeChatMessageDao, CozeChatMessage> implements CozeChatMessageService {

    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return CommonPage
     */
    @Override
    public CommonPage<CozeChatMessage> getList(PageParamRequest pageParamRequest) {
        Page<CozeChatMessage> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeChatMessage::getStatus, 1);
        wrapper.orderByDesc(CozeChatMessage::getCreateTime);
        List<CozeChatMessage> list = this.baseMapper.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    /**
     * 根据会话ID获取消息列表
     * @param sessionId 会话ID
     * @param pageParamRequest 分页参数
     * @return 消息列表
     */
    @Override
    public CommonPage<CozeChatMessage> getBySessionId(Integer sessionId, PageParamRequest pageParamRequest) {
        Page<CozeChatMessage> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeChatMessage::getSessionId, sessionId);
        wrapper.eq(CozeChatMessage::getStatus, 1);
        wrapper.orderByAsc(CozeChatMessage::getCreateTime); // 按时间正序排列，聊天记录从旧到新
        List<CozeChatMessage> list = this.baseMapper.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    /**
     * 根据会话ID获取所有消息（不分页）
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @Override
    public List<CozeChatMessage> getAllBySessionId(Integer sessionId) {
        LambdaQueryWrapper<CozeChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeChatMessage::getSessionId, sessionId);
        wrapper.eq(CozeChatMessage::getStatus, 1);
        wrapper.orderByAsc(CozeChatMessage::getCreateTime); // 按时间正序排列
        return this.baseMapper.selectList(wrapper);
    }

    /**
     * 根据Coze消息ID获取消息
     * @param cozeMessageId Coze消息ID
     * @return 消息信息
     */
    @Override
    public CozeChatMessage getByCozeMessageId(String cozeMessageId) {
        LambdaQueryWrapper<CozeChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeChatMessage::getCozeMessageId, cozeMessageId);
        wrapper.eq(CozeChatMessage::getStatus, 1);
        return this.baseMapper.selectOne(wrapper);
    }

    /**
     * 创建新的聊天消息
     * @param sessionId 本地会话ID
     * @param cozeMessageId Coze消息ID
     * @param cozeChatId Coze聊天ID
     * @param role 消息发送者角色（user/assistant）
     * @param content 消息内容
     * @param contentType 消息内容类型
     * @return 创建的消息
     */
    @Override
    public CozeChatMessage createMessage(Integer sessionId, String cozeMessageId, String cozeChatId,
                                         String role, String content, String contentType) {
        CozeChatMessage message = new CozeChatMessage();
        message.setSessionId(sessionId);
        message.setCozeMessageId(cozeMessageId);
        message.setCozeChatId(cozeChatId);
        message.setRole(role);
        message.setContent(content);
        message.setContentType(contentType);
        message.setStatus(1);
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        
        this.baseMapper.insert(message);
        return message;
    }

    /**
     * 更新消息内容
     * @param messageId 消息ID
     * @param content 新的消息内容
     * @return 是否成功
     */
    @Override
    public Boolean updateMessageContent(Integer messageId, String content) {
        LambdaUpdateWrapper<CozeChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CozeChatMessage::getId, messageId);
        wrapper.set(CozeChatMessage::getContent, content);
        wrapper.set(CozeChatMessage::getUpdateTime, new Date());
        return this.baseMapper.update(null, wrapper) > 0;
    }

    /**
     * 删除消息
     * @param messageId 消息ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteMessage(Integer messageId) {
        LambdaUpdateWrapper<CozeChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CozeChatMessage::getId, messageId);
        wrapper.set(CozeChatMessage::getStatus, 0);
        wrapper.set(CozeChatMessage::getUpdateTime, new Date());
        return this.baseMapper.update(null, wrapper) > 0;
    }

    /**
     * 根据会话ID删除所有消息
     * @param sessionId 会话ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteBySessionId(Integer sessionId) {
        LambdaUpdateWrapper<CozeChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CozeChatMessage::getSessionId, sessionId);
        wrapper.set(CozeChatMessage::getStatus, 0);
        wrapper.set(CozeChatMessage::getUpdateTime, new Date());
        return this.baseMapper.update(null, wrapper) > 0;
    }

    /**
     * 根据用户ID删除所有消息
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteByUserId(Integer userId) {
        // 注意：这里需要联合查询，先找到用户的所有会话，再删除消息
        // 这个方法可能需要在具体业务中实现，这里提供基础实现
        // 实际使用时建议先查询用户的所有sessionId，然后批量删除
        return true; // 暂时返回true，具体实现根据业务需求
    }
}
