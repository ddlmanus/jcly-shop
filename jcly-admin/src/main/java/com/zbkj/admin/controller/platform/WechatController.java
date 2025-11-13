package com.zbkj.admin.controller.platform;

import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.request.SaveConfigRequest;
import com.zbkj.common.response.WeChatJsSdkConfigResponse;
import com.zbkj.common.response.WechatOpenUploadResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.CommonSeparateConfigVo;
import com.zbkj.service.service.WechatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 微信缓存表 前端控制器
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
@RequestMapping("api/admin/platform/wechat")
@Api(tags = "微信控制器")
public class WechatController {

    @Autowired
    private WechatService wechatService;

    @PreAuthorize("hasAuthority('platform:wechat:public:js:config')")
    @ApiOperation(value = "获取微信公众号js配置")
    @RequestMapping(value = "/get/public/js/config", method = RequestMethod.GET)
    @ApiImplicitParam(name = "url", value = "页面地址url")
    public CommonResult<WeChatJsSdkConfigResponse> getPublicJsConfig(@RequestParam(value = "url") String url) {
        return CommonResult.success(wechatService.getPublicJsConfig(url));
    }

    @PreAuthorize("hasAuthority('platform:wechat:public:customize:menu:get')")
    @ApiOperation(value = "获取公众号自定义菜单")
    @RequestMapping(value = "/public/customize/menu", method = RequestMethod.GET)
    public CommonResult<JSONObject> getPublicCustomizeMenu() {
        return CommonResult.success(wechatService.getPublicCustomMenu());
    }

    @PreAuthorize("hasAuthority('platform:wechat:public:customize:menu:save')")
    @ApiOperation(value = "保存公众号自定义菜单")
    @RequestMapping(value = "/public/customize/menu/create", method = RequestMethod.POST)
    public CommonResult<JSONObject> savePublicCustomizeMenu(@RequestBody String data) {
        if (wechatService.createPublicCustomMenu(data)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:wechat:public:customize:menu:delete')")
    @ApiOperation(value = "删除公众号自定义菜单")
    @RequestMapping(value = "/public/customize/menu/delete", method = RequestMethod.GET)
    public CommonResult<JSONObject> deletePublicCustomizeMenu() {
        if (wechatService.deletePublicCustomMenu()) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:wechat:open:media:upload')")
    @ApiOperation(value = "微信开放平台上传素材")
    @RequestMapping(value = "/open/media/upload", method = RequestMethod.POST)
    public CommonResult<WechatOpenUploadResponse> upload(
            @RequestParam("media") @ApiParam(name = "media", value = "待上传素材图片文件", required = true) MultipartFile file,
            @RequestParam("type") @ApiParam(name = "type", value = "媒体文件类型，分别有图片（image）、语音（voice", required = true, allowableValues = "range[image,voice]") String type
    ) {
        return CommonResult.success(wechatService.openMediaUpload(file, type));
    }

    @PreAuthorize("hasAuthority('platform:wechat:mini:shipping:switch:get')")
    @ApiOperation(value = "获取微信小程序发货开关")
    @RequestMapping(value = "/get/shipping/switch", method = RequestMethod.GET)
    public CommonResult<CommonSeparateConfigVo> getShippingSwitch() {
        return CommonResult.success(wechatService.getShippingSwitch());
    }

    @PreAuthorize("hasAuthority('platform:wechat:mini:shipping:switch:update')")
    @ApiOperation(value = "更新微信小程序发货开关")
    @RequestMapping(value = "/update/shipping/switch", method = RequestMethod.POST)
    public CommonResult<Object> updateShippingSwitch(@RequestBody @Validated SaveConfigRequest request) {
        wechatService.updateShippingSwitch(request);
        return CommonResult.success();
    }
}



