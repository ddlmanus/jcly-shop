package com.zbkj.service.service;

import com.zbkj.common.model.coze.CozeBotConfig;
import com.zbkj.common.request.coze.*;
import com.zbkj.common.response.coze.*;

import java.util.List;
import java.util.Map;

/**
 * Coze AI 服务接口
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
public interface CozeService {

    // =================================================================================
    // 智能体管理相关接口
    // =================================================================================

    /**
     * 创建智能体
     * @param request 创建智能体请求参数
     * @return 创建智能体响应
     */
    CozeCreateBotResponse createBot(CozeCreateBotRequest request);

    /**
     * 更新智能体
     * @param request 更新智能体请求参数
     * @return 基础响应
     */
    CozeBaseResponse updateBot(CozeUpdateBotRequest request);

    /**
     * 发布智能体
     * @param request 发布智能体请求参数
     * @return 基础响应
     */
    CozeBaseResponse publishBot(CozePublishBotRequest request);

    /**
     * 下架智能体
     * @param request 下架智能体请求参数
     * @return 基础响应
     */
    CozeBaseResponse unpublishBot(CozeUnpublishBotRequest request);

    /**
     * 查看智能体列表
     * @param spaceId 空间ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 智能体列表响应
     */
    CozeBotListResponse getBotList(String spaceId, Integer pageNum, Integer pageSize);

    /**
     * 查看智能体配置
     * @param botId 智能体ID
     * @return 智能体配置响应
     */
    Object getBotConfig(String botId);

    /**
     * 获取本地智能体配置
     * @param botId 智能体ID
     * @return 智能体配置
     */
    CozeBotConfig getLocalBotConfig(String botId);

    /**
     * 同步智能体配置到本地
     * @param botId 智能体ID
     * @param merchantId 商户ID
     * @return 是否成功
     */
    Boolean syncBotConfigToLocal(String botId, Integer merchantId);



    // =================================================================================
    // 会话管理相关接口
    // =================================================================================

    /**
     * 创建会话
     * @param request 创建会话请求参数
     * @return 创建会话响应
     */
    CozeCreateConversationResponse createConversation(CozeCreateConversationRequest request);

    /**
     * 删除会话
     * @param conversationId 会话ID
     * @return 基础响应
     */
    CozeBaseResponse deleteConversation(String conversationId);

    /**
     * 更新会话名称
     * @param request 更新会话名称请求参数
     * @return 更新会话响应
     */
    CozeUpdateConversationResponse updateConversationName(CozeUpdateConversationRequest request);

    /**
     * 查看会话列表
     * @param request 查看会话列表请求参数
     * @return 会话列表响应
     */
    CozeGetConversationListResponse getConversationList(CozeGetConversationListRequest request);

    /**
     * 清除会话上下文
     * @param conversationId 会话ID
     * @return 基础响应
     */
    CozeBaseResponse clearConversationContext(String conversationId);

    // =================================================================================
    // 消息管理相关接口
    // =================================================================================

    /**
     * 创建消息
     * @param request 创建消息请求参数
     * @return 创建消息响应
     */
    CozeCreateMessageResponse createMessage(CozeCreateMessageRequest request);

    /**
     * 修改消息
     * @param request 修改消息请求参数
     * @return 修改消息响应
     */
    CozeModifyMessageResponse modifyMessage(CozeModifyMessageRequest request);

    /**
     * 删除消息
     * @param conversationId 会话ID
     * @param messageId 消息ID
     * @return 删除消息响应
     */
    CozeCreateMessageResponse deleteMessage(String conversationId, String messageId);

    /**
     * 查看消息列表
     * @param request 查看消息列表请求参数
     * @return 消息列表响应
     */
    CozeGetMessageListResponse getMessageList(CozeGetMessageListRequest request);

    /**
     * 查看消息详情
     * @param conversationId 会话ID
     * @param messageId 消息ID
     * @return 消息详情响应
     */
    CozeGetMessageDetailResponse getMessageDetail(String conversationId, String messageId);

    // =================================================================================
    // 对话相关接口
    // =================================================================================

    /**
     * 发起对话
     * @param request 发起对话请求参数
     * @return 对话响应
     */
    Object startChat(CozeStartChatRequest request);

    /**
     * 发起对话（通用参数版本）
     * @param requestParams 请求参数Map
     * @return 对话响应
     */
    Object startChat(Map<String, Object> requestParams);

    /**
     * 发起流式对话
     * @param requestParams 请求参数Map
     * @return 流式响应结果
     */
    com.zbkj.common.model.coze.stream.CozeStreamResponse startStreamChat(Map<String, Object> requestParams);

    /**
     * 取消进行中的对话
     * @param request 取消对话请求参数
     * @return 对话详情响应
     */
    Object cancelChat(CozeCancelChatRequest request);

    /**
     * 查看对话详情
     * @param conversationId 会话ID
     * @param chatId 对话ID
     * @return 对话详情响应
     */
    Object getChatDetail(String conversationId, String chatId);

    /**
     * 查看对话消息详情
     * @param conversationId 会话ID
     * @param chatId 对话ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 对话消息详情响应
     */
    Object getChatMessages(String conversationId, String chatId, Integer pageNum, Integer pageSize);

    // =================================================================================
    // 知识库管理相关接口
    // =================================================================================

    /**
     * 创建知识库
     * @param request 创建知识库请求参数
     * @return 创建知识库响应
     */
    CozeCreateKnowledgeResponse createKnowledge(CozeCreateKnowledgeRequest request);

    /**
     * 修改知识库信息
     * @param request 修改知识库请求参数
     * @return 基础响应
     */
    CozeBaseResponse updateKnowledge(CozeUpdateKnowledgeRequest request);

    /**
     * 删除知识库
     * @param datasetId 知识库ID
     * @return 基础响应
     */
    CozeBaseResponse deleteKnowledge(String datasetId);

    /**
     * 查看知识库列表（强类型返回）
     * @param spaceId 空间ID
     * @param name 知识库名称(可选)
     * @param formatType 知识库类型(可选)
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 知识库列表响应
     */
    CozeKnowledgeListResponse getKnowledgeListTyped(String spaceId, String name, Integer formatType, Integer pageNum, Integer pageSize);

    /**
     * 创建知识库文件（上传文件到知识库）
     * @param request 创建知识库文件请求参数
     * @return 创建知识库文件响应
     */
    CozeCreateKnowledgeFileResponse createKnowledgeFile(CozeCreateKnowledgeFileRequest request);

    /**
     * 创建知识库文件（支持原始文件信息）
     * @param request 创建知识库文件请求参数
     * @param originalFileSizes 原始文件大小映射（文件名->大小）
     * @return 创建知识库文件响应
     */
    CozeCreateKnowledgeFileResponse createKnowledgeFile(CozeCreateKnowledgeFileRequest request, Map<String, Long> originalFileSizes);

    /**
     * 创建知识库文件（支持原始文件信息和下载URL）
     * @param request 创建知识库文件请求参数
     * @param originalFileSizes 原始文件大小映射（文件名->大小）
     * @param fileDownloadUrls 文件下载URL映射（文件名->URL）
     * @return 创建知识库文件响应
     */
    CozeCreateKnowledgeFileResponse createKnowledgeFile(CozeCreateKnowledgeFileRequest request, Map<String, Long> originalFileSizes, Map<String, String> fileDownloadUrls);

    /**
     * 修改知识库文件
     * @param request 修改知识库文件请求参数
     * @return 基础响应
     */
    CozeBaseResponse updateKnowledgeFile(CozeUpdateKnowledgeFileRequest request);

    /**
     * 删除知识库文件
     * @param documentIds 文件ID列表
     * @return 基础响应
     */
    CozeBaseResponse deleteKnowledgeFile(List<String> documentIds);

    /**
     * 查看知识库文件列表（强类型返回）
     * @param datasetId 知识库ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 知识库文件列表响应
     */
    CozeKnowledgeFileListResponse getKnowledgeFileListTyped(String datasetId, Integer pageNum, Integer pageSize);


    /**
     * 查看知识库文件上传进度（强类型返回，符合API文档）
     * @param datasetId 知识库ID
     * @param documentIds 文件ID列表
     * @return 上传进度响应
     */
    CozeKnowledgeFileProgressResponse getKnowledgeFileProgressBatch(String datasetId, List<String> documentIds);

    // =================================================================================
    // 工作流相关接口
    // =================================================================================

    /**
     * 执行工作流
     * @param request 执行工作流请求参数
     * @return 工作流执行响应
     */
    CozeExecuteWorkflowResponse runWorkflow(CozeExecuteWorkflowRequest request);

    /**
     * 执行对话流
     * @param request 执行对话流请求参数
     * @return 对话流执行响应
     */
    CozeExecuteWorkflowResponse runChatWorkflow(CozeExecuteWorkflowRequest request);

    /**
     * 查询工作流列表
     * @param request 查询工作流列表请求参数
     * @return 工作流列表响应
     */
    CozeGetWorkflowListResponse getWorkflowList(CozeGetWorkflowListRequest request);

    // =================================================================================
    // 其他相关接口
    // =================================================================================

    /**
     * 查看空间列表
     * @return 空间列表响应
     */
    CozeGetSpaceListResponse getSpaceList(CozeGetSpaceListRequest request);

    /**
     * 获取空间成员列表
     * @param request 获取空间成员请求
     * @return 空间成员列表响应
     */
    CozeGetSpaceMembersResponse getSpaceMembers(CozeGetSpaceMembersRequest request);

    /**
     * 查看空间成员列表（强类型版本）
     * @param workspaceId 空间ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 空间成员列表响应
     */
    CozeGetSpaceMembersResponse getSpaceMembersTyped(String workspaceId, Integer pageNum, Integer pageSize);

    /**
     * 上传文件到Coze
     * @param file 要上传的文件
     * @return 上传文件响应
     */
    CozeUploadFileResponse uploadFile(org.springframework.web.multipart.MultipartFile file);

    /**
     * 查看应用列表
     * @param spaceId 空间ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 应用列表响应
     */
    Object getAppList(String spaceId, Integer pageNum, Integer pageSize);

    /**
     * 添加发布渠道
     * @param request 添加发布渠道请求参数
     * @return 基础响应
     */
    CozeBaseResponse addChannel(Object request);

    /**
     * 更新审核结果
     * @param request 更新审核结果请求参数
     * @return 基础响应
     */
    CozeBaseResponse updateAuditResult(Object request);

    CozeBotListResponse getplatBotList(String spaceId, Integer pageNum, Integer pageSize);

    // =================================================================================
    // 语音通话相关接口
    // =================================================================================

    /**
     * 创建房间
     * @param request 创建房间请求参数
     * @return 创建房间响应
     */
    CozeCreateRoomResponse createRoom(CozeCreateRoomRequest request);

    /**
     * 语音识别
     * @param request 语音识别请求参数
     * @return 语音识别响应
     */
    CozeVoiceTranscriptionResponse transcribeAudio(CozeVoiceTranscriptionRequest request);

    /**
     * 语音合成
     * @param request 语音合成请求参数
     * @return 语音合成音频数据
     */
    byte[] synthesizeSpeech(CozeVoiceSpeechRequest request);

    /**
     * 复刻音色
     * @param request 复刻音色请求参数
     * @return 复刻音色响应
     */
    CozeVoiceCloneResponse cloneVoice(CozeVoiceCloneRequest request);

    /**
     * 查看音色列表
     * @param request 查看音色列表请求参数
     * @return 音色列表响应
     */
    CozeVoiceListResponse getVoiceList(CozeVoiceListRequest request);
}
