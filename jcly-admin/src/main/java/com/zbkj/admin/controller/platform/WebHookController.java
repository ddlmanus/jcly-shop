package com.zbkj.admin.controller.platform;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zbkj.common.model.msg.ThirdMsg;
import com.zbkj.common.request.CsjStaticrequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.ThirdMsgService;
import com.zbkj.service.service.WebHookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;


@Slf4j
@RestController
@RequestMapping("api/admin/platform/webhook")
@Api(tags = "数据推送服务器")
public class WebHookController {

    @Resource
    private ThirdMsgService thirdMsgService;
    @Resource
    private WebHookService webHookService;

    /**
     * 采食家数据推送
     * @param jsonObject
     * @return
     */

    @ApiOperation(value = "采食家数据推送")
    @PostMapping("/data-change-event")
    public CommonResult<Boolean> handleWebhook(@RequestBody JSONObject jsonObject) {
        try {
            log.info("接受到的消息为：{}",jsonObject);
            processWebhook(jsonObject);
            String eventType = jsonObject.getString("event_type");
            switch (eventType) {
                case "update":
                    webHookService.handleCsjEvent(jsonObject);
                    break;

                default:
                    log.info("接受到的Unknown event type: " + eventType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("Webhook received, but failed to process");
        }
        return CommonResult.success(true);
    }

    private void processWebhook(JSONObject jsonObject) {
        ThirdMsg thirdMsg = new ThirdMsg();
        //获取当前时间 转换成yyyy-mm-dd hh:mm:ss格式
        thirdMsg.setCreateTime( DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        thirdMsg.setId(IdWorker.getIdStr());
        thirdMsg.setEventType(jsonObject.getString("event_type"));
        thirdMsg.setActionType(jsonObject.getString("action_type"));
        thirdMsg.setMsgContent(jsonObject.get("data").toString());
        thirdMsgService.save(thirdMsg);
    }

    @ApiOperation(value = "数据推送")
    @PostMapping("/csj/data-change-event")
    public CommonResult<Boolean> handleCsjWebhook(@RequestBody CsjStaticrequest request) {
        try {
            log.info("接受到的消息为：{}",request);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("Webhook received, but failed to process");
        }
        return CommonResult.success(true);
    }
    @ApiOperation(value = "数据推送")
    @PostMapping("/jushuitan/data-change")
    public CommonResult<Boolean> handleCsjWebhook2(@RequestParam String message) {
        try {
            log.info("接受到的消息为：{}",message);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("Webhook received, but failed to process");
        }
        return CommonResult.success(true);
    }
}
