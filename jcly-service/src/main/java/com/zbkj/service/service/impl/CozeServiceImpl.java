package com.zbkj.service.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbkj.common.model.coze.*;
import com.zbkj.common.request.coze.*;
import com.zbkj.common.response.coze.*;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.*;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.util.*;

/**
 * Coze AI 服务实现类
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
@Slf4j
@Service
public class CozeServiceImpl implements CozeService {

    @Resource(name = "cozeRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private CozeBotService cozeBotService;
    
    @Autowired
    private CozeKnowledgeService cozeKnowledgeService;
    
    @Autowired
    private CozeKnowledgeFileService cozeKnowledgeFileService;
    
    @Autowired
    private CozeWorkflowService cozeWorkflowService;
    
    @Autowired
    private CozeConversationService cozeConversationService;
    
    @Autowired
    private CozeMessageService cozeMessageService;
    
    @Autowired
    private CozeJwtService cozeJwtService;

    @Autowired
    private CozeStreamClient cozeStreamClient;

    @Autowired
    private UnifiedChatService unifiedChatService;
    
    @Autowired
    private CozeBotConfigService cozeBotConfigService;

    @Value("${coze.api.baseUrl:https://api.coze.cn}")
    private String cozeBaseUrl;
    
    @Value("${coze.rtc.appId:68d10fb10926e4017477e5f9}")
    private String rtcAppId;
    
    @Value("${coze.rtc.enableAppIdMapping:true}")
    private boolean enableAppIdMapping;
    @Autowired
    private UserService userService;

    /**
     * 获取请求头
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String accessToken = cozeJwtService.getAccessToken();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    /**
     * 根据消息数据确定消息状态
     * 注意：Coze API返回的消息对象没有status字段，根据消息类型返回合适的状态
     */
    private String determineMessageStatus(CozeCreateMessageResponse.MessageData messageData) {
        // Coze API返回的消息没有status字段，根据消息类型返回状态
        if (messageData.getType() == null || messageData.getType().isEmpty()) {
            return "created";
        }
        return "completed";
    }

    // =================================================================================
    // 智能体管理相关接口
    // =================================================================================

    @Override
    public CozeCreateBotResponse createBot(CozeCreateBotRequest request) {
        try {
            // 调用Coze API创建智能体
            String url = cozeBaseUrl + "/v1/bot/create";
            HttpEntity<CozeCreateBotRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeCreateBotResponse> response = restTemplate.postForEntity(url, entity, CozeCreateBotResponse.class);
            CozeCreateBotResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，保存到本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                CozeBot cozeBot = new CozeBot();
                cozeBot.setMerchantId(merchantId);
                cozeBot.setCozeBotId(cozeResponse.getData().getBotId());
                cozeBot.setName(request.getName());
                cozeBot.setDescription(request.getDescription());
                cozeBot.setIconUrl(request.getIconFileId()); // 存储文件ID作为图标URL
                // 将PromptInfo转换为JSON字符串存储
                if (request.getPromptInfo() != null && request.getPromptInfo().getPrompt() != null) {
                    cozeBot.setPromptInfo(request.getPromptInfo().getPrompt());
                }
                cozeBot.setVersion(1); // 新创建默认为草稿版本
                cozeBot.setPublishStatus(0); // 新创建默认为未发布
                cozeBot.setSpaceId(request.getSpaceId());
                // 存储模型ID
                if (request.getModelInfoConfig() != null && request.getModelInfoConfig().getModelId() != null) {
                    cozeBot.setModel(request.getModelInfoConfig().getModelId());
                }
                cozeBot.setStatus(1);
                cozeBot.setCreateTime(new Date());
                cozeBot.setUpdateTime(new Date());
                
                cozeBotService.saveOrUpdateBot(cozeBot);
                log.info("智能体保存到数据库成功, Bot ID: {}", cozeResponse.getData().getBotId());
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("创建智能体失败", e);
            throw new RuntimeException("创建智能体失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse updateBot(CozeUpdateBotRequest request) {
        try {
            // 调用Coze API更新智能体
            String url = cozeBaseUrl + "/v1/bot/update";
            HttpEntity<CozeUpdateBotRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.postForEntity(url, entity, CozeBaseResponse.class);
            CozeBaseResponse cozeResponse = response.getBody();
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merchantId = loginUser.getUser().getMerId();
            // 如果API调用成功，同步更新本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                CozeBot existingBot = cozeBotService.getByCozeBotId(request.getBotId(),merchantId);
                if (existingBot != null) {
                    existingBot.setName(request.getName());
                    existingBot.setDescription(request.getDescription());
                    existingBot.setIconUrl(request.getIconFileId()); // 存储文件ID作为图标URL
                    
                    // 更新提示语(Instructions)
                    if (request.getPromptInfo() != null && request.getPromptInfo().getPrompt() != null) {
                        existingBot.setInstructions(request.getPromptInfo().getPrompt());
                    }
                    
                    // 更新模型ID
                    if (request.getModelInfoConfig() != null && request.getModelInfoConfig().getModelId() != null) {
                        existingBot.setModel(request.getModelInfoConfig().getModelId());
                    }
                    
                    // 更新开场白信息 (存储到promptInfo字段)
                    if (request.getOnboardingInfo() != null) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            String onboardingJson = objectMapper.writeValueAsString(request.getOnboardingInfo());
                            existingBot.setPromptInfo(onboardingJson);
                        } catch (Exception e) {
                            log.warn("序列化开场白信息失败", e);
                        }
                    }
                    
                    // 更新知识库绑定信息
                    if (request.getKnowledge() != null && request.getKnowledge().getDatasetIds() != null) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            String knowledgeJson = objectMapper.writeValueAsString(request.getKnowledge().getDatasetIds());
                            existingBot.setKnowledgeIds(knowledgeJson);
                        } catch (Exception e) {
                            log.warn("序列化知识库信息失败", e);
                        }
                    }
                    
                    existingBot.setUpdateTime(new Date());
                    
                    cozeBotService.saveOrUpdateBot(existingBot);
                    log.info("智能体更新到数据库成功, Bot ID: {}", request.getBotId());
                }
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("更新智能体失败", e);
            throw new RuntimeException("更新智能体失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse publishBot(CozePublishBotRequest request) {
        try {
            String url = cozeBaseUrl + "/v1/bot/publish";
            HttpEntity<CozePublishBotRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.postForEntity(url, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("发布智能体失败", e);
            throw new RuntimeException("发布智能体失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse unpublishBot(CozeUnpublishBotRequest request) {
        try {
            String url = cozeBaseUrl + "/v1/bots/" + request.getBotId() + "/unpublish";
            HttpEntity<CozeUnpublishBotRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.postForEntity(url, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("下架智能体失败", e);
            throw new RuntimeException("下架智能体失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBotListResponse getBotList(String spaceId, Integer pageNum, Integer pageSize) {
        try {
            String url = cozeBaseUrl + "/v1/bots?workspace_id=" + spaceId + "&page_num=" + pageNum + "&page_size=" + pageSize;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeBotListResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeBotListResponse.class);
            
            CozeBotListResponse botListResponse = response.getBody();
            
            // 获取当前用户的商户ID
            try {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                if (loginUser != null && loginUser.getUser() != null) {
                    Integer merchantId = loginUser.getUser().getMerId();
                    // 同步数据到本地数据库
                    syncBotListToLocal(botListResponse, merchantId, spaceId);
                }
            } catch (Exception syncException) {
                // 同步失败不影响API返回，只记录日志
                log.warn("同步智能体数据到本地失败: {}", syncException.getMessage());
            }
            
            return botListResponse;
        } catch (Exception e) {
            log.error("查看智能体列表失败", e);
            throw new RuntimeException("查看智能体列表失败: " + e.getMessage());
        }
    }

    /**
     * 同步智能体列表到本地数据库
     */
    private void syncBotListToLocal(CozeBotListResponse response, Integer merchantId, String spaceId) {
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            return;
        }

        CozeBotListResponse.BotListData data = response.getData();
        List<CozeBotListResponse.BotInfo> bots = data.getItems();
        
        if (bots != null && !bots.isEmpty()) {
            for (CozeBotListResponse.BotInfo botInfo : bots) {
                saveBotToLocal(botInfo, merchantId, spaceId);
            }
        }
    }

    @Override
    public Object getBotConfig(String botId) {
        try {
            String url = cozeBaseUrl + "/v1/bot/get_online_info?bot_id=" + botId;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("查看智能体配置失败", e);
            throw new RuntimeException("查看智能体配置失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBotConfig getLocalBotConfig(String botId) {
        return cozeBotConfigService.getByCozeBotId(botId);
    }

    @Override
    public Boolean syncBotConfigToLocal(String botId, Integer merchantId) {
        try {
            // 获取智能体配置
            Object botConfig = getBotConfig(botId);
            if (botConfig == null) {
                log.warn("智能体配置为空: {}", botId);
                return false;
            }

            // 解析配置并保存到数据库
            CozeBotConfig config = parseBotConfigFromResponse(botConfig, merchantId);
            if (config != null) {
                config.setCozeBotId(botId);
                config.setMerchantId(merchantId);
                config.setStatus(1);
                config.setUpdateTime(new Date());
                
                if (config.getCreateTime() == null) {
                    config.setCreateTime(new Date());
                }

                return cozeBotConfigService.saveOrUpdateBotConfig(config);
            }
            
            return false;
        } catch (Exception e) {
            log.error("同步智能体配置失败: {}", e.getMessage(), e);
            return false;
        }
    }



    /**
     * 保存智能体到本地数据库
     */
    private void saveBotToLocal(CozeBotListResponse.BotInfo botInfo, Integer merchantId, String spaceId) {
        try {
            // 检查是否已存在
            CozeBot existingBot = cozeBotService.getByCozeBotId(botInfo.getBotId(),merchantId);
            
            CozeBot bot = new CozeBot();
            if (existingBot != null) {
                bot = existingBot;
            }
            
            // 设置基本信息
            bot.setMerchantId(merchantId);
            bot.setCozeBotId(botInfo.getBotId());
            bot.setName(botInfo.getName());
            bot.setDescription(botInfo.getDescription());
            bot.setIconUrl(botInfo.getIconUrl());
            bot.setSpaceId(spaceId);
            bot.setVersion(1); // 默认草稿版本
            if(botInfo.isPublishStatus()){
                bot.setPublishStatus(1);
            }else{
                bot.setPublishStatus(0);
            }
            bot.setStatus(1); // 默认启用
            bot.setIsDefault(0); // 默认非默认智能体
            
            // 设置模型信息（如果有的话）
            if (botInfo.getModelId() != null && !botInfo.getModelId().isEmpty()) {
                bot.setModel(botInfo.getModelId());
                log.debug("从列表API获取到模型ID: {} - {}", botInfo.getBotId(), botInfo.getModelId());
            }
            
            // 设置提示词信息
            if (botInfo.getPromptInfo() != null && botInfo.getPromptInfo().getPrompt() != null) {
                bot.setInstructions(botInfo.getPromptInfo().getPrompt());
                log.debug("从列表API获取到提示词信息: {} - {}", botInfo.getBotId(), botInfo.getPromptInfo().getPrompt().length() + " chars");
            }
            
            // 设置开场白信息（存储完整的JSON）
            if (botInfo.getOnboardingInfo() != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String onboardingJson = objectMapper.writeValueAsString(botInfo.getOnboardingInfo());
                    bot.setPromptInfo(onboardingJson);
                    log.debug("从列表API获取到开场白信息: {} - {}", botInfo.getBotId(), onboardingJson);
                } catch (Exception e) {
                    log.warn("序列化开场白信息失败: {}", e.getMessage());
                    // 如果序列化失败，至少保存开场白文本
                    if (botInfo.getOnboardingInfo().getPrologue() != null) {
                        bot.setPromptInfo(botInfo.getOnboardingInfo().getPrologue());
                    }
                }
            }
            
            // 对于已发布的智能体，尝试获取详细配置以获取完整的模型和知识库信息
            if (botInfo.isPublishStatus()) {
                try {
                    Object botConfig = getBotConfig(botInfo.getBotId());
                    if (botConfig != null) {
                        // 解析配置中的知识库信息
                        extractKnowledgeFromBotConfig(bot, botConfig);
                        // 解析配置中的模型信息（如果列表API没有提供的话）
                        extractModelInfoFromBotConfig(bot, botConfig);
                        log.debug("成功获取智能体详细配置: {}", botInfo.getBotId());
                        
                        // 同时保存配置信息到 eb_coze_bot_config 表
                        saveBotConfigToLocal(botInfo.getBotId(), botConfig, bot.getMerchantId());
                    }
                } catch (Exception e) {
                    log.warn("获取智能体详细配置失败: {} - {}，将使用列表API提供的基本信息", botInfo.getBotId(), e.getMessage());
                }
            } else {
                log.debug("智能体未发布，跳过详细配置获取: {}", botInfo.getBotId());
            }
            
            // 保存或更新
            if (existingBot == null) {
                bot.setCreateTime(new java.util.Date());
                bot.setUpdateTime(new java.util.Date());
                cozeBotService.save(bot);
            } else {
                bot.setUpdateTime(new java.util.Date());
                cozeBotService.updateById(bot);
            }
            
            log.info("同步智能体成功: {} - {}", botInfo.getBotId(), botInfo.getName());
        } catch (Exception e) {
            log.error("保存智能体到本地失败: {} - {}", botInfo.getBotId(), e.getMessage());
        }
    }
    
    /**
     * 从智能体配置中提取知识库信息
     */
    private void extractKnowledgeFromBotConfig(CozeBot bot, Object botConfig) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String configJson = objectMapper.writeValueAsString(botConfig);
            
            // 解析JSON配置
            JsonNode configNode = objectMapper.readTree(configJson);
            JsonNode dataNode = configNode.get("data");
            
            if (dataNode != null) {
                JsonNode knowledgeNode = dataNode.get("knowledge");
                if (knowledgeNode != null && knowledgeNode.isArray()) {
                    List<String> knowledgeIds = new ArrayList<>();
                    for (JsonNode knowledge : knowledgeNode) {
                        JsonNode idNode = knowledge.get("dataset_id");
                        if (idNode != null) {
                            knowledgeIds.add(idNode.asText());
                        }
                    }
                    
                    if (!knowledgeIds.isEmpty()) {
                        String knowledgeJson = objectMapper.writeValueAsString(knowledgeIds);
                        bot.setKnowledgeIds(knowledgeJson);
                        log.info("同步智能体知识库信息: {} - {}", bot.getCozeBotId(), knowledgeIds);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析智能体配置中的知识库信息失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从智能体配置中提取模型信息
     */
    private void extractModelInfoFromBotConfig(CozeBot bot, Object botConfig) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String configJson = objectMapper.writeValueAsString(botConfig);
            
            // 解析JSON配置
            JsonNode configNode = objectMapper.readTree(configJson);
            JsonNode dataNode = configNode.get("data");
            
            if (dataNode != null) {
                // 如果bot还没有模型信息，尝试从配置中获取
                if (bot.getModel() == null || bot.getModel().isEmpty()) {
                    JsonNode modelNode = dataNode.get("model");
                    if (modelNode != null && !modelNode.isNull()) {
                        String modelId = modelNode.asText();
                        if (modelId != null && !modelId.isEmpty()) {
                            bot.setModel(modelId);
                            log.info("从配置中提取到模型ID: {} - {}", bot.getCozeBotId(), modelId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析智能体配置中的模型信息失败: {}", e.getMessage());
        }
    }
    
    /**
     * 在同步智能体列表时保存配置信息到本地
     */
    private void saveBotConfigToLocal(String botId, Object botConfig, Integer merchantId) {
        try {
            // 解析配置信息为CozeBotConfig对象
            CozeBotConfig config = parseBotConfigFromResponse(botConfig, merchantId);
            if (config != null) {
                config.setCozeBotId(botId);
                config.setMerchantId(merchantId);
                config.setCreateTime(new java.util.Date());
                config.setUpdateTime(new java.util.Date());
                
                // 保存或更新配置
                cozeBotConfigService.saveOrUpdateBotConfig(config);
                log.info("同步智能体配置到本地成功: {}", botId);
            }
        } catch (Exception e) {
            log.warn("同步智能体配置到本地失败: {} - {}", botId, e.getMessage());
        }
    }

    /**
     * 解析智能体配置响应为CozeBotConfig对象
     */
    private CozeBotConfig parseBotConfigFromResponse(Object botConfigResponse, Integer merchantId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String configJson = objectMapper.writeValueAsString(botConfigResponse);
            
            JsonNode rootNode = objectMapper.readTree(configJson);
            JsonNode dataNode = rootNode.get("data");
            
            if (dataNode == null) {
                log.warn("智能体配置响应中没有data节点");
                return null;
            }
            
            CozeBotConfig config = new CozeBotConfig();
            config.setMerchantId(merchantId);
            
            // 基本信息
            config.setName(getJsonText(dataNode, "name"));
            config.setDescription(getJsonText(dataNode, "description"));
            config.setBotMode(getJsonInt(dataNode, "bot_mode"));
            config.setFolderId(getJsonText(dataNode, "folder_id"));
            config.setVersion(getJsonText(dataNode, "version"));
            config.setOwnerUserId(getJsonText(dataNode, "owner_user_id"));
            config.setIconUrl(getJsonText(dataNode, "icon_url"));
            config.setDefaultUserInputType(getJsonText(dataNode, "default_user_input_type"));
            config.setCozeCreateTime(getJsonLong(dataNode, "create_time"));
            config.setCozeUpdateTime(getJsonLong(dataNode, "update_time"));
            
            // 复杂对象转为JSON字符串
            config.setModelInfo(getJsonObjectAsString(dataNode, "model_info", objectMapper));
            config.setKnowledge(getJsonObjectAsString(dataNode, "knowledge", objectMapper));
            config.setVoiceDataList(getJsonArrayAsString(dataNode, "voice_data_list", objectMapper));
            config.setPluginInfoList(getJsonArrayAsString(dataNode, "plugin_info_list", objectMapper));
            config.setShortcutCommands(getJsonArrayAsString(dataNode, "shortcut_commands", objectMapper));
            config.setPromptInfo(getJsonObjectAsString(dataNode, "prompt_info", objectMapper));
            config.setOnboardingInfo(getJsonObjectAsString(dataNode, "onboarding_info", objectMapper));
            config.setWorkflowInfoList(getJsonArrayAsString(dataNode, "workflow_info_list", objectMapper));
            config.setVoiceInfoList(getJsonArrayAsString(dataNode, "voice_info_list", objectMapper));
            config.setBackgroundImageInfo(getJsonObjectAsString(dataNode, "background_image_info", objectMapper));
            config.setSuggestReplyInfo(getJsonObjectAsString(dataNode, "suggest_reply_info", objectMapper));
            config.setVariables(getJsonArrayAsString(dataNode, "variables", objectMapper));
            
            // 保存完整配置JSON
            config.setFullConfigJson(configJson);
            
            return config;
        } catch (Exception e) {
            log.error("解析智能体配置失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // 辅助方法
    private String getJsonText(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }
    
    private Integer getJsonInt(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asInt() : null;
    }
    
    private Long getJsonLong(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asLong() : null;
    }
    
    private String getJsonObjectAsString(JsonNode node, String fieldName, ObjectMapper objectMapper) {
        try {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull() && !fieldNode.isEmpty()) {
                return objectMapper.writeValueAsString(fieldNode);
            }
            return null;
        } catch (Exception e) {
            log.warn("序列化JSON对象失败: {} - {}", fieldName, e.getMessage());
            return null;
        }
    }
    
    private String getJsonArrayAsString(JsonNode node, String fieldName, ObjectMapper objectMapper) {
        try {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && fieldNode.isArray() && fieldNode.size() > 0) {
                return objectMapper.writeValueAsString(fieldNode);
            }
            return null;
        } catch (Exception e) {
            log.warn("序列化JSON数组失败: {} - {}", fieldName, e.getMessage());
            return null;
        }
    }

    // =================================================================================
    // 会话管理相关接口
    // =================================================================================

    @Override
    public CozeCreateConversationResponse createConversation(CozeCreateConversationRequest request) {
        try {
            // 调用Coze API创建会话
            String url = cozeBaseUrl + "/v1/conversation/create";
            HttpEntity<CozeCreateConversationRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeCreateConversationResponse> response = restTemplate.postForEntity(url, entity, CozeCreateConversationResponse.class);
            CozeCreateConversationResponse cozeResponse = response.getBody();
            return cozeResponse;
        } catch (Exception e) {
            log.error("创建会话失败", e);
            throw new RuntimeException("创建会话失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse deleteConversation(String conversationId) {
        try {
            String url = cozeBaseUrl + "/v1/conversations/" + conversationId;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("删除会话失败", e);
            throw new RuntimeException("删除会话失败: " + e.getMessage());
        }
    }

    @Override
    public CozeUpdateConversationResponse updateConversationName(CozeUpdateConversationRequest request) {
        try {
            // 调用Coze API更新会话名称
            String url = cozeBaseUrl + "/v1/conversation/" + request.getConversationId() + "/update";
            HttpEntity<CozeUpdateConversationRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeUpdateConversationResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, CozeUpdateConversationResponse.class);
            CozeUpdateConversationResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，更新本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                CozeConversation conversation = cozeConversationService.getByCozeConversationIdAndMerchant(
                    request.getConversationId(), merchantId);
                
                if (conversation != null) {
                    conversation.setConversationName(request.getConversationName());
                    conversation.setUpdateTime(new Date());
                    cozeConversationService.updateById(conversation);
                    log.info("本地会话名称已更新: {}", conversation.getCozeConversationId());
                }
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("更新会话名称失败", e);
            throw new RuntimeException("更新会话名称失败: " + e.getMessage());
        }
    }

    @Override
    public CozeGetConversationListResponse getConversationList(CozeGetConversationListRequest request) {
        try {
            // 调用Coze API获取会话列表
            String url = cozeBaseUrl + "/v1/conversations?bot_id=" + request.getBotId() + 
                        "&page_num=" + request.getPageNum() + "&page_size=" + request.getPageSize();
            
            // 添加可选参数
            if (request.getSortOrder() != null) {
                url += "&sort_order=" + request.getSortOrder();
            }
            if (request.getConnectorId() != null) {
                url += "&connector_id=" + request.getConnectorId();
            }
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeGetConversationListResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeGetConversationListResponse.class);
            CozeGetConversationListResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，同步会话数据到统一聊天会话表
            if (cozeResponse != null && cozeResponse.getCode() == 0 && cozeResponse.getData() != null) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                for (CozeGetConversationListResponse.ConversationInfo conversationInfo : cozeResponse.getData().getConversations()) {
                    try {
                        // 检查统一聊天会话表中是否已存在该会话
                        com.zbkj.common.model.chat.UnifiedChatSession existingSession = 
                            unifiedChatService.getSessionByCozeConversationId(conversationInfo.getId());
                        
                        if (existingSession == null) {
                            // 创建新的统一会话记录
                            com.zbkj.common.model.chat.UnifiedChatSession session = new com.zbkj.common.model.chat.UnifiedChatSession();
                            session.setSessionId(unifiedChatService.generateSessionId());
                            session.setUserId(merchantId.longValue()); // 商户作为用户
                            session.setUserType("MERCHANT");
                            session.setMerId(merchantId.longValue());
                            session.setSessionType("AI");
                            session.setCurrentServiceType("AI");
                            session.setCozeBotId(request.getBotId());
                            session.setCozeConversationId(conversationInfo.getId());
                            session.setSessionTitle("Coze会话 " + conversationInfo.getId());
                            session.setStatus("ACTIVE");
                            session.setTotalMessages(0);
                            session.setPriority("NORMAL");
                            
                            // 设置时间
                            Date createTime = new Date(conversationInfo.getCreatedAt() * 1000);
                            session.setCreateTime(createTime);
                            session.setUpdateTime(createTime);
                            session.setLastMessageTime(createTime);
                            
                            // 设置元数据
                            Map<String, Object> metaData = new HashMap<>();
                            metaData.put("lastSectionId", conversationInfo.getLastSectionId());
                            metaData.put("syncedFromCoze", true);
                            metaData.put("cozeCreatedAt", conversationInfo.getCreatedAt());
                            session.setMetaData(com.alibaba.fastjson.JSON.toJSONString(metaData));
                            
                            unifiedChatService.saveSession(session);
                            log.info("已创建新的统一会话记录: cozeConversationId={}, sessionId={}", 
                                conversationInfo.getId(), session.getSessionId());
                        } else {
                            // 更新现有记录的元数据
                            Map<String, Object> metaData = new HashMap<>();
                            if (existingSession.getMetaData() != null) {
                                metaData = com.alibaba.fastjson.JSON.parseObject(existingSession.getMetaData(), Map.class);
                            }
                            metaData.put("lastSectionId", conversationInfo.getLastSectionId());
                            metaData.put("lastSyncTime", System.currentTimeMillis());
                            
                            existingSession.setMetaData(com.alibaba.fastjson.JSON.toJSONString(metaData));
                            existingSession.setUpdateTime(new Date());
                            
                            unifiedChatService.updateSession(existingSession);
                            log.debug("已更新统一会话记录: cozeConversationId={}, sessionId={}", 
                                conversationInfo.getId(), existingSession.getSessionId());
                        }
                    } catch (Exception e) {
                        log.error("同步会话到统一表失败: cozeConversationId={}, error={}", 
                            conversationInfo.getId(), e.getMessage(), e);
                    }
                }
                
                log.info("已同步 {} 个Coze会话到统一聊天会话表", cozeResponse.getData().getConversations().size());
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("查看会话列表失败", e);
            throw new RuntimeException("查看会话列表失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse clearConversationContext(String conversationId) {
        try {
            String url = cozeBaseUrl + "/v1/conversation/" + conversationId + "/clear_context";
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.postForEntity(url, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("清除会话上下文失败", e);
            throw new RuntimeException("清除会话上下文失败: " + e.getMessage());
        }
    }

    // =================================================================================
    // 消息管理相关接口
    // =================================================================================

    @Override
    public CozeCreateMessageResponse createMessage(CozeCreateMessageRequest request) {
        try {
            // 调用Coze API创建消息
            String url = cozeBaseUrl + "/v1/conversation/message/create?conversation_id=" + request.getConversationId();
            HttpEntity<CozeCreateMessageRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeCreateMessageResponse> response = restTemplate.postForEntity(url, entity, CozeCreateMessageResponse.class);
            CozeCreateMessageResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，保存到本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                try {
                    LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                    if (loginUser != null && loginUser.getUser() != null) {
                        Integer merchantId = loginUser.getUser().getMerId();
                        
                        CozeMessage message = new CozeMessage();
                        message.setMerId(merchantId);
                        message.setCozeMessageId(cozeResponse.getData().getId());
                        message.setCozeConversationId(cozeResponse.getData().getConversationId());
                        message.setCozeBotId(cozeResponse.getData().getBotId());
                        message.setMessageType(cozeResponse.getData().getType());
                        message.setContent(cozeResponse.getData().getContent());
                        message.setContentType(cozeResponse.getData().getContentType());
                        message.setStatus("completed"); // 默认状态
                        message.setCreateTime(new Date(cozeResponse.getData().getCreatedAt() * 1000));
                        message.setUpdateTime(new Date(cozeResponse.getData().getUpdatedAt() * 1000));
                        
                        cozeMessageService.save(message);
                        log.info("消息已保存到本地数据库: {}", message.getCozeMessageId());
                    } else {
                        log.warn("无法获取当前登录用户信息，跳过消息保存到本地数据库");
                    }
                } catch (Exception saveException) {
                    log.warn("保存消息到本地数据库失败，但Coze API调用成功: {}", saveException.getMessage());
                }
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("创建消息失败", e);
            throw new RuntimeException("创建消息失败: " + e.getMessage());
        }
    }

    @Override
    public CozeModifyMessageResponse modifyMessage(CozeModifyMessageRequest request) {
        try {
            // 调用Coze API修改消息
            String url = cozeBaseUrl + "/v1/conversation/message/modify?conversation_id=" + request.getConversationId() + "&message_id=" + request.getMessageId();
            HttpEntity<CozeModifyMessageRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeModifyMessageResponse> response = restTemplate.postForEntity(url, entity, CozeModifyMessageResponse.class);
            CozeModifyMessageResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，更新本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                CozeMessage message = cozeMessageService.getByCozeMessageIdAndMerchant(
                    request.getMessageId(), merchantId);
                
                if (message != null) {
                    if (request.getContent() != null) {
                        message.setContent(request.getContent());
                    }
                    if (request.getContentType() != null) {
                        message.setContentType(request.getContentType());
                    }
                    message.setUpdateTime(new Date());
                    cozeMessageService.updateById(message);
                    log.info("本地消息已更新: {}", message.getCozeMessageId());
                }
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("修改消息失败", e);
            throw new RuntimeException("修改消息失败: " + e.getMessage());
        }
    }

    @Override
    public CozeCreateMessageResponse deleteMessage(String conversationId, String messageId) {
        try {
            // 调用Coze API删除消息
            String url = cozeBaseUrl + "/v1/conversation/message/delete?conversation_id=" + conversationId + "&message_id=" + messageId;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeCreateMessageResponse> response = restTemplate.postForEntity(url, entity, CozeCreateMessageResponse.class);
            CozeCreateMessageResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，删除本地数据库记录
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                CozeMessage message = cozeMessageService.getByCozeMessageIdAndMerchant(messageId, merchantId);
                if (message != null) {
                    cozeMessageService.removeById(message.getId());
                    log.info("本地消息已删除: {}", messageId);
                }
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("删除消息失败", e);
            throw new RuntimeException("删除消息失败: " + e.getMessage());
        }
    }

    @Override
    public CozeGetMessageListResponse getMessageList(CozeGetMessageListRequest request) {
        try {
            // 调用Coze API获取消息列表
            String url = cozeBaseUrl + "/v1/conversation/message/list?conversation_id=" + request.getConversationId();
            
            // 创建请求体，不包含conversationId（因为它在查询参数中）
            CozeGetMessageListRequest bodyRequest = CozeGetMessageListRequest.builder()
                    .order(request.getOrder())
                    .chatId(request.getChatId())
                    .beforeId(request.getBeforeId())
                    .afterId(request.getAfterId())
                    .limit(request.getLimit())
                    .build();
            
            HttpEntity<CozeGetMessageListRequest> entity = new HttpEntity<>(bodyRequest, getHeaders());
            ResponseEntity<CozeGetMessageListResponse> response = restTemplate.postForEntity(url, entity, CozeGetMessageListResponse.class);
            CozeGetMessageListResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，同步消息数据到统一消息表
            if (cozeResponse != null && cozeResponse.getCode() == 0 && cozeResponse.getData() != null) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                // 根据Coze会话ID查找对应的统一会话
                com.zbkj.common.model.chat.UnifiedChatSession unifiedSession = 
                    unifiedChatService.getSessionByCozeConversationId(request.getConversationId());
                
                if (unifiedSession == null) {
                    log.warn("未找到对应的统一会话，跳过消息同步: cozeConversationId={}", request.getConversationId());
                } else {
                    for (CozeCreateMessageResponse.MessageData messageData : cozeResponse.getData()) {
                        try {
                            // 检查统一消息表中是否已存在该消息
                            com.zbkj.common.model.chat.UnifiedChatMessage existingMessage = 
                                unifiedChatService.getMessageByCozeMessageId(messageData.getId());
                            
                            if (existingMessage == null) {
                                // 创建新的统一消息记录
                                com.zbkj.common.model.chat.UnifiedChatMessage message = 
                                    new com.zbkj.common.model.chat.UnifiedChatMessage();
                                
                                // 基本信息
                                message.setMessageId(unifiedChatService.generateMessageId());
                                message.setSessionId(unifiedSession.getSessionId());
                                
                                // Coze相关字段
                                message.setCozeMessageId(messageData.getId());
                                message.setCozeChatId(messageData.getChatId());
                                message.setCozeCreatedAt(messageData.getCreatedAt());
                                message.setCozeUpdatedAt(messageData.getUpdatedAt());
                                
                                // 发送者信息
                                if ("user".equals(messageData.getRole())) {
                                    message.setSenderId(unifiedSession.getUserId());
                                    message.setSenderType("USER");
                                    message.setSenderName("用户");
                                } else if ("assistant".equals(messageData.getRole())) {
                                    message.setSenderId(null); // AI没有具体的发送者ID
                                    message.setSenderType("AI");
                                    message.setSenderName("AI助手");
                                } else {
                                    message.setSenderId(null);
                                    message.setSenderType("SYSTEM");
                                    message.setSenderName("系统");
                                }
                                
                                // 消息内容
                                message.setRole(messageData.getRole());
                                message.setMessageType(mapCozeMessageType(messageData.getType()));
                                message.setContent(messageData.getContent());
                                message.setContentType(mapCozeContentType(messageData.getContentType()));
                                message.setRawContent(com.alibaba.fastjson.JSON.toJSONString(messageData));
                                
                                // 消息状态（Coze消息默认为已发送状态）
                                message.setStatus("sent");
                                message.setIsRead(false);
                                message.setIsSystemMessage("system".equals(messageData.getRole()));
                                
                                // 时间信息
                                Date createTime = new Date(messageData.getCreatedAt() * 1000);
                                message.setCreateTime(createTime);
                                message.setUpdateTime(new Date(messageData.getUpdatedAt() * 1000));
                                
                                // 元数据
                                Map<String, Object> metaData = new HashMap<>();
                                metaData.put("syncedFromCoze", true);
                                metaData.put("cozeMessageType", messageData.getType());
                                metaData.put("cozeContentType", messageData.getContentType());
                                message.setMetaData(com.alibaba.fastjson.JSON.toJSONString(metaData));
                                
                                unifiedChatService.saveMessage(message);
                                
                                // 更新会话的最后消息信息
                                unifiedChatService.updateSessionLastMessage(unifiedSession, message);
                                
                                log.info("已创建新的统一消息记录: cozeMessageId={}, messageId={}", 
                                    messageData.getId(), message.getMessageId());
                            } else {
                                // 更新现有记录
                                existingMessage.setContent(messageData.getContent());
                                existingMessage.setContentType(mapCozeContentType(messageData.getContentType()));
                                existingMessage.setStatus("sent"); // Coze消息默认为已发送状态
                                existingMessage.setUpdateTime(new Date(messageData.getUpdatedAt() * 1000));
                                existingMessage.setCozeUpdatedAt(messageData.getUpdatedAt());
                                
                                // 更新元数据
                                Map<String, Object> metaData = new HashMap<>();
                                if (existingMessage.getMetaData() != null) {
                                    metaData = com.alibaba.fastjson.JSON.parseObject(existingMessage.getMetaData(), Map.class);
                                }
                                metaData.put("lastSyncTime", System.currentTimeMillis());
                                existingMessage.setMetaData(com.alibaba.fastjson.JSON.toJSONString(metaData));
                                
                                unifiedChatService.updateMessage(existingMessage);
                                log.debug("已更新统一消息记录: cozeMessageId={}, messageId={}", 
                                    messageData.getId(), existingMessage.getMessageId());
                            }
                        } catch (Exception e) {
                            log.error("同步消息到统一表失败: cozeMessageId={}, error={}", 
                                messageData.getId(), e.getMessage(), e);
                        }
                    }
                }
                
                log.info("已同步 {} 个Coze消息到统一消息表", cozeResponse.getData().size());
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("查看消息列表失败", e);
            throw new RuntimeException("查看消息列表失败: " + e.getMessage());
        }
    }

    @Override
    public CozeGetMessageDetailResponse getMessageDetail(String conversationId, String messageId) {
        try {
            // 调用Coze API获取消息详情
            String url = cozeBaseUrl + "/v1/conversation/message/retrieve?conversation_id=" + conversationId + "&message_id=" + messageId;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeGetMessageDetailResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeGetMessageDetailResponse.class);
            CozeGetMessageDetailResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，同步消息数据到本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0 && cozeResponse.getData() != null) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                CozeMessage existingMessage = cozeMessageService.getByCozeMessageIdAndMerchant(messageId, merchantId);
                if (existingMessage == null) {
                    // 创建新的消息记录
                    CozeMessage message = new CozeMessage();
                    message.setMerId(merchantId);
                    message.setCozeMessageId(cozeResponse.getData().getId());
                    message.setCozeConversationId(cozeResponse.getData().getConversationId());
                    message.setCozeBotId(cozeResponse.getData().getBotId());
                    message.setMessageType(cozeResponse.getData().getType());
                    message.setContent(cozeResponse.getData().getContent());
                    message.setContentType(cozeResponse.getData().getContentType());
                    message.setStatus("completed"); // 默认状态
                    message.setCreateTime(new Date(cozeResponse.getData().getCreatedAt() * 1000));
                    message.setUpdateTime(new Date(cozeResponse.getData().getUpdatedAt() * 1000));
                    
                    cozeMessageService.save(message);
                    log.info("消息已保存到本地数据库: {}", message.getCozeMessageId());
                }
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("查看消息详情失败", e);
            throw new RuntimeException("查看消息详情失败: " + e.getMessage());
        }
    }

    // =================================================================================
    // 对话相关接口
    // =================================================================================

    @Override
    public Object startChat(CozeStartChatRequest request) {
        try {
            String url = cozeBaseUrl + "/v3/chat";
            if (request.getConversationId() != null) {
                url += "?conversation_id=" + request.getConversationId();
            }
            HttpEntity<CozeStartChatRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("发起对话失败", e);
            throw new RuntimeException("发起对话失败: " + e.getMessage());
        }
    }


    @Override
    public Object startChat(Map<String, Object> requestParams) {
        try {
            String url = cozeBaseUrl + "/v3/chat";
            
            // 如果参数中包含conversation_id，添加到URL查询参数中
            if (requestParams.containsKey("conversation_id")) {
                url += "?conversation_id=" + requestParams.get("conversation_id");
            }
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestParams, getHeaders());
            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
            
            log.info("Coze API调用成功，返回响应: {}", JSONUtil.toJsonStr(response.getBody()));
            return response.getBody();
            
        } catch (Exception e) {
            log.error("发起对话失败，请求参数: {}, 错误: {}", 
                     JSONUtil.toJsonStr(requestParams), e.getMessage(), e);
            throw new RuntimeException("发起对话失败: " + e.getMessage());
        }
    }

    @Override
    public com.zbkj.common.model.coze.stream.CozeStreamResponse startStreamChat(Map<String, Object> requestParams) {
        try {
            log.info("开始发起Coze流式对话，参数: {}", JSONUtil.toJsonStr(requestParams));
            
            // 确保开启流式响应
            requestParams.put("stream", true);
            
            // 调用流式客户端
            com.zbkj.common.model.coze.stream.CozeStreamResponse streamResponse = 
                cozeStreamClient.startStreamChat(requestParams);
            
            if (streamResponse.isFailed()) {
                throw new RuntimeException("流式对话失败: " + streamResponse.getErrorMessage());
            }
            
            log.info("Coze流式对话完成，状态: {}", streamResponse.getFinalStatus());
            return streamResponse;
            
        } catch (Exception e) {
            log.error("发起流式对话失败，请求参数: {}, 错误: {}", 
                     JSONUtil.toJsonStr(requestParams), e.getMessage(), e);
            throw new RuntimeException("发起流式对话失败: " + e.getMessage());
        }
    }

    @Override
    public Object cancelChat(CozeCancelChatRequest request) {
        try {
            String url = cozeBaseUrl + "/v3/chat/cancel";
            HttpEntity<CozeCancelChatRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("取消对话失败", e);
            throw new RuntimeException("取消对话失败: " + e.getMessage());
        }
    }

    @Override
    public Object getChatDetail(String conversationId, String chatId) {
        try {
            String url = cozeBaseUrl + "/v3/chat/retrieve?conversation_id=" + conversationId + "&chat_id=" + chatId;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("查看对话详情失败", e);
            throw new RuntimeException("查看对话详情失败: " + e.getMessage());
        }
    }

    @Override
    public Object getChatMessages(String conversationId, String chatId, Integer pageNum, Integer pageSize) {
        try {
            String url = cozeBaseUrl + "/v3/chat/message/list?conversation_id=" + conversationId + "&chat_id=" + chatId + "&page_num=" + pageNum + "&page_size=" + pageSize;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("查看对话消息详情失败", e);
            throw new RuntimeException("查看对话消息详情失败: " + e.getMessage());
        }
    }

    // =================================================================================
    // 知识库管理相关接口
    // =================================================================================

    @Override
    public CozeCreateKnowledgeResponse createKnowledge(CozeCreateKnowledgeRequest request) {
        try {
            // 调用Coze API创建知识库
            String url = cozeBaseUrl + "/v1/datasets";
            HttpEntity<CozeCreateKnowledgeRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeCreateKnowledgeResponse> response = restTemplate.postForEntity(url, entity, CozeCreateKnowledgeResponse.class);
            CozeCreateKnowledgeResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，保存到本地数据库
            if (cozeResponse != null) {
                try {
                    LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                    Integer merchantId = loginUser.getUser().getMerId();
                    
                    CozeKnowledge cozeKnowledge = new CozeKnowledge();
                    cozeKnowledge.setMerchantId(merchantId);
                    // 从响应中获取知识库ID
                    String datasetId = cozeResponse.getData() != null ? cozeResponse.getData().getDatasetId() : null;
                    if (datasetId != null) {
                        cozeKnowledge.setCozeKnowledgeId(datasetId);
                    } else {
                        log.warn("创建知识库成功，但无法从响应中获取dataset_id");
                        return cozeResponse; // 返回响应，不保存到数据库
                    }
                    cozeKnowledge.setName(request.getName());
                    cozeKnowledge.setDescription(request.getDescription());
                    cozeKnowledge.setSpaceId(request.getSpaceId());
                    cozeKnowledge.setStatus(1);
                    
                    cozeKnowledgeService.saveOrUpdateKnowledge(cozeKnowledge);
                    log.info("知识库保存到数据库成功, Knowledge ID: {}", cozeKnowledge.getCozeKnowledgeId());
                } catch (Exception e) {
                    log.warn("保存知识库到数据库失败，但Coze API调用成功", e);
                }
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("创建知识库失败", e);
            throw new RuntimeException("创建知识库失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse updateKnowledge(CozeUpdateKnowledgeRequest request) {
        try {
            String url = cozeBaseUrl + "/v1/datasets/" + request.getDatasetId();
            HttpEntity<CozeUpdateKnowledgeRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("修改知识库信息失败", e);
            throw new RuntimeException("修改知识库信息失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse deleteKnowledge(String datasetId) {
        try {
            String url = cozeBaseUrl + "/v1/datasets/" + datasetId;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("删除知识库失败", e);
            throw new RuntimeException("删除知识库失败: " + e.getMessage());
        }
    }


    @Override
    public CozeBaseResponse updateKnowledgeFile(CozeUpdateKnowledgeFileRequest request) {
        try {
            String url = cozeBaseUrl + "/open_api/knowledge/document/update";
            
            HttpHeaders headers = getHeaders();
            headers.set("Agw-Js-Conv", "str");
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<CozeUpdateKnowledgeFileRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("调用Coze API修改知识库文件，URL: {}, 请求参数: {}", url, request);
            
            ResponseEntity<CozeBaseResponse> response = restTemplate.postForEntity(url, entity, CozeBaseResponse.class);
            
            CozeBaseResponse result = response.getBody();
            if (result != null) {
                log.info("修改知识库文件成功，返回数据: code={}, msg={}", result.getCode(), result.getMsg());
            }
            
            return result;
        } catch (Exception e) {
            log.error("修改知识库文件失败，请求参数: {}, 错误信息: {}", request, e.getMessage(), e);
            throw new RuntimeException("修改知识库文件失败: " + e.getMessage());
        }
    }

    @Override
    public CozeCreateKnowledgeFileResponse createKnowledgeFile(CozeCreateKnowledgeFileRequest request) {
        return createKnowledgeFile(request, null);
    }

    /**
     * 创建知识库文件（支持原始文件信息）
     */
    @Override
    public CozeCreateKnowledgeFileResponse createKnowledgeFile(CozeCreateKnowledgeFileRequest request, Map<String, Long> originalFileSizes) {
        return createKnowledgeFile(request, originalFileSizes, null);
    }

    @Override
    public CozeCreateKnowledgeFileResponse createKnowledgeFile(CozeCreateKnowledgeFileRequest request, Map<String, Long> originalFileSizes, Map<String, String> fileDownloadUrls) {
        try {
            String url = cozeBaseUrl + "/open_api/knowledge/document/create";
            HttpHeaders headers = getHeaders();
            headers.set("Agw-Js-Conv", "str");
            HttpEntity<CozeCreateKnowledgeFileRequest> entity = new HttpEntity<>(request, headers);
            log.info("调用Coze API创建文件，URL: {}, 知识库ID: {}", url, request.getDatasetId());
            
            ResponseEntity<CozeCreateKnowledgeFileResponse> response = restTemplate.postForEntity(url, entity, CozeCreateKnowledgeFileResponse.class);
            
            CozeCreateKnowledgeFileResponse cozeResponse = response.getBody();
            log.info("Coze API响应: code={}, msg={}, documentInfos={}", 
                cozeResponse != null ? cozeResponse.getCode() : null,
                cozeResponse != null ? cozeResponse.getMsg() : null,
                cozeResponse != null && cozeResponse.getDocumentInfos() != null ? cozeResponse.getDocumentInfos().size() : 0);
            
            // 如果API调用成功，保存文件信息到本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0 && cozeResponse.getDocumentInfos() != null) {
                //判断是否为小程序用户上传
                Integer userId = userService.getUserId();
                if(userId != null&& userId > 0){
                    log.info("开始保存{}个文件到数据库，商户ID: {}", cozeResponse.getDocumentInfos().size());

                    for (CozeCreateKnowledgeFileResponse.DocumentInfo docInfo : cozeResponse.getDocumentInfos()) {
                        log.info("处理文件: ID={}, Name={}, Size={}, Type={}, Status={}",
                                docInfo.getDocumentId(), docInfo.getName(), docInfo.getSize(), docInfo.getType(), docInfo.getStatus());

                        CozeKnowledgeFile cozeFile = new CozeKnowledgeFile();
                        cozeFile.setMerchantId(0);
                        cozeFile.setCozeKnowledgeId(request.getDatasetId());
                        cozeFile.setCozeFileId(docInfo.getDocumentId());
                        cozeFile.setFileName(docInfo.getName());

                        // 设置文件大小和类型
                        Long fileSize = docInfo.getSize();
                        String fileType = docInfo.getType();

                        // 优先使用原始文件大小
                        if (originalFileSizes != null && originalFileSizes.containsKey(docInfo.getName())) {
                            fileSize = originalFileSizes.get(docInfo.getName());
                            log.info("使用原始文件大小：{} bytes，文件：{}", fileSize, docInfo.getName());
                        } else if (fileSize == null || fileSize == 0) {
                            log.warn("Coze API返回的文件大小为空或0，且无原始文件大小，文件：{}", docInfo.getName());
                            fileSize = 0L;
                        }

                        if (fileType == null || fileType.isEmpty()) {
                            log.warn("Coze API返回的文件类型为空，文件：{}，尝试从文件名推断", docInfo.getName());
                            // 从文件名推断类型
                            fileType = getFileExtensionFromName(docInfo.getName());
                        }

                        cozeFile.setFileSize(fileSize);
                        cozeFile.setFileType(fileType);
                        cozeFile.setCharCount(docInfo.getCharCount());
                        cozeFile.setSliceCount(docInfo.getSliceCount());
                        cozeFile.setHitCount(docInfo.getHitCount());

                        // 设置文件下载URL（阿里云对象存储URL）
                        String downloadUrl = "";
                        if (fileDownloadUrls != null && fileDownloadUrls.containsKey(docInfo.getName())) {
                            downloadUrl = fileDownloadUrls.get(docInfo.getName());
                            log.info("设置文件下载URL：{}，文件：{}", downloadUrl, docInfo.getName());
                        }
                        cozeFile.setCozeFileUrl(downloadUrl);
                        cozeFile.setUploadStatus(1); // 上传成功
                        cozeFile.setProcessStatus(docInfo.getStatus()); // Coze处理状态：9（处理中），0（成功），7（失败）
                        cozeFile.setProcessProgress(100); // 已上传完成
                        cozeFile.setStatus(1);
                        cozeFile.setCreateTime(new Date());
                        cozeFile.setUpdateTime(new Date());

                        try {
                            cozeKnowledgeFileService.save(cozeFile);
                            log.info("知识库文件保存到数据库成功, File ID: {}, 本地ID: {}, 大小: {} bytes, 类型: {}",
                                    docInfo.getDocumentId(), cozeFile.getId(), fileSize, fileType);
                        } catch (Exception saveEx) {
                            log.error("保存文件到数据库失败, File ID: {}", docInfo.getDocumentId(), saveEx);
                            throw saveEx;
                        }
                    }
                }else{
                    log.info("开始保存{}个文件到数据库，商户ID: {}", cozeResponse.getDocumentInfos().size(), 0);

                    for (CozeCreateKnowledgeFileResponse.DocumentInfo docInfo : cozeResponse.getDocumentInfos()) {
                        log.info("处理文件: ID={}, Name={}, Size={}, Type={}, Status={}",
                                docInfo.getDocumentId(), docInfo.getName(), docInfo.getSize(), docInfo.getType(), docInfo.getStatus());

                        CozeKnowledgeFile cozeFile = new CozeKnowledgeFile();
                        cozeFile.setMerchantId(0);
                        cozeFile.setCozeKnowledgeId(request.getDatasetId());
                        cozeFile.setCozeFileId(docInfo.getDocumentId());
                        cozeFile.setFileName(docInfo.getName());

                        // 设置文件大小和类型
                        Long fileSize = docInfo.getSize();
                        String fileType = docInfo.getType();

                        // 优先使用原始文件大小
                        if (originalFileSizes != null && originalFileSizes.containsKey(docInfo.getName())) {
                            fileSize = originalFileSizes.get(docInfo.getName());
                            log.info("使用原始文件大小：{} bytes，文件：{}", fileSize, docInfo.getName());
                        } else if (fileSize == null || fileSize == 0) {
                            log.warn("Coze API返回的文件大小为空或0，且无原始文件大小，文件：{}", docInfo.getName());
                            fileSize = 0L;
                        }

                        if (fileType == null || fileType.isEmpty()) {
                            log.warn("Coze API返回的文件类型为空，文件：{}，尝试从文件名推断", docInfo.getName());
                            // 从文件名推断类型
                            fileType = getFileExtensionFromName(docInfo.getName());
                        }

                        cozeFile.setFileSize(fileSize);
                        cozeFile.setFileType(fileType);
                        cozeFile.setCharCount(docInfo.getCharCount());
                        cozeFile.setSliceCount(docInfo.getSliceCount());
                        cozeFile.setHitCount(docInfo.getHitCount());

                        // 设置文件下载URL（阿里云对象存储URL）
                        String downloadUrl = "";
                        if (fileDownloadUrls != null && fileDownloadUrls.containsKey(docInfo.getName())) {
                            downloadUrl = fileDownloadUrls.get(docInfo.getName());
                            log.info("设置文件下载URL：{}，文件：{}", downloadUrl, docInfo.getName());
                        }
                        cozeFile.setCozeFileUrl(downloadUrl);
                        cozeFile.setUploadStatus(1); // 上传成功
                        cozeFile.setProcessStatus(docInfo.getStatus()); // Coze处理状态：9（处理中），0（成功），7（失败）
                        cozeFile.setProcessProgress(100); // 已上传完成
                        cozeFile.setStatus(1);
                        cozeFile.setCreateTime(new Date());
                        cozeFile.setUpdateTime(new Date());

                        try {
                            cozeKnowledgeFileService.save(cozeFile);
                            log.info("知识库文件保存到数据库成功, File ID: {}, 本地ID: {}, 大小: {} bytes, 类型: {}",
                                    docInfo.getDocumentId(), cozeFile.getId(), fileSize, fileType);
                        } catch (Exception saveEx) {
                            log.error("保存文件到数据库失败, File ID: {}", docInfo.getDocumentId(), saveEx);
                            throw saveEx;
                        }
                    }
                }
            } else {
                log.warn("Coze API调用失败或返回数据异常: response={}", cozeResponse);
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("创建知识库文件失败", e);
            throw new RuntimeException("创建知识库文件失败: " + e.getMessage());
        }
    }

    /**
     * 从文件名获取文件扩展名
     */
    private String getFileExtensionFromName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public CozeBaseResponse deleteKnowledgeFile(List<String> documentIds) {
        try {
            String url = cozeBaseUrl + "/open_api/knowledge/document/delete";
            HttpHeaders headers = getHeaders();
            headers.set("Agw-Js-Conv", "str");
            Object requestBody = new Object() {
                public List<String> document_ids = documentIds;
            };
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<CozeBaseResponse> response = restTemplate.postForEntity(url, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("删除知识库文件失败", e);
            throw new RuntimeException("删除知识库文件失败: " + e.getMessage());
        }
    }

    // =================================================================================
    // 工作流相关接口
    // =================================================================================

    @Override
    public CozeExecuteWorkflowResponse runWorkflow(CozeExecuteWorkflowRequest request) {
        try {
            String url = cozeBaseUrl + "/v1/workflow/run";
            HttpEntity<CozeExecuteWorkflowRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeExecuteWorkflowResponse> response = restTemplate.postForEntity(url, entity, CozeExecuteWorkflowResponse.class);
            
            CozeExecuteWorkflowResponse cozeResponse = response.getBody();
            
            // 如果执行成功，可以记录执行历史到数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                // 这里可以创建工作流执行记录表来记录执行历史
                log.info("工作流执行成功, Workflow ID: {}, Execute ID: {}", request.getWorkflowId(), cozeResponse.getExecuteId());
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("执行工作流失败", e);
            throw new RuntimeException("执行工作流失败: " + e.getMessage());
        }
    }

    @Override
    public CozeExecuteWorkflowResponse runChatWorkflow(CozeExecuteWorkflowRequest request) {
        try {
            String url = cozeBaseUrl + "/v1/workflow/run";  // 对话流也使用同一个执行接口，通过workflowMode区分
            HttpEntity<CozeExecuteWorkflowRequest> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeExecuteWorkflowResponse> response = restTemplate.postForEntity(url, entity, CozeExecuteWorkflowResponse.class);
            
            CozeExecuteWorkflowResponse cozeResponse = response.getBody();
            
            // 如果执行成功，记录执行历史
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                log.info("对话流执行成功, Workflow ID: {}, Execute ID: {}", request.getWorkflowId(), cozeResponse.getExecuteId());
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("执行对话流失败", e);
            throw new RuntimeException("执行对话流失败: " + e.getMessage());
        }
    }

    @Override
    public CozeGetWorkflowListResponse getWorkflowList(CozeGetWorkflowListRequest request) {
        try {
            String url = cozeBaseUrl + "/v1/workflows?workspace_id=" + request.getWorkspaceId() 
                    + "&page_num=" + request.getPageNum() 
                    + "&page_size=10";
            
            if (request.getWorkflowMode() != null) {
                url += "&workflow_mode=" + request.getWorkflowMode();
            }
            if (request.getAppId() != null) {
                url += "&app_id=" + request.getAppId();
            }
            if (request.getPublishStatus() != null) {
                url += "&publish_status=" + request.getPublishStatus();
            }

            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeGetWorkflowListResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeGetWorkflowListResponse.class);
            
            CozeGetWorkflowListResponse cozeResponse = response.getBody();
            
            // 如果API调用成功，同步工作流信息到本地数据库
            if (cozeResponse != null && cozeResponse.getCode() == 0 && cozeResponse.getData() != null 
                    && cozeResponse.getData().getItems() != null) {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                for (CozeGetWorkflowListResponse.WorkflowBasic workflowInfo : cozeResponse.getData().getItems()) {
                    CozeWorkflow cozeWorkflow = new CozeWorkflow();
                    cozeWorkflow.setMerchantId(merchantId);
                    cozeWorkflow.setCozeWorkflowId(workflowInfo.getWorkflowId());
                    cozeWorkflow.setName(workflowInfo.getWorkflowName());
                    cozeWorkflow.setDescription(workflowInfo.getDescription());
                    cozeWorkflow.setIconUrl(workflowInfo.getIconUrl());
                    cozeWorkflow.setWorkflowMode(request.getWorkflowMode() != null ? request.getWorkflowMode() : "workflow");
                    cozeWorkflow.setAppId(workflowInfo.getAppId());
                    cozeWorkflow.setPublishStatus(request.getPublishStatus() != null && request.getPublishStatus().equals("published_online") ? 1 : 0);
                    cozeWorkflow.setCreatorId(workflowInfo.getCreator() != null ? workflowInfo.getCreator().getId() : null);
                    cozeWorkflow.setCreatorName(workflowInfo.getCreator() != null ? workflowInfo.getCreator().getName() : null);
                    cozeWorkflow.setStatus(1);
                    
                    // 设置创建和更新时间
                    try {
                        if (workflowInfo.getCreatedAt() != null) {
                            cozeWorkflow.setCreateTime(new Date(Long.parseLong(workflowInfo.getCreatedAt()) * 1000));
                        }
                        if (workflowInfo.getUpdatedAt() != null) {
                            cozeWorkflow.setUpdateTime(new Date(Long.parseLong(workflowInfo.getUpdatedAt()) * 1000));
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析工作流时间失败", e);
                        cozeWorkflow.setCreateTime(new Date());
                        cozeWorkflow.setUpdateTime(new Date());
                    }
                    
                    cozeWorkflowService.saveOrUpdateWorkflow(cozeWorkflow);
                }
                
                log.info("工作流信息同步到数据库成功，共{}个工作流", cozeResponse.getData().getItems().size());
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("查询工作流列表失败", e);
            throw new RuntimeException("查询工作流列表失败: " + e.getMessage());
        }
    }

    // =================================================================================
    // 其他相关接口
    // =================================================================================

    @Override
    public CozeGetSpaceListResponse getSpaceList(CozeGetSpaceListRequest request) {
        try {
            StringBuilder urlBuilder = new StringBuilder(cozeBaseUrl + "/v1/workspaces");
            
            // 构建查询参数
            String separator = "?";
            if (request.getPageNum() != null) {
                urlBuilder.append(separator).append("page_num=").append(request.getPageNum());
                separator = "&";
            }
            if (request.getPageSize() != null) {
                urlBuilder.append(separator).append("page_size=").append(request.getPageSize());
                separator = "&";
            }
            if (request.getEnterpriseId() != null) {
                urlBuilder.append(separator).append("enterprise_id=").append(request.getEnterpriseId());
                separator = "&";
            }
            if (request.getUserId() != null) {
                urlBuilder.append(separator).append("user_id=").append(request.getUserId());
                separator = "&";
            }
            if (request.getCozeAccountId() != null) {
                urlBuilder.append(separator).append("coze_account_id=").append(request.getCozeAccountId());
            }
            
            String url = urlBuilder.toString();
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeGetSpaceListResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeGetSpaceListResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("查看空间列表失败", e);
            throw new RuntimeException("查看空间列表失败: " + e.getMessage());
        }
    }

    @Override
    public CozeGetSpaceMembersResponse getSpaceMembers(CozeGetSpaceMembersRequest request) {
        try {
            StringBuilder urlBuilder = new StringBuilder(cozeBaseUrl + "/v1/workspaces/" + request.getWorkspaceId() + "/members");
            
            // 构建查询参数
            String separator = "?";
            if (request.getPageNum() != null) {
                urlBuilder.append(separator).append("page_num=").append(request.getPageNum());
                separator = "&";
            }
            if (request.getPageSize() != null) {
                urlBuilder.append(separator).append("page_size=").append(request.getPageSize());
            }
            
            String url = urlBuilder.toString();
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeGetSpaceMembersResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeGetSpaceMembersResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("获取空间成员列表失败", e);
            throw new RuntimeException("获取空间成员列表失败: " + e.getMessage());
        }
    }

    @Override
    public Object getAppList(String spaceId, Integer pageNum, Integer pageSize) {
        try {
            String url = cozeBaseUrl + "/v1/apps?space_id=" + spaceId + "&page_num=" + pageNum + "&page_size=" + pageSize;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("查看应用列表失败", e);
            throw new RuntimeException("查看应用列表失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse addChannel(Object request) {
        try {
            String url = cozeBaseUrl + "/v1/connectors";
            HttpEntity<Object> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.postForEntity(url, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("添加发布渠道失败", e);
            throw new RuntimeException("添加发布渠道失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBaseResponse updateAuditResult(Object request) {
        try {
            String url = cozeBaseUrl + "/v1/audit/result/update";
            HttpEntity<Object> entity = new HttpEntity<>(request, getHeaders());
            ResponseEntity<CozeBaseResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, CozeBaseResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("更新审核结果失败", e);
            throw new RuntimeException("更新审核结果失败: " + e.getMessage());
        }
    }

    @Override
    public CozeBotListResponse getplatBotList(String spaceId, Integer pageNum, Integer pageSize) {
        try {
            String url = cozeBaseUrl + "/v1/bots?workspace_id=" + spaceId + "&page_num=" + pageNum + "&page_size=" + pageSize;
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeBotListResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeBotListResponse.class);

            CozeBotListResponse botListResponse = response.getBody();

            // 获取当前用户的平台ID
            try {
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                if (loginUser != null && loginUser.getUser() != null) {
                    Integer merchantId = loginUser.getUser().getMerId();
                    // 同步数据到本地数据库
                    syncBotListToLocal(botListResponse, merchantId, spaceId);
                }
            } catch (Exception syncException) {
                // 同步失败不影响API返回，只记录日志
                log.warn("同步智能体数据到本地失败: {}", syncException.getMessage());
            }

            return botListResponse;
        } catch (Exception e) {
            log.error("查看智能体列表失败", e);
            throw new RuntimeException("查看智能体列表失败: " + e.getMessage());
        }

    }

    @Override
    public CozeKnowledgeListResponse getKnowledgeListTyped(String spaceId, String name, Integer formatType, Integer pageNum, Integer pageSize) {
        try {
            StringBuilder urlBuilder = new StringBuilder(cozeBaseUrl + "/v1/datasets?space_id=" + spaceId);
            urlBuilder.append("&page_num=").append(pageNum);
            urlBuilder.append("&page_size=").append(pageSize);
            
            if (name != null && !name.trim().isEmpty()) {
                urlBuilder.append("&name=").append(name);
            }
            if (formatType != null) {
                urlBuilder.append("&format_type=").append(formatType);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeKnowledgeListResponse> response = restTemplate.exchange(
                urlBuilder.toString(), HttpMethod.GET, entity, CozeKnowledgeListResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("查看知识库列表失败", e);
            throw new RuntimeException("查看知识库列表失败: " + e.getMessage());
        }
    }

    @Override
    public CozeKnowledgeFileListResponse getKnowledgeFileListTyped(String datasetId, Integer pageNum, Integer pageSize) {
        try {
            String url = cozeBaseUrl + "/open_api/knowledge/document/list";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dataset_id", datasetId);
            requestBody.put("page", pageNum != null ? pageNum : 1);
            requestBody.put("size", pageSize != null ? pageSize : 20);
            
            HttpHeaders headers = getHeaders();
            headers.set("Agw-Js-Conv", "str");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("调用Coze API获取知识库文件列表，URL: {}, 请求参数: {}", url, requestBody);
            
            ResponseEntity<CozeKnowledgeFileListResponse> response = restTemplate.postForEntity(
                url, entity, CozeKnowledgeFileListResponse.class);
            
            CozeKnowledgeFileListResponse result = response.getBody();
            if (result != null) {
                log.info("成功获取知识库文件列表，返回数据: code={}, msg={}, total={}, 文件数量={}", 
                    result.getCode(), result.getMsg(), result.getTotal(), 
                    result.getDocumentInfos() != null ? result.getDocumentInfos().size() : 0);
            }
            
            return result;
        } catch (Exception e) {
            log.error("查看知识库文件列表失败，datasetId: {}, pageNum: {}, pageSize: {}, 错误信息: {}", 
                datasetId, pageNum, pageSize, e.getMessage(), e);
            throw new RuntimeException("查看知识库文件列表失败: " + e.getMessage());
        }
    }

    @Override
    public CozeKnowledgeFileProgressResponse getKnowledgeFileProgressBatch(String datasetId, List<String> documentIds) {
        try {
            String url = cozeBaseUrl + "/v1/datasets/" + datasetId + "/process";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("document_ids", documentIds);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, getHeaders());
            ResponseEntity<CozeKnowledgeFileProgressResponse> response = restTemplate.postForEntity(
                url, entity, CozeKnowledgeFileProgressResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            log.error("获取知识库文件上传进度失败", e);
            throw new RuntimeException("获取知识库文件上传进度失败: " + e.getMessage());
        }
    }

    @Override
    public CozeGetSpaceMembersResponse getSpaceMembersTyped(String workspaceId, Integer pageNum, Integer pageSize) {
        try {
            String url = cozeBaseUrl + "/v1/workspaces/" + workspaceId + "/members";
            
            Map<String, Object> params = new HashMap<>();
            params.put("page_num", pageNum);
            params.put("page_size", pageSize);
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            params.forEach(builder::queryParam);
            
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<CozeGetSpaceMembersResponse> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, CozeGetSpaceMembersResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            log.error("获取空间成员列表失败", e);
            throw new RuntimeException("获取空间成员列表失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 映射Coze消息类型到统一消息类型
     */
    private String mapCozeMessageType(String cozeType) {
        if (cozeType == null) return "text";
        
        switch (cozeType.toLowerCase()) {
            case "question":
            case "answer":
            case "text":
                return "text";
            case "function_call":
                return "function_call";
            case "tool_output":
                return "tool_response";
            case "image":
                return "image";
            case "file":
                return "file";
            case "audio":
                return "audio";
            case "video":
                return "video";
            default:
                return "text";
        }
    }

    /**
     * 映射Coze内容类型到统一内容类型
     */
    private String mapCozeContentType(String cozeContentType) {
        if (cozeContentType == null) return "text";
        
        switch (cozeContentType.toLowerCase()) {
            case "text":
                return "text";
            case "object_string":
                return "object_string";
            case "card":
                return "card";
            case "audio":
                return "audio";
            case "markdown":
                return "markdown";
            case "html":
                return "html";
            default:
                return "text";
        }
    }

    @Override
    public CozeUploadFileResponse uploadFile(MultipartFile file) {
        try {
            // 构建multipart/form-data请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 将MultipartFile转换为ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            
            body.add("file", resource);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String accessToken = cozeJwtService.getAccessToken();
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 调用Coze API上传文件
            String url = cozeBaseUrl + "/v1/files/upload";
            ResponseEntity<CozeUploadFileResponse> response = restTemplate.postForEntity(url, requestEntity, CozeUploadFileResponse.class);
            
            CozeUploadFileResponse cozeResponse = response.getBody();
            log.info("文件上传到Coze成功: {}", cozeResponse);
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("上传文件到Coze失败", e);
            throw new RuntimeException("上传文件到Coze失败: " + e.getMessage());
        }
    }

    // =================================================================================
    // 语音通话相关接口实现
    // =================================================================================

    @Override
    public CozeCreateRoomResponse createRoom(CozeCreateRoomRequest request) {
        try {
            log.info("开始创建Coze语音房间，智能体ID: {}, 会话ID: {}, 音色ID: {}", 
                    request.getBotId(), request.getConversationId(), request.getVoiceId());
            
            String url = cozeBaseUrl + "/v1/audio/rooms";
            HttpEntity<CozeCreateRoomRequest> entity = new HttpEntity<>(request, getHeaders());
            
            ResponseEntity<CozeCreateRoomResponse> response = restTemplate.postForEntity(url, entity, CozeCreateRoomResponse.class);
            CozeCreateRoomResponse cozeResponse = response.getBody();
            
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                String originalAppId = cozeResponse.getData().getAppId();
                
                // 添加Coze Access Token到响应中，用于Realtime WebSocket连接
                String accessToken = cozeJwtService.getAccessToken();
                cozeResponse.getData().setAccessToken(accessToken);
                
                // 如果启用了AppId映射修正，则使用配置的VolcEngine RTC AppId
                if (enableAppIdMapping) {
                    log.info("Coze语音房间创建成功，房间ID: {}, 原始应用ID: {}, 修正应用ID: {}, RTC Token: {}, Access Token: {}, 用户ID: {}", 
                            cozeResponse.getData().getRoomId(), 
                            originalAppId,
                            rtcAppId,
                            cozeResponse.getData().getToken().substring(0, 20) + "...",
                            accessToken.substring(0, 20) + "...",
                            cozeResponse.getData().getUid());
                    
                    // 将返回的AppId替换为正确的VolcEngine RTC AppId
                    cozeResponse.getData().setAppId(rtcAppId);
                    
                    log.info("AppId已修正为正确的VolcEngine RTC AppId: {}", rtcAppId);
                } else {
                    log.info("Coze语音房间创建成功，房间ID: {}, 应用ID: {}, RTC Token: {}, Access Token: {}, 用户ID: {}", 
                            cozeResponse.getData().getRoomId(), 
                            originalAppId,
                            cozeResponse.getData().getToken().substring(0, 20) + "...",
                            accessToken.substring(0, 20) + "...",
                            cozeResponse.getData().getUid());
                }
            } else {
                log.error("Coze语音房间创建失败，响应: {}", cozeResponse);
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("创建Coze语音房间失败，智能体ID: {}, 错误: {}", request.getBotId(), e.getMessage(), e);
            throw new RuntimeException("创建语音房间失败: " + e.getMessage());
        }
    }

    @Override
    public CozeVoiceTranscriptionResponse transcribeAudio(CozeVoiceTranscriptionRequest request) {
        try {
            log.info("开始进行Coze语音识别，文件名: {}", request.getFile().getOriginalFilename());
            
            // 构建multipart/form-data请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 将MultipartFile转换为ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(request.getFile().getBytes()) {
                @Override
                public String getFilename() {
                    return request.getFile().getOriginalFilename();
                }
            };
            
            body.add("file", resource);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String accessToken = cozeJwtService.getAccessToken();
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 调用Coze API进行语音识别
            String url = cozeBaseUrl + "/v1/audio/transcriptions";
            ResponseEntity<CozeVoiceTranscriptionResponse> response = restTemplate.postForEntity(url, requestEntity, CozeVoiceTranscriptionResponse.class);
            
            CozeVoiceTranscriptionResponse cozeResponse = response.getBody();
            
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                log.info("Coze语音识别成功，识别文本: {}", cozeResponse.getData().getText());
            } else {
                log.error("Coze语音识别失败，响应: {}", cozeResponse);
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("Coze语音识别失败，文件: {}, 错误: {}", 
                     request.getFile().getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("语音识别失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] synthesizeSpeech(CozeVoiceSpeechRequest request) {
        try {
            log.info("开始进行Coze语音合成，文本: {}, 音色ID: {}, 情感: {}", 
                    request.getInput(), request.getVoiceId(), request.getEmotion());
            
            String url = cozeBaseUrl + "/v1/audio/speech";
            HttpEntity<CozeVoiceSpeechRequest> entity = new HttpEntity<>(request, getHeaders());
            
            ResponseEntity<byte[]> response = restTemplate.postForEntity(url, entity, byte[].class);
            
            byte[] audioData = response.getBody();
            if (audioData != null) {
                log.info("Coze语音合成成功，生成音频大小: {} bytes", audioData.length);
            } else {
                log.error("Coze语音合成失败，返回数据为空");
                throw new RuntimeException("语音合成失败：返回数据为空");
            }
            
            return audioData;
        } catch (Exception e) {
            log.error("Coze语音合成失败，文本: {}, 音色ID: {}, 错误: {}", 
                     request.getInput(), request.getVoiceId(), e.getMessage(), e);
            throw new RuntimeException("语音合成失败: " + e.getMessage());
        }
    }

    @Override
    public CozeVoiceCloneResponse cloneVoice(CozeVoiceCloneRequest request) {
        try {
            log.info("开始进行Coze音色复刻，音色名称: {}, 语种: {}, 文件名: {}", 
                    request.getVoiceName(), request.getLanguage(), request.getFile().getOriginalFilename());
            
            // 构建multipart/form-data请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 添加文本参数
            body.add("voice_name", request.getVoiceName());
            if (request.getText() != null) {
                body.add("text", request.getText());
            }
            if (request.getLanguage() != null) {
                body.add("language", request.getLanguage());
            }
            if (request.getVoiceId() != null) {
                body.add("voice_id", request.getVoiceId());
            }
            if (request.getPreviewText() != null) {
                body.add("preview_text", request.getPreviewText());
            }
            if (request.getSpaceId() != null) {
                body.add("space_id", request.getSpaceId());
            }
            
            // 将MultipartFile转换为ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(request.getFile().getBytes()) {
                @Override
                public String getFilename() {
                    return request.getFile().getOriginalFilename();
                }
            };
            body.add("file", resource);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String accessToken = cozeJwtService.getAccessToken();
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 调用Coze API进行音色复刻
            String url = cozeBaseUrl + "/v1/audio/voices/clone";
            ResponseEntity<CozeVoiceCloneResponse> response = restTemplate.postForEntity(url, requestEntity, CozeVoiceCloneResponse.class);
            
            CozeVoiceCloneResponse cozeResponse = response.getBody();
            
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                log.info("Coze音色复刻成功，音色ID: {}", cozeResponse.getData().getVoiceId());
            } else {
                log.error("Coze音色复刻失败，响应: {}", cozeResponse);
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("Coze音色复刻失败，音色名称: {}, 错误: {}", 
                     request.getVoiceName(), e.getMessage(), e);
            throw new RuntimeException("音色复刻失败: " + e.getMessage());
        }
    }

    @Override
    public CozeVoiceListResponse getVoiceList(CozeVoiceListRequest request) {
        try {
            log.info("开始获取Coze音色列表，过滤系统音色: {}, 模型类型: {}, 页码: {}, 页大小: {}", 
                    request.getFilterSystemVoice(), request.getModelType(), request.getPageNum(), request.getPageSize());
            
            // 构建查询参数
            StringBuilder urlBuilder = new StringBuilder(cozeBaseUrl + "/v1/audio/voices");
            String separator = "?";
            
            if (request.getFilterSystemVoice() != null) {
                urlBuilder.append(separator).append("filter_system_voice=").append(request.getFilterSystemVoice());
                separator = "&";
            }
            if (request.getModelType() != null) {
                urlBuilder.append(separator).append("model_type=").append(request.getModelType());
                separator = "&";
            }
            if (request.getVoiceState() != null) {
                urlBuilder.append(separator).append("voice_state=").append(request.getVoiceState());
                separator = "&";
            }
            if (request.getPageNum() != null) {
                urlBuilder.append(separator).append("page_num=").append(request.getPageNum());
                separator = "&";
            }
            if (request.getPageSize() != null) {
                urlBuilder.append(separator).append("page_size=").append(request.getPageSize());
            }
            
            String url = urlBuilder.toString();
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            
            ResponseEntity<CozeVoiceListResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, CozeVoiceListResponse.class);
            CozeVoiceListResponse cozeResponse = response.getBody();
            
            if (cozeResponse != null && cozeResponse.getCode() == 0) {
                int voiceCount = cozeResponse.getData() != null && cozeResponse.getData().getVoiceList() != null 
                    ? cozeResponse.getData().getVoiceList().size() : 0;
                log.info("Coze音色列表获取成功，音色数量: {}, 是否有更多: {}", 
                        voiceCount, cozeResponse.getData() != null ? cozeResponse.getData().getHasMore() : false);
            } else {
                log.error("Coze音色列表获取失败，响应: {}", cozeResponse);
            }
            
            return cozeResponse;
        } catch (Exception e) {
            log.error("获取Coze音色列表失败，错误: {}", e.getMessage(), e);
            throw new RuntimeException("获取音色列表失败: " + e.getMessage());
        }
    }


}
