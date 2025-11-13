package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.merchant.MerchantStoreHours;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 门店营业时间配置 Mapper 接口
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Mapper
public interface MerchantStoreHoursDao extends BaseMapper<MerchantStoreHours> {

    /**
     * 根据门店ID获取营业时间配置
     * @param storeId 门店ID
     * @return 营业时间配置列表
     */
    List<MerchantStoreHours> getByStoreId(@Param("storeId") Integer storeId);

    /**
     * 批量插入营业时间配置
     * @param list 营业时间配置列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<MerchantStoreHours> list);

    /**
     * 批量更新营业时间配置
     * @param list 营业时间配置列表
     * @return 更新数量
     */
    int batchUpdate(@Param("list") List<MerchantStoreHours> list);

    /**
     * 根据门店ID删除营业时间配置
     * @param storeId 门店ID
     * @return 删除数量
     */
    int deleteByStoreId(@Param("storeId") Integer storeId);

    /**
     * 获取当前营业的门店
     * @param dayOfWeek 星期几
     * @param currentTime 当前时间（HH:mm:ss格式）
     * @return 营业中的门店ID列表
     */
    List<Integer> getOpenStoreIds(@Param("dayOfWeek") Integer dayOfWeek, @Param("currentTime") String currentTime);
} 