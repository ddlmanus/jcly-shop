package com.zbkj.admin.controller.platform;

import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.StockConsistencyService;
import com.zbkj.service.service.impl.StockConsistencyServiceImpl.ConsistencyCheckResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 库存一致性管理控制器
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: AI Assistant
 * +----------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/stock-consistency")
@Api(tags = "平台端 - 库存一致性管理")
@Validated
public class PlatformStockConsistencyController {

    @Autowired
    private StockConsistencyService stockConsistencyService;

    @PreAuthorize("hasAuthority('platform:stock:check')")
    @ApiOperation(value = "检查库存一致性")
    @GetMapping("/check")
    public CommonResult<ConsistencyCheckResult> checkStockConsistency(
            @ApiParam(value = "商户ID，为空时检查所有商户") @RequestParam(required = false) Integer merId,
            @ApiParam(value = "是否自动修复", defaultValue = "false") @RequestParam(defaultValue = "false") boolean autoRepair) {
        
        ConsistencyCheckResult result = stockConsistencyService.checkStockConsistency(merId, autoRepair);
        
        if (result.isConsistent()) {
            return CommonResult.success(result);
        } else {
            return CommonResult.success(result);
        }
    }

    @PreAuthorize("hasAuthority('platform:stock:repair')")
    @ApiOperation(value = "修复库存不一致问题")
    @PostMapping("/repair")
    public CommonResult<ConsistencyCheckResult> repairStockInconsistency(
            @ApiParam(value = "商户ID，为空时修复所有商户") @RequestParam(required = false) Integer merId) {
        
        ConsistencyCheckResult result = stockConsistencyService.repairStockInconsistency(merId);
        
        return CommonResult.success(result);
    }
}