package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.merchant.MerchantMemberMessage;
import com.zbkj.common.request.MemberMessagePageRequest;
import com.zbkj.common.request.MemberSendMessageRequest;
import com.zbkj.common.response.MerchantMemberMessageResponse;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.dao.MerchantMemberMessageDao;
import com.zbkj.service.service.MemberService;
import com.zbkj.service.service.MerchantMemberMessageService;
import com.zbkj.service.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户会员消息服务实现类
 */
@Slf4j
@Service
public class MerchantMemberMessageServiceImpl extends ServiceImpl<MerchantMemberMessageDao, MerchantMemberMessage> implements MerchantMemberMessageService {

    @Resource
    private MerchantMemberMessageDao merchantMemberMessageDao;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MerchantService merchantService;

    /**
     * 发送消息给会员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendMessage(MemberSendMessageRequest request) {
        try {
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            Integer merId = systemAdmin.getMerId();
            
            // 获取商户信息
            Merchant merchant = merchantService.getById(merId);
            if (merchant == null) {
                throw new CrmebException("商户信息不存在");
            }

            List<MerchantMemberMessage> messageList = new ArrayList<>();
            Date now = new Date();

            // 为每个会员创建消息记录
            for (Integer memberId : request.getMemberIds()) {
                Member member = memberService.getById(memberId);
                if (member == null) {
                    log.warn("会员不存在，会员ID：{}", memberId);
                    continue;
                }

                MerchantMemberMessage message = new MerchantMemberMessage();
                message.setMerId(merId);
                message.setUid(member.getUid());
                message.setMerchantName(merchant.getName());
                message.setMerchantAvatar(merchant.getAvatar());
                message.setContent(request.getContent());
                message.setMessageType(request.getMessageType());
                message.setIsRead(false);
                message.setCreateTime(now);
                message.setUpdateTime(now);
                message.setIsDel(false);

                messageList.add(message);
            }

            if (!CollectionUtils.isEmpty(messageList)) {
                return this.saveBatch(messageList);
            }

            return false;
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new CrmebException("发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 获取消息列表
     */
    @Override
    public List<MerchantMemberMessageResponse> getMessageList(MemberMessagePageRequest request) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        Integer merId = systemAdmin.getMerId();

        PageHelper.startPage(request.getPage(), request.getLimit());
        
        List<MerchantMemberMessageResponse> messageList = merchantMemberMessageDao.selectMessageListWithMember(
                merId,
                request.getMemberId(),
                request.getMessageType(),
                request.getIsRead(),
                request.getKeywords()
        );

        // 设置消息类型名称
        if (!CollectionUtils.isEmpty(messageList)) {
            Map<Integer, String> messageTypeMap = getMessageTypeMap();
            messageList.forEach(message -> {
                message.setMessageTypeName(messageTypeMap.get(message.getMessageType()));
            });
        }

        return messageList;
    }

    /**
     * 标记消息为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Integer messageId) {
        LambdaUpdateWrapper<MerchantMemberMessage> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(MerchantMemberMessage::getId, messageId)
                .set(MerchantMemberMessage::getIsRead, true)
                .set(MerchantMemberMessage::getUpdateTime, new Date());

        return this.update(updateWrapper);
    }

    /**
     * 批量标记消息为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchMarkAsRead(List<Integer> messageIds) {
        if (CollectionUtils.isEmpty(messageIds)) {
            return false;
        }

        LambdaUpdateWrapper<MerchantMemberMessage> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(MerchantMemberMessage::getId, messageIds)
                .set(MerchantMemberMessage::getIsRead, true)
                .set(MerchantMemberMessage::getUpdateTime, new Date());

        return this.update(updateWrapper);
    }

    /**
     * 删除消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Integer messageId) {
        LambdaUpdateWrapper<MerchantMemberMessage> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(MerchantMemberMessage::getId, messageId)
                .set(MerchantMemberMessage::getIsDel, true)
                .set(MerchantMemberMessage::getUpdateTime, new Date());

        return this.update(updateWrapper);
    }

    /**
     * 获取会员未读消息数量
     */
    @Override
    public int getUnreadCount(Integer merId, Integer uid) {
        return merchantMemberMessageDao.countUnreadMessages(merId, uid);
    }

    /**
     * 根据ID获取消息详情
     */
    @Override
    public MerchantMemberMessageResponse getMessageDetail(Integer messageId) {
        LambdaQueryWrapper<MerchantMemberMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantMemberMessage::getId, messageId)
                .eq(MerchantMemberMessage::getIsDel, false);

        MerchantMemberMessage message = this.getOne(queryWrapper);
        if (message == null) {
            return null;
        }

        // 获取会员信息
        Member member = memberService.getByUidAndMerId(message.getUid(), message.getMerId());

        MerchantMemberMessageResponse response = new MerchantMemberMessageResponse();
        response.setId(message.getId());
        response.setMerId(message.getMerId());
        response.setUid(message.getUid());
        response.setMerchantName(message.getMerchantName());
        response.setMerchantAvatar(message.getMerchantAvatar());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setMessageTypeName(getMessageTypeMap().get(message.getMessageType()));
        response.setIsRead(message.getIsRead());
        response.setCreateTime(message.getCreateTime());
        response.setUpdateTime(message.getUpdateTime());

        if (member != null) {
            response.setMemberNickname(member.getNickname());
            response.setMemberAvatar(member.getAvatar());
            response.setMemberPhone(member.getPhone());
        }

        return response;
    }

    /**
     * 获取消息类型映射
     */
    private Map<Integer, String> getMessageTypeMap() {
        Map<Integer, String> messageTypeMap = new HashMap<>();
        messageTypeMap.put(1, "店铺公告");
        messageTypeMap.put(2, "优惠通知");
        messageTypeMap.put(3, "其他");
        return messageTypeMap;
    }
}
