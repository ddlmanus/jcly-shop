package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.service.CustomerServiceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 人工客服配置Mapper接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface CustomerServiceConfigDao extends BaseMapper<CustomerServiceConfig> {

    /**
     * 根据商户ID获取配置
     */
    CustomerServiceConfig selectByMerId(@Param("merId") Integer merId);

    /**
     * 更新或插入配置
     */
    Integer insertOrUpdate(CustomerServiceConfig config);
}
