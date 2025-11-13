package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.MerchantStore;
import com.zbkj.common.model.merchant.MerchantStoreDeliveryArea;
import com.zbkj.common.model.merchant.MerchantStoreHours;
import com.zbkj.common.model.merchant.MerchantStoreStaff;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.MerchantStoreRequest;
import com.zbkj.common.request.MerchantStoreSearchRequest;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.dao.MerchantStoreDao;
import com.zbkj.service.dao.MerchantStoreDeliveryAreaDao;
import com.zbkj.service.dao.MerchantStoreHoursDao;
import com.zbkj.service.dao.MerchantStoreStaffDao;
import com.zbkj.service.service.MerchantStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户门店管理服务实现类
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Slf4j
@Service
public class MerchantStoreServiceImpl extends ServiceImpl<MerchantStoreDao, MerchantStore> 
        implements MerchantStoreService {

    @Resource
    private MerchantStoreDao merchantStoreDao;

    @Autowired
    private MerchantStoreHoursDao merchantStoreHoursDao;

    @Autowired
    private MerchantStoreStaffDao merchantStoreStaffDao;

    @Autowired
    private MerchantStoreDeliveryAreaDao merchantStoreDeliveryAreaDao;

    @Override
    public PageInfo<MerchantStore> getPageList(MerchantStoreSearchRequest request) {
        Page<MerchantStore> page = PageHelper.startPage(request.getPage(), request.getLimit());
        
        // 构建查询条件
        LambdaQueryWrapper<MerchantStore> wrapper = new LambdaQueryWrapper<>();
        
        // 基本条件
        if (ObjectUtil.isNotNull(request.getMerId())) {
            wrapper.eq(MerchantStore::getMerId, request.getMerId());
        }
        if (StrUtil.isNotBlank(request.getStoreCode())) {
            wrapper.like(MerchantStore::getStoreCode, request.getStoreCode());
        }
        if (StrUtil.isNotBlank(request.getStoreName())) {
            wrapper.like(MerchantStore::getStoreName, request.getStoreName());
        }
        if (StrUtil.isNotBlank(request.getStoreType())) {
            wrapper.eq(MerchantStore::getStoreType, request.getStoreType());
        }
        if (StrUtil.isNotBlank(request.getContactPerson())) {
            wrapper.like(MerchantStore::getContactPerson, request.getContactPerson());
        }
        if (StrUtil.isNotBlank(request.getContactPhone())) {
            wrapper.like(MerchantStore::getContactPhone, request.getContactPhone());
        }
        if (StrUtil.isNotBlank(request.getProvince())) {
            wrapper.eq(MerchantStore::getProvince, request.getProvince());
        }
        if (StrUtil.isNotBlank(request.getCity())) {
            wrapper.eq(MerchantStore::getCity, request.getCity());
        }
        if (StrUtil.isNotBlank(request.getDistrict())) {
            wrapper.eq(MerchantStore::getDistrict, request.getDistrict());
        }
        if (ObjectUtil.isNotNull(request.getStatus())) {
            wrapper.eq(MerchantStore::getStatus, request.getStatus());
        }
        if (ObjectUtil.isNotNull(request.getIsMain())) {
            wrapper.eq(MerchantStore::getIsMain, request.getIsMain());
        }
        
        // 关键字搜索（门店名称、联系人、联系电话）
        if (StrUtil.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(MerchantStore::getStoreName, request.getKeyword())
                            .or().like(MerchantStore::getContactPerson, request.getKeyword())
                            .or().like(MerchantStore::getContactPhone, request.getKeyword()));
        }
        
        // 时间范围
        if (StrUtil.isNotBlank(request.getStartTime())) {
            wrapper.ge(MerchantStore::getCreateTime, request.getStartTime());
        }
        if (StrUtil.isNotBlank(request.getEndTime())) {
            wrapper.le(MerchantStore::getCreateTime, request.getEndTime());
        }
        
        // 排序
        if (StrUtil.isNotBlank(request.getOrderBy())) {
            String orderBy = request.getOrderBy();
            boolean isDesc = "desc".equals(request.getOrderDirection());
            
            // 根据字段名进行排序
            switch (orderBy) {
                case "createTime":
                case "create_time":
                    if (isDesc) wrapper.orderByDesc(MerchantStore::getCreateTime);
                    else wrapper.orderByAsc(MerchantStore::getCreateTime);
                    break;
                case "updateTime":
                case "update_time":
                    if (isDesc) wrapper.orderByDesc(MerchantStore::getUpdateTime);
                    else wrapper.orderByAsc(MerchantStore::getUpdateTime);
                    break;
                case "storeName":
                case "store_name":
                    if (isDesc) wrapper.orderByDesc(MerchantStore::getStoreName);
                    else wrapper.orderByAsc(MerchantStore::getStoreName);
                    break;
                case "storeCode":
                case "store_code":
                    if (isDesc) wrapper.orderByDesc(MerchantStore::getStoreCode);
                    else wrapper.orderByAsc(MerchantStore::getStoreCode);
                    break;
                default:
                    wrapper.orderByDesc(MerchantStore::getCreateTime);
                    break;
            }
        } else {
            wrapper.orderByDesc(MerchantStore::getCreateTime);
        }
        
        List<MerchantStore> list = list(wrapper);
        return CommonPage.copyPageInfo(page, list);
    }

    @Override
    public List<MerchantStore> getStoreListByMerId(Integer merId) {
        if (ObjectUtil.isNull(merId)) {
            throw new CrmebException("商户ID不能为空");
        }
        
        LambdaQueryWrapper<MerchantStore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantStore::getMerId, merId)
               .eq(MerchantStore::getStatus, 1) // 只返回启用状态的门店
               .orderByDesc(MerchantStore::getIsMain) // 主门店优先
               .orderByDesc(MerchantStore::getCreateTime);
        
        return list(wrapper);
    }

    @Override
    public MerchantStore getMainStoreByMerId(Integer merId) {
        if (ObjectUtil.isNull(merId)) {
            throw new CrmebException("商户ID不能为空");
        }
        
        LambdaQueryWrapper<MerchantStore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantStore::getMerId, merId)
               .eq(MerchantStore::getIsMain, 1)
               .eq(MerchantStore::getStatus, 1)
               .last("limit 1");
        
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord createStore(MerchantStoreRequest request) {
        // 参数验证
        if (ObjectUtil.isNull(request.getMerId())) {
            return new MyRecord().set("status", false).set("msg", "商户ID不能为空");
        }
        
        // 生成门店编码
        if (StrUtil.isBlank(request.getStoreCode())) {
            request.setStoreCode(generateStoreCode(request.getMerId()));
        }
        
        // 检查门店编码是否已存在
        if (checkStoreCodeExists(request.getMerId(), request.getStoreCode(), null)) {
            return new MyRecord().set("status", false).set("msg", "门店编码已存在");
        }
        
        // 创建门店对象
        MerchantStore store = new MerchantStore();
        BeanUtils.copyProperties(request, store);
        
        // 生成完整地址
        store.setFullAddress(request.getProvince() + request.getCity() + 
                           request.getDistrict() + request.getAddressDetail());
        
        // 如果设置为主门店，需要先取消其他主门店
        if (request.getIsMain() != null && request.getIsMain() == 1) {
            cancelOtherMainStore(request.getMerId());
        }
        
        // 保存门店
        store.setCreateTime(LocalDateTime.now());
        store.setUpdateTime(LocalDateTime.now());
        boolean success = save(store);
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "门店创建成功").set("data", store);
        } else {
            return new MyRecord().set("status", false).set("msg", "门店创建失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord updateStore(Integer id, MerchantStoreRequest request) {
        if (ObjectUtil.isNull(id)) {
            return new MyRecord().set("status", false).set("msg", "门店ID不能为空");
        }
        
        // 检查门店是否存在
        MerchantStore existStore = getById(id);
        if (ObjectUtil.isNull(existStore)) {
            return new MyRecord().set("status", false).set("msg", "门店不存在");
        }
        
        // 检查门店编码是否已存在（排除当前门店）
        if (StrUtil.isNotBlank(request.getStoreCode()) && 
            checkStoreCodeExists(request.getMerId(), request.getStoreCode(), id)) {
            return new MyRecord().set("status", false).set("msg", "门店编码已存在");
        }
        
        // 更新门店信息
        MerchantStore store = new MerchantStore();
        BeanUtils.copyProperties(request, store);
        store.setId(id);
        
        // 生成完整地址
        store.setFullAddress(request.getProvince() + request.getCity() + 
                           request.getDistrict() + request.getAddressDetail());
        
        // 如果设置为主门店，需要先取消其他主门店
        if (request.getIsMain() != null && request.getIsMain() == 1 && 
            !existStore.getIsMain().equals(1)) {
            cancelOtherMainStore(request.getMerId());
        }
        
        store.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(store);
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "门店更新成功").set("data", store);
        } else {
            return new MyRecord().set("status", false).set("msg", "门店更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord deleteStore(Integer id) {
        if (ObjectUtil.isNull(id)) {
            return new MyRecord().set("status", false).set("msg", "门店ID不能为空");
        }
        
        // 检查门店是否存在
        MerchantStore store = getById(id);
        if (ObjectUtil.isNull(store)) {
            return new MyRecord().set("status", false).set("msg", "门店不存在");
        }
        
        // 检查是否为主门店
        if (store.getIsMain() != null && store.getIsMain() == 1) {
            return new MyRecord().set("status", false).set("msg", "主门店不能删除，请先设置其他门店为主门店");
        }
        
        try {
            // 删除相关数据
            // 删除营业时间
            LambdaQueryWrapper<MerchantStoreHours> hoursWrapper = new LambdaQueryWrapper<>();
            hoursWrapper.eq(MerchantStoreHours::getStoreId, id);
            merchantStoreHoursDao.delete(hoursWrapper);
            
            // 删除员工
            LambdaQueryWrapper<MerchantStoreStaff> staffWrapper = new LambdaQueryWrapper<>();
            staffWrapper.eq(MerchantStoreStaff::getStoreId, id);
            merchantStoreStaffDao.delete(staffWrapper);
            
            // 删除配送区域
            LambdaQueryWrapper<MerchantStoreDeliveryArea> areaWrapper = new LambdaQueryWrapper<>();
            areaWrapper.eq(MerchantStoreDeliveryArea::getStoreId, id);
            merchantStoreDeliveryAreaDao.delete(areaWrapper);
            
            // 删除门店
            boolean success = removeById(id);
            
            if (success) {
                return new MyRecord().set("status", true).set("msg", "门店删除成功");
            } else {
                return new MyRecord().set("status", false).set("msg", "门店删除失败");
            }
        } catch (Exception e) {
            log.error("删除门店失败", e);
            return new MyRecord().set("status", false).set("msg", "删除门店失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord batchDeleteStore(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new MyRecord().set("status", false).set("msg", "门店ID列表不能为空");
        }
        
        // 检查是否包含主门店
        LambdaQueryWrapper<MerchantStore> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MerchantStore::getId, ids)
               .eq(MerchantStore::getIsMain, 1);
        List<MerchantStore> mainStores = list(wrapper);
        if (CollUtil.isNotEmpty(mainStores)) {
            return new MyRecord().set("status", false).set("msg", "不能删除主门店，请先设置其他门店为主门店");
        }
        
        try {
            // 批量删除相关数据
            for (Integer id : ids) {
                // 删除营业时间
                LambdaQueryWrapper<MerchantStoreHours> hoursWrapper = new LambdaQueryWrapper<>();
                hoursWrapper.eq(MerchantStoreHours::getStoreId, id);
                merchantStoreHoursDao.delete(hoursWrapper);
                
                // 删除员工
                LambdaQueryWrapper<MerchantStoreStaff> staffWrapper = new LambdaQueryWrapper<>();
                staffWrapper.eq(MerchantStoreStaff::getStoreId, id);
                merchantStoreStaffDao.delete(staffWrapper);
                
                // 删除配送区域
                LambdaQueryWrapper<MerchantStoreDeliveryArea> areaWrapper = new LambdaQueryWrapper<>();
                areaWrapper.eq(MerchantStoreDeliveryArea::getStoreId, id);
                merchantStoreDeliveryAreaDao.delete(areaWrapper);
            }
            
            // 批量删除门店
            boolean success = removeByIds(ids);
            
            if (success) {
                return new MyRecord().set("status", true).set("msg", "门店批量删除成功");
            } else {
                return new MyRecord().set("status", false).set("msg", "门店批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除门店失败", e);
            return new MyRecord().set("status", false).set("msg", "批量删除门店失败：" + e.getMessage());
        }
    }

    @Override
    public MyRecord updateStoreStatus(Integer id, Integer status) {
        if (ObjectUtil.isNull(id) || ObjectUtil.isNull(status)) {
            return new MyRecord().set("status", false).set("msg", "参数不能为空");
        }
        
        // 检查门店是否存在
        MerchantStore store = getById(id);
        if (ObjectUtil.isNull(store)) {
            return new MyRecord().set("status", false).set("msg", "门店不存在");
        }
        
        // 检查主门店不能停用
        if (store.getIsMain() != null && store.getIsMain() == 1 && status == 0) {
            return new MyRecord().set("status", false).set("msg", "主门店不能停用");
        }
        
        LambdaUpdateWrapper<MerchantStore> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MerchantStore::getId, id)
               .set(MerchantStore::getStatus, status)
               .set(MerchantStore::getUpdateTime, LocalDateTime.now());
        
        boolean success = update(wrapper);
        
        if (success) {
            String statusName = status == 1 ? "启用" : "停用";
            return new MyRecord().set("status", true).set("msg", "门店" + statusName + "成功");
        } else {
            return new MyRecord().set("status", false).set("msg", "更新门店状态失败");
        }
    }

    @Override
    public MyRecord batchUpdateStoreStatus(List<Integer> ids, Integer status) {
        if (CollUtil.isEmpty(ids) || ObjectUtil.isNull(status)) {
            return new MyRecord().set("status", false).set("msg", "参数不能为空");
        }
        
        // 如果是停用操作，检查是否包含主门店
        if (status == 0) {
            LambdaQueryWrapper<MerchantStore> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(MerchantStore::getId, ids)
                   .eq(MerchantStore::getIsMain, 1);
            List<MerchantStore> mainStores = list(wrapper);
            if (CollUtil.isNotEmpty(mainStores)) {
                return new MyRecord().set("status", false).set("msg", "主门店不能停用");
            }
        }
        
        LambdaUpdateWrapper<MerchantStore> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(MerchantStore::getId, ids)
                     .set(MerchantStore::getStatus, status)
                     .set(MerchantStore::getUpdateTime, LocalDateTime.now());
        
        boolean success = update(updateWrapper);
        
        if (success) {
            String statusName = status == 1 ? "启用" : "停用";
            return new MyRecord().set("status", true).set("msg", "门店批量" + statusName + "成功，共更新" + ids.size() + "个门店");
        } else {
            return new MyRecord().set("status", false).set("msg", "批量更新门店状态失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord setMainStore(Integer merId, Integer storeId) {
        if (ObjectUtil.isNull(merId) || ObjectUtil.isNull(storeId)) {
            return new MyRecord().set("status", false).set("msg", "参数不能为空");
        }
        
        // 检查门店是否存在且属于该商户
        MerchantStore store = getById(storeId);
        if (ObjectUtil.isNull(store) || !store.getMerId().equals(merId)) {
            return new MyRecord().set("status", false).set("msg", "门店不存在或不属于该商户");
        }
        
        // 检查门店状态
        if (store.getStatus() == 0) {
            return new MyRecord().set("status", false).set("msg", "停用状态的门店不能设置为主门店");
        }
        
        try {
            // 先取消其他主门店
            cancelOtherMainStore(merId);
            
            // 设置当前门店为主门店
            LambdaUpdateWrapper<MerchantStore> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(MerchantStore::getId, storeId)
                   .set(MerchantStore::getIsMain, 1)
                   .set(MerchantStore::getUpdateTime, LocalDateTime.now());
            
            boolean success = update(wrapper);
            
            if (success) {
                return new MyRecord().set("status", true).set("msg", "主门店设置成功");
            } else {
                return new MyRecord().set("status", false).set("msg", "主门店设置失败");
            }
        } catch (Exception e) {
            log.error("设置主门店失败", e);
            return new MyRecord().set("status", false).set("msg", "设置主门店失败：" + e.getMessage());
        }
    }

    @Override
    public MerchantStore getStoreDetail(Integer id) {
        if (ObjectUtil.isNull(id)) {
            throw new CrmebException("门店ID不能为空");
        }
        
        MerchantStore store = getById(id);
        if (ObjectUtil.isNull(store)) {
            throw new CrmebException("门店不存在");
        }
        
        // 获取关联信息
        store.setStoreHours(getStoreHours(id));
        store.setStoreStaff(getStoreStaff(id));
        store.setDeliveryAreas(getStoreDeliveryAreas(id));
        
        return store;
    }

    @Override
    public List<MerchantStore> getNearbyStores(Double latitude, Double longitude, Double radius, Integer limit) {
        if (ObjectUtil.isNull(latitude) || ObjectUtil.isNull(longitude)) {
            throw new CrmebException("经纬度不能为空");
        }
        
        if (ObjectUtil.isNull(radius)) {
            radius = 10.0; // 默认10公里
        }
        
        if (ObjectUtil.isNull(limit)) {
            limit = 20; // 默认返回20个
        }
        
        // 计算经纬度范围
        Double latRange = radius / 111.0; // 纬度1度约111公里
        Double lngRange = radius / (111.0 * Math.cos(Math.toRadians(latitude))); // 经度范围根据纬度调整
        
        Double minLat = latitude - latRange;
        Double maxLat = latitude + latRange;
        Double minLng = longitude - lngRange;
        Double maxLng = longitude + lngRange;
        
        LambdaQueryWrapper<MerchantStore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantStore::getStatus, 1) // 只查询启用状态
               .isNotNull(MerchantStore::getLatitude)
               .isNotNull(MerchantStore::getLongitude)
               .between(MerchantStore::getLatitude, minLat, maxLat)
               .between(MerchantStore::getLongitude, minLng, maxLng)
               .orderByDesc(MerchantStore::getIsMain) // 主门店优先
               .orderByDesc(MerchantStore::getCreateTime)
               .last("limit " + limit);
        
        return list(wrapper);
    }

    @Override
    public Map<String, Object> getStoreStatistics(Integer storeId) {
        if (ObjectUtil.isNull(storeId)) {
            throw new CrmebException("门店ID不能为空");
        }
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取门店基本信息
        MerchantStore store = getById(storeId);
        if (ObjectUtil.isNull(store)) {
            throw new CrmebException("门店不存在");
        }
        
        // 员工数量
        LambdaQueryWrapper<MerchantStoreStaff> staffWrapper = new LambdaQueryWrapper<>();
        staffWrapper.eq(MerchantStoreStaff::getStoreId, storeId);
        int staffCount = Math.toIntExact(merchantStoreStaffDao.selectCount(staffWrapper));
        
        // 配送区域数量
        LambdaQueryWrapper<MerchantStoreDeliveryArea> areaWrapper = new LambdaQueryWrapper<>();
        areaWrapper.eq(MerchantStoreDeliveryArea::getStoreId, storeId);
        int deliveryAreaCount = Math.toIntExact(merchantStoreDeliveryAreaDao.selectCount(areaWrapper));
        
        statistics.put("storeId", storeId);
        statistics.put("storeName", store.getStoreName());
        statistics.put("staffCount", staffCount);
        statistics.put("deliveryAreaCount", deliveryAreaCount);
        statistics.put("isMain", store.getIsMain());
        statistics.put("status", store.getStatus());
        statistics.put("createTime", store.getCreateTime());
        
        return statistics;
    }

    @Override
    public Map<String, Object> getMerchantStoreCount(Integer merId) {
        if (ObjectUtil.isNull(merId)) {
            throw new CrmebException("商户ID不能为空");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 总门店数
        LambdaQueryWrapper<MerchantStore> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(MerchantStore::getMerId, merId);
        int totalCount = Math.toIntExact(count(totalWrapper));
        
        // 启用门店数
        LambdaQueryWrapper<MerchantStore> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(MerchantStore::getMerId, merId)
                    .eq(MerchantStore::getStatus, 1);
        int activeCount = Math.toIntExact(count(activeWrapper));
        
        // 停用门店数
        int inactiveCount = totalCount - activeCount;
        
        // 主门店数（应该只有1个或0个）
        LambdaQueryWrapper<MerchantStore> mainWrapper = new LambdaQueryWrapper<>();
        mainWrapper.eq(MerchantStore::getMerId, merId)
                  .eq(MerchantStore::getIsMain, 1);
        int mainCount = Math.toIntExact(count(mainWrapper));
        
        result.put("merId", merId);
        result.put("totalCount", totalCount);
        result.put("activeCount", activeCount);
        result.put("inactiveCount", inactiveCount);
        result.put("mainCount", mainCount);
        
        return result;
    }

    @Override
    public boolean checkStoreCodeExists(Integer merId, String storeCode, Integer excludeId) {
        if (ObjectUtil.isNull(merId) || StrUtil.isBlank(storeCode)) {
            return false;
        }
        
        LambdaQueryWrapper<MerchantStore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantStore::getMerId, merId)
               .eq(MerchantStore::getStoreCode, storeCode);
        
        if (ObjectUtil.isNotNull(excludeId)) {
            wrapper.ne(MerchantStore::getId, excludeId);
        }
        
        int count = count(wrapper);
        return count > 0;
    }

    @Override
    public String generateStoreCode(Integer merId) {
        if (ObjectUtil.isNull(merId)) {
            throw new CrmebException("商户ID不能为空");
        }
        
        // 生成格式：STORE_商户ID_时间戳后6位
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = timestamp.substring(timestamp.length() - 6);
        return "STORE_" + merId + "_" + suffix;
    }

    // 营业时间管理
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord setStoreHours(Integer storeId, List<MerchantStoreHours> storeHours) {
        if (ObjectUtil.isNull(storeId)) {
            return new MyRecord().set("status", false).set("msg", "门店ID不能为空");
        }
        
        // 检查门店是否存在
        MerchantStore store = getById(storeId);
        if (ObjectUtil.isNull(store)) {
            return new MyRecord().set("status", false).set("msg", "门店不存在");
        }
        
        try {
            // 先删除原有配置
            LambdaQueryWrapper<MerchantStoreHours> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(MerchantStoreHours::getStoreId, storeId);
            merchantStoreHoursDao.delete(deleteWrapper);
            
            // 插入新配置
            if (CollUtil.isNotEmpty(storeHours)) {
                for (MerchantStoreHours hours : storeHours) {
                    hours.setStoreId(storeId);
                    hours.setCreateTime(LocalDateTime.now());
                    hours.setUpdateTime(LocalDateTime.now());
                    merchantStoreHoursDao.insert(hours);
                }
            }
            
            return new MyRecord().set("status", true).set("msg", "营业时间设置成功");
        } catch (Exception e) {
            log.error("设置营业时间失败", e);
            return new MyRecord().set("status", false).set("msg", "设置营业时间失败：" + e.getMessage());
        }
    }

    @Override
    public List<MerchantStoreHours> getStoreHours(Integer storeId) {
        if (ObjectUtil.isNull(storeId)) {
            throw new CrmebException("门店ID不能为空");
        }
        
        LambdaQueryWrapper<MerchantStoreHours> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantStoreHours::getStoreId, storeId)
               .orderByAsc(MerchantStoreHours::getDayOfWeek)
               .orderByAsc(MerchantStoreHours::getCreateTime);
        
        return merchantStoreHoursDao.selectList(wrapper);
    }

    // 员工管理
    @Override
    public MyRecord addStoreStaff(MerchantStoreStaff storeStaff) {
        if (ObjectUtil.isNull(storeStaff) || ObjectUtil.isNull(storeStaff.getStoreId())) {
            return new MyRecord().set("status", false).set("msg", "门店员工信息不能为空");
        }
        
        // 检查门店是否存在
        MerchantStore store = getById(storeStaff.getStoreId());
        if (ObjectUtil.isNull(store)) {
            return new MyRecord().set("status", false).set("msg", "门店不存在");
        }
        
        // 检查员工电话是否已存在
        LambdaQueryWrapper<MerchantStoreStaff> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(MerchantStoreStaff::getStaffPhone, storeStaff.getStaffPhone());
        int count = Math.toIntExact(merchantStoreStaffDao.selectCount(phoneWrapper));
        if (count > 0) {
            return new MyRecord().set("status", false).set("msg", "员工电话已存在");
        }
        
        storeStaff.setCreateTime(LocalDateTime.now());
        storeStaff.setUpdateTime(LocalDateTime.now());
        
        boolean success = merchantStoreStaffDao.insert(storeStaff) > 0;
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "员工添加成功").set("data", storeStaff);
        } else {
            return new MyRecord().set("status", false).set("msg", "员工添加失败");
        }
    }

    @Override
    public MyRecord updateStoreStaff(MerchantStoreStaff storeStaff) {
        if (ObjectUtil.isNull(storeStaff) || ObjectUtil.isNull(storeStaff.getId())) {
            return new MyRecord().set("status", false).set("msg", "员工信息不能为空");
        }
        
        // 检查员工是否存在
        MerchantStoreStaff existStaff = merchantStoreStaffDao.selectById(storeStaff.getId());
        if (ObjectUtil.isNull(existStaff)) {
            return new MyRecord().set("status", false).set("msg", "员工不存在");
        }
        
        // 检查员工电话是否已存在（排除当前员工）
        LambdaQueryWrapper<MerchantStoreStaff> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(MerchantStoreStaff::getStaffPhone, storeStaff.getStaffPhone())
                    .ne(MerchantStoreStaff::getId, storeStaff.getId());
        int count = Math.toIntExact(merchantStoreStaffDao.selectCount(phoneWrapper));
        if (count > 0) {
            return new MyRecord().set("status", false).set("msg", "员工电话已存在");
        }
        
        storeStaff.setUpdateTime(LocalDateTime.now());
        
        boolean success = merchantStoreStaffDao.updateById(storeStaff) > 0;
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "员工更新成功").set("data", storeStaff);
        } else {
            return new MyRecord().set("status", false).set("msg", "员工更新失败");
        }
    }

    @Override
    public MyRecord deleteStoreStaff(Integer staffId) {
        if (ObjectUtil.isNull(staffId)) {
            return new MyRecord().set("status", false).set("msg", "员工ID不能为空");
        }
        
        // 检查员工是否存在
        MerchantStoreStaff staff = merchantStoreStaffDao.selectById(staffId);
        if (ObjectUtil.isNull(staff)) {
            return new MyRecord().set("status", false).set("msg", "员工不存在");
        }
        
        boolean success = merchantStoreStaffDao.deleteById(staffId) > 0;
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "员工删除成功");
        } else {
            return new MyRecord().set("status", false).set("msg", "员工删除失败");
        }
    }

    @Override
    public List<MerchantStoreStaff> getStoreStaff(Integer storeId) {
        if (ObjectUtil.isNull(storeId)) {
            throw new CrmebException("门店ID不能为空");
        }
        
        LambdaQueryWrapper<MerchantStoreStaff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantStoreStaff::getStoreId, storeId)
               .orderByDesc(MerchantStoreStaff::getCreateTime);
        
        return merchantStoreStaffDao.selectList(wrapper);
    }

    // 配送范围管理
    @Override
    public MyRecord addDeliveryArea(MerchantStoreDeliveryArea deliveryArea) {
        if (ObjectUtil.isNull(deliveryArea) || ObjectUtil.isNull(deliveryArea.getStoreId())) {
            return new MyRecord().set("status", false).set("msg", "配送范围信息不能为空");
        }
        
        // 检查门店是否存在
        MerchantStore store = getById(deliveryArea.getStoreId());
        if (ObjectUtil.isNull(store)) {
            return new MyRecord().set("status", false).set("msg", "门店不存在");
        }
        
        deliveryArea.setCreateTime(LocalDateTime.now());
        deliveryArea.setUpdateTime(LocalDateTime.now());
        
        boolean success = merchantStoreDeliveryAreaDao.insert(deliveryArea) > 0;
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "配送范围添加成功").set("data", deliveryArea);
        } else {
            return new MyRecord().set("status", false).set("msg", "配送范围添加失败");
        }
    }

    @Override
    public MyRecord updateDeliveryArea(MerchantStoreDeliveryArea deliveryArea) {
        if (ObjectUtil.isNull(deliveryArea) || ObjectUtil.isNull(deliveryArea.getId())) {
            return new MyRecord().set("status", false).set("msg", "配送范围信息不能为空");
        }
        
        // 检查配送范围是否存在
        MerchantStoreDeliveryArea existArea = merchantStoreDeliveryAreaDao.selectById(deliveryArea.getId());
        if (ObjectUtil.isNull(existArea)) {
            return new MyRecord().set("status", false).set("msg", "配送范围不存在");
        }
        
        deliveryArea.setUpdateTime(LocalDateTime.now());
        
        boolean success = merchantStoreDeliveryAreaDao.updateById(deliveryArea) > 0;
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "配送范围更新成功").set("data", deliveryArea);
        } else {
            return new MyRecord().set("status", false).set("msg", "配送范围更新失败");
        }
    }

    @Override
    public MyRecord deleteDeliveryArea(Integer areaId) {
        if (ObjectUtil.isNull(areaId)) {
            return new MyRecord().set("status", false).set("msg", "配送范围ID不能为空");
        }
        
        // 检查配送范围是否存在
        MerchantStoreDeliveryArea area = merchantStoreDeliveryAreaDao.selectById(areaId);
        if (ObjectUtil.isNull(area)) {
            return new MyRecord().set("status", false).set("msg", "配送范围不存在");
        }
        
        boolean success = merchantStoreDeliveryAreaDao.deleteById(areaId) > 0;
        
        if (success) {
            return new MyRecord().set("status", true).set("msg", "配送范围删除成功");
        } else {
            return new MyRecord().set("status", false).set("msg", "配送范围删除失败");
        }
    }

    @Override
    public List<MerchantStoreDeliveryArea> getStoreDeliveryAreas(Integer storeId) {
        if (ObjectUtil.isNull(storeId)) {
            throw new CrmebException("门店ID不能为空");
        }
        
        LambdaQueryWrapper<MerchantStoreDeliveryArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantStoreDeliveryArea::getStoreId, storeId)
               .orderByDesc(MerchantStoreDeliveryArea::getCreateTime);
        
        return merchantStoreDeliveryAreaDao.selectList(wrapper);
    }

    @Override
    public MerchantStoreDeliveryArea checkLocationInDeliveryArea(Integer storeId, BigDecimal latitude, BigDecimal longitude) {
        if (ObjectUtil.isNull(storeId) || ObjectUtil.isNull(latitude) || ObjectUtil.isNull(longitude)) {
            return null;
        }
        
        // 获取该门店的所有配送区域
        List<MerchantStoreDeliveryArea> areas = getStoreDeliveryAreas(storeId);
        
        for (MerchantStoreDeliveryArea area : areas) {
            // 简化的位置判断，实际项目中应该实现复杂的地理位置算法
            if ("CIRCLE".equals(area.getAreaType())) {
                // 圆形区域判断（简化版）
                BigDecimal centerLat = area.getCenterLatitude();
                BigDecimal centerLng = area.getCenterLongitude();
                BigDecimal radius = area.getRadius();
                
                if (ObjectUtil.isNotNull(centerLat) && ObjectUtil.isNotNull(centerLng) && ObjectUtil.isNotNull(radius)) {
                    // 简单的距离计算（实际应该使用地理距离公式）
                    double latDiff = Math.abs(latitude.doubleValue() - centerLat.doubleValue());
                    double lngDiff = Math.abs(longitude.doubleValue() - centerLng.doubleValue());
                    double distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
                    
                    if (distance <= radius.doubleValue() / 111000.0) { // 粗略换算为度
                        return area;
                    }
                }
            }
            // 多边形区域判断可以在这里添加
        }
        
        return null;
    }

    @Override
    public Map<String, Object> calculateDeliveryFee(Integer storeId, BigDecimal latitude, BigDecimal longitude, BigDecimal orderAmount) {
        if (ObjectUtil.isNull(storeId) || ObjectUtil.isNull(latitude) || ObjectUtil.isNull(longitude)) {
            throw new CrmebException("参数不能为空");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查位置是否在配送范围内
        MerchantStoreDeliveryArea area = checkLocationInDeliveryArea(storeId, latitude, longitude);
        
        if (ObjectUtil.isNull(area)) {
            result.put("canDeliver", false);
            result.put("message", "不在配送范围内");
            result.put("deliveryFee", BigDecimal.ZERO);
            return result;
        }
        
        // 计算配送费用
        BigDecimal deliveryFee = area.getDeliveryFee();
        if (ObjectUtil.isNull(deliveryFee)) {
            deliveryFee = BigDecimal.ZERO;
        }
        
        // 检查是否满足免配送费条件
        BigDecimal freeDeliveryAmount = area.getFreeDeliveryAmount();
        if (ObjectUtil.isNotNull(freeDeliveryAmount) && ObjectUtil.isNotNull(orderAmount) && 
            orderAmount.compareTo(freeDeliveryAmount) >= 0) {
            deliveryFee = BigDecimal.ZERO;
        }
        
        result.put("canDeliver", true);
        result.put("deliveryFee", deliveryFee);
        result.put("freeDeliveryAmount", freeDeliveryAmount);
        result.put("areaName", area.getAreaName());
        result.put("message", "配送费用计算成功");
        
        return result;
    }

    /**
     * 取消其他主门店
     */
    private void cancelOtherMainStore(Integer merId) {
        LambdaUpdateWrapper<MerchantStore> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MerchantStore::getMerId, merId)
               .eq(MerchantStore::getIsMain, 1)
               .set(MerchantStore::getIsMain, 0)
               .set(MerchantStore::getUpdateTime, LocalDateTime.now());
        update(wrapper);
    }
} 