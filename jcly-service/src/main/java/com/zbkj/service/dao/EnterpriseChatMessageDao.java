package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.coze.EnterpriseChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 企业聊天消息 Mapper 接口
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface EnterpriseChatMessageDao extends BaseMapper<EnterpriseChatMessage> {

    /**
     * 获取会话的上下文消息（用于传递给AI）
     */
    @Select("SELECT * FROM eb_coze_enterprise_chat_message " +
            "WHERE session_id = #{sessionId} AND status != 'deleted' " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<EnterpriseChatMessage> getContextMessages(@Param("sessionId") String sessionId, 
                                                  @Param("limit") Integer limit);

    /**
     * 获取会话的最后一条消息
     */
    @Select("SELECT * FROM eb_coze_enterprise_chat_message " +
            "WHERE session_id = #{sessionId} AND status != 'deleted' " +
            "ORDER BY create_time DESC LIMIT 1")
    EnterpriseChatMessage getLastMessage(@Param("sessionId") String sessionId);

    /**
     * 统计会话中的消息数量
     */
    @Select("SELECT COUNT(*) FROM eb_coze_enterprise_chat_message " +
            "WHERE session_id = #{sessionId} AND status != 'deleted'")
    Integer countSessionMessages(@Param("sessionId") String sessionId);

    /**
     * 批量更新消息状态
     */
    @Update("UPDATE eb_coze_enterprise_chat_message SET status = #{status} " +
            "WHERE session_id = #{sessionId} AND status != 'deleted'")
    int batchUpdateMessageStatus(@Param("sessionId") String sessionId, 
                               @Param("status") String status);

    /**
     * 获取用户消息统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_messages, " +
            "COUNT(CASE WHEN role = 'user' THEN 1 END) as user_messages, " +
            "COUNT(CASE WHEN role = 'assistant' THEN 1 END) as assistant_messages, " +
            "SUM(COALESCE(tokens_used, 0)) as total_tokens, " +
            "AVG(COALESCE(processing_time, 0)) as avg_processing_time " +
            "FROM eb_coze_enterprise_chat_message m " +
            "JOIN coze_enterprise_chat_session s ON m.session_id = s.session_id " +
            "WHERE s.user_id = #{userId} AND s.mer_id = #{merId} " +
            "AND m.status != 'deleted' AND s.status != 3 " +
            "AND m.create_time >= #{startDate} AND m.create_time <= #{endDate}")
    Map<String, Object> getUserMessageStatistics(@Param("userId") Long userId, 
                                                @Param("merId") Long merId, 
                                                @Param("startDate") String startDate, 
                                                @Param("endDate") String endDate);

    /**
     * 获取商户消息统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_messages, " +
            "COUNT(CASE WHEN role = 'user' THEN 1 END) as user_messages, " +
            "COUNT(CASE WHEN role = 'assistant' THEN 1 END) as assistant_messages, " +
            "SUM(COALESCE(tokens_used, 0)) as total_tokens, " +
            "AVG(COALESCE(processing_time, 0)) as avg_processing_time, " +
            "COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_messages " +
            "FROM eb_coze_enterprise_chat_message m " +
            "JOIN coze_enterprise_chat_session s ON m.session_id = s.session_id " +
            "WHERE s.mer_id = #{merId} AND m.status != 'deleted' AND s.status != 3 " +
            "AND m.create_time >= #{startDate} AND m.create_time <= #{endDate}")
    Map<String, Object> getMerchantMessageStatistics(@Param("merId") Long merId, 
                                                    @Param("startDate") String startDate, 
                                                    @Param("endDate") String endDate);

    /**
     * 获取热门对话主题（基于消息内容关键词）
     */
    @Select("SELECT " +
            "SUBSTRING(content, 1, 50) as topic, " +
            "COUNT(*) as message_count " +
            "FROM eb_coze_enterprise_chat_message m " +
            "JOIN coze_enterprise_chat_session s ON m.session_id = s.session_id " +
            "WHERE s.mer_id = #{merId} AND m.role = 'user' AND m.status != 'deleted' " +
            "AND m.create_time >= #{startDate} AND m.create_time <= #{endDate} " +
            "AND CHAR_LENGTH(m.content) > 5 " +
            "GROUP BY SUBSTRING(content, 1, 50) " +
            "ORDER BY message_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getHotTopics(@Param("merId") Long merId, 
                                         @Param("startDate") String startDate, 
                                         @Param("endDate") String endDate, 
                                         @Param("limit") Integer limit);

    /**
     * 获取失败的消息列表（用于重试）
     */
    @Select("SELECT * FROM eb_coze_enterprise_chat_message " +
            "WHERE session_id = #{sessionId} AND status = 'failed' " +
            "ORDER BY create_time DESC")
    List<EnterpriseChatMessage> getFailedMessages(@Param("sessionId") String sessionId);
}
