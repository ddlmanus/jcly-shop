package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.order.CityDeliveryDriver;
import com.zbkj.common.model.order.CityDeliveryDriverWorkRecord;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CityDeliveryDriverWorkRecordDao;
import com.zbkj.service.service.CityDeliveryDriverService;
import com.zbkj.service.service.CityDeliveryDriverWorkRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 配送员工作记录服务实现类
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
public class CityDeliveryDriverWorkRecordServiceImpl extends ServiceImpl<CityDeliveryDriverWorkRecordDao, CityDeliveryDriverWorkRecord> 
        implements CityDeliveryDriverWorkRecordService {

    @Autowired
    private CityDeliveryDriverService cityDeliveryDriverService;

    @Override
    public CityDeliveryDriverWorkRecord createOrUpdateWorkRecord(Integer driverId, Date workDate) {
        // 查找当日工作记录
        CityDeliveryDriverWorkRecord record = baseMapper.getByDriverIdAndDate(driverId, workDate);
        
        if (record == null) {
            // 创建新记录
            CityDeliveryDriver driver = cityDeliveryDriverService.getById(driverId);
            record = new CityDeliveryDriverWorkRecord();
            record.setDriverId(driverId);
            record.setDriverName(driver != null ? driver.getName() : "");
            record.setWorkDate(workDate);
            record.setIsDel(0);
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            save(record);
        }
        
        return record;
    }

    @Override
    public Boolean driverCheckIn(Integer driverId) {
        Date today = DateUtil.date();
        CityDeliveryDriverWorkRecord record = createOrUpdateWorkRecord(driverId, today);
        
        if (record.getCheckInTime() == null) {
            record.setCheckInTime(new Date());
            record.setUpdateTime(new Date());
            return updateById(record);
        }
        
        return true;
    }

    @Override
    public Boolean driverCheckOut(Integer driverId) {
        Date today = DateUtil.date();
        CityDeliveryDriverWorkRecord record = createOrUpdateWorkRecord(driverId, today);
        
        if (record.getCheckOutTime() == null) {
            record.setCheckOutTime(new Date());
            
            // 计算工作时长
            if (record.getCheckInTime() != null) {
                BigDecimal workHours = calculateWorkHours(record.getCheckInTime(), record.getCheckOutTime());
                record.setWorkHours(workHours);
            }
            
            record.setUpdateTime(new Date());
            return updateById(record);
        }
        
        return true;
    }

    @Override
    public Boolean updateOrderStats(Integer driverId, Boolean isCompleted, BigDecimal income, 
                                  BigDecimal distance, Integer deliveryTime) {
        Date today = DateUtil.date();
        CityDeliveryDriverWorkRecord record = createOrUpdateWorkRecord(driverId, today);
        
        // 更新订单统计
        record.setTotalOrders((record.getTotalOrders() != null ? record.getTotalOrders() : 0) + 1);
        
        if (isCompleted) {
            record.setCompletedOrders((record.getCompletedOrders() != null ? record.getCompletedOrders() : 0) + 1);
        } else {
            record.setCancelledOrders((record.getCancelledOrders() != null ? record.getCancelledOrders() : 0) + 1);
        }
        
        // 更新收入
        if (income != null) {
            BigDecimal currentIncome = record.getTotalIncome() != null ? record.getTotalIncome() : BigDecimal.ZERO;
            record.setTotalIncome(currentIncome.add(income));
            
            // 佣金按10%计算
            BigDecimal commission = income.multiply(new BigDecimal("0.1"));
            BigDecimal currentCommission = record.getCommissionIncome() != null ? record.getCommissionIncome() : BigDecimal.ZERO;
            record.setCommissionIncome(currentCommission.add(commission));
        }
        
        // 更新距离
        if (distance != null) {
            BigDecimal currentDistance = record.getTotalDistance() != null ? record.getTotalDistance() : BigDecimal.ZERO;
            record.setTotalDistance(currentDistance.add(distance));
            
            // 更新最大配送距离
            if (record.getMaxDeliveryDistance() == null || distance.compareTo(record.getMaxDeliveryDistance()) > 0) {
                record.setMaxDeliveryDistance(distance);
            }
        }
        
        // 更新平均配送时间
        if (deliveryTime != null && record.getCompletedOrders() != null && record.getCompletedOrders() > 0) {
            Integer currentAvgTime = record.getAvgDeliveryTime() != null ? record.getAvgDeliveryTime() : 0;
            Integer newAvgTime = (currentAvgTime * (record.getCompletedOrders() - 1) + deliveryTime) / record.getCompletedOrders();
            record.setAvgDeliveryTime(newAvgTime);
        }
        
        record.setUpdateTime(new Date());
        return updateById(record);
    }

    @Override
    public Boolean updateDriverRating(Integer driverId, BigDecimal rating) {
        Date today = DateUtil.date();
        CityDeliveryDriverWorkRecord record = createOrUpdateWorkRecord(driverId, today);
        
        BigDecimal currentTotalRating = record.getTotalRating() != null ? record.getTotalRating() : BigDecimal.ZERO;
        Integer currentRatingCount = record.getRatingCount() != null ? record.getRatingCount() : 0;
        
        record.setTotalRating(currentTotalRating.add(rating));
        record.setRatingCount(currentRatingCount + 1);
        
        // 计算平均评分
        BigDecimal avgRating = record.getTotalRating().divide(new BigDecimal(record.getRatingCount()), 2, RoundingMode.HALF_UP);
        record.setAverageRating(avgRating);
        
        record.setUpdateTime(new Date());
        return updateById(record);
    }

    @Override
    public Boolean recordException(Integer driverId, Integer exceptionType) {
        Date today = DateUtil.date();
        CityDeliveryDriverWorkRecord record = createOrUpdateWorkRecord(driverId, today);
        
        switch (exceptionType) {
            case 1: // 违规
                record.setViolationCount((record.getViolationCount() != null ? record.getViolationCount() : 0) + 1);
                break;
            case 2: // 异常
                record.setExceptionCount((record.getExceptionCount() != null ? record.getExceptionCount() : 0) + 1);
                break;
            case 3: // 超时
                record.setTimeoutCount((record.getTimeoutCount() != null ? record.getTimeoutCount() : 0) + 1);
                break;
            case 4: // 投诉
                record.setComplaintCount((record.getComplaintCount() != null ? record.getComplaintCount() : 0) + 1);
                break;
            default:
                return false;
        }
        
        record.setUpdateTime(new Date());
        return updateById(record);
    }

    @Override
    public PageInfo<CityDeliveryDriverWorkRecord> getWorkRecordPage(Integer driverId, String startDate, 
                                                                  String endDate, PageParamRequest pageParamRequest) {
        Page<CityDeliveryDriverWorkRecord> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        LambdaQueryWrapper<CityDeliveryDriverWorkRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverWorkRecord::getDriverId, driverId);
        wrapper.eq(CityDeliveryDriverWorkRecord::getIsDel, 0);
        
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(CityDeliveryDriverWorkRecord::getWorkDate, startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(CityDeliveryDriverWorkRecord::getWorkDate, endDate);
        }
        
        wrapper.orderByDesc(CityDeliveryDriverWorkRecord::getWorkDate);
        
        List<CityDeliveryDriverWorkRecord> list = list(wrapper);
        return new PageInfo<>(list);
    }

    @Override
    public Map<String, Object> getDriverIncomeStats(Integer driverId, String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<CityDeliveryDriverWorkRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryDriverWorkRecord::getDriverId, driverId);
        wrapper.eq(CityDeliveryDriverWorkRecord::getIsDel, 0);
        
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(CityDeliveryDriverWorkRecord::getWorkDate, startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(CityDeliveryDriverWorkRecord::getWorkDate, endDate);
        }
        
        List<CityDeliveryDriverWorkRecord> records = list(wrapper);
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalDistance = BigDecimal.ZERO;
        Integer totalOrders = 0;
        
        for (CityDeliveryDriverWorkRecord record : records) {
            totalIncome = totalIncome.add(record.getTotalIncome() != null ? record.getTotalIncome() : BigDecimal.ZERO);
            totalCommission = totalCommission.add(record.getCommissionIncome() != null ? record.getCommissionIncome() : BigDecimal.ZERO);
            totalDistance = totalDistance.add(record.getTotalDistance() != null ? record.getTotalDistance() : BigDecimal.ZERO);
            totalOrders += (record.getTotalOrders() != null ? record.getTotalOrders() : 0);
        }
        
        stats.put("totalIncome", totalIncome);
        stats.put("totalCommission", totalCommission);
        stats.put("totalDistance", totalDistance);
        stats.put("totalOrders", totalOrders);
        
        // 计算平均值
        if (totalOrders > 0) {
            stats.put("avgIncomePerOrder", totalIncome.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP));
        } else {
            stats.put("avgIncomePerOrder", BigDecimal.ZERO);
        }
        
        if (totalDistance.compareTo(BigDecimal.ZERO) > 0) {
            stats.put("avgIncomePerKm", totalIncome.divide(totalDistance, 2, RoundingMode.HALF_UP));
        } else {
            stats.put("avgIncomePerKm", BigDecimal.ZERO);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getDriverWorkStats(Integer driverId, Integer days) {
        // 基本实现，返回基础统计
        Map<String, Object> stats = new HashMap<>();
        stats.put("workingDays", days);
        return stats;
    }

    @Override
    public Map<String, Object> getDriverMonthlyStats(Integer driverId, Integer year, Integer month) {
        return baseMapper.getDriverMonthlyStats(driverId, year, month);
    }

    @Override
    public Map<String, Object> getDriverYearlyStats(Integer driverId, Integer year) {
        return baseMapper.getDriverYearlyStats(driverId, year);
    }

    @Override
    public List<Map<String, Object>> getTopPerformingDrivers(String startDate, String endDate, Integer limit) {
        // 基本实现
        return new ArrayList<>();
    }

    @Override
    public String calculateEfficiencyRating(Integer driverId, Date workDate) {
        CityDeliveryDriverWorkRecord record = baseMapper.getByDriverIdAndDate(driverId, workDate);
        
        if (record == null) {
            return "D";
        }
        
        // 简单的效率评级算法
        Integer completedOrders = record.getCompletedOrders() != null ? record.getCompletedOrders() : 0;
        BigDecimal avgRating = record.getAverageRating() != null ? record.getAverageRating() : BigDecimal.ZERO;
        Integer violations = record.getViolationCount() != null ? record.getViolationCount() : 0;
        
        if (completedOrders >= 15 && avgRating.compareTo(new BigDecimal("4.5")) >= 0 && violations == 0) {
            return "A";
        } else if (completedOrders >= 10 && avgRating.compareTo(new BigDecimal("4.0")) >= 0 && violations <= 1) {
            return "B";
        } else if (completedOrders >= 5 && avgRating.compareTo(new BigDecimal("3.5")) >= 0 && violations <= 2) {
            return "C";
        } else {
            return "D";
        }
    }

    @Override
    public Integer batchGenerateWorkRecords(Date startDate, Date endDate) {
        // 基本实现
        return 0;
    }

    @Override
    public CityDeliveryDriverWorkRecord getTodayWorkRecord(Integer driverId) {
        return baseMapper.getByDriverIdAndDate(driverId, new Date());
    }

    @Override
    public List<CityDeliveryDriverWorkRecord> getRecentWorkRecords(Integer driverId, Integer days) {
       LambdaQueryWrapper<CityDeliveryDriverWorkRecord> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.eq(CityDeliveryDriverWorkRecord::getDriverId, driverId)
               .ge(CityDeliveryDriverWorkRecord::getWorkDate, DateUtil.offsetDay(new Date(), -days));
       return list(queryWrapper);
    }

    @Override
    public BigDecimal calculateWorkHours(Date checkInTime, Date checkOutTime) {
        if (checkInTime == null || checkOutTime == null) {
            return BigDecimal.ZERO;
        }
        
        long diffInMillies = checkOutTime.getTime() - checkInTime.getTime();
        long diffInHours = diffInMillies / (60 * 60 * 1000);
        
        return new BigDecimal(diffInHours).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Boolean updateOnlineTime(Integer driverId, Integer onlineMinutes) {
        Date today = DateUtil.date();
        CityDeliveryDriverWorkRecord record = createOrUpdateWorkRecord(driverId, today);
        
        record.setOnlineMinutes(onlineMinutes);
        record.setUpdateTime(new Date());
        
        return updateById(record);
    }

    @Override
    public Map<String, Object> generateWorkReport(Integer driverId, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取收入统计
        Map<String, Object> incomeStats = getDriverIncomeStats(driverId, startDate, endDate);
        report.putAll(incomeStats);
        
        // 获取工作记录
        List<CityDeliveryDriverWorkRecord> records = baseMapper.getByDriverIdAndDateRange(
                driverId, DateUtil.parse(startDate), DateUtil.parse(endDate));
        
        report.put("workRecords", records);
        report.put("reportPeriod", startDate + " 至 " + endDate);
        report.put("generateTime", new Date());
        
        return report;
    }
} 