package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryFeeRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 同城配送费用规则表 Mapper 接口
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
public interface CityDeliveryFeeRuleDao extends BaseMapper<CityDeliveryFeeRule> {

    /**
     * 获取启用的费用规则
     */
    List<CityDeliveryFeeRule> getEnabledRules();

    /**
     * 根据配送类型获取费用规则
     */
    List<CityDeliveryFeeRule> getRulesByDeliveryType(@Param("deliveryType") Integer deliveryType);

    /**
     * 根据区域获取费用规则
     */
    CityDeliveryFeeRule getRuleByArea(@Param("areaId") Integer areaId);

    /**
     * 根据规则名称获取费用规则
     */
    CityDeliveryFeeRule getRuleByName(@Param("ruleName") String ruleName);

    /**
     * 更新费用规则状态
     */
    int updateRuleStatus(@Param("ruleId") Integer ruleId,
                       @Param("status") Integer status);

    /**
     * 批量更新费用规则状态
     */
    int batchUpdateRuleStatus(@Param("ruleIds") List<Integer> ruleIds,
                            @Param("status") Integer status);

    /**
     * 获取默认费用规则
     */
    CityDeliveryFeeRule getDefaultRule();

    /**
     * 获取有效的费用规则（在有效期内）
     */
    List<CityDeliveryFeeRule> getEffectiveRules();

    /**
     * 根据适用区域获取费用规则
     */
    List<CityDeliveryFeeRule> getRulesByApplicableAreas(@Param("areaIds") String areaIds);
} 