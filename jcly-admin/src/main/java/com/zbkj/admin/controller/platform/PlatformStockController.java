package com.zbkj.admin.controller.platform;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockInRequest;
import com.zbkj.common.request.StockOutRequest;
import com.zbkj.common.request.StockSearchRequest;
import com.zbkj.common.response.StockResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.StockInRecordService;
import com.zbkj.service.service.StockOutRecordService;
import com.zbkj.service.service.StockService;
import com.zbkj.service.service.ProductAttrValueService;
import com.zbkj.common.model.product.ProductAttrValue;

import java.util.List;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 平台端库存管理控制器
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
@RequestMapping("api/admin/platform/stock")
@Api(tags = "平台端库存管理控制器")
public class PlatformStockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockInRecordService stockInRecordService;

    @Autowired
    private StockOutRecordService stockOutRecordService;

    @PreAuthorize("hasAuthority('platform:stock:list')")
    @ApiOperation(value = "库存分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockResponse>> getStockList(@Validated StockSearchRequest request, 
                                                                @Validated PageParamRequest pageParamRequest) {
        // 平台端可以查看所有商户的库存，request.merId由前端传入
        return CommonResult.success(CommonPage.restPage(stockService.getStockPage(request, pageParamRequest)));
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "平台端商品入库")
    @PreAuthorize("hasAuthority('platform:stock:in')")
    @ApiOperation(value = "商品入库")
    @RequestMapping(value = "/in", method = RequestMethod.POST)
    public CommonResult<String> stockIn(@RequestBody @Validated StockInRequest request) {
        Integer operatorId = SecurityUtil.getLoginUserVo().getUser().getId();
        String operatorName = SecurityUtil.getLoginUserVo().getUser().getRealName();
        
        // 平台端需要指定商户ID，可以从商品信息中获取或由前端传入
        // 这里假设根据商品ID获取商户ID的逻辑在服务层处理
        if (stockInRecordService.stockIn(request, operatorId, operatorName, null)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "平台端商品出库")
    @PreAuthorize("hasAuthority('platform:stock:out')")
    @ApiOperation(value = "商品出库")
    @RequestMapping(value = "/out", method = RequestMethod.POST)
    public CommonResult<String> stockOut(@RequestBody @Validated StockOutRequest request) {
        Integer operatorId = SecurityUtil.getLoginUserVo().getUser().getId();
        String operatorName = SecurityUtil.getLoginUserVo().getUser().getRealName();
        
        // 平台端需要指定商户ID，可以从商品信息中获取或由前端传入
        if (stockOutRecordService.stockOut(request, operatorId, operatorName, null)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:stock:in:record:list')")
    @ApiOperation(value = "入库记录分页列表")
    @RequestMapping(value = "/in/record/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<com.zbkj.common.model.stock.StockInRecord>> getStockInRecordList(@RequestParam(value = "merId", required = false) Integer merId,
                                                                                                      @Validated PageParamRequest pageParamRequest) {
        // 平台端可以查看指定商户或所有商户的入库记录
        return CommonResult.success(CommonPage.restPage(stockInRecordService.getStockInRecordPage(merId, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('platform:stock:out:record:list')")
    @ApiOperation(value = "出库记录分页列表")
    @RequestMapping(value = "/out/record/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<com.zbkj.common.model.stock.StockOutRecord>> getStockOutRecordList(@RequestParam(value = "merId", required = false) Integer merId,
                                                                                                        @Validated PageParamRequest pageParamRequest) {
        // 平台端可以查看指定商户或所有商户的出库记录
        return CommonResult.success(CommonPage.restPage(stockOutRecordService.getStockOutRecordPage(merId, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('platform:stock:statistics')")
    @ApiOperation(value = "库存统计")
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public CommonResult<Object> getStockStatistics(@RequestParam(value = "merId", required = false) Integer merId) {
        // TODO: 实现库存统计功能，如总库存、低库存预警、各商户库存概况等
        return CommonResult.success();
    }

    @Autowired
    private ProductAttrValueService productAttrValueService;

  //  @PreAuthorize("hasAuthority('platform:stock:product:sku')")
    @ApiOperation(value = "根据商品ID获取SKU列表")
    @RequestMapping(value = "/product/{productId}/sku", method = RequestMethod.GET)
    public CommonResult<List<ProductAttrValue>> getProductSkuList(@PathVariable Integer productId) {
        List<ProductAttrValue> skuList = productAttrValueService.getListByProductId(productId);
        return CommonResult.success(skuList);
    }
} 