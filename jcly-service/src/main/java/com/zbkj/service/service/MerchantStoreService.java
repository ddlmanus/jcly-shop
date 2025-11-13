package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.MerchantStore;
import com.zbkj.common.model.merchant.MerchantStoreDeliveryArea;
import com.zbkj.common.model.merchant.MerchantStoreHours;
import com.zbkj.common.model.merchant.MerchantStoreStaff;
import com.zbkj.common.request.MerchantStoreRequest;
import com.zbkj.common.request.MerchantStoreSearchRequest;
import com.zbkj.common.vo.MyRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商户门店管理服务接口
 * 
 * @author 系统
 * @date 2025-01-07
 */
public interface MerchantStoreService extends IService<MerchantStore> {

    /**
     * 门店分页列表
     * @param request 查询条件
     * @return 分页数据
     */
    PageInfo<MerchantStore> getPageList(MerchantStoreSearchRequest request);

    /**
     * 根据商户ID获取门店列表
     * @param merId 商户ID
     * @return 门店列表
     */
    List<MerchantStore> getStoreListByMerId(Integer merId);

    /**
     * 获取商户的主门店
     * @param merId 商户ID
     * @return 主门店信息
     */
    MerchantStore getMainStoreByMerId(Integer merId);

    /**
     * 创建门店
     * @param request 门店信息
     * @return 操作结果
     */
    MyRecord createStore(MerchantStoreRequest request);

    /**
     * 更新门店信息
     * @param id 门店ID
     * @param request 门店信息
     * @return 操作结果
     */
    MyRecord updateStore(Integer id, MerchantStoreRequest request);

    /**
     * 删除门店
     * @param id 门店ID
     * @return 操作结果
     */
    MyRecord deleteStore(Integer id);

    /**
     * 批量删除门店
     * @param ids 门店ID列表
     * @return 操作结果
     */
    MyRecord batchDeleteStore(List<Integer> ids);

    /**
     * 更新门店状态
     * @param id 门店ID
     * @param status 状态值
     * @return 操作结果
     */
    MyRecord updateStoreStatus(Integer id, Integer status);

    /**
     * 批量更新门店状态
     * @param ids 门店ID列表
     * @param status 状态值
     * @return 操作结果
     */
    MyRecord batchUpdateStoreStatus(List<Integer> ids, Integer status);

    /**
     * 设置主门店
     * @param merId 商户ID
     * @param storeId 门店ID
     * @return 操作结果
     */
    MyRecord setMainStore(Integer merId, Integer storeId);

    /**
     * 获取门店详情（包含关联信息）
     * @param id 门店ID
     * @return 门店详情
     */
    MerchantStore getStoreDetail(Integer id);

    /**
     * 根据位置查找附近的门店
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 搜索半径（千米）
     * @param limit 返回数量限制
     * @return 附近门店列表
     */
    List<MerchantStore> getNearbyStores(Double latitude, Double longitude, Double radius, Integer limit);

    /**
     * 获取门店统计信息
     * @param storeId 门店ID
     * @return 统计信息
     */
    Map<String, Object> getStoreStatistics(Integer storeId);

    /**
     * 获取商户门店数量统计
     * @param merId 商户ID
     * @return 统计信息
     */
    Map<String, Object> getMerchantStoreCount(Integer merId);

    /**
     * 检查门店编码是否已存在
     * @param merId 商户ID
     * @param storeCode 门店编码
     * @param excludeId 排除的门店ID（用于编辑时检查）
     * @return 是否存在
     */
    boolean checkStoreCodeExists(Integer merId, String storeCode, Integer excludeId);

    /**
     * 生成门店编码
     * @param merId 商户ID
     * @return 门店编码
     */
    String generateStoreCode(Integer merId);

    // 营业时间管理
    /**
     * 设置门店营业时间
     * @param storeId 门店ID
     * @param storeHours 营业时间配置列表
     * @return 操作结果
     */
    MyRecord setStoreHours(Integer storeId, List<MerchantStoreHours> storeHours);

    /**
     * 获取门店营业时间
     * @param storeId 门店ID
     * @return 营业时间配置列表
     */
    List<MerchantStoreHours> getStoreHours(Integer storeId);

    // 员工管理
    /**
     * 添加门店员工
     * @param storeStaff 员工信息
     * @return 操作结果
     */
    MyRecord addStoreStaff(MerchantStoreStaff storeStaff);

    /**
     * 更新门店员工
     * @param storeStaff 员工信息
     * @return 操作结果
     */
    MyRecord updateStoreStaff(MerchantStoreStaff storeStaff);

    /**
     * 删除门店员工
     * @param staffId 员工ID
     * @return 操作结果
     */
    MyRecord deleteStoreStaff(Integer staffId);

    /**
     * 获取门店员工列表
     * @param storeId 门店ID
     * @return 员工列表
     */
    List<MerchantStoreStaff> getStoreStaff(Integer storeId);

    // 配送范围管理
    /**
     * 添加配送范围
     * @param deliveryArea 配送范围信息
     * @return 操作结果
     */
    MyRecord addDeliveryArea(MerchantStoreDeliveryArea deliveryArea);

    /**
     * 更新配送范围
     * @param deliveryArea 配送范围信息
     * @return 操作结果
     */
    MyRecord updateDeliveryArea(MerchantStoreDeliveryArea deliveryArea);

    /**
     * 删除配送范围
     * @param areaId 配送范围ID
     * @return 操作结果
     */
    MyRecord deleteDeliveryArea(Integer areaId);

    /**
     * 获取门店配送范围列表
     * @param storeId 门店ID
     * @return 配送范围列表
     */
    List<MerchantStoreDeliveryArea> getStoreDeliveryAreas(Integer storeId);

    /**
     * 检查指定位置是否在配送范围内
     * @param storeId 门店ID
     * @param latitude 纬度
     * @param longitude 经度
     * @return 配送区域信息（如果在范围内）
     */
    MerchantStoreDeliveryArea checkLocationInDeliveryArea(Integer storeId, BigDecimal latitude, BigDecimal longitude);

    /**
     * 计算配送费用
     * @param storeId 门店ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param orderAmount 订单金额
     * @return 配送费用信息
     */
    Map<String, Object> calculateDeliveryFee(Integer storeId, BigDecimal latitude, BigDecimal longitude, BigDecimal orderAmount);
} 