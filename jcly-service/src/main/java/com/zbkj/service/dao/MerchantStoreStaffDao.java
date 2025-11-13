package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.merchant.MerchantStoreStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 门店员工关联 Mapper 接口
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Mapper
public interface MerchantStoreStaffDao extends BaseMapper<MerchantStoreStaff> {

    /**
     * 根据门店ID获取员工列表
     * @param storeId 门店ID
     * @return 员工列表
     */
    List<MerchantStoreStaff> getByStoreId(@Param("storeId") Integer storeId);

    /**
     * 根据商户ID获取所有员工列表
     * @param merId 商户ID
     * @return 员工列表
     */
    List<MerchantStoreStaff> getByMerId(@Param("merId") Integer merId);

    /**
     * 获取门店管理员列表
     * @param storeId 门店ID
     * @return 管理员列表
     */
    List<MerchantStoreStaff> getManagersByStoreId(@Param("storeId") Integer storeId);

    /**
     * 检查员工电话是否已存在
     * @param staffPhone 员工电话
     * @param excludeId 排除的员工ID（用于编辑时检查）
     * @return 存在数量
     */
    int checkStaffPhoneExists(@Param("staffPhone") String staffPhone, @Param("excludeId") Integer excludeId);

    /**
     * 获取门店员工统计
     * @param storeId 门店ID
     * @return 统计信息
     */
    Map<String, Object> getStoreStaffStatistics(@Param("storeId") Integer storeId);

    /**
     * 批量更新员工状态
     * @param staffIds 员工ID列表
     * @param status 状态值
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("staffIds") List<Integer> staffIds, @Param("status") Integer status);

    /**
     * 根据门店ID删除员工
     * @param storeId 门店ID
     * @return 删除数量
     */
    int deleteByStoreId(@Param("storeId") Integer storeId);
} 