package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.merchant.MerchantStoreDeliveryArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 门店配送范围 Mapper 接口
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Mapper
public interface MerchantStoreDeliveryAreaDao extends BaseMapper<MerchantStoreDeliveryArea> {

    /**
     * 根据门店ID获取配送范围列表
     * @param storeId 门店ID
     * @return 配送范围列表
     */
    List<MerchantStoreDeliveryArea> getByStoreId(@Param("storeId") Integer storeId);

    /**
     * 根据位置查找可配送的门店
     * @param latitude 纬度
     * @param longitude 经度
     * @return 可配送的门店ID列表
     */
    List<Integer> getDeliveryStoresByLocation(@Param("latitude") BigDecimal latitude, 
                                             @Param("longitude") BigDecimal longitude);

    /**
     * 检查指定位置是否在配送范围内
     * @param storeId 门店ID
     * @param latitude 纬度
     * @param longitude 经度
     * @return 配送区域信息（如果在范围内）
     */
    MerchantStoreDeliveryArea checkLocationInDeliveryArea(@Param("storeId") Integer storeId,
                                                         @Param("latitude") BigDecimal latitude, 
                                                         @Param("longitude") BigDecimal longitude);

    /**
     * 计算配送费用
     * @param storeId 门店ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param orderAmount 订单金额
     * @return 配送费用信息
     */
    Map<String, Object> calculateDeliveryFee(@Param("storeId") Integer storeId,
                                           @Param("latitude") BigDecimal latitude, 
                                           @Param("longitude") BigDecimal longitude,
                                           @Param("orderAmount") BigDecimal orderAmount);

    /**
     * 获取门店配送范围统计
     * @param storeId 门店ID
     * @return 统计信息
     */
    Map<String, Object> getDeliveryAreaStatistics(@Param("storeId") Integer storeId);

    /**
     * 批量更新配送范围状态
     * @param areaIds 配送范围ID列表
     * @param isEnabled 是否启用
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("areaIds") List<Integer> areaIds, @Param("isEnabled") Integer isEnabled);

    /**
     * 根据门店ID删除配送范围
     * @param storeId 门店ID
     * @return 删除数量
     */
    int deleteByStoreId(@Param("storeId") Integer storeId);

    /**
     * 获取指定商户的所有配送范围
     * @param merId 商户ID
     * @return 配送范围列表
     */
    List<MerchantStoreDeliveryArea> getByMerId(@Param("merId") Integer merId);
} 