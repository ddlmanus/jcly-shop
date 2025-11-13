package com.zbkj.admin.controller.platform;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.task.ProductDataFixTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品数据修复控制器
 * 用于手动触发商品数据修复任务
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/product/fix")
@Api(tags = "平台端-商品数据修复")
public class ProductDataFixController {

    @Autowired
    private ProductDataFixTask productDataFixTask;

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "手动修复商品数据")
    @PreAuthorize("hasAuthority('platform:product:data:fix')")
    @ApiOperation(value = "手动修复商品数据")
    @RequestMapping(value = "/manual", method = RequestMethod.POST)
    public CommonResult<String> manualFix() {
        log.info("接收到手动修复商品数据的请求");

        try {
            String report = productDataFixTask.manualFix();
            return CommonResult.success(report);
        } catch (Exception e) {
            log.error("手动修复商品数据失败", e);
            return CommonResult.failed("修复任务执行失败: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('platform:product:data:fix')")
    @ApiOperation(value = "修复分类ID")
    @RequestMapping(value = "/category", method = RequestMethod.POST)
    public CommonResult<String> fixCategory() {
        log.info("接收到修复分类ID的请求");

        try {
            int count = productDataFixTask.fixCategoryId();
            return CommonResult.success("修复完成,共修复 " + count + " 个商品的分类ID");
        } catch (Exception e) {
            log.error("修复分类ID失败", e);
            return CommonResult.failed("修复失败: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('platform:product:data:fix')")
    @ApiOperation(value = "修复图片路径")
    @RequestMapping(value = "/image", method = RequestMethod.POST)
    public CommonResult<String> fixImage() {
        log.info("接收到修复图片路径的请求");

        try {
            int count = productDataFixTask.fixImagePath();
            return CommonResult.success("修复完成,共修复 " + count + " 个商品的图片路径");
        } catch (Exception e) {
            log.error("修复图片路径失败", e);
            return CommonResult.failed("修复失败: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('platform:product:data:fix')")
    @ApiOperation(value = "补充商品详情")
    @RequestMapping(value = "/description", method = RequestMethod.POST)
    public CommonResult<String> fixDescription() {
        log.info("接收到补充商品详情的请求");

        try {
            int count = productDataFixTask.fixProductDescription();
            return CommonResult.success("补充完成,共补充 " + count + " 个商品的详情");
        } catch (Exception e) {
            log.error("补充商品详情失败", e);
            return CommonResult.failed("补充失败: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('platform:product:data:fix')")
    @ApiOperation(value = "同步SKU图片")
    @RequestMapping(value = "/sku/image", method = RequestMethod.POST)
    public CommonResult<String> syncSkuImage() {
        log.info("接收到同步SKU图片的请求");

        try {
            int count = productDataFixTask.syncSkuImage();
            return CommonResult.success("同步完成,共同步 " + count + " 个SKU的图片");
        } catch (Exception e) {
            log.error("同步SKU图片失败", e);
            return CommonResult.failed("同步失败: " + e.getMessage());
        }
    }
}
