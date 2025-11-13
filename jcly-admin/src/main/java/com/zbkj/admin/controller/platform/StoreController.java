package com.zbkj.admin.controller.platform;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.platform.Store;
import com.zbkj.common.request.StoreRequest;
import com.zbkj.common.request.StoreSearchRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.service.StoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台门店管理控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/store")
@Api(tags = "平台门店管理")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @ApiOperation(value = "分页获取门店列表", notes = "支持通过城市ID(cityId)进行精确查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<PageInfo<Store>> getPageList(@Validated StoreSearchRequest request) {
        PageInfo<Store> pageInfo = storeService.getPageList(request);
        return CommonResult.success(pageInfo);
    }

    @ApiOperation(value = "根据商户ID获取门店列表")
    @RequestMapping(value = "/list/merchant/{merId}", method = RequestMethod.GET)
    public CommonResult<List<Store>> getStoreListByMerId(@PathVariable Integer merId) {
        List<Store> list = storeService.getStoreListByMerId(merId);
        return CommonResult.success(list);
    }

    @ApiOperation(value = "根据城市ID获取门店列表")
    @RequestMapping(value = "/list/city/{cityId}", method = RequestMethod.GET)
    public CommonResult<List<Store>> getStoreListByCityId(@PathVariable Long cityId) {
        StoreSearchRequest request = new StoreSearchRequest();
        request.setCityId(cityId);
        request.setPage(1);
        request.setLimit(1000); // 获取该城市下所有门店
        PageInfo<Store> pageInfo = storeService.getPageList(request);
        return CommonResult.success(pageInfo.getList());
    }

    @ApiOperation(value = "获取门店详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<Store> getStoreDetail(@PathVariable Integer id) {
        Store store = storeService.getStoreDetail(id);
        return CommonResult.success(store);
    }

    @ApiOperation(value = "创建门店")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<Store> createStore(@RequestBody @Validated StoreRequest request) {
        MyRecord record = storeService.createStore(request);
        if (record.getBoolean("status")) {
            return CommonResult.success((Store) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    @ApiOperation(value = "更新门店")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public CommonResult<Store> updateStore(@PathVariable Integer id,
                                          @RequestBody @Validated StoreRequest request) {
        MyRecord record = storeService.updateStore(id, request);
        if (record.getBoolean("status")) {
            return CommonResult.success((Store) record.get("data")).setMessage(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    @ApiOperation(value = "删除门店")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<String> deleteStore(@PathVariable Integer id) {
        MyRecord record = storeService.deleteStore(id);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    @ApiOperation(value = "批量更新门店状态")
    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public CommonResult<String> batchUpdateStatus(@RequestBody @Validated List<Integer> ids, 
                                                 @RequestParam Integer status) {
        MyRecord record = storeService.batchUpdateStatus(ids, status);
        if (record.getBoolean("status")) {
            return CommonResult.success(record.getStr("msg"));
        } else {
            return CommonResult.failed(record.getStr("msg"));
        }
    }

    @ApiOperation(value = "获取附近的门店")
    @RequestMapping(value = "/nearby", method = RequestMethod.GET)
    public CommonResult<List<Store>> getNearbyStores(@RequestParam Double latitude,
                                                   @RequestParam Double longitude,
                                                   @RequestParam(required = false, defaultValue = "5.0") Double radius,
                                                   @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Store> list = storeService.getNearbyStores(latitude, longitude, radius, limit);
        return CommonResult.success(list);
    }
}