package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.CityDeliveryFeeRule;
import com.zbkj.common.request.PageParamRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 同城配送费用规则服务接口
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
public interface CityDeliveryFeeRuleService extends IService<CityDeliveryFeeRule> {

    /**
     * 获取费用规则分页列表
     */
    List<CityDeliveryFeeRule> getList(PageParamRequest pageParamRequest);

    /**
     * 新增费用规则
     */
    Boolean create(CityDeliveryFeeRule feeRule);

    /**
     * 更新费用规则
     */
    Boolean updateFeeRule(CityDeliveryFeeRule feeRule);

    /**
     * 删除费用规则
     */
    Boolean delete(Integer feeRuleId);

    /**
     * 计算配送费用
     */
    Map<String, Object> calculateFee(Integer feeRuleId, BigDecimal distance, Integer deliveryType);

    /**
     * 根据区域获取费用规则
     */
    CityDeliveryFeeRule getRuleByArea(Integer areaId);

    /**
     * 根据配送类型获取费用规则
     */
    List<CityDeliveryFeeRule> getRulesByDeliveryType(Integer deliveryType);

    /**
     * 获取启用的费用规则
     */
    List<CityDeliveryFeeRule> getEnabledRules();

    /**
     * 获取默认费用规则
     */
    CityDeliveryFeeRule getDefaultRule();

    /**
     * 更新费用规则状态
     */
    Boolean updateStatus(Integer feeRuleId, Integer status);

    /**
     * 批量更新费用规则状态
     */
    Boolean batchUpdateStatus(List<Integer> feeRuleIds, Integer status);

    /**
     * 详细费用计算
     */
    Map<String, Object> calculateDetailedFee(String pickupAddress, String deliveryAddress, 
                                           Integer deliveryType, BigDecimal weight, BigDecimal volume);

    /**
     * 根据距离计算费用
     */
    BigDecimal calculateFeeByDistance(BigDecimal distance, Integer deliveryType);

    /**
     * 根据重量和体积计算附加费用
     */
    BigDecimal calculateExtraFee(BigDecimal weight, BigDecimal volume);

    /**
     * 预览费用计算
     */
    Map<String, Object> previewFee(String fromAddress, String toAddress, Integer deliveryType, 
                                 BigDecimal weight, BigDecimal volume);

    /**
     * 详细费用计算（扩展）
     */
    Map<String, Object> calculateDetailedFee(Integer feeRuleId, BigDecimal distance, BigDecimal weight, 
                                           BigDecimal volume, Integer deliveryType, Boolean isNightTime, 
                                           Boolean isBadWeather, Boolean isHoliday, Integer urgentLevel);
} 