package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.DateConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.CityDeliveryDriver;
import com.zbkj.common.model.order.CityDeliveryFeeRule;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.model.order.CityDeliveryArea;
import com.zbkj.common.response.CityDeliveryOrderResponse;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.CityDeliveryOrderRequest;
import com.zbkj.common.request.CityDeliveryOrderListRequest;
import com.zbkj.service.dao.CityDeliveryOrderDao;
import com.zbkj.service.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.order.CityDeliveryTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zbkj.common.model.order.Order;
import com.zbkj.service.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.Page;
import com.zbkj.common.page.CommonPage;

/**
 * 同城配送服务实现类
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
public class CityDeliveryServiceImpl extends ServiceImpl<CityDeliveryOrderDao, CityDeliveryOrder> implements CityDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(CityDeliveryServiceImpl.class);

    @Autowired
    private CityDeliveryDriverService cityDeliveryDriverService;
    
    @Autowired
    private CityDeliveryAreaService cityDeliveryAreaService;
    
    @Autowired
    private CityDeliveryFeeRuleService cityDeliveryFeeRuleService;
    
    @Autowired
    private CityDeliveryDispatchService cityDeliveryDispatchService;
    
    @Autowired
    private TencentMapService tencentMapService;

    @Autowired
    private CityDeliveryOrderDao cityDeliveryOrderDao;

    // 暂时注释掉CityDeliveryTrackService相关代码，因为该服务尚未实现
    // @Autowired
    // private CityDeliveryTrackService cityDeliveryTrackService;

    @Autowired
    private OrderService orderService;

    /**
     * 创建同城配送订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CityDeliveryOrder createDeliveryOrder(String orderNo, Integer merId, Integer uid,
                                                String pickupAddress, String pickupContact, String pickupPhone,
                                                String deliveryAddress, String deliveryContact, String deliveryPhone,
                                                Integer deliveryType, String scheduledTime, 
                                                BigDecimal longitude, BigDecimal latitude, String deliveryRemark) {
        try {
            // 检查是否已存在配送订单
            QueryWrapper<CityDeliveryOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_no", orderNo).eq("is_del", false);
            CityDeliveryOrder existOrder = getOne(queryWrapper);
            if (ObjectUtil.isNotNull(existOrder)) {
                throw new CrmebException("订单已存在配送记录");
            }

            // 生成配送单号
            String deliveryOrderNo = generateDeliveryOrderNo();

            // 创建配送订单
            CityDeliveryOrder deliveryOrder = new CityDeliveryOrder();
            deliveryOrder.setDeliveryOrderNo(deliveryOrderNo);
            deliveryOrder.setOrderNo(orderNo);
            deliveryOrder.setMerId(merId);
            deliveryOrder.setUserId(uid);
            deliveryOrder.setPickupAddress(pickupAddress);
            deliveryOrder.setPickupContact(pickupContact);
            deliveryOrder.setPickupPhone(pickupPhone);
            deliveryOrder.setDeliveryAddress(deliveryAddress);
            deliveryOrder.setDeliveryContact(deliveryContact);
            deliveryOrder.setDeliveryContactPhone(deliveryPhone);
            deliveryOrder.setDeliveryType(deliveryType);
            deliveryOrder.setDeliveryStatus(0); // 0-待分配
            // 如果 scheduledTime 是字符串格式，需要转换为Date类型
            // 这里先设置为空，需要根据实际业务逻辑处理
            // deliveryOrder.setEstimatedDeliveryTime(convertStringToDate(scheduledTime));
            deliveryOrder.setDeliveryLongitude(longitude);
            deliveryOrder.setDeliveryLatitude(latitude);
            deliveryOrder.setDeliveryRemark(deliveryRemark);
            deliveryOrder.setCreateTime(new Date());
            deliveryOrder.setUpdateTime(new Date());
            deliveryOrder.setIsDel(0);

            // 解析取件地址的经纬度
            try {
                Map<String, BigDecimal> pickupLocation = tencentMapService.geocoding(pickupAddress);
                if (pickupLocation != null) {
                    deliveryOrder.setPickupLongitude(pickupLocation.get("longitude"));
                    deliveryOrder.setPickupLatitude(pickupLocation.get("latitude"));
                }
                
                // 如果没有提供收货地址的经纬度，则进行地址解析
                if (longitude == null || latitude == null) {
                    Map<String, BigDecimal> deliveryLocation = tencentMapService.geocoding(deliveryAddress);
                    if (deliveryLocation != null) {
                        deliveryOrder.setDeliveryLongitude(deliveryLocation.get("longitude"));
                        deliveryOrder.setDeliveryLatitude(deliveryLocation.get("latitude"));
                    }
                }
                
                // 计算配送费用
                BigDecimal deliveryFee = calculateDeliveryFee(pickupAddress, deliveryAddress, deliveryType);
                deliveryOrder.setDeliveryFee(deliveryFee);
                
                // 计算配送距离
                if (deliveryOrder.getPickupLongitude() != null && deliveryOrder.getPickupLatitude() != null &&
                    deliveryOrder.getDeliveryLongitude() != null && deliveryOrder.getDeliveryLatitude() != null) {
                    BigDecimal distance = tencentMapService.calculateDistanceByCoordinates(
                        deliveryOrder.getPickupLatitude(), deliveryOrder.getPickupLongitude(),
                        deliveryOrder.getDeliveryLatitude(), deliveryOrder.getDeliveryLongitude()
                    );
                    deliveryOrder.setDeliveryDistance(distance);
                }
                
            } catch (Exception e) {
                // 地址解析失败不影响订单创建
                System.err.println("地址解析失败：" + e.getMessage());
            }

            // 保存配送订单
            save(deliveryOrder);

            // 添加初始轨迹记录
            addDeliveryTrack(deliveryOrderNo, null, null, "配送订单已创建", "配送订单已创建，等待分配配送员");

            return deliveryOrder;
        } catch (Exception e) {
            throw new CrmebException("创建配送订单失败：" + e.getMessage());
        }
    }

    @Override
    public CityDeliveryOrder createDeliveryOrderFromRequest(CityDeliveryOrderRequest request, Integer merId) {
        try {
            // 获取原订单信息
            Order order = orderService.getByOrderNo(request.getOrderNo());
            if (order == null) {
                throw new CrmebException("原订单不存在");
            }

            // 计算配送距离和费用
            BigDecimal distance = request.getDistance();
            BigDecimal deliveryFee = request.getDeliveryFee();
            
            if (distance == null || deliveryFee == null) {
                // 如果没有传入距离和费用，则重新计算
                deliveryFee = calculateDeliveryFee(request.getPickupAddress(), request.getDeliveryAddress(), request.getDeliveryType());
            }

            // 创建配送订单（收货人信息从请求中获取，如果没有则使用默认值）
            CityDeliveryOrder deliveryOrder = createDeliveryOrder(
                request.getOrderNo(),
                merId,
                order.getUid(),
                request.getPickupAddress(),
                request.getPickupContact(),
                request.getPickupPhone(),
                request.getDeliveryAddress() != null ? request.getDeliveryAddress() : "待补充收货地址",
                request.getDeliveryContact() != null ? request.getDeliveryContact() : "待补充收货人",
                request.getDeliveryPhone() != null ? request.getDeliveryPhone() : "待补充收货电话",
                request.getDeliveryType(),
                request.getScheduledTime(),
                request.getPickupLongitude(),
                request.getPickupLatitude(),
                request.getDeliveryRemark()
            );

            // 处理配送员分配
            if (request.getDriverId() != null) {
                // 手动指定配送员
                CityDeliveryDriver driver = cityDeliveryDriverService.getById(request.getDriverId());
                if (driver != null) {
                    assignDriver(deliveryOrder.getDeliveryOrderNo(), request.getDriverId(), 
                               driver.getName(), driver.getPhone());
                }
            } else {
                // 自动分配配送员
                try {
                    autoAssignDriver(deliveryOrder.getDeliveryOrderNo());
                } catch (Exception e) {
                    logger.warn("自动分配配送员失败: {}", e.getMessage());
                    // 自动分配失败不影响订单创建，可以后续手动分配
                }
            }

            return deliveryOrder;
        } catch (Exception e) {
            logger.error("根据请求创建配送订单失败: {}", e.getMessage(), e);
            throw new CrmebException("创建配送订单失败: " + e.getMessage());
        }
    }

    /**
     * 分配配送员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignDriver(String deliveryOrderNo, Integer driverId, String driverName, String driverPhone) {
        try {
            // 获取配送订单
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            if (!deliveryOrder.getDeliveryStatus().equals(1)) {
                throw new CrmebException("订单状态不允许分配配送员");
            }

            // 检查配送员状态
            CityDeliveryDriver driver = cityDeliveryDriverService.getById(driverId);
            if (ObjectUtil.isNull(driver) || !driver.getStatus().equals(1)) {
                throw new CrmebException("配送员不可用");
            }

            // 更新配送订单
            deliveryOrder.setDriverId(driverId);
            deliveryOrder.setDriverName(driverName);
            deliveryOrder.setDriverPhone(driverPhone);
            deliveryOrder.setDeliveryStatus(2); // 2-已分配
            deliveryOrder.setUpdateTime(new Date());
            updateById(deliveryOrder);

            // 添加轨迹记录
            addDeliveryTrack(deliveryOrderNo, null, null, 
                "已分配配送员", "配送员 " + driverName + " 已接单，正在前往取件");

            return true;
        } catch (Exception e) {
            throw new CrmebException("分配配送员失败：" + e.getMessage());
        }
    }

    /**
     * 更新配送状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDeliveryStatus(String deliveryOrderNo, Integer deliveryStatus, String remark) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            Integer oldStatus = deliveryOrder.getDeliveryStatus();
            deliveryOrder.setDeliveryStatus(deliveryStatus);
            deliveryOrder.setUpdateTime(new Date());

            // 根据状态设置相应的时间
            switch (deliveryStatus) {
                case 3: // 取件中
                    deliveryOrder.setPickupTime(new Date());
                    break;
                case 4: // 配送中
                    deliveryOrder.setStartDeliveryTime(new Date());
                    break;
                case 5: // 已送达
                    deliveryOrder.setFinishTime(new Date());
                    break;
                case 6: // 已取消
                    deliveryOrder.setCancelTime(new Date());
                    deliveryOrder.setCancelReason(remark);
                    break;
                case 7: // 异常
                    deliveryOrder.setExceptionTime(new Date());
                    deliveryOrder.setExceptionReason(remark);
                    break;
            }

            updateById(deliveryOrder);

            // 添加状态变更轨迹
            String statusDesc = getStatusDescription(deliveryStatus);
            addDeliveryTrack(deliveryOrderNo, null, null, statusDesc, 
                StrUtil.isNotBlank(remark) ? remark : statusDesc);

            return true;
        } catch (Exception e) {
            throw new CrmebException("更新配送状态失败：" + e.getMessage());
        }
    }

    /**
     * 添加配送轨迹
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addDeliveryTrack(String deliveryOrderNo, BigDecimal longitude, BigDecimal latitude, 
                                  String address, String status) {
        try {
            // 这里应该调用配送轨迹服务，暂时简化处理
            // 可以将轨迹信息存储到单独的轨迹表中
            return true;
        } catch (Exception e) {
            System.err.println("添加配送轨迹失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 根据订单号获取配送信息
     */
    @Override
    public CityDeliveryOrderResponse getDeliveryOrderByOrderNo(String orderNo) {
        try {
            QueryWrapper<CityDeliveryOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_no", orderNo).eq("is_del", false);
            CityDeliveryOrder deliveryOrder = getOne(queryWrapper);
            
            if (ObjectUtil.isNull(deliveryOrder)) {
                return null;
            }

            return convertToResponse(deliveryOrder);
        } catch (Exception e) {
            System.err.println("获取配送订单失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 根据配送单号获取配送信息
     */
    @Override
    public CityDeliveryOrderResponse getDeliveryOrderByDeliveryNo(String deliveryOrderNo) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                return null;
            }

            return convertToResponse(deliveryOrder);
        } catch (Exception e) {
            System.err.println("获取配送订单失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 取消配送订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelDeliveryOrder(String deliveryOrderNo, String cancelReason) {
        try {
            return updateDeliveryStatus(deliveryOrderNo, 6, cancelReason);
        } catch (Exception e) {
            throw new CrmebException("取消配送订单失败：" + e.getMessage());
        }
    }

    /**
     * 获取配送员的配送列表
     */
    @Override
    public List<CityDeliveryOrderResponse> getDriverDeliveryList(Integer driverId, Integer status) {
        try {
            QueryWrapper<CityDeliveryOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("driver_id", driverId).eq("is_del", false);
            
            if (ObjectUtil.isNotNull(status)) {
                queryWrapper.eq("delivery_status", status);
            }
            
            queryWrapper.orderByDesc("create_time");
            
            List<CityDeliveryOrder> deliveryOrders = list(queryWrapper);
            
            return deliveryOrders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取配送员配送列表失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 计算配送费用
     */
    @Override
    public BigDecimal calculateDeliveryFee(String pickupAddress, String deliveryAddress, Integer deliveryType) {
        try {
            // 检查是否在服务范围内
            if (!cityDeliveryAreaService.isAddressInServiceArea(pickupAddress) || 
                !cityDeliveryAreaService.isAddressInServiceArea(deliveryAddress)) {
                throw new CrmebException("地址不在配送服务范围内");
            }

            // 计算距离
            BigDecimal distance = tencentMapService.calculateDistance(pickupAddress, deliveryAddress);
            
            // 使用费用规则服务计算费用
            return cityDeliveryFeeRuleService.calculateFeeByDistance(distance, deliveryType);
        } catch (Exception e) {
            throw new CrmebException("计算配送费用失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用配送员列表
     */
    @Override
    public List<Integer> getAvailableDrivers(String pickupAddress, BigDecimal serviceRadius) {
        try {
            // 获取地址的经纬度
            Map<String, BigDecimal> location = tencentMapService.geocoding(pickupAddress);
            if (ObjectUtil.isNull(location)) {
                return new ArrayList<>();
            }

            BigDecimal longitude = location.get("longitude");
            BigDecimal latitude = location.get("latitude");

            // 获取附近可用的配送员
            return cityDeliveryDriverService.getAvailableDriversNearby(longitude, latitude, serviceRadius);
        } catch (Exception e) {
            System.err.println("获取可用配送员失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }
    /**
     * 根据配送单号获取配送订单
     */
    private CityDeliveryOrder getByDeliveryOrderNo(String deliveryOrderNo) {
        QueryWrapper<CityDeliveryOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delivery_order_no", deliveryOrderNo).eq("is_del", false);
        return getOne(queryWrapper);
    }

    /**
     * 转换为响应对象
     */
    private CityDeliveryOrderResponse convertToResponse(CityDeliveryOrder deliveryOrder) {
        CityDeliveryOrderResponse response = new CityDeliveryOrderResponse();
        BeanUtils.copyProperties(deliveryOrder, response);
        
        // 设置状态描述
        response.setDeliveryStatusDesc(getStatusDescription(deliveryOrder.getDeliveryStatus()));
        
        // 暂时注释掉轨迹信息，等CityDeliveryTrackService实现后再启用
        // response.setDeliveryTrackList(getDeliveryTrackList(deliveryOrder.getDeliveryOrderNo()));
        
        return response;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(Integer status) {
        switch (status) {
            case 1: return "待分配";
            case 2: return "已分配";
            case 3: return "取件中";
            case 4: return "配送中";
            case 5: return "已送达";
            case 6: return "已取消";
            case 7: return "异常";
            default: return "未知状态";
        }
    }

    /**
     * 获取配送轨迹列表
     */
    private List<CityDeliveryOrderResponse.DeliveryTrackData> getDeliveryTrackList(String deliveryOrderNo) {
        // 这里应该从轨迹表中查询，暂时返回空列表
        return new ArrayList<>();
    }

    /**
     * 自动分配配送员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoAssignDriver(String deliveryOrderNo) {
        try {
            return cityDeliveryDispatchService.autoAssignDriver(deliveryOrderNo);
        } catch (Exception e) {
            throw new CrmebException("自动分配配送员失败：" + e.getMessage());
        }
    }

    /**
     * 获取商户配送订单列表
     */
    @Override
    public List<CityDeliveryOrderResponse> getMerchantDeliveryList(Integer merId, Integer status) {
        try {
            QueryWrapper<CityDeliveryOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mer_id", merId).eq("is_del", false);
            
            if (ObjectUtil.isNotNull(status)) {
                queryWrapper.eq("delivery_status", status);
            }
            
            queryWrapper.orderByDesc("create_time");
            
            List<CityDeliveryOrder> deliveryOrders = list(queryWrapper);
            
            return deliveryOrders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取商户配送订单列表失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取配送订单轨迹
     */
    @Override
    public List<Map<String, Object>> getDeliveryTrack(String deliveryOrderNo) {
        try {
            // 这里应该从轨迹表中查询，暂时返回模拟数据
            List<Map<String, Object>> trackList = new ArrayList<>();
            
            CityDeliveryOrder order = getByDeliveryOrderNo(deliveryOrderNo);
            if (order != null) {
                // 添加基础轨迹节点
                if (order.getCreateTime() != null) {
                    Map<String, Object> track1 = new HashMap<>();
                    track1.put("time", order.getCreateTime());
                    track1.put("status", "订单已创建");
                    track1.put("description", "配送订单已创建，等待分配配送员");
                    trackList.add(track1);
                }
                
                if (order.getDriverId() != null) {
                    Map<String, Object> track2 = new HashMap<>();
                    track2.put("time", order.getUpdateTime());
                    track2.put("status", "已分配配送员");
                    track2.put("description", "配送员已接单，正在前往取件");
                    trackList.add(track2);
                }
            }
            
            return trackList;
        } catch (Exception e) {
            System.err.println("获取配送轨迹失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 配送员接单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean driverAcceptOrder(String deliveryOrderNo, Integer driverId) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            if (!deliveryOrder.getDeliveryStatus().equals(1)) {
                throw new CrmebException("订单状态不允许接单");
            }

            // 检查配送员是否可以接单
            if (!cityDeliveryDriverService.checkDriverCanTakeOrder(driverId)) {
                throw new CrmebException("配送员当前无法接单");
            }

            CityDeliveryDriver driver = cityDeliveryDriverService.getById(driverId);
            
            // 更新订单状态
            deliveryOrder.setDriverId(driverId);
            deliveryOrder.setDriverName(driver.getName());
            deliveryOrder.setDriverPhone(driver.getPhone());
            deliveryOrder.setDeliveryStatus(2); // 已接单
            deliveryOrder.setUpdateTime(new Date());
            updateById(deliveryOrder);

            // 增加配送员当前订单数
            cityDeliveryDriverService.incrementCurrentOrders(driverId);

            // 添加轨迹记录
            addDeliveryTrack(deliveryOrderNo, null, null, 
                "配送员已接单", "配送员 " + driver.getName() + " 已接单，正在前往取件");

            return true;
        } catch (Exception e) {
            throw new CrmebException("配送员接单失败：" + e.getMessage());
        }
    }

    /**
     * 配送员开始取件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean driverStartPickup(String deliveryOrderNo, Integer driverId) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            if (!deliveryOrder.getDriverId().equals(driverId)) {
                throw new CrmebException("无权限操作此订单");
            }

            if (!deliveryOrder.getDeliveryStatus().equals(2)) {
                throw new CrmebException("订单状态不允许开始取件");
            }

            // 更新订单状态
            deliveryOrder.setDeliveryStatus(3); // 取件中
            deliveryOrder.setPickupTime(new Date());
            deliveryOrder.setUpdateTime(new Date());
            updateById(deliveryOrder);

            // 添加轨迹记录
            addDeliveryTrack(deliveryOrderNo, null, null, 
                "开始取件", "配送员已到达取件地点，开始取件");

            return true;
        } catch (Exception e) {
            throw new CrmebException("开始取件失败：" + e.getMessage());
        }
    }

    /**
     * 配送员确认取件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean driverConfirmPickup(String deliveryOrderNo, Integer driverId, String pickupCode) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            if (!deliveryOrder.getDriverId().equals(driverId)) {
                throw new CrmebException("无权限操作此订单");
            }

            if (!deliveryOrder.getDeliveryStatus().equals(3)) {
                throw new CrmebException("订单状态不允许确认取件");
            }

            // 验证取件码（如果设置了的话）
            if (StrUtil.isNotBlank(deliveryOrder.getPickupCode()) && 
                !deliveryOrder.getPickupCode().equals(pickupCode)) {
                throw new CrmebException("取件码不正确");
            }

            // 更新订单状态
            deliveryOrder.setDeliveryStatus(4); // 配送中
            deliveryOrder.setActualPickupTime(new Date());
            deliveryOrder.setStartDeliveryTime(new Date());
            deliveryOrder.setUpdateTime(new Date());
            updateById(deliveryOrder);

            // 添加轨迹记录
            addDeliveryTrack(deliveryOrderNo, null, null, 
                "取件完成", "配送员已取件完成，正在前往收货地点");

            return true;
        } catch (Exception e) {
            throw new CrmebException("确认取件失败：" + e.getMessage());
        }
    }

    /**
     * 配送员开始配送
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean driverStartDelivery(String deliveryOrderNo, Integer driverId) {
        try {
            // 这个方法和确认取件合并了，直接调用确认取件
            return driverConfirmPickup(deliveryOrderNo, driverId, null);
        } catch (Exception e) {
            throw new CrmebException("开始配送失败：" + e.getMessage());
        }
    }

    /**
     * 配送员完成配送
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean driverCompleteDelivery(String deliveryOrderNo, Integer driverId, String deliveryCode) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            if (!deliveryOrder.getDriverId().equals(driverId)) {
                throw new CrmebException("无权限操作此订单");
            }

            if (!deliveryOrder.getDeliveryStatus().equals(4)) {
                throw new CrmebException("订单状态不允许完成配送");
            }

            // 验证收货码（如果设置了的话）
            if (StrUtil.isNotBlank(deliveryOrder.getDeliveryCode()) && 
                !deliveryOrder.getDeliveryCode().equals(deliveryCode)) {
                throw new CrmebException("收货码不正确");
            }

            // 更新订单状态
            deliveryOrder.setDeliveryStatus(5); // 已送达
            deliveryOrder.setActualDeliveryTime(new Date());
            deliveryOrder.setFinishTime(new Date());
            deliveryOrder.setUpdateTime(new Date());
            updateById(deliveryOrder);

            // 减少配送员当前订单数
            cityDeliveryDriverService.decrementCurrentOrders(driverId);

            // 更新配送员统计信息
            CityDeliveryDriver driver = cityDeliveryDriverService.getById(driverId);
            if (driver != null) {
                cityDeliveryDriverService.updateDriverOrderCount(driverId, 0); // 增加完成订单数
                if (deliveryOrder.getDriverCommission() != null) {
                    cityDeliveryDriverService.updateDriverIncome(driverId, deliveryOrder.getDriverCommission());
                }
            }

            // 添加轨迹记录
            addDeliveryTrack(deliveryOrderNo, null, null, 
                "配送完成", "商品已成功送达客户手中");

            return true;
        } catch (Exception e) {
            throw new CrmebException("完成配送失败：" + e.getMessage());
        }
    }

    /**
     * 配送异常上报
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reportDeliveryException(String deliveryOrderNo, Integer driverId, Integer exceptionType, String description) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            if (!deliveryOrder.getDriverId().equals(driverId)) {
                throw new CrmebException("无权限操作此订单");
            }

            // 更新订单异常信息
            deliveryOrder.setDeliveryStatus(7); // 异常状态
            deliveryOrder.setExceptionTime(new Date());
            deliveryOrder.setExceptionReason(description);
            deliveryOrder.setUpdateTime(new Date());
            updateById(deliveryOrder);

            // 添加轨迹记录
            addDeliveryTrack(deliveryOrderNo, null, null, 
                "配送异常", "配送出现异常：" + description);

            // TODO: 这里应该创建异常记录到异常表中

            return true;
        } catch (Exception e) {
            throw new CrmebException("上报配送异常失败：" + e.getMessage());
        }
    }

    /**
     * 获取配送统计
     */
    @Override
    public Map<String, Object> getDeliveryStats(Integer merId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            QueryWrapper<CityDeliveryOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false);
            
            if (merId != null) {
                queryWrapper.eq("mer_id", merId);
            }
            
            // 总订单数
            stats.put("totalOrders", count(queryWrapper));
            
            // 各状态订单数
            for (int status = 1; status <= 7; status++) {
                QueryWrapper<CityDeliveryOrder> statusQuery = new QueryWrapper<>();
                statusQuery.eq("is_del", false).eq("delivery_status", status);
                if (merId != null) {
                    statusQuery.eq("mer_id", merId);
                }
                stats.put("status" + status, count(statusQuery));
            }
            
            // 今日订单数
            QueryWrapper<CityDeliveryOrder> todayQuery = new QueryWrapper<>();
            todayQuery.eq("is_del", false)
                     .ge("create_time", DateUtil.beginOfDay(new Date()));
            if (merId != null) {
                todayQuery.eq("mer_id", merId);
            }
            stats.put("todayOrders", count(todayQuery));
            
            return stats;
        } catch (Exception e) {
            System.err.println("获取配送统计失败：" + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 生成配送订单号
     */
    @Override
    public String generateDeliveryOrderNo() {
        return "CD" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + 
               String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 检查地址是否在配送范围内
     */
    @Override
    public Boolean checkAddressInRange(String address) {
        try {
            return cityDeliveryAreaService.isAddressInServiceArea(address);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取配送时间段
     */
    @Override
    public List<Map<String, String>> getDeliveryTimeSlots(String address) {
        try {
            CityDeliveryArea area = cityDeliveryAreaService.getAreaByAddress(address);
            if (area != null) {
                return cityDeliveryAreaService.getDeliveryTimeSlots(area.getId());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 预估配送时间
     */
    @Override
    public String estimateDeliveryTime(String pickupAddress, String deliveryAddress, Integer deliveryType) {
        try {
            Integer estimateMinutes = tencentMapService.estimateDeliveryTime(pickupAddress, deliveryAddress);
            
            // 根据配送类型调整时间
            if (deliveryType != null && deliveryType == 1) {
                // 即时配送，加上准备时间
                estimateMinutes += 15;
            } else {
                // 预约配送，加上更多准备时间
                estimateMinutes += 30;
            }
            
            return estimateMinutes + "分钟";
        } catch (Exception e) {
            return "60分钟"; // 默认预估时间
        }
    }

    /**
     * 获取配送进度
     */
    @Override
    public Map<String, Object> getDeliveryProgress(String deliveryOrderNo) {
        try {
            CityDeliveryOrder order = getByDeliveryOrderNo(deliveryOrderNo);
            if (order == null) {
                return new HashMap<>();
            }
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("deliveryOrderNo", deliveryOrderNo);
            progress.put("status", order.getDeliveryStatus());
            progress.put("statusDesc", getStatusDescription(order.getDeliveryStatus()));
            progress.put("driverName", order.getDriverName());
            progress.put("driverPhone", order.getDriverPhone());
            progress.put("createTime", order.getCreateTime());
            progress.put("estimatedDeliveryTime", order.getEstimatedDeliveryTime());
            progress.put("actualDeliveryTime", order.getActualDeliveryTime());
            progress.put("trackList", getDeliveryTrack(deliveryOrderNo));
            
            return progress;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 重新分配配送员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reassignDriver(String deliveryOrderNo, Integer newDriverId) {
        try {
            CityDeliveryOrder deliveryOrder = getByDeliveryOrderNo(deliveryOrderNo);
            if (ObjectUtil.isNull(deliveryOrder)) {
                throw new CrmebException("配送订单不存在");
            }

            // 减少原配送员订单数
            if (deliveryOrder.getDriverId() != null) {
                cityDeliveryDriverService.decrementCurrentOrders(deliveryOrder.getDriverId());
            }

            // 分配新配送员
            CityDeliveryDriver newDriver = cityDeliveryDriverService.getById(newDriverId);
            if (ObjectUtil.isNull(newDriver)) {
                throw new CrmebException("新配送员不存在");
            }

            return assignDriver(deliveryOrderNo, newDriverId, newDriver.getName(), newDriver.getPhone());
        } catch (Exception e) {
            throw new CrmebException("重新分配配送员失败：" + e.getMessage());
        }
    }

    /**
     * 获取超时订单
     */
    @Override
    public List<CityDeliveryOrderResponse> getTimeoutOrders() {
        try {
            // 查询超过2小时未完成的订单
            Date timeoutThreshold = new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000);
            
            QueryWrapper<CityDeliveryOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .in("delivery_status", Arrays.asList(1, 2, 3, 4))
                       .lt("create_time", timeoutThreshold);
            
            List<CityDeliveryOrder> timeoutOrders = list(queryWrapper);
            
            return timeoutOrders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取超时订单失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 处理超时订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleTimeoutOrder(String deliveryOrderNo) {
        try {
            // 自动重新分配配送员
            return autoAssignDriver(deliveryOrderNo);
        } catch (Exception e) {
            // 如果重新分配失败，标记为异常
            reportDeliveryException(deliveryOrderNo, null, 1, "订单超时，自动处理");
            return false;
        }
    }

    /**
     * 配送员评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rateDriver(String deliveryOrderNo, Integer userId, Integer rating, String comment) {
        try {
            CityDeliveryOrder order = getByDeliveryOrderNo(deliveryOrderNo);
            if (order == null || !order.getUserId().equals(userId)) {
                throw new CrmebException("无权限评价");
            }

            if (!order.getDeliveryStatus().equals(5)) {
                throw new CrmebException("订单未完成，无法评价");
            }

            // 更新订单评价信息
            order.setCustomerRating(rating);
            order.setCustomerFeedback(comment);
            order.setUpdateTime(new Date());
            updateById(order);

            // TODO: 这里应该创建评价记录到评价表中
            // TODO: 更新配送员评分

            return true;
        } catch (Exception e) {
            throw new CrmebException("评价失败：" + e.getMessage());
        }
    }

    /**
     * 获取配送费用明细
     */
    @Override
    public Map<String, Object> getDeliveryFeeDetail(String pickupAddress, String deliveryAddress, Integer deliveryType) {
        try {
            return cityDeliveryFeeRuleService.calculateDetailedFee(pickupAddress, deliveryAddress, deliveryType, null, null);
        } catch (Exception e) {
            throw new CrmebException("获取配送费用明细失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新配送状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateDeliveryStatus(List<String> deliveryOrderNos, Integer status) {
        try {
            for (String deliveryOrderNo : deliveryOrderNos) {
                updateDeliveryStatus(deliveryOrderNo, status, "批量操作");
            }
            return true;
        } catch (Exception e) {
            throw new CrmebException("批量更新配送状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取配送区域信息
     */
    @Override
    public Map<String, Object> getDeliveryAreaInfo(String address) {
        try {
            CityDeliveryArea area = cityDeliveryAreaService.getAreaByAddress(address);
            if (area == null) {
                return new HashMap<>();
            }
            
            Map<String, Object> areaInfo = new HashMap<>();
            areaInfo.put("areaId", area.getId());
            areaInfo.put("areaName", area.getAreaName());
            areaInfo.put("serviceRadius", area.getServiceRadius());
            areaInfo.put("serviceStartTime", area.getServiceStartTime());
            areaInfo.put("serviceEndTime", area.getServiceEndTime());
            areaInfo.put("supportInstantDelivery", area.getSupportInstantDelivery());
            areaInfo.put("supportScheduledDelivery", area.getSupportScheduledDelivery());
            
            return areaInfo;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 配送员位置更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDriverLocation(Integer driverId, BigDecimal longitude, BigDecimal latitude, String address) {
        try {
            return cityDeliveryDriverService.updateDriverLocation(driverId, longitude, latitude, address);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取配送员实时位置
     */
    @Override
    public Map<String, Object> getDriverLocation(Integer driverId) {
        try {
            return cityDeliveryDriverService.getDriverRealtimeStatus(driverId);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 获取配送热力图数据
     */
    @Override
    public List<Map<String, Object>> getDeliveryHeatmapData(String startDate, String endDate) {
        try {
            // TODO: 实现热力图数据查询
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取配送效率统计
     */
    @Override
    public Map<String, Object> getDeliveryEfficiencyStats(String startDate, String endDate) {
        try {
            // TODO: 实现配送效率统计
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 配送路径优化
     */
    @Override
    public List<Map<String, Object>> optimizeDeliveryRoute(Integer driverId, List<String> deliveryOrderNos) {
        try {
            // TODO: 实现路径优化算法
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public PageInfo<CityDeliveryOrderResponse> getDeliveryOrderPage(CityDeliveryOrderListRequest request) {
        try {
            // 使用标准的分页方式
            Page<CityDeliveryOrder> page = PageHelper.startPage(request.getPage(), request.getLimit());
            
            // 构建查询条件
            LambdaQueryWrapper<CityDeliveryOrder> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(CityDeliveryOrder::getIsDel, 0);
            
            if (StringUtils.isNotBlank(request.getDeliveryOrderNo())) {
                wrapper.like(CityDeliveryOrder::getDeliveryOrderNo, request.getDeliveryOrderNo());
            }
            if (StringUtils.isNotBlank(request.getOrderNo())) {
                wrapper.like(CityDeliveryOrder::getOrderNo, request.getOrderNo());
            }
            if (request.getDeliveryStatus() != null) {
                wrapper.eq(CityDeliveryOrder::getDeliveryStatus, request.getDeliveryStatus());
            }
            if (request.getDeliveryType() != null) {
                wrapper.eq(CityDeliveryOrder::getDeliveryType, request.getDeliveryType());
            }
            if (request.getDriverId() != null) {
                wrapper.eq(CityDeliveryOrder::getDriverId, request.getDriverId());
            }
            if (request.getMerId() != null) {
                wrapper.eq(CityDeliveryOrder::getMerId, request.getMerId());
            }
            if (request.getUserId() != null) {
                wrapper.eq(CityDeliveryOrder::getUserId, request.getUserId());
            }
            if (StringUtils.isNotBlank(request.getStartDate())) {
                wrapper.ge(CityDeliveryOrder::getCreateTime, request.getStartDate() + " 00:00:00");
            }
            if (StringUtils.isNotBlank(request.getEndDate())) {
                wrapper.le(CityDeliveryOrder::getCreateTime, request.getEndDate() + " 23:59:59");
            }
            if (StringUtils.isNotBlank(request.getPickupAddress())) {
                wrapper.like(CityDeliveryOrder::getPickupAddress, request.getPickupAddress());
            }
            if (StringUtils.isNotBlank(request.getDeliveryAddress())) {
                wrapper.like(CityDeliveryOrder::getDeliveryAddress, request.getDeliveryAddress());
            }

            // 排序
            wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);

            // 执行查询
            List<CityDeliveryOrder> list = cityDeliveryOrderDao.selectList(wrapper);
            
            // 处理空结果
            if (CollUtil.isEmpty(list)) {
                return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
            }
            
            // 转换为响应对象
            List<CityDeliveryOrderResponse> responseList = list.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            // 使用标准方法创建PageInfo
            return CommonPage.copyPageInfo(page, responseList);
        } catch (Exception e) {
            logger.error("获取配送订单分页列表失败: {}", e.getMessage(), e);
            throw new CrmebException("获取配送订单列表失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOrderStatistics(Integer merId, String startDate, String endDate) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 构建查询条件
            LambdaQueryWrapper<CityDeliveryOrder> wrapper = Wrappers.lambdaQuery();
            if (merId != null) {
                wrapper.eq(CityDeliveryOrder::getMerId, merId);
            }
            if (StringUtils.isNotBlank(startDate)) {
                wrapper.ge(CityDeliveryOrder::getCreateTime, startDate + " 00:00:00");
            }
            if (StringUtils.isNotBlank(endDate)) {
                wrapper.le(CityDeliveryOrder::getCreateTime, endDate + " 23:59:59");
            }

            // 总订单数
            Integer totalOrdersInt = cityDeliveryOrderDao.selectCount(wrapper);
            Long totalOrders = totalOrdersInt.longValue();
            statistics.put("totalOrders", totalOrders);

            // 各状态订单数
            for (int status = 0; status <= 9; status++) {
                LambdaQueryWrapper<CityDeliveryOrder> statusWrapper = wrapper.clone();
                statusWrapper.eq(CityDeliveryOrder::getDeliveryStatus, status);
                Integer countInt = cityDeliveryOrderDao.selectCount(statusWrapper);
                Long count = countInt.longValue();
                statistics.put("status" + status + "Count", count);
            }

            // 完成率
            LambdaQueryWrapper<CityDeliveryOrder> completedWrapper = wrapper.clone();
            completedWrapper.eq(CityDeliveryOrder::getDeliveryStatus, 4);
            Integer completedOrdersInt = cityDeliveryOrderDao.selectCount(completedWrapper);
            Long completedOrders = completedOrdersInt.longValue();
            
            BigDecimal completionRate = totalOrders > 0 ? 
                new BigDecimal(completedOrders).divide(new BigDecimal(totalOrders), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)) :
                BigDecimal.ZERO;
            statistics.put("completionRate", completionRate);

            // 总配送费用
            wrapper.select(CityDeliveryOrder::getDeliveryFee);
            List<CityDeliveryOrder> feeList = cityDeliveryOrderDao.selectList(wrapper);
            BigDecimal totalFee = feeList.stream()
                .filter(order -> order.getDeliveryFee() != null)
                .map(CityDeliveryOrder::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("totalDeliveryFee", totalFee);

            // 平均配送费用
            BigDecimal avgFee = totalOrders > 0 ? 
                totalFee.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            statistics.put("avgDeliveryFee", avgFee);

            return statistics;
        } catch (Exception e) {
            logger.error("获取订单统计信息失败: {}", e.getMessage(), e);
            throw new CrmebException("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据字段名获取对应的数据库列
     */
    private SFunction<CityDeliveryOrder, ?> getColumnByField(String field) {
        switch (field) {
            case "createTime":
                return CityDeliveryOrder::getCreateTime;
            case "deliveryFee":
                return CityDeliveryOrder::getDeliveryFee;
            case "deliveryStatus":
                return CityDeliveryOrder::getDeliveryStatus;
            default:
                return CityDeliveryOrder::getCreateTime;
        }
    }

    // 删除重复的convertToResponse方法，保留原有的方法实现
} 