package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 同城配送区域表 Mapper 接口
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
public interface CityDeliveryAreaDao extends BaseMapper<CityDeliveryArea> {

    /**
     * 根据位置获取服务区域
     */
    List<CityDeliveryArea> getAreasByLocation(@Param("longitude") BigDecimal longitude,
                                            @Param("latitude") BigDecimal latitude);

    /**
     * 根据城市获取区域
     */
    List<CityDeliveryArea> getAreasByCity(@Param("province") String province,
                                        @Param("city") String city,
                                        @Param("district") String district);

    /**
     * 获取启用的区域
     */
    List<CityDeliveryArea> getEnabledAreas();

    /**
     * 根据区域名称获取区域
     */
    CityDeliveryArea getAreaByName(@Param("areaName") String areaName);

    /**
     * 更新区域状态
     */
    int updateAreaStatus(@Param("areaId") Integer areaId,
                       @Param("status") Integer status);

    /**
     * 批量更新区域状态
     */
    int batchUpdateAreaStatus(@Param("areaIds") List<Integer> areaIds,
                            @Param("status") Integer status);

    /**
     * 检查位置是否在区域范围内
     */
    int checkLocationInArea(@Param("areaId") Integer areaId,
                          @Param("longitude") BigDecimal longitude,
                          @Param("latitude") BigDecimal latitude);

    /**
     * 获取区域配送时间段
     */
    String getAreaTimeSlots(@Param("areaId") Integer areaId);

    /**
     * 根据费用规则获取区域
     */
    List<CityDeliveryArea> getAreasByFeeRule(@Param("feeRuleId") Integer feeRuleId);

    /**
     * 获取区域统计信息
     */
    CityDeliveryArea getAreaWithStats(@Param("areaId") Integer areaId);
} 