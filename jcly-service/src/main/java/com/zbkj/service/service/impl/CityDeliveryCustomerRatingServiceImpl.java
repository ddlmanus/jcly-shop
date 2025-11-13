package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.order.CityDeliveryCustomerRating;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CityDeliveryCustomerRatingDao;
import com.zbkj.service.service.CityDeliveryCustomerRatingService;
import com.zbkj.service.service.CityDeliveryOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配送员客户评价服务实现类
 * @author 荆楚粮油
 * @since 2024-01-15
 */
@Service
public class CityDeliveryCustomerRatingServiceImpl extends ServiceImpl<CityDeliveryCustomerRatingDao, CityDeliveryCustomerRating> 
        implements CityDeliveryCustomerRatingService {

    @Autowired
    private CityDeliveryOrderService cityDeliveryOrderService;

//    @Override
//    public IPage<CityDeliveryCustomerRating> getList(Integer driverId, Integer auditStatus,
//                                                    String keyword, Date startDate, Date endDate,
//                                                    PageParamRequest pageParamRequest) {
//        Page<CityDeliveryCustomerRating> page = new Page<>(pageParamRequest.getPage(), pageParamRequest.getLimit());
//
//        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(CityDeliveryCustomerRating::getIsDel, false);
//
//        if (driverId != null) {
//            wrapper.eq(CityDeliveryCustomerRating::getDriverId, driverId);
//        }
//
//        if (auditStatus != null) {
//            wrapper.eq(CityDeliveryCustomerRating::getAuditStatus, auditStatus);
//        }
//
//        if (StringUtils.isNotBlank(keyword)) {
//            wrapper.and(w -> w.like(CityDeliveryCustomerRating::getDeliveryOrderNo, keyword)
//                           .or().like(CityDeliveryCustomerRating::getCustomerName, keyword)
//                           .or().like(CityDeliveryCustomerRating::getComment, keyword));
//        }
//
//        if (startDate != null) {
//            wrapper.ge(CityDeliveryCustomerRating::getCreateTime, startDate);
//        }
//
//        if (endDate != null) {
//            wrapper.le(CityDeliveryCustomerRating::getCreateTime, endDate);
//        }
//
//        wrapper.orderByDesc(CityDeliveryCustomerRating::getCreateTime);
//
//        return baseMapper.selectPage(page, wrapper);
//    }
//
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRating(CityDeliveryCustomerRating rating) {
        // 验证配送订单是否存在
        CityDeliveryOrder order = cityDeliveryOrderService.getByOrderNo(rating.getDeliveryOrderNo());
        if (order == null) {
            throw new RuntimeException("配送订单不存在");
        }

        // 检查是否已经评价过
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDeliveryOrderNo, rating.getDeliveryOrderNo())
               .eq(CityDeliveryCustomerRating::getCustomerId, rating.getCustomerId())
               .eq(CityDeliveryCustomerRating::getIsDel, false);

        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该订单已经评价过了");
        }

        // 验证配送员是否为该订单的配送员
        if (!order.getDriverId().equals(rating.getDriverId())) {
            throw new RuntimeException("配送员信息不匹配");
        }

        // 设置默认值
        rating.setId(null);
        rating.setAuditStatus(0); // 待审核
        rating.setIsDel(false);
        rating.setCreateTime(new Date());
        rating.setUpdateTime(new Date());

        // 计算综合评分
        BigDecimal overallRating = calculateOverallRating(
            rating.getSpeedRating(),
            rating.getServiceRating(),
            rating.getQualityRating()
        );
        rating.setOverallRating(overallRating);

        return save(rating);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean auditRating(Integer ratingId, Integer auditStatus, String auditRemark, Integer auditAdminId) {
        CityDeliveryCustomerRating rating = getById(ratingId);
        if (rating == null) {
            throw new RuntimeException("评价记录不存在");
        }

        if (rating.getAuditStatus() != 0) {
            throw new RuntimeException("评价已经审核过了");
        }

        rating.setAuditStatus(auditStatus);
        rating.setAuditRemark(auditRemark);
        rating.setAuditAdminId(auditAdminId);
        rating.setAuditTime(new Date());
        rating.setUpdateTime(new Date());

        return updateById(rating);
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAuditRating(List<Integer> ratingIds, Integer auditStatus, String auditRemark, Integer auditAdminId) {
        if (ratingIds == null || ratingIds.isEmpty()) {
            throw new RuntimeException("请选择要审核的评价");
        }

        Date auditTime = new Date();
        int result = baseMapper.batchUpdateAuditStatus(ratingIds, auditStatus, auditRemark, auditAdminId, auditTime);

        return result > 0;
    }

    @Override
    public Map<String, Object> getDriverRatingStats(Integer driverId) {
        return baseMapper.getDriverRatingStats(driverId);
    }

    @Override
    public BigDecimal calculateDriverAverageRating(Integer driverId) {
        //计算配送员评价评分
        LambdaQueryWrapper<CityDeliveryCustomerRating> lambda = new LambdaQueryWrapper<>();
        lambda.eq(CityDeliveryCustomerRating::getDriverId, driverId);
        List<CityDeliveryCustomerRating> ratings = baseMapper.selectList(lambda);
        //获取总评分
        BigDecimal totalRating = ratings.stream().map(CityDeliveryCustomerRating::getOverallRating).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalCount = ratings.size();
        return totalCount > 0 ? totalRating.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    @Override
    public Map<String, BigDecimal> getDriverRatingDimensionStats(Integer driverId) {
        return baseMapper.getDriverRatingDimensionStats(driverId);
    }


    public List<CityDeliveryCustomerRating> getByDeliveryOrderNo(String deliveryOrderNo) {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDeliveryOrderNo, deliveryOrderNo)
               .eq(CityDeliveryCustomerRating::getIsDel, false)
               .orderByDesc(CityDeliveryCustomerRating::getCreateTime);
        
        return list(wrapper);
    }


    public List<CityDeliveryCustomerRating> getByDeliveryOrderNos(List<String> deliveryOrderNos) {
        if (deliveryOrderNos == null || deliveryOrderNos.isEmpty()) {
            return null;
        }
        
        return baseMapper.selectByDeliveryOrderNos(deliveryOrderNos);
    }


    public Map<String, Object> getRatingStatisticsReport(Date startDate, Date endDate) {
        return baseMapper.getRatingStatisticsReport(startDate, endDate);
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRating(Integer ratingId) {
        CityDeliveryCustomerRating rating = getById(ratingId);
        if (rating == null) {
            throw new RuntimeException("评价记录不存在");
        }
        
        rating.setIsDel(true);
        rating.setUpdateTime(new Date());
        
        return updateById(rating);
    }


    public Boolean canCustomerRate(String deliveryOrderNo, Integer customerId) {
        // 检查订单是否存在且已完成
        CityDeliveryOrder order = cityDeliveryOrderService.getByOrderNo(deliveryOrderNo);
        if (order == null) {
            return false;
        }
        
        // 检查订单状态是否为已完成
        if (order.getDeliveryStatus() != 5) { // 8表示已完成
            return false;
        }
        
        // 检查是否为该订单的客户
        if (!order.getUid().equals(customerId)) {
            return false;
        }
        
        // 检查是否已经评价过
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDeliveryOrderNo, deliveryOrderNo)
               .eq(CityDeliveryCustomerRating::getCustomerId, customerId)
               .eq(CityDeliveryCustomerRating::getIsDel, false);
        
        return baseMapper.selectCount(wrapper) == 0;
    }


    public List<CityDeliveryCustomerRating> getRecentRatings(Integer driverId, Integer limit) {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDriverId, driverId)
               .eq(CityDeliveryCustomerRating::getIsDel, false)
               .eq(CityDeliveryCustomerRating::getAuditStatus, 1)
               .orderByDesc(CityDeliveryCustomerRating::getCreateTime)
               .last("LIMIT " + (limit != null ? limit : 10));
        
        return list(wrapper);
    }

    // 实现接口中的其他方法
    @Override
    public boolean createRating(CityDeliveryCustomerRating rating) {
        return saveRating(rating);
    }

    @Override
    public List<CityDeliveryCustomerRating> getLatestRatings(Integer driverId, Integer limit) {
        return getRecentRatings(driverId, limit);
    }

    @Override
    public List<CityDeliveryCustomerRating> getRatingsByDeliveryOrderNos(List<String> deliveryOrderNos) {
        return getByDeliveryOrderNos(deliveryOrderNos);
    }

    @Override
    public BigDecimal getDriverGoodRatingRate(Integer driverId) {
        Map<String, Object> stats = getDriverRatingStats(driverId);
        if (stats == null) {
            return BigDecimal.valueOf(100);
        }
        
        Object excellentCount = stats.get("excellent_count");
        Object goodCount = stats.get("good_count");
        Object totalCount = stats.get("total_count");
        
        if (totalCount == null || ((Number) totalCount).intValue() == 0) {
            return BigDecimal.valueOf(100);
        }
        
        int excellent = excellentCount != null ? ((Number) excellentCount).intValue() : 0;
        int good = goodCount != null ? ((Number) goodCount).intValue() : 0;
        int total = ((Number) totalCount).intValue();
        
        return BigDecimal.valueOf((excellent + good) * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public List<CityDeliveryCustomerRating> getDriverPoorRatings(Integer driverId) {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDriverId, driverId)
               .lt(CityDeliveryCustomerRating::getOverallRating, new BigDecimal("2.5"))
               .eq(CityDeliveryCustomerRating::getIsDel, false)
               .eq(CityDeliveryCustomerRating::getAuditStatus, 1)
               .orderByDesc(CityDeliveryCustomerRating::getCreateTime);
        
        return list(wrapper);
    }

    @Override
    public BigDecimal calculateSystemRating(String deliveryOrderNo) {
        // 根据配送表现计算系统评分
        return BigDecimal.valueOf(4.8);
    }

    @Override
    public boolean updateFinalRating(Integer ratingId) {
        CityDeliveryCustomerRating rating = getById(ratingId);
        if (rating == null) {
            return false;
        }
        
        // 重新计算综合评分
        BigDecimal overallRating = calculateOverallRating(
            rating.getSpeedRating(),
            rating.getServiceRating(), 
            rating.getQualityRating()
        );
        
        rating.setOverallRating(overallRating);
        rating.setUpdateTime(new Date());
        
        return updateById(rating);
    }

    @Override
    public BigDecimal calculateRatingWeight(Integer customerId) {
        return BigDecimal.valueOf(1.0);
    }

    @Override
    public List<CityDeliveryCustomerRating> getPendingAuditRatings() {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getAuditStatus, 0)
               .eq(CityDeliveryCustomerRating::getIsDel, false)
               .orderByAsc(CityDeliveryCustomerRating::getCreateTime);
        
        return list(wrapper);
    }

    @Override
    public List<CityDeliveryCustomerRating> getHighQualityRatings(Integer driverId, Integer limit) {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDriverId, driverId)
               .ge(CityDeliveryCustomerRating::getOverallRating, new BigDecimal("4.5"))
               .eq(CityDeliveryCustomerRating::getIsDel, false)
               .eq(CityDeliveryCustomerRating::getAuditStatus, 1)
               .isNotNull(CityDeliveryCustomerRating::getComment)
               .ne(CityDeliveryCustomerRating::getComment, "")
               .orderByDesc(CityDeliveryCustomerRating::getOverallRating)
               .orderByDesc(CityDeliveryCustomerRating::getCreateTime)
               .last("LIMIT " + (limit != null ? limit : 10));
        
        return list(wrapper);
    }

    @Override
    public CityDeliveryCustomerRating getRatingByDeliveryOrderNo(String deliveryOrderNo) {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDeliveryOrderNo, deliveryOrderNo)
               .eq(CityDeliveryCustomerRating::getIsDel, false)
               .orderByDesc(CityDeliveryCustomerRating::getCreateTime);
        return getOne(wrapper);
    }

    @Override
    public List<CityDeliveryCustomerRating> getRatingsByDriverId(Integer driverId, Date startDate, Date endDate) {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDriverId, driverId)
               .eq(CityDeliveryCustomerRating::getIsDel, false)
               .eq(CityDeliveryCustomerRating::getAuditStatus, 1);
        
        if (startDate != null) {
            wrapper.ge(CityDeliveryCustomerRating::getCreateTime, startDate);
        }
        if (endDate != null) {
            wrapper.le(CityDeliveryCustomerRating::getCreateTime, endDate);
        }
        
        wrapper.orderByDesc(CityDeliveryCustomerRating::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean updateAuditStatus(Integer ratingId, Integer auditStatus, String auditRemark, Integer auditAdminId) {
        return auditRating(ratingId, auditStatus, auditRemark, auditAdminId);
    }

    @Override
    public boolean hasCustomerRated(String deliveryOrderNo, Integer customerId) {
        LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryCustomerRating::getDeliveryOrderNo, deliveryOrderNo)
               .eq(CityDeliveryCustomerRating::getCustomerId, customerId)
               .eq(CityDeliveryCustomerRating::getIsDel, false);
        
        return count(wrapper) > 0;
    }

    @Override
    public boolean batchProcessRatings(List<Integer> ratingIds, Integer auditStatus, String auditRemark, Integer auditAdminId) {
        return batchAuditRating(ratingIds, auditStatus, auditRemark, auditAdminId);
    }

    @Override
    public Map<String, Object> getRatingDetailWithDeliveryInfo(Integer ratingId) {
        CityDeliveryCustomerRating rating = getById(ratingId);
        if (rating == null) {
            return null;
        }
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("rating", rating);
        return detail;
    }

    @Override
    public List<Map<String, Object>> getRatingTrendData(Integer driverId, Date startDate, Date endDate) {
        return getDriverRatingTrend(driverId, startDate, endDate);
    }

    private List<Map<String, Object>> getDriverRatingTrend(Integer driverId, Date startDate, Date endDate) {
        return baseMapper.getDriverRatingTrend(driverId, startDate, endDate);
    }

    @Override
    public boolean handleComplaintRating(Integer ratingId, String handlerResult) {
        CityDeliveryCustomerRating rating = getById(ratingId);
        if (rating == null) {
            return false;
        }
        
        rating.setAdminReply(handlerResult);
        rating.setAdminReplyTime(new Date());
        rating.setComplaintHandled(true);
        rating.setUpdateTime(new Date());
        
        return updateById(rating);
    }

    @Override
    public boolean autoCreateRatingRecord(String deliveryOrderNo) {
        CityDeliveryCustomerRating rating = new CityDeliveryCustomerRating();
        rating.setDeliveryOrderNo(deliveryOrderNo);
        rating.setOverallRating(new BigDecimal("5.0"));
        rating.setServiceRating(new BigDecimal("5.0"));
        rating.setSpeedRating(new BigDecimal("5.0"));
        rating.setQualityRating(new BigDecimal("5.0"));
        rating.setComment("系统默认好评");
        rating.setAuditStatus(1);
        rating.setIsDel(false);
        rating.setCreateTime(new Date());
        rating.setUpdateTime(new Date());
        
        return save(rating);
    }

    /**
     * 计算综合评分
     */
    private BigDecimal calculateOverallRating(BigDecimal speedRating, BigDecimal serviceRating, BigDecimal qualityRating) {
        if (speedRating == null || serviceRating == null || qualityRating == null) {
            return new BigDecimal("5.0");
        }
        
        // 权重：配送速度40%，服务态度30%，配送质量30%
        BigDecimal overall = speedRating.multiply(new BigDecimal("0.4"))
                           .add(serviceRating.multiply(new BigDecimal("0.3")))
                           .add(qualityRating.multiply(new BigDecimal("0.3")));
        
        return overall.setScale(1, BigDecimal.ROUND_HALF_UP);
    }
} 