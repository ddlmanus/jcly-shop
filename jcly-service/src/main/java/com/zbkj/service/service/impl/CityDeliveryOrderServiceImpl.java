package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.response.CityDeliveryOrderResponse;
import com.zbkj.service.dao.CityDeliveryOrderDao;
import com.zbkj.service.service.CityDeliveryOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 配送订单服务实现类
 * @author 荆楚粮油
 * @since 2024-01-15
 */
@Service
public class CityDeliveryOrderServiceImpl extends ServiceImpl<CityDeliveryOrderDao, CityDeliveryOrder> 
        implements CityDeliveryOrderService {

    @Override
    public CityDeliveryOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getOrderNo, orderNo);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        return this.getOne(wrapper);
    }

    @Override
    public CityDeliveryOrder getByDeliveryOrderNo(String deliveryOrderNo) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDeliveryOrderNo, deliveryOrderNo);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        return this.getOne(wrapper);
    }

    @Override
    public List<CityDeliveryOrder> getByDriverId(Integer driverId) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDriverId, driverId);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<CityDeliveryOrder> getByMerId(Integer merId) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getMerId, merId);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<CityDeliveryOrder> getByUserId(Integer userId) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getUid, userId);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<CityDeliveryOrder> getByStatus(Integer status) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDeliveryStatus, status);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.orderByAsc(CityDeliveryOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<CityDeliveryOrder> getDriverCurrentOrders(Integer driverId) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDriverId, driverId);
        wrapper.in(CityDeliveryOrder::getDeliveryStatus, Arrays.asList(1, 2, 3)); // 已派单、已接单、取件中、配送中
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.orderByAsc(CityDeliveryOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<CityDeliveryOrder> getPendingOrders() {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDeliveryStatus, 0); // 待分配
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.orderByDesc(CityDeliveryOrder::getUrgencyLevel);
        wrapper.orderByAsc(CityDeliveryOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(String deliveryOrderNo, Integer status, Date updateTime) {
        LambdaUpdateWrapper<CityDeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CityDeliveryOrder::getDeliveryOrderNo, deliveryOrderNo);
        updateWrapper.set(CityDeliveryOrder::getDeliveryStatus, status);
        updateWrapper.set(CityDeliveryOrder::getUpdateTime, updateTime);
        
        // 根据状态设置相应的时间字段
        if (status == 3) { // 开始配送
            updateWrapper.set(CityDeliveryOrder::getStartDeliveryTime, updateTime);
        } else if (status == 4 || status == 5) { // 已完成或已送达
            updateWrapper.set(CityDeliveryOrder::getFinishTime, updateTime);
            updateWrapper.set(CityDeliveryOrder::getActualDeliveryTime, updateTime);
        } else if (status == 6 || status == 9) { // 已取消
            updateWrapper.set(CityDeliveryOrder::getCancelTime, updateTime);
        }
        
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignDriver(String deliveryOrderNo, Integer driverId, String driverName, String driverPhone) {
        LambdaUpdateWrapper<CityDeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CityDeliveryOrder::getDeliveryOrderNo, deliveryOrderNo);
        updateWrapper.set(CityDeliveryOrder::getDriverId, driverId);
        updateWrapper.set(CityDeliveryOrder::getDriverName, driverName);
        updateWrapper.set(CityDeliveryOrder::getDriverPhone, driverPhone);
        updateWrapper.set(CityDeliveryOrder::getDeliveryStatus, 1); // 已派单
        updateWrapper.set(CityDeliveryOrder::getUpdateTime, new Date());
        
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeliveryTime(String deliveryOrderNo, Date pickupTime, Date deliveryTime) {
        LambdaUpdateWrapper<CityDeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CityDeliveryOrder::getDeliveryOrderNo, deliveryOrderNo);
        updateWrapper.set(CityDeliveryOrder::getPickupTime, pickupTime);
        updateWrapper.set(CityDeliveryOrder::getActualDeliveryTime, deliveryTime);
        updateWrapper.set(CityDeliveryOrder::getUpdateTime, new Date());
        
        return this.update(updateWrapper);
    }

    @Override
    public List<CityDeliveryOrder> getTimeoutOrders() {
        // 获取超时的配送订单（这里可以根据业务规则定义超时逻辑）
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CityDeliveryOrder::getDeliveryStatus, Arrays.asList(1, 2, 3)); // 进行中的订单
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.lt(CityDeliveryOrder::getEstimatedDeliveryTime, new Date()); // 超过预计送达时间
        wrapper.orderByAsc(CityDeliveryOrder::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getDeliveryStatistics(Integer merId) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<CityDeliveryOrder> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(CityDeliveryOrder::getIsDel, 0);
        if (merId != null) {
            baseWrapper.eq(CityDeliveryOrder::getMerId, merId);
        }
        
        // 总订单数
        stats.put("totalOrders", this.count(baseWrapper));
        
        // 各状态订单数
        for (int status = 0; status <= 9; status++) {
            LambdaQueryWrapper<CityDeliveryOrder> statusWrapper = new LambdaQueryWrapper<>();
            statusWrapper.eq(CityDeliveryOrder::getIsDel, 0);
            statusWrapper.eq(CityDeliveryOrder::getDeliveryStatus, status);
            if (merId != null) {
                statusWrapper.eq(CityDeliveryOrder::getMerId, merId);
            }
            stats.put("status" + status, this.count(statusWrapper));
        }
        
        // 今日订单数
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        
        LambdaQueryWrapper<CityDeliveryOrder> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(CityDeliveryOrder::getIsDel, 0);
        todayWrapper.ge(CityDeliveryOrder::getCreateTime, startOfDay);
        if (merId != null) {
            todayWrapper.eq(CityDeliveryOrder::getMerId, merId);
        }
        stats.put("todayOrders", this.count(todayWrapper));
        
        return stats;
    }

    @Override
    public List<CityDeliveryOrder> queryByConditions(Map<String, Object> conditions) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        
        if (conditions.containsKey("deliveryStatus")) {
            wrapper.eq(CityDeliveryOrder::getDeliveryStatus, conditions.get("deliveryStatus"));
        }
        if (conditions.containsKey("driverId")) {
            wrapper.eq(CityDeliveryOrder::getDriverId, conditions.get("driverId"));
        }
        if (conditions.containsKey("merId")) {
            wrapper.eq(CityDeliveryOrder::getMerId, conditions.get("merId"));
        }
        if (conditions.containsKey("userId")) {
            wrapper.eq(CityDeliveryOrder::getUid, conditions.get("userId"));
        }
        if (conditions.containsKey("startDate")) {
            wrapper.ge(CityDeliveryOrder::getCreateTime, conditions.get("startDate"));
        }
        if (conditions.containsKey("endDate")) {
            wrapper.le(CityDeliveryOrder::getCreateTime, conditions.get("endDate"));
        }
        
        wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<String> deliveryOrderNos, Integer status) {
        LambdaUpdateWrapper<CityDeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CityDeliveryOrder::getDeliveryOrderNo, deliveryOrderNos);
        updateWrapper.set(CityDeliveryOrder::getDeliveryStatus, status);
        updateWrapper.set(CityDeliveryOrder::getUpdateTime, new Date());
        
        return this.update(updateWrapper);
    }

    @Override
    public CityDeliveryOrderResponse convertToResponse(CityDeliveryOrder order) {
        if (order == null) {
            return null;
        }
        
        CityDeliveryOrderResponse response = new CityDeliveryOrderResponse();
        BeanUtils.copyProperties(order, response);
        
        // 设置状态文本
        response.setDeliveryStatusText(getDeliveryStatusText(order.getDeliveryStatus()));
        response.setDeliveryTypeText(getDeliveryTypeText(order.getDeliveryType()));
        
        return response;
    }

    @Override
    public List<CityDeliveryOrder> getByDateRange(Date startDate, Date endDate) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.between(CityDeliveryOrder::getCreateTime, startDate, endDate);
        wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    public List<CityDeliveryOrder> getDriverTodayOrders(Integer driverId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();
        
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDriverId, driverId);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        wrapper.between(CityDeliveryOrder::getCreateTime, startOfDay, endOfDay);
        wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    public long countDriverOrders(Integer driverId, Integer status) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDriverId, driverId);
        wrapper.eq(CityDeliveryOrder::getIsDel, 0);
        
        if (status != null) {
            wrapper.eq(CityDeliveryOrder::getDeliveryStatus, status);
        }
        
        return this.count(wrapper);
    }

    @Override
    public Map<String, Object> getOrderDetail(String deliveryOrderNo) {
        CityDeliveryOrder order = getByDeliveryOrderNo(deliveryOrderNo);
        if (order == null) {
            return null;
        }
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("order", order);
        detail.put("statusText", getDeliveryStatusText(order.getDeliveryStatus()));
        detail.put("typeText", getDeliveryTypeText(order.getDeliveryType()));
        
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String deliveryOrderNo, String cancelReason) {
        LambdaUpdateWrapper<CityDeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CityDeliveryOrder::getDeliveryOrderNo, deliveryOrderNo);
        updateWrapper.set(CityDeliveryOrder::getDeliveryStatus, 9); // 已取消
        updateWrapper.set(CityDeliveryOrder::getCancelReason, cancelReason);
        updateWrapper.set(CityDeliveryOrder::getCancelTime, new Date());
        updateWrapper.set(CityDeliveryOrder::getUpdateTime, new Date());
        
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleException(String deliveryOrderNo, String exceptionReason) {
        LambdaUpdateWrapper<CityDeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CityDeliveryOrder::getDeliveryOrderNo, deliveryOrderNo);
        updateWrapper.set(CityDeliveryOrder::getDeliveryStatus, 7); // 异常
        updateWrapper.set(CityDeliveryOrder::getExceptionReason, exceptionReason);
        updateWrapper.set(CityDeliveryOrder::getExceptionTime, new Date());
        updateWrapper.set(CityDeliveryOrder::getUpdateTime, new Date());
        
        return this.update(updateWrapper);
    }

    @Override
    public List<CityDeliveryOrder> getList(Page<CityDeliveryOrder> pageRequest, LambdaQueryWrapper<CityDeliveryOrder> wrapper) {
        List<CityDeliveryOrder> cityDeliveryOrders = this.baseMapper.selectList(wrapper);
        return cityDeliveryOrders;
    }


    /**
     * 获取配送状态文本
     */
    private String getDeliveryStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待派单";
            case 1: return "已派单";
            case 2: return "已接单";
            case 3: return "取件中";
            case 4: return "配送中";
            case 5: return "已完成";
            case 6: return "已取消";
            case 7: return "异常";
            case 8: return "配送失败";
            default: return "未知状态";
        }
    }

    /**
     * 获取配送类型文本
     */
    private String getDeliveryTypeText(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "即时配送";
            case 2: return "预约配送";
            default: return "未知类型";
        }
    }
} 