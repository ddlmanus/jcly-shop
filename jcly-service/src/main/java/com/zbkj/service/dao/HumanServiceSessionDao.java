package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.service.HumanServiceSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 人工客服会话Mapper接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface HumanServiceSessionDao extends BaseMapper<HumanServiceSession> {

    /**
     * 根据企业会话ID查询人工客服会话
     */
    HumanServiceSession selectByEnterpriseSessionId(@Param("enterpriseSessionId") String enterpriseSessionId);

    /**
     * 获取等待中的会话列表
     */
    List<HumanServiceSession> selectWaitingSessions(@Param("merId") Integer merId);

    /**
     * 获取客服当前会话数
     */
    Integer countCurrentSessions(@Param("staffId") Integer staffId);

    /**
     * 获取排队统计信息
     */
    Map<String, Object> getQueueStatistics(@Param("merId") Integer merId);

    /**
     * 获取客服会话统计
     */
    List<Map<String, Object>> getStaffSessionStatistics(@Param("merId") Integer merId,
                                                        @Param("startDate") String startDate, 
                                                        @Param("endDate") String endDate);

    /**
     * 更新会话状态
     */
    Integer updateSessionStatus(@Param("sessionId") String sessionId, 
                               @Param("status") String status,
                               @Param("staffId") Integer staffId);
}
