package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryDriver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 同城配送员表 Mapper 接口
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
@Mapper
public interface CityDeliveryDriverDao extends BaseMapper<CityDeliveryDriver> {

    /**
     * 根据位置获取附近的配送员
     */
    List<CityDeliveryDriver> getNearbyDrivers(@Param("longitude") BigDecimal longitude,
                                             @Param("latitude") BigDecimal latitude,
                                             @Param("radius") BigDecimal radius);

    /**
     * 获取在线配送员
     */
    List<CityDeliveryDriver> getOnlineDrivers();

    /**
     * 获取可用配送员（在线且可接单）
     */
    List<CityDeliveryDriver> getAvailableDrivers();

    /**
     * 根据区域获取配送员
     */
    List<CityDeliveryDriver> getDriversByArea(@Param("workArea") String workArea);

    /**
     * 更新配送员位置
     */
    int updateDriverLocation(@Param("driverId") Integer driverId,
                           @Param("longitude") BigDecimal longitude,
                           @Param("latitude") BigDecimal latitude,
                           @Param("address") String address);

    /**
     * 更新配送员状态
     */
    int updateDriverStatus(@Param("driverId") Integer driverId,
                         @Param("status") Integer status);

    /**
     * 批量更新配送员状态
     */
    int batchUpdateDriverStatus(@Param("driverIds") List<Integer> driverIds,
                              @Param("status") Integer status);

    /**
     * 获取配送员统计信息
     */
    CityDeliveryDriver getDriverWithStats(@Param("driverId") Integer driverId);

    /**
     * 更新配送员订单数
     */
    int updateDriverOrderCount(@Param("driverId") Integer driverId,
                             @Param("increment") Integer increment);

    /**
     * 更新配送员收入
     */
    int updateDriverIncome(@Param("driverId") Integer driverId,
                         @Param("income") BigDecimal income);

    /**
     * 获取认证通过的配送员
     */
    List<CityDeliveryDriver> getCertifiedDrivers();

    /**
     * 根据手机号获取配送员
     */
    CityDeliveryDriver getDriverByPhone(@Param("phone") String phone);

    /**
     * 根据配送员编号获取配送员
     */
    CityDeliveryDriver getDriverByCode(@Param("driverCode") String driverCode);
} 