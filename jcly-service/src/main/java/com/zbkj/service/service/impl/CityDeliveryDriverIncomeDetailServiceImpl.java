package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.order.CityDeliveryDriverIncomeDetail;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.model.order.CityDeliveryFeeRule;
import com.zbkj.common.model.order.CityDeliveryDriver;
import com.zbkj.service.dao.CityDeliveryDriverIncomeDetailDao;
import com.zbkj.service.service.CityDeliveryDriverIncomeDetailService;
import com.zbkj.service.service.CityDeliveryOrderService;
import com.zbkj.service.service.CityDeliveryFeeRuleService;
import com.zbkj.service.service.CityDeliveryDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 配送员收入明细服务实现类
 * @author 荆楚粮油
 * @since 2024-01-15
 */
@Service
public class CityDeliveryDriverIncomeDetailServiceImpl extends ServiceImpl<CityDeliveryDriverIncomeDetailDao, CityDeliveryDriverIncomeDetail> 
        implements CityDeliveryDriverIncomeDetailService {

    @Autowired
    private CityDeliveryOrderService cityDeliveryOrderService;
    
    @Autowired
    private CityDeliveryFeeRuleService cityDeliveryFeeRuleService;
    
    @Autowired
    private CityDeliveryDriverService cityDeliveryDriverService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createIncomeDetail(CityDeliveryDriverIncomeDetail incomeDetail) {
        // 验证必要字段
        if (incomeDetail.getDriverId() == null || incomeDetail.getDeliveryOrderNo() == null) {
            throw new IllegalArgumentException("配送员ID和配送订单号不能为空");
        }

        // 检查是否已存在记录
        LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getDeliveryOrderNo, incomeDetail.getDeliveryOrderNo());
        if (this.getOne(wrapper) != null) {
            throw new IllegalStateException("该配送订单已存在收入明细记录");
        }

        // 计算收入明细
        calculateIncomeDetail(incomeDetail);

        // 保存记录
        return this.save(incomeDetail);
    }

    @Override
    public CityDeliveryDriverIncomeDetail getByDeliveryOrderNo(String deliveryOrderNo) {
        LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getDeliveryOrderNo, deliveryOrderNo);
        wrapper.eq(CityDeliveryDriverIncomeDetail::getIsDel, false);
        return this.getOne(wrapper);
    }

    @Override
    public List<CityDeliveryDriverIncomeDetail> getByDriverId(Integer driverId, Date startDate, Date endDate) {
        LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getDriverId, driverId);
        wrapper.eq(CityDeliveryDriverIncomeDetail::getIsDel, false);
        
        if (startDate != null) {
            wrapper.ge(CityDeliveryDriverIncomeDetail::getCompletedTime, startDate);
        }
        if (endDate != null) {
            wrapper.le(CityDeliveryDriverIncomeDetail::getCompletedTime, endDate);
        }
        
        wrapper.orderByDesc(CityDeliveryDriverIncomeDetail::getCompletedTime);
        return this.list(wrapper);
    }

    @Override
    public BigDecimal calculateTotalIncome(Integer driverId, Date startDate, Date endDate) {
        List<CityDeliveryDriverIncomeDetail> incomeList = getByDriverId(driverId, startDate, endDate);
        return incomeList.stream()
                .filter(income -> income.getSettlementStatus() != null && income.getSettlementStatus() != 9) // 排除已取消的
                .map(CityDeliveryDriverIncomeDetail::getDriverIncome)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateMonthlyIncome(Integer driverId, Integer year, Integer month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.MONTH, 1);
        Date endDate = cal.getTime();
        
        return calculateTotalIncome(driverId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getIncomeStats(Integer driverId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总收入
        BigDecimal totalIncome = calculateTotalIncome(driverId, null, null);
        stats.put("totalIncome", totalIncome);
        
        // 本月收入
        LocalDate now = LocalDate.now();
        BigDecimal monthlyIncome = calculateMonthlyIncome(driverId, now.getYear(), now.getMonthValue());
        stats.put("monthlyIncome", monthlyIncome);
        
        // 今日收入
        Date today = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date tomorrow = Date.from(now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        BigDecimal todayIncome = calculateTotalIncome(driverId, today, tomorrow);
        stats.put("todayIncome", todayIncome);
        
        // 订单统计
        LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getDriverId, driverId);
        wrapper.eq(CityDeliveryDriverIncomeDetail::getIsDel, false);
        
        long totalOrders = this.count(wrapper);
        stats.put("totalOrders", totalOrders);
        
        // 结算统计
        wrapper.eq(CityDeliveryDriverIncomeDetail::getSettlementStatus, 1);
        long settledOrders = this.count(wrapper);
        stats.put("settledOrders", settledOrders);
        
        wrapper.clear();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getDriverId, driverId);
        wrapper.eq(CityDeliveryDriverIncomeDetail::getIsDel, false);
        wrapper.eq(CityDeliveryDriverIncomeDetail::getSettlementStatus, 0);
        long unsettledOrders = this.count(wrapper);
        stats.put("unsettledOrders", unsettledOrders);
        
        // 平均收入
        if (totalOrders > 0) {
            stats.put("averageIncome", totalIncome.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP));
        } else {
            stats.put("averageIncome", BigDecimal.ZERO);
        }
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSettlement(List<Integer> incomeDetailIds) {
        if (incomeDetailIds == null || incomeDetailIds.isEmpty()) {
            return false;
        }
        
        LambdaUpdateWrapper<CityDeliveryDriverIncomeDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CityDeliveryDriverIncomeDetail::getId, incomeDetailIds);
        updateWrapper.eq(CityDeliveryDriverIncomeDetail::getSettlementStatus, 0); // 只更新未结算的
        updateWrapper.set(CityDeliveryDriverIncomeDetail::getSettlementStatus, 1);
        updateWrapper.set(CityDeliveryDriverIncomeDetail::getSettlementTime, new Date());
        
        return this.update(updateWrapper);
    }

    @Override
    public boolean updateSettlementStatus(Integer id, Integer status) {
        LambdaUpdateWrapper<CityDeliveryDriverIncomeDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CityDeliveryDriverIncomeDetail::getId, id);
        updateWrapper.set(CityDeliveryDriverIncomeDetail::getSettlementStatus, status);
        
        if (status == 1) { // 已结算
            updateWrapper.set(CityDeliveryDriverIncomeDetail::getSettlementTime, new Date());
        }
        
        return this.update(updateWrapper);
    }

    @Override
    public List<CityDeliveryDriverIncomeDetail> getUnsettledIncomes(Integer driverId) {
        LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getDriverId, driverId);
        wrapper.eq(CityDeliveryDriverIncomeDetail::getSettlementStatus, 0);
        wrapper.eq(CityDeliveryDriverIncomeDetail::getIsDel, false);
        wrapper.orderByAsc(CityDeliveryDriverIncomeDetail::getCompletedTime);
        
        return this.list(wrapper);
    }

    @Override
    public List<CityDeliveryDriverIncomeDetail> getIncomeDetailsByDateRange(Integer driverId, Date startDate, Date endDate) {
        return getByDriverId(driverId, startDate, endDate);
    }

    @Override
    public BigDecimal calculateDailyIncome(Integer driverId, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();
        
        return calculateTotalIncome(driverId, startOfDay, endOfDay);
    }

    @Override
    public List<Map<String, Object>> getIncomeTrend(Integer driverId, Date startDate, Date endDate) {
        List<CityDeliveryDriverIncomeDetail> incomeList = getByDriverId(driverId, startDate, endDate);
        Map<String, BigDecimal> dailyIncomeMap = new LinkedHashMap<>();
        
        // 按日期汇总收入
        for (CityDeliveryDriverIncomeDetail income : incomeList) {
            if (income.getCompletedTime() != null) {
                String dateKey = income.getCompletedTime().toString().substring(0, 10); // YYYY-MM-DD
                dailyIncomeMap.merge(dateKey, income.getDriverIncome(), BigDecimal::add);
            }
        }
        
        // 转换为结果格式
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : dailyIncomeMap.entrySet()) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", entry.getKey());
            dayData.put("income", entry.getValue());
            trendData.add(dayData);
        }
        
        return trendData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCustomerRating(String deliveryOrderNo, BigDecimal rating, String comment) {
        LambdaUpdateWrapper<CityDeliveryDriverIncomeDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CityDeliveryDriverIncomeDetail::getDeliveryOrderNo, deliveryOrderNo);
        updateWrapper.set(CityDeliveryDriverIncomeDetail::getCustomerRating, rating);
        updateWrapper.set(CityDeliveryDriverIncomeDetail::getRemark, comment);
        
        // 根据评分计算评分奖励
        BigDecimal ratingBonus = calculateRatingBonus(rating);
        if (ratingBonus.compareTo(BigDecimal.ZERO) > 0) {
            CityDeliveryDriverIncomeDetail existing = getByDeliveryOrderNo(deliveryOrderNo);
            if (existing != null) {
                BigDecimal newIncome = existing.getDriverIncome().add(ratingBonus);
                updateWrapper.set(CityDeliveryDriverIncomeDetail::getRatingBonus, ratingBonus);
                updateWrapper.set(CityDeliveryDriverIncomeDetail::getDriverIncome, newIncome);
            }
        }
        
        return this.update(updateWrapper);
    }

    @Override
    public Map<String, BigDecimal> calculateBonusAndDeduction(CityDeliveryDriverIncomeDetail incomeDetail) {
        Map<String, BigDecimal> result = new HashMap<>();
        
        // 距离奖励计算
        BigDecimal distanceBonus = BigDecimal.ZERO;
        if (incomeDetail.getDeliveryDistance() != null) {
            BigDecimal distance = incomeDetail.getDeliveryDistance();
            if (distance.compareTo(BigDecimal.valueOf(5)) > 0) {
                if (distance.compareTo(BigDecimal.valueOf(10)) > 0) {
                    // 超过10公里，每公里奖励2元
                    distanceBonus = distance.subtract(BigDecimal.valueOf(10)).multiply(BigDecimal.valueOf(2))
                            .add(BigDecimal.valueOf(5)); // 5-10公里的奖励
                } else {
                    // 5-10公里，每公里奖励1元
                    distanceBonus = distance.subtract(BigDecimal.valueOf(5));
                }
            }
        }
        result.put("distanceBonus", distanceBonus);
        
        // 时效奖励计算
        BigDecimal timeBonus = BigDecimal.ZERO;
        if (incomeDetail.getDeliveryDuration() != null) {
            int duration = incomeDetail.getDeliveryDuration();
            if (duration <= 20) { // 20分钟内完成
                timeBonus = BigDecimal.valueOf(5);
            } else if (duration <= 30) { // 30分钟内完成
                timeBonus = BigDecimal.valueOf(3);
            } else if (duration <= 45) { // 45分钟内完成
                timeBonus = BigDecimal.valueOf(1);
            }
        }
        result.put("timeBonus", timeBonus);
        
        // 评分奖励
        BigDecimal ratingBonus = calculateRatingBonus(incomeDetail.getCustomerRating());
        result.put("ratingBonus", ratingBonus);
        
        // 其他奖励（夜间、恶劣天气等）
        BigDecimal otherBonus = calculateOtherBonus(incomeDetail);
        result.put("otherBonus", otherBonus);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoCalculateIncomeDetail(String deliveryOrderNo) {
        // 获取配送订单信息
        CityDeliveryOrder order = cityDeliveryOrderService.getByDeliveryOrderNo(deliveryOrderNo);
        if (order == null || order.getDeliveryStatus() != 4) { // 必须是已完成状态
            return false;
        }
        
        // 检查是否已存在收入明细
        if (getByDeliveryOrderNo(deliveryOrderNo) != null) {
            return false; // 已存在，不重复创建
        }
        
        // 创建收入明细记录
        CityDeliveryDriverIncomeDetail incomeDetail = new CityDeliveryDriverIncomeDetail();
        incomeDetail.setDriverId(order.getDriverId());
        incomeDetail.setDriverName(order.getDriverName());
        incomeDetail.setDeliveryOrderNo(deliveryOrderNo);
        incomeDetail.setOrderNo(order.getOrderNo());
        incomeDetail.setMerId(order.getMerId());
        incomeDetail.setUserId(order.getUid());
        incomeDetail.setAreaId(order.getAreaId());
        incomeDetail.setFeeRuleId(order.getFeeRuleId());
        incomeDetail.setOrderAmount(order.getOrderAmount());
        incomeDetail.setDeliveryFee(order.getDeliveryFee());
        incomeDetail.setDeliveryDistance(order.getDeliveryDistance());
        incomeDetail.setDeliveryType(order.getDeliveryType());
        incomeDetail.setDeliveryStatus(order.getDeliveryStatus());
        incomeDetail.setCompletedTime(order.getActualDeliveryTime());
        incomeDetail.setStartTime(order.getStartDeliveryTime());
        incomeDetail.setEndTime(order.getActualDeliveryTime());
        incomeDetail.setPickupAddress(order.getPickupAddress());
        incomeDetail.setDeliveryAddress(order.getDeliveryAddress());
        
        // 计算配送时长
        if (order.getStartDeliveryTime() != null && order.getActualDeliveryTime() != null) {
            long durationMillis = order.getActualDeliveryTime().getTime() - order.getStartDeliveryTime().getTime();
            int durationMinutes = (int) (durationMillis / (1000 * 60));
            incomeDetail.setDeliveryDuration(durationMinutes);
        }
        
        // 设置默认评分
        incomeDetail.setCustomerRating(BigDecimal.valueOf(5.0));
        incomeDetail.setSettlementStatus(0); // 未结算
        incomeDetail.setIsDel(false);
        incomeDetail.setCreateTime(new Date());
        incomeDetail.setUpdateTime(new Date());
        
        return createIncomeDetail(incomeDetail);
    }

    @Override
    public List<Map<String, Object>> getDriverRankingData(Date startDate, Date endDate, Integer limit) {
        // 这里可以通过SQL或者代码实现排行榜逻辑
        // 简单实现：按收入排序
        LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getIsDel, false);
        if (startDate != null) {
            wrapper.ge(CityDeliveryDriverIncomeDetail::getCompletedTime, startDate);
        }
        if (endDate != null) {
            wrapper.le(CityDeliveryDriverIncomeDetail::getCompletedTime, endDate);
        }
        
        List<CityDeliveryDriverIncomeDetail> allIncomes = this.list(wrapper);
        Map<Integer, Map<String, Object>> driverStatsMap = new HashMap<>();
        
        // 按配送员汇总数据
        for (CityDeliveryDriverIncomeDetail income : allIncomes) {
            Integer driverId = income.getDriverId();
            Map<String, Object> stats = driverStatsMap.computeIfAbsent(driverId, k -> {
                Map<String, Object> map = new HashMap<>();
                map.put("driverId", driverId);
                map.put("driverName", income.getDriverName());
                map.put("totalIncome", BigDecimal.ZERO);
                map.put("orderCount", 0);
                return map;
            });
            
            BigDecimal totalIncome = (BigDecimal) stats.get("totalIncome");
            stats.put("totalIncome", totalIncome.add(income.getDriverIncome()));
            stats.put("orderCount", (Integer) stats.get("orderCount") + 1);
        }
        
        // 转换为列表并排序
        List<Map<String, Object>> rankingList = new ArrayList<>(driverStatsMap.values());
        rankingList.sort((a, b) -> ((BigDecimal) b.get("totalIncome")).compareTo((BigDecimal) a.get("totalIncome")));
        
        // 限制返回数量
        if (limit != null && limit > 0 && rankingList.size() > limit) {
            rankingList = rankingList.subList(0, limit);
        }
        
        return rankingList;
    }

    @Override
    public BigDecimal calculatePlatformIncome(BigDecimal totalFee, BigDecimal driverIncome) {
        if (totalFee == null || driverIncome == null) {
            return BigDecimal.ZERO;
        }
        return totalFee.subtract(driverIncome);
    }

    @Override
    public Map<String, Object> getIncomeStatisticsReport(Date startDate, Date endDate) {
        LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverIncomeDetail::getIsDel, false);
        if (startDate != null) {
            wrapper.ge(CityDeliveryDriverIncomeDetail::getCompletedTime, startDate);
        }
        if (endDate != null) {
            wrapper.le(CityDeliveryDriverIncomeDetail::getCompletedTime, endDate);
        }
        
        List<CityDeliveryDriverIncomeDetail> incomes = this.list(wrapper);
        
        Map<String, Object> report = new HashMap<>();
        BigDecimal totalDriverIncome = BigDecimal.ZERO;
        BigDecimal totalPlatformIncome = BigDecimal.ZERO;
        BigDecimal totalDeliveryFee = BigDecimal.ZERO;
        int totalOrders = incomes.size();
        
        for (CityDeliveryDriverIncomeDetail income : incomes) {
            totalDriverIncome = totalDriverIncome.add(income.getDriverIncome());
            totalPlatformIncome = totalPlatformIncome.add(income.getPlatformIncome());
            totalDeliveryFee = totalDeliveryFee.add(income.getDeliveryFee());
        }
        
        report.put("totalOrders", totalOrders);
        report.put("totalDriverIncome", totalDriverIncome);
        report.put("totalPlatformIncome", totalPlatformIncome);
        report.put("totalDeliveryFee", totalDeliveryFee);
        report.put("averageDriverIncome", totalOrders > 0 ? totalDriverIncome.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        report.put("averageDeliveryFee", totalOrders > 0 ? totalDeliveryFee.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        return report;
    }

    /**
     * 计算收入明细
     */
    private void calculateIncomeDetail(CityDeliveryDriverIncomeDetail incomeDetail) {
        // 获取费用规则
        CityDeliveryFeeRule feeRule = null;
        if (incomeDetail.getFeeRuleId() != null) {
            feeRule = cityDeliveryFeeRuleService.getById(incomeDetail.getFeeRuleId());
        }
        
        // 计算基础收入
        BigDecimal baseIncome = BigDecimal.valueOf(10); // 默认基础收入10元
        if (feeRule != null && feeRule.getBaseFee() != null) {
            baseIncome = feeRule.getBaseFee();
        }
        incomeDetail.setBaseIncome(baseIncome);
        
        // 计算佣金收入
        BigDecimal deliveryFee = incomeDetail.getDeliveryFee() != null ? incomeDetail.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal commissionRate = BigDecimal.valueOf(0.15); // 默认15%
        
        // 获取配送员佣金比例
        if (incomeDetail.getDriverId() != null) {
            CityDeliveryDriver driver = cityDeliveryDriverService.getById(incomeDetail.getDriverId());
            if (driver != null && driver.getCommissionRate() != null) {
                commissionRate = driver.getCommissionRate().divide(BigDecimal.valueOf(100)); // 转换为小数
            }
        }
        
        BigDecimal commissionIncome = deliveryFee.multiply(commissionRate);
        incomeDetail.setCommissionIncome(commissionIncome);
        incomeDetail.setCommissionRate(commissionRate.multiply(BigDecimal.valueOf(100))); // 存储为百分比
        
        // 计算各种奖励
        Map<String, BigDecimal> bonusMap = calculateBonusAndDeduction(incomeDetail);
        incomeDetail.setDistanceBonus(bonusMap.get("distanceBonus"));
        incomeDetail.setTimeBonus(bonusMap.get("timeBonus"));
        incomeDetail.setRatingBonus(bonusMap.get("ratingBonus"));
        incomeDetail.setOtherBonus(bonusMap.get("otherBonus"));
        
        // 计算总收入
        BigDecimal totalIncome = baseIncome
                .add(commissionIncome)
                .add(incomeDetail.getDistanceBonus())
                .add(incomeDetail.getTimeBonus())
                .add(incomeDetail.getRatingBonus())
                .add(incomeDetail.getOtherBonus());
        
        // 扣除违规金额
        if (incomeDetail.getDeductionAmount() != null) {
            totalIncome = totalIncome.subtract(incomeDetail.getDeductionAmount());
        }
        
        incomeDetail.setDriverIncome(totalIncome);
        
        // 计算平台收入
        BigDecimal platformIncome = deliveryFee.subtract(totalIncome);
        if (platformIncome.compareTo(BigDecimal.ZERO) < 0) {
            platformIncome = BigDecimal.ZERO; // 平台收入不能为负
        }
        incomeDetail.setPlatformIncome(platformIncome);
        
        // 设置用户支付费用
        incomeDetail.setUserPayment(deliveryFee);
    }

    /**
     * 计算评分奖励
     */
    private BigDecimal calculateRatingBonus(BigDecimal rating) {
        if (rating == null) {
            return BigDecimal.ZERO;
        }
        
        if (rating.compareTo(BigDecimal.valueOf(5.0)) == 0) {
            return BigDecimal.valueOf(2); // 5星奖励2元
        } else if (rating.compareTo(BigDecimal.valueOf(4.0)) >= 0) {
            return BigDecimal.valueOf(1); // 4星及以上奖励1元
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * 计算其他奖励（夜间、恶劣天气等）
     */
    private BigDecimal calculateOtherBonus(CityDeliveryDriverIncomeDetail incomeDetail) {
        BigDecimal otherBonus = BigDecimal.ZERO;
        
        // 夜间配送奖励（22:00-06:00）
        if (incomeDetail.getStartTime() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(incomeDetail.getStartTime());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour >= 22 || hour < 6) {
                otherBonus = otherBonus.add(BigDecimal.valueOf(3)); // 夜间奖励3元
            }
        }
        
        // 其他特殊情况奖励可以在这里添加
        
        return otherBonus;
    }
} 