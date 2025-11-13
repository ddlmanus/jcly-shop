package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeConversation;
import com.zbkj.common.request.PageParamRequest;

/**
 * <p>
 * 扣子会话表 服务类
 * </p>
 *
 * @author dudl
 * @since 2024-01-01
 */
public interface CozeConversationService extends IService<CozeConversation> {

    /**
     * 分页查询商户的会话列表
     * @param paramRequest 分页对象
     * @param merId 商户ID
     * @param botId 智能体ID (可选)
     * @return 分页结果
     */
    PageInfo<CozeConversation> getConversationsByMerchant(PageParamRequest paramRequest, Integer merId, String botId);

    /**
     * 根据会话ID和商户ID查询会话详情
     * @param conversationId 会话ID
     * @param merId 商户ID
     * @return 会话详情
     */
    CozeConversation getByConversationIdAndMerchant(String conversationId, Integer merId);

    /**
     * 根据Coze会话ID和商户ID查询会话
     * @param cozeConversationId Coze会话ID
     * @param merId 商户ID
     * @return 会话信息
     */
    CozeConversation getByCozeConversationIdAndMerchant(String cozeConversationId, Integer merId);

    /**
     * 保存或更新会话信息
     * @param conversation 会话对象
     * @return 是否成功
     */
    boolean saveOrUpdateConversation(CozeConversation conversation);

    /**
     * 删除商户的会话
     * @param conversationId 会话ID
     * @param merId 商户ID
     * @return 是否成功
     */
    boolean deleteConversationByMerchant(String conversationId, Integer merId);
}
