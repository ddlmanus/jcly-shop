package com.zbkj.service.service;

import com.zbkj.common.model.member.MemberIntegralRule;

/**
 * 会员积分规则服务接口
 */
public interface MemberIntegralRuleService {

    /**
     * 获取积分规则
     * @return 积分规则
     */
    MemberIntegralRule getRule();

    /**
     * 根据商户ID获取积分规则
     * @param merId 商户ID
     * @return 积分规则
     */
    MemberIntegralRule getByMerId(Integer merId);

    /**
     * 保存积分规则
     * @param memberIntegralRule 积分规则
     * @return 保存结果
     */
    boolean saveRule(MemberIntegralRule memberIntegralRule);
}