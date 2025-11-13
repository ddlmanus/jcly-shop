package com.zbkj.admin.controller.publicly;

import com.zbkj.admin.service.WechatMessageService;
import com.zbkj.common.annotation.CustomResponseAnnotation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


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
@RequestMapping("api/publicly/wechat/public/callback")
@Api(tags = "微信公众号回调管理")
@CustomResponseAnnotation
public class WeChatMessageController {

    @Autowired
    private WechatMessageService weChatMessageService;

    @ApiOperation(value = "接受微信推送过来的消息")
    @RequestMapping(value = "/webHook", method = RequestMethod.POST)
    public String webHook(HttpServletRequest request) {
        return weChatMessageService.messageEvent(request);
    }

    @ApiOperation(value = "微信推送地址验证")
    @RequestMapping(value = {"/webHook"}, method = RequestMethod.GET)
    private void webHook(
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "echostr") String echostr,
            HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.print(echostr);
        writer.flush();
        writer.close();
    }
}



