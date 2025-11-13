package com.zbkj.service.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单统计服务接口
 * 扩展OrderService，提供数据看板所需的统计功能
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
public interface OrderStatisticsService {

    /**
     * 获取总销售额（已支付订单）
     * @param merchantId 商户ID，null表示全部
     * @return 总销售额
     */
    BigDecimal getTotalSales(Integer merchantId);

    /**
     * 获取总订单数（已支付订单）
     * @param merchantId 商户ID，null表示全部
     * @return 总订单数
     */
    Integer getTotalOrderCount(Integer merchantId);

    /**
     * 获取指定日期范围内的销售额
     * @param merchantId 商户ID，null表示全部
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 销售额
     */
    BigDecimal getSalesByDateRange(Integer merchantId, String startDate, String endDate);

    /**
     * 获取指定日期范围内的订单数量
     * @param merchantId 商户ID，null表示全部
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 订单数量
     */
    Integer getOrderCountByDateRange(Integer merchantId, String startDate, String endDate);

    /**
     * 获取地区销售数据分布
     * @param merchantId 商户ID，null表示全部
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 地区销售数据列表
     */
    List<Map<String, Object>> getRegionSalesData(Integer merchantId, String startDate, String endDate);

    /**
     * 获取商品分类销量排行
     * @param merchantId 商户ID，null表示全部
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @param limit 排行数量
     * @return 分类排行数据
     */
    List<Map<String, Object>> getCategoryRanking(Integer merchantId, String startDate, String endDate, Integer limit);

    /**
     * 获取分销商销量/销售额排行
     * @param merchantId 商户ID，null表示全部
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @param limit 排行数量
     * @return 分销商排行数据
     */
    List<Map<String, Object>> getDistributorRanking(Integer merchantId, String startDate, String endDate, Integer limit);

    /**
     * 获取月度销售趋势数据
     * @param merchantId 商户ID，null表示全部
     * @param year 年份
     * @return 月度趋势数据
     */
    List<Map<String, Object>> getMonthlySalesTrend(Integer merchantId, Integer year);

    /**
     * 获取订单状态分布
     * @param merchantId 商户ID，null表示全部
     * @return 订单状态分布数据
     */
    Map<String, Integer> getOrderStatusDistribution(Integer merchantId);

    /**
     * 获取支付方式分布
     * @param merchantId 商户ID，null表示全部
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 支付方式分布数据
     */
    List<Map<String, Object>> getPaymentMethodDistribution(Integer merchantId, String startDate, String endDate);

    /**
     * 获取客单价统计
     * @param merchantId 商户ID，null表示全部
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 客单价数据
     */
    Map<String, Object> getAverageOrderValue(Integer merchantId, String startDate, String endDate);
}