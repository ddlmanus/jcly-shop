package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.platform.Store;
import com.zbkj.common.request.StoreRequest;
import com.zbkj.common.request.StoreSearchRequest;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.dao.StoreDao;
import com.zbkj.service.service.MerchantService;
import com.zbkj.service.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台门店服务实现类
 */
@Slf4j
@Service
public class StoreServiceImpl extends ServiceImpl<StoreDao, Store> implements StoreService {

    @Resource
    private StoreDao storeDao;

    @Autowired
    private MerchantService merchantService;
    // 地球半径，单位：千米
    private static final double EARTH_RADIUS = 6371.0;

    @Override
    public PageInfo<Store> getPageList(StoreSearchRequest request) {
        Page<Store> page = PageHelper.startPage(request.getPage(), request.getLimit());

        // 构建查询条件
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();

        if (ObjectUtil.isNotNull(request.getMerId())) {
            wrapper.eq(Store::getMerId, request.getMerId());
        }
        if (StrUtil.isNotBlank(request.getStoreCode())) {
            wrapper.like(Store::getStoreCode, request.getStoreCode());
        }
        if (StrUtil.isNotBlank(request.getStoreName())) {
            wrapper.like(Store::getStoreName, request.getStoreName());
        }
        if (StrUtil.isNotBlank(request.getStoreType())) {
            wrapper.eq(Store::getStoreType, request.getStoreType());
        }
        if (StrUtil.isNotBlank(request.getContactPerson())) {
            wrapper.like(Store::getContactPerson, request.getContactPerson());
        }
        if (StrUtil.isNotBlank(request.getContactPhone())) {
            wrapper.like(Store::getContactPhone, request.getContactPhone());
        }
        if (StrUtil.isNotBlank(request.getProvince())) {
            wrapper.eq(Store::getProvince, request.getProvince());
        }
        if (StrUtil.isNotBlank(request.getCity())) {
            wrapper.eq(Store::getCity, request.getCity());
        }
        if (ObjectUtil.isNotNull(request.getCityId())) {
            wrapper.eq(Store::getCityId, request.getCityId());
        }
        if (StrUtil.isNotBlank(request.getDistrict())) {
            wrapper.eq(Store::getDistrict, request.getDistrict());
        }
        if (ObjectUtil.isNotNull(request.getStatus())) {
            wrapper.eq(Store::getStatus, request.getStatus());
        }
        if (StrUtil.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(Store::getStoreName, request.getKeyword())
                            .or().like(Store::getContactPerson, request.getKeyword())
                            .or().like(Store::getContactPhone, request.getKeyword())
                            .or().like(Store::getFullAddress, request.getKeyword()));
        }

        // 排序
        if (StrUtil.isNotBlank(request.getOrderBy())) {
            if ("asc".equals(request.getOrderDirection())) {
                wrapper.orderByAsc(Store::getCreateTime);
            } else {
                wrapper.orderByDesc(Store::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Store::getSort).orderByDesc(Store::getId);
        }

        List<Store> storeList = storeDao.selectList(wrapper);

        // 填充商户名称
        for (Store store : storeList) {
            if (store.getMerId() != null && store.getMerId() > 0) {
                try {
                    Merchant merchant = merchantService.getByIdException(store.getMerId());
                    store.setMerchantName(merchant.getName());
                } catch (Exception e) {
                    log.error("获取商户名称失败：" + e.getMessage());
                }
            }
        }

        return new PageInfo<>(storeList);
    }

    @Override
    public List<Store> getStoreListByMerId(Integer merId) {
        if (merId == null || merId <= 0) {
            return null;
        }
        return storeDao.getStoreListByMerId(merId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord createStore(StoreRequest request) {
        MyRecord record = new MyRecord();

        // 检查门店编码是否已存在
        if (StrUtil.isNotBlank(request.getStoreCode())) {
            LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Store::getStoreCode, request.getStoreCode());
            Store existStore = storeDao.selectOne(wrapper);
            if (existStore != null) {
                return record.set("status", false).set("msg", "门店编码已存在");
            }
        }

        // 检查商户是否存在
        if (request.getMerId() != null && request.getMerId() > 0) {
            boolean exists = merchantService.checkMerchantExists(request.getMerId());
            if (!exists) {
                return record.set("status", false).set("msg", "所选商户不存在");
            }
        }

        Store store = new Store();
        BeanUtils.copyProperties(request, store);

        // 设置完整地址
        store.setFullAddress(store.getProvince() + store.getCity() + store.getDistrict()+store.getStreet() + store.getAddressDetail());
        store.setCreateTime(LocalDateTime.now());
        store.setUpdateTime(LocalDateTime.now());
        boolean save = save(store);
        if (save) {
            return record.set("status", true).set("msg", "添加门店成功").set("data", store);
        } else {
            return record.set("status", false).set("msg", "添加门店失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord updateStore(Integer id, StoreRequest request) {
        MyRecord record = new MyRecord();

        Store store = getById(id);
        if (store == null) {
            return record.set("status", false).set("msg", "门店不存在");
        }

        // 检查门店编码是否已存在
        if (StrUtil.isNotBlank(request.getStoreCode()) && !request.getStoreCode().equals(store.getStoreCode())) {
            LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Store::getStoreCode, request.getStoreCode());
            wrapper.ne(Store::getId, id);
            Store existStore = storeDao.selectOne(wrapper);
            if (existStore != null) {
                return record.set("status", false).set("msg", "门店编码已存在");
            }
//            int count = storeDao.checkStoreCodeExists(request.getStoreCode(), id);
//            if (count > 0) {
//                return record.set("status", false).set("msg", "门店编码已存在");
//            }
        }

        // 检查商户是否存在
        if (request.getMerId() != null && request.getMerId() > 0) {
            boolean exists = merchantService.checkMerchantExists(request.getMerId());
            if (!exists) {
                return record.set("status", false).set("msg", "所选商户不存在");
            }
        }

        BeanUtils.copyProperties(request, store);

        // 设置完整地址
        store.setFullAddress(store.getProvince() + store.getCity() + store.getDistrict() + store.getAddressDetail());

        boolean update = updateById(store);
        if (update) {
            return record.set("status", true).set("msg", "更新门店成功").set("data", store);
        } else {
            return record.set("status", false).set("msg", "更新门店失败");
        }
    }

    @Override
    public Store getStoreDetail(Integer id) {
        Store store = getById(id);
        if (store == null) {
            throw new CrmebException("门店不存在");
        }

        // 填充商户名称
        if (store.getMerId() != null && store.getMerId() > 0) {
            try {
                Merchant merchant = merchantService.getByIdException(store.getMerId());
                store.setMerchantName(merchant.getName());
            } catch (Exception e) {
                log.error("获取商户名称失败：" + e.getMessage());
            }
        }

        return store;
    }

    @Override
    public List<Store> getNearbyStores(Double latitude, Double longitude, Double radius, Integer limit) {
        if (latitude == null || longitude == null) {
            throw new CrmebException("位置信息不能为空");
        }

        if (radius == null || radius <= 0) {
            radius = 10.0; // 默认10公里
        }

        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10条
        }

        // 计算经纬度范围，用于初步筛选，提高查询效率
        // 1度纬度约等于111公里
        double latRange = radius / 111.0;
        // 1度经度的距离随纬度变化，在纬度lat处约等于111*cos(lat)公里
        double lngRange = radius / (111.0 * Math.cos(Math.toRadians(latitude)));
        
        double minLat = latitude - latRange;
        double maxLat = latitude + latRange;
        double minLng = longitude - lngRange;
        double maxLng = longitude + lngRange;

        // 先用矩形范围进行初步筛选，减少需要计算距离的数据量
        LambdaQueryWrapper<Store> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Store::getStatus, 1) // 只查询启用状态的门店
               .isNotNull(Store::getLatitude) // 确保有经纬度信息
               .isNotNull(Store::getLongitude)
               .ge(Store::getLatitude, BigDecimal.valueOf(minLat))
               .le(Store::getLatitude, BigDecimal.valueOf(maxLat))
               .ge(Store::getLongitude, BigDecimal.valueOf(minLng))
               .le(Store::getLongitude, BigDecimal.valueOf(maxLng));

        List<Store> candidateStores = storeDao.selectList(wrapper);
        
        // 如果没有候选门店，直接返回空列表
        if (candidateStores.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算精确距离并过滤
        final Double finalLatitude = latitude;
        final Double finalLongitude = longitude;
        final Double finalRadius = radius;
        
        List<Store> nearbyStores = candidateStores.stream()
                .filter(store -> {
                    double distance = calculateDistance(finalLatitude, finalLongitude, store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
                    return distance <= finalRadius;
                })
                .sorted((s1, s2) -> {
                    double distance1 = calculateDistance(finalLatitude, finalLongitude, s1.getLatitude().doubleValue(), s1.getLongitude().doubleValue());
                    double distance2 = calculateDistance(finalLatitude, finalLongitude, s2.getLatitude().doubleValue(), s2.getLongitude().doubleValue());
                    return Double.compare(distance1, distance2);
                })
                .limit(limit)
                .collect(Collectors.toList());

        // 为结果设置距离信息并填充商户名称
        for (Store store : nearbyStores) {
            double distance = calculateDistance(latitude, longitude, store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
            store.setDistance(BigDecimal.valueOf(distance).setScale(2, BigDecimal.ROUND_HALF_UP));
            
            if (store.getMerId() != null && store.getMerId() > 0) {
                try {
                    Merchant merchant = merchantService.getByIdException(store.getMerId());
                    store.setMerchantName(merchant.getName());
                } catch (Exception e) {
                    log.error("获取商户名称失败：" + e.getMessage());
                }
            }
        }

        return nearbyStores;
    }

    /**
     * 使用Haversine公式计算两点之间的距离（单位：千米）
     *
     * @param lat1 第一点的纬度
     * @param lng1 第一点的经度
     * @param lat2 第二点的纬度
     * @param lng2 第二点的经度
     * @return 两点之间的距离，单位为千米
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // 转换为弧度
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double radLng1 = Math.toRadians(lng1);
        double radLng2 = Math.toRadians(lng2);

        // Haversine公式
        double dLat = radLat2 - radLat1;
        double dLng = radLng2 - radLng1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 地球半径乘以弧度得到距离（千米）
        return EARTH_RADIUS * c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord batchUpdateStatus(List<Integer> storeIds, Integer status) {
        MyRecord record = new MyRecord();

        if (storeIds == null || storeIds.isEmpty()) {
            return record.set("status", false).set("msg", "请选择门店");
        }

        if (status == null || (status != 0 && status != 1)) {
            return record.set("status", false).set("msg", "状态值不正确");
        }

        int count = storeDao.batchUpdateStatus(storeIds, status);
        if (count > 0) {
            return record.set("status", true).set("msg", "更新状态成功").set("count", count);
        } else {
            return record.set("status", false).set("msg", "更新状态失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord deleteStore(Integer id) {
        MyRecord record = new MyRecord();

        Store store = getById(id);
        if (store == null) {
            return record.set("status", false).set("msg", "门店不存在");
        }

        boolean remove = removeById(id);
        if (remove) {
            return record.set("status", true).set("msg", "删除门店成功");
        } else {
            return record.set("status", false).set("msg", "删除门店失败");
        }
    }

    @Override
    public List<Store> getAllStores() {
        // 构建查询条件，只查询状态为启用的门店
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStatus, 1);
        wrapper.orderByDesc(Store::getSort).orderByDesc(Store::getId);

        List<Store> storeList = storeDao.selectList(wrapper);

        // 填充商户名称
        for (Store store : storeList) {
            if (store.getMerId() != null && store.getMerId() > 0) {
                try {
                    Merchant merchant = merchantService.getByIdException(store.getMerId());
                    store.setMerchantName(merchant.getName());
                } catch (Exception e) {
                    log.error("获取商户名称失败：" + e.getMessage());
                }
            }
        }

        return storeList;
    }
}
