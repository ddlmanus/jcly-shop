package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.CityDeliveryFeeRule;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CityDeliveryFeeRuleDao;
import com.zbkj.service.service.CityDeliveryFeeRuleService;
import com.zbkj.service.service.TencentMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import com.github.pagehelper.Page;

/**
 * 同城配送费用规则服务实现类
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
@Service
public class CityDeliveryFeeRuleServiceImpl extends ServiceImpl<CityDeliveryFeeRuleDao, CityDeliveryFeeRule> implements CityDeliveryFeeRuleService {

    @Autowired
    private TencentMapService tencentMapService;

    /**
     * 费用规则分页列表
     */
    @Override
    public List<CityDeliveryFeeRule> getList(PageParamRequest pageParamRequest) {
        // 使用PageHelper进行分页
        Page<CityDeliveryFeeRule> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        QueryWrapper<CityDeliveryFeeRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", 0)
                   .orderByDesc("create_time");
        
        List<CityDeliveryFeeRule> list = list(queryWrapper);
        
        // 处理空结果
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        
        return list;
    }

    /**
     * 费用规则分页列表（返回PageInfo）
     */
    public PageInfo<CityDeliveryFeeRule> getPage(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        QueryWrapper<CityDeliveryFeeRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", false)
                   .orderByDesc("create_time");
        
        List<CityDeliveryFeeRule> list = list(queryWrapper);
        return new PageInfo<>(list);
    }

    /**
     * 根据状态获取费用规则列表
     */
    public List<CityDeliveryFeeRule> getByStatus(Integer status) {
        QueryWrapper<CityDeliveryFeeRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", false);
        
        if (ObjectUtil.isNotNull(status)) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    /**
     * 新增费用规则
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(CityDeliveryFeeRule feeRule) {
        try {
            // 检查规则名称是否重复
            QueryWrapper<CityDeliveryFeeRule> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rule_name", feeRule.getRuleName())
                       .eq("is_del", false);
            CityDeliveryFeeRule existRule = getOne(queryWrapper);
            if (ObjectUtil.isNotNull(existRule)) {
                throw new CrmebException("规则名称已存在");
            }

            // 设置默认值
            if (ObjectUtil.isNull(feeRule.getStatus())) {
                feeRule.setStatus(1); // 默认启用
            }
            if (ObjectUtil.isNull(feeRule.getBaseFee())) {
                feeRule.setBaseFee(BigDecimal.ZERO);
            }
            if (ObjectUtil.isNull(feeRule.getBaseDistance())) {
                feeRule.setBaseDistance(BigDecimal.valueOf(3.0)); // 默认3公里内
            }
            if (ObjectUtil.isNull(feeRule.getAdditionalFeePerKm())) {
                feeRule.setAdditionalFeePerKm(BigDecimal.valueOf(2.0)); // 默认每公里2元
            }
            
            feeRule.setCreateTime(new Date());
            feeRule.setUpdateTime(new Date());
            feeRule.setIsDel(0); // 修复：设置为0（未删除）

            return save(feeRule);
        } catch (Exception e) {
            throw new CrmebException("创建费用规则失败：" + e.getMessage());
        }
    }

    /**
     * 新增费用规则（兼容方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createFeeRule(CityDeliveryFeeRule feeRule) {
        return create(feeRule);
    }

    /**
     * 更新费用规则
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFeeRule(CityDeliveryFeeRule feeRule) {
        try {
            CityDeliveryFeeRule existRule = getById(feeRule.getId());
            if (ObjectUtil.isNull(existRule)) {
                throw new CrmebException("费用规则不存在");
            }

            // 检查规则名称是否重复（排除自己）
            QueryWrapper<CityDeliveryFeeRule> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rule_name", feeRule.getRuleName())
                       .eq("is_del", false)
                       .ne("id", feeRule.getId());
            CityDeliveryFeeRule duplicateRule = getOne(queryWrapper);
            if (ObjectUtil.isNotNull(duplicateRule)) {
                throw new CrmebException("规则名称已存在");
            }

            feeRule.setUpdateTime(new Date());
            return updateById(feeRule);
        } catch (Exception e) {
            throw new CrmebException("更新费用规则失败：" + e.getMessage());
        }
    }

    /**
     * 删除费用规则
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        try {
            CityDeliveryFeeRule rule = getById(id);
            if (ObjectUtil.isNull(rule)) {
                throw new CrmebException("费用规则不存在");
            }

            // 软删除
            rule.setIsDel(1);
            rule.setUpdateTime(new Date());
            
            return updateById(rule);
        } catch (Exception e) {
            throw new CrmebException("删除费用规则失败：" + e.getMessage());
        }
    }

    /**
     * 删除费用规则（兼容方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFeeRule(Integer id) {
        return delete(id);
    }

    /**
     * 更新规则状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Integer id, Integer status) {
        try {
            CityDeliveryFeeRule rule = getById(id);
            if (ObjectUtil.isNull(rule)) {
                throw new CrmebException("费用规则不存在");
            }

            rule.setStatus(status);
            rule.setUpdateTime(new Date());
            
            return updateById(rule);
        } catch (Exception e) {
            throw new CrmebException("更新规则状态失败：" + e.getMessage());
        }
    }

    /**
     * 根据区域获取费用规则
     */
    @Override
    public CityDeliveryFeeRule getRuleByArea(Integer areaId) {
        return baseMapper.getRuleByArea(areaId);
    }

    /**
     * 获取默认费用规则
     */
    @Override
    public CityDeliveryFeeRule getDefaultRule() {
        return baseMapper.getDefaultRule();
    }

    /**
     * 批量更新费用规则状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateStatus(List<Integer> feeRuleIds, Integer status) {
        try {
            if (CollUtil.isEmpty(feeRuleIds)) {
                return false;
            }
            
            int result = baseMapper.batchUpdateRuleStatus(feeRuleIds, status);
            return result > 0;
        } catch (Exception e) {
            throw new CrmebException("批量更新规则状态失败：" + e.getMessage());
        }
    }

    /**
     * 计算配送费用（使用地址）
     */
    public BigDecimal calculateFee(String fromAddress, String toAddress, Integer deliveryType) {
        try {
            // 获取启用的费用规则
            List<CityDeliveryFeeRule> enabledRules = getByStatus(1);
            if (CollUtil.isEmpty(enabledRules)) {
                // 如果没有规则，返回默认费用
                return getDefaultFee(deliveryType);
            }

            // 选择最适合的规则（按优先级排序）
            CityDeliveryFeeRule rule = enabledRules.get(0);

            // 计算距离
            BigDecimal distance = calculateDistance(fromAddress, toAddress);
            if (ObjectUtil.isNull(distance)) {
                return getDefaultFee(deliveryType);
            }

            // 计算基础费用
            BigDecimal totalFee = calculateBaseFee(rule, distance, deliveryType);

            // 应用时间、天气、节假日等系数
            totalFee = applyMultipliers(totalFee, rule);

            // 确保费用不低于最小值
            if (ObjectUtil.isNotNull(rule.getMinFee()) && totalFee.compareTo(rule.getMinFee()) < 0) {
                totalFee = rule.getMinFee();
            }

            // 确保费用不超过最大值
            if (ObjectUtil.isNotNull(rule.getMaxFee()) && totalFee.compareTo(rule.getMaxFee()) > 0) {
                totalFee = rule.getMaxFee();
            }

            return totalFee.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            System.err.println("计算配送费用失败：" + e.getMessage());
            return getDefaultFee(deliveryType);
        }
    }

    /**
     * 计算配送费用（接口方法）
     */
    @Override
    public Map<String, Object> calculateFee(Integer feeRuleId, BigDecimal distance, Integer deliveryType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 根据规则ID获取费用规则
            CityDeliveryFeeRule rule = null;
            if (ObjectUtil.isNotNull(feeRuleId)) {
                rule = getById(feeRuleId);
                if (ObjectUtil.isNull(rule)) {
                    throw new CrmebException("费用规则不存在");
                }
            } else {
                // 如果没有指定规则ID，使用第一个启用的规则
                List<CityDeliveryFeeRule> enabledRules = getByStatus(1);
                if (CollUtil.isNotEmpty(enabledRules)) {
                    rule = enabledRules.get(0);
                }
            }

            if (ObjectUtil.isNull(rule)) {
                // 使用默认计算
                BigDecimal defaultFee = getDefaultFeeByDistance(distance, deliveryType);
                result.put("totalFee", defaultFee);
                result.put("baseFee", defaultFee);
                result.put("distance", distance);
                return result;
            }

            // 计算基础费用
            BigDecimal baseFee = calculateBaseFee(rule, distance, deliveryType);
            result.put("baseFee", baseFee);
            result.put("distance", distance);

            // 应用时间、天气、节假日等系数
            BigDecimal totalFee = applyMultipliers(baseFee, rule);

            // 确保费用不低于最小值
            if (ObjectUtil.isNotNull(rule.getMinFee()) && totalFee.compareTo(rule.getMinFee()) < 0) {
                totalFee = rule.getMinFee();
            }

            // 确保费用不超过最大值
            if (ObjectUtil.isNotNull(rule.getMaxFee()) && totalFee.compareTo(rule.getMaxFee()) > 0) {
                totalFee = rule.getMaxFee();
            }

            result.put("totalFee", totalFee.setScale(2, RoundingMode.HALF_UP));
            return result;
        } catch (Exception e) {
            System.err.println("计算配送费用失败：" + e.getMessage());
            BigDecimal defaultFee = getDefaultFeeByDistance(distance, deliveryType);
            result.put("totalFee", defaultFee);
            result.put("baseFee", defaultFee);
            result.put("distance", distance);
            return result;
        }
    }

    /**
     * 根据距离计算费用
     */
    @Override
    public BigDecimal calculateFeeByDistance(BigDecimal distance, Integer deliveryType) {
        try {
            List<CityDeliveryFeeRule> enabledRules = getByStatus(1);
            if (CollUtil.isEmpty(enabledRules)) {
                return getDefaultFeeByDistance(distance, deliveryType);
            }

            CityDeliveryFeeRule rule = enabledRules.get(0);
            BigDecimal totalFee = calculateBaseFee(rule, distance, deliveryType);
            totalFee = applyMultipliers(totalFee, rule);

            // 应用费用限制
            if (ObjectUtil.isNotNull(rule.getMinFee()) && totalFee.compareTo(rule.getMinFee()) < 0) {
                totalFee = rule.getMinFee();
            }
            if (ObjectUtil.isNotNull(rule.getMaxFee()) && totalFee.compareTo(rule.getMaxFee()) > 0) {
                totalFee = rule.getMaxFee();
            }

            return totalFee.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            System.err.println("根据距离计算配送费用失败：" + e.getMessage());
            return getDefaultFeeByDistance(distance, deliveryType);
        }
    }

    /**
     * 根据重量和体积计算附加费用
     */
    @Override
    public BigDecimal calculateExtraFee(BigDecimal weight, BigDecimal volume) {
        try {
            List<CityDeliveryFeeRule> enabledRules = getByStatus(1);
            if (CollUtil.isEmpty(enabledRules)) {
                return BigDecimal.ZERO;
            }

            CityDeliveryFeeRule rule = enabledRules.get(0);
            BigDecimal extraFee = BigDecimal.ZERO;

            // 计算重量费用（简化实现：基于JSON规则）
            if (ObjectUtil.isNotNull(weight) && weight.compareTo(BigDecimal.ZERO) > 0) {
                // 简化为固定费率：每公斤1元
                BigDecimal weightFee = weight.multiply(BigDecimal.ONE);
                extraFee = extraFee.add(weightFee);
            }

            // 计算体积费用（简化实现：基于JSON规则）
            if (ObjectUtil.isNotNull(volume) && volume.compareTo(BigDecimal.ZERO) > 0) {
                // 简化为固定费率：每立方米2元
                BigDecimal volumeFee = volume.multiply(BigDecimal.valueOf(2));
                extraFee = extraFee.add(volumeFee);
            }

            return extraFee.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            System.err.println("计算附加费用失败：" + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取所有启用的费用规则
     */
    @Override
    public List<CityDeliveryFeeRule> getEnabledRules() {
        return getByStatus(1);
    }

    /**
     * 根据配送类型获取费用规则
     */
    @Override
    public List<CityDeliveryFeeRule> getRulesByDeliveryType(Integer deliveryType) {
        QueryWrapper<CityDeliveryFeeRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", false)
                   .eq("status", 1);

        // deliveryType 字段已移除，所有规则都适用于所有配送类型

        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    /**
     * 预览费用计算
     */
    @Override
    public Map<String, Object> previewFee(String fromAddress, String toAddress, Integer deliveryType, 
                                         BigDecimal weight, BigDecimal volume) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 计算距离
            BigDecimal distance = calculateDistance(fromAddress, toAddress);
            result.put("distance", distance);

            // 计算基础费用
            BigDecimal baseFee = calculateFee(fromAddress, toAddress, deliveryType);
            result.put("baseFee", baseFee);

            // 计算附加费用
            BigDecimal extraFee = calculateExtraFee(weight, volume);
            result.put("extraFee", extraFee);

            // 计算总费用
            BigDecimal totalFee = baseFee.add(extraFee);
            result.put("totalFee", totalFee);

            // 费用明细
            Map<String, BigDecimal> feeDetail = new HashMap<>();
            feeDetail.put("基础配送费", baseFee);
            if (extraFee.compareTo(BigDecimal.ZERO) > 0) {
                feeDetail.put("重量体积费", extraFee);
            }
            result.put("feeDetail", feeDetail);

        } catch (Exception e) {
            System.err.println("预览费用计算失败：" + e.getMessage());
            result.put("error", "计算失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 详细费用计算
     */
    @Override
    public Map<String, Object> calculateDetailedFee(Integer feeRuleId, BigDecimal distance, BigDecimal weight, 
                                                   BigDecimal volume, Integer deliveryType, Boolean isNightTime, 
                                                   Boolean isBadWeather, Boolean isHoliday, Integer urgentLevel) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 根据规则ID获取费用规则
            CityDeliveryFeeRule rule = null;
            if (ObjectUtil.isNotNull(feeRuleId)) {
                rule = getById(feeRuleId);
                if (ObjectUtil.isNull(rule)) {
                    throw new CrmebException("费用规则不存在");
                }
            } else {
                // 如果没有指定规则ID，使用第一个启用的规则
                List<CityDeliveryFeeRule> enabledRules = getByStatus(1);
                if (CollUtil.isNotEmpty(enabledRules)) {
                    rule = enabledRules.get(0);
                }
            }

            if (ObjectUtil.isNull(rule)) {
                // 使用默认计算
                BigDecimal defaultFee = getDefaultFeeByDistance(distance, deliveryType);
                result.put("totalFee", defaultFee);
                result.put("baseFee", defaultFee);
                result.put("extraFee", BigDecimal.ZERO);
                result.put("multiplierFee", BigDecimal.ZERO);
                result.put("distance", distance);
                return result;
            }

            // 计算基础费用
            BigDecimal baseFee = calculateBaseFee(rule, distance, deliveryType);
            result.put("baseFee", baseFee);
            result.put("distance", distance);

            // 计算附加费用（重量体积）- 简化实现
            BigDecimal extraFee = calculateExtraFee(weight, volume);
            result.put("extraFee", extraFee);

            // 计算系数费用
            BigDecimal multiplierFee = BigDecimal.ZERO;
            BigDecimal currentFee = baseFee.add(extraFee);
            
            // 夜间配送费用
            if (Boolean.TRUE.equals(isNightTime) && ObjectUtil.isNotNull(rule.getNightDeliveryFee())) {
                multiplierFee = multiplierFee.add(rule.getNightDeliveryFee());
            }
            
            // 雨天配送费用
            if (Boolean.TRUE.equals(isBadWeather) && ObjectUtil.isNotNull(rule.getRainyDayFee())) {
                multiplierFee = multiplierFee.add(rule.getRainyDayFee());
            }
            
            // 节假日配送费用
            if (Boolean.TRUE.equals(isHoliday) && ObjectUtil.isNotNull(rule.getHolidayDeliveryFee())) {
                multiplierFee = multiplierFee.add(rule.getHolidayDeliveryFee());
            }
            
            // 紧急等级加费
            if (ObjectUtil.isNotNull(urgentLevel) && urgentLevel > 0) {
                BigDecimal urgentExtra = currentFee.multiply(BigDecimal.valueOf(urgentLevel * 0.1)); // 每级加10%
                multiplierFee = multiplierFee.add(urgentExtra);
            }
            
            result.put("multiplierFee", multiplierFee);

            // 计算总费用
            BigDecimal totalFee = baseFee.add(extraFee).add(multiplierFee);
            
            // 应用费用限制
            if (ObjectUtil.isNotNull(rule.getMinFee()) && totalFee.compareTo(rule.getMinFee()) < 0) {
                totalFee = rule.getMinFee();
            }
            if (ObjectUtil.isNotNull(rule.getMaxFee()) && totalFee.compareTo(rule.getMaxFee()) > 0) {
                totalFee = rule.getMaxFee();
            }
            
            result.put("totalFee", totalFee.setScale(2, RoundingMode.HALF_UP));

            // 费用明细
            Map<String, BigDecimal> feeDetail = new HashMap<>();
            feeDetail.put("基础配送费", baseFee);
            if (extraFee.compareTo(BigDecimal.ZERO) > 0) {
                feeDetail.put("重量体积费", extraFee);
            }
            if (multiplierFee.compareTo(BigDecimal.ZERO) > 0) {
                feeDetail.put("时段天气加费", multiplierFee);
            }
            result.put("feeDetail", feeDetail);

            // 规则信息
            result.put("ruleId", rule.getId());
            result.put("ruleName", rule.getRuleName());

        } catch (Exception e) {
            System.err.println("详细费用计算失败：" + e.getMessage());
            result.put("error", "计算失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 详细费用计算（接受地址参数）
     */
    @Override
    public Map<String, Object> calculateDetailedFee(String pickupAddress, String deliveryAddress, 
                                                   Integer deliveryType, BigDecimal weight, BigDecimal volume) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 计算距离
            BigDecimal distance = calculateDistance(pickupAddress, deliveryAddress);
            if (ObjectUtil.isNull(distance)) {
                throw new CrmebException("无法计算地址间距离");
            }
            
            // 使用第一个启用的规则
            List<CityDeliveryFeeRule> enabledRules = getByStatus(1);
            if (CollUtil.isEmpty(enabledRules)) {
                // 使用默认计算
                BigDecimal defaultFee = getDefaultFeeByDistance(distance, deliveryType);
                result.put("totalFee", defaultFee);
                result.put("baseFee", defaultFee);
                result.put("extraFee", BigDecimal.ZERO);
                result.put("distance", distance);
                return result;
            }
            
            CityDeliveryFeeRule rule = enabledRules.get(0);
            
            // 计算基础费用
            BigDecimal baseFee = calculateBaseFee(rule, distance, deliveryType);
            result.put("baseFee", baseFee);
            result.put("distance", distance);

            // 计算附加费用（重量体积）
            BigDecimal extraFee = calculateExtraFee(weight, volume);
            result.put("extraFee", extraFee);

            // 计算系数费用（使用当前时间判断）
            BigDecimal multiplierFee = BigDecimal.ZERO;
            
            // 夜间配送费用
            if (isNightTime() && ObjectUtil.isNotNull(rule.getNightDeliveryFee())) {
                multiplierFee = multiplierFee.add(rule.getNightDeliveryFee());
            }
            
            // 节假日配送费用
            if (isHoliday() && ObjectUtil.isNotNull(rule.getHolidayDeliveryFee())) {
                multiplierFee = multiplierFee.add(rule.getHolidayDeliveryFee());
            }
            
            // 雨天配送费用
            if (isBadWeather() && ObjectUtil.isNotNull(rule.getRainyDayFee())) {
                multiplierFee = multiplierFee.add(rule.getRainyDayFee());
            }
            
            result.put("multiplierFee", multiplierFee);

            // 计算总费用
            BigDecimal totalFee = baseFee.add(extraFee).add(multiplierFee);
            
            // 应用费用限制
            if (ObjectUtil.isNotNull(rule.getMinFee()) && totalFee.compareTo(rule.getMinFee()) < 0) {
                totalFee = rule.getMinFee();
            }
            if (ObjectUtil.isNotNull(rule.getMaxFee()) && totalFee.compareTo(rule.getMaxFee()) > 0) {
                totalFee = rule.getMaxFee();
            }
            
            result.put("totalFee", totalFee.setScale(2, RoundingMode.HALF_UP));

            // 费用明细
            Map<String, BigDecimal> feeDetail = new HashMap<>();
            feeDetail.put("基础配送费", baseFee);
            if (extraFee.compareTo(BigDecimal.ZERO) > 0) {
                feeDetail.put("重量体积费", extraFee);
            }
            if (multiplierFee.compareTo(BigDecimal.ZERO) > 0) {
                feeDetail.put("时段天气加费", multiplierFee);
            }
            result.put("feeDetail", feeDetail);

            // 规则信息
            result.put("ruleId", rule.getId());
            result.put("ruleName", rule.getRuleName());

        } catch (Exception e) {
            System.err.println("详细费用计算失败：" + e.getMessage());
            result.put("error", "计算失败：" + e.getMessage());
        }
        
        return result;
    }

    // ========== 私有方法 ==========

    /**
     * 计算两地址间距离
     */
    private BigDecimal calculateDistance(String fromAddress, String toAddress) {
        try {
            if (ObjectUtil.isNotNull(tencentMapService)) {
                return tencentMapService.calculateDistance(fromAddress, toAddress);
            } else {
                // 如果地图服务不可用，返回默认距离
                return BigDecimal.valueOf(5.0);
            }
        } catch (Exception e) {
            System.err.println("计算距离失败：" + e.getMessage());
            return BigDecimal.valueOf(5.0);
        }
    }

    /**
     * 计算基础费用
     */
    private BigDecimal calculateBaseFee(CityDeliveryFeeRule rule, BigDecimal distance, Integer deliveryType) {
        BigDecimal baseFee = rule.getBaseFee();
        
        // 超出基础距离的费用
        if (distance.compareTo(rule.getBaseDistance()) > 0) {
            BigDecimal extraDistance = distance.subtract(rule.getBaseDistance());
            BigDecimal extraFee = extraDistance.multiply(rule.getAdditionalFeePerKm());
            baseFee = baseFee.add(extraFee);
        }

        // 根据配送类型调整费用
        if (ObjectUtil.isNotNull(deliveryType)) {
            switch (deliveryType) {
                case 1: // 即时配送
                    baseFee = baseFee.multiply(BigDecimal.valueOf(1.5));
                    break;
                case 2: // 预约配送
                    // 无额外费用
                    break;
                case 3: // 次日达
                    baseFee = baseFee.multiply(BigDecimal.valueOf(0.8));
                    break;
            }
        }

        return baseFee;
    }

    /**
     * 应用各种系数（简化实现）
     */
    private BigDecimal applyMultipliers(BigDecimal baseFee, CityDeliveryFeeRule rule) {
        BigDecimal totalFee = baseFee;

        // 夜间配送费用
        if (isNightTime() && ObjectUtil.isNotNull(rule.getNightDeliveryFee())) {
            totalFee = totalFee.add(rule.getNightDeliveryFee());
        }

        // 节假日配送费用
        if (isHoliday() && ObjectUtil.isNotNull(rule.getHolidayDeliveryFee())) {
            totalFee = totalFee.add(rule.getHolidayDeliveryFee());
        }

        // 雨天配送费用
        if (isBadWeather() && ObjectUtil.isNotNull(rule.getRainyDayFee())) {
            totalFee = totalFee.add(rule.getRainyDayFee());
        }

        return totalFee;
    }

    /**
     * 获取默认费用
     */
    private BigDecimal getDefaultFee(Integer deliveryType) {
        BigDecimal defaultFee = BigDecimal.valueOf(10.0); // 默认基础费用

        if (ObjectUtil.isNotNull(deliveryType)) {
            switch (deliveryType) {
                case 1: // 即时配送
                    defaultFee = BigDecimal.valueOf(15.0);
                    break;
                case 2: // 预约配送
                    defaultFee = BigDecimal.valueOf(10.0);
                    break;
                case 3: // 次日达
                    defaultFee = BigDecimal.valueOf(8.0);
                    break;
            }
        }

        return defaultFee;
    }

    /**
     * 根据距离获取默认费用
     */
    private BigDecimal getDefaultFeeByDistance(BigDecimal distance, Integer deliveryType) {
        BigDecimal baseFee = getDefaultFee(deliveryType);
        
        if (ObjectUtil.isNotNull(distance) && distance.compareTo(BigDecimal.valueOf(3.0)) > 0) {
            BigDecimal extraDistance = distance.subtract(BigDecimal.valueOf(3.0));
            BigDecimal extraFee = extraDistance.multiply(BigDecimal.valueOf(2.0));
            baseFee = baseFee.add(extraFee);
        }

        return baseFee;
    }

    /**
     * 判断是否为夜间时间
     */
    private Boolean isNightTime() {
        int hour = DateUtil.hour(new Date(), true);
        return hour >= 22 || hour <= 6;
    }

    /**
     * 判断是否为恶劣天气（简化实现）
     */
    private Boolean isBadWeather() {
        // 这里可以接入天气API
        return false;
    }

    /**
     * 判断是否为节假日（简化实现）
     */
    private Boolean isHoliday() {
        // 这里可以接入节假日API
        return false;
    }
} 