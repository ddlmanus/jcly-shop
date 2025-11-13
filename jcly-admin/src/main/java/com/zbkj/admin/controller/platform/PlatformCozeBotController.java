package com.zbkj.admin.controller.platform;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeBot;
import com.zbkj.common.model.coze.CozeBotConfig;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.coze.*;
import com.zbkj.common.response.coze.*;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 平台端 - Coze智能体管理控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/coze/bot")
@Api(tags = "平台端 - Coze智能体管理")
public class PlatformCozeBotController {

    @Autowired
    private CozeService cozeService;
    
    @Autowired
    private CozeBotService cozeBotService;

    @ApiOperation(value = "创建智能体")
    // @PreAuthorize("hasAuthority('platform:coze:bot:create')")
    @PostMapping("/create")
    public CommonResult<CozeCreateBotResponse> createBot(@RequestBody @Validated CozeCreateBotRequest request) {
        CozeCreateBotResponse response = cozeService.createBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "更新智能体")
    // @PreAuthorize("hasAuthority('platform:coze:bot:update')")
    @PostMapping("/update")
    public CommonResult<CozeBaseResponse> updateBot(@RequestBody @Validated CozeUpdateBotRequest request) {
        CozeBaseResponse response = cozeService.updateBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "发布智能体")
    // @PreAuthorize("hasAuthority('platform:coze:bot:publish')")
    @PostMapping("/publish")
    public CommonResult<CozeBaseResponse> publishBot(@RequestBody @Validated CozePublishBotRequest request) {
        CozeBaseResponse response = cozeService.publishBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "下架智能体")
    // @PreAuthorize("hasAuthority('platform:coze:bot:unpublish')")
    @PostMapping("/{botId}/unpublish")
    public CommonResult<CozeBaseResponse> unpublishBot(@PathVariable String botId, @RequestBody @Validated CozeUnpublishBotRequest request) {
        request.setBotId(botId);
        CozeBaseResponse response = cozeService.unpublishBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看智能体列表 (Coze API)")
    // @PreAuthorize("hasAuthority('platform:coze:bot:list')")
    @GetMapping("/list")
    public CommonResult<CozeBotListResponse> getBotList(@RequestParam String spaceId, 
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "20") Integer pageSize) {
        CozeBotListResponse response = cozeService.getplatBotList(spaceId, pageNum, pageSize);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地智能体列表")
    // @PreAuthorize("hasAuthority('platform:coze:bot:view')")
    @GetMapping("/local/list")
    public CommonResult<CommonPage<CozeBot>> getLocalBotList(PageParamRequest pageParamRequest) {
        // 平台端使用登录用户ID作为merchantId
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeBot> response = cozeBotService.getByMerchantId(merchantId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "获取本地智能体详情")
    // @PreAuthorize("hasAuthority('platform:coze:bot:view')")
    @GetMapping("/local/{cozeBotId}")
    public CommonResult<CozeBot> getLocalBotDetail(@PathVariable String cozeBotId) {
        // 平台端使用登录用户ID作为merchantId
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeBot response = cozeBotService.getByCozeBotId(cozeBotId,merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除本地智能体")
    // @PreAuthorize("hasAuthority('platform:coze:bot:delete')")
    @DeleteMapping("/local/{cozeBotId}")
    public CommonResult<Boolean> deleteLocalBot(@PathVariable String cozeBotId) {
        // 平台端使用登录用户ID作为merchantId
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeBotService.deleteByBotId(cozeBotId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "设置默认智能体")
    // @PreAuthorize("hasAuthority('platform:coze:bot:setDefault')")
    @PostMapping("/local/{cozeBotId}/setDefault")
    public CommonResult<Boolean> setDefaultBot(@PathVariable String cozeBotId) {
        // 平台端使用登录用户ID作为merchantId
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeBotService.setDefaultBot(cozeBotId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取默认智能体")
    // @PreAuthorize("hasAuthority('platform:coze:bot:view')")
    @GetMapping("/local/default")
    public CommonResult<CozeBot> getDefaultBot() {
        // 平台端使用登录用户ID作为merchantId
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeBot response = cozeBotService.getDefaultBot(merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取智能体配置")
    //  @PreAuthorize("hasAuthority('merchant:coze:bot:config:view')")
    @GetMapping("/{botId}/config")
    public CommonResult<Object> getBotConfig(@PathVariable String botId) {
        Object response = cozeService.getBotConfig(botId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地智能体配置")
    //  @PreAuthorize("hasAuthority('merchant:coze:bot:config:view')")
    @GetMapping("/{botId}/config/local")
    public CommonResult<CozeBotConfig> getLocalBotConfig(@PathVariable String botId) {
        CozeBotConfig response = cozeService.getLocalBotConfig(botId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "同步智能体配置")
    // @PreAuthorize("hasAuthority('platform:coze:bot:sync-config')")
    @PostMapping("/{botId}/sync-config")
    public CommonResult<String> syncBotConfig(@PathVariable String botId) {
        try {
            Boolean result = cozeService.syncBotConfigToLocal(botId, 0); // 平台端merchantId为0
            if (result) {
                return CommonResult.success("同步智能体配置成功");
            } else {
                return CommonResult.failed("同步智能体配置失败");
            }
        } catch (Exception e) {
            log.error("同步智能体配置失败", e);
            return CommonResult.failed("同步智能体配置失败：" + e.getMessage());
        }
    }
}