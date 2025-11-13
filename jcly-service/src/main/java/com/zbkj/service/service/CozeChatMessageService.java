package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.coze.CozeChatMessage;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

/**
 * Coze聊天消息服务接口
 * @author: auto-generated
 * @date: 2024/01/01
 */
public interface CozeChatMessageService extends IService<CozeChatMessage> {

    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return CommonPage
     */
    CommonPage<CozeChatMessage> getList(PageParamRequest pageParamRequest);

    /**
     * 根据会话ID获取消息列表
     * @param sessionId 会话ID
     * @param pageParamRequest 分页参数
     * @return 消息列表
     */
    CommonPage<CozeChatMessage> getBySessionId(Integer sessionId, PageParamRequest pageParamRequest);

    /**
     * 根据会话ID获取所有消息（不分页）
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<CozeChatMessage> getAllBySessionId(Integer sessionId);

    /**
     * 根据Coze消息ID获取消息
     * @param cozeMessageId Coze消息ID
     * @return 消息信息
     */
    CozeChatMessage getByCozeMessageId(String cozeMessageId);

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
    CozeChatMessage createMessage(Integer sessionId, String cozeMessageId, String cozeChatId, 
                                  String role, String content, String contentType);

    /**
     * 更新消息内容
     * @param messageId 消息ID
     * @param content 新的消息内容
     * @return 是否成功
     */
    Boolean updateMessageContent(Integer messageId, String content);

    /**
     * 删除消息
     * @param messageId 消息ID
     * @return 是否成功
     */
    Boolean deleteMessage(Integer messageId);

    /**
     * 根据会话ID删除所有消息
     * @param sessionId 会话ID
     * @return 是否成功
     */
    Boolean deleteBySessionId(Integer sessionId);

    /**
     * 根据用户ID删除所有消息
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteByUserId(Integer userId);
}
