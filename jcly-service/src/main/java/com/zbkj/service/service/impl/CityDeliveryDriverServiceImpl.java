package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.CityDeliveryDriver;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CityDeliveryDriverResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.service.dao.CityDeliveryDriverDao;
import com.zbkj.service.service.CityDeliveryDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * 同城配送员服务实现类
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
public class CityDeliveryDriverServiceImpl extends ServiceImpl<CityDeliveryDriverDao, CityDeliveryDriver> implements CityDeliveryDriverService {

    @Autowired
    private CityDeliveryDriverDao cityDeliveryDriverDao;

    @Override
    public List<CityDeliveryDriverResponse> getList(Map<String, Object> params, PageParamRequest pageParamRequest) {
        QueryWrapper<CityDeliveryDriver> queryWrapper = new QueryWrapper<>();
        
        // 搜索条件
        if (params.get("name") != null && !params.get("name").toString().trim().isEmpty()) {
            queryWrapper.like("name", params.get("name").toString().trim());
        }
        if (params.get("phone") != null && !params.get("phone").toString().trim().isEmpty()) {
            queryWrapper.like("phone", params.get("phone").toString().trim());
        }
        if (params.get("status") != null && !params.get("status").toString().trim().isEmpty()) {
            queryWrapper.eq("status", params.get("status"));
        }
        if (params.get("certificationStatus") != null && !params.get("certificationStatus").toString().trim().isEmpty()) {
            queryWrapper.eq("certification_status", params.get("certificationStatus"));
        }
        if (params.get("workArea") != null && !params.get("workArea").toString().trim().isEmpty()) {
            queryWrapper.like("work_area", params.get("workArea").toString().trim());
        }
        
        queryWrapper.eq("is_del", 0);
        queryWrapper.orderByDesc("create_time");
        
        List<CityDeliveryDriver> drivers = baseMapper.selectList(queryWrapper);
        return drivers.stream()
                .map(CityDeliveryDriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取配送员列表（简单版本，用于分页）
     */
    @Override
    public List<CityDeliveryDriver> getList(PageParamRequest pageParamRequest) {
        // 使用PageHelper进行分页
        Page<CityDeliveryDriver> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        QueryWrapper<CityDeliveryDriver> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", 0)
                   .orderByDesc("create_time");
        
        List<CityDeliveryDriver> list = baseMapper.selectList(queryWrapper);
        
        // 处理空结果
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        
        return list;
    }

    @Override
    @Transactional
    public Boolean create(CityDeliveryDriver driver) {
        // 检查手机号是否已存在
        if (getDriverByPhone(driver.getPhone()) != null) {
            throw new CrmebException("手机号已存在");
        }
        // 设置默认值
        driver.setStatus(0); // 默认离线
        driver.setAvailableStatus(0); // 默认不可用
        driver.setCertificationStatus(0); // 默认未认证
        driver.setCurrentOrders(0);
        driver.setMaxOrders(3);
        driver.setTotalDeliveries(0);
        driver.setCompletedDeliveries(0);
        driver.setMonthlyDeliveries(0);
        driver.setViolationCount(0);
        driver.setRating(BigDecimal.valueOf(5.0));
        driver.setTotalIncome(BigDecimal.ZERO);
        driver.setMonthlyIncome(BigDecimal.ZERO);
        driver.setCreateTime(new Date());
        driver.setUpdateTime(new Date());
        driver.setIsDel(0);
        
        return baseMapper.insert(driver) > 0;
    }

    @Override
    @Transactional
    public Boolean updateDriver(CityDeliveryDriver driver) {
        if (driver.getId() == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver existDriver = baseMapper.selectById(driver.getId());
        if (existDriver == null) {
            throw new CrmebException("配送员不存在");
        }
        
        // 检查手机号是否被其他配送员使用
        if (!existDriver.getPhone().equals(driver.getPhone())) {
            CityDeliveryDriver phoneDriver = getDriverByPhone(driver.getPhone());
            if (phoneDriver != null && !phoneDriver.getId().equals(driver.getId())) {
                throw new CrmebException("手机号已被其他配送员使用");
            }
        }
        
        driver.setUpdateTime(new Date());
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    @Transactional
    public Boolean delete(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null) {
            throw new CrmebException("配送员不存在");
        }
        
        // 检查是否有进行中的订单
        if (driver.getCurrentOrders() != null && driver.getCurrentOrders() > 0) {
            throw new CrmebException("该配送员有进行中的订单，无法删除");
        }
        
        driver.setIsDel(1);
        driver.setUpdateTime(new Date());
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    public CityDeliveryDriverResponse getDriverDetail(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        return CityDeliveryDriverResponse.fromEntity(driver);
    }

    @Override
    @Transactional
    public Boolean updateStatus(Integer driverId, Integer status) {
        if (driverId == null || status == null) {
            throw new CrmebException("参数不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        driver.setStatus(status);
        driver.setUpdateTime(new Date());
        
        // 如果设置为在线，更新最后在线时间
        if (status == 1) {
            driver.setLastOnlineTime(new Date());
        }
        
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    @Transactional
    public Boolean batchUpdateStatus(List<Integer> driverIds, Integer status) {
        if (driverIds == null || driverIds.isEmpty() || status == null) {
            throw new CrmebException("参数不能为空");
        }
        
        return cityDeliveryDriverDao.batchUpdateDriverStatus(driverIds, status) > 0;
    }

    @Override
    @Transactional
    public Boolean certification(Integer driverId, Integer certificationStatus, String remark) {
        if (driverId == null || certificationStatus == null) {
            throw new CrmebException("参数不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        driver.setCertificationStatus(certificationStatus);
        driver.setCertificationRemark(remark);
        driver.setUpdateTime(new Date());
        
        // 如果认证失败，设置失败原因
        if (certificationStatus == 3) {
            driver.setCertificationFailReason(remark);
        }
        
        // 如果认证通过，设置为可用状态
        if (certificationStatus == 2) {
            driver.setAvailableStatus(1);
        }
        
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    public List<CityDeliveryDriverResponse> getNearbyDrivers(BigDecimal longitude, BigDecimal latitude, BigDecimal radius) {
        if (longitude == null || latitude == null || radius == null) {
            throw new CrmebException("位置参数不能为空");
        }
        
        List<CityDeliveryDriver> drivers = cityDeliveryDriverDao.getNearbyDrivers(longitude, latitude, radius);
        return drivers.stream()
                .map(CityDeliveryDriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CityDeliveryDriverResponse> getOnlineDrivers() {
        List<CityDeliveryDriver> drivers = cityDeliveryDriverDao.getOnlineDrivers();
        return drivers.stream()
                .map(CityDeliveryDriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CityDeliveryDriverResponse> getAvailableDrivers() {
        List<CityDeliveryDriver> drivers = cityDeliveryDriverDao.getAvailableDrivers();
        return drivers.stream()
                .map(CityDeliveryDriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CityDeliveryDriverResponse> getDriversByArea(String workArea) {
        if (workArea == null || workArea.trim().isEmpty()) {
            throw new CrmebException("工作区域不能为空");
        }
        
        List<CityDeliveryDriver> drivers = cityDeliveryDriverDao.getDriversByArea(workArea);
        return drivers.stream()
                .map(CityDeliveryDriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean updateDriverLocation(Integer driverId, BigDecimal longitude, BigDecimal latitude, String address) {
        if (driverId == null || longitude == null || latitude == null) {
            throw new CrmebException("参数不能为空");
        }
        
        return cityDeliveryDriverDao.updateDriverLocation(driverId, longitude, latitude, address) > 0;
    }

    @Override
    public Map<String, Object> getDriverWorkStats(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = cityDeliveryDriverDao.getDriverWithStats(driverId);
        if (driver == null) {
            throw new CrmebException("配送员不存在");
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDeliveries", driver.getTotalDeliveries());
        stats.put("completedDeliveries", driver.getCompletedDeliveries());
        stats.put("monthlyDeliveries", driver.getMonthlyDeliveries());
        stats.put("totalIncome", driver.getTotalIncome());
        stats.put("monthlyIncome", driver.getMonthlyIncome());
        stats.put("rating", driver.getRating());
        stats.put("violationCount", driver.getViolationCount());
        stats.put("currentOrders", driver.getCurrentOrders());
        stats.put("maxOrders", driver.getMaxOrders());
        
        return stats;
    }

    @Override
    public CityDeliveryDriver getDriverByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<CityDeliveryDriver> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CityDeliveryDriver::getPhone, phone);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public CityDeliveryDriver getDriverByCode(String driverCode) {
        if (driverCode == null || driverCode.trim().isEmpty()) {
            return null;
        }
        return cityDeliveryDriverDao.getDriverByCode(driverCode);
    }

    @Override
    public String generateDriverCode() {
        return "D" + DateUtil.format(new Date(), "yyyyMMddHHmmss") +
               String.format("%04d", (int)(Math.random() * 10000));
    }

    @Override
    @Transactional
    public Boolean updateDriverOrderCount(Integer driverId, Integer increment) {
        if (driverId == null || increment == null) {
            throw new CrmebException("参数不能为空");
        }
        
        return cityDeliveryDriverDao.updateDriverOrderCount(driverId, increment) > 0;
    }

    @Override
    @Transactional
    public Boolean updateDriverIncome(Integer driverId, BigDecimal income) {
        if (driverId == null || income == null) {
            throw new CrmebException("参数不能为空");
        }
        
        return cityDeliveryDriverDao.updateDriverIncome(driverId, income) > 0;
    }

    @Override
    public BigDecimal getDriverRating(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        return driver.getRating();
    }

    @Override
    @Transactional
    public Boolean updateDriverRating(Integer driverId, BigDecimal rating) {
        if (driverId == null || rating == null) {
            throw new CrmebException("参数不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        driver.setRating(rating);
        driver.setUpdateTime(new Date());
        
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    public Boolean checkDriverCanTakeOrder(Integer driverId) {
        if (driverId == null) {
            return false;
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            return false;
        }
        
        // 检查配送员状态
        if (driver.getStatus() != 1) { // 不在线
            return false;
        }
        
        if (driver.getAvailableStatus() != 1) { // 不可用
            return false;
        }
        
        if (driver.getCertificationStatus() != 2) { // 未认证通过
            return false;
        }
        
        // 检查订单数量
        int currentOrders = driver.getCurrentOrders() == null ? 0 : driver.getCurrentOrders();
        int maxOrders = driver.getMaxOrders() == null ? 3 : driver.getMaxOrders();
        
        return currentOrders < maxOrders;
    }

    @Override
    public Integer getDriverCurrentOrderCount(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        return driver.getCurrentOrders() == null ? 0 : driver.getCurrentOrders();
    }

    @Override
    public CityDeliveryDriver getBestDriver(BigDecimal pickupLongitude, BigDecimal pickupLatitude, 
                                          BigDecimal deliveryLongitude, BigDecimal deliveryLatitude) {
        // 获取附近的可用配送员
        List<CityDeliveryDriver> availableDrivers = cityDeliveryDriverDao.getAvailableDrivers();
        
        if (availableDrivers.isEmpty()) {
            return null;
        }
        
        // 计算最佳配送员（简单的距离计算）
        CityDeliveryDriver bestDriver = null;
        double minDistance = Double.MAX_VALUE;
        
        for (CityDeliveryDriver driver : availableDrivers) {
            if (driver.getLongitude() != null && driver.getLatitude() != null) {
                double distance = calculateDistance(
                    driver.getLatitude().doubleValue(), driver.getLongitude().doubleValue(),
                    pickupLatitude.doubleValue(), pickupLongitude.doubleValue()
                );
                
                if (distance < minDistance) {
                    minDistance = distance;
                    bestDriver = driver;
                }
            }
        }
        
        return bestDriver;
    }

    @Override
    @Transactional
    public Boolean driverOnline(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        if (driver.getCertificationStatus() != 2) {
            throw new CrmebException("配送员未认证通过，无法上线");
        }
        
        driver.setStatus(1); // 设置为在线
        driver.setLastOnlineTime(new Date());
        driver.setUpdateTime(new Date());
        
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    @Transactional
    public Boolean driverOffline(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        // 检查是否有进行中的订单
        if (driver.getCurrentOrders() != null && driver.getCurrentOrders() > 0) {
            throw new CrmebException("配送员有进行中的订单，无法下线");
        }
        
        driver.setStatus(0); // 设置为离线
        driver.setUpdateTime(new Date());
        
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    public Map<String, Object> getDriverTodayStats(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        // 这里应该查询今日的工作记录，暂时返回基本数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrders", 0);
        stats.put("todayCompleted", 0);
        stats.put("todayIncome", BigDecimal.ZERO);
        stats.put("onlineHours", 0);
        
        return stats;
    }

    @Override
    public Map<String, Object> getDriverMonthStats(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("monthlyDeliveries", driver.getMonthlyDeliveries());
        stats.put("monthlyIncome", driver.getMonthlyIncome());
        stats.put("averageRating", driver.getRating());
        stats.put("violationCount", driver.getViolationCount());
        
        return stats;
    }

    @Override
    public List<Integer> getAvailableDriversNearby(BigDecimal longitude, BigDecimal latitude, BigDecimal radius) {
        if (longitude == null || latitude == null || radius == null) {
            return new ArrayList<>();
        }
        
        List<CityDeliveryDriver> drivers = cityDeliveryDriverDao.getNearbyDrivers(longitude, latitude, radius);
        return drivers.stream()
                .filter(driver -> checkDriverCanTakeOrder(driver.getId()))
                .map(CityDeliveryDriver::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean incrementCurrentOrders(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        return cityDeliveryDriverDao.updateDriverOrderCount(driverId, 1) > 0;
    }

    @Override
    @Transactional
    public Boolean decrementCurrentOrders(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        return cityDeliveryDriverDao.updateDriverOrderCount(driverId, -1) > 0;
    }

    @Override
    public List<CityDeliveryDriverResponse> getAvailableDriversWithConditions(Map<String, Object> conditions) {
        QueryWrapper<CityDeliveryDriver> queryWrapper = new QueryWrapper<>();
        
        // 基本条件：在线、可用、认证通过
        queryWrapper.eq("status", 1);
        queryWrapper.eq("available_status", 1);
        queryWrapper.eq("certification_status", 2);
        queryWrapper.eq("is_del", 0);
        
        // 订单数量限制
        queryWrapper.apply("current_orders < max_orders");
        
        // 附加条件
        if (conditions.get("workArea") != null) {
            queryWrapper.like("work_area", conditions.get("workArea").toString());
        }
        if (conditions.get("minRating") != null) {
            queryWrapper.ge("rating", conditions.get("minRating"));
        }
        if (conditions.get("vehicleType") != null) {
            queryWrapper.eq("vehicle_type", conditions.get("vehicleType"));
        }
        
        List<CityDeliveryDriver> drivers = baseMapper.selectList(queryWrapper);
        return drivers.stream()
                .map(CityDeliveryDriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean batchUpdateDriverLocation(List<Map<String, Object>> locationData) {
        if (locationData == null || locationData.isEmpty()) {
            return false;
        }
        
        for (Map<String, Object> data : locationData) {
            Integer driverId = (Integer) data.get("driverId");
            BigDecimal longitude = (BigDecimal) data.get("longitude");
            BigDecimal latitude = (BigDecimal) data.get("latitude");
            String address = (String) data.get("address");
            
            if (driverId != null && longitude != null && latitude != null) {
                updateDriverLocation(driverId, longitude, latitude, address);
            }
        }
        
        return true;
    }

    @Override
    public Map<String, Object> getDriverRealtimeStatus(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("driverId", driver.getId());
        status.put("driverName", driver.getName());
        status.put("status", driver.getStatus());
        status.put("availableStatus", driver.getAvailableStatus());
        status.put("currentOrders", driver.getCurrentOrders());
        status.put("maxOrders", driver.getMaxOrders());
        status.put("longitude", driver.getLongitude());
        status.put("latitude", driver.getLatitude());
        status.put("currentAddress", driver.getCurrentAddress());
        status.put("lastOnlineTime", driver.getLastOnlineTime());
        status.put("locationUpdateTime", driver.getLocationUpdateTime());
        status.put("canTakeOrder", checkDriverCanTakeOrder(driverId));
        
        return status;
    }

    @Override
    @Transactional
    public Boolean driverCheckIn(Integer driverId, BigDecimal longitude, BigDecimal latitude) {
        if (driverId == null || longitude == null || latitude == null) {
            throw new CrmebException("参数不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        if (driver.getCertificationStatus() != 2) {
            throw new CrmebException("配送员未认证通过，无法签到");
        }
        
        // 更新状态为在线
        driver.setStatus(1);
        driver.setAvailableStatus(1);
        driver.setLongitude(longitude);
        driver.setLatitude(latitude);
        driver.setLastOnlineTime(new Date());
        driver.setLocationUpdateTime(new Date());
        driver.setUpdateTime(new Date());
        
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    @Transactional
    public Boolean driverCheckOut(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        // 检查是否有进行中的订单
        if (driver.getCurrentOrders() != null && driver.getCurrentOrders() > 0) {
            throw new CrmebException("配送员有进行中的订单，无法签退");
        }
        
        // 更新状态为离线
        driver.setStatus(0);
        driver.setAvailableStatus(0);
        driver.setUpdateTime(new Date());
        
        return baseMapper.updateById(driver) > 0;
    }

    @Override
    public List<Integer> getDriverCoverageAreas(Integer driverId) {
        if (driverId == null) {
            throw new CrmebException("配送员ID不能为空");
        }
        
        CityDeliveryDriver driver = baseMapper.selectById(driverId);
        if (driver == null || driver.getIsDel() == 1) {
            throw new CrmebException("配送员不存在");
        }
        
        // 这里需要根据配送员的位置和服务半径计算覆盖的区域
        // 暂时返回空列表，实际实现需要结合配送区域管理
        return new ArrayList<>();
    }

    @Override
    public List<CityDeliveryDriver> getByStatus(Integer status) {
        QueryWrapper<CityDeliveryDriver> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", 0);
        
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("last_online_time");
        
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<CityDeliveryDriver> getBusyDrivers() {
        QueryWrapper<CityDeliveryDriver> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", 0);
        queryWrapper.eq("status", 2); // 忙碌状态
        queryWrapper.or();
        queryWrapper.eq("status", 1).apply("current_orders > 0"); // 在线但有订单的配送员
        
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<CityDeliveryDriver> getOfflineDrivers() {
        QueryWrapper<CityDeliveryDriver> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", 0);
        queryWrapper.eq("status", 0); // 离线状态
        queryWrapper.orderByDesc("last_online_time");
        
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 计算两点间距离（简单的直线距离计算）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // 地球半径（公里）
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return earthRadius * c;
    }
} 