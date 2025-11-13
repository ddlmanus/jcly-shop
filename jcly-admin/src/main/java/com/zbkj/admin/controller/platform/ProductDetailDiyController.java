package com.zbkj.admin.controller.platform;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.page.PageDiy;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.page.PageDiyEditNameRequest;
import com.zbkj.common.request.page.PageDiyRequest;
import com.zbkj.common.response.page.PageDiyResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.PageDiyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 商品详情装修 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/product-detail-diy")
@Api(tags = "商品详情装修 控制器")
public class ProductDetailDiyController {

    @Autowired
    private PageDiyService pageDiyService;

    // 商品详情装修类型
    private static final Integer PRODUCT_DETAIL_TYPE = 2;

    /**
     * 商品详情装修方案列表
     */
   // @PreAuthorize("hasAuthority('platform:product:detail:diy:list')")
    @ApiOperation(value = "商品详情装修方案列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "搜索关键字", dataType = "String")
    })
    public CommonResult<CommonPage<PageDiy>> getList(@RequestParam(defaultValue = "") String name, PageParamRequest pageParamRequest) {
        // 只查询type=2（商品详情装修）的数据
        List<PageDiy> allList = pageDiyService.getList(name, pageParamRequest);
        List<PageDiy> filteredList = allList.stream()
                .filter(item -> PRODUCT_DETAIL_TYPE.equals(item.getType()))
                .collect(Collectors.toList());
        
        CommonPage<PageDiy> pageDiyCommonPage = CommonPage.restPage(filteredList);
        return CommonResult.success(pageDiyCommonPage);
    }

    /**
     * 新增商品详情装修方案
     */
   // @PreAuthorize("hasAuthority('platform:product:detail:diy:add')")
    @ApiOperation(value = "新增商品详情装修方案")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<PageDiy> save(@RequestBody @Validated PageDiyRequest pageDiyRequest) {
        PageDiy pageDiy = new PageDiy();
        BeanUtils.copyProperties(pageDiyRequest, pageDiy);
        
        // 设置类型为商品详情装修
        pageDiy.setType(PRODUCT_DETAIL_TYPE);
        
        // 如果传入了value，则转换为JSON字符串
        if (pageDiyRequest.getValue() != null) {
            pageDiy.setValue(JSON.toJSONString(pageDiyRequest.getValue()));
        } else {
            // 设置空配置
            JSONObject emptyConfig = new JSONObject();
            pageDiy.setValue(emptyConfig.toJSONString());
        }
        
        return CommonResult.success(pageDiyService.savePageDiy(pageDiy));
    }

    /**
     * 更新商品详情装修方案配置
     */
  //  @PreAuthorize("hasAuthority('platform:product:detail:diy:update')")
    @ApiOperation(value = "更新商品详情装修方案配置")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated PageDiyRequest pageDiyRequest) {
        PageDiy pageDiy = new PageDiy();
        BeanUtils.copyProperties(pageDiyRequest, pageDiy);
        pageDiy.setValue(JSON.toJSONString(pageDiyRequest.getValue()));
        
        if (pageDiyService.editPageDiy(pageDiy)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 删除商品详情装修方案
     */
  //  @PreAuthorize("hasAuthority('platform:product:detail:diy:delete')")
    @ApiOperation(value = "删除商品详情装修方案")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        PageDiy existingDiy = pageDiyService.getById(id);
        if (existingDiy == null || !PRODUCT_DETAIL_TYPE.equals(existingDiy.getType())) {
            return CommonResult.failed("装修方案不存在");
        }
        
        // 如果是默认方案，不允许删除
        if (Integer.valueOf(1).equals(existingDiy.getIsDefault())) {
            return CommonResult.failed("默认方案不允许删除");
        }
        
        if (pageDiyService.removeById(id)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 设置为默认商品详情装修方案
     */
 //   @PreAuthorize("hasAuthority('platform:product:detail:diy:setdefault')")
    @ApiOperation(value = "设置为默认商品详情装修方案")
    @RequestMapping(value = "/setdefault/{id}", method = RequestMethod.GET)
    public CommonResult<String> setDefault(@PathVariable(value = "id") Integer id) {
        if (pageDiyService.setProductDetailDiyDefault(id)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 获取商品详情装修方案详情
     */
   // @PreAuthorize("hasAuthority('platform:product:detail:diy:config')")
    @ApiOperation(value = "获取商品详情装修方案详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<PageDiyResponse> info(@PathVariable(value = "id") Integer id) {
        PageDiy pageDiy = pageDiyService.getDiyPageByPageIdForAdmin(id);
        if (ObjectUtil.isNull(pageDiy)) {
            throw new CrmebException("未找到对应模版信息");
        }
        
        PageDiyResponse response = new PageDiyResponse();
        BeanUtils.copyProperties(pageDiy, response);
        response.setValue(JSON.parseObject(pageDiy.getValue()));
        return CommonResult.success(response);
    }

    /**
     * 获取默认商品详情装修方案ID
     */
    @ApiOperation(value = "获取默认商品详情装修方案ID")
    @RequestMapping(value = "/getdefault", method = RequestMethod.GET)
    public CommonResult<Integer> getDefault() {
        return CommonResult.success(pageDiyService.getDefaultId());
    }
}

