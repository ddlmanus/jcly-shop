package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.CityDeliveryArea;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CityDeliveryAreaDao;
import com.zbkj.service.service.CityDeliveryAreaService;
import com.zbkj.service.service.TencentMapService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import cn.hutool.core.collection.CollUtil;

/**
 * 同城配送区域服务实现类
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
public class CityDeliveryAreaServiceImpl extends ServiceImpl<CityDeliveryAreaDao, CityDeliveryArea> implements CityDeliveryAreaService {

    @Autowired
    private CityDeliveryAreaDao cityDeliveryAreaDao;

    @Autowired
    private TencentMapService tencentMapService;

    /**
     * 配送区域分页列表
     */
    @Override
    public List<CityDeliveryArea> getList(PageParamRequest pageParamRequest) {
        // 使用PageHelper进行分页
        Page<CityDeliveryArea> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        QueryWrapper<CityDeliveryArea> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", 0)
                   .orderByDesc("create_time");
        
        List<CityDeliveryArea> list = list(queryWrapper);
        
        // 处理空结果
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        
        return list;
    }

    @Override
    @Transactional
    public Boolean create(CityDeliveryArea area) {
        // 检查区域名称是否已存在
        if (getAreaByName(area.getAreaName()) != null) {
            throw new CrmebException("区域名称已存在");
        }
        
        // 设置默认值
        area.setStatus(1); // 默认启用
        area.setSupportInstantDelivery(1); // 默认支持即时配送
        area.setSupportScheduledDelivery(1); // 默认支持预约配送
        area.setServiceRadius(area.getServiceRadius() == null ? BigDecimal.valueOf(5.0) : area.getServiceRadius());
        area.setMaxDeliveryDistance(area.getMaxDeliveryDistance() == null ? BigDecimal.valueOf(10.0) : area.getMaxDeliveryDistance());
        area.setServiceStartTime(area.getServiceStartTime() == null ? "08:00" : area.getServiceStartTime());
        area.setServiceEndTime(area.getServiceEndTime() == null ? "22:00" : area.getServiceEndTime());
        area.setSort(area.getSort() == null ? 0 : area.getSort());
        area.setCreateTime(new Date());
        area.setUpdateTime(new Date());
        area.setIsDel(0);
        
        return baseMapper.insert(area) > 0;
    }

    @Override
    @Transactional
    public Boolean updateArea(CityDeliveryArea area) {
        if (area.getId() == null) {
            throw new CrmebException("区域ID不能为空");
        }
        
        CityDeliveryArea existArea = baseMapper.selectById(area.getId());
        if (existArea == null || existArea.getIsDel() == 1) {
            throw new CrmebException("配送区域不存在");
        }
        
        // 检查区域名称是否被其他区域使用
        if (!existArea.getAreaName().equals(area.getAreaName())) {
            CityDeliveryArea nameArea = getAreaByName(area.getAreaName());
            if (nameArea != null && !nameArea.getId().equals(area.getId())) {
                throw new CrmebException("区域名称已被其他区域使用");
            }
        }
        
        area.setUpdateTime(new Date());
        return baseMapper.updateById(area) > 0;
    }

    @Override
    @Transactional
    public Boolean delete(Integer areaId) {
        if (areaId == null) {
            throw new CrmebException("区域ID不能为空");
        }
        
        CityDeliveryArea area = baseMapper.selectById(areaId);
        if (area == null || area.getIsDel() == 1) {
            throw new CrmebException("配送区域不存在");
        }
        
        area.setIsDel(1);
        area.setUpdateTime(new Date());
        return baseMapper.updateById(area) > 0;
    }

    @Override
    public CityDeliveryArea getAreaByAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 通过腾讯地图获取地址的经纬度
            Map<String, BigDecimal> coordinates = tencentMapService.geocoding(address);
            if (coordinates == null || coordinates.isEmpty()) {
                return null;
            }
            
            BigDecimal longitude = coordinates.get("lng");
            BigDecimal latitude = coordinates.get("lat");
            
            if (longitude == null || latitude == null) {
                return null;
            }
            
            // 根据经纬度查找区域
            List<CityDeliveryArea> areas = cityDeliveryAreaDao.getAreasByLocation(longitude, latitude);
            
            // 返回第一个匹配的区域（按服务半径排序，最小的优先）
            return areas.isEmpty() ? null : areas.get(0);
            
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Boolean isAddressInServiceArea(String address) {
        return getAreaByAddress(address) != null;
    }

    @Override
    public List<Map<String, String>> getDeliveryTimeSlots(Integer areaId) {
        if (areaId == null) {
            return new ArrayList<>();
        }
        
        String timeSlots = cityDeliveryAreaDao.getAreaTimeSlots(areaId);
        if (timeSlots == null || timeSlots.trim().isEmpty()) {
            // 返回默认时间段
            return getDefaultTimeSlots();
        }
        
        // 解析JSON格式的时间段（这里简化处理）
        return getDefaultTimeSlots();
    }

    @Override
    public List<CityDeliveryArea> getAreasByLocation(BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null) {
            return new ArrayList<>();
        }
        
        return cityDeliveryAreaDao.getAreasByLocation(longitude, latitude);
    }

    @Override
    public List<CityDeliveryArea> getAreasByCity(String province, String city, String district) {
        return cityDeliveryAreaDao.getAreasByCity(province, city, district);
    }

    @Override
    public List<CityDeliveryArea> getEnabledAreas() {
        return cityDeliveryAreaDao.getEnabledAreas();
    }

    @Override
    @Transactional
    public Boolean updateStatus(Integer areaId, Integer status) {
        if (areaId == null || status == null) {
            throw new CrmebException("参数不能为空");
        }
        
        return cityDeliveryAreaDao.updateAreaStatus(areaId, status) > 0;
    }

    @Override
    @Transactional
    public Boolean batchUpdateStatus(List<Integer> areaIds, Integer status) {
        if (areaIds == null || areaIds.isEmpty() || status == null) {
            throw new CrmebException("参数不能为空");
        }
        
        return cityDeliveryAreaDao.batchUpdateAreaStatus(areaIds, status) > 0;
    }

    @Override
    public Boolean checkLocationInArea(Integer areaId, BigDecimal longitude, BigDecimal latitude) {
        if (areaId == null || longitude == null || latitude == null) {
            return false;
        }
        
        return cityDeliveryAreaDao.checkLocationInArea(areaId, longitude, latitude) > 0;
    }

    /**
     * 根据区域名称获取区域
     */
    private CityDeliveryArea getAreaByName(String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<CityDeliveryArea> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CityDeliveryArea::getAreaName,areaName);
        return baseMapper.selectOne(lambdaQueryWrapper);
    }

    /**
     * 获取默认时间段
     */
    private List<Map<String, String>> getDefaultTimeSlots() {
        List<Map<String, String>> timeSlots = new ArrayList<>();
        
        // 上午时段
        Map<String, String> morning = new HashMap<>();
        morning.put("label", "上午(09:00-12:00)");
        morning.put("value", "09:00-12:00");
        timeSlots.add(morning);
        
        // 下午时段
        Map<String, String> afternoon = new HashMap<>();
        afternoon.put("label", "下午(13:00-17:00)");
        afternoon.put("value", "13:00-17:00");
        timeSlots.add(afternoon);
        
        // 晚上时段
        Map<String, String> evening = new HashMap<>();
        evening.put("label", "晚上(18:00-21:00)");
        evening.put("value", "18:00-21:00");
        timeSlots.add(evening);
        
        return timeSlots;
    }
} 