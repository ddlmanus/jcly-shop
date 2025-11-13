package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeMessage;
import com.zbkj.common.request.PageParamRequest;

/**
 * <p>
 * 扣子消息表 服务类
 * </p>
 *
 * @author dudl
 * @since 2024-01-01
 */
public interface CozeMessageService extends IService<CozeMessage> {

    /**
     * 分页查询商户的消息列表
     * @param paramRequest 分页对象
     * @param merId 商户ID
     * @param conversationId 会话ID (可选)
     * @param messageType 消息类型 (可选)
     * @return 分页结果
     */
    PageInfo<CozeMessage> getMessagesByMerchant(PageParamRequest paramRequest, Integer merId, String conversationId, String messageType);

    /**
     * 根据消息ID和商户ID查询消息详情
     * @param messageId 消息ID
     * @param merId 商户ID
     * @return 消息详情
     */
    CozeMessage getByMessageIdAndMerchant(String messageId, Integer merId);

    /**
     * 根据Coze消息ID和商户ID查询消息
     * @param cozeMessageId Coze消息ID
     * @param merId 商户ID
     * @return 消息信息
     */
    CozeMessage getByCozeMessageIdAndMerchant(String cozeMessageId, Integer merId);

    /**
     * 保存或更新消息信息
     * @param message 消息对象
     * @return 是否成功
     */
    boolean saveOrUpdateMessage(CozeMessage message);

    /**
     * 删除商户的消息
     * @param messageId 消息ID
     * @param merId 商户ID
     * @return 是否成功
     */
    boolean deleteMessageByMerchant(String messageId, Integer merId);

    /**
     * 根据会话ID和商户ID查询消息列表
     * @param conversationId 会话ID
     * @param merId 商户ID
     * @return 消息列表
     */
    PageInfo<CozeMessage> getMessagesByConversationAndMerchant(PageParamRequest paramRequest, String conversationId, Integer merId);
}
