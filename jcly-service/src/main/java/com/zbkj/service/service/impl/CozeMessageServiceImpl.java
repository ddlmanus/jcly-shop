package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeMessage;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CozeMessageDao;
import com.zbkj.service.service.CozeMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 扣子消息表 服务实现类
 * </p>
 *
 * @author dudl
 * @since 2024-01-01
 */
@Service
public class CozeMessageServiceImpl extends ServiceImpl<CozeMessageDao, CozeMessage> implements CozeMessageService {

    @Override
    public PageInfo<CozeMessage> getMessagesByMerchant(PageParamRequest paramRequest, Integer merId, String conversationId, String messageType) {
        Page<CozeMessage> page = PageHelper.startPage(paramRequest.getPage(), paramRequest.getLimit());
        LambdaQueryWrapper<CozeMessage> wrapper = new LambdaQueryWrapper();
        wrapper.eq(CozeMessage::getMerId, merId);
        if (conversationId != null && !conversationId.trim().isEmpty()) {
            wrapper.eq(CozeMessage::getCozeConversationId, conversationId);
        }
        if (messageType != null && !messageType.trim().isEmpty()) {
            wrapper.eq(CozeMessage::getMessageType, messageType);
        }
        wrapper.orderByDesc(CozeMessage::getCreateTime);
        List<CozeMessage> cozeMessages = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(page,cozeMessages);
    }

    @Override
    public CozeMessage getByMessageIdAndMerchant(String messageId, Integer merId) {
        LambdaQueryWrapper<CozeMessage> wrapper = new LambdaQueryWrapper();
        wrapper.eq(CozeMessage::getId, messageId)
               .eq(CozeMessage::getMerId, merId);
        return getOne(wrapper);
    }

    @Override
    public CozeMessage getByCozeMessageIdAndMerchant(String cozeMessageId, Integer merId) {
        LambdaQueryWrapper<CozeMessage> wrapper = new LambdaQueryWrapper();
        wrapper.eq(CozeMessage::getCozeMessageId, cozeMessageId)
               .eq(CozeMessage::getMerId, merId);
        return getOne(wrapper);
    }

    @Override
    public boolean saveOrUpdateMessage(CozeMessage message) {
        if (message.getId() != null) {
            // 更新现有消息
            return updateById(message);
        } else {
            // 创建新消息
            return save(message);
        }
    }

    @Override
    public boolean deleteMessageByMerchant(String messageId, Integer merId) {
        LambdaQueryWrapper<CozeMessage> wrapper = new LambdaQueryWrapper();
        wrapper.eq(CozeMessage::getId, messageId)
               .eq(CozeMessage::getMerId, merId);
        return remove(wrapper);
    }

    @Override
    public PageInfo<CozeMessage> getMessagesByConversationAndMerchant(PageParamRequest paramRequest, String conversationId, Integer merId) {
        Page<CozeMessage> objects = PageHelper.startPage(paramRequest.getPage(), paramRequest.getLimit());
        LambdaQueryWrapper<CozeMessage> wrapper = new LambdaQueryWrapper();
        wrapper.eq(CozeMessage::getCozeConversationId, conversationId)
               .eq(CozeMessage::getMerId, merId)
               .orderByAsc(CozeMessage::getCreateTime);
        List<CozeMessage> cozeMessages = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(objects, cozeMessages);
    }
}
