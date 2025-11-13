package com.zbkj.admin.controller.platform;

import com.zbkj.common.response.PlantFormScanResponse;
import com.zbkj.common.response.PlatformHomeAreaResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.HomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/scan/platform/statistics/home")
@Api(tags = "平台端主页控制器")
public class ScanIndexController {

    @Autowired
    private HomeService homeService;

    /**
     * webscoket访问统计
     * @return
     */
    @ApiOperation(value = "大屏数据")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<PlantFormScanResponse> indexDate() {
        return CommonResult.success(homeService.indexScanDate());
    }
    @ApiOperation(value = "大屏数据")
    @RequestMapping(value = "/index/area", method = RequestMethod.GET)
    public CommonResult<List<PlatformHomeAreaResponse>> indexArea() {
        return CommonResult.success(homeService.indexArea());
    }
}
