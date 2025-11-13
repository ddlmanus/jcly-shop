package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.service.CustomerServiceStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人工客服员工Mapper接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface CustomerServiceStaffDao extends BaseMapper<CustomerServiceStaff> {

    /**
     * 根据员工ID查询客服信息
     */
    CustomerServiceStaff selectByAdminId(@Param("adminId") Integer adminId);

    /**
     * 获取可分配的客服列表
     */
    List<CustomerServiceStaff> selectAvailableStaff(@Param("merId") Integer merId, 
                                                   @Param("serviceLevel") String serviceLevel);

    /**
     * 更新客服在线状态
     */
    Integer updateOnlineStatus(@Param("staffId") Integer staffId, @Param("onlineStatus") String onlineStatus);

    /**
     * 更新当前会话数
     */
    Integer updateCurrentSessions(@Param("staffId") Integer staffId, @Param("increment") Integer increment);

    /**
     * 获取在线客服统计
     */
    Integer countOnlineStaff(@Param("merId") Integer merId);
}
