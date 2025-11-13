package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.coze.CozeChatSession;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

/**
 * Coze聊天会话服务接口
 * @author: auto-generated
 * @date: 2024/01/01
 */
public interface CozeChatSessionService extends IService<CozeChatSession> {

    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return CommonPage
     */
    CommonPage<CozeChatSession> getList(PageParamRequest pageParamRequest);

    /**
     * 根据用户ID获取聊天会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<CozeChatSession> getByUserId(Integer userId);

    /**
     * 根据用户ID和Coze会话ID获取会话
     * @param userId 用户ID
     * @param cozeSessionId Coze会话ID
     * @return 会话信息
     */
    CozeChatSession getByUserIdAndCozeSessionId(Integer userId, String cozeSessionId);

    /**
     * 创建新的聊天会话
     * @param userId 用户ID
     * @param cozeBotId Coze智能体ID
     * @param cozeSessionId Coze会话ID
     * @param sessionName 会话名称
     * @return 创建的会话
     */
    CozeChatSession createSession(Integer userId, String cozeBotId, String cozeSessionId, String sessionName);

    /**
     * 更新会话名称
     * @param sessionId 会话ID
     * @param sessionName 新的会话名称
     * @return 是否成功
     */
    Boolean updateSessionName(Integer sessionId, String sessionName);

    /**
     * 删除会话
     * @param sessionId 会话ID
     * @return 是否成功
     */
    Boolean deleteSession(Integer sessionId);

    /**
     * 根据用户ID删除所有会话
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteByUserId(Integer userId);
}
