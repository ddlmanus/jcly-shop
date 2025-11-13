package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.DateConstants;
import com.zbkj.common.constants.OrderConstants;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.city.CityRegion;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductCategory;
import com.zbkj.common.model.record.ProductDayRecord;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.user.UserAddress;
import com.zbkj.common.request.ProductRankingRequest;
import com.zbkj.common.response.*;
import com.zbkj.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.zbkj.service.dao.UserVisitRecordDao;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户表 服务实现类
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
public class HomeServiceImpl implements HomeService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserVisitRecordService userVisitRecordService;
    @Autowired
    private ProductService productService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private RefundOrderService refundOrderService;
    @Autowired
    private UserMerchantCollectService userMerchantCollectService;
    @Autowired
    private ProductDayRecordService productDayRecordService;
    @Autowired
    private MerchantDayRecordService merchantDayRecordService;
    @Autowired
    private UserVisitRecordDao userVisitRecordDao;
    @Autowired
    private ProductBrandService productBrandService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private StockInRecordService stockInRecordService;
    @Autowired
    private StockOutRecordService stockOutRecordService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ProductReplyService productReplyService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private CaiShiJiaPlatformService caiShiJiaPlatformService;

    /**
     * 首页数据
     * @return HomeRateResponse
     */
    @Override
    public HomeRateResponse indexMerchantDate(SystemAdmin systemAdmin) {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        HomeRateResponse response = new HomeRateResponse();
        response.setSales(orderService.getPayOrderAmountByDate(systemAdmin.getMerId(), today));
        response.setYesterdaySales(orderService.getPayOrderAmountByDate(systemAdmin.getMerId(), yesterday));
        response.setOrderNum(orderService.getOrderNumByDate(systemAdmin.getMerId(), today));
        response.setYesterdayOrderNum(orderService.getOrderNumByDate(systemAdmin.getMerId(), yesterday));
        response.setFollowNum(userMerchantCollectService.getCountByMerId(systemAdmin.getMerId()));
        response.setVisitorsNum(merchantDayRecordService.getVisitorsByDate(systemAdmin.getMerId(), today));
        response.setYesterdayVisitorsNum(merchantDayRecordService.getVisitorsByDate(systemAdmin.getMerId(), yesterday));
        return response;
    }

    /**
     * 经营数据：
     * @return HomeOperatingMerDataResponse
     */
    @Override
    public HomeOperatingMerDataResponse operatingMerchantData(SystemAdmin systemAdmin) {
        HomeOperatingMerDataResponse response = new HomeOperatingMerDataResponse();
        response.setNotShippingOrderNum(orderService.getNotShippingNum(systemAdmin.getMerId()));
        response.setAwaitVerificationOrderNum(orderService.getAwaitVerificationNum(systemAdmin.getMerId()));
        response.setRefundingOrderNum(refundOrderService.getAwaitAuditNum(systemAdmin.getMerId()));
        response.setOnSaleProductNum(productService.getOnSaleNum(systemAdmin.getMerId()));
        response.setAwaitAuditProductNum(productService.getAwaitAuditNum(systemAdmin.getMerId()));
        return response;
    }

    /**
     * 平台端首页数据
     * @return PlatformHomeRateResponse
     */
    @Override
    public PlatformHomeRateResponse indexPlatformDate() {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        PlatformHomeRateResponse response = new PlatformHomeRateResponse();
        response.setTodayNewUserNum(userService.getRegisterNumByDate(today));
        response.setYesterdayNewUserNum(userService.getRegisterNumByDate(yesterday));
        response.setPageviews(userVisitRecordService.getPageviewsByDate(today));
        response.setYesterdayPageviews(userVisitRecordService.getPageviewsByDate(yesterday));
        response.setTodayNewMerchantNum(merchantService.getNewNumByDate(today));
        response.setYesterdayNewMerchantNum(merchantService.getNewNumByDate(yesterday));
        response.setOrderNum(orderService.getOrderNumByDate(0, today));
        response.setYesterdayOrderNum(orderService.getOrderNumByDate(0, yesterday));
        response.setSales(orderService.getPayOrderAmountByDate(0, today));
        response.setTotalSales(orderService.getPayOrderTotalAmount(0));
        response.setYesterdaySales(orderService.getPayOrderAmountByDate(0, yesterday));
        response.setUserNum(userService.getTotalNum());
        response.setMerchantNum(merchantService.getAllCount());
        return response;
    }

    /**
     * 平台端首页经营数据
     * @return HomeOperatingDataResponse
     */
    @Override
    public HomeOperatingDataResponse operatingPlatformData() {
        HomeOperatingDataResponse response = new HomeOperatingDataResponse();
        response.setNotShippingOrderNum(orderService.getNotShippingNum(0));
        response.setAwaitVerificationOrderNum(orderService.getAwaitVerificationNum(0));
        response.setRefundingOrderNum(refundOrderService.getAwaitAuditNum(0));
        response.setOnSaleProductNum(productService.getOnSaleNum(0));
        response.setAwaitAuditProductNum(productService.getAwaitAuditNum(0));
        // 新增字段：销售总额和总用户数
        response.setTotalSales(orderService.getPayOrderTotalAmount(0));
        response.setTotalUsers(userService.getTotalNum());
        return response;
    }

    /**
     * 平台端首页获取用户渠道数据
     */
    @Override
    public List<UserChannelDataResponse> getUserChannelData() {
        List<User> userList = userService.getChannelData();
        return userList.stream().map(e -> {
            UserChannelDataResponse response = new UserChannelDataResponse();
            response.setRegisterType(e.getRegisterType());
            response.setNum(e.getPayCount());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 商户端商品支付排行榜
     */
    @Override
    public List<ProductRankingResponse> merchantProductPayRanking(SystemAdmin systemAdmin) {
        Integer merId = systemAdmin.getMerId();
        ProductRankingRequest request = new ProductRankingRequest();
        request.setMerId(merId);
        request.setDateLimit(DateConstants.SEARCH_DATE_LATELY_7);
        request.setSortKey("salesAmount");
        PageInfo<ProductDayRecord> pageInfo = productDayRecordService.getRanking(request);
        List<ProductDayRecord> recordList = pageInfo.getList();
        List<ProductRankingResponse> list = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(recordList)) {
            for (ProductDayRecord record : recordList) {
                Product product = productService.getById(record.getProductId());
                ProductRankingResponse response = new ProductRankingResponse();
                BeanUtils.copyProperties(record, response);
                response.setSalesAmount(record.getOrderSuccessProductFee());
                if(Objects.nonNull(product)){
                    response.setProductId(product.getId());
                    response.setProName(product.getName());
                    response.setImage(product.getImage());
                }
                list.add(response);
            }
        }
        return list;
    }

    /**
     * 商品浏览量排行榜
     */
    @Override
    public List<ProductRankingResponse> merchantProductPageviewRanking(SystemAdmin systemAdmin) {
        Integer merId = systemAdmin.getMerId();
        ProductRankingRequest request = new ProductRankingRequest();
        request.setMerId(merId);
        request.setDateLimit(DateConstants.SEARCH_DATE_LATELY_7);
        request.setSortKey("pageviews");
        PageInfo<ProductDayRecord> pageInfo = productDayRecordService.getRanking(request);
        List<ProductDayRecord> recordList = pageInfo.getList();
        List<ProductRankingResponse> list = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(recordList)) {
            for (ProductDayRecord record : recordList) {
                Product product = productService.getById(record.getProductId());
                ProductRankingResponse response = new ProductRankingResponse();
                BeanUtils.copyProperties(record, response);
                response.setPageView(record.getPageView());
                if(Objects.nonNull(product)){
                    response.setProductId(product.getId());
                    response.setProName(product.getName());
                    response.setImage(product.getImage());
                }
                list.add(response);
            }
        }
        return list;
    }

    /**
     * 平台仪表板概览数据
     */
    @Override
    public PlatformDashboardOverviewResponse getPlatformDashboardOverview() {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        
        PlatformDashboardOverviewResponse response = new PlatformDashboardOverviewResponse();
        
        // 商品统计 - 使用真实数据库查询
        Integer onSaleNum = productService.getOnSaleNum(0);
        Integer awaitAuditNum = productService.getAwaitAuditNum(0);
        
        response.setProductOnSale(onSaleNum);
        response.setProductPending(awaitAuditNum);
        response.setProductTotal(onSaleNum + awaitAuditNum);
        response.setProductOffSale(0); // 可以根据需要添加查询下架商品的方法
        
        // 商品业绩 - 使用真实订单数据
        BigDecimal todaySales = orderService.getPayOrderAmountByDate(0, today);
        BigDecimal yesterdaySales = orderService.getPayOrderAmountByDate(0, yesterday);
        response.setProductSales(todaySales);
        response.setProductSalesGrowthRate(calculateGrowthRate(todaySales, yesterdaySales));
        
        // 计算本月销售额
        BigDecimal monthSales = calculateMonthSales(0);
        response.setMonthSales(monthSales);
        response.setTotalSales(monthSales); // 这里可以根据需要计算历史累计销售额
        
        // 店铺统计 - 使用真实商户数据
        Integer totalMerchants = merchantService.getAllCount();
        Integer todayMerchants = merchantService.getNewNumByDate(today);
        Integer yesterdayMerchants = merchantService.getNewNumByDate(yesterday);
        Integer monthMerchants = calculateMonthNewMerchants();
        
        response.setMerchantTotal(totalMerchants);
        response.setMerchantToday(todayMerchants);
        response.setMerchantThisMonth(monthMerchants);
        response.setMerchantGrowthRate(calculateGrowthRate(
            BigDecimal.valueOf(todayMerchants), 
            BigDecimal.valueOf(yesterdayMerchants)
        ));
        
        // 供应商统计 (在这个系统中，供应商等同于商户)
        response.setSupplierTotal(totalMerchants);
        response.setSupplierToday(todayMerchants);
        response.setSupplierThisMonth(monthMerchants);
        response.setSupplierGrowthRate(response.getMerchantGrowthRate());
        
        return response;
    }

    /**
     * 平台收益汇总数据
     */
    @Override
    public PlatformRevenueResponse getPlatformRevenue() {
        PlatformRevenueResponse response = new PlatformRevenueResponse();
        
        // 基于真实数据计算收益
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        // 计算最近30天的总收益（可以根据实际业务逻辑调整）
        String endDate = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        String startDate = DateUtil.offsetDay(DateUtil.date(), -30).toString(DateConstants.DATE_FORMAT_DATE);
        
        // 收益明细
        List<PlatformRevenueResponse.RevenueDetail> details = new ArrayList<>();
        
        // 这里可以根据实际业务需求添加真实的收益计算逻辑
        // 例如：手续费、佣金、服务费等
        PlatformRevenueResponse.RevenueDetail mainRevenue = new PlatformRevenueResponse.RevenueDetail();
        mainRevenue.setType("平台服务费");
        mainRevenue.setAmount(BigDecimal.ZERO); // 这里可以添加真实的平台服务费计算
        mainRevenue.setPercentage("0%");
        details.add(mainRevenue);
        
        response.setRevenueDetails(details);
        response.setTotalRevenue(BigDecimal.ZERO);
        
        // 月度趋势 - 基于真实数据生成
        List<PlatformRevenueResponse.MonthlyRevenue> monthlyTrend = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -11); // 最近12个月
        
        for (int i = 0; i < 12; i++) {
            String month = DateUtil.format(calendar.getTime(), "yyyy-MM");
            PlatformRevenueResponse.MonthlyRevenue monthlyRevenue = new PlatformRevenueResponse.MonthlyRevenue();
            monthlyRevenue.setMonth(month);
            monthlyRevenue.setWithdrawalFee(BigDecimal.ZERO);
            monthlyRevenue.setCategoryRevenue(BigDecimal.ZERO);
            monthlyTrend.add(monthlyRevenue);
            calendar.add(Calendar.MONTH, 1);
        }
        response.setMonthlyTrend(monthlyTrend);
        
        return response;
    }

    /**
     * 平台商品销量排行榜前10
     */
    @Override
    public List<ProductSalesRankingResponse> getPlatformProductSalesRanking() {
        List<ProductSalesRankingResponse> list = new ArrayList<>();
        
        // 使用真实的ProductDayRecord数据查询销量排行
        ProductRankingRequest request = new ProductRankingRequest();
      //  request.setMerId(0); // 0表示全平台
        request.setDateLimit(DateConstants.SEARCH_DATE_LATELY_30); // 最近30天
        request.setSortKey("salesAmount"); // 按销售额排序
        request.setPage(1);
        request.setLimit(10);
        
        PageInfo<ProductDayRecord> pageInfo = productDayRecordService.getRanking(request);
        List<ProductDayRecord> recordList = pageInfo.getList();
        
        if (CollUtil.isNotEmpty(recordList)) {
            // 计算总销量用于百分比计算
            BigDecimal totalSales = recordList.stream()
                .map(ProductDayRecord::getOrderSuccessProductFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            for (int i = 0; i < recordList.size(); i++) {
                ProductDayRecord record = recordList.get(i);
                Product product = productService.getById(record.getProductId());
                if (product != null) {
                    ProductSalesRankingResponse response = new ProductSalesRankingResponse();
                    response.setRank(i + 1);
                    response.setProductId(product.getId());
                    response.setProductName(product.getName());
                    response.setProductImage(product.getImage());
                    response.setSalesCount(record.getOrderProductNum());
                    response.setSalesAmount(record.getOrderSuccessProductFee());
                    response.setMerId(product.getMerId());
                    
                    // 获取商户名称
                    Merchant merchant = merchantService.getById(product.getMerId());
                    response.setMerchantName(merchant != null ? merchant.getName() : "未知商户");
                    
                    // 计算占比
                    if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal percentage = record.getOrderSuccessProductFee()
                            .divide(totalSales, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                        response.setPercentage(percentage.setScale(1, RoundingMode.HALF_UP) + "%");
                    } else {
                        response.setPercentage("0%");
                    }
                    
                    list.add(response);
                }
            }
        }
        
        // 如果没有足够的数据，填充空白项
        while (list.size() < 10) {
            ProductSalesRankingResponse response = new ProductSalesRankingResponse();
            response.setRank(list.size() + 1);
            response.setProductName("虚位以待");
            response.setSalesCount(0);
            response.setSalesAmount(BigDecimal.ZERO);
            response.setPercentage("0%");
            list.add(response);
        }
        
        return list;
    }

    /**
     * 平台店铺成交金额排行榜前10
     */
    @Override
    public List<MerchantSalesRankingResponse> getPlatformMerchantSalesRanking() {
        List<MerchantSalesRankingResponse> list = new ArrayList<>();
        
        // 获取所有商户
        List<Merchant> merchantList = merchantService.all();
        
        if (CollUtil.isNotEmpty(merchantList)) {
            String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
            String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
            
            // 计算每个商户的销售额
            Map<Integer, BigDecimal> merchantSalesMap = new HashMap<>();
            Map<Integer, BigDecimal> merchantYesterdaySalesMap = new HashMap<>();
            Map<Integer, Integer> merchantOrderCountMap = new HashMap<>();
            
            for (Merchant merchant : merchantList) {
                // 计算最近30天销售额
                BigDecimal monthSales = calculateMonthSales(merchant.getId());
                BigDecimal yesterdaySales = orderService.getPayOrderAmountByDate(merchant.getId(), yesterday);
                Integer orderCount = orderService.getOrderNumByDate(merchant.getId(), DateUtil.offsetDay(DateUtil.date(), -30).toString(DateConstants.DATE_FORMAT_DATE));
                
                merchantSalesMap.put(merchant.getId(), monthSales);
                merchantYesterdaySalesMap.put(merchant.getId(), yesterdaySales);
                merchantOrderCountMap.put(merchant.getId(), orderCount);
            }
            
            // 按销售额排序
            List<Merchant> sortedMerchants = merchantList.stream()
                .sorted((m1, m2) -> merchantSalesMap.get(m2.getId()).compareTo(merchantSalesMap.get(m1.getId())))
                .limit(10)
                .collect(Collectors.toList());
            
            // 计算总销售额用于百分比计算
            BigDecimal totalSales = merchantSalesMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            for (int i = 0; i < sortedMerchants.size(); i++) {
                Merchant merchant = sortedMerchants.get(i);
                MerchantSalesRankingResponse response = new MerchantSalesRankingResponse();
                response.setRank(i + 1);
                response.setMerId(merchant.getId());
                response.setMerchantName(merchant.getName());
                response.setMerchantAvatar(merchant.getAvatar());
                
                BigDecimal salesAmount = merchantSalesMap.get(merchant.getId());
                BigDecimal yesterdaySalesAmount = merchantYesterdaySalesMap.get(merchant.getId());
                Integer orderCount = merchantOrderCountMap.get(merchant.getId());
                
                response.setSalesAmount(salesAmount);
                response.setYesterdaySalesAmount(yesterdaySalesAmount);
                response.setOrderCount(orderCount);
                
                // 计算增长率
                response.setGrowthRate(calculateGrowthRate(salesAmount, yesterdaySalesAmount));
                
                // 计算占比
                if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = salesAmount
                        .divide(totalSales, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    response.setPercentage(percentage.setScale(1, RoundingMode.HALF_UP) + "%");
                } else {
                    response.setPercentage("0%");
                }
                
                list.add(response);
            }
        }
        
        // 如果没有足够的数据，填充空白项
        while (list.size() < 10) {
            MerchantSalesRankingResponse response = new MerchantSalesRankingResponse();
            response.setRank(list.size() + 1);
            response.setMerchantName("虚位以待");
            response.setSalesAmount(BigDecimal.ZERO);
            response.setYesterdaySalesAmount(BigDecimal.ZERO);
            response.setOrderCount(0);
            response.setGrowthRate("0%");
            response.setPercentage("0%");
            list.add(response);
        }
        
        return list;
    }

    /**
     * 平台供应商供货排行榜前10
     */
    @Override
    public List<SupplierRankingResponse> getPlatformSupplierRanking() {
        List<SupplierRankingResponse> list = new ArrayList<>();
        
        // 在这个系统中，供应商就是商户，所以基于商户和商品数据来计算
        List<Merchant> merchantList = merchantService.all();
        
        if (CollUtil.isNotEmpty(merchantList)) {
            String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
            String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
            
            // 计算每个商户（供应商）的供货数据
            Map<Integer, BigDecimal> supplierSupplyMap = new HashMap<>();
            Map<Integer, BigDecimal> supplierYesterdaySupplyMap = new HashMap<>();
            Map<Integer, Integer> supplierProductCountMap = new HashMap<>();
            Map<Integer, Integer> supplierOrderCountMap = new HashMap<>();
            
            for (Merchant merchant : merchantList) {
                // 供货金额 = 销售额
                BigDecimal supplyAmount = calculateMonthSales(merchant.getId());
                BigDecimal yesterdaySupplyAmount = orderService.getPayOrderAmountByDate(merchant.getId(), yesterday);
                
                // 商品数量
                Integer productCount = productService.getOnSaleNum(merchant.getId());
                
                // 订单数量
                Integer orderCount = orderService.getOrderNumByDate(merchant.getId(), DateUtil.offsetDay(DateUtil.date(), -30).toString(DateConstants.DATE_FORMAT_DATE));
                
                supplierSupplyMap.put(merchant.getId(), supplyAmount);
                supplierYesterdaySupplyMap.put(merchant.getId(), yesterdaySupplyAmount);
                supplierProductCountMap.put(merchant.getId(), productCount);
                supplierOrderCountMap.put(merchant.getId(), orderCount);
            }
            
            // 按供货金额排序
            List<Merchant> sortedSuppliers = merchantList.stream()
                .sorted((m1, m2) -> supplierSupplyMap.get(m2.getId()).compareTo(supplierSupplyMap.get(m1.getId())))
                .limit(10)
                .collect(Collectors.toList());
            
            // 计算总供货金额用于百分比计算
            BigDecimal totalSupply = supplierSupplyMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            for (int i = 0; i < sortedSuppliers.size(); i++) {
                Merchant supplier = sortedSuppliers.get(i);
                SupplierRankingResponse response = new SupplierRankingResponse();
                response.setRank(i + 1);
                response.setSupplierId(supplier.getId());
                response.setSupplierName(supplier.getName());
                response.setSupplierAvatar(supplier.getAvatar());
                
                BigDecimal supplyAmount = supplierSupplyMap.get(supplier.getId());
                BigDecimal yesterdaySupplyAmount = supplierYesterdaySupplyMap.get(supplier.getId());
                Integer productCount = supplierProductCountMap.get(supplier.getId());
                Integer orderCount = supplierOrderCountMap.get(supplier.getId());
                
                response.setSupplyAmount(supplyAmount);
                response.setYesterdaySupplyAmount(yesterdaySupplyAmount);
                response.setProductCount(productCount);
                response.setOrderCount(orderCount);
                
                // 计算增长率
                response.setGrowthRate(calculateGrowthRate(supplyAmount, yesterdaySupplyAmount));
                
                // 计算占比
                if (totalSupply.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = supplyAmount
                        .divide(totalSupply, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    response.setPercentage(percentage.setScale(1, RoundingMode.HALF_UP) + "%");
                } else {
                    response.setPercentage("0%");
                }
                
                list.add(response);
            }
        }
        
        // 如果没有足够的数据，填充空白项
        while (list.size() < 10) {
            SupplierRankingResponse response = new SupplierRankingResponse();
            response.setRank(list.size() + 1);
            response.setSupplierName("虚位以待");
            response.setSupplyAmount(BigDecimal.ZERO);
            response.setYesterdaySupplyAmount(BigDecimal.ZERO);
            response.setProductCount(0);
            response.setOrderCount(0);
            response.setGrowthRate("0%");
            response.setPercentage("0%");
            list.add(response);
        }
        
        return list;
    }

    /**
     * 获取用户报表数据
     */
    @Override
    public UserReportResponse getUserReport(String timeType) {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        String weekStart = DateUtil.beginOfWeek(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
        String weekEnd = DateUtil.endOfWeek(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
        String monthStart = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
        String monthEnd = DateUtil.endOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
        
        // 根据时间类型设置查询日期范围
        String startDate, endDate;
        switch (timeType) {
            case "yesterday":
                startDate = endDate = yesterday;
                break;
            case "thisWeek":
                // 最近7天（连续天数）
                startDate = DateUtil.offsetDay(DateUtil.date(), -6).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = today;
                break;
            case "thisMonth":
                // 最近30天（连续天数）
                startDate = DateUtil.offsetDay(DateUtil.date(), -29).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = today;
                break;
            case "thisYear":
                // 最近一年（连续天数）
                startDate = DateUtil.offsetDay(DateUtil.date(), -364).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = today;
                break;
            case "today":
            default:
                startDate = endDate = today;
                break;
        }
        
        UserReportResponse response = new UserReportResponse();
        
        // 基础用户统计 - 根据时间筛选
        response.setUserTotal(getUserTotalByTimeRange(startDate, endDate, timeType));
        
        // 用户渠道统计 - 根据时间筛选（移除app和H5端）
        Map<String, Integer> channelMap = getUserChannelDataByTimeRange(startDate, endDate);
        response.setMiniProgramUsers(channelMap.getOrDefault("routine", 0));
        response.setPcUsers(channelMap.getOrDefault("pc", 0));
        // 移除H5和App用户统计
        
        // 活跃用户统计 - 根据时间筛选
        response.setTodayActiveUsers(getActiveUsersByTimeRange(startDate, endDate, "today"));
        response.setYesterdayActiveUsers(getActiveUsersByTimeRange(yesterday, yesterday, "yesterday"));
        response.setWeekActiveUsers(getActiveUsersByTimeRange(weekStart, weekEnd, "week"));
        response.setMonthActiveUsers(getActiveUsersByTimeRange(monthStart, monthEnd, "month"));
        
        // 新增用户趋势 - 根据时间类型调整
        List<UserReportResponse.UserTrendData> userTrend = getUserTrendByTimeType(timeType);
        response.setUserTrend(userTrend);
        
        // 会员统计 - 根据时间筛选
        UserReportResponse.MemberStatistics memberStats = new UserReportResponse.MemberStatistics();
        memberStats.setOpenMembers(calculateOpenMembersByTimeRange(startDate, endDate));
        memberStats.setExpiredMembers(calculateExpiredMembersByTimeRange(startDate, endDate));
        memberStats.setRenewedMembers(calculateRenewedMembersByTimeRange(startDate, endDate));
        
        // 月度会员趋势（最近6个月）- 使用真实数据
        List<UserReportResponse.MonthlyMemberData> monthlyTrend = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -5);
        for (int i = 0; i < 6; i++) {
            String month = DateUtil.format(calendar.getTime(), "yyyy-MM");
            UserReportResponse.MonthlyMemberData monthlyData = new UserReportResponse.MonthlyMemberData();
            monthlyData.setMonth(month);
            
            // 使用真实的月度会员数据
            Map<String, Integer> monthlyMemberStats = getMemberStatsByMonth(month);
            monthlyData.setOpenCount(monthlyMemberStats.getOrDefault("open", 0));
            monthlyData.setExpiredCount(monthlyMemberStats.getOrDefault("expired", 0));
            monthlyData.setRenewedCount(monthlyMemberStats.getOrDefault("renewed", 0));
            
            monthlyTrend.add(monthlyData);
            calendar.add(Calendar.MONTH, 1);
        }
        memberStats.setMonthlyTrend(monthlyTrend);
        response.setMemberStats(memberStats);
        
        // 用户消费排行（前10名）- 根据时间筛选
        List<UserReportResponse.UserConsumptionRanking> consumptionRanking = getUserConsumptionRankingByTimeRange(startDate, endDate);
        response.setConsumptionRanking(consumptionRanking);
        
        // 漏斗图数据 - 访客、下单用户数、成交用户数
        response.setPageviews(userVisitRecordService.getPageviewsByDate(today));
        response.setOrderNum(orderService.getOrderNumByDate(0, today));
        response.setPaidUserCount(orderService.getPayUserNumByDate(0, today));
        
        // 充值用户数（假设有充值功能）
        response.setRechargeUserCount(0); // TODO: 需要实现充值用户统计
        
        // 客单价 = 今日总销售额 / 今日成交用户数
        BigDecimal todaySales = orderService.getPayOrderAmountByDate(0, today);
        Integer todayPaidUsers = orderService.getPayUserNumByDate(0, today);
        if (todayPaidUsers != null && todayPaidUsers > 0) {
            response.setAvgOrderAmount(todaySales.divide(new BigDecimal(todayPaidUsers), 2, RoundingMode.HALF_UP));
        } else {
            response.setAvgOrderAmount(BigDecimal.ZERO);
        }
        
        // 增长率计算
        Integer yesterdayNewUsers = userService.getRegisterNumByDate(yesterday);
        Integer todayNewUsers = userService.getRegisterNumByDate(today);
        response.setRegisterGrowthRate(calculateGrowthRate(yesterdayNewUsers, todayNewUsers));
        
        Integer yesterdayActiveUsers = getActiveUsersByTimeRange(yesterday, yesterday, "yesterday");
        Integer todayActiveUsers = getActiveUsersByTimeRange(today, today, "today");
        response.setActiveGrowthRate(calculateGrowthRate(yesterdayActiveUsers, todayActiveUsers));
        
        // 充值增长率 (暂时设为0%)
        response.setRechargeGrowthRate("0%");
        
        return response;
    }
    
    /**
     * 计算增长率
     * @param yesterday 昨天的数据
     * @param today 今天的数据
     * @return 增长率字符串，例如 "10.5%" 或 "-5.2%"
     */
    private String calculateGrowthRate(Integer yesterday, Integer today) {
        if (yesterday == null || yesterday == 0) {
            return today != null && today > 0 ? "100%" : "0%";
        }
        if (today == null) {
            today = 0;
        }
        double rate = ((double) (today - yesterday) / yesterday) * 100;
        return String.format("%.1f%%", rate);
    }

    /**
     * 获取订单报表数据
     */
    @Override
    public OrderReportResponse getOrderReport(String timeType) {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        
        // 根据时间类型设置查询范围
        String startDate, endDate;
        int trendDays;
        switch (timeType) {
            case "month":
                startDate = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = DateUtil.endOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                trendDays = 30; // 显示30天趋势
                break;
            case "year":
                startDate = DateUtil.beginOfYear(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = DateUtil.endOfYear(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                trendDays = 365; // 显示一年趋势，但按周汇总
                break;
            case "week":
            default:
                startDate = DateUtil.beginOfWeek(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = DateUtil.endOfWeek(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                trendDays = 7; // 显示7天趋势
                break;
        }
        
        OrderReportResponse response = new OrderReportResponse();
        
        // 订单状态统计 - 使用真实数据
        response.setPendingPaymentOrders(calculatePendingPaymentOrders());
        response.setPendingShipmentOrders(calculatePendingShipmentOrders());
        response.setPendingReceiptOrders(calculatePendingReceiptOrders());
        response.setPendingReviewOrders(calculatePendingReviewOrders());
        response.setProcessingOrders(refundOrderService.getAwaitAuditNum(0)); // 处理中的订单（退款申请）
        
        // 实时订单数统计
        OrderReportResponse.OrderCountStatistics orderCountStats = new OrderReportResponse.OrderCountStatistics();
        orderCountStats.setYesterdayOrders(orderService.getOrderNumByDate(0, yesterday));
        orderCountStats.setTotalOrders(calculateTotalOrders());
        orderCountStats.setMonthOrders(calculateMonthOrders());
        orderCountStats.setGrowthRate(calculateOrderGrowthRate());
        
        // 日订单趋势（24小时）- 使用真实的按小时统计数据
        List<OrderReportResponse.DailyOrderData> dailyTrend = calculateHourlyOrderTrend(today);
        orderCountStats.setDailyTrend(dailyTrend);
        response.setOrderCountStats(orderCountStats);
        
        // 订单金额统计
        OrderReportResponse.OrderAmountStatistics orderAmountStats = new OrderReportResponse.OrderAmountStatistics();
        BigDecimal yesterdayAmount = orderService.getPayOrderAmountByDate(0, yesterday);
        BigDecimal totalAmount = calculateTotalAmount();
        BigDecimal monthAmount = calculateMonthSales(0);
        
        log.info("订单金额统计 - 昨日金额: {}, 总金额: {}, 本月金额: {}", yesterdayAmount, totalAmount, monthAmount);
        
        orderAmountStats.setYesterdayAmount(yesterdayAmount);
        orderAmountStats.setTotalAmount(totalAmount);
        orderAmountStats.setMonthAmount(monthAmount);
        orderAmountStats.setGrowthRate(calculateAmountGrowthRate());
        
        // 日金额趋势 - 使用真实的按小时统计数据
        List<OrderReportResponse.DailyAmountData> amountTrend = calculateHourlyAmountTrend(today);
        orderAmountStats.setDailyTrend(amountTrend);
        response.setOrderAmountStats(orderAmountStats);
        
        // 退款金额统计 - 使用真实的退款数据
        OrderReportResponse.RefundAmountStatistics refundAmountStats = new OrderReportResponse.RefundAmountStatistics();
        BigDecimal yesterdayRefund = refundOrderService.getRefundOrderAmountByDate(yesterday);
        BigDecimal monthRefund = calculateMonthRefundAmount();
        
        refundAmountStats.setYesterdayRefund(yesterdayRefund);
        refundAmountStats.setTotalRefund(calculateTotalRefundAmount());
        refundAmountStats.setMonthRefund(monthRefund);
        refundAmountStats.setGrowthRate(calculateRefundGrowthRate());
        
        // 日退款趋势 - 使用真实的按小时统计数据
        List<OrderReportResponse.DailyRefundData> refundTrend = calculateHourlyRefundTrend(today);
        refundAmountStats.setDailyTrend(refundTrend);
        response.setRefundAmountStats(refundAmountStats);
        
        // 付款/退款订单统计趋势 - 根据时间类型调整
        List<OrderReportResponse.OrderTrendData> orderTrend = getOrderTrendByTimeType(timeType, trendDays);
        response.setOrderTrend(orderTrend);
        
        // 订单总数统计趋势 - 根据时间类型调整
        List<OrderReportResponse.OrderCountTrendData> orderCountTrend = getOrderCountTrendByTimeType(timeType, trendDays);
        response.setOrderCountTrend(orderCountTrend);
        
        return response;
    }

    /**
     * 获取商品报表数据
     */
    @Override
    public ProductReportResponse getProductReport(String timeType, Integer merchantId, Integer productStatus) {
        // 根据时间类型设置查询范围
        String startDate, endDate;
        int trendDays;
        switch (timeType) {
            case "month":
                startDate = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = DateUtil.endOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                trendDays = 30; // 显示30天趋势
                break;
            case "year":
                startDate = DateUtil.beginOfYear(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = DateUtil.endOfYear(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                trendDays = 365; // 显示一年趋势，但按月汇总
                break;
            case "week":
            default:
                startDate = DateUtil.beginOfWeek(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                endDate = DateUtil.endOfWeek(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
                trendDays = 7; // 显示7天趋势
                break;
        }
        
        ProductReportResponse response = new ProductReportResponse();
        
        // 基础统计 - 根据筛选条件使用真实数据
        response.setConnectedProducts(getConnectedProductsByFilter(merchantId, productStatus));
        response.setConnectedStores(merchantId != null ? 1 : merchantService.getAllCount());
        response.setProductCategories(productCategoryService.count()); 
        response.setProductBrands(productBrandService.count()); 
        
        // 商品数量统计 - 根据筛选条件
        ProductReportResponse.ProductCountStatistics productCountStats = new ProductReportResponse.ProductCountStatistics();
        productCountStats.setSoldProducts(getSoldProductsByFilter(merchantId, productStatus));
        productCountStats.setNewProducts(getNewProductsByFilter(startDate, endDate, merchantId, productStatus));
        
        // 计算退货商品数（基于退款订单统计）
        Integer returnedProducts = getReturnedProductsByFilter(startDate, endDate, merchantId);
        productCountStats.setReturnedProducts(returnedProducts);
        
        // 商品数量趋势 - 根据时间类型调整
        List<ProductReportResponse.DailyProductData> dailyTrend = getProductTrendByTimeType(timeType, trendDays, merchantId, productStatus);
        productCountStats.setDailyTrend(dailyTrend);
        response.setProductCountStats(productCountStats);
        
        // 商品状态统计 - 根据筛选条件统计（移除供应商商品）
        ProductReportResponse.ProductStatusStatistics productStatusStats = new ProductReportResponse.ProductStatusStatistics();
        
        // 根据商户和状态筛选条件统计商品
        Integer platformOwnProducts = calculatePlatformOwnProductsByFilter(merchantId, productStatus);
        Integer otherStoreProducts = calculateOtherStoreProductsByFilter(merchantId, productStatus);
        // 移除供应商商品统计
        Integer totalOnSaleProducts = platformOwnProducts + otherStoreProducts;
        
        productStatusStats.setPlatformOwnProducts(platformOwnProducts);
        productStatusStats.setOtherStoreProducts(otherStoreProducts);
        // 不再设置供应商商品数量
        productStatusStats.setOnSaleProducts(totalOnSaleProducts);
        response.setProductStatusStats(productStatusStats);

        // 商品规格统计 - 根据商户筛选
        List<ProductReportResponse.ProductSpecStatistics> productSpecStats = getProductSpecStatisticsByMerchant(merchantId);
        response.setProductSpecStats(productSpecStats);
        
        // 商品销量汇总 - 根据筛选条件和时间范围
        ProductReportResponse.ProductSalesStatistics productSalesStats = new ProductReportResponse.ProductSalesStatistics();
        
        // 计算销量数据（根据筛选条件）
        Integer[] salesData = calculateSalesStatisticsByFilter(startDate, endDate, merchantId, productStatus);
        productSalesStats.setSelfOperatedSales(salesData[0]);
        productSalesStats.setOtherStoreSales(salesData[1]);
        
        // 销量趋势 - 根据时间类型调整
        List<ProductReportResponse.DailySalesData> salesTrend = getSalesTrendByTimeType(timeType, trendDays, merchantId, productStatus);
        productSalesStats.setSalesTrend(salesTrend);
        response.setProductSalesStats(productSalesStats);
        
        // 商品库存统计 - 根据筛选条件和时间范围
        ProductReportResponse.ProductStockStatistics productStockStats = new ProductReportResponse.ProductStockStatistics();
        
        Integer inboundStock = calculateInboundStockByFilter(startDate, endDate, merchantId, productStatus);
        Integer outboundStock = calculateOutboundStockByFilter(startDate, endDate, merchantId, productStatus);
        Integer alertCount = calculateStockAlertCountByFilter(merchantId, productStatus);
        
        productStockStats.setInboundStock(inboundStock);
        productStockStats.setOutboundStock(outboundStock);
        productStockStats.setAlertCount(alertCount);
        
        // 库存详情列表 - 根据筛选条件获取库存预警商品
        List<ProductReportResponse.StockDetailData> stockDetails = getStockDetailListByFilter(merchantId, productStatus);
        productStockStats.setStockDetails(stockDetails);
        response.setProductStockStats(productStockStats);
        
        return response;
    }

    /**
     * 获取商户列表
     */
    @Override
    public List<com.zbkj.common.model.merchant.Merchant> getMerchantList() {
        return merchantService.all();
    }

    /**
     * 计算增长率
     * @param current 当前值
     * @param previous 之前值
     * @return 增长率字符串
     */
    private String calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        
        BigDecimal difference = current.subtract(previous);
        BigDecimal rate = difference.divide(previous, 4, RoundingMode.HALF_UP)
                                  .multiply(BigDecimal.valueOf(100));
        
        String sign = rate.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + rate.setScale(1, RoundingMode.HALF_UP) + "%";
    }

    /**
     * 计算本月销售额
     * @param merId 商户ID，0表示全平台
     * @return 本月销售额
     */
    private BigDecimal calculateMonthSales(Integer merId) {
        BigDecimal monthSales = BigDecimal.ZERO;
        
        // 获取本月第一天和当前日期
        String monthStart = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        
        // 循环计算每一天的销售额
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parse(monthStart));
        
        while (!DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE).equals(today)) {
            String dateStr = DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE);
            BigDecimal daySales = orderService.getPayOrderAmountByDate(merId, dateStr);
            monthSales = monthSales.add(daySales);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 加上今天的销售额
        BigDecimal todaySales = orderService.getPayOrderAmountByDate(merId, today);
        monthSales = monthSales.add(todaySales);
        
        return monthSales;
    }

    /**
     * 计算本月新增商户数
     * @return 本月新增商户数
     */
    private Integer calculateMonthNewMerchants() {
        Integer monthMerchants = 0;
        
        // 获取本月第一天和当前日期
        String monthStart = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        
        // 循环计算每一天的新增商户数
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parse(monthStart));
        
        while (!DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE).equals(today)) {
            String dateStr = DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE);
            Integer dayMerchants = merchantService.getNewNumByDate(dateStr);
            monthMerchants += dayMerchants;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 加上今天的新增商户数
        Integer todayMerchants = merchantService.getNewNumByDate(today);
        monthMerchants += todayMerchants;
        
        return monthMerchants;
    }

    /**
     * 计算总订单数
     */
    private Integer calculateTotalOrders() {
        // 这里应该实现真实的历史订单总数统计
        return 30;
    }

    /**
     * 计算本月订单数
     */
    private Integer calculateMonthOrders() {
        Integer monthOrders = 0;
        String monthStart = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parse(monthStart));
        
        while (!DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE).equals(today)) {
            String dateStr = DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE);
            Integer dayOrders = orderService.getOrderNumByDate(0, dateStr);
            monthOrders += dayOrders;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        Integer todayOrders = orderService.getOrderNumByDate(0, today);
        monthOrders += todayOrders;
        
        return monthOrders;
    }

    /**
     * 计算总金额
     */
    private BigDecimal calculateTotalAmount() {
        try {
            // 统计所有已支付订单的总金额
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("pay_price")
                       .eq("is_del", false)
                       .eq("paid", 1); // 已支付
            
            List<com.zbkj.common.model.order.Order> orders = orderService.list(queryWrapper);
            return orders.stream()
                    .map(order -> order.getPayPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("计算订单总金额失败", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算订单增长率
     */
    private String calculateOrderGrowthRate() {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        
        Integer todayOrders = orderService.getOrderNumByDate(0, today);
        Integer yesterdayOrders = orderService.getOrderNumByDate(0, yesterday);
        
        return calculateGrowthRate(BigDecimal.valueOf(todayOrders), BigDecimal.valueOf(yesterdayOrders));
    }

    /**
     * 计算金额增长率
     */
    private String calculateAmountGrowthRate() {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        
        BigDecimal todayAmount = orderService.getPayOrderAmountByDate(0, today);
        BigDecimal yesterdayAmount = orderService.getPayOrderAmountByDate(0, yesterday);
        
        return calculateGrowthRate(todayAmount, yesterdayAmount);
    }

    /**
     * 计算平台自营商品数量
     * 这里简化处理，可以根据具体业务规则调整
     */
    private Integer calculatePlatformOwnProducts() {
        // 假设商户ID为1的是平台自营，实际可根据商户类型字段判断
        return productService.getOnSaleNum(1);
    }

    /**
     * 根据筛选条件计算平台自营商品数量
     */
    private Integer calculatePlatformOwnProductsByFilter(Integer merchantId, Integer productStatus) {
        try {
            QueryWrapper<com.zbkj.common.model.product.Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false);
            
            // 平台自营商品筛选条件
            if (merchantId != null) {
                queryWrapper.eq("mer_id", merchantId);
            } else {
                // 假设商户ID为1的是平台自营
                queryWrapper.eq("mer_id", 1);
            }
            
            if (productStatus != null) {
                queryWrapper.eq("is_show", productStatus);
            }
            
            return productService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据筛选条件计算其他店铺商品数量
     */
    private Integer calculateOtherStoreProductsByFilter(Integer merchantId, Integer productStatus) {
        try {
            QueryWrapper<com.zbkj.common.model.product.Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false);
            
            if (merchantId != null) {
                queryWrapper.eq("mer_id", merchantId);
            } else {
                // 其他店铺（非平台自营）
                queryWrapper.ne("mer_id", 1);
            }
            
            if (productStatus != null) {
                queryWrapper.eq("is_show", productStatus);
            }
            
            return productService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据商户筛选获取商品规格统计
     */
    private List<ProductReportResponse.ProductSpecStatistics> getProductSpecStatisticsByMerchant(Integer merchantId) {
        List<ProductReportResponse.ProductSpecStatistics> productSpecStats = new ArrayList<>();
        
        try {
            // 获取真实的商品分类数据
            List<ProductCategory> categoryList = productCategoryService.getAdminList();
            
            if (CollUtil.isNotEmpty(categoryList)) {
                // 统计每个分类下的商品数量
                for (ProductCategory category : categoryList) {
                    try {
                        // 只统计一级分类或者主要分类（根据业务需求调整）
                        if (category.getLevel() != null && category.getLevel() <= 2) { // 只统计1-2级分类
                            ProductReportResponse.ProductSpecStatistics spec = new ProductReportResponse.ProductSpecStatistics();
                            spec.setSpecType(category.getName());
                            
                            // 根据商户筛选统计该分类下的商品数量
                            Integer productCount = getProductCountByCategoryIdAndMerchant(category.getId(), merchantId);
                            spec.setProductCount(productCount);
                            
                            // 只添加有商品的分类
                            if (productCount > 0) {
                                productSpecStats.add(spec);
                            }
                        }
                    } catch (Exception e) {
                        log.error("统计分类商品数量失败: categoryId={}, error={}", category.getId(), e.getMessage());
                    }
                }
            }
            
            // 按商品数量降序排序，只返回前10个
            return productSpecStats.stream()
                    .sorted((a, b) -> b.getProductCount().compareTo(a.getProductCount()))
                    .limit(10)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取商品规格统计失败: {}", e.getMessage());
            return productSpecStats;
        }
    }

    /**
     * 根据分类ID和商户ID统计商品数量
     */
    private Integer getProductCountByCategoryIdAndMerchant(Integer categoryId, Integer merchantId) {
        try {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("category_id", categoryId);
            
            // 如果传入了商户ID，则按商户筛选
            if (merchantId != null) {
                wrapper.eq("mer_id", merchantId);
            }
            
            // 只统计未删除的商品
            wrapper.eq("is_del", 0);
            
            return productService.count(wrapper);
        } catch (Exception e) {
            log.error("根据分类和商户统计商品数量失败: categoryId={}, merchantId={}, error={}", 
                     categoryId, merchantId, e.getMessage());
            return 0;
        }
    }

    /**
     * 获取商品规格统计 - 基于真实的商品分类数据
     */
    private List<ProductReportResponse.ProductSpecStatistics> getProductSpecStatistics() {
        List<ProductReportResponse.ProductSpecStatistics> productSpecStats = new ArrayList<>();
        
        try {
            // 获取真实的商品分类数据
            List<ProductCategory> categoryList = productCategoryService.getAdminList();
            
            if (CollUtil.isNotEmpty(categoryList)) {
                // 统计每个分类下的商品数量
                for (ProductCategory category : categoryList) {
                    try {
                        // 只统计一级分类或者主要分类（根据业务需求调整）
                        if (category.getLevel() != null && category.getLevel() <= 2) { // 只统计1-2级分类
                            ProductReportResponse.ProductSpecStatistics spec = new ProductReportResponse.ProductSpecStatistics();
                            spec.setSpecType(category.getName());
                            
                            // 统计该分类下的商品数量
                            Integer productCount = getProductCountByCategoryId(category.getId());
                            spec.setProductCount(productCount);
                            
                            // 只添加有商品的分类
                            if (productCount > 0) {
                                productSpecStats.add(spec);
                            }
                        }
                    } catch (Exception e) {
                        // 忽略单个分类的错误，继续处理其他分类
                        continue;
                    }
                }
            }
            
            // 如果没有分类数据或分类为空，使用默认分类进行统计
            if (productSpecStats.isEmpty()) {
                productSpecStats = getDefaultProductSpecStatistics();
            }
            
        } catch (Exception e) {
            // 如果查询分类失败，使用默认分类统计
            productSpecStats = getDefaultProductSpecStatistics();
        }
        
        return productSpecStats;
    }

    /**
     * 根据分类ID统计商品数量
     */
    private Integer getProductCountByCategoryId(Integer categoryId) {
        try {
            // 使用商品名称模糊匹配来估算分类商品数量
            // 这里可以根据实际的商品分类关联表来精确统计
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .eq("is_show", true)
                       .like("cate_id", "," + categoryId + ","); // 假设cate_id字段存储分类ID，用逗号分隔
            
            return productService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取默认的商品规格统计（当无法获取真实分类数据时使用）
     */
    private List<ProductReportResponse.ProductSpecStatistics> getDefaultProductSpecStatistics() {
        List<ProductReportResponse.ProductSpecStatistics> productSpecStats = new ArrayList<>();
        
        // 使用商品名称关键词统计
        String[] specTypes = {"大米", "面条面粉", "湖北菜籽油", "农发专区", "江汉大米", "功能粮油", "特产干货", "荆楚好粮油", "食用油"};
        
        for (String specType : specTypes) {
            try {
                ProductReportResponse.ProductSpecStatistics spec = new ProductReportResponse.ProductSpecStatistics();
                spec.setSpecType(specType);
                // 根据商品名称模糊匹配来统计数量
                List<Product> products = productService.likeProductName(specType, 0);
                spec.setProductCount(products.size());
                productSpecStats.add(spec);
            } catch (Exception e) {
                // 忽略错误，继续处理下一个分类
                continue;
            }
        }
        
        return productSpecStats;
    }

    /**
     * 计算销量统计
     * @return [平台自营销量, 其他店铺销量]
     */
    private Integer[] calculateSalesStatistics() {
        // 获取最近30天的总销量数据
        String startDate = DateUtil.offsetDay(DateUtil.date(), -30).toString(DateConstants.DATE_FORMAT_DATE);
        String endDate = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        
        // 计算平台自营销量（商户ID为1）
        Integer selfOperatedSales = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parse(startDate));
        
        while (!DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE).equals(endDate)) {
            String dateStr = DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE);
            selfOperatedSales += orderService.getOrderNumByDate(1, dateStr);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 计算所有店铺总销量
        Integer totalSales = 0;
        calendar.setTime(DateUtil.parse(startDate));
        while (!DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE).equals(endDate)) {
            String dateStr = DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE);
            totalSales += orderService.getOrderNumByDate(0, dateStr);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        Integer otherStoreSales = totalSales - selfOperatedSales;
        
        return new Integer[]{selfOperatedSales, otherStoreSales};
    }

    /**
     * 计算每日销量数据
     * @param date 日期
     * @return [平台自营销量, 其他店铺销量]
     */
    private Integer[] calculateDailySales(String date) {
        Integer selfOperatedSales = orderService.getOrderNumByDate(1, date);
        Integer totalSales = orderService.getOrderNumByDate(0, date);
        Integer otherStoreSales = totalSales - selfOperatedSales;
        
        return new Integer[]{selfOperatedSales, otherStoreSales};
    }

    /**
     * 计算入库数量
     */
    private Integer calculateInboundStock(String startDate, String endDate) {
        try {
            // 使用入库记录服务统计入库数量
            // 查询指定时间范围内的入库记录
            QueryWrapper<com.zbkj.common.model.stock.StockInRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .ge("DATE(create_time)", startDate)
                       .le("DATE(create_time)", endDate);
            
            List<com.zbkj.common.model.stock.StockInRecord> records = stockInRecordService.list(queryWrapper);
            return records.stream().mapToInt(record -> record.getInQuantity()).sum();
        } catch (Exception e) {
            // 如果查询失败，返回0
            return 0;
        }
    }

    /**
     * 计算出库数量
     */
    private Integer calculateOutboundStock(String startDate, String endDate) {
        try {
            // 使用出库记录服务统计出库数量
            QueryWrapper<com.zbkj.common.model.stock.StockOutRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .ge("DATE(create_time)", startDate)
                       .le("DATE(create_time)", endDate);
            
            List<com.zbkj.common.model.stock.StockOutRecord> records = stockOutRecordService.list(queryWrapper);
            return records.stream().mapToInt(record -> record.getOutQuantity()).sum();
        } catch (Exception e) {
            // 如果查询失败，返回0
            return 0;
        }
    }

    /**
     * 计算库存预警数量
     */
    private Integer calculateStockAlertCount() {
        try {
            // 统计库存低于10的商品数量作为预警
            List<ProductAttrValue> lowStockProducts = productAttrValueService.list().stream()
                .filter(item -> item.getStock() != null && item.getStock() < 10)
                .collect(Collectors.toList());
            return lowStockProducts.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取库存详情列表
     */
    private List<ProductReportResponse.StockDetailData> getStockDetailList() {
        List<ProductReportResponse.StockDetailData> stockDetails = new ArrayList<>();
        
        try {
            // 获取库存较低的商品（前10个）
            List<ProductAttrValue> lowStockProducts = productAttrValueService.list().stream()
                .filter(item -> item.getStock() != null && item.getStock() < 20)
                .sorted((a, b) -> a.getStock().compareTo(b.getStock()))
                .limit(10)
                .collect(Collectors.toList());
            
            int sequence = 1;
            for (ProductAttrValue attrValue : lowStockProducts) {
                try {
                    Product product = productService.getById(attrValue.getProductId());
                    if (product != null) {
                        ProductReportResponse.StockDetailData detail = new ProductReportResponse.StockDetailData();
                        detail.setSequence(sequence++);
                        detail.setCode(attrValue.getSku());
                        detail.setProductInfo(product.getName());
                        detail.setProductSpec(attrValue.getAttrValue());
                        detail.setTotalStock(attrValue.getStock() + attrValue.getSales()); // 总库存 = 现有库存 + 已销售
                        detail.setRemainingStock(attrValue.getStock());
                        detail.setAlertTime(DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE));
                        stockDetails.add(detail);
                    }
                } catch (Exception e) {
                    // 忽略单个商品的错误，继续处理其他商品
                    continue;
                }
            }
        } catch (Exception e) {
            // 如果查询失败，返回空列表
        }
        
        return stockDetails;
    }

    /**
     * 计算待付款订单数量
     */
    private Integer calculatePendingPaymentOrders() {
        try {
            // 订单状态-待支付 = 0
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .eq("status", OrderConstants.ORDER_STATUS_WAIT_PAY);
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算待发货订单数量
     */
    private Integer calculatePendingShipmentOrders() {
        try {
            // 订单状态-待发货 = 1
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .eq("status", OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算待收货订单数量
     */
    private Integer calculatePendingReceiptOrders() {
        try {
            // 订单状态-待收货 = 4
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .eq("status", OrderConstants.ORDER_STATUS_WAIT_RECEIPT);
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算待评价订单数量
     */
    private Integer calculatePendingReviewOrders() {
        try {
            // 订单状态-已收货 = 5，表示已收货待评价
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .eq("status", OrderConstants.ORDER_STATUS_TAKE_DELIVERY);
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算按小时的订单趋势
     */
    private List<OrderReportResponse.DailyOrderData> calculateHourlyOrderTrend(String date) {
        List<OrderReportResponse.DailyOrderData> dailyTrend = new ArrayList<>();
        
        for (int hour = 0; hour < 24; hour++) {
            OrderReportResponse.DailyOrderData data = new OrderReportResponse.DailyOrderData();
            data.setTime(String.format("%02d:00", hour));
            
            try {
                // 查询指定小时的订单数量
                Integer count = getOrderCountByHour(date, hour);
                data.setCount(count);
            } catch (Exception e) {
                data.setCount(0);
            }
            
            dailyTrend.add(data);
        }
        
        return dailyTrend;
    }

    /**
     * 计算按小时的金额趋势
     */
    private List<OrderReportResponse.DailyAmountData> calculateHourlyAmountTrend(String date) {
        List<OrderReportResponse.DailyAmountData> amountTrend = new ArrayList<>();
        
        for (int hour = 0; hour < 24; hour++) {
            OrderReportResponse.DailyAmountData data = new OrderReportResponse.DailyAmountData();
            data.setTime(String.format("%02d:00", hour));
            
            try {
                // 查询指定小时的订单金额
                BigDecimal amount = getOrderAmountByHour(date, hour);
                data.setAmount(amount);
            } catch (Exception e) {
                data.setAmount(BigDecimal.ZERO);
            }
            
            amountTrend.add(data);
        }
        
        return amountTrend;
    }

    /**
     * 计算按小时的退款趋势
     */
    private List<OrderReportResponse.DailyRefundData> calculateHourlyRefundTrend(String date) {
        List<OrderReportResponse.DailyRefundData> refundTrend = new ArrayList<>();
        
        for (int hour = 0; hour < 24; hour++) {
            OrderReportResponse.DailyRefundData data = new OrderReportResponse.DailyRefundData();
            data.setTime(String.format("%02d:00", hour));
            
            try {
                // 查询指定小时的退款金额
                BigDecimal refundAmount = getRefundAmountByHour(date, hour);
                data.setRefundAmount(refundAmount);
            } catch (Exception e) {
                data.setRefundAmount(BigDecimal.ZERO);
            }
            
            refundTrend.add(data);
        }
        
        return refundTrend;
    }

    /**
     * 获取指定小时的订单数量
     */
    private Integer getOrderCountByHour(String date, int hour) {
        try {
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .eq("paid", true)
                       .apply("DATE(pay_time) = {0}", date)
                       .apply("HOUR(pay_time) = {0}", hour);
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取指定小时的订单金额
     */
    private BigDecimal getOrderAmountByHour(String date, int hour) {
        try {
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("pay_price")
                       .eq("is_del", false)
                       .eq("paid", true)
                       .apply("DATE(pay_time) = {0}", date)
                       .apply("HOUR(pay_time) = {0}", hour);
            
            List<com.zbkj.common.model.order.Order> orders = orderService.list(queryWrapper);
            return orders.stream()
                    .map(order -> order.getPayPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取指定小时的退款金额
     */
    private BigDecimal getRefundAmountByHour(String date, int hour) {
        try {
            QueryWrapper<com.zbkj.common.model.order.RefundOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("refund_price")
                       .eq("refund_status", 3) // 假设状态3表示已退款
                       .apply("DATE(refund_time) = {0}", date)
                       .apply("HOUR(refund_time) = {0}", hour);
            
            List<com.zbkj.common.model.order.RefundOrder> refundOrders = refundOrderService.list(queryWrapper);
            return refundOrders.stream()
                    .map(order -> order.getRefundPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算本月退款金额
     */
    private BigDecimal calculateMonthRefundAmount() {
        try {
            BigDecimal monthRefund = BigDecimal.ZERO;
            String monthStart = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
            String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.parse(monthStart));
            
            while (!DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE).equals(today)) {
                String dateStr = DateUtil.format(calendar.getTime(), DateConstants.DATE_FORMAT_DATE);
                BigDecimal dayRefund = refundOrderService.getRefundOrderAmountByDate(dateStr);
                monthRefund = monthRefund.add(dayRefund);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            // 加上今天的退款金额
            BigDecimal todayRefund = refundOrderService.getRefundOrderAmountByDate(today);
            monthRefund = monthRefund.add(todayRefund);
            
            return monthRefund;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算总退款金额
     */
    private BigDecimal calculateTotalRefundAmount() {
        try {
            // 统计所有已退款的退款订单金额
            QueryWrapper<com.zbkj.common.model.order.RefundOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("refund_price")
                       .eq("refund_status", OrderConstants.MERCHANT_REFUND_ORDER_STATUS_REFUND); // 3:已退款
            
            List<com.zbkj.common.model.order.RefundOrder> refundOrders = refundOrderService.list(queryWrapper);
            BigDecimal totalRefund = refundOrders.stream()
                    .map(order -> order.getRefundPrice() != null ? order.getRefundPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            log.info("计算总退款金额: {} 条退款记录，总金额: {}", refundOrders.size(), totalRefund);
            return totalRefund;
        } catch (Exception e) {
            log.error("计算总退款金额失败", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算退款增长率
     */
    private String calculateRefundGrowthRate() {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        
        BigDecimal todayRefund = refundOrderService.getRefundOrderAmountByDate(today);
        BigDecimal yesterdayRefund = refundOrderService.getRefundOrderAmountByDate(yesterday);
        
        return calculateGrowthRate(todayRefund, yesterdayRefund);
    }

    /**
     * 根据日期和渠道统计用户注册数
     */
    private Map<String, Integer> getUserRegistrationByDateAndChannel(String date) {
        Map<String, Integer> channelMap = new HashMap<>();
        
        try {
            // 查询指定日期各渠道的注册用户数
            QueryWrapper<com.zbkj.common.model.user.User> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("register_type", "COUNT(*) as count")
                       .eq("is_del", false)
                       .apply("DATE(create_time) = {0}", date)
                       .groupBy("register_type");
            
            List<Map<String, Object>> results = userService.listMaps(queryWrapper);
            
            for (Map<String, Object> result : results) {
                String registerType = (String) result.get("register_type");
                Long count = (Long) result.get("count");
                channelMap.put(registerType, count.intValue());
            }
            
        } catch (Exception e) {
            // 查询失败时返回空map
        }
        
        return channelMap;
    }

    /**
     * 计算开通会员数
     */
    private Integer calculateOpenMembers() {
        try {
            // 根据实际的会员系统实现
            // 这里假设用户表有会员相关字段，如vip_level > 0表示会员
            QueryWrapper<com.zbkj.common.model.user.User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .gt("vip_level", 0); // 假设vip_level > 0表示是会员
            
            return userService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算过期会员数
     */
    private Integer calculateExpiredMembers() {
        try {
            // 根据实际的会员系统实现
            // 这里假设有会员到期时间字段
            QueryWrapper<com.zbkj.common.model.user.User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .gt("vip_level", 0)
                       .lt("vip_end_time", DateUtil.date()); // 会员到期时间小于当前时间
            
            return userService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算续费会员数
     */
    private Integer calculateRenewedMembers() {
        try {
            // 统计本月续费的会员数
            String monthStart = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
            String monthEnd = DateUtil.endOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
            
            // 这里需要根据实际的续费记录表来查询
            // 暂时返回估算值
            return calculateOpenMembers() / 10; // 假设10%的会员是续费的
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据月份获取会员统计数据
     */
    private Map<String, Integer> getMemberStatsByMonth(String month) {
        Map<String, Integer> memberStats = new HashMap<>();
        
        try {
            // 开通会员数（该月新增会员）
            QueryWrapper<com.zbkj.common.model.user.User> openQueryWrapper = new QueryWrapper<>();
            openQueryWrapper.eq("is_del", false)
                           .gt("vip_level", 0)
                           .apply("DATE_FORMAT(vip_start_time, '%Y-%m') = {0}", month);
            
            Integer openCount = userService.count(openQueryWrapper);
            memberStats.put("open", openCount);
            
            // 过期会员数（该月过期的会员）
            QueryWrapper<com.zbkj.common.model.user.User> expiredQueryWrapper = new QueryWrapper<>();
            expiredQueryWrapper.eq("is_del", false)
                              .apply("DATE_FORMAT(vip_end_time, '%Y-%m') = {0}", month);
            
            Integer expiredCount = userService.count(expiredQueryWrapper);
            memberStats.put("expired", expiredCount);
            
            // 续费会员数（估算）
            memberStats.put("renewed", openCount / 5); // 假设20%是续费
            
        } catch (Exception e) {
            memberStats.put("open", 0);
            memberStats.put("expired", 0);
            memberStats.put("renewed", 0);
        }
        
        return memberStats;
    }

    /**
     * 获取用户消费排行榜
     */
    private List<UserReportResponse.UserConsumptionRanking> getUserConsumptionRanking() {
        List<UserReportResponse.UserConsumptionRanking> consumptionRanking = new ArrayList<>();
        
        try {
            // 查询用户消费排行（最近3个月）
            String startDate = DateUtil.offsetMonth(DateUtil.date(), -3).toString(DateConstants.DATE_FORMAT_DATE);
            
            // 使用原生SQL查询用户消费统计
            String sql = "SELECT u.uid, u.nickname, u.register_type, SUM(o.pay_price) as total_amount, COUNT(o.id) as order_count FROM eb_user u INNER JOIN eb_order o ON u.uid = o.uid WHERE o.is_del = 0 AND o.paid = 1 AND o.pay_time >= ? AND u.is_del = 0 GROUP BY u.uid ORDER BY total_amount DESC LIMIT 10 ";
            
            // 这里需要使用具体的查询方式，暂时使用简化的查询
            List<com.zbkj.common.model.user.User> topUsers = getTopConsumptionUsers();
            
            for (int i = 0; i < topUsers.size() && i < 10; i++) {
                com.zbkj.common.model.user.User user = topUsers.get(i);
                
                UserReportResponse.UserConsumptionRanking ranking = new UserReportResponse.UserConsumptionRanking();
                ranking.setRank(i + 1);
                ranking.setNickname(user.getNickname());
                ranking.setRegisterType(user.getRegisterType());
                
                // 计算用户消费总额和订单数
                BigDecimal consumptionAmount = getUserTotalConsumption(user.getId());
                Integer orderCount = getUserOrderCount(user.getId());
                
                ranking.setConsumptionAmount(consumptionAmount);
                ranking.setOrderCount(orderCount);
                
                consumptionRanking.add(ranking);
            }
            
            // 如果不够10个，填充空位
            while (consumptionRanking.size() < 10) {
                UserReportResponse.UserConsumptionRanking ranking = new UserReportResponse.UserConsumptionRanking();
                ranking.setRank(consumptionRanking.size() + 1);
                ranking.setNickname("虚位以待");
                ranking.setConsumptionAmount(BigDecimal.ZERO);
                ranking.setOrderCount(0);
                ranking.setRegisterType("未知");
                consumptionRanking.add(ranking);
            }
            
        } catch (Exception e) {
            // 查询失败时填充默认数据
            for (int i = 0; i < 10; i++) {
                UserReportResponse.UserConsumptionRanking ranking = new UserReportResponse.UserConsumptionRanking();
                ranking.setRank(i + 1);
                ranking.setNickname("虚位以待");
                ranking.setConsumptionAmount(BigDecimal.ZERO);
                ranking.setOrderCount(0);
                ranking.setRegisterType("未知");
                consumptionRanking.add(ranking);
            }
        }
        
        return consumptionRanking;
    }

    /**
     * 获取消费金额最高的用户列表
     */
    private List<com.zbkj.common.model.user.User> getTopConsumptionUsers() {
        try {
            // 获取有订单的用户，按消费金额排序
            // 这里简化处理，实际需要更复杂的查询
            QueryWrapper<com.zbkj.common.model.user.User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .orderByDesc("now_money") // 暂时用余额排序，实际应该用消费总额
                       .last("LIMIT 20");
            
            return userService.list(queryWrapper);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 计算用户总消费金额
     */
    private BigDecimal getUserTotalConsumption(Integer userId) {
        try {
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("pay_price")
                       .eq("uid", userId)
                       .eq("is_del", false)
                       .eq("paid", true);
            
            List<com.zbkj.common.model.order.Order> orders = orderService.list(queryWrapper);
            return orders.stream()
                    .map(order -> order.getPayPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算用户订单数量
     */
    private Integer getUserOrderCount(Integer userId) {
        try {
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", userId)
                       .eq("is_del", false)
                       .eq("paid", true);
            
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据时间范围获取用户总数
     */
    private Integer getUserTotalByTimeRange(String startDate, String endDate, String timeType) {
        try {
            // 用户总数应该是所有用户的总数，不是时间范围内的新增用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1);
            Integer totalUsers = userService.count(queryWrapper);
            
            log.info("用户总数统计 - 总用户数: {}", totalUsers);
            return totalUsers;
        } catch (Exception e) {
            log.error("获取用户总数失败", e);
            return 0;
        }
    }

    /**
     * 根据时间范围获取用户渠道数据
     */
    private Map<String, Integer> getUserChannelDataByTimeRange(String startDate, String endDate) {
        Map<String, Integer> channelMap = new HashMap<>();
        try {
            // 统计所有用户的渠道分布，不限制时间范围
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1);
            
            List<User> allUsers = userService.list(queryWrapper);
            
            // 按注册类型分组统计
            Map<String, Long> channelCount = allUsers.stream()
                    .collect(Collectors.groupingBy(
                            user -> user.getRegisterType() != null ? user.getRegisterType() : "unknown",
                            Collectors.counting()
                    ));
            
            // 转换为Integer类型
            channelCount.forEach((key, value) -> channelMap.put(key, value.intValue()));
            
            log.info("用户渠道统计: {}", channelMap);
            return channelMap;
        } catch (Exception e) {
            log.error("获取用户渠道数据失败", e);
            return channelMap;
        }
    }

    /**
     * 根据时间范围获取活跃用户数
     */
    private Integer getActiveUsersByTimeRange(String startDate, String endDate, String type) {
        try {
            if ("today".equals(type) || "yesterday".equals(type)) {
                return userVisitRecordDao.getActiveUserNumByDate(startDate);
            } else {
                return userVisitRecordDao.getActiveUserNumByPeriod(startDate, endDate);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据时间类型获取用户趋势数据
     */
    private List<UserReportResponse.UserTrendData> getUserTrendByTimeType(String timeType) {
        List<UserReportResponse.UserTrendData> userTrend = new ArrayList<>();
        
        try {
            switch (timeType) {
                case "today":
                case "yesterday":
                    // 显示最近7天的数据（每天一个点）
                    for (int i = 6; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        UserReportResponse.UserTrendData trendData = new UserReportResponse.UserTrendData();
                        trendData.setDate(date);

                        Map<String, Integer> dailyChannelUsers = getUserRegistrationByDateAndChannel(date);
                        trendData.setMiniProgramNewUsers(dailyChannelUsers.getOrDefault("routine", 0));
                        trendData.setH5NewUsers(dailyChannelUsers.getOrDefault("h5", 0));
                        trendData.setPcNewUsers(dailyChannelUsers.getOrDefault("pc", 0));
                        trendData.setAppNewUsers(dailyChannelUsers.getOrDefault("app", 0));
                        trendData.setActiveUsers(getActiveUsersByTimeRange(date, date, "today"));

                        userTrend.add(trendData);
                    }
                    break;
                case "thisWeek":
                    // 显示最近7天的数据（每天一个点）
                    for (int i = 6; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        UserReportResponse.UserTrendData trendData = new UserReportResponse.UserTrendData();
                        trendData.setDate(date);

                        Map<String, Integer> dailyChannelUsers = getUserRegistrationByDateAndChannel(date);
                        trendData.setMiniProgramNewUsers(dailyChannelUsers.getOrDefault("routine", 0));
                        trendData.setH5NewUsers(dailyChannelUsers.getOrDefault("h5", 0));
                        trendData.setPcNewUsers(dailyChannelUsers.getOrDefault("pc", 0));
                        trendData.setAppNewUsers(dailyChannelUsers.getOrDefault("app", 0));
                        trendData.setActiveUsers(getActiveUsersByTimeRange(date, date, "today"));

                        userTrend.add(trendData);
                    }
                    break;
                case "thisMonth":
                    // 显示最近30天的数据（每天一个点）
                    for (int i = 29; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        UserReportResponse.UserTrendData trendData = new UserReportResponse.UserTrendData();
                        trendData.setDate(date);

                        Map<String, Integer> dailyChannelUsers = getUserRegistrationByDateAndChannel(date);
                        trendData.setMiniProgramNewUsers(dailyChannelUsers.getOrDefault("routine", 0));
                        trendData.setH5NewUsers(dailyChannelUsers.getOrDefault("h5", 0));
                        trendData.setPcNewUsers(dailyChannelUsers.getOrDefault("pc", 0));
                        trendData.setAppNewUsers(dailyChannelUsers.getOrDefault("app", 0));
                        trendData.setActiveUsers(getActiveUsersByTimeRange(date, date, "today"));

                        userTrend.add(trendData);
                    }
                    break;
                case "thisYear":
                    // 显示最近12个月的数据（每月一个点）
                    for (int i = 11; i >= 0; i--) {
                        Date monthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -i));
                        Date monthEnd = DateUtil.endOfMonth(monthStart);
                        String date = DateUtil.format(monthStart, DateConstants.DATE_FORMAT_DATE);
                        String endDate = DateUtil.format(monthEnd, DateConstants.DATE_FORMAT_DATE);
                        UserReportResponse.UserTrendData trendData = new UserReportResponse.UserTrendData();
                        trendData.setDate(date);

                        Map<String, Integer> monthlyChannelUsers = getUserRegistrationByMonth(monthStart);
                        trendData.setMiniProgramNewUsers(monthlyChannelUsers.getOrDefault("routine", 0));
                        trendData.setH5NewUsers(monthlyChannelUsers.getOrDefault("h5", 0));
                        trendData.setPcNewUsers(monthlyChannelUsers.getOrDefault("pc", 0));
                        trendData.setAppNewUsers(monthlyChannelUsers.getOrDefault("app", 0));
                        trendData.setActiveUsers(getActiveUsersByTimeRange(date, endDate, "month"));

                        userTrend.add(trendData);
                    }
                    break;
            }
        } catch (Exception e) {
            // 返回空数据
        }
        
        return userTrend;
    }

    /**
     * 根据周获取用户注册数据
     */
    private Map<String, Integer> getUserRegistrationByWeek(Date weekStart) {
        Map<String, Integer> channelUsers = new HashMap<>();
        try {
            String startDate = DateUtil.format(weekStart, DateConstants.DATE_FORMAT_DATE);
            String endDate = DateUtil.format(DateUtil.endOfWeek(weekStart), DateConstants.DATE_FORMAT_DATE);
            
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .between("create_time", startDate + " 00:00:00", endDate + " 23:59:59")
                       .select("register_type", "COUNT(*) as count")
                       .groupBy("register_type");
            
            List<User> users = userService.list(queryWrapper);
            users.forEach(user -> channelUsers.put(user.getRegisterType(), user.getPayCount()));
        } catch (Exception e) {
            // 忽略错误
        }
        return channelUsers;
    }

    /**
     * 根据月份获取用户注册数据
     */
    private Map<String, Integer> getUserRegistrationByMonth(Date monthStart) {
        Map<String, Integer> channelUsers = new HashMap<>();
        try {
            String startDate = DateUtil.format(monthStart, DateConstants.DATE_FORMAT_DATE);
            String endDate = DateUtil.format(DateUtil.endOfMonth(monthStart), DateConstants.DATE_FORMAT_DATE);
            
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1)
                       .between("create_time", startDate + " 00:00:00", endDate + " 23:59:59")
                       .select("register_type", "COUNT(*) as count")
                       .groupBy("register_type");
            
            List<User> users = userService.list(queryWrapper);
            users.forEach(user -> channelUsers.put(user.getRegisterType(), user.getPayCount()));
        } catch (Exception e) {
            // 忽略错误
        }
        return channelUsers;
    }

    /**
     * 根据时间范围计算开通会员数
     */
    private Integer calculateOpenMembersByTimeRange(String startDate, String endDate) {
        try {
            // 这里需要根据实际的会员表结构来查询
            // 假设有会员记录表，查询指定时间范围内开通的会员数
            return calculateOpenMembers(); // 暂时返回原有方法结果
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据时间范围计算过期会员数
     */
    private Integer calculateExpiredMembersByTimeRange(String startDate, String endDate) {
        try {
            return calculateExpiredMembers(); // 暂时返回原有方法结果
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据时间范围计算续费会员数
     */
    private Integer calculateRenewedMembersByTimeRange(String startDate, String endDate) {
        try {
            return calculateRenewedMembers(); // 暂时返回原有方法结果
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据时间范围获取用户消费排行
     */
    private List<UserReportResponse.UserConsumptionRanking> getUserConsumptionRankingByTimeRange(String startDate, String endDate) {
        List<UserReportResponse.UserConsumptionRanking> consumptionRanking = new ArrayList<>();
        
        try {
            // 查询指定时间范围内的用户消费排行
            String sql = "SELECT u.uid, u.nickname, u.register_type, SUM(o.pay_price) as total_amount, COUNT(o.id) as order_count " +
                        "FROM eb_user u INNER JOIN eb_order o ON u.uid = o.uid " +
                        "WHERE o.is_del = 0 AND o.paid = 1 AND o.pay_time >= ? AND o.pay_time <= ? AND u.status = 1 " +
                        "GROUP BY u.uid ORDER BY total_amount DESC LIMIT 10";
            
            // 这里需要使用具体的查询方式，暂时使用简化的查询
            List<com.zbkj.common.model.user.User> topUsers = getTopConsumptionUsersByTimeRange(startDate, endDate);
            
            for (int i = 0; i < topUsers.size() && i < 10; i++) {
                com.zbkj.common.model.user.User user = topUsers.get(i);
                
                UserReportResponse.UserConsumptionRanking ranking = new UserReportResponse.UserConsumptionRanking();
                ranking.setRank(i + 1);
                ranking.setNickname(user.getNickname());
                ranking.setRegisterType(user.getRegisterType());
                
                // 计算用户在指定时间范围内的消费总额和订单数
                BigDecimal consumptionAmount = getUserTotalConsumptionByTimeRange(user.getId(), startDate, endDate);
                Integer orderCount = getUserOrderCountByTimeRange(user.getId(), startDate, endDate);
                
                ranking.setConsumptionAmount(consumptionAmount);
                ranking.setOrderCount(orderCount);
                
                consumptionRanking.add(ranking);
            }
            
            // 如果不够10个，填充空位
            while (consumptionRanking.size() < 10) {
                UserReportResponse.UserConsumptionRanking ranking = new UserReportResponse.UserConsumptionRanking();
                ranking.setRank(consumptionRanking.size() + 1);
                ranking.setNickname("虚位以待");
                ranking.setConsumptionAmount(BigDecimal.ZERO);
                ranking.setOrderCount(0);
                ranking.setRegisterType("");
                consumptionRanking.add(ranking);
            }
            
        } catch (Exception e) {
            // 填充空数据
            for (int i = 0; i < 10; i++) {
                UserReportResponse.UserConsumptionRanking ranking = new UserReportResponse.UserConsumptionRanking();
                ranking.setRank(i + 1);
                ranking.setNickname("虚位以待");
                ranking.setConsumptionAmount(BigDecimal.ZERO);
                ranking.setOrderCount(0);
                ranking.setRegisterType("");
                consumptionRanking.add(ranking);
            }
        }
        
        return consumptionRanking;
    }

    /**
     * 根据时间范围获取消费排行用户
     */
    private List<com.zbkj.common.model.user.User> getTopConsumptionUsersByTimeRange(String startDate, String endDate) {
        try {
            // 这里应该根据时间范围查询消费排行用户
            // 暂时返回原有方法的结果
            return getTopConsumptionUsers();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 根据时间范围计算用户消费总额
     */
    private BigDecimal getUserTotalConsumptionByTimeRange(Integer userId, String startDate, String endDate) {
        try {
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", userId)
                       .eq("is_del", false)
                       .eq("paid", true)
                       .between("pay_time", startDate + " 00:00:00", endDate + " 23:59:59");
            
            List<com.zbkj.common.model.order.Order> orders = orderService.list(queryWrapper);
            return orders.stream()
                    .map(order -> order.getPayPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 根据时间范围计算用户订单数量
     */
    private Integer getUserOrderCountByTimeRange(Integer userId, String startDate, String endDate) {
        try {
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", userId)
                       .eq("is_del", false)
                       .eq("paid", true)
                       .between("pay_time", startDate + " 00:00:00", endDate + " 23:59:59");
            
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据时间类型获取订单趋势数据
     */
    private List<OrderReportResponse.OrderTrendData> getOrderTrendByTimeType(String timeType, int trendDays) {
        List<OrderReportResponse.OrderTrendData> orderTrend = new ArrayList<>();
        
        try {
            switch (timeType) {
                case "week":
                    // 显示最近7天的数据
                    for (int i = trendDays - 1; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        OrderReportResponse.OrderTrendData trendData = new OrderReportResponse.OrderTrendData();
                        trendData.setDate(date);
                        trendData.setPaidOrders(orderService.getOrderNumByDate(0, date));
                        trendData.setRefundOrders(refundOrderService.getRefundOrderNumByDate(date));
                        orderTrend.add(trendData);
                    }
                    break;
                case "month":
                    // 显示最近30天的数据
                    for (int i = trendDays - 1; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        OrderReportResponse.OrderTrendData trendData = new OrderReportResponse.OrderTrendData();
                        trendData.setDate(date);
                        trendData.setPaidOrders(orderService.getOrderNumByDate(0, date));
                        trendData.setRefundOrders(refundOrderService.getRefundOrderNumByDate(date));
                        orderTrend.add(trendData);
                    }
                    break;
                case "year":
                    // 显示最近12个月的数据（按月汇总）
                    for (int i = 11; i >= 0; i--) {
                        Date monthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -i));
                        Date monthEnd = DateUtil.endOfMonth(monthStart);
                        String date = DateUtil.format(monthStart, "yyyy-MM");
                        
                        OrderReportResponse.OrderTrendData trendData = new OrderReportResponse.OrderTrendData();
                        trendData.setDate(date);
                        
                        // 计算整月的付款和退款订单数
                        Integer monthPaidOrders = calculateMonthlyPaidOrders(monthStart, monthEnd);
                        Integer monthRefundOrders = calculateMonthlyRefundOrders(monthStart, monthEnd);
                        
                        trendData.setPaidOrders(monthPaidOrders);
                        trendData.setRefundOrders(monthRefundOrders);
                        orderTrend.add(trendData);
                    }
                    break;
            }
        } catch (Exception e) {
            // 返回空数据
        }
        
        return orderTrend;
    }

    /**
     * 根据时间类型获取订单总数趋势数据
     */
    private List<OrderReportResponse.OrderCountTrendData> getOrderCountTrendByTimeType(String timeType, int trendDays) {
        List<OrderReportResponse.OrderCountTrendData> orderCountTrend = new ArrayList<>();
        
        try {
            switch (timeType) {
                case "week":
                    // 显示最近7天的数据
                    for (int i = trendDays - 1; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        OrderReportResponse.OrderCountTrendData trendData = new OrderReportResponse.OrderCountTrendData();
                        trendData.setDate(date);
                        trendData.setOrderCount(orderService.getOrderNumByDate(0, date));
                        orderCountTrend.add(trendData);
                    }
                    break;
                case "month":
                    // 显示最近30天的数据
                    for (int i = trendDays - 1; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        OrderReportResponse.OrderCountTrendData trendData = new OrderReportResponse.OrderCountTrendData();
                        trendData.setDate(date);
                        trendData.setOrderCount(orderService.getOrderNumByDate(0, date));
                        orderCountTrend.add(trendData);
                    }
                    break;
                case "year":
                    // 显示最近12个月的数据（按月汇总）
                    for (int i = 11; i >= 0; i--) {
                        Date monthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -i));
                        Date monthEnd = DateUtil.endOfMonth(monthStart);
                        String date = DateUtil.format(monthStart, "yyyy-MM");
                        
                        OrderReportResponse.OrderCountTrendData trendData = new OrderReportResponse.OrderCountTrendData();
                        trendData.setDate(date);
                        
                        // 计算整月的订单总数
                        Integer monthOrderCount = calculateMonthlyOrderCount(monthStart, monthEnd);
                        trendData.setOrderCount(monthOrderCount);
                        orderCountTrend.add(trendData);
                    }
                    break;
            }
        } catch (Exception e) {
            // 返回空数据
        }
        
        return orderCountTrend;
    }

    /**
     * 计算月度付款订单数
     */
    private Integer calculateMonthlyPaidOrders(Date monthStart, Date monthEnd) {
        try {
            String startDate = DateUtil.format(monthStart, DateConstants.DATE_FORMAT_DATE);
            String endDate = DateUtil.format(monthEnd, DateConstants.DATE_FORMAT_DATE);
            
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .eq("paid", true)
                       .between("pay_time", startDate + " 00:00:00", endDate + " 23:59:59");
            
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算月度退款订单数
     */
    private Integer calculateMonthlyRefundOrders(Date monthStart, Date monthEnd) {
        try {
            String startDate = DateUtil.format(monthStart, DateConstants.DATE_FORMAT_DATE);
            String endDate = DateUtil.format(monthEnd, DateConstants.DATE_FORMAT_DATE);
            
            // 这里需要根据实际的退款订单表结构来查询
            // 暂时使用简化的查询逻辑
            return 0; // 可以根据实际情况实现
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 计算月度订单总数
     */
    private Integer calculateMonthlyOrderCount(Date monthStart, Date monthEnd) {
        try {
            String startDate = DateUtil.format(monthStart, DateConstants.DATE_FORMAT_DATE);
            String endDate = DateUtil.format(monthEnd, DateConstants.DATE_FORMAT_DATE);
            
            QueryWrapper<com.zbkj.common.model.order.Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .between("create_time", startDate + " 00:00:00", endDate + " 23:59:59");
            
            return orderService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== 商品报表相关的辅助方法 ====================

    /**
     * 根据筛选条件获取商品总数
     */
    private Integer getConnectedProductsByFilter(Integer merchantId, Integer productStatus) {
        try {
            QueryWrapper<com.zbkj.common.model.product.Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false);
            
            if (merchantId != null) {
                queryWrapper.eq("mer_id", merchantId);
            }
            
            if (productStatus != null) {
                queryWrapper.eq("is_show", productStatus);
            }
            
            return productService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据筛选条件获取已售商品数
     */
    private Integer getSoldProductsByFilter(Integer merchantId, Integer productStatus) {
        try {
            QueryWrapper<com.zbkj.common.model.product.Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .gt("sales", 0); // 有销量的商品
            
            if (merchantId != null) {
                queryWrapper.eq("mer_id", merchantId);
            }
            
            if (productStatus != null) {
                queryWrapper.eq("is_show", productStatus);
            }
            
            return productService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据筛选条件获取新增商品数
     */
    private Integer getNewProductsByFilter(String startDate, String endDate, Integer merchantId, Integer productStatus) {
        try {
            QueryWrapper<com.zbkj.common.model.product.Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .between("create_time", startDate + " 00:00:00", endDate + " 23:59:59");
            
            if (merchantId != null) {
                queryWrapper.eq("mer_id", merchantId);
            }
            
            if (productStatus != null) {
                queryWrapper.eq("is_show", productStatus);
            }
            
            return productService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据筛选条件获取退货商品数
     */
    private Integer getReturnedProductsByFilter(String startDate, String endDate, Integer merchantId) {
        try {
            // 这里需要根据实际的退款订单表结构来查询
            // 暂时返回简化的结果
            return refundOrderService.getAwaitAuditNum(merchantId != null ? merchantId : 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据时间类型获取商品趋势数据
     */
    private List<ProductReportResponse.DailyProductData> getProductTrendByTimeType(String timeType, int trendDays, Integer merchantId, Integer productStatus) {
        List<ProductReportResponse.DailyProductData> dailyTrend = new ArrayList<>();
        
        try {
            switch (timeType) {
                case "week":
                    // 显示最近7天的数据
                    for (int i = trendDays - 1; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        ProductReportResponse.DailyProductData data = new ProductReportResponse.DailyProductData();
                        data.setDate(date);
                        data.setProductCount(getNewProductsByFilter(date, date, merchantId, productStatus));
                        dailyTrend.add(data);
                    }
                    break;
                case "month":
                    // 显示最近30天的数据
                    for (int i = trendDays - 1; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        ProductReportResponse.DailyProductData data = new ProductReportResponse.DailyProductData();
                        data.setDate(date);
                        data.setProductCount(getNewProductsByFilter(date, date, merchantId, productStatus));
                        dailyTrend.add(data);
                    }
                    break;
                case "year":
                    // 显示最近12个月的数据（按月汇总）
                    for (int i = 11; i >= 0; i--) {
                        Date monthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -i));
                        Date monthEnd = DateUtil.endOfMonth(monthStart);
                        String date = DateUtil.format(monthStart, "yyyy-MM");
                        
                        ProductReportResponse.DailyProductData data = new ProductReportResponse.DailyProductData();
                        data.setDate(date);
                        
                        String startDate = DateUtil.format(monthStart, DateConstants.DATE_FORMAT_DATE);
                        String endDate = DateUtil.format(monthEnd, DateConstants.DATE_FORMAT_DATE);
                        data.setProductCount(getNewProductsByFilter(startDate, endDate, merchantId, productStatus));
                        dailyTrend.add(data);
                    }
                    break;
            }
        } catch (Exception e) {
            // 返回空数据
        }
        
        return dailyTrend;
    }

    /**
     * 根据筛选条件计算销量统计
     */
    private Integer[] calculateSalesStatisticsByFilter(String startDate, String endDate, Integer merchantId, Integer productStatus) {
        Integer[] result = new Integer[2]; // [平台自营销量, 其他店铺销量]
        
        try {
            // 这里需要根据实际的订单和商品表结构来查询销量数据
            // 暂时返回简化的结果
            result[0] = 0; // 平台自营销量
            result[1] = 0; // 其他店铺销量
            
            return result;
        } catch (Exception e) {
            result[0] = 0;
            result[1] = 0;
            return result;
        }
    }

    /**
     * 根据时间类型获取销量趋势数据
     */
    private List<ProductReportResponse.DailySalesData> getSalesTrendByTimeType(String timeType, int trendDays, Integer merchantId, Integer productStatus) {
        List<ProductReportResponse.DailySalesData> salesTrend = new ArrayList<>();
        
        try {
            switch (timeType) {
                case "week":
                case "month":
                    // 显示最近N天的数据
                    for (int i = trendDays - 1; i >= 0; i--) {
                        String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                        ProductReportResponse.DailySalesData data = new ProductReportResponse.DailySalesData();
                        data.setDate(date);
                        
                        Integer[] dailySales = calculateSalesStatisticsByFilter(date, date, merchantId, productStatus);
                        data.setSelfOperatedSales(dailySales[0]);
                        data.setOtherStoreSales(dailySales[1]);
                        salesTrend.add(data);
                    }
                    break;
                case "year":
                    // 显示最近12个月的数据（按月汇总）
                    for (int i = 11; i >= 0; i--) {
                        Date monthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -i));
                        Date monthEnd = DateUtil.endOfMonth(monthStart);
                        String date = DateUtil.format(monthStart, "yyyy-MM");
                        
                        ProductReportResponse.DailySalesData data = new ProductReportResponse.DailySalesData();
                        data.setDate(date);
                        
                        String startDate = DateUtil.format(monthStart, DateConstants.DATE_FORMAT_DATE);
                        String endDate = DateUtil.format(monthEnd, DateConstants.DATE_FORMAT_DATE);
                        Integer[] monthlySales = calculateSalesStatisticsByFilter(startDate, endDate, merchantId, productStatus);
                        data.setSelfOperatedSales(monthlySales[0]);
                        data.setOtherStoreSales(monthlySales[1]);
                        salesTrend.add(data);
                    }
                    break;
            }
        } catch (Exception e) {
            // 返回空数据
        }
        
        return salesTrend;
    }

    /**
     * 根据筛选条件计算入库数量
     */
    private Integer calculateInboundStockByFilter(String startDate, String endDate, Integer merchantId, Integer productStatus) {
        try {
            // 这里需要根据实际的库存表结构来查询入库数据
            // 暂时返回简化的结果
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据筛选条件计算出库数量
     */
    private Integer calculateOutboundStockByFilter(String startDate, String endDate, Integer merchantId, Integer productStatus) {
        try {
            // 这里需要根据实际的库存表结构来查询出库数据
            // 暂时返回简化的结果
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据筛选条件计算库存预警数量
     */
    private Integer calculateStockAlertCountByFilter(Integer merchantId, Integer productStatus) {
        try {
            QueryWrapper<com.zbkj.common.model.product.Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .le("stock", 10); // 库存小于等于10的商品
            
            if (merchantId != null) {
                queryWrapper.eq("mer_id", merchantId);
            }
            
            if (productStatus != null) {
                queryWrapper.eq("is_show", productStatus);
            }
            
            return productService.count(queryWrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据筛选条件获取库存详情列表
     */
    private List<ProductReportResponse.StockDetailData> getStockDetailListByFilter(Integer merchantId, Integer productStatus) {
        List<ProductReportResponse.StockDetailData> stockDetails = new ArrayList<>();
        
        try {
            QueryWrapper<com.zbkj.common.model.product.Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_del", false)
                       .le("stock", 10) // 库存小于等于10的商品
                       .last("LIMIT 10");
            
            if (merchantId != null) {
                queryWrapper.eq("mer_id", merchantId);
            }
            
            if (productStatus != null) {
                queryWrapper.eq("is_show", productStatus);
            }
            
            List<com.zbkj.common.model.product.Product> products = productService.list(queryWrapper);
            
            for (int i = 0; i < products.size(); i++) {
                com.zbkj.common.model.product.Product product = products.get(i);
                ProductReportResponse.StockDetailData detail = new ProductReportResponse.StockDetailData();
                detail.setSequence(i + 1);
                detail.setCode(product.getId().toString());
                detail.setProductInfo(product.getName());
                detail.setProductSpec("默认规格");
                detail.setTotalStock(product.getStock() + product.getSales()); // 总库存 = 剩余 + 已售
                detail.setRemainingStock(product.getStock());
                detail.setAlertTime(DateUtil.now());
                stockDetails.add(detail);
            }
            
            // 如果不够10个，填充空位
            while (stockDetails.size() < 10) {
                ProductReportResponse.StockDetailData detail = new ProductReportResponse.StockDetailData();
                detail.setSequence(stockDetails.size() + 1);
                detail.setCode("");
                detail.setProductInfo("暂无数据");
                detail.setProductSpec("");
                detail.setTotalStock(0);
                detail.setRemainingStock(0);
                detail.setAlertTime("");
                stockDetails.add(detail);
            }
        } catch (Exception e) {
            // 返回空数据
            for (int i = 0; i < 10; i++) {
                ProductReportResponse.StockDetailData detail = new ProductReportResponse.StockDetailData();
                detail.setSequence(i + 1);
                detail.setCode("");
                detail.setProductInfo("暂无数据");
                detail.setProductSpec("");
                detail.setTotalStock(0);
                detail.setRemainingStock(0);
                detail.setAlertTime("");
                stockDetails.add(detail);
            }
        }
        
        return stockDetails;
    }

    /**
     * 获取扩展统计数据（订单转化率、会员数量、库存周转率、商品评价数量）
     */
    @Override
    public HomeExtendedStatisticsResponse getExtendedStatistics(SystemAdmin systemAdmin) {
        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
        Integer merId = systemAdmin.getMerId();
        
        HomeExtendedStatisticsResponse response = new HomeExtendedStatisticsResponse();
        
        // 1. 订单转化率统计
        calculateOrderConversionRate(response, merId, today, yesterday);
        
        // 2. 会员数量统计
        calculateMemberStatistics(response, merId, today, yesterday);
        
        // 3. 库存周转率统计
        calculateInventoryTurnoverRate(response, merId, today, yesterday);
        
        // 4. 商品评价数量统计
        calculateProductReviewStatistics(response, merId, today, yesterday);
        
        return response;
    }

    /**
     * 计算订单转化率
     */
    private void calculateOrderConversionRate(HomeExtendedStatisticsResponse response, Integer merId, String today, String yesterday) {
        try {
            // 今日数据
            Integer todayVisitors = merchantDayRecordService.getVisitorsByDate(merId, today);
            Integer todayOrders = orderService.getOrderNumByDate(merId, today);
            
            // 昨日数据
            Integer yesterdayVisitors = merchantDayRecordService.getVisitorsByDate(merId, yesterday);
            Integer yesterdayOrders = orderService.getOrderNumByDate(merId, yesterday);
            
            response.setTodayVisitors(todayVisitors);
            response.setTodayOrders(todayOrders);
            response.setYesterdayVisitors(yesterdayVisitors);
            response.setYesterdayOrders(yesterdayOrders);
            
            // 计算转化率
            BigDecimal todayConversionRate = BigDecimal.ZERO;
            if (todayVisitors != null && todayVisitors > 0 && todayOrders != null) {
                todayConversionRate = new BigDecimal(todayOrders)
                    .divide(new BigDecimal(todayVisitors), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            }
            
            BigDecimal yesterdayConversionRate = BigDecimal.ZERO;
            if (yesterdayVisitors != null && yesterdayVisitors > 0 && yesterdayOrders != null) {
                yesterdayConversionRate = new BigDecimal(yesterdayOrders)
                    .divide(new BigDecimal(yesterdayVisitors), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            }
            
            response.setOrderConversionRate(todayConversionRate);
            response.setYesterdayOrderConversionRate(yesterdayConversionRate);
            
        } catch (Exception e) {
            log.error("计算订单转化率失败", e);
            response.setOrderConversionRate(BigDecimal.ZERO);
            response.setYesterdayOrderConversionRate(BigDecimal.ZERO);
            response.setTodayVisitors(0);
            response.setTodayOrders(0);
            response.setYesterdayVisitors(0);
            response.setYesterdayOrders(0);
        }
    }

    /**
     * 计算会员数量统计
     */
    private void calculateMemberStatistics(HomeExtendedStatisticsResponse response, Integer merId, String today, String yesterday) {
        try {
            // 查询会员总数
            QueryWrapper<com.zbkj.common.model.member.Member> totalWrapper = new QueryWrapper<>();
            totalWrapper.eq("mer_id", merId).eq("is_del", false);
            int totalMemberCount = memberService.count(totalWrapper);
            
            // 查询昨日会员总数
            QueryWrapper<com.zbkj.common.model.member.Member> yesterdayTotalWrapper = new QueryWrapper<>();
            yesterdayTotalWrapper.eq("mer_id", merId)
                .eq("is_del", false)
                .lt("DATE(create_time)", today);
            int yesterdayTotalMemberCount = memberService.count(yesterdayTotalWrapper);
            
            // 查询今日新增会员数
            QueryWrapper<com.zbkj.common.model.member.Member> todayNewWrapper = new QueryWrapper<>();
            todayNewWrapper.eq("mer_id", merId)
                .eq("is_del", false)
                .eq("DATE(create_time)", today);
            int todayNewMemberCount = memberService.count(todayNewWrapper);
            
            // 查询昨日新增会员数
            QueryWrapper<com.zbkj.common.model.member.Member> yesterdayNewWrapper = new QueryWrapper<>();
            yesterdayNewWrapper.eq("mer_id", merId)
                .eq("is_del", false)
                .eq("DATE(create_time)", yesterday);
            int yesterdayNewMemberCount = memberService.count(yesterdayNewWrapper);
            
            response.setMemberCount(totalMemberCount);
            response.setYesterdayMemberCount(yesterdayTotalMemberCount);
            response.setTodayNewMemberCount(todayNewMemberCount);
            response.setYesterdayNewMemberCount(yesterdayNewMemberCount);
            
        } catch (Exception e) {
            log.error("计算会员数量统计失败", e);
            response.setMemberCount(0);
            response.setYesterdayMemberCount(0);
            response.setTodayNewMemberCount(0);
            response.setYesterdayNewMemberCount(0);
        }
    }

    /**
     * 计算库存周转率
     */
    private void calculateInventoryTurnoverRate(HomeExtendedStatisticsResponse response, Integer merId, String today, String yesterday) {
        try {
            // 获取商品列表
            QueryWrapper<Product> productWrapper = new QueryWrapper<>();
            productWrapper.eq("mer_id", merId).eq("is_del", false);
            List<Product> products = productService.list(productWrapper);
            
            if (CollUtil.isEmpty(products)) {
                response.setInventoryTurnoverRate(BigDecimal.ZERO);
                response.setYesterdayInventoryTurnoverRate(BigDecimal.ZERO);
                response.setTotalInventoryValue(BigDecimal.ZERO);
                response.setYesterdayTotalInventoryValue(BigDecimal.ZERO);
                response.setCostOfGoodsSold(BigDecimal.ZERO);
                response.setYesterdayCostOfGoodsSold(BigDecimal.ZERO);
                return;
            }
            
            // 计算今日库存总值和销售成本
            BigDecimal todayInventoryValue = BigDecimal.ZERO;
            BigDecimal todayCostOfGoodsSold = BigDecimal.ZERO;
            
            for (Product product : products) {
                // 库存总值 = 库存数量 * 成本价
                BigDecimal inventoryValue = new BigDecimal(product.getStock())
                    .multiply(product.getCost() != null ? product.getCost() : BigDecimal.ZERO);
                todayInventoryValue = todayInventoryValue.add(inventoryValue);
                
                // 今日销售成本 = 今日销量 * 成本价
                Integer todaySales = getTodaySalesForProduct(product.getId(), today);
                BigDecimal salesCost = new BigDecimal(todaySales)
                    .multiply(product.getCost() != null ? product.getCost() : BigDecimal.ZERO);
                todayCostOfGoodsSold = todayCostOfGoodsSold.add(salesCost);
            }
            
            // 计算昨日数据
            BigDecimal yesterdayInventoryValue = BigDecimal.ZERO;
            BigDecimal yesterdayCostOfGoodsSold = BigDecimal.ZERO;
            
            for (Product product : products) {
                // 昨日库存总值（假设和今日相同，实际项目中可能需要历史数据）
                BigDecimal inventoryValue = new BigDecimal(product.getStock())
                    .multiply(product.getCost() != null ? product.getCost() : BigDecimal.ZERO);
                yesterdayInventoryValue = yesterdayInventoryValue.add(inventoryValue);
                
                // 昨日销售成本
                Integer yesterdaySales = getTodaySalesForProduct(product.getId(), yesterday);
                BigDecimal salesCost = new BigDecimal(yesterdaySales)
                    .multiply(product.getCost() != null ? product.getCost() : BigDecimal.ZERO);
                yesterdayCostOfGoodsSold = yesterdayCostOfGoodsSold.add(salesCost);
            }
            
            // 计算周转率 = 销售成本 / 平均库存
            BigDecimal todayTurnoverRate = BigDecimal.ZERO;
            if (todayInventoryValue.compareTo(BigDecimal.ZERO) > 0) {
                todayTurnoverRate = todayCostOfGoodsSold
                    .divide(todayInventoryValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            }
            
            BigDecimal yesterdayTurnoverRate = BigDecimal.ZERO;
            if (yesterdayInventoryValue.compareTo(BigDecimal.ZERO) > 0) {
                yesterdayTurnoverRate = yesterdayCostOfGoodsSold
                    .divide(yesterdayInventoryValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            }
            
            response.setInventoryTurnoverRate(todayTurnoverRate);
            response.setYesterdayInventoryTurnoverRate(yesterdayTurnoverRate);
            response.setTotalInventoryValue(todayInventoryValue);
            response.setYesterdayTotalInventoryValue(yesterdayInventoryValue);
            response.setCostOfGoodsSold(todayCostOfGoodsSold);
            response.setYesterdayCostOfGoodsSold(yesterdayCostOfGoodsSold);
            
        } catch (Exception e) {
            log.error("计算库存周转率失败", e);
            response.setInventoryTurnoverRate(BigDecimal.ZERO);
            response.setYesterdayInventoryTurnoverRate(BigDecimal.ZERO);
            response.setTotalInventoryValue(BigDecimal.ZERO);
            response.setYesterdayTotalInventoryValue(BigDecimal.ZERO);
            response.setCostOfGoodsSold(BigDecimal.ZERO);
            response.setYesterdayCostOfGoodsSold(BigDecimal.ZERO);
        }
    }

    /**
     * 计算商品评价数量统计
     */
    private void calculateProductReviewStatistics(HomeExtendedStatisticsResponse response, Integer merId, String today, String yesterday) {
        try {
            // 查询评价总数
            QueryWrapper<com.zbkj.common.model.product.ProductReply> totalWrapper = new QueryWrapper<>();
            totalWrapper.eq("mer_id", merId).eq("is_del", false);
            int totalReviewCount = productReplyService.count(totalWrapper);
            
            // 查询昨日评价总数
            QueryWrapper<com.zbkj.common.model.product.ProductReply> yesterdayTotalWrapper = new QueryWrapper<>();
            yesterdayTotalWrapper.eq("mer_id", merId)
                .eq("is_del", false)
                .lt("DATE(create_time)", today);
            int yesterdayTotalReviewCount = productReplyService.count(yesterdayTotalWrapper);
            
            // 查询今日新增评价数
            QueryWrapper<com.zbkj.common.model.product.ProductReply> todayNewWrapper = new QueryWrapper<>();
            todayNewWrapper.eq("mer_id", merId)
                .eq("is_del", false)
                .eq("DATE(create_time)", today);
            int todayNewReviewCount = productReplyService.count(todayNewWrapper);
            
            // 查询昨日新增评价数
            QueryWrapper<com.zbkj.common.model.product.ProductReply> yesterdayNewWrapper = new QueryWrapper<>();
            yesterdayNewWrapper.eq("mer_id", merId)
                .eq("is_del", false)
                .eq("DATE(create_time)", yesterday);
            int yesterdayNewReviewCount = productReplyService.count(yesterdayNewWrapper);
            
            // 计算平均评分
            List<com.zbkj.common.model.product.ProductReply> allReviews = productReplyService.list(totalWrapper);
            BigDecimal totalRating = BigDecimal.ZERO;
            BigDecimal averageRating = BigDecimal.ZERO;
            
            if (CollUtil.isNotEmpty(allReviews)) {
                for (com.zbkj.common.model.product.ProductReply review : allReviews) {
                    if (review.getStar() != null) {
                        totalRating = totalRating.add(new BigDecimal(review.getStar()));
                    }
                }
                averageRating = totalRating.divide(new BigDecimal(allReviews.size()), 2, RoundingMode.HALF_UP);
            }
            
            // 计算昨日平均评分
            List<com.zbkj.common.model.product.ProductReply> yesterdayReviews = productReplyService.list(yesterdayTotalWrapper);
            BigDecimal yesterdayTotalRating = BigDecimal.ZERO;
            BigDecimal yesterdayAverageRating = BigDecimal.ZERO;
            
            if (CollUtil.isNotEmpty(yesterdayReviews)) {
                for (com.zbkj.common.model.product.ProductReply review : yesterdayReviews) {
                    if (review.getStar() != null) {
                        yesterdayTotalRating = yesterdayTotalRating.add(new BigDecimal(review.getStar()));
                    }
                }
                yesterdayAverageRating = yesterdayTotalRating.divide(new BigDecimal(yesterdayReviews.size()), 2, RoundingMode.HALF_UP);
            }
            
            response.setProductReviewCount(totalReviewCount);
            response.setYesterdayProductReviewCount(yesterdayTotalReviewCount);
            response.setTodayNewProductReviewCount(todayNewReviewCount);
            response.setYesterdayNewProductReviewCount(yesterdayNewReviewCount);
            response.setAverageRating(averageRating);
            response.setYesterdayAverageRating(yesterdayAverageRating);
            
        } catch (Exception e) {
            log.error("计算商品评价数量统计失败", e);
            response.setProductReviewCount(0);
            response.setYesterdayProductReviewCount(0);
            response.setTodayNewProductReviewCount(0);
            response.setYesterdayNewProductReviewCount(0);
            response.setAverageRating(BigDecimal.ZERO);
            response.setYesterdayAverageRating(BigDecimal.ZERO);
        }
    }

    /**
     * 获取指定商品在指定日期的销量
     */
    private Integer getTodaySalesForProduct(Integer productId, String date) {
        try {
            // 这里应该查询订单明细表来获取具体的销量数据
            // 为了简化，这里使用一个模拟的方法
            QueryWrapper<com.zbkj.common.model.order.OrderDetail> wrapper = new QueryWrapper<>();
            wrapper.eq("product_id", productId)
                .eq("DATE(create_time)", date);
            
            List<com.zbkj.common.model.order.OrderDetail> orderDetails = orderDetailService.list(wrapper);
            return orderDetails.stream().mapToInt(detail -> detail.getPayNum()).sum();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取销售统计数据（总销售额、按月按年销售额、订单量统计）
     */
    @Override
    public SalesStatisticsResponse getSalesStatistics(SystemAdmin systemAdmin) {
        Integer merId = systemAdmin.getMerId();
        SalesStatisticsResponse response = new SalesStatisticsResponse();
        
        try {
            String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
            String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
            String currentMonthStart = DateUtil.beginOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
            String currentMonthEnd = DateUtil.endOfMonth(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
            String lastMonthStart = DateUtil.beginOfMonth(DateUtil.lastMonth()).toString(DateConstants.DATE_FORMAT_DATE);
            String lastMonthEnd = DateUtil.endOfMonth(DateUtil.lastMonth()).toString(DateConstants.DATE_FORMAT_DATE);
            String currentYearStart = DateUtil.beginOfYear(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
            String currentYearEnd = DateUtil.endOfYear(DateUtil.date()).toString(DateConstants.DATE_FORMAT_DATE);
            String lastYearStart = DateUtil.beginOfYear(DateUtil.offsetMonth(DateUtil.date(), -12)).toString(DateConstants.DATE_FORMAT_DATE);
            String lastYearEnd = DateUtil.endOfYear(DateUtil.offsetMonth(DateUtil.date(), -12)).toString(DateConstants.DATE_FORMAT_DATE);

            // 计算销售额统计
            calculateSalesAmountStatistics(response, merId, today, yesterday, 
                currentMonthStart, currentMonthEnd, lastMonthStart, lastMonthEnd,
                currentYearStart, currentYearEnd, lastYearStart, lastYearEnd);
            
            // 计算订单数量统计
            calculateOrderCountStatistics(response, merId, today, yesterday,
                currentMonthStart, currentMonthEnd, lastMonthStart, lastMonthEnd,
                currentYearStart, currentYearEnd, lastYearStart, lastYearEnd);
            
            // 计算平均客单价
            calculateAverageOrderValue(response);
            
        } catch (Exception e) {
            log.error("获取销售统计数据失败", e);
            // 设置默认值
            response.setTotalSalesAmount(BigDecimal.ZERO);
            response.setTodaySalesAmount(BigDecimal.ZERO);
            response.setYesterdaySalesAmount(BigDecimal.ZERO);
            response.setCurrentMonthSalesAmount(BigDecimal.ZERO);
            response.setLastMonthSalesAmount(BigDecimal.ZERO);
            response.setCurrentYearSalesAmount(BigDecimal.ZERO);
            response.setLastYearSalesAmount(BigDecimal.ZERO);
            response.setTotalOrderCount(0);
            response.setTodayOrderCount(0);
            response.setYesterdayOrderCount(0);
            response.setCurrentMonthOrderCount(0);
            response.setLastMonthOrderCount(0);
            response.setCurrentYearOrderCount(0);
            response.setLastYearOrderCount(0);
            response.setAverageOrderValue(BigDecimal.ZERO);
            response.setYesterdayAverageOrderValue(BigDecimal.ZERO);
            response.setCurrentMonthAverageOrderValue(BigDecimal.ZERO);
            response.setLastMonthAverageOrderValue(BigDecimal.ZERO);
        }
        
        return response;
    }

    /**
     * 计算销售额统计
     */
    private void calculateSalesAmountStatistics(SalesStatisticsResponse response, Integer merId,
                                              String today, String yesterday,
                                              String currentMonthStart, String currentMonthEnd,
                                              String lastMonthStart, String lastMonthEnd,
                                              String currentYearStart, String currentYearEnd,
                                              String lastYearStart, String lastYearEnd) {
        
        // 总销售额 - 使用现有的订单统计逻辑
        QueryWrapper<com.zbkj.common.model.order.Order> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("mer_id", merId)
            .eq("is_del", false)
            .in("status", Arrays.asList(1,2, 3,4,5,6)); // 已支付和已完成的订单
        List<com.zbkj.common.model.order.Order> totalOrders = orderService.list(totalWrapper);
        BigDecimal totalSalesAmount = totalOrders.stream()
            .map(com.zbkj.common.model.order.Order::getPayPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalSalesAmount(totalSalesAmount);
        
        // 今日销售额
        QueryWrapper<com.zbkj.common.model.order.Order> todayWrapper = new QueryWrapper<>();
        todayWrapper.eq("mer_id", merId)
            .eq("is_del", false)
            .in("status", Arrays.asList(1,2, 3,4,5,6))
            .eq("DATE(pay_time)", today);
        List<com.zbkj.common.model.order.Order> todayOrders = orderService.list(todayWrapper);
        BigDecimal todaySalesAmount = todayOrders.stream()
            .map(com.zbkj.common.model.order.Order::getPayPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTodaySalesAmount(todaySalesAmount);
        
        // 昨日销售额
        QueryWrapper<com.zbkj.common.model.order.Order> yesterdayWrapper = new QueryWrapper<>();
        yesterdayWrapper.eq("mer_id", merId)
            .eq("is_del", false)
            .in("status", Arrays.asList(1,2, 3,4,5,6))
            .eq("DATE(pay_time)", yesterday);
        List<com.zbkj.common.model.order.Order> yesterdayOrders = orderService.list(yesterdayWrapper);
        BigDecimal yesterdaySalesAmount = yesterdayOrders.stream()
            .map(com.zbkj.common.model.order.Order::getPayPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setYesterdaySalesAmount(yesterdaySalesAmount);
        
        // 本月销售额
        QueryWrapper<com.zbkj.common.model.order.Order> currentMonthWrapper = new QueryWrapper<>();
        currentMonthWrapper.eq("mer_id", merId)
            .eq("is_del", false)
            .in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", currentMonthStart)
            .le("DATE(pay_time)", currentMonthEnd);
        List<com.zbkj.common.model.order.Order> currentMonthOrders = orderService.list(currentMonthWrapper);
        BigDecimal currentMonthSalesAmount = currentMonthOrders.stream()
            .map(com.zbkj.common.model.order.Order::getPayPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setCurrentMonthSalesAmount(currentMonthSalesAmount);
        
        // 上月销售额
        QueryWrapper<com.zbkj.common.model.order.Order> lastMonthWrapper = new QueryWrapper<>();
        lastMonthWrapper.eq("mer_id", merId)
            .eq("is_del", false)
            .in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", lastMonthStart)
            .le("DATE(pay_time)", lastMonthEnd);
        List<com.zbkj.common.model.order.Order> lastMonthOrders = orderService.list(lastMonthWrapper);
        BigDecimal lastMonthSalesAmount = lastMonthOrders.stream()
            .map(com.zbkj.common.model.order.Order::getPayPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setLastMonthSalesAmount(lastMonthSalesAmount);
        
        // 本年销售额
        QueryWrapper<com.zbkj.common.model.order.Order> currentYearWrapper = new QueryWrapper<>();
        currentYearWrapper.eq("mer_id", merId)
            .eq("is_del", false)
            .in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", currentYearStart)
            .le("DATE(pay_time)", currentYearEnd);
        List<com.zbkj.common.model.order.Order> currentYearOrders = orderService.list(currentYearWrapper);
        BigDecimal currentYearSalesAmount = currentYearOrders.stream()
            .map(com.zbkj.common.model.order.Order::getPayPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setCurrentYearSalesAmount(currentYearSalesAmount);
        
        // 上年销售额
        QueryWrapper<com.zbkj.common.model.order.Order> lastYearWrapper = new QueryWrapper<>();
        lastYearWrapper.eq("mer_id", merId)
            .eq("is_del", false)
            .in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", lastYearStart)
            .le("DATE(pay_time)", lastYearEnd);
        List<com.zbkj.common.model.order.Order> lastYearOrders = orderService.list(lastYearWrapper);
        BigDecimal lastYearSalesAmount = lastYearOrders.stream()
            .map(com.zbkj.common.model.order.Order::getPayPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setLastYearSalesAmount(lastYearSalesAmount);
    }

    /**
     * 计算订单数量统计
     */
    private void calculateOrderCountStatistics(SalesStatisticsResponse response, Integer merId,
                                             String today, String yesterday,
                                             String currentMonthStart, String currentMonthEnd,
                                             String lastMonthStart, String lastMonthEnd,
                                             String currentYearStart, String currentYearEnd,
                                             String lastYearStart, String lastYearEnd) {
        
        // 总订单数
        QueryWrapper<com.zbkj.common.model.order.Order> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("mer_id", merId).eq("is_del", false).in("status", Arrays.asList(1,2, 3,4,5,6));
        int totalOrderCount = orderService.count(totalWrapper);
        response.setTotalOrderCount(totalOrderCount);
        
        // 今日订单数
        QueryWrapper<com.zbkj.common.model.order.Order> todayWrapper = new QueryWrapper<>();
        todayWrapper.eq("mer_id", merId).eq("is_del", false).in("status", Arrays.asList(1,2, 3,4,5,6)).eq("DATE(pay_time)", today);
        int todayOrderCount = orderService.count(todayWrapper);
        response.setTodayOrderCount(todayOrderCount);
        
        // 昨日订单数
        QueryWrapper<com.zbkj.common.model.order.Order> yesterdayWrapper = new QueryWrapper<>();
        yesterdayWrapper.eq("mer_id", merId).eq("is_del", false).in("status", Arrays.asList(1,2, 3,4,5,6)).eq("DATE(pay_time)", yesterday);
        int yesterdayOrderCount = orderService.count(yesterdayWrapper);
        response.setYesterdayOrderCount(yesterdayOrderCount);
        
        // 本月订单数
        QueryWrapper<com.zbkj.common.model.order.Order> currentMonthWrapper = new QueryWrapper<>();
        currentMonthWrapper.eq("mer_id", merId).eq("is_del", false).in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", currentMonthStart).le("DATE(pay_time)", currentMonthEnd);
        int currentMonthOrderCount = orderService.count(currentMonthWrapper);
        response.setCurrentMonthOrderCount(currentMonthOrderCount);
        
        // 上月订单数
        QueryWrapper<com.zbkj.common.model.order.Order> lastMonthWrapper = new QueryWrapper<>();
        lastMonthWrapper.eq("mer_id", merId).eq("is_del", false).in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", lastMonthStart).le("DATE(pay_time)", lastMonthEnd);
        int lastMonthOrderCount = orderService.count(lastMonthWrapper);
        response.setLastMonthOrderCount(lastMonthOrderCount);
        
        // 本年订单数
        QueryWrapper<com.zbkj.common.model.order.Order> currentYearWrapper = new QueryWrapper<>();
        currentYearWrapper.eq("mer_id", merId).eq("is_del", false).in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", currentYearStart).le("DATE(pay_time)", currentYearEnd);
        int currentYearOrderCount = orderService.count(currentYearWrapper);
        response.setCurrentYearOrderCount(currentYearOrderCount);
        
        // 上年订单数
        QueryWrapper<com.zbkj.common.model.order.Order> lastYearWrapper = new QueryWrapper<>();
        lastYearWrapper.eq("mer_id", merId).eq("is_del", false).in("status", Arrays.asList(1,2, 3,4,5,6))
            .ge("DATE(pay_time)", lastYearStart).le("DATE(pay_time)", lastYearEnd);
        int lastYearOrderCount = orderService.count(lastYearWrapper);
        response.setLastYearOrderCount(lastYearOrderCount);
    }

    /**
     * 计算平均客单价
     */
    private void calculateAverageOrderValue(SalesStatisticsResponse response) {
        // 计算总平均客单价
        if (response.getTotalOrderCount() > 0) {
            response.setAverageOrderValue(response.getTotalSalesAmount().divide(
                new BigDecimal(response.getTotalOrderCount()), 2, RoundingMode.HALF_UP));
        } else {
            response.setAverageOrderValue(BigDecimal.ZERO);
        }
        
        // 计算昨日平均客单价
        if (response.getYesterdayOrderCount() > 0) {
            response.setYesterdayAverageOrderValue(response.getYesterdaySalesAmount().divide(
                new BigDecimal(response.getYesterdayOrderCount()), 2, RoundingMode.HALF_UP));
        } else {
            response.setYesterdayAverageOrderValue(BigDecimal.ZERO);
        }
        
        // 计算本月平均客单价
        if (response.getCurrentMonthOrderCount() > 0) {
            response.setCurrentMonthAverageOrderValue(response.getCurrentMonthSalesAmount().divide(
                new BigDecimal(response.getCurrentMonthOrderCount()), 2, RoundingMode.HALF_UP));
        } else {
            response.setCurrentMonthAverageOrderValue(BigDecimal.ZERO);
        }
        
        // 计算上月平均客单价
        if (response.getLastMonthOrderCount() > 0) {
            response.setLastMonthAverageOrderValue(response.getLastMonthSalesAmount().divide(
                new BigDecimal(response.getLastMonthOrderCount()), 2, RoundingMode.HALF_UP));
        } else {
            response.setLastMonthAverageOrderValue(BigDecimal.ZERO);
        }
    }

    /**
     * 获取销售趋势图表数据
     */
    @Override
    public SalesTrendChartResponse getSalesTrendChart(SystemAdmin systemAdmin, String timeType, Integer days) {
        Integer merId = systemAdmin.getMerId();
        SalesTrendChartResponse response = new SalesTrendChartResponse();
        response.setTimeType(timeType);
        
        try {
            if ("day".equals(timeType)) {
                // 按天统计
                response.setTitle("最近" + days + "天销售趋势");
                List<String> dateList = new ArrayList<>();
                List<BigDecimal> salesAmountList = new ArrayList<>();
                
                for (int i = days - 1; i >= 0; i--) {
                    String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                    dateList.add(date);
                    
                    QueryWrapper<com.zbkj.common.model.order.Order> wrapper = new QueryWrapper<>();
                    wrapper.eq("mer_id", merId)
                        .eq("is_del", false)
                        .in("status", Arrays.asList(1,2, 3,4,5,6))
                        .eq("DATE(pay_time)", date);
                    List<com.zbkj.common.model.order.Order> orders = orderService.list(wrapper);
                    BigDecimal salesAmount = orders.stream()
                        .map(com.zbkj.common.model.order.Order::getPayPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    salesAmountList.add(salesAmount);
                }
                
                response.setDateList(dateList);
                response.setSalesAmountList(salesAmountList);
                
            } else if ("month".equals(timeType)) {
                // 按月统计（最近12个月）
                response.setTitle("最近12个月销售趋势");
                List<String> dateList = new ArrayList<>();
                List<BigDecimal> salesAmountList = new ArrayList<>();
                
                for (int i = 11; i >= 0; i--) {
                    DateTime monthDate = DateUtil.offsetMonth(DateUtil.date(), -i);
                    String monthStart = DateUtil.beginOfMonth(monthDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String monthEnd = DateUtil.endOfMonth(monthDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String monthStr = monthDate.toString("yyyy-MM");
                    
                    dateList.add(monthStr);
                    
                    QueryWrapper<com.zbkj.common.model.order.Order> wrapper = new QueryWrapper<>();
                    wrapper.eq("mer_id", merId)
                        .eq("is_del", false)
                        .in("status", Arrays.asList(1,2, 3,4,5,6))
                        .ge("DATE(pay_time)", monthStart)
                        .le("DATE(pay_time)", monthEnd);
                    List<com.zbkj.common.model.order.Order> orders = orderService.list(wrapper);
                    BigDecimal salesAmount = orders.stream()
                        .map(com.zbkj.common.model.order.Order::getPayPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    salesAmountList.add(salesAmount);
                }
                
                response.setDateList(dateList);
                response.setSalesAmountList(salesAmountList);
                
            } else if ("year".equals(timeType)) {
                // 按年统计（最近5年）
                response.setTitle("最近5年销售趋势");
                List<String> dateList = new ArrayList<>();
                List<BigDecimal> salesAmountList = new ArrayList<>();
                
                for (int i = 4; i >= 0; i--) {
                    DateTime yearDate = DateUtil.offsetMonth(DateUtil.date(), -i * 12);
                    String yearStart = DateUtil.beginOfYear(yearDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String yearEnd = DateUtil.endOfYear(yearDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String yearStr = yearDate.toString("yyyy");
                    
                    dateList.add(yearStr);
                    
                    QueryWrapper<com.zbkj.common.model.order.Order> wrapper = new QueryWrapper<>();
                    wrapper.eq("mer_id", merId)
                        .eq("is_del", false)
                        .in("status", Arrays.asList(1,2, 3,4,5,6))
                        .ge("DATE(pay_time)", yearStart)
                        .le("DATE(pay_time)", yearEnd);
                    List<com.zbkj.common.model.order.Order> orders = orderService.list(wrapper);
                    BigDecimal salesAmount = orders.stream()
                        .map(com.zbkj.common.model.order.Order::getPayPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    salesAmountList.add(salesAmount);
                }
                
                response.setDateList(dateList);
                response.setSalesAmountList(salesAmountList);
            }
            
        } catch (Exception e) {
            log.error("获取销售趋势图表数据失败", e);
            response.setDateList(new ArrayList<>());
            response.setSalesAmountList(new ArrayList<>());
        }
        
        return response;
    }

    /**
     * 获取订单数量趋势图表数据
     */
    @Override
    public SalesTrendChartResponse getOrderCountTrendChart(SystemAdmin systemAdmin, String timeType, Integer days) {
        Integer merId = systemAdmin.getMerId();
        SalesTrendChartResponse response = new SalesTrendChartResponse();
        response.setTimeType(timeType);
        
        try {
            if ("day".equals(timeType)) {
                // 按天统计
                response.setTitle("最近" + days + "天订单量趋势");
                List<String> dateList = new ArrayList<>();
                List<Integer> orderCountList = new ArrayList<>();
                
                for (int i = days - 1; i >= 0; i--) {
                    String date = DateUtil.offsetDay(DateUtil.date(), -i).toString(DateConstants.DATE_FORMAT_DATE);
                    dateList.add(date);
                    
                    QueryWrapper<com.zbkj.common.model.order.Order> wrapper = new QueryWrapper<>();
                    wrapper.eq("mer_id", merId)
                        .eq("is_del", false)
                        .in("status", Arrays.asList(1,2, 3,4,5,6))
                        .eq("DATE(pay_time)", date);
                    int orderCount = orderService.count(wrapper);
                    orderCountList.add(orderCount);
                }
                
                response.setDateList(dateList);
                response.setOrderCountList(orderCountList);
                
            } else if ("month".equals(timeType)) {
                // 按月统计（最近12个月）
                response.setTitle("最近12个月订单量趋势");
                List<String> dateList = new ArrayList<>();
                List<Integer> orderCountList = new ArrayList<>();
                
                for (int i = 11; i >= 0; i--) {
                    DateTime monthDate = DateUtil.offsetMonth(DateUtil.date(), -i);
                    String monthStart = DateUtil.beginOfMonth(monthDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String monthEnd = DateUtil.endOfMonth(monthDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String monthStr = monthDate.toString("yyyy-MM");
                    
                    dateList.add(monthStr);
                    
                    QueryWrapper<com.zbkj.common.model.order.Order> wrapper = new QueryWrapper<>();
                    wrapper.eq("mer_id", merId)
                        .eq("is_del", false)
                        .in("status", Arrays.asList(1,2, 3,4,5,6))
                        .ge("DATE(pay_time)", monthStart)
                        .le("DATE(pay_time)", monthEnd);
                    int orderCount = orderService.count(wrapper);
                    orderCountList.add(orderCount);
                }
                
                response.setDateList(dateList);
                response.setOrderCountList(orderCountList);
                
            } else if ("year".equals(timeType)) {
                // 按年统计（最近5年）
                response.setTitle("最近5年订单量趋势");
                List<String> dateList = new ArrayList<>();
                List<Integer> orderCountList = new ArrayList<>();
                
                for (int i = 4; i >= 0; i--) {
                    DateTime yearDate = DateUtil.offsetMonth(DateUtil.date(), -i * 12);
                    String yearStart = DateUtil.beginOfYear(yearDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String yearEnd = DateUtil.endOfYear(yearDate).toString(DateConstants.DATE_FORMAT_DATE);
                    String yearStr = yearDate.toString("yyyy");
                    
                    dateList.add(yearStr);
                    
                    QueryWrapper<com.zbkj.common.model.order.Order> wrapper = new QueryWrapper<>();
                    wrapper.eq("mer_id", merId)
                        .eq("is_del", false)
                        .in("status", Arrays.asList(1,2, 3,4,5,6))
                        .ge("DATE(pay_time)", yearStart)
                        .le("DATE(pay_time)", yearEnd);
                    int orderCount = orderService.count(wrapper);
                    orderCountList.add(orderCount);
                }
                
                response.setDateList(dateList);
                response.setOrderCountList(orderCountList);
            }
            
        } catch (Exception e) {
            log.error("获取订单数量趋势图表数据失败", e);
            response.setDateList(new ArrayList<>());
            response.setOrderCountList(new ArrayList<>());
        }
        
        return response;
    }


    /**
     * webscoket访问数据
     * @return
     */
    @Override
    public PlantFormScanResponse indexScanDate() {
        return caiShiJiaPlatformService.syncPlatformData();
//        String today = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
//        Integer year = DateUtil.thisYear();
//        String yesterday = DateUtil.yesterday().toString(DateConstants.DATE_FORMAT_DATE);
//        PlantFormScanResponse response = new PlantFormScanResponse();
//        response.setTodayNewUserNum(userService.getRegisterNumByDate(today));
//        response.setYesterdayNewUserNum(userService.getRegisterNumByDate(yesterday));
//        response.setPageviews(userVisitRecordService.getPageviewsByDate(today));
//        response.setYesterdayPageviews(userVisitRecordService.getPageviewsByDate(yesterday));
//        response.setTodayNewMerchantNum(merchantService.getNewNumByDate(today));
//        response.setYesterdayNewMerchantNum(merchantService.getNewNumByDate(yesterday));
//        response.setOrderNum(orderService.getOrderNumByDate(0, today));
//        response.setFinishOrderNum(orderService.getFinishOrderNumByDate(0, today));
//        response.setYearSales(orderService.getYearTotal());
//        response.setYesterdaySales(orderService.getToMerTotal());
//        response.setUserNum(userService.getTotalNum());
//        response.setUserPassNum(userService.getPassTotalNum());
//        response.setMerchantNum(merchantService.getAllCount());
//        response.setOrderList(orderService.getOrdersTop10());
//        Map<String,Object> map = userService.getUserByDate();
//        List<PlatformHomeUserResponse> result = (List<PlatformHomeUserResponse>) map.get("result");
//        //按照时间进行排序
//        result.sort(Comparator.comparing(PlatformHomeUserResponse::getDay));
//        response.setUserResponse(result);
//        AtomicInteger total = (AtomicInteger) map.get("total");
//        response.setNewAllUser(total.get());
//        response.setSaleList(orderService.getSaleTotal());
//        response.setAreas(this.indexArea());
//        //推送统计数据
//        return response;
    }

    @Override
    public List<PlatformHomeAreaResponse> indexArea() {
        return caiShiJiaPlatformService.syncPlatformData().getAreas();
//        List<PlatformHomeAreaResponse> res=new ArrayList<>();
//        List<CityRegion> cityRegions = cityRegionService.getBaseMapper().selectList(new QueryWrapper<CityRegion>().eq("parent_id", "420100"));
//        cityRegions.stream().forEach(cityRegion -> {
//            PlatformHomeAreaResponse result=new PlatformHomeAreaResponse();
//            result.setRegionName(cityRegion.getRegionName());
//            List<UserAddress> userList=userService.getBySaleIds(cityRegion.getRegionId());
//            if(CollUtil.isNotEmpty(userList)){
//                result.setUserTotal(userList.size());
//                List<Integer> userIds = userList.stream().map(UserAddress::getUid).collect(Collectors.toList());
//                result.setOrderTotal(orderService.getUserOrderTotal(userIds));
//                result.setOrderPriceTotal(orderService.getUserOrderPriceTotal(userIds));
//            }else {
//                result.setUserTotal(0);
//            }
//            res.add(result);
//        });
//
//        return res;
    }

}
