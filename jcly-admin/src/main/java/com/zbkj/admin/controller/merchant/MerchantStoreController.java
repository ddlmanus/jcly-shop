package com.zbkj.admin.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.model.merchant.MerchantStore;
import com.zbkj.common.model.merchant.MerchantStoreDeliveryArea;
import com.zbkj.common.model.merchant.MerchantStoreHours;
import com.zbkj.common.model.merchant.MerchantStoreStaff;
import com.zbkj.common.request.MerchantStoreRequest;
import com.zbkj.common.request.MerchantStoreSearchRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.service.MerchantStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商户门店管理控制器
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/store")
@Api(tags = "商户门店管理")
public class MerchantStoreController {

    @Autowired
    private MerchantStoreService merchantStoreService;

    /**
     * 门店分页列表
     */
   // @PreAuthorize("hasAuthority('merchant:store:list')")
    @ApiOperation(value = "门店分页列表")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "门店分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<PageInfo<MerchantStore>> getPageList(@Validated MerchantStoreSearchRequest request) {
        PageInfo<MerchantStore> pageInfo = merchantStoreService.getPageList(request);
        return CommonResult.success(pageInfo);
    }

    /**
     * 根据商户ID获取门店列表
     */
   // @PreAuthorize("hasAuthority('merchant:store:list')")
    @ApiOperation(value = "根据商户ID获取门店列表")
    @ApiImplicitParam(name = "merId", value = "商户ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/list/merchant/{merId}", method = RequestMethod.GET)
    public CommonResult<List<MerchantStore>> getStoreListByMerId(@PathVariable Integer merId) {
        List<MerchantStore> list = merchantStoreService.getStoreListByMerId(merId);
        return CommonResult.success(list);
    }

    /**
     * 获取商户的主门店
     */
   // @PreAuthorize("hasAuthority('merchant:store:list')")
    @ApiOperation(value = "获取商户的主门店")
    @ApiImplicitParam(name = "merId", value = "商户ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/main/merchant/{merId}", method = RequestMethod.GET)
    public CommonResult<MerchantStore> getMainStoreByMerId(@PathVariable Integer merId) {
        MerchantStore store = merchantStoreService.getMainStoreByMerId(merId);
        return CommonResult.success(store);
    }

    /**
     * 门店详情
     */
  //  @PreAuthorize("hasAuthority('merchant:store:info')")
    @ApiOperation(value = "门店详情")
    @ApiImplicitParam(name = "id", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<MerchantStore> getStoreDetail(@PathVariable Integer id) {
        MerchantStore store = merchantStoreService.getStoreDetail(id);
        return CommonResult.success(store);
    }

    /**
     * 创建门店
     */
  //  @PreAuthorize("hasAuthority('merchant:store:save')")
    @ApiOperation(value = "创建门店")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "创建门店")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<MerchantStore> createStore(@RequestBody @Validated MerchantStoreRequest request) {
        MyRecord record = merchantStoreService.createStore(request);
        if (record.getBoolean("status")) {
            return CommonResult.success((MerchantStore) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 更新门店信息
     */
  //  @PreAuthorize("hasAuthority('merchant:store:update')")
    @ApiOperation(value = "更新门店信息")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新门店信息")
    @ApiImplicitParam(name = "id", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public CommonResult<MerchantStore> updateStore(@PathVariable Integer id, 
                                                  @RequestBody @Validated MerchantStoreRequest request) {
        MyRecord record = merchantStoreService.updateStore(id, request);
        if (record.getBoolean("status")) {
            return CommonResult.success((MerchantStore) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 删除门店
     */
  //  @PreAuthorize("hasAuthority('merchant:store:delete')")
    @ApiOperation(value = "删除门店")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除门店")
    @ApiImplicitParam(name = "id", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<String> deleteStore(@PathVariable Integer id) {
        MyRecord record = merchantStoreService.deleteStore(id);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 批量删除门店
     */
   // @PreAuthorize("hasAuthority('merchant:store:delete')")
    @ApiOperation(value = "批量删除门店")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "批量删除门店")
    @RequestMapping(value = "/delete/batch", method = RequestMethod.DELETE)
    public CommonResult<String> batchDeleteStore(@RequestBody List<Integer> ids) {
        MyRecord record = merchantStoreService.batchDeleteStore(ids);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 更新门店状态
     */
  //  @PreAuthorize("hasAuthority('merchant:store:status')")
    @ApiOperation(value = "更新门店状态")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新门店状态")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "门店ID", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "status", value = "状态值", required = true, dataType = "Integer")
    })
    @RequestMapping(value = "/status/{id}/{status}", method = RequestMethod.PUT)
    public CommonResult<String> updateStoreStatus(@PathVariable Integer id, @PathVariable Integer status) {
        MyRecord record = merchantStoreService.updateStoreStatus(id, status);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 批量更新门店状态
     */
   // @PreAuthorize("hasAuthority('merchant:store:status')")
    @ApiOperation(value = "批量更新门店状态")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "批量更新门店状态")
    @ApiImplicitParam(name = "status", value = "状态值", required = true, dataType = "Integer")
    @RequestMapping(value = "/status/batch/{status}", method = RequestMethod.PUT)
    public CommonResult<String> batchUpdateStoreStatus(@RequestBody List<Integer> ids, @PathVariable Integer status) {
        MyRecord record = merchantStoreService.batchUpdateStoreStatus(ids, status);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 设置主门店
     */
  //  @PreAuthorize("hasAuthority('merchant:store:main')")
    @ApiOperation(value = "设置主门店")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "设置主门店")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "merId", value = "商户ID", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer")
    })
    @RequestMapping(value = "/main/{merId}/{storeId}", method = RequestMethod.PUT)
    public CommonResult<String> setMainStore(@PathVariable Integer merId, @PathVariable Integer storeId) {
        MyRecord record = merchantStoreService.setMainStore(merId, storeId);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 根据位置查找附近的门店
     */
  //  @PreAuthorize("hasAuthority('merchant:store:list')")
    @ApiOperation(value = "根据位置查找附近的门店")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "latitude", value = "纬度", required = true, dataType = "Double"),
        @ApiImplicitParam(name = "longitude", value = "经度", required = true, dataType = "Double"),
        @ApiImplicitParam(name = "radius", value = "搜索半径（千米）", dataType = "Double"),
        @ApiImplicitParam(name = "limit", value = "返回数量限制", dataType = "Integer")
    })
    @RequestMapping(value = "/nearby", method = RequestMethod.GET)
    public CommonResult<List<MerchantStore>> getNearbyStores(@RequestParam Double latitude,
                                                            @RequestParam Double longitude,
                                                            @RequestParam(required = false) Double radius,
                                                            @RequestParam(required = false) Integer limit) {
        List<MerchantStore> list = merchantStoreService.getNearbyStores(latitude, longitude, radius, limit);
        return CommonResult.success(list);
    }

    /**
     * 获取门店统计信息
     */
  //  @PreAuthorize("hasAuthority('merchant:store:statistics')")
    @ApiOperation(value = "获取门店统计信息")
    @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/statistics/{storeId}", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getStoreStatistics(@PathVariable Integer storeId) {
        Map<String, Object> statistics = merchantStoreService.getStoreStatistics(storeId);
        return CommonResult.success(statistics);
    }

    /**
     * 获取商户门店数量统计
     */
  //  @PreAuthorize("hasAuthority('merchant:store:statistics')")
    @ApiOperation(value = "获取商户门店数量统计")
    @ApiImplicitParam(name = "merId", value = "商户ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/count/{merId}", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getMerchantStoreCount(@PathVariable Integer merId) {
        Map<String, Object> count = merchantStoreService.getMerchantStoreCount(merId);
        return CommonResult.success(count);
    }

    /**
     * 检查门店编码是否已存在
     */
  //  @PreAuthorize("hasAuthority('merchant:store:list')")
    @ApiOperation(value = "检查门店编码是否已存在")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "merId", value = "商户ID", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "storeCode", value = "门店编码", required = true, dataType = "String"),
        @ApiImplicitParam(name = "excludeId", value = "排除的门店ID", dataType = "Integer")
    })
    @RequestMapping(value = "/check/code", method = RequestMethod.GET)
    public CommonResult<Boolean> checkStoreCodeExists(@RequestParam Integer merId,
                                                     @RequestParam String storeCode,
                                                     @RequestParam(required = false) Integer excludeId) {
        boolean exists = merchantStoreService.checkStoreCodeExists(merId, storeCode, excludeId);
        return CommonResult.success(exists);
    }

    /**
     * 生成门店编码
     */
  //  @PreAuthorize("hasAuthority('merchant:store:save')")
    @ApiOperation(value = "生成门店编码")
    @ApiImplicitParam(name = "merId", value = "商户ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/generate/code/{merId}", method = RequestMethod.GET)
    public CommonResult<String> generateStoreCode(@PathVariable Integer merId) {
        String storeCode = merchantStoreService.generateStoreCode(merId);
        return CommonResult.success(storeCode);
    }

    // 营业时间管理
    /**
     * 设置门店营业时间
     */
  //  @PreAuthorize("hasAuthority('merchant:store:hours')")
    @ApiOperation(value = "设置门店营业时间")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "设置门店营业时间")
    @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/hours/{storeId}", method = RequestMethod.POST)
    public CommonResult<String> setStoreHours(@PathVariable Integer storeId, 
                                             @RequestBody List<MerchantStoreHours> storeHours) {
        MyRecord record = merchantStoreService.setStoreHours(storeId, storeHours);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 获取门店营业时间
     */
 //   @PreAuthorize("hasAuthority('merchant:store:hours')")
    @ApiOperation(value = "获取门店营业时间")
    @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/hours/{storeId}", method = RequestMethod.GET)
    public CommonResult<List<MerchantStoreHours>> getStoreHours(@PathVariable Integer storeId) {
        List<MerchantStoreHours> hours = merchantStoreService.getStoreHours(storeId);
        return CommonResult.success(hours);
    }

    // 员工管理
    /**
     * 添加门店员工
     */
   // @PreAuthorize("hasAuthority('merchant:store:staff')")
    @ApiOperation(value = "添加门店员工")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "添加门店员工")
    @RequestMapping(value = "/staff/save", method = RequestMethod.POST)
    public CommonResult<MerchantStoreStaff> addStoreStaff(@RequestBody @Validated MerchantStoreStaff storeStaff) {
        MyRecord record = merchantStoreService.addStoreStaff(storeStaff);
        if (record.getBoolean("status")) {
            return CommonResult.success((MerchantStoreStaff) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 更新门店员工
     */
   // @PreAuthorize("hasAuthority('merchant:store:staff')")
    @ApiOperation(value = "更新门店员工")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新门店员工")
    @RequestMapping(value = "/staff/update", method = RequestMethod.PUT)
    public CommonResult<MerchantStoreStaff> updateStoreStaff(@RequestBody @Validated MerchantStoreStaff storeStaff) {
        MyRecord record = merchantStoreService.updateStoreStaff(storeStaff);
        if (record.getBoolean("status")) {
            return CommonResult.success((MerchantStoreStaff) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 删除门店员工
     */
 //   @PreAuthorize("hasAuthority('merchant:store:staff')")
    @ApiOperation(value = "删除门店员工")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除门店员工")
    @ApiImplicitParam(name = "staffId", value = "员工ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/staff/delete/{staffId}", method = RequestMethod.DELETE)
    public CommonResult<String> deleteStoreStaff(@PathVariable Integer staffId) {
        MyRecord record = merchantStoreService.deleteStoreStaff(staffId);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 获取门店员工列表
     */
 //   @PreAuthorize("hasAuthority('merchant:store:staff')")
    @ApiOperation(value = "获取门店员工列表")
    @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/staff/{storeId}", method = RequestMethod.GET)
    public CommonResult<List<MerchantStoreStaff>> getStoreStaff(@PathVariable Integer storeId) {
        List<MerchantStoreStaff> staff = merchantStoreService.getStoreStaff(storeId);
        return CommonResult.success(staff);
    }

    // 配送范围管理
    /**
     * 添加配送范围
     */
  //  @PreAuthorize("hasAuthority('merchant:store:delivery')")
    @ApiOperation(value = "添加配送范围")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "添加配送范围")
    @RequestMapping(value = "/delivery/save", method = RequestMethod.POST)
    public CommonResult<MerchantStoreDeliveryArea> addDeliveryArea(@RequestBody @Validated MerchantStoreDeliveryArea deliveryArea) {
        MyRecord record = merchantStoreService.addDeliveryArea(deliveryArea);
        if (record.getBoolean("status")) {
            return CommonResult.success((MerchantStoreDeliveryArea) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 更新配送范围
     */
 //   @PreAuthorize("hasAuthority('merchant:store:delivery')")
    @ApiOperation(value = "更新配送范围")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新配送范围")
    @RequestMapping(value = "/delivery/update", method = RequestMethod.PUT)
    public CommonResult<MerchantStoreDeliveryArea> updateDeliveryArea(@RequestBody @Validated MerchantStoreDeliveryArea deliveryArea) {
        MyRecord record = merchantStoreService.updateDeliveryArea(deliveryArea);
        if (record.getBoolean("status")) {
            return CommonResult.success((MerchantStoreDeliveryArea) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 删除配送范围
     */
 //   @PreAuthorize("hasAuthority('merchant:store:delivery')")
    @ApiOperation(value = "删除配送范围")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除配送范围")
    @ApiImplicitParam(name = "areaId", value = "配送范围ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/delivery/delete/{areaId}", method = RequestMethod.DELETE)
    public CommonResult<String> deleteDeliveryArea(@PathVariable Integer areaId) {
        MyRecord record = merchantStoreService.deleteDeliveryArea(areaId);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    /**
     * 获取门店配送范围列表
     */
  //  @PreAuthorize("hasAuthority('merchant:store:delivery')")
    @ApiOperation(value = "获取门店配送范围列表")
    @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/delivery/{storeId}", method = RequestMethod.GET)
    public CommonResult<List<MerchantStoreDeliveryArea>> getStoreDeliveryAreas(@PathVariable Integer storeId) {
        List<MerchantStoreDeliveryArea> areas = merchantStoreService.getStoreDeliveryAreas(storeId);
        return CommonResult.success(areas);
    }

    /**
     * 检查指定位置是否在配送范围内
     */
  //  @PreAuthorize("hasAuthority('merchant:store:delivery')")
    @ApiOperation(value = "检查指定位置是否在配送范围内")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "latitude", value = "纬度", required = true, dataType = "BigDecimal"),
        @ApiImplicitParam(name = "longitude", value = "经度", required = true, dataType = "BigDecimal")
    })
    @RequestMapping(value = "/delivery/check/{storeId}", method = RequestMethod.GET)
    public CommonResult<MerchantStoreDeliveryArea> checkLocationInDeliveryArea(@PathVariable Integer storeId,
                                                                              @RequestParam BigDecimal latitude,
                                                                              @RequestParam BigDecimal longitude) {
        MerchantStoreDeliveryArea area = merchantStoreService.checkLocationInDeliveryArea(storeId, latitude, longitude);
        return CommonResult.success(area);
    }

    /**
     * 计算配送费用
     */
   // @PreAuthorize("hasAuthority('merchant:store:delivery')")
    @ApiOperation(value = "计算配送费用")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "storeId", value = "门店ID", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "latitude", value = "纬度", required = true, dataType = "BigDecimal"),
        @ApiImplicitParam(name = "longitude", value = "经度", required = true, dataType = "BigDecimal"),
        @ApiImplicitParam(name = "orderAmount", value = "订单金额", required = true, dataType = "BigDecimal")
    })
    @RequestMapping(value = "/delivery/fee/{storeId}", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> calculateDeliveryFee(@PathVariable Integer storeId,
                                                                 @RequestParam BigDecimal latitude,
                                                                 @RequestParam BigDecimal longitude,
                                                                 @RequestParam BigDecimal orderAmount) {
        Map<String, Object> fee = merchantStoreService.calculateDeliveryFee(storeId, latitude, longitude, orderAmount);
        return CommonResult.success(fee);
    }
} 