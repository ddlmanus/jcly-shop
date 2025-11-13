package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryException;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 配送异常记录表 Mapper 接口
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
public interface CityDeliveryExceptionDao extends BaseMapper<CityDeliveryException> {

    /**
     * 根据配送订单号获取异常记录
     */
    List<CityDeliveryException> getExceptionsByDeliveryOrderNo(@Param("deliveryOrderNo") String deliveryOrderNo);

    /**
     * 根据配送员ID获取异常记录
     */
    List<CityDeliveryException> getExceptionsByDriverId(@Param("driverId") Integer driverId);

    /**
     * 根据异常类型获取异常记录
     */
    List<CityDeliveryException> getExceptionsByType(@Param("exceptionType") Integer exceptionType);

    /**
     * 根据处理状态获取异常记录
     */
    List<CityDeliveryException> getExceptionsByHandleStatus(@Param("handleStatus") Integer handleStatus);

    /**
     * 获取待处理的异常记录
     */
    List<CityDeliveryException> getPendingExceptions();

    /**
     * 更新异常处理状态
     */
    int updateExceptionHandleStatus(@Param("exceptionId") Integer exceptionId,
                                  @Param("handleStatus") Integer handleStatus,
                                  @Param("handleResult") String handleResult,
                                  @Param("handlerId") Integer handlerId);

    /**
     * 根据举报人类型获取异常记录
     */
    List<CityDeliveryException> getExceptionsByReporterType(@Param("reporterType") Integer reporterType);

    /**
     * 获取配送员异常统计
     */
    Integer getDriverExceptionCount(@Param("driverId") Integer driverId);

    /**
     * 批量更新异常处理状态
     */
    int batchUpdateExceptionHandleStatus(@Param("exceptionIds") List<Integer> exceptionIds,
                                       @Param("handleStatus") Integer handleStatus);
} 