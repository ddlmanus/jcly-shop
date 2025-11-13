package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.BillConstants;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.bill.MerchantBill;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.FundsFlowRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.FinanceStatisticsResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.zbkj.service.dao.MerchantBillDao;
import com.zbkj.service.service.MerchantBalanceRecordService;
import com.zbkj.service.service.MerchantBillService;
import com.zbkj.service.service.MerchantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MerchantBillServiceImpl 接口实现
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
public class MerchantBillServiceImpl extends ServiceImpl<MerchantBillDao, MerchantBill> implements MerchantBillService {

    @Resource
    private MerchantBillDao dao;
    @Resource
    private MerchantService merchantService;
    @Resource
    private MerchantBalanceRecordService merchantBalanceRecordService;

    /**
     * 资金监控
     *
     * @param request          查询参数
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<MerchantBill> getFundsFlow(FundsFlowRequest request, PageParamRequest pageParamRequest) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        Page<MerchantBill> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<MerchantBill> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantBill::getMerId, systemAdmin.getMerId());
        if (StrUtil.isNotBlank(request.getOrderNo())) {
            lqw.eq(MerchantBill::getOrderNo, request.getOrderNo());
        }
        if (StrUtil.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(request.getDateLimit());
            lqw.between(MerchantBill::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        lqw.orderByDesc(MerchantBill::getId);
        List<MerchantBill> merchantBillList = dao.selectList(lqw);
        return CommonPage.copyPageInfo(page, merchantBillList);
    }

    /**
     * 获取商户财务统计数据
     * @param merId 商户ID
     * @param days 统计天数
     * @return 财务统计数据
     */
    @Override
    public FinanceStatisticsResponse getMerchantFinanceStatistics(Integer merId, Integer days) {
        FinanceStatisticsResponse response = new FinanceStatisticsResponse();
        
        // 获取今日日期
        String today = DateUtil.today();
        String yesterday = DateUtil.format(DateUtil.yesterday(), "yyyy-MM-dd");
        
        // 今日统计
        BigDecimal todayIncome = getTotalIncomeByDate(merId, today);
        BigDecimal todayExpenditure = getTotalExpenditureByDate(merId, today);
        response.setTodayIncome(todayIncome);
        response.setTodayExpenditure(todayExpenditure);
        response.setTodayBalance(todayIncome.subtract(todayExpenditure));
        
        // 昨日统计  
        BigDecimal yesterdayIncome = getTotalIncomeByDate(merId, yesterday);
        BigDecimal yesterdayExpenditure = getTotalExpenditureByDate(merId, yesterday);
        response.setYesterdayIncome(yesterdayIncome);
        response.setYesterdayExpenditure(yesterdayExpenditure);
        response.setYesterdayBalance(yesterdayIncome.subtract(yesterdayExpenditure));
        
        // 本月统计
        BigDecimal monthIncome = BigDecimal.ZERO;
        BigDecimal monthExpenditure = BigDecimal.ZERO;
        
        // 计算本月每天的数据
        List<FinanceStatisticsResponse.FinanceTrendData> incomeTrend = new ArrayList<>();
        List<FinanceStatisticsResponse.FinanceTrendData> expenditureTrend = new ArrayList<>();
        List<FinanceStatisticsResponse.FinanceTrendData> balanceTrend = new ArrayList<>();
        
        Date monthStartDate = DateUtil.beginOfMonth(new Date());
        Date todayDate = new Date();
        
        for (Date date = monthStartDate; !date.after(todayDate); date = DateUtil.offsetDay(date, 1)) {
            String dateStr = DateUtil.format(date, "yyyy-MM-dd");
            
            BigDecimal dayIncome = getTotalIncomeByDate(merId, dateStr);
            BigDecimal dayExpenditure = getTotalExpenditureByDate(merId, dateStr);
            BigDecimal dayBalance = dayIncome.subtract(dayExpenditure);
            
            monthIncome = monthIncome.add(dayIncome);
            monthExpenditure = monthExpenditure.add(dayExpenditure);
            
            // 构建趋势数据
            FinanceStatisticsResponse.FinanceTrendData trendData = new FinanceStatisticsResponse.FinanceTrendData();
            trendData.setDate(dateStr);
            trendData.setIncome(dayIncome);
            trendData.setExpenditure(dayExpenditure);
            trendData.setBalance(dayBalance);
            
            incomeTrend.add(trendData);
            expenditureTrend.add(trendData);
            balanceTrend.add(trendData);
        }
        
        response.setMonthIncome(monthIncome);
        response.setMonthExpenditure(monthExpenditure);
        response.setMonthBalance(monthIncome.subtract(monthExpenditure));
        
        // 获取商户当前余额信息
        Merchant merchant = merchantService.getById(merId);
        if (merchant != null) {
            response.setCurrentBalance(merchant.getBalance());
            // 可提现金额 = 当前余额 - 冻结金额
            BigDecimal frozenAmount = merchantBalanceRecordService.getFreezeAmountByMerId(merId);
            response.setFrozenAmount(frozenAmount);
            response.setWithdrawableAmount(merchant.getBalance().subtract(frozenAmount));
        }
        
        response.setIncomeTrend(incomeTrend);
        response.setExpenditureTrend(expenditureTrend);
        response.setBalanceTrend(balanceTrend);
        
        return response;
    }

    /**
     * 获取指定日期的总收入
     * @param merId 商户ID
     * @param date 日期 yyyy-MM-dd
     * @return 总收入
     */
    private BigDecimal getTotalIncomeByDate(Integer merId, String date) {
        LambdaQueryWrapper<MerchantBill> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantBill::getMerId, merId);
        lqw.eq(MerchantBill::getPm, BillConstants.BILL_PM_ADD); // 收入
        lqw.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        
        List<MerchantBill> billList = dao.selectList(lqw);
        return billList.stream()
                .map(MerchantBill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取指定日期的总支出
     * @param merId 商户ID
     * @param date 日期 yyyy-MM-dd
     * @return 总支出
     */
    private BigDecimal getTotalExpenditureByDate(Integer merId, String date) {
        LambdaQueryWrapper<MerchantBill> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantBill::getMerId, merId);
        lqw.eq(MerchantBill::getPm, BillConstants.BILL_PM_SUB); // 支出
        lqw.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        
        List<MerchantBill> billList = dao.selectList(lqw);
        return billList.stream()
                .map(MerchantBill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

