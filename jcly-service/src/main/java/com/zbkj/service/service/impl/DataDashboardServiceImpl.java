package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductBrand;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.merchant.MerchantAddress;
import com.zbkj.common.model.user.UserAddress;
import com.zbkj.common.model.city.CityRegion;
import com.zbkj.service.service.*;
import com.zbkj.common.model.product.ProductCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据看板服务实现类
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
@Slf4j
@Service
public class DataDashboardServiceImpl implements DataDashboardService {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CityRegionService cityRegionService;
    
    @Autowired
    private UserAddressService userAddressService;
    
    @Autowired
    private OrderDetailService orderDetailService;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private ProductAttrValueService productAttrValueService;
    
    @Autowired
    private ProductBrandService productBrandService;
    
    @Autowired
    private MerchantOrderService merchantOrderService;
    
    @Autowired
    private MerchantAddressService merchantAddressService;

    @Override
    public JSONObject getOverviewData(Integer year, Integer month) {
        JSONObject result = new JSONObject();
        
        try {
            // 使用当前年份，忽略传入的year参数，因为大屏显示的是年度总计
            int currentYear = year;
            String yearStartDate = currentYear + "-01-01";
            String yearEndDate = currentYear + "-12-31";
            
            // 获取年度所有已支付订单
            List<Order> yearOrders = getOrdersByCondition(null, yearStartDate, yearEndDate);
            
            // 年度总销售额（转换为万元）
            BigDecimal yearTotalSales = yearOrders.stream()
                .map(Order::getPayPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal yearSalesInWan = yearTotalSales.divide(BigDecimal.valueOf(10000), 1, RoundingMode.HALF_UP);
            result.put("totalSales", yearSalesInWan); // 549.3万元
            
            // 年度总订单量
            result.put("totalOrders", yearOrders.size());
            
            // 店铺总数量
            Integer totalMerchants = getMerchantCount(currentYear);
            result.put("totalMerchants", totalMerchants); // 2479个
            
            // 当月数据
            int currentMonth = LocalDate.now().getMonthValue();
            String monthStartDate = String.format("%d-%02d-01", currentYear, currentMonth);
            String monthEndDate;
            if (currentMonth == 12) {
                monthEndDate = (currentYear + 1) + "-01-01";
            } else {
                monthEndDate = String.format("%d-%02d-01", currentYear, currentMonth + 1);
            }
            
            // 年度商品品牌总数
            Integer yearBrandCount = getYearBrandCount(currentYear);
            result.put("yearBrandCount", yearBrandCount);
            
            // 年度商品SKU总数
            Integer yearSkuCount = getYearSkuCount(currentYear);
            result.put("yearSkuCount", yearSkuCount);
            
            // 总用户量
            Integer totalUsers = getTotalUserCount(currentYear);
            result.put("totalUsers", totalUsers); // 42831人
            
        } catch (Exception e) {
            log.error("获取概览数据失败", e);
            result.put("totalSales", BigDecimal.ZERO);
            result.put("totalOrders", BigDecimal.ZERO);
            result.put("totalMerchants", 0);
            result.put("yearBrandCount", BigDecimal.ZERO);
            result.put("yearSkuCount", 0);
            result.put("totalUsers", 0);
        }
        
        return result;
    }

    @Override
    public JSONObject getYearlySalesData(Integer year, String dataType) {
        JSONObject result = new JSONObject();
        
        try {
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            
            // 确保所有12个月都有数据，即使为0
            for (int month = 1; month <= 12; month++) {
                String[] dateRange = buildDateRange(year, month);
                String startDate = dateRange[0];
                String endDate = dateRange[1];
                
                List<Order> monthOrders = getOrdersByCondition(null, startDate, endDate);
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", month + "月");
                
                if ("sales".equals(dataType)) {
                    // 销售额统计
                    BigDecimal monthSales = monthOrders.stream()
                        .map(Order::getPayPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // 转换为万元
                    BigDecimal salesInWan = monthSales.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
                    monthData.put("value", salesInWan);
                } else if ("orders".equals(dataType)) {
                    // 订单量统计
                    monthData.put("value", monthOrders.size());
                } else {
                    // 默认返回销售额
                    BigDecimal monthSales = monthOrders.stream()
                        .map(Order::getPayPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal salesInWan = monthSales.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
                    monthData.put("value", salesInWan);
                }
                
                monthlyData.add(monthData);
            }
            
            result.put("data", monthlyData);
            result.put("year", year);
            result.put("dataType", dataType);
            
            log.info("获取年度销售数据成功，年份：{}，数据类型：{}", year, dataType);
            
        } catch (Exception e) {
            log.error("获取年度销售数据失败，年份：{}，数据类型：{}", year, dataType, e);
            
            // 即使出错也返回12个月的空数据
            List<Map<String, Object>> emptyMonthlyData = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", month + "月");
                monthData.put("value", "sales".equals(dataType) ? BigDecimal.ZERO : 0);
                emptyMonthlyData.add(monthData);
            }
            
            result.put("data", emptyMonthlyData);
            result.put("year", year);
            result.put("dataType", dataType);
        }
        
        return result;
    }

    @Override
    public JSONObject getYearlyUserData(Integer year) {
        JSONObject result = new JSONObject();
        
        try {
            List<Map<String, Object>> monthlyUsers = new ArrayList<>();
            Integer totalYearUsers = 0;
            
            for (int month = 1; month <= 12; month++) {
                String[] dateRange = buildDateRange(year, month);
                String startDate = dateRange[0];
                String endDate = dateRange[1];
                
                Integer monthlyUserCount = getUserCount(startDate, endDate);
                Map<String, Object> userData = new HashMap<>();
                userData.put("month", month + "月");
                userData.put("value", monthlyUserCount != null ? monthlyUserCount : 0);
                monthlyUsers.add(userData);
                
                totalYearUsers += (monthlyUserCount != null ? monthlyUserCount : 0);
            }
            
            result.put("users", monthlyUsers);
            result.put("totalUsers", totalYearUsers);
            result.put("year", year);
            
            log.info("获取年度用户数据成功，年份：{}，总用户数：{}", year, totalYearUsers);
            
        } catch (Exception e) {
            log.error("获取年度用户数据失败，年份：{}", year, e);
            
            // 即使出错也返回12个月的空数据
            List<Map<String, Object>> emptyMonthlyUsers = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("month", month + "月");
                userData.put("value", 0);
                emptyMonthlyUsers.add(userData);
            }
            
            result.put("users", emptyMonthlyUsers);
            result.put("totalUsers", 0);
            result.put("year", year);
        }
        
        return result;
    }

    /**
     * 判断是否为直辖市
     */
    private boolean isDirectControlledMunicipality(String provinceName) {
        return "北京市".equals(provinceName) || "天津市".equals(provinceName) || 
               "上海市".equals(provinceName) || "重庆市".equals(provinceName);
    }

    @Override
    public JSONObject getRegionSalesData(Integer year, Integer month, String regionCode, String statisticsType) {
        JSONObject result = new JSONObject();
        
        try {
            String[] dateRange = buildDateRange(year, month);
            String startDate = dateRange[0];
            String endDate = dateRange[1];
            
            // 确定区域层级和查询条件
            Integer regionType;
            Integer parentId;
            
            if (StrUtil.isBlank(regionCode)) {
                // 查询所有省份
                regionType = 1; // 省级
                parentId = null;
            } else {
                // 根据regionCode判断是省还是市，然后查询下一级
                LambdaQueryWrapper<CityRegion> checkWrapper = Wrappers.lambdaQuery();
                checkWrapper.eq(CityRegion::getRegionId, regionCode);
                CityRegion currentRegion = cityRegionService.getOne(checkWrapper);
                
                if (currentRegion == null) {
                    result.put("regions", new ArrayList<>());
                    result.put("currentRegion", null);
                    result.put("statisticsType", statisticsType);
                    return result;
                }
                
                if (currentRegion.getRegionType() == 1) {
                    // 当前是省，检查是否为直辖市
                    String provinceName = currentRegion.getRegionName();
                    if (isDirectControlledMunicipality(provinceName)) {
                        // 直辖市：跳过市级，直接查询区级
                        regionType = 3; // 区级
                        // 查找直辖市下的市辖区
                        LambdaQueryWrapper<CityRegion> cityWrapper = Wrappers.lambdaQuery();
                        cityWrapper.eq(CityRegion::getParentId, currentRegion.getRegionId())
                                  .eq(CityRegion::getRegionType, 2);
                        List<CityRegion> cities = cityRegionService.list(cityWrapper);
                        
                        // 收集所有市级区域的ID作为父级ID列表
                        if (!cities.isEmpty()) {
                            // 对于直辖市，我们需要查询所有市级区域下的区级数据
                            List<Integer> cityIds = cities.stream()
                                .map(CityRegion::getRegionId)
                                .collect(Collectors.toList());
                            parentId = currentRegion.getRegionId(); // 用省级ID，后面特殊处理
                        } else {
                            parentId = currentRegion.getRegionId();
                        }
                    } else {
                        // 普通省份：查询下级市
                        regionType = 2; // 市级
                        parentId = currentRegion.getRegionId();
                    }
                } else if (currentRegion.getRegionType() == 2) {
                    // 当前是市，查询下级区
                    regionType = 3; // 区级
                    parentId = currentRegion.getRegionId();
                } else {
                    // 已经是最低级别
                    result.put("regions", new ArrayList<>());
                    result.put("currentRegion", currentRegion.getRegionName());
                    result.put("statisticsType", statisticsType);
                    return result;
                }
            }
            
            // 获取区域列表
            LambdaQueryWrapper<CityRegion> regionWrapper = Wrappers.lambdaQuery();
            regionWrapper.eq(CityRegion::getRegionType, regionType);
            if (parentId != null) {
                if (regionType == 3) {
                    // 如果是查询区级数据，需要特殊处理直辖市
                    LambdaQueryWrapper<CityRegion> checkParentWrapper = Wrappers.lambdaQuery();
                    checkParentWrapper.eq(CityRegion::getRegionId, parentId);
                    CityRegion parentRegion = cityRegionService.getOne(checkParentWrapper);
                    
                    if (parentRegion != null && parentRegion.getRegionType() == 1 && 
                        isDirectControlledMunicipality(parentRegion.getRegionName())) {
                        // 对于直辖市，需要查询所有市级区域下的区级数据
                        LambdaQueryWrapper<CityRegion> cityWrapper = Wrappers.lambdaQuery();
                        cityWrapper.eq(CityRegion::getParentId, parentId)
                                  .eq(CityRegion::getRegionType, 2);
                        List<CityRegion> cities = cityRegionService.list(cityWrapper);
                        
                        if (!cities.isEmpty()) {
                            List<Integer> cityIds = cities.stream()
                                .map(CityRegion::getRegionId)
                                .collect(Collectors.toList());
                            regionWrapper.in(CityRegion::getParentId, cityIds);
                        } else {
                            regionWrapper.eq(CityRegion::getParentId, parentId);
                        }
                    } else {
                        regionWrapper.eq(CityRegion::getParentId, parentId);
                    }
                } else {
                    regionWrapper.eq(CityRegion::getParentId, parentId);
                }
            }
            regionWrapper.orderByAsc(CityRegion::getRegionName);
            List<CityRegion> regions = cityRegionService.list(regionWrapper);
            
            List<Map<String, Object>> regionDataList = new ArrayList<>();
            
            for (CityRegion region : regions) {
                Map<String, Object> regionData = new HashMap<>();
                regionData.put("regionCode", region.getRegionId().toString());
                regionData.put("regionName", region.getRegionName());
                regionData.put("regionType", region.getRegionType());
                
                // 根据统计类型计算数据
                Object statisticsValue = calculateRegionStatistics(region, startDate, endDate, statisticsType);
                regionData.put("value", statisticsValue);
                
                // 添加地图坐标（如果是省级）
                if (region.getRegionType() == 1) {
                    Map<String, double[]> provinceCoords = getProvinceCoordinates();
                    String provinceName = region.getRegionName().replace("省", "").replace("市", "").replace("自治区", "");
                    double[] coords = provinceCoords.get(provinceName);
                    if (coords != null) {
                        regionData.put("longitude", coords[0]);
                        regionData.put("latitude", coords[1]);
                    } else {
                        regionData.put("longitude", 114.2972);
                        regionData.put("latitude", 30.5928);
                    }
                }
                
                regionDataList.add(regionData);
            }
            
            result.put("regions", regionDataList);
            result.put("year", year);
            result.put("month", month);
            result.put("statisticsType", statisticsType);
            result.put("regionType", regionType);
            
            log.info("获取地区数据分布成功，年份：{}，月份：{}，统计类型：{}，数据量：{}", year, month, statisticsType, regionDataList.size());
            
        } catch (Exception e) {
            log.error("获取地区数据分布失败，年份：{}，月份：{}，统计类型：{}", year, month, statisticsType, e);
            
            // 即使出错也尝试返回完整的区域列表，统计数据为0
            try {
                // 确定区域层级
                Integer regionType = 1; // 默认省级
                Integer parentId = null;
                
                if (StrUtil.isNotBlank(regionCode)) {
                    LambdaQueryWrapper<CityRegion> checkWrapper = Wrappers.lambdaQuery();
                    checkWrapper.eq(CityRegion::getRegionId, regionCode);
                    CityRegion currentRegion = cityRegionService.getOne(checkWrapper);
                    
                    if (currentRegion != null) {
                        if (currentRegion.getRegionType() == 1) {
                            // 检查是否为直辖市
                            String provinceName = currentRegion.getRegionName();
                            if (isDirectControlledMunicipality(provinceName)) {
                                regionType = 3; // 直辖市直接显示区级
                                parentId = currentRegion.getRegionId();
                            } else {
                                regionType = 2; // 普通省份显示市级
                                parentId = currentRegion.getRegionId();
                            }
                        } else if (currentRegion.getRegionType() == 2) {
                            regionType = 3; // 区级
                            parentId = currentRegion.getRegionId();
                        }
                    }
                }
                
                // 获取区域列表
                LambdaQueryWrapper<CityRegion> regionWrapper = Wrappers.lambdaQuery();
                regionWrapper.eq(CityRegion::getRegionType, regionType);
                if (parentId != null) {
                    if (regionType == 3) {
                        // 如果是查询区级数据，需要特殊处理直辖市
                        LambdaQueryWrapper<CityRegion> checkParentWrapper = Wrappers.lambdaQuery();
                        checkParentWrapper.eq(CityRegion::getRegionId, parentId);
                        CityRegion parentRegion = cityRegionService.getOne(checkParentWrapper);
                        
                        if (parentRegion != null && parentRegion.getRegionType() == 1 && 
                            isDirectControlledMunicipality(parentRegion.getRegionName())) {
                            // 对于直辖市，需要查询所有市级区域下的区级数据
                            LambdaQueryWrapper<CityRegion> cityWrapper = Wrappers.lambdaQuery();
                            cityWrapper.eq(CityRegion::getParentId, parentId)
                                      .eq(CityRegion::getRegionType, 2);
                            List<CityRegion> cities = cityRegionService.list(cityWrapper);
                            
                            if (!cities.isEmpty()) {
                                List<Integer> cityIds = cities.stream()
                                    .map(CityRegion::getRegionId)
                                    .collect(Collectors.toList());
                                regionWrapper.in(CityRegion::getParentId, cityIds);
                            } else {
                                regionWrapper.eq(CityRegion::getParentId, parentId);
                            }
                        } else {
                            regionWrapper.eq(CityRegion::getParentId, parentId);
                        }
                    } else {
                        regionWrapper.eq(CityRegion::getParentId, parentId);
                    }
                }
                regionWrapper.orderByAsc(CityRegion::getRegionName);
                List<CityRegion> regions = cityRegionService.list(regionWrapper);
                
                List<Map<String, Object>> emptyRegionDataList = new ArrayList<>();
                for (CityRegion region : regions) {
                    Map<String, Object> regionData = new HashMap<>();
                    regionData.put("regionCode", region.getRegionId().toString());
                    regionData.put("regionName", region.getRegionName());
                    regionData.put("regionType", region.getRegionType());
                    
                    // 设置为默认的0值
                    switch (statisticsType) {
                        case "sales":
                            regionData.put("value", BigDecimal.ZERO);
                            break;
                        case "orders":
                        case "users":
                        case "newUsers":
                        case "merchants":
                            regionData.put("value", 0);
                            break;
                        default:
                            regionData.put("value", BigDecimal.ZERO);
                            break;
                    }
                    
                    // 添加地图坐标（如果是省级）
                    if (region.getRegionType() == 1) {
                        Map<String, double[]> provinceCoords = getProvinceCoordinates();
                        String provinceName = region.getRegionName().replace("省", "").replace("市", "").replace("自治区", "");
                        double[] coords = provinceCoords.get(provinceName);
                        if (coords != null) {
                            regionData.put("longitude", coords[0]);
                            regionData.put("latitude", coords[1]);
                        } else {
                            regionData.put("longitude", 114.2972);
                            regionData.put("latitude", 30.5928);
                        }
                    }
                    
                    emptyRegionDataList.add(regionData);
                }
                
                result.put("regions", emptyRegionDataList);
                result.put("regionType", regionType);
                
            } catch (Exception ex) {
                log.error("获取空区域列表也失败", ex);
                result.put("regions", new ArrayList<>());
            }
            
            result.put("year", year);
            result.put("month", month);
            result.put("statisticsType", statisticsType);
        }
        
        return result;
    }
    
    private Object calculateRegionStatistics(CityRegion region, String startDate, String endDate, String statisticsType) {
        try {
            switch (statisticsType) {
                case "sales":
                    return calculateRegionSales(region, startDate, endDate);
                case "orders":
                    return calculateRegionOrders(region, startDate, endDate);
                case "users":
                    return calculateRegionUsers(region, startDate, endDate);
                case "newUsers":
                    return calculateRegionNewUsers(region, startDate, endDate);
                case "merchants":
                    return calculateRegionMerchants(region);
                default:
                    return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.debug("计算区域统计数据失败，区域：{}，类型：{}，返回0值", region.getRegionName(), statisticsType, e);
            // 根据统计类型返回对应的0值
            switch (statisticsType) {
                case "sales":
                    return BigDecimal.ZERO;
                case "orders":
                case "users":
                case "newUsers":
                case "merchants":
                    return 0;
                default:
                    return BigDecimal.ZERO;
            }
        }
    }
    
    private BigDecimal calculateRegionSales(CityRegion region, String startDate, String endDate) {
        // 获取该区域的所有订单
        List<Order> orders = getOrdersByRegion(region, startDate, endDate);
        BigDecimal totalSales = orders.stream()
            .map(Order::getPayPrice)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 转换为万元
        BigDecimal salesInWan = totalSales.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);

        // 如果有销售额但转换后为0.00万元,则显示为0.01万元
        if (totalSales.compareTo(BigDecimal.ZERO) > 0 && salesInWan.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("0.01");
        }

        return salesInWan;
    }
    
    private Integer calculateRegionOrders(CityRegion region, String startDate, String endDate) {
        List<Order> orders = getOrdersByRegion(region, startDate, endDate);
        return orders.size();
    }
    
    private Integer calculateRegionUsers(CityRegion region, String startDate, String endDate) {
        // 获取该区域的用户数量（通过用户地址关联）
        return getUsersByRegion(region, null, null);
    }
    
    private Integer calculateRegionNewUsers(CityRegion region, String startDate, String endDate) {
        // 获取该区域指定时间内新增的用户数量
        return getUsersByRegion(region, startDate, endDate);
    }
    
    private Integer calculateRegionMerchants(CityRegion region) {
        // 获取该区域的商户数量（通过商户地址关联）
        return getMerchantsByRegion(region);
    }
    
    private List<Order> getOrdersByRegion(CityRegion region, String startDate, String endDate) {
        log.info("开始查询地区订单，目标地区：{}，地区类型：{}", region.getRegionName(), region.getRegionType());

        // 根据地区类型构建查询关键词
        String regionKeyword;

        if (region.getRegionType() == 1) {
            // 省级查询：使用完整省名，如"湖北省"
            regionKeyword = region.getRegionName();
            // 确保省级名称包含"省"或"市"或"自治区"后缀
            if (!regionKeyword.endsWith("省") && !regionKeyword.endsWith("市") &&
                !regionKeyword.endsWith("自治区") && !regionKeyword.endsWith("特别行政区")) {
                // 如果没有后缀，根据名称判断应该加什么后缀
                if (isDirectControlledMunicipality(regionKeyword)) {
                    regionKeyword += "市";
                } else if (regionKeyword.equals("内蒙古") || regionKeyword.equals("广西") ||
                          regionKeyword.equals("西藏") || regionKeyword.equals("宁夏") || regionKeyword.equals("新疆")) {
                    regionKeyword += "自治区";
                } else if (regionKeyword.equals("香港") || regionKeyword.equals("澳门")) {
                    regionKeyword += "特别行政区";
                } else {
                    regionKeyword += "省";
                }
            }
            log.info("省级查询，关键词: {}", regionKeyword);
        } else if (region.getRegionType() == 2) {
            // 市级查询：需要查询父级省份，拼接"省+市"，如"湖北省武汉市"或"湖北省黄石市"
            LambdaQueryWrapper<CityRegion> parentWrapper = Wrappers.lambdaQuery();
            parentWrapper.eq(CityRegion::getRegionId, region.getParentId());
            CityRegion parentProvince = cityRegionService.getOne(parentWrapper);

            if (parentProvince != null) {
                String provinceName = parentProvince.getRegionName();
                // 确保省名有后缀
                if (!provinceName.endsWith("省") && !provinceName.endsWith("市") &&
                    !provinceName.endsWith("自治区") && !provinceName.endsWith("特别行政区")) {
                    if (isDirectControlledMunicipality(provinceName)) {
                        provinceName += "市";
                    } else if (provinceName.equals("内蒙古") || provinceName.equals("广西") ||
                              provinceName.equals("西藏") || provinceName.equals("宁夏") || provinceName.equals("新疆")) {
                        provinceName += "自治区";
                    } else if (provinceName.equals("香港") || provinceName.equals("澳门")) {
                        provinceName += "特别行政区";
                    } else {
                        provinceName += "省";
                    }
                }

                String cityName = region.getRegionName();
                // 确保市名有"市"后缀
                if (!cityName.endsWith("市") && !cityName.endsWith("区") && !cityName.endsWith("州") && !cityName.endsWith("盟")) {
                    cityName += "市";
                }

                // 直辖市特殊处理：北京、上海、天津、重庆
                if (isDirectControlledMunicipality(parentProvince.getRegionName())) {
                    regionKeyword = provinceName; // 直辖市只用城市名即可，如"北京市"
                } else {
                    regionKeyword = provinceName + cityName; // 普通省份：省名+市名，如"湖北省武汉市"或"湖北省黄石市"
                }
            } else {
                regionKeyword = region.getRegionName();
                if (!regionKeyword.endsWith("市") && !regionKeyword.endsWith("区") &&
                    !regionKeyword.endsWith("州") && !regionKeyword.endsWith("盟")) {
                    regionKeyword += "市";
                }
            }
            log.info("市级查询，关键词: {}", regionKeyword);
        } else {
            // 区级查询：需要查询父级市和祖父级省，拼接"省+市+区"或"市+区"
            LambdaQueryWrapper<CityRegion> parentWrapper = Wrappers.lambdaQuery();
            parentWrapper.eq(CityRegion::getRegionId, region.getParentId());
            CityRegion parentCity = cityRegionService.getOne(parentWrapper);

            if (parentCity != null) {
                String cityName = parentCity.getRegionName();
                // 确保市名有后缀
                if (!cityName.endsWith("市") && !cityName.endsWith("区") && !cityName.endsWith("州") && !cityName.endsWith("盟")) {
                    cityName += "市";
                }

                String districtName = region.getRegionName();
                // 确保区名有后缀
                if (!districtName.endsWith("区") && !districtName.endsWith("县") &&
                    !districtName.endsWith("市") && !districtName.endsWith("旗")) {
                    districtName += "区";
                }

                // 查询祖父级省份
                LambdaQueryWrapper<CityRegion> grandParentWrapper = Wrappers.lambdaQuery();
                grandParentWrapper.eq(CityRegion::getRegionId, parentCity.getParentId());
                CityRegion grandParentProvince = cityRegionService.getOne(grandParentWrapper);

                if (grandParentProvince != null) {
                    String provinceName = grandParentProvince.getRegionName();
                    // 确保省名有后缀
                    if (!provinceName.endsWith("省") && !provinceName.endsWith("市") &&
                        !provinceName.endsWith("自治区") && !provinceName.endsWith("特别行政区")) {
                        if (isDirectControlledMunicipality(provinceName)) {
                            provinceName += "市";
                        } else if (provinceName.equals("内蒙古") || provinceName.equals("广西") ||
                                  provinceName.equals("西藏") || provinceName.equals("宁夏") || provinceName.equals("新疆")) {
                            provinceName += "自治区";
                        } else if (provinceName.equals("香港") || provinceName.equals("澳门")) {
                            provinceName += "特别行政区";
                        } else {
                            provinceName += "省";
                        }
                    }

                    // 直辖市特殊处理
                    if (isDirectControlledMunicipality(grandParentProvince.getRegionName())) {
                        regionKeyword = provinceName + districtName; // 如"天津市和平区"
                    } else {
                        regionKeyword = provinceName + cityName + districtName; // 普通城市：省名+市名+区名，如"湖北省武汉市洪山区"
                    }
                } else {
                    regionKeyword = cityName + districtName;
                }
            } else {
                regionKeyword = region.getRegionName();
                if (!regionKeyword.endsWith("区") && !regionKeyword.endsWith("县") &&
                    !regionKeyword.endsWith("市") && !regionKeyword.endsWith("旗")) {
                    regionKeyword += "区";
                }
            }
            log.info("区级查询，关键词: {}", regionKeyword);
        }

        // 直接在数据库层面通过JOIN和LIKE进行过滤，避免加载所有订单到内存
        LambdaQueryWrapper<Order> orderWrapper = Wrappers.lambdaQuery();
        orderWrapper.eq(Order::getPaid, 1);
        orderWrapper.eq(Order::getIsDel, false);
        orderWrapper.like(Order::getOrderNo, "SH"); // 业务需求：订单号必须包含SH

        if (StrUtil.isNotBlank(startDate)) {
            orderWrapper.ge(Order::getCreateTime, startDate + " 00:00:00");
        }
        if (StrUtil.isNotBlank(endDate)) {
            orderWrapper.le(Order::getCreateTime, endDate + " 23:59:59");
        }

        // 通过子查询关联 eb_merchant_order 表，直接在数据库层面过滤地址
        // 使用 inSql 来实现子查询
        orderWrapper.inSql(Order::getOrderNo,
            "SELECT mo.order_no FROM eb_merchant_order mo " +
            "WHERE mo.user_address LIKE '%" + regionKeyword + "%'"
        );

        List<Order> filteredOrders = orderService.list(orderWrapper);

        log.info("地区{}查询结果：订单数={}，关键词={}", region.getRegionName(), filteredOrders.size(), regionKeyword);

        // 如果查询结果为0，输出调试信息
        if (filteredOrders.isEmpty()) {
            log.warn("地区{}没有查询到订单，关键词：{}", region.getRegionName(), regionKeyword);
        }

        return filteredOrders;
    }

    /**
     * 从订单中提取完整地址
     */
    private String extractAddressFromOrder(Order order) {
        try {
            // 优先从商户订单表获取完整地址
            LambdaQueryWrapper<MerchantOrder> merchantOrderWrapper = Wrappers.lambdaQuery();
            merchantOrderWrapper.eq(MerchantOrder::getOrderNo, order.getOrderNo());
            merchantOrderWrapper.last("LIMIT 1");

            MerchantOrder merchantOrder = merchantOrderService.getOne(merchantOrderWrapper);
            if (merchantOrder != null && StrUtil.isNotBlank(merchantOrder.getUserAddress())) {
                return merchantOrder.getUserAddress();
            }
        } catch (Exception e) {
            log.debug("从商户订单表提取地址失败，订单号：{}", order.getOrderNo(), e);
        }

        return null;
    }
    
    private Integer getUsersByRegion(CityRegion region, String startDate, String endDate) {
        try {
            LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(User::getStatus, true);
            
            if (StrUtil.isNotBlank(startDate)) {
                wrapper.ge(User::getCreateTime, startDate + " 00:00:00");
            }
            if (StrUtil.isNotBlank(endDate)) {
                wrapper.le(User::getCreateTime, endDate + " 23:59:59");
            }
            
            List<User> users = userService.list(wrapper);
            
            // 直接通过用户表的地址字段过滤区域
            return (int) users.stream()
                .filter(user -> {
                    try {
                        // 根据区域类型进行匹配
                        switch (region.getRegionType()) {
                            case 1: // 省级
                                String userProvince = StrUtil.isNotBlank(user.getProvince()) ? 
                                    user.getProvince().replace("省", "").replace("市", "").replace("自治区", "") : "";
                                return region.getRegionName().equals(userProvince) ||
                                       region.getRegionName().replace("省", "").replace("市", "").replace("自治区", "").equals(userProvince);
                            case 2: // 市级
                                String userCity = StrUtil.isNotBlank(user.getCity()) ? 
                                    user.getCity().replace("市", "").replace("区", "") : "";
                                return region.getRegionName().equals(userCity) ||
                                       region.getRegionName().replace("市", "").replace("区", "").equals(userCity);
                            case 3: // 区级
                                String userDistrict = StrUtil.isNotBlank(user.getDistrict()) ? 
                                    user.getDistrict().replace("区", "").replace("县", "") : "";
                                return region.getRegionName().equals(userDistrict) ||
                                       region.getRegionName().replace("区", "").replace("县", "").equals(userDistrict);
                            default:
                                return false;
                        }
                    } catch (Exception e) {
                        log.debug("过滤用户地址失败，用户ID：{}", user.getId(), e);
                        return false;
                    }
                })
                .count();
        } catch (Exception e) {
            log.error("获取区域用户数量失败", e);
            return 0;
        }
    }
    
    private Integer getMerchantsByRegion(CityRegion region) {
        try {
            // 获取所有活跃的商户ID
            LambdaQueryWrapper<Merchant> merchantWrapper = Wrappers.lambdaQuery();
            merchantWrapper.eq(Merchant::getIsDel, false);
            merchantWrapper.eq(Merchant::getIsSwitch, true);
            merchantWrapper.select(Merchant::getId);
            List<Merchant> merchants = merchantService.list(merchantWrapper);
            
            if (merchants.isEmpty()) {
                return 0;
            }
            
            List<Integer> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());
            
            // 从商户地址表查询匹配的商户数量
            LambdaQueryWrapper<MerchantAddress> addressWrapper = Wrappers.lambdaQuery();
            addressWrapper.in(MerchantAddress::getMerId, merchantIds);
            addressWrapper.eq(MerchantAddress::getIsDel, false);
            addressWrapper.eq(MerchantAddress::getIsShow, true);
            
            List<MerchantAddress> merchantAddresses = merchantAddressService.list(addressWrapper);
            
            // 通过商户的地址信息过滤区域
            Set<Integer> matchedMerchantIds = merchantAddresses.stream()
                .filter(address -> {
                    try {
                        // 根据区域类型进行匹配
                        switch (region.getRegionType()) {
                            case 1: // 省级
                                String merchantProvince = StrUtil.isNotBlank(address.getProvince()) ? 
                                    address.getProvince().replace("省", "").replace("市", "").replace("自治区", "") : "";
                                return region.getRegionName().equals(merchantProvince) ||
                                       region.getRegionName().replace("省", "").replace("市", "").replace("自治区", "").equals(merchantProvince);
                            case 2: // 市级
                                String merchantCity = StrUtil.isNotBlank(address.getCity()) ? 
                                    address.getCity().replace("市", "").replace("区", "") : "";
                                return region.getRegionName().equals(merchantCity) ||
                                       region.getRegionName().replace("市", "").replace("区", "").equals(merchantCity);
                            case 3: // 区级
                                String merchantDistrict = StrUtil.isNotBlank(address.getDistrict()) ? 
                                    address.getDistrict().replace("区", "").replace("县", "") : "";
                                return region.getRegionName().equals(merchantDistrict) ||
                                       region.getRegionName().replace("区", "").replace("县", "").equals(merchantDistrict);
                            default:
                                return false;
                        }
                    } catch (Exception e) {
                        log.debug("过滤商户地址失败，商户ID：{}", address.getMerId(), e);
                        return false;
                    }
                })
                .map(MerchantAddress::getMerId)
                .collect(Collectors.toSet());
                
            return matchedMerchantIds.size();
        } catch (Exception e) {
            log.error("获取区域商户数量失败", e);
            return 0;
        }
    }
    
    private String extractRegionFromOrder(Order order, Integer regionType) {
        try {
            // 优先从商户订单表获取地址信息
            LambdaQueryWrapper<MerchantOrder> merchantOrderWrapper = Wrappers.lambdaQuery();
            merchantOrderWrapper.eq(MerchantOrder::getOrderNo, order.getOrderNo());
            merchantOrderWrapper.last("LIMIT 1");
            
            MerchantOrder merchantOrder = merchantOrderService.getOne(merchantOrderWrapper);
            if (merchantOrder != null && StrUtil.isNotBlank(merchantOrder.getUserAddress())) {
                String region = extractRegionFromAddress(merchantOrder.getUserAddress(), regionType);
                log.debug("订单{}从商户订单表提取地址：{}，解析结果：{}", order.getOrderNo(), merchantOrder.getUserAddress(), region);
                if (region != null && !"其他".equals(region)) {
                    return region;
                }
            } else {
                log.debug("订单{}在商户订单表中未找到地址信息", order.getOrderNo());
            }
            
            // 备选方案：通过用户默认地址获取地址信息
            if (order.getUid() != null) {
                LambdaQueryWrapper<UserAddress> addressWrapper = Wrappers.lambdaQuery();
                addressWrapper.eq(UserAddress::getUid, order.getUid());
                addressWrapper.eq(UserAddress::getIsDefault, true);
                addressWrapper.eq(UserAddress::getIsDel, false);
                addressWrapper.last("LIMIT 1");
                
                UserAddress userAddress = userAddressService.getOne(addressWrapper);
                if (userAddress != null) {
                    String region = null;
                    switch (regionType) {
                        case 1: // 省级
                            region = StrUtil.isNotBlank(userAddress.getProvince()) ? 
                                userAddress.getProvince().replace("省", "").replace("市", "").replace("自治区", "") : "其他";
                            break;
                        case 2: // 市级
                            region = StrUtil.isNotBlank(userAddress.getCity()) ? 
                                userAddress.getCity().replace("市", "").replace("区", "") : "其他";
                            break;
                        case 3: // 区级
                            region = StrUtil.isNotBlank(userAddress.getDistrict()) ? 
                                userAddress.getDistrict().replace("区", "").replace("县", "") : "其他";
                            break;
                    }
                    log.debug("订单{}从用户默认地址提取：province={}, city={}, district={}，解析结果：{}", 
                        order.getOrderNo(), userAddress.getProvince(), userAddress.getCity(), userAddress.getDistrict(), region);
                    if (region != null && !"其他".equals(region)) {
                        return region;
                    }
                }
                
                // 如果没有默认地址，尝试获取任意一个地址
                addressWrapper.clear();
                addressWrapper.eq(UserAddress::getUid, order.getUid());
                addressWrapper.eq(UserAddress::getIsDel, false);
                addressWrapper.last("LIMIT 1");
                
                userAddress = userAddressService.getOne(addressWrapper);
                if (userAddress != null) {
                    String region = null;
                    switch (regionType) {
                        case 1: // 省级
                            region = StrUtil.isNotBlank(userAddress.getProvince()) ? 
                                userAddress.getProvince().replace("省", "").replace("市", "").replace("自治区", "") : "其他";
                            break;
                        case 2: // 市级
                            region = StrUtil.isNotBlank(userAddress.getCity()) ? 
                                userAddress.getCity().replace("市", "").replace("区", "") : "其他";
                            break;
                        case 3: // 区级
                            region = StrUtil.isNotBlank(userAddress.getDistrict()) ? 
                                userAddress.getDistrict().replace("区", "").replace("县", "") : "其他";
                            break;
                    }
                    log.debug("订单{}从用户任意地址提取：province={}, city={}, district={}，解析结果：{}", 
                        order.getOrderNo(), userAddress.getProvince(), userAddress.getCity(), userAddress.getDistrict(), region);
                    if (region != null && !"其他".equals(region)) {
                        return region;
                    }
                }
            }
            
            // 最后备选方案：从订单扩展信息中解析地址
            if (StrUtil.isNotBlank(order.getOrderExtend())) {
                String region = extractRegionFromAddress(order.getOrderExtend(), regionType);
                log.debug("订单{}从订单扩展信息提取地址：{}，解析结果：{}", order.getOrderNo(), order.getOrderExtend(), region);
                if (region != null && !"其他".equals(region)) {
                    return region;
                }
            }
        } catch (Exception e) {
            log.debug("提取订单区域信息失败，订单号：{}", order.getOrderNo(), e);
        }
        
        log.debug("订单{}最终返回地区：其他", order.getOrderNo());
        return "其他";
    }

    @Override
    public JSONObject getCategoryRanking(Integer year, Integer limit) {
        JSONObject result = new JSONObject();
        
        try {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            
            // 获取当年所有已支付订单
            List<Order> orders = getOrdersByCondition(null, startDate, endDate);
            List<String> orderNos = orders.stream().map(Order::getOrderNo).collect(Collectors.toList());
            
            // 获取所有品牌
            LambdaQueryWrapper<ProductBrand> brandWrapper = Wrappers.lambdaQuery();
            brandWrapper.eq(ProductBrand::getIsShow, true); // 只显示启用的品牌
            brandWrapper.eq(ProductBrand::getIsDel, false); // 只显示未删除的品牌
            List<ProductBrand> brands = productBrandService.list(brandWrapper);
            
            List<Map<String, Object>> brandRankingList = new ArrayList<>();
            BigDecimal totalAllSales = BigDecimal.ZERO;
            
            for (ProductBrand brand : brands) {
                BigDecimal brandSales = BigDecimal.ZERO;
                
                // 即使没有订单，也要显示该品牌
                if (!CollUtil.isEmpty(orderNos)) {
                    // 获取该品牌下的商品
                    LambdaQueryWrapper<Product> productWrapper = Wrappers.lambdaQuery();
                    productWrapper.eq(Product::getIsDel, false);
                    productWrapper.eq(Product::getBrandId, brand.getId());
                    List<Product> products = productService.list(productWrapper);
                    
                    if (!CollUtil.isEmpty(products)) {
                        List<Integer> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
                        
                        // 获取该品牌商品的订单详情
                        LambdaQueryWrapper<OrderDetail> detailWrapper = Wrappers.lambdaQuery();
                        detailWrapper.in(OrderDetail::getProductId, productIds);
                        detailWrapper.in(OrderDetail::getOrderNo, orderNos);
                        List<OrderDetail> orderDetails = orderDetailService.list(detailWrapper);
                        
                        if (!CollUtil.isEmpty(orderDetails)) {
                            // 计算该品牌的总销售额
                            brandSales = orderDetails.stream()
                                .map(OrderDetail::getPayPrice)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        }
                    }
                }
                
                // 转换为万元
                BigDecimal salesInWan = brandSales.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
                
                Map<String, Object> brandData = new HashMap<>();
                brandData.put("brandName", brand.getName()); // 保持字段名不变，避免前端改动
                brandData.put("totalSales", salesInWan);
                brandData.put("ratio", "0.0%"); // 先设置为0，后面计算占比
                
                brandRankingList.add(brandData);
                totalAllSales = totalAllSales.add(brandSales);
            }
            
            // 计算占比
            for (Map<String, Object> brandData : brandRankingList) {
                BigDecimal sales = (BigDecimal) brandData.get("totalSales");
                BigDecimal salesOriginal = sales.multiply(BigDecimal.valueOf(10000)); // 转回原始金额计算占比
                
                if (totalAllSales.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal ratio = salesOriginal.divide(totalAllSales, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    brandData.put("ratio", ratio.setScale(1, RoundingMode.HALF_UP) + "%");
                }
            }
            
            // 按销售额排序（降序）
            brandRankingList.sort((a, b) -> {
                BigDecimal salesA = (BigDecimal) a.get("totalSales");
                BigDecimal salesB = (BigDecimal) b.get("totalSales");
                return salesB.compareTo(salesA);
            });
            
            result.put("categories", brandRankingList); // 保持字段名不变，避免前端改动
            result.put("year", year);
            
            log.info("获取品牌排行成功，年份：{}，品牌数量：{}", year, brandRankingList.size());
            
        } catch (Exception e) {
            log.error("获取品牌排行失败，年份：{}", year, e);
            result.put("categories", new ArrayList<>());
            result.put("year", year);
        }
        
        return result;
    }

    @Override
    public JSONObject getDistributorRanking(Integer year, Integer month, Integer limit, String dataType) {
        JSONObject result = new JSONObject();
        
        try {
            String[] dateRange = buildDateRange(year, month);
            String startDate = dateRange[0];
            String endDate = dateRange[1];
            
            // 获取所有商户列表（包括没有订单的商户）
            LambdaQueryWrapper<Merchant> merchantWrapper = Wrappers.lambdaQuery();
            merchantWrapper.eq(Merchant::getIsDel, false);
            merchantWrapper.eq(Merchant::getIsSwitch, true);
            List<Merchant> merchants = merchantService.list(merchantWrapper);
            
            List<Map<String, Object>> distributorRankingList = new ArrayList<>();
            
            // 遍历所有商户，包括没有订单的商户
            for (Merchant merchant : merchants) {
                // 获取商户的订单数据
                List<Order> orders = getOrdersByCondition(merchant.getId(), startDate, endDate);
                
                BigDecimal merchantSales = orders.stream()
                    .map(Order::getPayPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Map<String, Object> distributorData = new HashMap<>();
                distributorData.put("name", merchant.getName());
                distributorData.put("merchantId", merchant.getId());
                
                if ("orders".equals(dataType)) {
                    // 订单量统计
                    distributorData.put("value", orders.size());
                } else {
                    // 销售额统计（默认）- 转换为万元
                    BigDecimal salesInWan = merchantSales.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
                    distributorData.put("value", salesInWan);
                }
                
                distributorData.put("sales", merchantSales.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP));
                distributorData.put("orders", orders.size());
                distributorData.put("ratio", "0.0%");
                
                distributorRankingList.add(distributorData);
            }
            
            // 按指定数据类型排序（降序）
            if ("orders".equals(dataType)) {
                distributorRankingList.sort((a, b) -> {
                    Integer ordersA = (Integer) a.get("value");
                    Integer ordersB = (Integer) b.get("value");
                    return ordersB.compareTo(ordersA);
                });
            } else {
                distributorRankingList.sort((a, b) -> {
                    BigDecimal salesA = (BigDecimal) a.get("value");
                    BigDecimal salesB = (BigDecimal) b.get("value");
                    return salesB.compareTo(salesA);
                });
            }
            
            // 限制数量（如果指定了limit）
            if (limit != null && limit > 0 && distributorRankingList.size() > limit) {
                distributorRankingList = distributorRankingList.subList(0, limit);
            }
            
            // 计算限制后商户的总数（用于计算占比）
            BigDecimal limitedTotalSales = BigDecimal.ZERO;
            Integer limitedTotalOrders = 0;
            
            for (Map<String, Object> item : distributorRankingList) {
                if ("orders".equals(dataType)) {
                    limitedTotalOrders += (Integer) item.get("value");
                } else {
                    BigDecimal itemSales = (BigDecimal) item.get("value");
                    limitedTotalSales = limitedTotalSales.add(itemSales.multiply(BigDecimal.valueOf(10000))); // 还原为元
                }
            }
            
            // 计算占比（基于限制后的商户总和）
            if ("orders".equals(dataType) && limitedTotalOrders > 0) {
                // 订单量占比
                for (Map<String, Object> item : distributorRankingList) {
                    Integer itemOrders = (Integer) item.get("value");
                    BigDecimal ratio = BigDecimal.valueOf(itemOrders)
                        .divide(BigDecimal.valueOf(limitedTotalOrders), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    item.put("ratio", ratio.setScale(1, RoundingMode.HALF_UP) + "%");
                }
            } else if (!"orders".equals(dataType) && limitedTotalSales.compareTo(BigDecimal.ZERO) > 0) {
                // 销售额占比
                for (Map<String, Object> item : distributorRankingList) {
                    BigDecimal itemSales = (BigDecimal) item.get("value");
                    BigDecimal originalSales = itemSales.multiply(BigDecimal.valueOf(10000)); // 还原为元
                    BigDecimal ratio = originalSales.divide(limitedTotalSales, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    item.put("ratio", ratio.setScale(1, RoundingMode.HALF_UP) + "%");
                }
            }
            
            result.put("distributors", distributorRankingList);
            result.put("year", year);
            result.put("month", month);
            result.put("dataType", dataType);
            result.put("totalMerchants", merchants.size());
            
            log.info("获取分销商排行数据成功，年份：{}，月份：{}，数据类型：{}，商户总数：{}，返回数量：{}", 
                year, month, dataType, merchants.size(), distributorRankingList.size());
            
        } catch (Exception e) {
            log.error("获取分销商排行数据失败，年份：{}，月份：{}，数据类型：{}", year, month, dataType, e);
            
            // 即使出错也尝试返回空的商户列表
            try {
                LambdaQueryWrapper<Merchant> merchantWrapper = Wrappers.lambdaQuery();
                merchantWrapper.eq(Merchant::getIsDel, false);
                merchantWrapper.eq(Merchant::getIsSwitch, true);
                List<Merchant> merchants = merchantService.list(merchantWrapper);
                
                List<Map<String, Object>> emptyDistributorList = new ArrayList<>();
                for (Merchant merchant : merchants) {
                    Map<String, Object> distributorData = new HashMap<>();
                    distributorData.put("name", merchant.getName());
                    distributorData.put("merchantId", merchant.getId());
                    distributorData.put("value", "orders".equals(dataType) ? 0 : BigDecimal.ZERO);
                    distributorData.put("sales", BigDecimal.ZERO);
                    distributorData.put("orders", 0);
                    distributorData.put("ratio", "0.0%");
                    emptyDistributorList.add(distributorData);
                }
                
                result.put("distributors", emptyDistributorList);
                result.put("totalMerchants", merchants.size());
            } catch (Exception ex) {
                result.put("distributors", new ArrayList<>());
                result.put("totalMerchants", 0);
            }
            
            result.put("year", year);
            result.put("month", month);
            result.put("dataType", dataType);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getAvailableProvinces() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<CityRegion> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(CityRegion::getRegionType, 1);
            wrapper.orderByAsc(CityRegion::getRegionName);
            List<CityRegion> provinces = cityRegionService.list(wrapper);
            
            for (CityRegion province : provinces) {
                Map<String, Object> provinceData = new HashMap<>();
                String provinceName = province.getRegionName().replace("省", "").replace("市", "").replace("自治区", "");
                provinceData.put("code", province.getRegionId());
                provinceData.put("name", provinceName);
                provinceData.put("fullName", province.getRegionName());
                result.add(provinceData);
            }
            
            log.info("获取省份列表成功，数据量：{}", result.size());
            
        } catch (Exception e) {
            log.error("获取省份列表失败", e);
        }
        
        return result;
    }

    // Private helper methods

    private String[] buildDateRange(Integer year, Integer month) {
        String startDate, endDate;
        
        if (month != null && month >= 1 && month <= 12) {
            LocalDate firstDay = LocalDate.of(year, month, 1);
            LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);
            startDate = firstDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            endDate = lastDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            startDate = year + "-01-01";
            endDate = year + "-12-31";
        }
        
        return new String[]{startDate, endDate};
    }

    private List<Order> getOrdersByCondition(Integer merchantId, String startDate, String endDate) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
        
        if (merchantId != null && merchantId > 0) {
            wrapper.eq(Order::getMerId, merchantId);
        }
        wrapper.eq(Order::getPaid, 1);
        
        if (StrUtil.isNotBlank(startDate)) {
            wrapper.ge(Order::getCreateTime, startDate + " 00:00:00");
        }
        if (StrUtil.isNotBlank(endDate)) {
            wrapper.le(Order::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.eq(Order::getIsDel, false);
        // 订单号必须包含SH（业务需求）
        wrapper.like(Order::getOrderNo, "SH");
        
        List<Order> orders = orderService.list(wrapper);
        log.info("查询订单结果：总数={}，查询条件：merchantId={}，startDate={}，endDate={}", 
            orders.size(), merchantId, startDate, endDate);
        
        // 打印前几个订单号用于调试
        if (!orders.isEmpty()) {
            orders.stream().limit(5).forEach(order -> 
                log.info("订单号示例：{}，金额：{}，创建时间：{}", 
                    order.getOrderNo(), order.getPayPrice(), order.getCreateTime()));
        }
        
        return orders;
    }

    private Integer getTotalStock() {
        try {
            LambdaQueryWrapper<Product> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Product::getIsDel, false);
            
            List<Product> products = productService.list(wrapper);
            return products.stream()
                .mapToInt(Product::getStock)
                .sum();
        } catch (Exception e) {
            log.error("获取总库存失败", e);
            return 0;
        }
    }

    private Integer getUserCount(String startDate, String endDate) {
        try {
            LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(User::getStatus, true);
            
            if (StrUtil.isNotBlank(startDate)) {
                wrapper.ge(User::getCreateTime, startDate + " 00:00:00");
            }
            if (StrUtil.isNotBlank(endDate)) {
                wrapper.le(User::getCreateTime, endDate + " 23:59:59");
            }
            wrapper.isNotNull(User::getCity);
            return userService.count(wrapper);
        } catch (Exception e) {
            log.error("获取用户数量失败", e);
            return 0;
        }
    }

    private JSONObject getTodayData(String today) {
        JSONObject todayData = new JSONObject();
        
        try {
            List<Order> todayOrders = getOrdersByCondition(null, today, today);
            
            BigDecimal todaySales = todayOrders.stream()
                .map(Order::getPayPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            Integer todayUsers = getUserCount(today, today);
            
            todayData.put("sales", todaySales);
            todayData.put("orders", todayOrders.size());
            todayData.put("users", todayUsers != null ? todayUsers : 0);
        } catch (Exception e) {
            log.error("获取今日数据失败", e);
            todayData.put("sales", BigDecimal.ZERO);
            todayData.put("orders", 0);
            todayData.put("users", 0);
        }
        
        return todayData;
    }

    private String extractProvinceFromOrder(Order order) {
        try {
            // 优先从商户订单表获取地址信息
            LambdaQueryWrapper<MerchantOrder> merchantOrderWrapper = Wrappers.lambdaQuery();
            merchantOrderWrapper.eq(MerchantOrder::getOrderNo, order.getOrderNo());
            merchantOrderWrapper.last("LIMIT 1");
            
            MerchantOrder merchantOrder = merchantOrderService.getOne(merchantOrderWrapper);
            if (merchantOrder != null && StrUtil.isNotBlank(merchantOrder.getUserAddress())) {
                String region = extractRegionFromAddress(merchantOrder.getUserAddress(), 1); // 1表示省级
                if (region != null && !"其他".equals(region)) {
                    return region;
                }
            }
            
            // 备选方案：通过用户默认地址获取省份信息
            if (order.getUid() != null) {
                LambdaQueryWrapper<UserAddress> addressWrapper = Wrappers.lambdaQuery();
                addressWrapper.eq(UserAddress::getUid, order.getUid());
                addressWrapper.eq(UserAddress::getIsDefault, true);
                addressWrapper.eq(UserAddress::getIsDel, false);
                addressWrapper.last("LIMIT 1");
                
                UserAddress userAddress = userAddressService.getOne(addressWrapper);
                if (userAddress != null && StrUtil.isNotBlank(userAddress.getProvince())) {
                    return userAddress.getProvince().replace("省", "").replace("市", "").replace("自治区", "");
                }
                
                // 如果没有默认地址，尝试获取任意一个地址
                addressWrapper.clear();
                addressWrapper.eq(UserAddress::getUid, order.getUid());
                addressWrapper.eq(UserAddress::getIsDel, false);
                addressWrapper.last("LIMIT 1");
                
                userAddress = userAddressService.getOne(addressWrapper);
                if (userAddress != null && StrUtil.isNotBlank(userAddress.getProvince())) {
                    return userAddress.getProvince().replace("省", "").replace("市", "").replace("自治区", "");
                }
            }
            
            // 最后备选方案：从订单扩展信息中解析地址
            if (StrUtil.isNotBlank(order.getOrderExtend())) {
                String region = extractRegionFromAddress(order.getOrderExtend(), 1); // 1表示省级
                if (region != null && !"其他".equals(region)) {
                    return region;
                }
            }
        } catch (Exception e) {
            log.debug("提取订单省份信息失败，订单号：{}", order.getOrderNo(), e);
        }
        
        return "其他";
    }

    private String extractRegionFromAddress(String address, Integer regionType) {
        if (StrUtil.isBlank(address)) {
            return "其他";
        }
        
        log.debug("开始解析地址：{}，目标区域类型：{}", address, regionType);
        
        try {
            // 省份列表
            String[] provinces = {"北京市", "天津市", "河北省", "山西省", "内蒙古自治区", "辽宁省", "吉林省", "黑龙江省", 
                                 "上海市", "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", 
                                 "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省", "重庆市", "四川省", "贵州省", 
                                 "云南省", "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区"};
            
            // 简化版省份名称（用于匹配）
            String[] provincesShort = {"北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江", 
                                      "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", 
                                      "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州", 
                                      "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆"};
            
            if (regionType == 1) {
                // 省级：匹配省份
                for (int i = 0; i < provinces.length; i++) {
                    if (address.contains(provinces[i]) || address.contains(provincesShort[i])) {
                        log.debug("地址{}匹配省份：{}", address, provincesShort[i]);
                        return provincesShort[i];
                    }
                }
            } else if (regionType == 2) {
                // 市级：简单提取（实际项目中可能需要更复杂的城市匹配逻辑）
                if (address.contains("武汉")) {
                    log.debug("地址{}匹配城市：武汉", address);
                    return "武汉";
                }
                if (address.contains("黄石")) return "黄石";
                if (address.contains("十堰")) return "十堰";
                if (address.contains("宜昌")) return "宜昌";
                if (address.contains("襄阳")) return "襄阳";
                if (address.contains("鄂州")) return "鄂州";
                if (address.contains("荆门")) return "荆门";
                if (address.contains("孝感")) return "孝感";
                if (address.contains("荆州")) return "荆州";
                if (address.contains("黄冈")) return "黄冈";
                if (address.contains("咸宁")) return "咸宁";
                if (address.contains("随州")) return "随州";
                if (address.contains("恩施")) return "恩施";
                // 可以继续添加更多城市...
                log.debug("地址{}未匹配到任何城市", address);
                return "其他";
            } else if (regionType == 3) {
                // 区级：简单提取（实际项目中可能需要更复杂的区县匹配逻辑）
                if (address.contains("江岸区")) return "江岸区";
                if (address.contains("江汉区")) return "江汉区";
                if (address.contains("硚口区")) return "硚口区";
                if (address.contains("汉阳区")) return "汉阳区";
                if (address.contains("武昌区")) return "武昌区";
                if (address.contains("青山区")) return "青山区";
                if (address.contains("洪山区")) return "洪山区";
                // 可以继续添加更多区县...
                log.debug("地址{}未匹配到任何区域", address);
                return "其他";
            }
            
        } catch (Exception e) {
            log.debug("解析地址失败：{}", address, e);
        }
        
        log.debug("地址{}最终返回：其他", address);
        return "其他";
    }
    private List<Integer> getMerchantIdsByProvince(String province) {
        LambdaQueryWrapper<Merchant> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Merchant::getIsDel, false);
        wrapper.eq(Merchant::getIsSwitch, true);
        
        // Simplified - would need proper address matching
        List<Merchant> merchants = merchantService.list(wrapper);
        return merchants.stream()
            .map(Merchant::getId)
            .collect(Collectors.toList());
    }

    private Integer getUserCountByCondition(String startDate, String endDate, String province) {
        try {
            LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(User::getStatus, true);
            
            if (StrUtil.isNotBlank(startDate)) {
                wrapper.ge(User::getCreateTime, startDate + " 00:00:00");
            }
            if (StrUtil.isNotBlank(endDate)) {
                wrapper.le(User::getCreateTime, endDate + " 23:59:59");
            }
            
            return userService.count(wrapper);
        } catch (Exception e) {
            log.error("根据条件获取用户数量失败", e);
            return 0;
        }
    }
    private List<Map<String, Object>> getRegionSalesDataInternal(Integer merchantId, String startDate, String endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 获取订单数据并通过用户地址表获取省份信息
            LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
            if (merchantId != null && merchantId > 0) {
                wrapper.eq(Order::getMerId, merchantId);
            }
            wrapper.eq(Order::getPaid, 1); // 已支付
            if (StrUtil.isNotBlank(startDate)) {
                wrapper.ge(Order::getCreateTime, startDate + " 00:00:00");
            }
            if (StrUtil.isNotBlank(endDate)) {
                wrapper.le(Order::getCreateTime, endDate + " 23:59:59");
            }
            
            List<Order> orders = orderService.list(wrapper);
            
            // 按省份分组统计
            Map<String, List<Order>> regionOrderMap = new HashMap<>();
            
            for (Order order : orders) {
                String province = extractProvinceFromOrder(order);
                regionOrderMap.computeIfAbsent(province, k -> new ArrayList<>()).add(order);
            }
            
            // 为每个省份添加坐标和统计数据
            Map<String, double[]> provinceCoords = getProvinceCoordinates();
            
            for (Map.Entry<String, List<Order>> entry : regionOrderMap.entrySet()) {
                String province = entry.getKey();
                List<Order> regionOrders = entry.getValue();
                
                BigDecimal sales = regionOrders.stream()
                    .map(Order::getPayPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Map<String, Object> regionItem = new HashMap<>();
                regionItem.put("name", province);
                regionItem.put("value", sales);
                regionItem.put("orders", regionOrders.size());
                
                // 添加坐标信息
                double[] coords = provinceCoords.get(province);
                if (coords != null) {
                    regionItem.put("longitude", coords[0]);
                    regionItem.put("latitude", coords[1]);
                } else {
                    regionItem.put("longitude", 114.2972);
                    regionItem.put("latitude", 30.5928);
                }
                
                result.add(regionItem);
            }
            
            log.info("获取地区销售数据成功，商户ID：{}，数据量：{}", merchantId, result.size());
            
        } catch (Exception e) {
            log.error("获取地区销售数据失败，商户ID：{}", merchantId, e);
        }
        
        return result;
    }
    private BigDecimal getTotalSalesByMerchant(Integer merchantId) {
        try {
            LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
            if (merchantId != null && merchantId > 0) {
                wrapper.eq(Order::getMerId, merchantId);
            }
            wrapper.eq(Order::getPaid, 1);
            
            List<Order> orders = orderService.list(wrapper);
            return orders.stream()
                .map(Order::getPayPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("获取商户总销售额失败", e);
            return BigDecimal.ZERO;
        }
    }

    private Integer getTotalOrderCountByMerchant(Integer merchantId) {
        try {
            LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
            if (merchantId != null && merchantId > 0) {
                wrapper.eq(Order::getMerId, merchantId);
            }
            wrapper.eq(Order::getPaid, 1);
            return orderService.count(wrapper);
        } catch (Exception e) {
            log.error("获取商户总订单数失败", e);
            return 0;
        }
    }

    /**
     * 获取商户总数
     */
    private Integer getMerchantCount(Integer year) {
        try {
            String yearStartDate = year + "-01-01";
            String yearEndDate = year + "-12-31";
            LambdaQueryWrapper<Merchant> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Merchant::getIsDel, false);
            wrapper.ge(Merchant::getCreateTime, yearStartDate)
                    .le(Merchant::getCreateTime, yearEndDate);
            return merchantService.count(wrapper);
        } catch (Exception e) {
            log.error("获取商户总数失败", e);
            return 0;
        }
    }

    /**
     * 获取用户总数
     */
    private Integer getTotalUserCount(Integer year) {
        try {
            String yearStartDate = year + "-01-01";
            String yearEndDate = year + "-12-31";
            LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(User::getStatus, true);
            wrapper.ge(User::getCreateTime, yearStartDate)
                    .le(User::getCreateTime, yearEndDate);
            //市不能为空
            wrapper.isNotNull(User::getCity);
            return userService.count(wrapper);
        } catch (Exception e) {
            log.error("获取用户总数失败", e);
            return 0;
        }
    }

    private Integer getYearBrandCount(int year) {
        try {
            String yearStartDate = year + "-01-01";
            String yearEndDate = year + "-12-31";
            
            LambdaQueryWrapper<ProductBrand> wrapper = Wrappers.lambdaQuery();
            wrapper.ge(ProductBrand::getCreateTime, yearStartDate)
                   .le(ProductBrand::getCreateTime, yearEndDate)
                   .eq(ProductBrand::getIsShow, true)
                   .eq(ProductBrand::getIsDel, false);
            
            return productBrandService.count(wrapper);
        } catch (Exception e) {
            log.error("获取年度品牌总数失败", e);
            return 0;
        }
    }

    private Integer getYearSkuCount(int year) {
        try {
            String yearStartDate = year + "-01-01";
            String yearEndDate = year + "-12-31";
            
            LambdaQueryWrapper<Product> wrapper = Wrappers.lambdaQuery();
            wrapper.ge(Product::getCreateTime, yearStartDate)
                   .le(Product::getCreateTime, yearEndDate)
                   .eq(Product::getIsShow, true)
                   .eq(Product::getIsDel, false);
            
            List<Product> products = productService.list(wrapper);
            int totalSkuCount = 0;
            
            for (Product product : products) {
                LambdaQueryWrapper<ProductAttrValue> attrWrapper = Wrappers.lambdaQuery();
                attrWrapper.eq(ProductAttrValue::getProductId, product.getId());
                totalSkuCount += productAttrValueService.count(attrWrapper);
            }
            
            return totalSkuCount;
        } catch (Exception e) {
            log.error("获取年度SKU总数失败", e);
            return 0;
        }
    }

    private Map<String, double[]> getProvinceCoordinates() {
        Map<String, double[]> coords = new HashMap<>();

        // 主要省份坐标（经度，纬度）
        coords.put("北京", new double[]{116.4074, 39.9042});
        coords.put("上海", new double[]{121.4737, 31.2304});
        coords.put("天津", new double[]{117.2008, 39.0842});
        coords.put("重庆", new double[]{106.5516, 29.5630});
        coords.put("河北", new double[]{114.5149, 38.0428});
        coords.put("山西", new double[]{112.5489, 37.8570});
        coords.put("辽宁", new double[]{123.4315, 41.8057});
        coords.put("吉林", new double[]{125.3245, 43.8868});
        coords.put("黑龙江", new double[]{126.6420, 45.7569});
        coords.put("江苏", new double[]{118.7633, 32.0615});
        coords.put("浙江", new double[]{120.1536, 30.2650});
        coords.put("安徽", new double[]{117.2272, 31.8206});
        coords.put("福建", new double[]{119.2965, 26.0745});
        coords.put("江西", new double[]{115.8999, 28.6759});
        coords.put("山东", new double[]{117.0009, 36.6758});
        coords.put("河南", new double[]{113.6401, 34.7566});
        coords.put("湖北", new double[]{114.2972, 30.5928});
        coords.put("湖南", new double[]{112.9388, 28.2283});
        coords.put("广东", new double[]{113.2644, 23.1291});
        coords.put("海南", new double[]{110.3312, 20.0311});
        coords.put("四川", new double[]{104.0757, 30.6515});
        coords.put("贵州", new double[]{106.7070, 26.5783});
        coords.put("云南", new double[]{102.7123, 25.0406});
        coords.put("陕西", new double[]{108.9398, 34.3416});
        coords.put("甘肃", new double[]{103.8236, 36.0581});
        coords.put("青海", new double[]{101.7781, 36.6233});
        coords.put("内蒙古", new double[]{111.7708, 40.8477});
        coords.put("广西", new double[]{108.3201, 22.8241});
        coords.put("西藏", new double[]{91.1177, 29.6466});
        coords.put("宁夏", new double[]{106.2589, 38.4723});
        coords.put("新疆", new double[]{87.6168, 43.7930});

        return coords;
    }
}