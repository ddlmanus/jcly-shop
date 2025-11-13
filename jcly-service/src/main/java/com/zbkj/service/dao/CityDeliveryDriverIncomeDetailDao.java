package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryDriverIncomeDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 配送员收入明细Dao接口
 * @author 荆楚粮油
 * @since 2024-01-15
 */
@Mapper
public interface CityDeliveryDriverIncomeDetailDao extends BaseMapper<CityDeliveryDriverIncomeDetail> {

    /**
     * 根据配送员ID统计收入
     */
    Map<String, Object> getDriverIncomeStats(@Param("driverId") Integer driverId, 
                                           @Param("startDate") Date startDate, 
                                           @Param("endDate") Date endDate);

    /**
     * 获取配送员收入趋势数据
     */
    List<Map<String, Object>> getDriverIncomeTrend(@Param("driverId") Integer driverId, 
                                                  @Param("startDate") Date startDate, 
                                                  @Param("endDate") Date endDate);

    /**
     * 获取配送员排行榜数据
     */
    List<Map<String, Object>> getDriverRankingList(@Param("startDate") Date startDate, 
                                                  @Param("endDate") Date endDate, 
                                                  @Param("limit") Integer limit);

    /**
     * 批量更新结算状态
     */
    int batchUpdateSettlementStatus(@Param("ids") List<Integer> ids, 
                                   @Param("status") Integer status, 
                                   @Param("settlementTime") Date settlementTime);

    /**
     * 计算配送员平均评分
     */
    BigDecimal calculateDriverAverageRating(@Param("driverId") Integer driverId);

    /**
     * 获取收入明细统计报表
     */
    Map<String, Object> getIncomeStatisticsReport(@Param("startDate") Date startDate, 
                                                 @Param("endDate") Date endDate);

    /**
     * 根据配送订单号列表批量查询收入明细
     */
    List<CityDeliveryDriverIncomeDetail> selectByDeliveryOrderNos(@Param("deliveryOrderNos") List<String> deliveryOrderNos);
} 