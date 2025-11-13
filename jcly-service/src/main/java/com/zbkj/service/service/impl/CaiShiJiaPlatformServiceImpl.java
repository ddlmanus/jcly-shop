package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.csj.CsjAreaStatic;
import com.zbkj.common.model.csj.CsjStatic;
import com.zbkj.common.model.csj.CsjTotalStatic;
import com.zbkj.common.response.PlantFormScanResponse;
import com.zbkj.common.response.PlatformHomeAreaResponse;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.service.service.CaiShiJiaPlatformService;
import com.zbkj.service.service.CsjAreaStaticService;
import com.zbkj.service.service.CsjStaticService;
import com.zbkj.service.service.CsjTotalStaticService;
import com.zbkj.service.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 采食家平台数据同步服务实现
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
public class CaiShiJiaPlatformServiceImpl implements CaiShiJiaPlatformService {

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Autowired
    private CsjStaticService csjStaticService;

    @Autowired
    private CsjAreaStaticService csjAreaStaticService;

    @Autowired
    private CsjTotalStaticService csjTotalStaticService;

    @Autowired
    private SystemConfigService systemConfigService;

    // 配置键名常量
    private static final String CONFIG_CSJ_LOGIN_URL = "csj_login_url";
    private static final String CONFIG_CSJ_DASHBOARD_URL = "csj_dashboard_url";
    private static final String CONFIG_CSJ_AREA_URL = "csj_area_url";
    private static final String CONFIG_CSJ_ACCOUNT = "csj_account";
    private static final String CONFIG_CSJ_PASSWORD = "csj_password";

    /**
     * 获取采食家平台登录URL
     */
    private String getCsjLoginUrl() {
        return systemConfigService.getValueByKey(CONFIG_CSJ_LOGIN_URL);
    }

    /**
     * 获取采食家平台大屏URL
     */
    private String getCsjDashboardUrl() {
        return systemConfigService.getValueByKey(CONFIG_CSJ_DASHBOARD_URL);
    }

    /**
     * 获取采食家平台区域URL
     */
    private String getCsjAreaUrl() {
        return systemConfigService.getValueByKey(CONFIG_CSJ_AREA_URL);
    }

    /**
     * 获取采食家平台账号
     */
    private String getCsjAccount() {
        return systemConfigService.getValueByKey(CONFIG_CSJ_ACCOUNT);
    }

    /**
     * 获取采食家平台密码
     */
    private String getCsjPassword() {
        return systemConfigService.getValueByKey(CONFIG_CSJ_PASSWORD);
    }

    @Override
    public String loginToPlatform(String account, String password) {
        log.info("开始登录采食家平台，账户: {}", account);
        
        try {
            // 构建登录请求参数
            JSONObject loginParam = new JSONObject();
            loginParam.put("account", account);
            loginParam.put("pwd", password);
            loginParam.put("captchaVO", new JSONObject());

            // 发送登录请求
            JSONObject loginResponse = restTemplateUtil.postJsonDataAndReturnJson(getCsjLoginUrl(), loginParam);
            
            if (ObjectUtil.isNull(loginResponse)) {
                throw new CrmebException("登录采食家平台失败：响应为空");
            }

            log.info("采食家平台登录响应: {}", loginResponse.toJSONString());

            // 检查登录是否成功
            Integer code = loginResponse.getInteger("code");
            if (code == null || code != 200) {
                String message = loginResponse.getString("msg");
                throw new CrmebException("登录采食家平台失败：" + message);
            }

            // 提取token
            JSONObject data = loginResponse.getJSONObject("data");
            if (ObjectUtil.isNull(data)) {
                throw new CrmebException("登录采食家平台失败：响应数据为空");
            }

            String token = data.getString("token");
            if (ObjectUtil.isEmpty(token)) {
                throw new CrmebException("登录采食家平台失败：未获取到token");
            }

            log.info("采食家平台登录成功，获取到token");
            return token;

        } catch (Exception e) {
            log.error("登录采食家平台异常", e);
            throw new CrmebException("登录采食家平台异常：" + e.getMessage());
        }
    }

    @Override
    public PlantFormScanResponse syncPlatformData() {
        log.info("开始同步采食家平台大屏数据");
        
        try {
            // 先登录获取token
            String token = loginToPlatform(getCsjAccount(), getCsjPassword());
            
            // 构建请求头
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authori-zation", token);
            
            // 获取大屏统计数据
            JSONObject dashboardData;
            try {
                dashboardData = restTemplateUtil.getData(getCsjDashboardUrl(), headers);
            } catch (Exception e) {
                log.error("调用采食家大屏接口失败", e);
                throw new CrmebException("调用采食家大屏接口失败：" + e.getMessage());
            }
            
            if (ObjectUtil.isNull(dashboardData)) {
                throw new CrmebException("获取采食家平台大屏数据失败：响应为空");
            }

            log.info("采食家平台大屏数据响应: {}", dashboardData.toJSONString());

            // 检查响应是否成功
            Integer code = dashboardData.getInteger("code");
            if (code == null || code != 200) {
                String message = dashboardData.getString("msg");
                throw new CrmebException("获取采食家平台大屏数据失败：" + message);
            }

            // 解析数据并转换为我们的响应对象
            JSONObject data = dashboardData.getJSONObject("data");
            if (ObjectUtil.isNull(data)) {
                throw new CrmebException("获取采食家平台大屏数据失败：数据为空");
            }

            PlantFormScanResponse response = JSON.parseObject(data.toJSONString(), PlantFormScanResponse.class);

            log.info("采食家平台大屏数据同步成功");
            //保存数据库
            return response;

        } catch (Exception e) {
            log.error("同步采食家平台大屏数据异常", e);
            throw new CrmebException("同步采食家平台大屏数据异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncAndSaveData() {
        log.info("开始同步并保存采食家平台数据");
        
        try {
            // 获取平台数据
            PlantFormScanResponse data = syncPlatformData();
            
            if (ObjectUtil.isNull(data)) {
                log.error("获取采食家平台数据为空");
                return false;
            }
            
            Date currentDate = new Date();
            LocalDate today = LocalDate.now();
            
            // 保存每日统计数据到CsjStatic表
            saveDailyStaticData(data, currentDate, today);
            
            // 保存总统计数据到CsjTotalStatic表
            saveTotalStaticData(data, currentDate, today);
            
            // 保存区域统计数据到CsjAreaStatic表
            saveAreaStaticData(data, currentDate);
            
            log.info("采食家平台数据同步并保存成功");
            return true;
            
        } catch (Exception e) {
            log.error("同步并保存采食家平台数据异常", e);
            throw new CrmebException("同步并保存采食家平台数据异常：" + e.getMessage());
        }
    }
    
    private void saveDailyStaticData(PlantFormScanResponse data, Date currentDate, LocalDate today) {
        log.info("保存每日统计数据");
        
        // 查询今天是否已有数据
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String[] parts = yearMonth.split("-");
        String year = parts[0];
        String month = parts[1];
        
        LambdaQueryWrapper<CsjStatic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CsjStatic::getYear, year)
                   .eq(CsjStatic::getMonth, month);
        
        CsjStatic existingStatic = csjStaticService.getOne(queryWrapper);
        
        CsjStatic csjStatic = new CsjStatic();
        if (ObjectUtil.isNotNull(existingStatic)) {
            // 更新现有记录
            csjStatic.setId(existingStatic.getId());
            csjStatic.setUpdateTime(currentDate);
        } else {
            // 创建新记录
            csjStatic.setCreateTime(currentDate);
            csjStatic.setUpdateTime(currentDate);
            csjStatic.setStatus(true);
            csjStatic.setIsDel(false);
            csjStatic.setSort(0);
        }
        
        // 设置统计数据
        csjStatic.setYear(year);
        csjStatic.setMonth(month);
        csjStatic.setSales(data.getSales());
        csjStatic.setYearSales(data.getYearSales());
        csjStatic.setYesterdaySales(data.getYesterdaySales());
        csjStatic.setPageviews(data.getPageviews());
        csjStatic.setYesterdayPageviews(data.getYesterdayPageviews());
        csjStatic.setOrderNum(data.getOrderNum());
        csjStatic.setFinishOrderNum(data.getFinishOrderNum());
        csjStatic.setYesterdayOrderNum(data.getYesterdayOrderNum());
        csjStatic.setTodayNewUserNum(data.getTodayNewUserNum());
        csjStatic.setYesterdayNewUserNum(data.getYesterdayNewUserNum());
        csjStatic.setTodayNewMerchantNum(data.getTodayNewMerchantNum());
        csjStatic.setYesterdayNewMerchantNum(data.getYesterdayNewMerchantNum());
        
        csjStaticService.saveOrUpdate(csjStatic);
    }
    
    private void saveTotalStaticData(PlantFormScanResponse data, Date currentDate, LocalDate today) {
        log.info("保存总统计数据");
        
        // 查询今天是否已有总数据
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        LambdaQueryWrapper<CsjTotalStatic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("DATE_FORMAT(statistics_date, '%Y-%m-%d') = {0}", dateStr);
        
        CsjTotalStatic existingTotal = csjTotalStaticService.getOne(queryWrapper);
        
        CsjTotalStatic csjTotalStatic = new CsjTotalStatic();
        if (ObjectUtil.isNotNull(existingTotal)) {
            // 更新现有记录
            csjTotalStatic.setId(existingTotal.getId());
            csjTotalStatic.setUpdateTime(currentDate);
        } else {
            // 创建新记录
            csjTotalStatic.setCreateTime(currentDate);
            csjTotalStatic.setUpdateTime(currentDate);
            csjTotalStatic.setStatus(true);
            csjTotalStatic.setIsDel(false);
            csjTotalStatic.setSort(0);
        }
        
        // 设置总统计数据
        csjTotalStatic.setStatisticsDate(currentDate);
        csjTotalStatic.setYearSales(data.getYearSales());
        csjTotalStatic.setUserNum(data.getUserNum());
        csjTotalStatic.setUserPassNum(data.getUserPassNum());
        csjTotalStatic.setMerchantNum(data.getMerchantNum());
        csjTotalStatic.setNewAllUser(data.getNewAllUser());
        
        csjTotalStaticService.saveOrUpdate(csjTotalStatic);
    }
    
    private void saveAreaStaticData(PlantFormScanResponse data, Date currentDate) {
        log.info("保存区域统计数据");
        
        List<PlatformHomeAreaResponse> areas = data.getAreas();
        if (ObjectUtil.isEmpty(areas)) {
            log.info("区域数据为空，跳过保存");
            return;
        }
        
        // 删除现有的区域数据
        csjAreaStaticService.remove(new LambdaQueryWrapper<>());
        
        // 保存新的区域数据
        for (int i = 0; i < areas.size(); i++) {
            PlatformHomeAreaResponse areaResponse = areas.get(i);
            
            CsjAreaStatic csjAreaStatic = new CsjAreaStatic();
            csjAreaStatic.setOrderTotal(areaResponse.getOrderTotal());
            csjAreaStatic.setUserTotal(areaResponse.getUserTotal());
            csjAreaStatic.setOrderPriceTotal(areaResponse.getOrderPriceTotal());
            csjAreaStatic.setRegionName(areaResponse.getRegionName());
            csjAreaStatic.setSort(i);
            csjAreaStatic.setStatus(true);
            csjAreaStatic.setIsDel(false);
            csjAreaStatic.setCreateTime(currentDate);
            csjAreaStatic.setUpdateTime(currentDate);
            
            csjAreaStaticService.save(csjAreaStatic);
        }
    }
}