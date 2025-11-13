package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 配送轨迹表 Mapper 接口
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
public interface CityDeliveryTrackDao extends BaseMapper<CityDeliveryTrack> {

    /**
     * 根据配送订单号获取轨迹列表
     */
    List<CityDeliveryTrack> getTracksByDeliveryOrderNo(@Param("deliveryOrderNo") String deliveryOrderNo);

    /**
     * 根据配送员ID获取轨迹列表
     */
    List<CityDeliveryTrack> getTracksByDriverId(@Param("driverId") Integer driverId);

    /**
     * 获取最新的轨迹记录
     */
    CityDeliveryTrack getLatestTrack(@Param("deliveryOrderNo") String deliveryOrderNo);

    /**
     * 根据状态码获取轨迹
     */
    List<CityDeliveryTrack> getTracksByStatusCode(@Param("statusCode") Integer statusCode);

    /**
     * 根据轨迹类型获取轨迹
     */
    List<CityDeliveryTrack> getTracksByType(@Param("trackType") Integer trackType);

    /**
     * 删除配送订单的轨迹记录
     */
    int deleteTracksByDeliveryOrderNo(@Param("deliveryOrderNo") String deliveryOrderNo);

    /**
     * 获取配送员的实时位置
     */
    CityDeliveryTrack getDriverLatestLocation(@Param("driverId") Integer driverId);

    /**
     * 批量插入轨迹记录
     */
    int batchInsertTracks(@Param("tracks") List<CityDeliveryTrack> tracks);
} 