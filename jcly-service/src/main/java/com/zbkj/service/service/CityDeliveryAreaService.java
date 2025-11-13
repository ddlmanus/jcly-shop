package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.CityDeliveryArea;
import com.zbkj.common.request.PageParamRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 同城配送区域服务接口
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
public interface CityDeliveryAreaService extends IService<CityDeliveryArea> {

    /**
     * 获取区域分页列表
     */
    List<CityDeliveryArea> getList(PageParamRequest pageParamRequest);

    /**
     * 新增配送区域
     */
    Boolean create(CityDeliveryArea area);

    /**
     * 更新配送区域
     */
    Boolean updateArea(CityDeliveryArea area);

    /**
     * 删除配送区域
     */
    Boolean delete(Integer areaId);

    /**
     * 根据地址获取配送区域
     */
    CityDeliveryArea getAreaByAddress(String address);

    /**
     * 检查地址是否在服务范围内
     */
    Boolean isAddressInServiceArea(String address);

    /**
     * 获取配送时间段
     */
    List<Map<String, String>> getDeliveryTimeSlots(Integer areaId);

    /**
     * 根据位置获取服务区域
     */
    List<CityDeliveryArea> getAreasByLocation(BigDecimal longitude, BigDecimal latitude);

    /**
     * 根据城市获取区域
     */
    List<CityDeliveryArea> getAreasByCity(String province, String city, String district);

    /**
     * 获取启用的区域
     */
    List<CityDeliveryArea> getEnabledAreas();

    /**
     * 更新区域状态
     */
    Boolean updateStatus(Integer areaId, Integer status);

    /**
     * 批量更新区域状态
     */
    Boolean batchUpdateStatus(List<Integer> areaIds, Integer status);

    /**
     * 检查位置是否在区域范围内
     */
    Boolean checkLocationInArea(Integer areaId, BigDecimal longitude, BigDecimal latitude);
} 