package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.platform.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 平台门店信息 Mapper 接口
 */
@Mapper
public interface StoreDao extends BaseMapper<Store> {

    /**
     * 获取门店列表（带商户名称）
     * @param params 查询参数
     * @return 门店列表
     */
    List<Store> getStoreListWithMerchant(@Param("params") Map<String, Object> params);

    /**
     * 根据商户ID获取门店列表
     * @param merId 商户ID
     * @return 门店列表
     */
    List<Store> getStoreListByMerId(@Param("merId") Integer merId);

    /**
     * 根据位置查找附近的门店
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 搜索半径（千米）
     * @param limit 返回数量限制
     * @return 附近门店列表
     */
    List<Store> getNearbyStores(@Param("latitude") Double latitude, 
                               @Param("longitude") Double longitude, 
                               @Param("radius") Double radius, 
                               @Param("limit") Integer limit);

    /**
     * 获取门店统计信息
     * @param storeId 门店ID
     * @return 统计信息
     */
    Map<String, Object> getStoreStatistics(@Param("storeId") Integer storeId);

    /**
     * 批量更新门店状态
     * @param storeIds 门店ID列表
     * @param status 状态值
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("storeIds") List<Integer> storeIds, @Param("status") Integer status);

    /**
     * 检查门店编码是否已存在
     * @param storeCode 门店编码
     * @param excludeId 排除的门店ID（用于编辑时检查）
     * @return 存在数量
     */
    int checkStoreCodeExists(@Param("storeCode") String storeCode, 
                            @Param("excludeId") Integer excludeId);
}