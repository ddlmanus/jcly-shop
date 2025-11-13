package com.zbkj.admin.controller.publicly;

import com.zbkj.admin.service.SystemStateService;
import com.zbkj.common.response.system.SystemStateInfoResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统状态控制器
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Slf4j
@RestController
@RequestMapping("api/publicly/system/state")
@Api(tags = "系统状态控制器")
public class SystemStateController {

    @Autowired
    private SystemStateService systemStateService;

    @ApiOperation(value = "获取系统状态信息")
    @RequestMapping(value = "/get/info", method = RequestMethod.GET)
    public SystemStateInfoResponse getIngo() {
        return systemStateService.getIngo();
    }

}
