package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.service.HumanServiceMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人工客服消息Mapper接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface HumanServiceMessageDao extends BaseMapper<HumanServiceMessage> {

    /**
     * 获取会话未读消息数
     */
    Integer countUnreadMessages(@Param("sessionId") String sessionId, @Param("receiverId") Long receiverId);

    /**
     * 批量标记消息为已读
     */
    Integer markMessagesAsRead(@Param("sessionId") String sessionId, @Param("receiverId") Long receiverId);

    /**
     * 获取用户离线消息
     */
    List<HumanServiceMessage> selectOfflineMessages(@Param("receiverId") Long receiverId, 
                                                   @Param("receiverType") String receiverType);

    /**
     * 获取会话最后一条消息
     */
    HumanServiceMessage selectLastMessage(@Param("sessionId") String sessionId);
}
