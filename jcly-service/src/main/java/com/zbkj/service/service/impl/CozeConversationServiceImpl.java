package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeConversation;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CozeConversationDao;
import com.zbkj.service.service.CozeConversationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 扣子会话表 服务实现类
 * </p>
 *
 * @author dudl
 * @since 2024-01-01
 */
@Service
public class CozeConversationServiceImpl extends ServiceImpl<CozeConversationDao, CozeConversation> implements CozeConversationService {

    @Override
    public PageInfo<CozeConversation> getConversationsByMerchant(PageParamRequest paramRequest, Integer merId, String botId) {
        Page<CozeConversation> page = PageHelper.startPage(paramRequest.getPage(), paramRequest.getLimit());
        LambdaQueryWrapper<CozeConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeConversation::getMerId, merId);
        if (botId != null && !botId.trim().isEmpty()) {
            wrapper.eq(CozeConversation::getCozeBotId, botId);
        }
        wrapper.orderByDesc(CozeConversation::getCreateTime);
        List<CozeConversation> cozeConversations = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(page, cozeConversations);
    }

    @Override
    public CozeConversation getByConversationIdAndMerchant(String conversationId, Integer merId) {
        LambdaQueryWrapper<CozeConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeConversation::getId, conversationId)
               .eq(CozeConversation::getMerId, merId);
        return getOne(wrapper);
    }

    @Override
    public CozeConversation getByCozeConversationIdAndMerchant(String cozeConversationId, Integer merId) {
        LambdaQueryWrapper<CozeConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeConversation::getCozeConversationId, cozeConversationId)
               .eq(CozeConversation::getMerId, merId);
        return getOne(wrapper);
    }

    @Override
    public boolean saveOrUpdateConversation(CozeConversation conversation) {
        if (conversation.getId() != null) {
            // 更新现有会话
            return updateById(conversation);
        } else {
            // 创建新会话
            return save(conversation);
        }
    }

    @Override
    public boolean deleteConversationByMerchant(String conversationId, Integer merId) {
        LambdaQueryWrapper<CozeConversation> wrapper =new LambdaQueryWrapper<>();
        wrapper.eq(CozeConversation::getId, conversationId)
               .eq(CozeConversation::getMerId, merId);
        return remove(wrapper);
    }
}
