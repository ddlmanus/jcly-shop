package com.zbkj.admin.controller.platform;

import com.zbkj.common.response.PlantFormScanResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CaiShiJiaPlatformService;
import com.zbkj.service.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 平台端 - 采食家数据同步管理
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
@RestController
@RequestMapping("api/admin/platform/csj")
@Api(tags = "平台端 - 采食家数据同步管理")
public class PlatformCsjDataSyncController {

    @Autowired
    private CaiShiJiaPlatformService caiShiJiaPlatformService;

    @Autowired
    private SystemConfigService systemConfigService;

    @ApiOperation(value = "同步采食家平台大屏数据")
    @PostMapping("/sync/dashboard")
    public CommonResult<PlantFormScanResponse> syncDashboardData() {
        try {
            PlantFormScanResponse response = caiShiJiaPlatformService.syncPlatformData();
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("同步采食家平台大屏数据失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "使用指定账号同步采食家平台大屏数据")
    @PostMapping("/sync/dashboard/with-account")
    public CommonResult<PlantFormScanResponse> syncDashboardDataWithAccount(
            @RequestParam String account, 
            @RequestParam String password) {
        try {
            // 先测试登录
            String token = caiShiJiaPlatformService.loginToPlatform(account, password);
            
            // 然后同步数据（这里可以考虑传递账号密码参数，或者先设置到服务中）
            PlantFormScanResponse response = caiShiJiaPlatformService.syncPlatformData();
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("同步采食家平台大屏数据失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "同步并保存采食家平台数据到数据库")
    @PostMapping("/sync/save")
    public CommonResult<Boolean> syncAndSaveData() {
        try {
            boolean result = caiShiJiaPlatformService.syncAndSaveData();
            if (result) {
                return CommonResult.success(true);
            } else {
                return CommonResult.failed("数据同步保存失败");
            }
        } catch (Exception e) {
            log.error("同步并保存采食家平台数据失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "测试采食家平台登录")
    @PostMapping("/test/login")
    public CommonResult<String> testLogin(@RequestParam String account, @RequestParam String password) {
        try {
            String token = caiShiJiaPlatformService.loginToPlatform(account, password);
            return CommonResult.success(token);
        } catch (Exception e) {
            log.error("测试采食家平台登录失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "获取采食家配置信息")
    @GetMapping("/config")
    public CommonResult<Map<String, String>> getCsjConfig() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("csj_login_url", systemConfigService.getValueByKey("csj_login_url"));
            config.put("csj_dashboard_url", systemConfigService.getValueByKey("csj_dashboard_url"));
            config.put("csj_area_url", systemConfigService.getValueByKey("csj_area_url"));
            config.put("csj_account", systemConfigService.getValueByKey("csj_account"));
            // 密码字段不返回，只返回是否已配置
            String password = systemConfigService.getValueByKey("csj_password");
            config.put("csj_password_configured", password != null && !password.trim().isEmpty() ? "true" : "false");
            
            return CommonResult.success(config);
        } catch (Exception e) {
            log.error("获取采食家配置失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "更新采食家配置信息")
    @PostMapping("/config")
    public CommonResult<Boolean> updateCsjConfig(@RequestBody Map<String, String> configMap) {
        try {
            boolean success = true;
            
            // 更新各个配置项
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 验证配置键名
                if (key.startsWith("csj_") && value != null && !value.trim().isEmpty()) {
                    boolean updated = systemConfigService.updateOrSaveValueByName(key, value);
                    if (!updated) {
                        success = false;
                        log.error("更新配置项失败: {} = {}", key, value);
                    }
                }
            }
            
            if (success) {
                return CommonResult.success(true);
            } else {
                return CommonResult.failed("部分配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新采食家配置失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }
}