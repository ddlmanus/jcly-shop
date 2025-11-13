package com.zbkj.admin.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.model.stock.StockInRecord;
import com.zbkj.common.model.stock.StockOutRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockInRequest;
import com.zbkj.common.request.StockInRecordSearchRequest;
import com.zbkj.common.request.StockOutRequest;
import com.zbkj.common.request.StockOutRecordSearchRequest;
import com.zbkj.common.request.StockSearchRequest;
import com.zbkj.common.response.StockResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.StockInRecordService;
import com.zbkj.service.service.StockOutRecordService;
import com.zbkj.service.service.StockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 商户端库存管理控制器
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
@RequestMapping("api/admin/merchant/stock")
@Api(tags = "商户端库存管理控制器")
public class MerchantStockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockInRecordService stockInRecordService;

    @Autowired
    private StockOutRecordService stockOutRecordService;

    @ApiOperation(value = "库存分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getStockList(@Valid StockSearchRequest request, 
                                                          @Valid PageParamRequest pageParamRequest) {
        // 商户端只能查看自己的库存
        request.setMerId(SecurityUtil.getLoginUserVo().getUser().getMerId());
        PageInfo<StockResponse> pageInfo = stockService.getStockPage(request, pageParamRequest);
        
        // 构造前端期望的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("limit", pageInfo.getPageSize());
        
        return CommonResult.success(result);
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "商品入库")
    @ApiOperation(value = "商品入库")
    @RequestMapping(value = "/in", method = RequestMethod.POST)
    public CommonResult<String> stockIn(@RequestBody @Valid StockInRequest request) {
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        Integer operatorId = SecurityUtil.getLoginUserVo().getUser().getId();
        String operatorName = SecurityUtil.getLoginUserVo().getUser().getRealName();
        
        if (stockInRecordService.stockIn(request, operatorId, operatorName, merId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "商品出库")
    @ApiOperation(value = "商品出库")
    @RequestMapping(value = "/out", method = RequestMethod.POST)
    public CommonResult<String> stockOut(@RequestBody @Valid StockOutRequest request) {
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        Integer operatorId = SecurityUtil.getLoginUserVo().getUser().getId();
        String operatorName = SecurityUtil.getLoginUserVo().getUser().getRealName();
        
        if (stockOutRecordService.stockOut(request, operatorId, operatorName, merId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "入库记录分页列表")
    @RequestMapping(value = "/in/record/list", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getStockInRecordList(@Valid StockInRecordSearchRequest searchRequest,
                                                                  @Valid PageParamRequest pageParamRequest) {
        // 商户端只能查看自己的入库记录
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        searchRequest.setMerId(merId);
        PageInfo<StockInRecord> pageInfo = stockInRecordService.getStockInRecordPage(searchRequest, pageParamRequest);
        
        // 构造前端期望的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("limit", pageInfo.getPageSize());
        
        return CommonResult.success(result);
    }

    @ApiOperation(value = "出库记录分页列表")
    @RequestMapping(value = "/out/record/list", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getStockOutRecordList(@Valid StockOutRecordSearchRequest searchRequest,
                                                                   @Valid PageParamRequest pageParamRequest) {
        // 商户端只能查看自己的出库记录
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        searchRequest.setMerId(merId);
        PageInfo<StockOutRecord> pageInfo = stockOutRecordService.getStockOutRecordPage(searchRequest, pageParamRequest);
        
        // 构造前端期望的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("limit", pageInfo.getPageSize());
        
        return CommonResult.success(result);
    }
} 