package com.zbkj.service.dao.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 统一聊天消息 Dao
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface UnifiedChatMessageDao extends BaseMapper<UnifiedChatMessage> {

}
