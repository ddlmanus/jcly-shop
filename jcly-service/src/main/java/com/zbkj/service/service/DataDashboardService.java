package com.zbkj.service.service;

import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Map;

/**
 * 数据看板服务接口
 * 提供荆楚粮油云数据平台的统计分析功能
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
public interface DataDashboardService {

    /**
     * 获取顶部统计数据概览
     * @param year 年份（必填）
     * @param month 月份（必填，1-12）
     * @return 统计数据
     */
    JSONObject getOverviewData(Integer year, Integer month);

    /**
     * 获取销售额/订单量全年统计
     * @param year 年份
     * @param dataType 数据类型（sales销售额、orders订单量）
     * @return 年度销售数据
     */
    JSONObject getYearlySalesData(Integer year, String dataType);

    /**
     * 获取新增用户全年统计
     * @param year 年份
     * @return 年度用户数据
     */
    JSONObject getYearlyUserData(Integer year);

    /**
     * 获取地区销售数据分布
     * @param year 年份
     * @param month 月份
     * @param regionCode 区域代码（可选，不传则查询所有省份，传省份代码查询下级市，传市代码查询下级区）
     * @param statisticsType 统计类型（sales销售额、orders订单量、users用户量、newUsers新增用户量、merchants商户数）
     * @return 地区销售数据列表
     */
    JSONObject getRegionSalesData(Integer year, Integer month, String regionCode, String statisticsType);

    /**
     * 获取商品品牌销量排行
     * @param year 年份
     * @param limit 排行数量
     * @return 分类排行数据
     */
    JSONObject getCategoryRanking(Integer year, Integer limit);

    /**
     * 获取分销商销量/销售额排行
     * @param year 年份
     * @param month 月份（可选）
     * @param limit 排行数量（可选，不传则返回所有商户）
     * @param dataType 数据类型（sales销售额、orders订单量）
     * @return 分销商排行数据
     */
    JSONObject getDistributorRanking(Integer year, Integer month, Integer limit, String dataType);

    /**
     * 获取所有可用省份列表（用于下拉选择）
     * @return 省份列表
     */
    List<Map<String, Object>> getAvailableProvinces();


}