package com.zbkj.admin.controller.platform;

import cn.hutool.core.collection.CollUtil;
import com.zbkj.common.annotation.CustomResponseAnnotation;
import com.zbkj.common.request.OrderSearchRequest;
import com.zbkj.common.request.PlatProductSearchRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.ExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


/**
 *  平台端导出控制器
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2022 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/export")
@Api(tags = "平台端导出控制器")
public class PlatformExportController {

    @Autowired
    private ExportService exportService;

    @PreAuthorize("hasAuthority('platform:export:order:excel')")
    @ApiOperation(value = "导出订单Excel")
    @RequestMapping(value = "/order/excel", method = RequestMethod.GET)
    public CommonResult<HashMap<String, String>> exportOrder(@Validated OrderSearchRequest request){
        String fileName = exportService.exportOrder(request);
        HashMap<String, String> map = CollUtil.newHashMap();
        map.put("fileName", fileName);
        return CommonResult.success(map);
    }

    @CustomResponseAnnotation
    @PreAuthorize("hasAuthority('platform:export:product:excel')")
    @ApiOperation(value = "导出商品Excel")
    @RequestMapping(value = "/product/excel", method = RequestMethod.GET)
    public CommonResult<String> exportProduct(@Validated PlatProductSearchRequest request, HttpServletResponse response) throws IOException {
        // 直接使用EasyExcel写入response输出流
       return CommonResult.success(exportService.exportProductToStream(request,response));
    }

}



