package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryCustomerRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 配送员客户评价Dao接口
 * @author 荆楚粮油
 * @since 2024-01-15
 */
@Mapper
public interface CityDeliveryCustomerRatingDao extends BaseMapper<CityDeliveryCustomerRating> {

    /**
     * 根据配送员ID统计评价数据
     */
    Map<String, Object> getDriverRatingStats(@Param("driverId") Integer driverId);

    /**
     * 获取配送员评价趋势数据
     */
    List<Map<String, Object>> getDriverRatingTrend(@Param("driverId") Integer driverId, 
                                                  @Param("startDate") Date startDate, 
                                                  @Param("endDate") Date endDate);

    /**
     * 计算配送员平均评分
     */
    BigDecimal calculateDriverAverageRating(@Param("driverId") Integer driverId);

    /**
     * 获取配送员各维度评分统计
     */
    Map<String, BigDecimal> getDriverRatingDimensionStats(@Param("driverId") Integer driverId);

    /**
     * 批量更新审核状态
     */
    int batchUpdateAuditStatus(@Param("ratingIds") List<Integer> ratingIds, 
                              @Param("auditStatus") Integer auditStatus,
                              @Param("auditRemark") String auditRemark,
                              @Param("auditAdminId") Integer auditAdminId,
                              @Param("auditTime") Date auditTime);

    /**
     * 获取评价统计报表
     */
    Map<String, Object> getRatingStatisticsReport(@Param("startDate") Date startDate, 
                                                 @Param("endDate") Date endDate);

    /**
     * 根据多个配送订单号查询评价
     */
    List<CityDeliveryCustomerRating> selectByDeliveryOrderNos(@Param("deliveryOrderNos") List<String> deliveryOrderNos);
} 