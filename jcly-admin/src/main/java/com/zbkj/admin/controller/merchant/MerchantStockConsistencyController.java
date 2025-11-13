package com.zbkj.admin.controller.merchant;

import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
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
 * 商户端库存一致性管理控制器
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
@RequestMapping("api/admin/merchant/stock-consistency")
@Api(tags = "商户端 - 库存一致性管理")
@Validated
public class MerchantStockConsistencyController {

    @Autowired
    private StockConsistencyService stockConsistencyService;

    @PreAuthorize("hasAuthority('merchant:stock:check')")
    @ApiOperation(value = "检查库存一致性")
    @GetMapping("/check")
    public CommonResult<ConsistencyCheckResult> checkStockConsistency(
            @ApiParam(value = "是否自动修复", defaultValue = "false") @RequestParam(defaultValue = "false") boolean autoRepair) {
        
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        ConsistencyCheckResult result = stockConsistencyService.checkStockConsistency(admin.getMerId(), autoRepair);
        
        if (result.isConsistent()) {
            return CommonResult.success(result);
        } else {
            return CommonResult.success(result);
        }
    }

    @PreAuthorize("hasAuthority('merchant:stock:repair')")
    @ApiOperation(value = "修复库存不一致问题")
    @PostMapping("/repair")
    public CommonResult<ConsistencyCheckResult> repairStockInconsistency() {
        
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        ConsistencyCheckResult result = stockConsistencyService.repairStockInconsistency(admin.getMerId());
        
        return CommonResult.success(result);
    }
}