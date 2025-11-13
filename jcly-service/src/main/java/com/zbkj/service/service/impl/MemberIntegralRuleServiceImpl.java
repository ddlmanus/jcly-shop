package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.member.MemberIntegralRule;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.dao.MemberIntegralRuleDao;
import com.zbkj.service.service.MemberIntegralRuleService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员积分规则服务实现类
 */
@Service
public class MemberIntegralRuleServiceImpl implements MemberIntegralRuleService {

    @Resource
    private MemberIntegralRuleDao memberIntegralRuleDao;

    @Autowired
    private UserService userService;


    /**
     * 获取积分规则
     * @return 积分规则
     */
    @Override
    public MemberIntegralRule getRule() {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        Integer merId = systemAdmin.getMerId();
       // Integer merId = userService.getCurrentMerId();
        if (merId == null || merId <= 0) {
            return getDefaultRule();
        }

        LambdaQueryWrapper<MemberIntegralRule> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberIntegralRule::getMerId, merId);
        MemberIntegralRule rule = memberIntegralRuleDao.selectOne(lambdaQueryWrapper);

        if (rule == null) {
            return getDefaultRule();
        }

        return rule;
    }

    /**
     * 根据商户ID获取积分规则
     * @param merId 商户ID
     * @return 积分规则
     */
    @Override
    public MemberIntegralRule getByMerId(Integer merId) {
        if (merId == null || merId <= 0) {
            return getDefaultRule();
        }

        LambdaQueryWrapper<MemberIntegralRule> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberIntegralRule::getMerId, merId);
        MemberIntegralRule rule = memberIntegralRuleDao.selectOne(lambdaQueryWrapper);

        if (rule == null) {
            return getDefaultRule();
        }

        return rule;
    }

    /**
     * 保存积分规则
     * @param memberIntegralRule 积分规则
     * @return 保存结果
     */
    @Override
    public boolean saveRule(MemberIntegralRule memberIntegralRule) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        Integer merId = systemAdmin.getMerId();
        if (merId == null || merId <= 0) {
            return false;
        }

        // 设置商户ID
        memberIntegralRule.setMerId(merId);

        // 查询是否已存在规则
        LambdaQueryWrapper<MemberIntegralRule> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberIntegralRule::getMerId, merId);
        MemberIntegralRule existRule = memberIntegralRuleDao.selectOne(lambdaQueryWrapper);

        if (existRule != null) {
            // 更新规则
            memberIntegralRule.setId(existRule.getId());
            memberIntegralRule.setUpdateTime(new Date());
            return memberIntegralRuleDao.updateById(memberIntegralRule) > 0;
        } else {
            // 新增规则
            memberIntegralRule.setCreateTime(new Date());
            memberIntegralRule.setUpdateTime(new Date());
            return memberIntegralRuleDao.insert(memberIntegralRule) > 0;
        }
    }

    /**
     * 获取默认积分规则
     * @return 默认积分规则
     */
    private MemberIntegralRule getDefaultRule() {
        MemberIntegralRule rule = new MemberIntegralRule();
        rule.setMoneyToIntegral(new BigDecimal("10")); // 默认10元=1积分
        rule.setIntegralToMoney(new BigDecimal("100")); // 默认100积分=1元
        rule.setIntegralDeductionLimit(50); // 默认最多抵扣50%
        rule.setIntegralExpireType(0); // 默认永不过期
        rule.setIntegralExpireDays(365); // 默认有效期365天
        rule.setIntegralDescription("1. 会员消费可获得积分，积分可用于抵扣或兑换商品\n2. 积分永久有效，请放心使用");
        return rule;
    }
}