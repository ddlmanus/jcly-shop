package com.zbkj.service.service;

import com.zbkj.common.model.order.CityDeliveryDriver;

import java.util.List;
import java.util.Map;

/**
 * 同城配送调度服务接口
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
public interface CityDeliveryDispatchService {

    /**
     * 自动分配配送员
     * 
     * @param deliveryOrderNo 配送单号
     * @return 是否分配成功
     */
    Boolean autoAssignDriver(String deliveryOrderNo);

    /**
     * 手动分配配送员
     * 
     * @param deliveryOrderNo 配送单号
     * @param driverId 配送员ID
     * @return 是否分配成功
     */
    Boolean manualAssignDriver(String deliveryOrderNo, Integer driverId);

    /**
     * 重新分配配送员
     * 
     * @param deliveryOrderNo 配送单号
     * @param reason 重新分配原因
     * @return 是否分配成功
     */
    Boolean reassignDriver(String deliveryOrderNo, String reason);

    /**
     * 重新分配订单（返回详细信息）
     * 
     * @param deliveryOrderNo 配送单号
     * @param reason 重新分配原因
     * @return 分配结果详细信息
     */
    Map<String, Object> reassignOrder(String deliveryOrderNo, String reason);

    /**
     * 获取可用配送员列表
     * 
     * @param deliveryOrderNo 配送单号
     * @return 可用配送员列表
     */
    List<CityDeliveryDriver> getAvailableDriversForOrder(String deliveryOrderNo);

    /**
     * 计算配送员负载
     * 
     * @param driverId 配送员ID
     * @return 负载信息
     */
    Map<String, Object> calculateDriverLoad(Integer driverId);

    /**
     * 负载均衡分配
     * 
     * @param driverIds 配送员ID列表
     * @return 最佳配送员ID
     */
    Integer balanceLoadAssign(List<Integer> driverIds);

    /**
     * 智能调度算法
     * 
     * @param deliveryOrderNo 配送单号
     * @return 最佳配送员ID
     */
    Integer intelligentDispatch(String deliveryOrderNo);

    /**
     * 获取调度统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getDispatchStats();

    /**
     * 实时调度监控
     * 
     * @return 实时监控信息
     */
    Map<String, Object> realtimeDispatchMonitor();

    /**
     * 批量调度多个订单
     * 
     * @param deliveryOrderNos 配送单号列表
     * @return 批量调度结果
     */
    Map<String, Object> batchDispatchOrders(List<String> deliveryOrderNos);

    /**
     * 基于机器学习的智能调度
     * 
     * @param deliveryOrderNo 配送单号
     * @return 最佳配送员ID
     */
    Integer mlBasedDispatch(String deliveryOrderNo);

    /**
     * 路径优化调度
     * 
     * @param driverId 配送员ID
     * @param orderNos 订单号列表
     * @return 优化后的配送路径
     */
    List<Map<String, Object>> optimizeDeliveryRoute(Integer driverId, List<String> orderNos);

    /**
     * 预测性调度
     * 
     * @param areaId 区域ID
     * @param timeSlot 时间段
     * @return 预测调度方案
     */
    Map<String, Object> predictiveDispatch(Integer areaId, String timeSlot);

    /**
     * 动态负载调整
     * 
     * @return 调整结果
     */
    Map<String, Object> dynamicLoadAdjustment();

    /**
     * 紧急订单快速调度
     * 
     * @param deliveryOrderNo 紧急配送单号
     * @return 调度结果
     */
    Map<String, Object> emergencyDispatch(String deliveryOrderNo);

    /**
     * 多配送员协同调度
     * 
     * @param largeOrderNo 大型订单号
     * @param requiredDriverCount 需要配送员数量
     * @return 协同调度结果
     */
    Map<String, Object> collaborativeDispatch(String largeOrderNo, Integer requiredDriverCount);

    /**
     * 基于历史数据的智能预分配
     * 
     * @param driverId 配送员ID
     * @return 预分配订单列表
     */
    List<String> intelligentPreAssignment(Integer driverId);

    /**
     * 调度决策解释
     * 
     * @param deliveryOrderNo 配送单号
     * @param driverId 分配的配送员ID
     * @return 决策解释
     */
    Map<String, Object> explainDispatchDecision(String deliveryOrderNo, Integer driverId);

    /**
     * 调度性能分析
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 性能分析报告
     */
    Map<String, Object> analyzeDispatchPerformance(String startDate, String endDate);

    /**
     * 调度算法A/B测试
     * 
     * @param algorithmType 算法类型
     * @return A/B测试结果
     */
    Map<String, Object> dispatchAlgorithmABTest(String algorithmType);
} 