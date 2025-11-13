package com.zbkj.service.service;

import java.util.List;
import java.util.Map;

/**
 * 聊天快捷回复服务接口
 * 提供预设回复、智能推荐功能
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface ChatQuickReplyService {

    /**
     * 获取快捷回复列表
     * @param userId 用户ID
     * @param userType 用户类型: USER, STAFF, ADMIN
     * @param category 分类: greeting, common, product, order, service等
     * @return 快捷回复列表
     */
    List<Map<String, Object>> getQuickReplies(Integer userId, String userType, String category);

    /**
     * 获取所有快捷回复分类
     * @param userType 用户类型
     * @return 分类列表
     */
    List<Map<String, Object>> getQuickReplyCategories(String userType);

    /**
     * 创建快捷回复
     * @param userId 创建者ID
     * @param userType 用户类型
     * @param category 分类
     * @param title 标题
     * @param content 内容
     * @param contentType 内容类型: text, image, product_card等
     * @param tags 标签
     * @return 创建结果
     */
    Map<String, Object> createQuickReply(Integer userId, String userType, String category, 
                                        String title, String content, String contentType, String tags);

    /**
     * 更新快捷回复
     * @param replyId 回复ID
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param category 分类
     * @param tags 标签
     * @return 更新结果
     */
    Map<String, Object> updateQuickReply(Integer replyId, Integer userId, String title, 
                                        String content, String category, String tags);

    /**
     * 删除快捷回复
     * @param replyId 回复ID
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteQuickReply(Integer replyId, Integer userId);

    /**
     * 搜索快捷回复
     * @param keyword 关键词
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 搜索结果
     */
    List<Map<String, Object>> searchQuickReplies(String keyword, Integer userId, String userType);

    /**
     * 获取智能推荐回复
     * @param userMessage 用户消息
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐回复列表
     */
    List<Map<String, Object>> getSmartReplySuggestions(String userMessage, String sessionId, 
                                                       Integer userId, int limit);

    /**
     * 记录快捷回复使用
     * @param replyId 回复ID
     * @param userId 使用者ID
     * @param sessionId 会话ID
     */
    void recordQuickReplyUsage(Integer replyId, Integer userId, String sessionId);

    /**
     * 获取常用快捷回复
     * @param userId 用户ID
     * @param userType 用户类型
     * @param limit 数量限制
     * @return 常用回复列表
     */
    List<Map<String, Object>> getFrequentlyUsedReplies(Integer userId, String userType, int limit);

    /**
     * 批量导入快捷回复
     * @param userId 用户ID
     * @param userType 用户类型
     * @param repliesData 回复数据列表
     * @return 导入结果
     */
    Map<String, Object> batchImportQuickReplies(Integer userId, String userType, 
                                               List<Map<String, Object>> repliesData);

    /**
     * 导出快捷回复
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 导出数据
     */
    Map<String, Object> exportQuickReplies(Integer userId, String userType);
}
