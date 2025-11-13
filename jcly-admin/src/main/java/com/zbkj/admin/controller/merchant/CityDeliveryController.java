package com.zbkj.admin.controller.merchant;

import cn.hutool.core.date.DateUtil;
import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.response.CityDeliveryOrderResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.*;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.model.order.CityDeliveryArea;
import com.zbkj.common.model.order.CityDeliveryFeeRule;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.CityDeliveryOrderRequest;
import com.zbkj.common.request.CityDeliveryOrderListRequest;
import com.zbkj.common.request.CityDeliveryDriverCreateRequest;
import com.zbkj.common.request.CityDeliveryDriverUpdateRequest;
import com.zbkj.common.request.CityDeliveryAreaCreateRequest;
import com.zbkj.common.request.CityDeliveryAreaUpdateRequest;
import com.zbkj.common.request.CityDeliveryFeeRuleCreateRequest;
import com.zbkj.common.request.CityDeliveryFeeRuleUpdateRequest;
import com.zbkj.common.page.CommonPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.vo.CityVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.model.order.CityDeliveryDriver;
import com.zbkj.common.response.CityDeliveryDriverResponse;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

import com.zbkj.common.model.order.CityDeliveryDriverWorkRecord;
import java.util.Calendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.order.CityDeliveryDriverIncomeDetail;
import com.zbkj.common.model.order.CityDeliveryCustomerRating;

/**
 * 商户端同城配送控制器
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
@RequestMapping("api/admin/merchant/city-delivery")
@Api(tags = "商户端同城配送控制器")
@Validated
public class CityDeliveryController {

    @Autowired
    private CityDeliveryService cityDeliveryService;

    @Autowired
    private CityDeliveryAreaService cityDeliveryAreaService;

    @Autowired
    private CityDeliveryFeeRuleService cityDeliveryFeeRuleService;

    @Autowired
    private CityRegionService cityRegionService;

    @Autowired
    private CityDeliveryDriverService cityDeliveryDriverService;

    @Autowired
    private CityDeliveryTrackingService cityDeliveryTrackingService;

    @Autowired
    private CityDeliveryDriverWorkRecordService cityDeliveryDriverWorkRecordService;

    @Autowired
    private CityDeliveryOrderService cityDeliveryOrderService;

    @Autowired
    private CityDeliveryDriverIncomeDetailService cityDeliveryDriverIncomeDetailService;

    @Autowired
    private CityDeliveryCustomerRatingService cityDeliveryCustomerRatingService;

   // @PreAuthorize("hasAuthority('merchant:city-delivery:order:info')")
    @ApiOperation(value = "根据订单号获取同城配送信息")
    @RequestMapping(value = "/order/{orderNo}", method = RequestMethod.GET)
    public CommonResult<CityDeliveryOrderResponse> getDeliveryOrderByOrderNo(
            @ApiParam(value = "订单号", required = true) @PathVariable String orderNo) {
        
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByOrderNo(orderNo);
        
        if (deliveryOrder != null && !deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
            return CommonResult.failed("无权限访问该配送信息");
        }
        
        return CommonResult.success(deliveryOrder);
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:order:detail')")
    @ApiOperation(value = "根据配送单号获取同城配送详情")
    @RequestMapping(value = "/detail/{deliveryOrderNo}", method = RequestMethod.GET)
    public CommonResult<CityDeliveryOrderResponse> getDeliveryOrderDetail(
            @ApiParam(value = "配送单号", required = true) @PathVariable String deliveryOrderNo) {
        
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
        
        if (deliveryOrder != null && !deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
            return CommonResult.failed("无权限访问该配送信息");
        }
        
        return CommonResult.success(deliveryOrder);
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新配送状态")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:status:update')")
    @ApiOperation(value = "更新配送状态")
    @RequestMapping(value = "/status/update", method = RequestMethod.POST)
    public CommonResult<String> updateDeliveryStatus(
            @ApiParam(value = "配送单号", required = true) @NotBlank(message = "配送单号不能为空") @RequestParam String deliveryOrderNo,
            @ApiParam(value = "配送状态", required = true) @NotNull(message = "配送状态不能为空") @RequestParam Integer deliveryStatus,
            @ApiParam(value = "备注") @RequestParam(required = false) String remark) {
        
        // 验证商户权限
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
        
        if (deliveryOrder == null) {
            return CommonResult.failed("配送订单不存在");
        }
        
        if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
            return CommonResult.failed("无权限操作该配送订单");
        }
        
        Boolean result = cityDeliveryService.updateDeliveryStatus(deliveryOrderNo, deliveryStatus, remark);
        
        if (result) {
            return CommonResult.success("配送状态更新成功");
        } else {
            return CommonResult.failed("配送状态更新失败");
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "分配配送员")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:driver:assign')")
    @ApiOperation(value = "分配配送员")
    @RequestMapping(value = "/driver/assign", method = RequestMethod.POST)
    public CommonResult<String> assignDriver(
            @ApiParam(value = "配送单号", required = true) @NotBlank(message = "配送单号不能为空") @RequestParam String deliveryOrderNo,
            @ApiParam(value = "配送员姓名", required = true) @NotBlank(message = "配送员姓名不能为空") @RequestParam String driverName,
            @ApiParam(value = "配送员手机号", required = true) @NotBlank(message = "配送员手机号不能为空") @RequestParam String driverPhone) {
        
        // 验证商户权限
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
        
        if (deliveryOrder == null) {
            return CommonResult.failed("配送订单不存在");
        }
        
        if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
            return CommonResult.failed("无权限操作该配送订单");
        }
        
        Boolean result = cityDeliveryService.assignDriver(deliveryOrderNo, null, driverName, driverPhone);
        
        if (result) {
            return CommonResult.success("配送员分配成功");
        } else {
            return CommonResult.failed("配送员分配失败");
        }
    }

    // ==================== 智能调度API ====================

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "手动分配配送员")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:dispatch:manual-assign')")
    @ApiOperation(value = "手动分配配送员")
    @RequestMapping(value = "/dispatch/manual-assign", method = RequestMethod.POST)
    public CommonResult<String> manualAssignDriver(@RequestBody Map<String, Object> data) {
        try {
            String deliveryOrderNo = (String) data.get("deliveryOrderNo");
            Integer driverId = (Integer) data.get("driverId");
            String driverName = (String) data.get("driverName");
            String driverPhone = (String) data.get("driverPhone");
            
            // 验证商户权限
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
            
            if (deliveryOrder == null) {
                return CommonResult.failed("配送订单不存在");
            }
            
            if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
                return CommonResult.failed("无权限操作该配送订单");
            }
            
            Boolean result = cityDeliveryService.assignDriver(deliveryOrderNo, driverId, driverName, driverPhone);
            
            if (result) {
                return CommonResult.success("配送员分配成功");
            } else {
                return CommonResult.failed("配送员分配失败");
            }
        } catch (Exception e) {
            log.error("手动分配配送员失败: {}", e.getMessage(), e);
            return CommonResult.failed("配送员分配失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "批量分配配送员")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:dispatch:batch-assign')")
    @ApiOperation(value = "批量分配配送员")
    @RequestMapping(value = "/dispatch/batch-assign", method = RequestMethod.POST)
    public CommonResult<String> batchAssignDriver(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<String> deliveryOrderNos = (List<String>) data.get("deliveryOrderNos");
            Integer driverId = (Integer) data.get("driverId");
            String driverName = (String) data.get("driverName");
            String driverPhone = (String) data.get("driverPhone");
            
            if (deliveryOrderNos == null || deliveryOrderNos.isEmpty()) {
                return CommonResult.failed("请选择需要分配的配送订单");
            }
            
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            int successCount = 0;
            int failCount = 0;
            
            for (String deliveryOrderNo : deliveryOrderNos) {
                // 验证商户权限
                CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
                
                if (deliveryOrder == null || !deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
                    failCount++;
                    continue;
                }
                
                Boolean result = cityDeliveryService.assignDriver(deliveryOrderNo, driverId, driverName, driverPhone);
                if (result) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            
            return CommonResult.success(String.format("批量分配完成，成功：%d，失败：%d", successCount, failCount));
            
        } catch (Exception e) {
            log.error("批量分配配送员失败: {}", e.getMessage(), e);
            return CommonResult.failed("批量分配配送员失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "自动分配配送员")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:dispatch:auto-assign')")
    @ApiOperation(value = "自动分配配送员")
    @RequestMapping(value = "/dispatch/auto-assign", method = RequestMethod.POST)
    public CommonResult<String> autoAssignDriver(@RequestBody Map<String, Object> data) {
        try {
            String deliveryOrderNo = (String) data.get("deliveryOrderNo");
            
            // 验证商户权限
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
            
            if (deliveryOrder == null) {
                return CommonResult.failed("配送订单不存在");
            }
            
            if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
                return CommonResult.failed("无权限操作该配送订单");
            }
            
            Boolean result = cityDeliveryService.autoAssignDriver(deliveryOrderNo);
            
            if (result) {
                return CommonResult.success("自动分配配送员成功");
            } else {
                return CommonResult.failed("自动分配配送员失败");
            }
        } catch (Exception e) {
            log.error("自动分配配送员失败: {}", e.getMessage(), e);
            return CommonResult.failed("自动分配配送员失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "重新分配配送员")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:dispatch:reassign')")
    @ApiOperation(value = "重新分配配送员")
    @RequestMapping(value = "/dispatch/reassign", method = RequestMethod.POST)
    public CommonResult<String> reassignDriver(@RequestBody Map<String, Object> data) {
        try {
            String deliveryOrderNo = (String) data.get("deliveryOrderNo");
            Integer newDriverId = (Integer) data.get("newDriverId");
            String newDriverName = (String) data.get("newDriverName");
            String newDriverPhone = (String) data.get("newDriverPhone");
            
            // 验证商户权限
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
            
            if (deliveryOrder == null) {
                return CommonResult.failed("配送订单不存在");
            }
            
            if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
                return CommonResult.failed("无权限操作该配送订单");
            }
            
            Boolean result = cityDeliveryService.reassignDriver(deliveryOrderNo, newDriverId);
            
            if (result) {
                return CommonResult.success("重新分配配送员成功");
            } else {
                return CommonResult.failed("重新分配配送员失败");
            }
        } catch (Exception e) {
            log.error("重新分配配送员失败: {}", e.getMessage(), e);
            return CommonResult.failed("重新分配配送员失败: " + e.getMessage());
        }
    }

   // @PreAuthorize("hasAuthority('merchant:city-delivery:dispatch:available-drivers')")
    @ApiOperation(value = "获取订单可用配送员")
    @RequestMapping(value = "/dispatch/available-drivers/{deliveryOrderNo}", method = RequestMethod.GET)
    public CommonResult<List<Integer>> getAvailableDriversForOrder(@PathVariable String deliveryOrderNo) {
        try {
            // 验证商户权限
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
            
            if (deliveryOrder == null) {
                return CommonResult.failed("配送订单不存在");
            }
            
            if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
                return CommonResult.failed("无权限操作该配送订单");
            }
            
            List<Integer> availableDrivers = cityDeliveryService.getAvailableDrivers(
                deliveryOrder.getPickupAddress(), BigDecimal.valueOf(10)
            );
            
            return CommonResult.success(availableDrivers);
        } catch (Exception e) {
            log.error("获取订单可用配送员失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取订单可用配送员失败: " + e.getMessage());
        }
    }

    // ==================== 数据导出API ====================

   // @PreAuthorize("hasAuthority('merchant:city-delivery:order:export')")
    @ApiOperation(value = "导出配送订单数据")
    @RequestMapping(value = "/order/export", method = RequestMethod.GET)
    public CommonResult<String> exportDeliveryOrders(CityDeliveryOrderListRequest request) {
        try {
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            
            // 添加商户过滤条件
            request.setMerId(systemAdmin.getMerId());
            
            // 这里应该实现具体的导出逻辑
            // 暂时返回成功消息
            return CommonResult.success("导出功能开发中...");
        } catch (Exception e) {
            log.error("导出配送订单数据失败: {}", e.getMessage(), e);
            return CommonResult.failed("导出配送订单数据失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "添加配送轨迹")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:track:add')")
    @ApiOperation(value = "添加配送轨迹")
    @RequestMapping(value = "/track/add", method = RequestMethod.POST)
    public CommonResult<String> addDeliveryTrack(
            @ApiParam(value = "配送单号", required = true) @NotBlank(message = "配送单号不能为空") @RequestParam String deliveryOrderNo,
            @ApiParam(value = "经度") @RequestParam(required = false) BigDecimal longitude,
            @ApiParam(value = "纬度") @RequestParam(required = false) BigDecimal latitude,
            @ApiParam(value = "地址") @RequestParam(required = false) String address,
            @ApiParam(value = "状态描述", required = true) @NotBlank(message = "状态描述不能为空") @RequestParam String status) {
        
        // 验证商户权限
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
        
        if (deliveryOrder == null) {
            return CommonResult.failed("配送订单不存在");
        }
        
        if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
            return CommonResult.failed("无权限操作该配送订单");
        }
        
        Boolean result = cityDeliveryService.addDeliveryTrack(deliveryOrderNo, longitude, latitude, address, status);
        
        if (result) {
            return CommonResult.success("配送轨迹添加成功");
        } else {
            return CommonResult.failed("配送轨迹添加失败");
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "取消配送订单")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:order:cancel')")
    @ApiOperation(value = "取消配送订单")
    @RequestMapping(value = "/order/cancel", method = RequestMethod.POST)
    public CommonResult<String> cancelDeliveryOrder(
            @ApiParam(value = "配送单号", required = true) @NotBlank(message = "配送单号不能为空") @RequestParam String deliveryOrderNo,
            @ApiParam(value = "取消原因") @RequestParam(required = false) String cancelReason) {
        
        // 验证商户权限
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
        
        if (deliveryOrder == null) {
            return CommonResult.failed("配送订单不存在");
        }
        
        if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
            return CommonResult.failed("无权限操作该配送订单");
        }
        
        Boolean result = cityDeliveryService.cancelDeliveryOrder(deliveryOrderNo, cancelReason);
        
        if (result) {
            return CommonResult.success("配送订单取消成功");
        } else {
            return CommonResult.failed("配送订单取消失败");
        }
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:fee:calculate')")
    @ApiOperation(value = "计算配送费用")
    @RequestMapping(value = "/fee/calculate", method = RequestMethod.GET)
    public CommonResult<BigDecimal> calculateDeliveryFee(
            @ApiParam(value = "取件地址", required = true) @NotBlank(message = "取件地址不能为空") @RequestParam String pickupAddress,
            @ApiParam(value = "送货地址", required = true) @NotBlank(message = "送货地址不能为空") @RequestParam String deliveryAddress,
            @ApiParam(value = "配送类型：1-即时配送，2-预约配送") @RequestParam(required = false, defaultValue = "1") Integer deliveryType) {
        
        BigDecimal fee = cityDeliveryService.calculateDeliveryFee(pickupAddress, deliveryAddress, deliveryType);
        return CommonResult.success(fee);
    }

   // @PreAuthorize("hasAuthority('merchant:city-delivery:driver:available')")
    @ApiOperation(value = "获取可用配送员列表")
    @RequestMapping(value = "/driver/available", method = RequestMethod.GET)
    public CommonResult<List<Integer>> getAvailableDrivers(
            @ApiParam(value = "取件地址", required = true) @NotBlank(message = "取件地址不能为空") @RequestParam String pickupAddress,
            @ApiParam(value = "服务半径", required = false, defaultValue = "10") BigDecimal serviceRadius) {
        
        List<Integer> availableDrivers = cityDeliveryService.getAvailableDrivers(pickupAddress, serviceRadius);
        return CommonResult.success(availableDrivers);
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "创建同城配送订单")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:order:create')")
    @ApiOperation(value = "创建同城配送订单")
    @RequestMapping(value = "/order/create", method = RequestMethod.POST)
    public CommonResult<CityDeliveryOrderResponse> createDeliveryOrder(
            @Valid @RequestBody CityDeliveryOrderRequest request) {
        
        try {
            // 获取商户信息
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            
            // 参数验证
            if (request.getDeliveryType() == 2 && (request.getScheduledTime() == null || request.getScheduledTime().isEmpty())) {
                return CommonResult.failed("预约配送必须指定预约时间");
            }
            
            // 创建配送订单
            CityDeliveryOrder deliveryOrder = cityDeliveryService.createDeliveryOrderFromRequest(
                request, systemAdmin.getMerId()
            );
            
            // 返回订单详情
            CityDeliveryOrderResponse response = cityDeliveryService.getDeliveryOrderByDeliveryNo(
                deliveryOrder.getDeliveryOrderNo()
            );
            
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("创建同城配送订单失败: {}", e.getMessage(), e);
            return CommonResult.failed("同城配送订单创建失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取配送订单列表")
    @RequestMapping(value = "/order/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CityDeliveryOrderResponse>> getDeliveryOrderList(
            @Validated CityDeliveryOrderListRequest request,
            @Validated PageParamRequest pageParamRequest) {
        
        try {
            // 获取商户信息
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            
            // 设置商户ID过滤
            request.setMerId(systemAdmin.getMerId());
            
            // 设置分页参数
            request.setPage(pageParamRequest.getPage());
            request.setLimit(pageParamRequest.getLimit());
            
            // 使用现有的分页方法
            PageInfo<CityDeliveryOrderResponse> pageInfo = cityDeliveryService.getDeliveryOrderPage(request);
            
            return CommonResult.success(CommonPage.restPage(pageInfo));
        } catch (Exception e) {
            log.error("获取配送订单列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送订单列表失败: " + e.getMessage());
        }
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:order:statistics')")
    @ApiOperation(value = "获取配送订单统计")
    @RequestMapping(value = "/order/statistics", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getDeliveryOrderStatistics(
            @ApiParam(value = "开始日期") @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束日期") @RequestParam(required = false) String endDate) {
        
        try {
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            Map<String, Object> statistics = cityDeliveryService.getOrderStatistics(
                systemAdmin.getMerId(), startDate, endDate
            );
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取配送订单统计失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送订单统计失败: " + e.getMessage());
        }
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:area:check')")
    @ApiOperation(value = "检查地址是否在服务范围内")
    @RequestMapping(value = "/area/check-service", method = RequestMethod.GET)
    public CommonResult<Boolean> checkAddressInServiceArea(
            @ApiParam(value = "地址", required = true) @NotBlank(message = "地址不能为空") @RequestParam String address) {
        
        Boolean result = cityDeliveryAreaService.isAddressInServiceArea(address);
        return CommonResult.success(result);
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:area:address')")
    @ApiOperation(value = "根据地址获取配送区域")
    @RequestMapping(value = "/area/by-address", method = RequestMethod.GET)
    public CommonResult<CityDeliveryArea> getAreaByAddress(
            @ApiParam(value = "地址", required = true) @NotBlank(message = "地址不能为空") @RequestParam String address) {
        
        CityDeliveryArea area = cityDeliveryAreaService.getAreaByAddress(address);
        return CommonResult.success(area);
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:area:timeslots')")
    @ApiOperation(value = "获取配送时间段")
    @RequestMapping(value = "/area/time-slots/{areaId}", method = RequestMethod.GET)
    public CommonResult<List<Map<String, String>>> getDeliveryTimeSlots(@PathVariable Integer areaId) {
        
        List<Map<String, String>> timeSlots = cityDeliveryAreaService.getDeliveryTimeSlots(areaId);
        return CommonResult.success(timeSlots);
    }

    // ==================== 费用规则管理 ====================

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:list')")
    @ApiOperation(value = "获取费用规则分页列表")
    @RequestMapping(value = "/fee-rule/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CityDeliveryFeeRule>> getFeeRuleList(
            @Validated PageParamRequest pageParamRequest) {
        
        try {
            // 使用正确的分页方法
            Page<CityDeliveryFeeRule> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
            
            List<CityDeliveryFeeRule> list = cityDeliveryFeeRuleService.getList(pageParamRequest);
            
            // 使用CommonPage.copyPageInfo方法，这是标准的分页处理方式
            PageInfo<CityDeliveryFeeRule> pageInfo = CommonPage.copyPageInfo(page, list);
            
            return CommonResult.success(CommonPage.restPage(pageInfo));
        } catch (Exception e) {
            log.error("获取费用规则列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取费用规则列表失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增费用规则")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:create')")
    @ApiOperation(value = "新增费用规则")
    @RequestMapping(value = "/fee-rule/create", method = RequestMethod.POST)
    public CommonResult<String> createFeeRule(@Valid @RequestBody CityDeliveryFeeRuleCreateRequest request) {
        try {
            // 转换为实体类
            CityDeliveryFeeRule feeRule = new CityDeliveryFeeRule();
            BeanUtils.copyProperties(request, feeRule);
            feeRule.setCreateTime(new Date());
            feeRule.setUpdateTime(new Date());
            feeRule.setIsDel(0);
            
            Boolean result = cityDeliveryFeeRuleService.create(feeRule);
            if (result) {
                return CommonResult.success("费用规则创建成功");
            } else {
                return CommonResult.failed("费用规则创建失败");
            }
        } catch (Exception e) {
            log.error("新增费用规则失败: {}", e.getMessage(), e);
            return CommonResult.failed("费用规则创建失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新费用规则")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:update')")
    @ApiOperation(value = "更新费用规则")
    @RequestMapping(value = "/fee-rule/update", method = RequestMethod.POST)
    public CommonResult<String> updateFeeRule(@Valid @RequestBody CityDeliveryFeeRuleUpdateRequest request) {
        try {
            // 转换为实体类
            CityDeliveryFeeRule feeRule = new CityDeliveryFeeRule();
            BeanUtils.copyProperties(request, feeRule);
            feeRule.setUpdateTime(new Date());
            
            Boolean result = cityDeliveryFeeRuleService.updateFeeRule(feeRule);
            if (result) {
                return CommonResult.success("费用规则更新成功");
            } else {
                return CommonResult.failed("费用规则更新失败");
            }
        } catch (Exception e) {
            log.error("更新费用规则失败: {}", e.getMessage(), e);
            return CommonResult.failed("费用规则更新失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除费用规则")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:delete')")
    @ApiOperation(value = "删除费用规则")
    @RequestMapping(value = "/fee-rule/delete/{id}", method = RequestMethod.POST)
    public CommonResult<String> deleteFeeRule(@PathVariable Integer id) {
        try {
            Boolean result = cityDeliveryFeeRuleService.delete(id);
            if (result) {
                return CommonResult.success("费用规则删除成功");
            } else {
                return CommonResult.failed("费用规则删除失败");
            }
        } catch (Exception e) {
            log.error("删除费用规则失败: {}", e.getMessage(), e);
            return CommonResult.failed("费用规则删除失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新费用规则状态")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:status')")
    @ApiOperation(value = "更新费用规则状态")
    @RequestMapping(value = "/fee-rule/status/{id}", method = RequestMethod.POST)
    public CommonResult<String> updateFeeRuleStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            Boolean result = cityDeliveryFeeRuleService.updateStatus(id, status);
            if (result) {
                return CommonResult.success("费用规则状态更新成功");
            } else {
                return CommonResult.failed("费用规则状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新费用规则状态失败: {}", e.getMessage(), e);
            return CommonResult.failed("费用规则状态更新失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "计算配送费用")
    @RequestMapping(value = "/fee-rule/calculate", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> calculateDeliveryFeeByRule(
            @ApiParam(value = "费用规则ID") @RequestParam(required = false) Integer feeRuleId,
            @ApiParam(value = "配送距离", required = true) @NotNull(message = "配送距离不能为空") @RequestParam BigDecimal distance,
            @ApiParam(value = "配送类型") @RequestParam(required = false, defaultValue = "1") Integer deliveryType) {
        
        Map<String, Object> result = cityDeliveryFeeRuleService.calculateFee(feeRuleId, distance, deliveryType);
        return CommonResult.success(result);
    }

    @ApiOperation(value = "详细费用计算")
    @RequestMapping(value = "/fee-rule/calculate-detailed", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> calculateDetailedFee(
            @ApiParam(value = "费用规则ID") @RequestParam(required = false) Integer feeRuleId,
            @ApiParam(value = "配送距离", required = true) @NotNull(message = "配送距离不能为空") @RequestParam BigDecimal distance,
            @ApiParam(value = "重量(kg)") @RequestParam(required = false, defaultValue = "0") BigDecimal weight,
            @ApiParam(value = "体积(m³)") @RequestParam(required = false, defaultValue = "0") BigDecimal volume,
            @ApiParam(value = "配送类型") @RequestParam(required = false, defaultValue = "1") Integer deliveryType,
            @ApiParam(value = "是否夜间配送") @RequestParam(required = false, defaultValue = "false") Boolean isNightTime,
            @ApiParam(value = "是否恶劣天气") @RequestParam(required = false, defaultValue = "false") Boolean isBadWeather,
            @ApiParam(value = "是否节假日") @RequestParam(required = false, defaultValue = "false") Boolean isHoliday,
            @ApiParam(value = "紧急等级") @RequestParam(required = false, defaultValue = "1") Integer urgentLevel) {
        
        Map<String, Object> result = cityDeliveryFeeRuleService.calculateDetailedFee(
                feeRuleId, distance, weight, volume, deliveryType, isNightTime, isBadWeather, isHoliday, urgentLevel);
        return CommonResult.success(result);
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:preview')")
    @ApiOperation(value = "预览费用计算")
    @RequestMapping(value = "/fee-rule/preview", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> previewDeliveryFee(
            @ApiParam(value = "取件地址", required = true) @NotBlank(message = "取件地址不能为空") @RequestParam String fromAddress,
            @ApiParam(value = "送货地址", required = true) @NotBlank(message = "送货地址不能为空") @RequestParam String toAddress,
            @ApiParam(value = "配送类型") @RequestParam(required = false, defaultValue = "1") Integer deliveryType,
            @ApiParam(value = "重量（kg）") @RequestParam(required = false) BigDecimal weight,
            @ApiParam(value = "体积（m³）") @RequestParam(required = false) BigDecimal volume) {
        
        Map<String, Object> result = cityDeliveryFeeRuleService.previewFee(fromAddress, toAddress, deliveryType, weight, volume);
        return CommonResult.success(result);
    }

 //   @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:detail')")
    @ApiOperation(value = "获取费用规则详情")
    @RequestMapping(value = "/fee-rule/detail/{id}", method = RequestMethod.GET)
    public CommonResult<CityDeliveryFeeRule> getFeeRuleDetail(@PathVariable Integer id) {
        try {
            CityDeliveryFeeRule feeRule = cityDeliveryFeeRuleService.getById(id);
            if (feeRule != null && feeRule.getIsDel() != 1) {
                return CommonResult.success(feeRule);
            } else {
                return CommonResult.failed("费用规则不存在");
            }
        } catch (Exception e) {
            log.error("获取费用规则详情失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取费用规则详情失败: " + e.getMessage());
        }
    }

 //   @PreAuthorize("hasAuthority('merchant:city-delivery:fee-rule:enabled')")
    @ApiOperation(value = "获取启用的费用规则")
    @RequestMapping(value = "/fee-rule/enabled", method = RequestMethod.GET)
    public CommonResult<List<CityDeliveryFeeRule>> getEnabledFeeRules() {
        try {
            List<CityDeliveryFeeRule> feeRules = cityDeliveryFeeRuleService.getEnabledRules();
            return CommonResult.success(feeRules);
        } catch (Exception e) {
            log.error("获取启用的费用规则失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取启用的费用规则失败: " + e.getMessage());
        }
    }

    // ==================== 配送区域管理 ====================

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:area:list')")
    @ApiOperation(value = "获取配送区域列表")
    @RequestMapping(value = "/area/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CityDeliveryArea>> getAreaList(
            @Validated PageParamRequest pageParamRequest) {
        
        try {
            // 使用正确的分页方法
            Page<CityDeliveryArea> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
            
            List<CityDeliveryArea> list = cityDeliveryAreaService.getList(pageParamRequest);
            
            // 使用CommonPage.copyPageInfo方法，这是标准的分页处理方式
            PageInfo<CityDeliveryArea> pageInfo = CommonPage.copyPageInfo(page, list);
            
            return CommonResult.success(CommonPage.restPage(pageInfo));
        } catch (Exception e) {
            log.error("获取配送区域列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送区域列表失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增配送区域")
  //  @PreAuthorize("hasAuthority('merchant:city-delivery:area:create')")
    @ApiOperation(value = "新增配送区域")
    @RequestMapping(value = "/area/create", method = RequestMethod.POST)
    public CommonResult<String> createArea(@Valid @RequestBody CityDeliveryAreaCreateRequest request) {
        try {
            // 转换为实体类
            CityDeliveryArea area = new CityDeliveryArea();
            BeanUtils.copyProperties(request, area);
            area.setCreateTime(new Date());
            area.setUpdateTime(new Date());
            area.setIsDel(0);
            
            Boolean result = cityDeliveryAreaService.create(area);
            if (result) {
                return CommonResult.success("配送区域创建成功");
            } else {
                return CommonResult.failed("配送区域创建失败");
            }
        } catch (Exception e) {
            log.error("新增配送区域失败: {}", e.getMessage(), e);
            return CommonResult.failed("配送区域创建失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新配送区域")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:area:update')")
    @ApiOperation(value = "更新配送区域")
    @RequestMapping(value = "/area/update", method = RequestMethod.POST)
    public CommonResult<String> updateArea(@Valid @RequestBody CityDeliveryAreaUpdateRequest request) {
        try {
            // 转换为实体类
            CityDeliveryArea area = new CityDeliveryArea();
            BeanUtils.copyProperties(request, area);
            area.setUpdateTime(new Date());
            
            Boolean result = cityDeliveryAreaService.updateArea(area);
            if (result) {
                return CommonResult.success("配送区域更新成功");
            } else {
                return CommonResult.failed("配送区域更新失败");
            }
        } catch (Exception e) {
            log.error("更新配送区域失败: {}", e.getMessage(), e);
            return CommonResult.failed("配送区域更新失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除配送区域")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:area:delete')")
    @ApiOperation(value = "删除配送区域")
    @RequestMapping(value = "/area/delete/{id}", method = RequestMethod.POST)
    public CommonResult<String> deleteArea(@PathVariable Integer id) {
        Boolean result = cityDeliveryAreaService.delete(id);
        if (result) {
            return CommonResult.success("配送区域删除成功");
        } else {
            return CommonResult.failed("配送区域删除失败");
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新配送区域状态")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:area:status')")
    @ApiOperation(value = "更新配送区域状态")
    @RequestMapping(value = "/area/status", method = RequestMethod.POST)
    public CommonResult<String> updateAreaStatus(@RequestBody Map<String, Object> data) {
        try {
            Integer id = (Integer) data.get("id");
            Integer status = (Integer) data.get("status");
            
            Boolean result = cityDeliveryAreaService.updateStatus(id, status);
            if (result) {
                return CommonResult.success("配送区域状态更新成功");
            } else {
                return CommonResult.failed("配送区域状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新配送区域状态失败: {}", e.getMessage(), e);
            return CommonResult.failed("配送区域状态更新失败: " + e.getMessage());
        }
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "批量更新配送区域状态")
   // @PreAuthorize("hasAuthority('merchant:city-delivery:area:batch-status')")
    @ApiOperation(value = "批量更新配送区域状态")
    @RequestMapping(value = "/area/batch-status", method = RequestMethod.POST)
    public CommonResult<String> batchUpdateAreaStatus(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) data.get("ids");
            Integer status = (Integer) data.get("status");
            
            if (ids == null || ids.isEmpty()) {
                return CommonResult.failed("请选择需要更新的配送区域");
            }
            
            Boolean result = cityDeliveryAreaService.batchUpdateStatus(ids, status);
            if (result) {
                return CommonResult.success("批量更新配送区域状态成功");
            } else {
                return CommonResult.failed("批量更新配送区域状态失败");
            }
        } catch (Exception e) {
            log.error("批量更新配送区域状态失败: {}", e.getMessage(), e);
            return CommonResult.failed("批量更新配送区域状态失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取配送区域详情")
    @RequestMapping(value = "/area/detail/{id}", method = RequestMethod.GET)
    public CommonResult<CityDeliveryArea> getAreaDetail(@PathVariable Integer id) {
        CityDeliveryArea area = cityDeliveryAreaService.getById(id);
        if (area != null && area.getIsDel() != 1) {
            return CommonResult.success(area);
        } else {
            return CommonResult.failed("配送区域不存在");
        }
    }

   // @PreAuthorize("hasAuthority('merchant:city-delivery:area:enabled')")
    @ApiOperation(value = "获取启用的配送区域")
    @RequestMapping(value = "/area/enabled", method = RequestMethod.GET)
    public CommonResult<List<CityDeliveryArea>> getEnabledAreas() {
        List<CityDeliveryArea> areas = cityDeliveryAreaService.getEnabledAreas();
        return CommonResult.success(areas);
    }

   // @PreAuthorize("hasAuthority('merchant:city-delivery:area:by-city')")
    @ApiOperation(value = "根据城市获取配送区域")
    @RequestMapping(value = "/area/by-city", method = RequestMethod.GET)
    public CommonResult<List<CityDeliveryArea>> getAreasByCity(
            @ApiParam(value = "省份") @RequestParam(required = false) String province,
            @ApiParam(value = "城市") @RequestParam(required = false) String city,
            @ApiParam(value = "区县") @RequestParam(required = false) String district) {
        
        List<CityDeliveryArea> areas = cityDeliveryAreaService.getAreasByCity(province, city, district);
        return CommonResult.success(areas);
    }

   // @PreAuthorize("hasAuthority('merchant:city-delivery:area:by-location')")
    @ApiOperation(value = "根据位置获取配送区域")
    @RequestMapping(value = "/area/by-location", method = RequestMethod.GET)
    public CommonResult<List<CityDeliveryArea>> getAreasByLocation(
            @ApiParam(value = "经度", required = true) @NotNull(message = "经度不能为空") @RequestParam BigDecimal longitude,
            @ApiParam(value = "纬度", required = true) @NotNull(message = "纬度不能为空") @RequestParam BigDecimal latitude) {
        
        List<CityDeliveryArea> areas = cityDeliveryAreaService.getAreasByLocation(longitude, latitude);
        return CommonResult.success(areas);
    }

  //  @PreAuthorize("hasAuthority('merchant:city-delivery:area:check-location')")
    @ApiOperation(value = "检查位置是否在配送区域内")
    @RequestMapping(value = "/area/check-location", method = RequestMethod.GET)
    public CommonResult<Boolean> checkLocationInArea(
            @ApiParam(value = "区域ID", required = true) @NotNull(message = "区域ID不能为空") @RequestParam Integer areaId,
            @ApiParam(value = "经度", required = true) @NotNull(message = "经度不能为空") @RequestParam BigDecimal longitude,
            @ApiParam(value = "纬度", required = true) @NotNull(message = "纬度不能为空") @RequestParam BigDecimal latitude) {
        
        Boolean result = cityDeliveryAreaService.checkLocationInArea(areaId, longitude, latitude);
        return CommonResult.success(result);
    }

   // @PreAuthorize("hasAuthority('merchant:city:delivery:regions')")
    @ApiOperation(value = "获取配送区域列表")
    @RequestMapping(value = "/regions/tree", method = RequestMethod.GET)
    public CommonResult<List<CityVo>> getDeliveryRegionsTree() {
        try {
            List<CityVo> regionTree = cityRegionService.getRegionListTree();
            return CommonResult.success(regionTree);
        } catch (Exception e) {
            log.error("获取配送区域列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送区域列表失败: " + e.getMessage());
        }
    }

   // @PreAuthorize("hasAuthority('merchant:city:delivery:cities')")
    @ApiOperation(value = "获取城市列表")
    @RequestMapping(value = "/cities/tree", method = RequestMethod.GET)
    public CommonResult<List<CityVo>> getDeliveryCitiesTree() {
        try {
            List<CityVo> cityTree = cityRegionService.getCityListTree();
            return CommonResult.success(cityTree);
        } catch (Exception e) {
            log.error("获取城市列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取城市列表失败: " + e.getMessage());
        }
    }

    // ==================== 配送员查询（商户端只读） ====================

    @ApiOperation(value = "获取配送员列表（商户端只读）")
    @RequestMapping(value = "/driver/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CityDeliveryDriver>> getDriverList(
            @Validated PageParamRequest pageParamRequest) {
        
        try {
            // 使用正确的分页方法
            Page<CityDeliveryDriver> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
            
            List<CityDeliveryDriver> list = cityDeliveryDriverService.getList(pageParamRequest);
            
            // 使用CommonPage.copyPageInfo方法，这是标准的分页处理方式
            PageInfo<CityDeliveryDriver> pageInfo = CommonPage.copyPageInfo(page, list);
            
            return CommonResult.success(CommonPage.restPage(pageInfo));
        } catch (Exception e) {
            log.error("获取配送员列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员列表失败: " + e.getMessage());
        }
    }

    // ==================== 配送员管理（商户端） ====================

    @ApiOperation(value = "创建配送员")
    @RequestMapping(value = "/driver/create", method = RequestMethod.POST)
    public CommonResult<Boolean> createDriver(@RequestBody @Valid CityDeliveryDriverCreateRequest request) {
        try {
            // 转换为实体类
            CityDeliveryDriver driver = new CityDeliveryDriver();
            BeanUtils.copyProperties(request, driver);
            driver.setCreateTime(new Date());
            driver.setUpdateTime(new Date());
            driver.setIsDel(0);
            
            Boolean result = cityDeliveryDriverService.create(driver);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("创建配送员失败: {}", e.getMessage(), e);
            return CommonResult.failed("创建配送员失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "更新配送员信息")
    @RequestMapping(value = "/driver/update", method = RequestMethod.POST)
    public CommonResult<Boolean> updateDriver(@RequestBody @Valid CityDeliveryDriverUpdateRequest request) {
        try {
            // 转换为实体类
            CityDeliveryDriver driver = new CityDeliveryDriver();
            BeanUtils.copyProperties(request, driver);
            driver.setUpdateTime(new Date());
            
            Boolean result = cityDeliveryDriverService.updateDriver(driver);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("更新配送员信息失败: {}", e.getMessage(), e);
            return CommonResult.failed("更新配送员信息失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "删除配送员")
    @RequestMapping(value = "/driver/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<Boolean> deleteDriver(@PathVariable Integer id) {
        try {
            Boolean result = cityDeliveryDriverService.delete(id);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("删除配送员失败: {}", e.getMessage(), e);
            return CommonResult.failed("删除配送员失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "更新配送员状态")
    @RequestMapping(value = "/driver/status", method = RequestMethod.POST)
    public CommonResult<Boolean> updateDriverStatus(@RequestBody Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            Integer status = (Integer) params.get("status");
            
            Boolean result = cityDeliveryDriverService.updateStatus(id, status);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("更新配送员状态失败: {}", e.getMessage(), e);
            return CommonResult.failed("更新配送员状态失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取配送员详情")
    @RequestMapping(value = "/driver/detail/{id}", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getDriverDetail(@PathVariable Integer id) {
        try {
            // 获取配送员基本信息
            CityDeliveryDriver driverInfo = cityDeliveryDriverService.getById(id);
            if (driverInfo == null) {
                return CommonResult.failed("配送员不存在");
            }
            
            // 构建完整的详情对象
            Map<String, Object> result = new HashMap<>();
            result.put("basicInfo", driverInfo);
            
            // 获取统计信息
            Map<String, Object> statistics = getDriverStatistics(id);
            result.put("statistics", statistics);
            
            // 获取最近的配送订单
            List<Map<String, Object>> recentOrders = getDriverRecentOrders(id, 10);
            result.put("recentOrders", recentOrders);
            
            // 获取工作记录统计
            Map<String, Object> workStats = getDriverWorkStatistics(id);
            result.put("workStats", workStats);
            
            // 获取收入统计
            Map<String, Object> incomeStats = getDriverIncomeStatistics(id);
            result.put("incomeStats", incomeStats);
            
            // 获取评价统计
            Map<String, Object> ratingStats = getDriverRatingStatistics(id);
            result.put("ratingStats", ratingStats);
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取配送员详情失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取配送员统计信息
     */
    private Map<String, Object> getDriverStatistics(Integer driverId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 订单统计
        LambdaQueryWrapper<CityDeliveryOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(CityDeliveryOrder::getDriverId, driverId)
                   .eq(CityDeliveryOrder::getIsDel, 0);
        
        List<CityDeliveryOrder> allOrders = cityDeliveryOrderService.list(orderWrapper);
        stats.put("totalOrders", allOrders.size());
        
        long completedOrders = allOrders.stream()
                .filter(order -> order.getDeliveryStatus() == 4)
                .count();
        stats.put("completedOrders", completedOrders);
        
        // 本月订单统计
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date monthStart = calendar.getTime();
        
        long monthlyOrders = allOrders.stream()
                .filter(order -> order.getCreateTime().after(monthStart))
                .count();
        stats.put("monthlyOrders", monthlyOrders);
        
        // 计算总收入
        BigDecimal totalIncome = allOrders.stream()
                .filter(order -> order.getDeliveryStatus() == 4)
                .map(order -> order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalIncome", totalIncome);
        
        // 计算平均评分
        BigDecimal avgRating = cityDeliveryCustomerRatingService.calculateDriverAverageRating(driverId);
        stats.put("averageRating", avgRating);
        
        return stats;
    }

    /**
     * 获取配送员最近订单
     */
    private List<Map<String, Object>> getDriverRecentOrders(Integer driverId, Integer limit) {
        LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CityDeliveryOrder::getDriverId, driverId)
               .eq(CityDeliveryOrder::getIsDel, 0)
               .orderByDesc(CityDeliveryOrder::getCreateTime)
               .last("LIMIT " + limit);
        
        List<CityDeliveryOrder> orders = cityDeliveryOrderService.list(wrapper);
        
        return orders.stream().map(order -> {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("deliveryOrderNo", order.getDeliveryOrderNo());
            orderInfo.put("orderNo", order.getOrderNo());
            orderInfo.put("pickupAddress", order.getPickupAddress());
            orderInfo.put("deliveryAddress", order.getDeliveryAddress());
            orderInfo.put("deliveryFee", order.getDeliveryFee());
            orderInfo.put("deliveryStatus", order.getDeliveryStatus());
            orderInfo.put("createTime", order.getCreateTime());
            orderInfo.put("deliveryStatusText", getDeliveryStatusText(order.getDeliveryStatus()));
            return orderInfo;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取配送员工作统计
     */
    private Map<String, Object> getDriverWorkStatistics(Integer driverId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取最近30天的工作记录
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date startDate = calendar.getTime();
            
            String startDateStr = DateUtil.format(startDate, "yyyy-MM-dd");
            String endDateStr = DateUtil.format(new Date(), "yyyy-MM-dd");
            
            Map<String, Object> workStats = cityDeliveryDriverWorkRecordService.getDriverWorkStats(driverId, 30);
            stats.putAll(workStats);
            
        } catch (Exception e) {
            log.warn("获取工作统计失败: {}", e.getMessage());
            stats.put("workDays", 0);
            stats.put("totalWorkHours", 0);
            stats.put("averageWorkHours", 0);
        }
        
        return stats;
    }

    /**
     * 获取配送员收入统计
     */
    private Map<String, Object> getDriverIncomeStatistics(Integer driverId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取收入统计
            Map<String, Object> incomeStats = cityDeliveryDriverWorkRecordService.getDriverIncomeStats(
                    driverId, null, null);
            stats.putAll(incomeStats);
            
        } catch (Exception e) {
            log.warn("获取收入统计失败: {}", e.getMessage());
            stats.put("totalIncome", BigDecimal.ZERO);
            stats.put("monthlyIncome", BigDecimal.ZERO);
            stats.put("todayIncome", BigDecimal.ZERO);
        }
        
        return stats;
    }

    /**
     * 获取配送员评价统计
     */
    private Map<String, Object> getDriverRatingStatistics(Integer driverId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Map<String, Object> ratingStats = cityDeliveryCustomerRatingService.getDriverRatingStats(driverId);
            stats.putAll(ratingStats);
            
        } catch (Exception e) {
            log.warn("获取评价统计失败: {}", e.getMessage());
            stats.put("totalRatings", 0);
            stats.put("averageRating", BigDecimal.valueOf(5.0));
        }
        
        return stats;
    }

    @ApiOperation(value = "配送员上线")
    @RequestMapping(value = "/driver/online/{id}", method = RequestMethod.POST)
    public CommonResult<Boolean> driverOnline(@PathVariable Integer id) {
        try {
            Boolean result = cityDeliveryDriverService.driverOnline(id);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("配送员上线失败: {}", e.getMessage(), e);
            return CommonResult.failed("配送员上线失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "配送员下线")
    @RequestMapping(value = "/driver/offline/{id}", method = RequestMethod.POST)
    public CommonResult<Boolean> driverOffline(@PathVariable Integer id) {
        try {
            Boolean result = cityDeliveryDriverService.driverOffline(id);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("配送员下线失败: {}", e.getMessage(), e);
            return CommonResult.failed("配送员下线失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "批量更新配送员状态")
    @RequestMapping(value = "/driver/batch-status", method = RequestMethod.POST)
    public CommonResult<Boolean> batchUpdateDriverStatus(@RequestBody Map<String, Object> params) {
        try {
            List<Integer> ids = (List<Integer>) params.get("ids");
            Integer status = (Integer) params.get("status");
            
            Boolean result = cityDeliveryDriverService.batchUpdateStatus(ids, status);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("批量更新配送员状态失败: {}", e.getMessage(), e);
            return CommonResult.failed("批量更新配送员状态失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "配送员认证")
    @RequestMapping(value = "/driver/certification", method = RequestMethod.POST)
    public CommonResult<Boolean> driverCertification(@RequestBody Map<String, Object> params) {
        try {
            Integer driverId = (Integer) params.get("id");
            Integer certificationStatus = (Integer) params.get("certificationStatus");
            String remark = (String) params.get("remark");
            
            Boolean result = cityDeliveryDriverService.certification(driverId, certificationStatus, remark);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("配送员认证失败: {}", e.getMessage(), e);
            return CommonResult.failed("配送员认证失败: " + e.getMessage());
        }
    }

    // ==================== 配送订单管理 ====================
    
    // ==================== 配送员详情相关API ====================

    @ApiOperation(value = "获取配送员配送订单")
    @RequestMapping(value = "/driver/delivery-orders/{driverId}", method = RequestMethod.GET)
    public CommonResult<CommonPage<CityDeliveryOrderResponse>> getDriverDeliveryOrders(
            @PathVariable Integer driverId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @Validated PageParamRequest pageParamRequest) {
        try {
            // 构建查询条件
            CityDeliveryOrderListRequest request = new CityDeliveryOrderListRequest();
            request.setDriverId(driverId);
            request.setDeliveryStatus(status);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setPage(pageParamRequest.getPage());
            request.setLimit(pageParamRequest.getLimit());
            
            PageInfo<CityDeliveryOrderResponse> pageInfo = cityDeliveryService.getDeliveryOrderPage(request);
            return CommonResult.success(CommonPage.restPage(pageInfo));
        } catch (Exception e) {
            log.error("获取配送员配送订单失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员配送订单失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取配送员配送订单详情（包含收入和评价信息）")
    @RequestMapping(value = "/driver/orders/detail", method = RequestMethod.GET)
    public CommonResult<List<Map<String, Object>>> getDriverDeliveryOrdersDetail(
            @RequestParam Integer driverId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            // 从数据库查询真实的配送订单数据
            LambdaQueryWrapper<CityDeliveryOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CityDeliveryOrder::getDriverId,driverId);
            wrapper.eq(CityDeliveryOrder::getIsDel, 0);
            
            if (status != null) {
                wrapper.eq(CityDeliveryOrder::getDeliveryStatus, status);
            }
            
            wrapper.orderByDesc(CityDeliveryOrder::getCreateTime);
            
            // 分页查询
            Page<CityDeliveryOrder> pageRequest = new Page<>(page, limit);
            List<CityDeliveryOrder> cityDeliveryOrders = cityDeliveryOrderService.getList(pageRequest, wrapper);
            
            List<Map<String, Object>> detailList = new ArrayList<>();
            
            for (CityDeliveryOrder order : cityDeliveryOrders) {
                Map<String, Object> detail = new HashMap<>();
                
                // 基本订单信息
                detail.put("id", order.getId());
                detail.put("deliveryOrderNo", order.getDeliveryOrderNo());
                detail.put("orderNo", order.getOrderNo());
                detail.put("pickupAddress", order.getPickupAddress());
                detail.put("deliveryAddress", order.getDeliveryAddress());
                detail.put("deliveryFee", order.getDeliveryFee());
                detail.put("deliveryStatus", order.getDeliveryStatus());
                detail.put("createTime", order.getCreateTime());
                detail.put("deliveryDistance", order.getDeliveryDistance() != null ? order.getDeliveryDistance() : order.getDistance());
                
                // 配送时间信息 - 严谨计算
                Date startTime = order.getStartDeliveryTime();
                Date endTime = order.getActualDeliveryTime();
                detail.put("startTime", startTime);
                detail.put("endTime", endTime);
                
                // 严谨的配送时长计算
                long deliveryDurationMinutes = 0;
                if (startTime != null && endTime != null) {
                    deliveryDurationMinutes = calculateDeliveryDuration(startTime, endTime);
                }
                detail.put("deliveryDuration", deliveryDurationMinutes);
                
                // 查询收入明细数据
                CityDeliveryDriverIncomeDetail incomeDetail = cityDeliveryDriverIncomeDetailService.getByDeliveryOrderNo(order.getDeliveryOrderNo());
                
                if (incomeDetail != null) {
                    // 使用真实的收入明细数据
                    detail.put("driverIncome", incomeDetail.getDriverIncome());
                    detail.put("baseIncome", incomeDetail.getBaseIncome());
                    detail.put("commissionIncome", incomeDetail.getCommissionIncome());
                    detail.put("distanceBonus", incomeDetail.getDistanceBonus());
                    detail.put("timeBonus", incomeDetail.getTimeBonus());
                    detail.put("ratingBonus", incomeDetail.getRatingBonus());
                    detail.put("otherBonus", incomeDetail.getOtherBonus());
                    detail.put("commissionRate", incomeDetail.getCommissionRate());
                    detail.put("settlementStatus", incomeDetail.getSettlementStatus());
                    detail.put("customerRating", incomeDetail.getCustomerRating());
                    detail.put("customerComment", incomeDetail.getRemark() != null ? incomeDetail.getRemark() : "暂无评价");
                } else {
                    // 如果没有收入明细，自动计算
                    BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
                    BigDecimal deliveryDistance = order.getDeliveryDistance() != null ? order.getDeliveryDistance() : 
                                                 (order.getDistance() != null ? order.getDistance() : BigDecimal.ZERO);
                    
                    Map<String, BigDecimal> incomeCalculation = calculateDriverIncome(
                        deliveryFee, deliveryDistance, deliveryDurationMinutes, driverId);
                    
                    detail.put("driverIncome", incomeCalculation.get("totalIncome"));
                    detail.put("baseIncome", incomeCalculation.get("baseIncome"));
                    detail.put("commissionIncome", incomeCalculation.get("commissionIncome"));
                    detail.put("distanceBonus", incomeCalculation.get("distanceBonus"));
                    detail.put("timeBonus", incomeCalculation.get("timeBonus"));
                    detail.put("ratingBonus", incomeCalculation.get("ratingBonus"));
                    detail.put("otherBonus", incomeCalculation.get("otherBonus"));
                    detail.put("commissionRate", BigDecimal.valueOf(15));
                    detail.put("settlementStatus", 0); // 未结算
                    detail.put("customerRating", order.getCustomerRating() != null ? BigDecimal.valueOf(order.getCustomerRating()) : BigDecimal.valueOf(5.0));
                    detail.put("customerComment", order.getCustomerFeedback() != null ? order.getCustomerFeedback() : "暂无评价");
                    
                    // 自动创建收入明细记录
                    if (order.getDeliveryStatus() == 4 || order.getDeliveryStatus() == 5) { // 已完成或已送达
                        tryCreateIncomeDetail(order, incomeCalculation);
                    }
                }
                
                // 查询客户评价数据
                CityDeliveryCustomerRating customerRating = cityDeliveryCustomerRatingService.getRatingByDeliveryOrderNo(order.getDeliveryOrderNo());
                if (customerRating != null) {
                    detail.put("customerRating", customerRating.getOverallRating());
                    detail.put("customerComment", customerRating.getComment() != null ? customerRating.getComment() : "暂无评价");
                    detail.put("serviceRating", customerRating.getServiceRating());
                    detail.put("speedRating", customerRating.getSpeedRating());
                    detail.put("qualityRating", customerRating.getQualityRating());
                }
                
                // 状态文本
                detail.put("deliveryStatusText", getDeliveryStatusText(order.getDeliveryStatus()));
                detail.put("settlementStatusText", getSettlementStatusText((Integer) detail.get("settlementStatus")));
                
                detailList.add(detail);
            }
            
            return CommonResult.success(detailList);
        } catch (Exception e) {
            log.error("获取配送员配送订单详情失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员配送订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 尝试创建收入明细记录
     */
    private void tryCreateIncomeDetail(CityDeliveryOrder order, Map<String, BigDecimal> incomeCalculation) {
        try {
            if (cityDeliveryDriverIncomeDetailService != null) {
                cityDeliveryDriverIncomeDetailService.autoCalculateIncomeDetail(order.getDeliveryOrderNo());
            }
        } catch (Exception e) {
            log.warn("自动创建收入明细失败: {}", e.getMessage());
        }
    }

    /**
     * 严谨计算配送时长（分钟）
     */
    private long calculateDeliveryDuration(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        
        try {
            // 转换为LocalDateTime进行精确计算
            LocalDateTime start = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            
            // 使用Duration进行精确时间计算
            Duration duration = Duration.between(start, end);
            return duration.toMinutes();
        } catch (Exception e) {
            // 降级到简单计算
            long durationMillis = endTime.getTime() - startTime.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        }
    }

    /**
     * 严谨的配送员收入计算
     */
    private Map<String, BigDecimal> calculateDriverIncome(BigDecimal deliveryFee, BigDecimal deliveryDistance, 
                                                         long deliveryDurationMinutes, Integer driverId) {
        Map<String, BigDecimal> result = new HashMap<>();
        
        // 1. 基础收入计算（根据区域和时间段）
        BigDecimal baseIncome = calculateBaseIncome(deliveryDistance, deliveryDurationMinutes);
        result.put("baseIncome", baseIncome);
        
        // 2. 佣金收入计算（根据配送员等级）
        BigDecimal commissionRate = getDriverCommissionRate(driverId);
        BigDecimal commissionIncome = deliveryFee.multiply(commissionRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        result.put("commissionIncome", commissionIncome);
        
        // 3. 距离奖励计算
        BigDecimal distanceBonus = calculateDistanceBonus(deliveryDistance);
        result.put("distanceBonus", distanceBonus);
        
        // 4. 时效奖励计算
        BigDecimal timeBonus = calculateTimeBonus(deliveryDurationMinutes, deliveryDistance);
        result.put("timeBonus", timeBonus);
        
        // 5. 评分奖励（暂时使用默认值）
        BigDecimal ratingBonus = BigDecimal.valueOf(2.00); // 5星评价奖励
        result.put("ratingBonus", ratingBonus);
        
        // 6. 其他奖励（夜间、恶劣天气等）
        BigDecimal otherBonus = calculateOtherBonus();
        result.put("otherBonus", otherBonus);
        
        // 7. 计算总收入
        BigDecimal totalIncome = baseIncome
                .add(commissionIncome)
                .add(distanceBonus)
                .add(timeBonus)
                .add(ratingBonus)
                .add(otherBonus);
        
        result.put("totalIncome", totalIncome);
        
        return result;
    }

    /**
     * 计算基础收入
     */
    private BigDecimal calculateBaseIncome(BigDecimal distance, long durationMinutes) {
        BigDecimal baseIncome = BigDecimal.valueOf(10.00); // 起步价10元
        
        // 根据距离调整基础收入
        if (distance.compareTo(BigDecimal.valueOf(3)) > 0) {
            // 超过3公里，每公里增加1元基础收入
            BigDecimal extraDistance = distance.subtract(BigDecimal.valueOf(3));
            baseIncome = baseIncome.add(extraDistance.multiply(BigDecimal.valueOf(1.00)));
        }
        
        // 根据配送时长调整（超过1小时增加难度费）
        if (durationMinutes > 60) {
            baseIncome = baseIncome.add(BigDecimal.valueOf(3.00));
        }
        
        return baseIncome.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取配送员佣金比例
     */
    private BigDecimal getDriverCommissionRate(Integer driverId) {
        // 实际应查询配送员等级和佣金设置
        // 这里根据配送员ID模拟不同等级
        if (driverId % 3 == 0) {
            return BigDecimal.valueOf(20.00); // 优秀配送员20%
        } else if (driverId % 2 == 0) {
            return BigDecimal.valueOf(15.00); // 普通配送员15%
        } else {
            return BigDecimal.valueOf(12.00); // 新手配送员12%
        }
    }

    /**
     * 计算距离奖励
     */
    private BigDecimal calculateDistanceBonus(BigDecimal distance) {
        BigDecimal bonus = BigDecimal.ZERO;
        
        if (distance.compareTo(BigDecimal.valueOf(5)) > 0) {
            if (distance.compareTo(BigDecimal.valueOf(10)) > 0) {
                // 超过10公里：前5公里0奖励，5-10公里每公里1元，10公里以上每公里2元
                BigDecimal over10km = distance.subtract(BigDecimal.valueOf(10));
                bonus = BigDecimal.valueOf(5).add(over10km.multiply(BigDecimal.valueOf(2))); // 5-10公里的5元 + 超过10公里的部分
            } else {
                // 5-10公里：每超过1公里奖励1元
                BigDecimal over5km = distance.subtract(BigDecimal.valueOf(5));
                bonus = over5km.multiply(BigDecimal.valueOf(1));
            }
        }
        
        return bonus.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算时效奖励
     */
    private BigDecimal calculateTimeBonus(long actualMinutes, BigDecimal distance) {
        // 根据距离计算标准配送时间
        BigDecimal standardMinutes = BigDecimal.valueOf(20).add(distance.multiply(BigDecimal.valueOf(3))); // 基础20分钟 + 每公里3分钟
        long standardTime = standardMinutes.longValue();
        
        if (actualMinutes <= standardTime * 0.8) {
            // 提前20%以上完成，奖励5元
            return BigDecimal.valueOf(5.00);
        } else if (actualMinutes <= standardTime * 0.9) {
            // 提前10%-20%完成，奖励3元
            return BigDecimal.valueOf(3.00);
        } else if (actualMinutes <= standardTime) {
            // 准时完成，奖励1元
            return BigDecimal.valueOf(1.00);
        }
        
        return BigDecimal.ZERO; // 超时无奖励
    }

    /**
     * 计算其他奖励
     */
    private BigDecimal calculateOtherBonus() {
        BigDecimal bonus = BigDecimal.ZERO;
        
        // 获取当前时间判断是否夜间配送
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 22 || hour < 6) {
            // 夜间配送（22:00-06:00）奖励3元
            bonus = bonus.add(BigDecimal.valueOf(3.00));
        }
        
        // 可以添加其他特殊情况奖励：恶劣天气、节假日等
        
        return bonus;
    }

    @ApiOperation(value = "获取配送员每日轨迹")
    @RequestMapping(value = "/driver/daily-track/{driverId}", method = RequestMethod.GET)
    public CommonResult<List<Map<String, Object>>> getDriverDailyTrack(
            @PathVariable Integer driverId,
            @RequestParam String date) {
        try {
            // 这里需要调用轨迹服务获取配送员指定日期的轨迹数据
            List<Map<String, Object>> trackData = cityDeliveryTrackingService.getDriverDailyTrack(driverId, date);
            
            return CommonResult.success(trackData);
        } catch (Exception e) {
            log.error("获取配送员每日轨迹失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员每日轨迹失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取配送员工作记录")
    @RequestMapping(value = "/driver/work-records/{driverId}", method = RequestMethod.GET)
    public CommonResult<CommonPage<Map<String, Object>>> getDriverWorkRecords(
            @PathVariable Integer driverId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @Validated PageParamRequest pageParamRequest) {
        try {
            // 使用真实的工作记录服务
            PageInfo<CityDeliveryDriverWorkRecord> pageInfo = cityDeliveryDriverWorkRecordService.getWorkRecordPage(
                    driverId, startDate, endDate, pageParamRequest);
            
            // 转换为前端需要的Map格式
            List<Map<String, Object>> workRecords = new ArrayList<>();
            for (CityDeliveryDriverWorkRecord record : pageInfo.getList()) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("date", record.getWorkDate());
                recordMap.put("checkInTime", record.getCheckInTime());
                recordMap.put("checkOutTime", record.getCheckOutTime());
                recordMap.put("workHours", record.getWorkHours());
                recordMap.put("deliveryCount", record.getTotalOrders());
                recordMap.put("completedCount", record.getCompletedOrders());
                recordMap.put("totalDistance", record.getTotalDistance());
                recordMap.put("totalIncome", record.getTotalIncome());
                recordMap.put("commission", record.getCommissionIncome());
                recordMap.put("violationCount", record.getViolationCount());
                recordMap.put("averageRating", record.getAverageRating());
                recordMap.put("onlineMinutes", record.getOnlineMinutes());
                recordMap.put("workArea", record.getWorkArea());
                recordMap.put("maxDeliveryDistance", record.getMaxDeliveryDistance());
                recordMap.put("avgDeliveryTime", record.getAvgDeliveryTime());
                recordMap.put("efficiencyRating", record.getEfficiencyRating());
                recordMap.put("exceptionCount", record.getExceptionCount());
                recordMap.put("timeoutCount", record.getTimeoutCount());
                recordMap.put("complaintCount", record.getComplaintCount());
                workRecords.add(recordMap);
            }
            
            // 创建新的分页信息
            PageInfo<Map<String, Object>> resultPageInfo = new PageInfo<>(workRecords);
            resultPageInfo.setPageNum(pageInfo.getPageNum());
            resultPageInfo.setPageSize(pageInfo.getPageSize());
            resultPageInfo.setTotal(pageInfo.getTotal());
            resultPageInfo.setPages(pageInfo.getPages());
            
            return CommonResult.success(CommonPage.restPage(resultPageInfo));
        } catch (Exception e) {
            log.error("获取配送员工作记录失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员工作记录失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取配送员收入统计")
    @RequestMapping(value = "/driver/income-stats/{driverId}", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getDriverIncomeStats(
            @PathVariable Integer driverId,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        try {
            // 计算日期范围
            Date endDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_MONTH, -days + 1);
            Date startDate = calendar.getTime();
            
            String startDateStr = DateUtil.format(startDate, "yyyy-MM-dd");
            String endDateStr = DateUtil.format(endDate, "yyyy-MM-dd");
            
            // 使用真实的收入统计服务
            Map<String, Object> incomeStats = cityDeliveryDriverWorkRecordService.getDriverIncomeStats(
                    driverId, startDateStr, endDateStr);
            
            // 获取工作统计
            Map<String, Object> workStats = cityDeliveryDriverWorkRecordService.getDriverWorkStats(driverId, days);
            
            // 合并统计数据
            Map<String, Object> result = new HashMap<>();
            result.putAll(incomeStats);
            result.putAll(workStats);
            
            // 获取最近几日的详细收入数据
            List<CityDeliveryDriverWorkRecord> recentRecords = cityDeliveryDriverWorkRecordService
                    .getRecentWorkRecords(driverId, days);
            
            List<Map<String, Object>> dailyIncome = new ArrayList<>();
            for (CityDeliveryDriverWorkRecord record : recentRecords) {
                Map<String, Object> daily = new HashMap<>();
                daily.put("date", DateUtil.format(record.getWorkDate(), "yyyy-MM-dd"));
                daily.put("income", record.getTotalIncome() != null ? record.getTotalIncome() : BigDecimal.ZERO);
                daily.put("orders", record.getTotalOrders() != null ? record.getTotalOrders() : 0);
                daily.put("commission", record.getCommissionIncome() != null ? record.getCommissionIncome() : BigDecimal.ZERO);
                dailyIncome.add(daily);
            }
            result.put("dailyIncome", dailyIncome);
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取配送员收入统计失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员收入统计失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取订单详细轨迹")
    @RequestMapping(value = "/order/track/{deliveryOrderNo}", method = RequestMethod.GET)
    public CommonResult<List<Map<String, Object>>> getOrderTrack(@PathVariable String deliveryOrderNo) {
        try {
            // 验证商户权限
            SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
            CityDeliveryOrderResponse deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
            
            if (deliveryOrder == null) {
                return CommonResult.failed("配送订单不存在");
            }
            
            if (!deliveryOrder.getMerId().equals(systemAdmin.getMerId())) {
                return CommonResult.failed("无权限访问该配送订单轨迹");
            }
            
            // 获取订单轨迹
            List<Map<String, Object>> trackList = cityDeliveryService.getDeliveryTrack(deliveryOrderNo);
            
            return CommonResult.success(trackList);
        } catch (Exception e) {
            log.error("获取订单详细轨迹失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取订单详细轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 获取配送状态文本
     */
    private String getDeliveryStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待接单";
            case 1: return "已接单";
            case 2: return "取件中";
            case 3: return "配送中";
            case 4: return "已送达";
            case 5: return "配送失败";
            case 9: return "已取消";
            default: return "未知状态";
        }
    }

    /**
     * 获取结算状态文本
     */
    private String getSettlementStatusText(Integer status) {
        if (status == null) return "未结算";
        switch (status) {
            case 0: return "未结算";
            case 1: return "已结算";
            case 2: return "已提现";
            default: return "未知状态";
        }
    }

    @ApiOperation(value = "获取配送员收入明细列表")
    @RequestMapping(value = "/driver/income-detail/{driverId}", method = RequestMethod.GET)
    public CommonResult<CommonPage<Map<String, Object>>> getDriverIncomeDetail(
            @PathVariable Integer driverId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @Validated PageParamRequest pageParamRequest) {
        try {
            // 使用分页查询收入明细
            Page<CityDeliveryDriverIncomeDetail> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
            
            LambdaQueryWrapper<CityDeliveryDriverIncomeDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CityDeliveryDriverIncomeDetail::getDriverId, driverId)
                   .eq(CityDeliveryDriverIncomeDetail::getIsDel, 0);
            
            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge(CityDeliveryDriverIncomeDetail::getCreateTime, startDate);
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le(CityDeliveryDriverIncomeDetail::getCreateTime, endDate);
            }
            
            wrapper.orderByDesc(CityDeliveryDriverIncomeDetail::getCreateTime);
            
            List<CityDeliveryDriverIncomeDetail> incomeDetails = cityDeliveryDriverIncomeDetailService.list(wrapper);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> result = incomeDetails.stream().map(detail -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", detail.getId());
                item.put("deliveryOrderNo", detail.getDeliveryOrderNo());
                item.put("orderNo", detail.getOrderNo());
                item.put("driverIncome", detail.getDriverIncome());
                item.put("baseIncome", detail.getBaseIncome());
                item.put("commissionIncome", detail.getCommissionIncome());
                item.put("distanceBonus", detail.getDistanceBonus());
                item.put("timeBonus", detail.getTimeBonus());
                item.put("ratingBonus", detail.getRatingBonus());
                item.put("otherBonus", detail.getOtherBonus());
                item.put("commissionRate", detail.getCommissionRate());
                item.put("settlementStatus", detail.getSettlementStatus());
                item.put("settlementStatusText", getSettlementStatusText(detail.getSettlementStatus()));
                item.put("createTime", detail.getCreateTime());
                item.put("customerRating", detail.getCustomerRating());
                item.put("remark", detail.getRemark());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(result);
            pageInfo.setPageNum(page.getPageNum());
            pageInfo.setPageSize(page.getPageSize());
            pageInfo.setTotal(page.getTotal());
            pageInfo.setPages(page.getPages());
            
            return CommonResult.success(CommonPage.restPage(pageInfo));
        } catch (Exception e) {
            log.error("获取配送员收入明细失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员收入明细失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取配送员评价记录")
    @RequestMapping(value = "/driver/rating-records/{driverId}", method = RequestMethod.GET)
    public CommonResult<CommonPage<Map<String, Object>>> getDriverRatingRecords(
            @PathVariable Integer driverId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @Validated PageParamRequest pageParamRequest) {
        try {
            // 使用分页查询评价记录
            Page<CityDeliveryCustomerRating> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
            
            LambdaQueryWrapper<CityDeliveryCustomerRating> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CityDeliveryCustomerRating::getDriverId, driverId)
                   .eq(CityDeliveryCustomerRating::getIsDel, false)
                   .eq(CityDeliveryCustomerRating::getAuditStatus, 1); // 只显示审核通过的评价
            
            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge(CityDeliveryCustomerRating::getCreateTime, startDate);
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le(CityDeliveryCustomerRating::getCreateTime, endDate);
            }
            
            wrapper.orderByDesc(CityDeliveryCustomerRating::getCreateTime);
            
            List<CityDeliveryCustomerRating> ratings = cityDeliveryCustomerRatingService.list(wrapper);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> result = ratings.stream().map(rating -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rating.getId());
                item.put("deliveryOrderNo", rating.getDeliveryOrderNo());
                item.put("orderNo", rating.getOrderNo());
                item.put("customerName", rating.getCustomerName());
                item.put("overallRating", rating.getOverallRating());
                item.put("serviceRating", rating.getServiceRating());
                item.put("speedRating", rating.getSpeedRating());
                item.put("qualityRating", rating.getQualityRating());
                item.put("comment", rating.getComment());
                item.put("commentTags", rating.getCommentTags());
                item.put("ratingImages", rating.getRatingImages());
                item.put("createTime", rating.getCreateTime());
                item.put("onTimeDelivery", rating.getOnTimeDelivery());
                item.put("packagingIntact", rating.getPackagingIntact());
                item.put("wouldRecommend", rating.getWouldRecommend());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(result);
            pageInfo.setPageNum(page.getPageNum());
            pageInfo.setPageSize(page.getPageSize());
            pageInfo.setTotal(page.getTotal());
            pageInfo.setPages(page.getPages());
            
            return CommonResult.success(CommonPage.restPage(pageInfo));
        } catch (Exception e) {
            log.error("获取配送员评价记录失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取配送员评价记录失败: " + e.getMessage());
        }
    }

} 