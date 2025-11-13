package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.platform.Store;
import com.zbkj.common.request.StoreRequest;
import com.zbkj.common.request.StoreSearchRequest;
import com.zbkj.common.vo.MyRecord;

import java.util.List;
import java.util.Map;

/**
 * 平台门店服务接口
 */
public interface StoreService extends IService<Store> {

    /**
     * 分页获取门店列表
     * @param request 搜索请求参数
     * @return 分页门店列表
     */
    PageInfo<Store> getPageList(StoreSearchRequest request);

    /**
     * 根据商户ID获取门店列表
     * @param merId 商户ID
     * @return 门店列表
     */
    List<Store> getStoreListByMerId(Integer merId);

    /**
     * 创建门店
     * @param request 门店请求参数
     * @return 操作结果
     */
    MyRecord createStore(StoreRequest request);

    /**
     * 更新门店
     * @param id 门店ID
     * @param request 门店请求参数
     * @return 操作结果
     */
    MyRecord updateStore(Integer id, StoreRequest request);

    /**
     * 获取门店详情
     * @param id 门店ID
     * @return 门店详情
     */
    Store getStoreDetail(Integer id);

    /**
     * 获取附近的门店
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 搜索半径（千米）
     * @param limit 返回数量限制
     * @return 附近门店列表
     */
    List<Store> getNearbyStores(Double latitude, Double longitude, Double radius, Integer limit);

    /**
     * 批量更新门店状态
     * @param storeIds 门店ID列表
     * @param status 状态值
     * @return 操作结果
     */
    MyRecord batchUpdateStatus(List<Integer> storeIds, Integer status);

    /**
     * 删除门店
     * @param id 门店ID
     * @return 操作结果
     */
    MyRecord deleteStore(Integer id);
    
    /**
     * 获取所有门店列表
     * @return 门店列表
     */
    List<Store> getAllStores();
}
