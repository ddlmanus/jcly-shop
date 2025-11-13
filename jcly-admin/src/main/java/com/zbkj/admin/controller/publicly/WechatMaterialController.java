package com.zbkj.admin.controller.publicly;

import com.zbkj.common.request.wxmplive.media.WechatMediaUploadLocalRequest;
import com.zbkj.common.response.WechatOpenUploadResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.WechatMediaService;
import com.zbkj.service.service.WechatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 类的详细说明
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/4/14
 */
@Slf4j
@RestController
@RequestMapping("api/publicly/wechat/material")
@Api(tags = "微信素材控制器")
public class WechatMaterialController {

    @Autowired
    private WechatMediaService wechatMediaService;

    @Autowired
    private WechatService wechatService;

    @ApiOperation(value = "微信小程序-新增临时素材-本地")
    @RequestMapping(value = "/ma/media/upload/localhost", method = RequestMethod.POST)
    public CommonResult<WxMediaUploadResult> WechatMaMediaUploadLocal(@RequestBody WechatMediaUploadLocalRequest request) {
        WxMediaUploadResult wxMediaUploadResult = wechatMediaService.uploadMediaByLocal(request.getType(), request.getImagePath());
        return CommonResult.success(wxMediaUploadResult);
    }

    @ApiOperation(value = "微信公众号-新增其他类型永久素材")
    @RequestMapping(value = "/mp/material/add/material", method = RequestMethod.POST)
    public CommonResult<WechatOpenUploadResponse> upload(
            @RequestParam("media") @ApiParam(name = "media", value = "待上传素材图片文件", required = true) MultipartFile file,
            @RequestParam("type") @ApiParam(name = "type", value = "媒体文件类型，分别有图片（image）、语音（voice", required = true, allowableValues = "range[image,voice]") String type
    ) {
//        return CommonResult.success(wechatMediaService.openMediaUpload(file, type));
        return CommonResult.success(wechatService.openMediaUpload(file, type));
    }

}
