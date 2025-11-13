package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.merchant.MerchantMemberMessage;
import com.zbkj.common.request.MemberMessagePageRequest;
import com.zbkj.common.request.MemberSendMessageRequest;
import com.zbkj.common.response.MerchantMemberMessageResponse;

import java.util.List;

/**
 * 商户会员消息服务接口
 */
public interface MerchantMemberMessageService extends IService<MerchantMemberMessage> {

    /**
     * 发送消息给会员
     * @param request 发送消息请求
     * @return 是否成功
     */
    boolean sendMessage(MemberSendMessageRequest request);

    /**
     * 获取消息列表
     * @param request 查询请求
     * @return 消息列表
     */
    List<MerchantMemberMessageResponse> getMessageList(MemberMessagePageRequest request);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean markAsRead(Integer messageId);

    /**
     * 批量标记消息为已读
     * @param messageIds 消息ID列表
     * @return 是否成功
     */
    boolean batchMarkAsRead(List<Integer> messageIds);

    /**
     * 删除消息
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean deleteMessage(Integer messageId);

    /**
     * 获取会员未读消息数量
     * @param merId 商户ID
     * @param uid 用户ID
     * @return 未读消息数量
     */
    int getUnreadCount(Integer merId, Integer uid);

    /**
     * 根据ID获取消息详情
     * @param messageId 消息ID
     * @return 消息详情
     */
    MerchantMemberMessageResponse getMessageDetail(Integer messageId);
}
