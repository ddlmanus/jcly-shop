package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.coze.EnterpriseChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 企业聊天会话 Mapper 接口
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface EnterpriseChatSessionDao extends BaseMapper<EnterpriseChatSession> {

    /**
     * 根据用户ID和智能体ID查找最新的活跃会话
     */
    @Select("SELECT * FROM eb_coze_enterprise_chat_session " +
            "WHERE user_id = #{userId} AND mer_id = #{merId} AND coze_bot_id = #{cozeBotId} " +
            "AND status = 1 ORDER BY update_time DESC LIMIT 1")
    EnterpriseChatSession findLatestActiveSession(@Param("userId") Long userId, 
                                                @Param("merId") Long merId, 
                                                @Param("cozeBotId") String cozeBotId);

    /**
     * 获取用户的会话统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_sessions, " +
            "SUM(total_messages) as total_messages, " +
            "AVG(total_messages) as avg_messages_per_session " +
            "FROM eb_coze_enterprise_chat_session " +
            "WHERE user_id = #{userId} AND mer_id = #{merId} AND status != 3")
    Map<String, Object> getUserSessionStatistics(@Param("userId") Long userId, 
                                                @Param("merId") Long merId);

    /**
     * 获取商户的会话统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_sessions, " +
            "COUNT(DISTINCT user_id) as unique_users, " +
            "SUM(total_messages) as total_messages, " +
            "AVG(total_messages) as avg_messages_per_session " +
            "FROM eb_coze_enterprise_chat_session " +
            "WHERE mer_id = #{merId} AND status != 3 " +
            "AND create_time >= #{startDate} AND create_time <= #{endDate}")
    Map<String, Object> getMerchantSessionStatistics(@Param("merId") Long merId, 
                                                    @Param("startDate") String startDate, 
                                                    @Param("endDate") String endDate);

    /**
     * 获取热门智能体使用排行
     */
    @Select("SELECT " +
            "coze_bot_id, " +
            "COUNT(*) as session_count, " +
            "SUM(total_messages) as total_messages " +
            "FROM eb_coze_enterprise_chat_session " +
            "WHERE mer_id = #{merId} AND status != 3 " +
            "AND create_time >= #{startDate} AND create_time <= #{endDate} " +
            "GROUP BY coze_bot_id " +
            "ORDER BY session_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getHotBotRanking(@Param("merId") Long merId, 
                                             @Param("startDate") String startDate, 
                                             @Param("endDate") String endDate, 
                                             @Param("limit") Integer limit);
}
