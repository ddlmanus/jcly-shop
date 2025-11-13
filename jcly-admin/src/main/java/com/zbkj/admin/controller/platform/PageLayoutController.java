package com.zbkj.admin.controller.platform;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zbkj.common.model.page.PageLayoutNavigationHistory;
import com.zbkj.common.model.page.PageLayoutUserMenuHistory;
import com.zbkj.common.model.page.PageLayoutSplashAdHistory;
import com.zbkj.common.response.PageLayoutBottomNavigationResponse;
import com.zbkj.common.response.PageLayoutIndexResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.SplashAdConfigVo;
import com.zbkj.service.service.PageLayoutNavigationHistoryService;
import com.zbkj.service.service.PageLayoutUserMenuHistoryService;
import com.zbkj.service.service.PageLayoutSplashAdHistoryService;
import com.zbkj.service.service.PageLayoutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 页面设计 前端控制器
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/page/layout")
@Api(tags = "页面布局管理")
public class PageLayoutController {

    @Autowired
    private PageLayoutService pageLayoutService;
    @Autowired
    private PageLayoutNavigationHistoryService pageLayoutNavigationHistoryService;
    @Autowired
    private PageLayoutUserMenuHistoryService pageLayoutUserMenuHistoryService;
    @Autowired
    private PageLayoutSplashAdHistoryService pageLayoutSplashAdHistoryService;

    @PreAuthorize("hasAuthority('platform:page:layout:index')")
    @ApiOperation(value = "页面首页")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<PageLayoutIndexResponse> index() {
        return CommonResult.success(pageLayoutService.index());
    }

    @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation')")
    @ApiOperation(value = "页面底部导航")
    @RequestMapping(value = "/bottom/navigation/get", method = RequestMethod.GET)
    public CommonResult<PageLayoutBottomNavigationResponse> getBottomNavigation() {
        return CommonResult.success(pageLayoutService.getBottomNavigation());
    }

    @PreAuthorize("hasAuthority('platform:page:layout:index:banner:save')")
    @ApiOperation(value = "页面首页banner保存")
    @RequestMapping(value = "/index/banner/save", method = RequestMethod.POST)
    public CommonResult<Object> indexBannerSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.indexBannerSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:page:layout:index:menu:save')")
    @ApiOperation(value = "页面首页menu保存")
    @RequestMapping(value = "/index/menu/save", method = RequestMethod.POST)
    public CommonResult<Object> indexMenuSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.indexMenuSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:page:layout:index:banner:save')")
    @ApiOperation(value = "页面用户中心banner保存")
    @RequestMapping(value = "/user/banner/save", method = RequestMethod.POST)
    public CommonResult<Object> userBannerSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.userBannerSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:page:layout:user:menu:save')")
    @ApiOperation(value = "页面用户中心导航保存")
    @RequestMapping(value = "/user/menu/save", method = RequestMethod.POST)
    public CommonResult<Object> userMenuSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.userMenuSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation:save')")
    @ApiOperation(value = "底部导航保存")
    @RequestMapping(value = "/bottom/navigation/save", method = RequestMethod.POST)
    public CommonResult<Object> bottomNavigationSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.bottomNavigationSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:get')")
    @ApiOperation(value = "获取开屏广告配置")
    @RequestMapping(value = "/splash/ad/get", method = RequestMethod.GET)
    public CommonResult<SplashAdConfigVo> getSplashAdConfig() {
        return CommonResult.success(pageLayoutService.getSplashAdConfig());
    }

    @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:save')")
    @ApiOperation(value = "编辑开屏广告配置")
    @RequestMapping(value = "/splash/ad/save", method = RequestMethod.POST)
    public CommonResult<Object> splashAdConfigSave(@RequestBody @Validated SplashAdConfigVo configVo) {
        if (pageLayoutService.splashAdConfigSave(configVo)) {
            return CommonResult.success("编辑成功");
        }
        return CommonResult.failed("编辑失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation:history:save')")
    @ApiOperation(value = "保存底部导航为历史模版")
    @RequestMapping(value = "/bottom/navigation/history/save", method = RequestMethod.POST)
    public CommonResult<Object> saveNavigationHistory(
            @ApiParam(value = "模版名称", required = true) @RequestParam(value = "templateName") String templateName,
            @RequestBody JSONObject jsonObject) {
        if (pageLayoutNavigationHistoryService.saveHistoryTemplate(templateName, jsonObject)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation:history:list')")
    @ApiOperation(value = "分页查询底部导航历史记录")
    @RequestMapping(value = "/bottom/navigation/history/list", method = RequestMethod.GET)
    public CommonResult<IPage<PageLayoutNavigationHistory>> getNavigationHistoryList(
            @ApiParam(value = "页码", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", required = true) @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        IPage<PageLayoutNavigationHistory> historyPage = pageLayoutNavigationHistoryService.getHistoryPage(page, limit);
        return CommonResult.success(historyPage);
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation:history:get')")
    @ApiOperation(value = "获取底部导航历史记录详情")
    @RequestMapping(value = "/bottom/navigation/history/detail/{id}", method = RequestMethod.GET)
    public CommonResult<PageLayoutNavigationHistory> getNavigationHistory(
            @ApiParam(value = "历史记录ID", required = true) @PathVariable Integer id) {
        PageLayoutNavigationHistory history = pageLayoutNavigationHistoryService.getById(id);
        return CommonResult.success(history);
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation:history:set:default')")
    @ApiOperation(value = "设置默认底部导航配置")
    @RequestMapping(value = "/bottom/navigation/history/set/default", method = RequestMethod.POST)
    public CommonResult<Object> setNavigationHistoryDefault(
            @ApiParam(value = "历史记录ID", required = true) @RequestParam(value = "id") Integer id) {
        if (pageLayoutNavigationHistoryService.setDefaultAndApply(id)) {
            return CommonResult.success("设置成功");
        }
        return CommonResult.failed("设置失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation:history:delete')")
    @ApiOperation(value = "删除底部导航历史记录")
    @RequestMapping(value = "/bottom/navigation/history/delete", method = RequestMethod.POST)
    public CommonResult<Object> deleteNavigationHistory(
            @ApiParam(value = "历史记录ID", required = true) @RequestParam(value = "id") Integer id) {
        if (pageLayoutNavigationHistoryService.deleteHistory(id)) {
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:bottom:navigation:history:update')")
    @ApiOperation(value = "更新底部导航历史记录")
    @RequestMapping(value = "/bottom/navigation/history/update", method = RequestMethod.POST)
    public CommonResult<Object> updateNavigationHistory(@RequestBody PageLayoutNavigationHistory history) {
        if (pageLayoutNavigationHistoryService.updateHistory(history)) {
            return CommonResult.success("更新成功");
        }
        return CommonResult.failed("更新失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:user:menu:history:save')")
    @ApiOperation(value = "保存用户中心菜单为历史模版")
    @RequestMapping(value = "/user/menu/history/save", method = RequestMethod.POST)
    public CommonResult<Object> saveUserMenuHistory(
            @ApiParam(value = "模版名称", required = true) @RequestParam(value = "templateName") String templateName,
            @RequestBody JSONObject jsonObject) {
        if (pageLayoutUserMenuHistoryService.saveHistoryTemplate(templateName, jsonObject)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:user:menu:history:list')")
    @ApiOperation(value = "分页查询用户中心菜单历史记录")
    @RequestMapping(value = "/user/menu/history/list", method = RequestMethod.GET)
    public CommonResult<IPage<PageLayoutUserMenuHistory>> getUserMenuHistoryList(
            @ApiParam(value = "页码", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", required = true) @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        IPage<PageLayoutUserMenuHistory> historyPage = pageLayoutUserMenuHistoryService.getHistoryPage(page, limit);
        return CommonResult.success(historyPage);
    }

  //  @PreAuthorize("hasAuthority('platform:page:layout:user:menu:history:get')")
    @ApiOperation(value = "获取用户中心菜单历史记录详情")
    @RequestMapping(value = "/user/menu/history/detail/{id}", method = RequestMethod.GET)
    public CommonResult<PageLayoutUserMenuHistory> getUserMenuHistory(
            @ApiParam(value = "历史记录ID", required = true) @PathVariable Integer id) {
        PageLayoutUserMenuHistory history = pageLayoutUserMenuHistoryService.getById(id);
        return CommonResult.success(history);
    }

  //  @PreAuthorize("hasAuthority('platform:page:layout:user:menu:history:set:default')")
    @ApiOperation(value = "设置默认用户中心菜单配置")
    @RequestMapping(value = "/user/menu/history/set/default", method = RequestMethod.POST)
    public CommonResult<Object> setUserMenuHistoryDefault(
            @ApiParam(value = "历史记录ID", required = true) @RequestParam(value = "id") Integer id) {
        if (pageLayoutUserMenuHistoryService.setDefaultAndApply(id)) {
            return CommonResult.success("设置成功");
        }
        return CommonResult.failed("设置失败");
    }

  //  @PreAuthorize("hasAuthority('platform:page:layout:user:menu:history:delete')")
    @ApiOperation(value = "删除用户中心菜单历史记录")
    @RequestMapping(value = "/user/menu/history/delete", method = RequestMethod.POST)
    public CommonResult<Object> deleteUserMenuHistory(
            @ApiParam(value = "历史记录ID", required = true) @RequestParam(value = "id") Integer id) {
        if (pageLayoutUserMenuHistoryService.deleteHistory(id)) {
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }

  //  @PreAuthorize("hasAuthority('platform:page:layout:user:menu:history:update')")
    @ApiOperation(value = "更新用户中心菜单历史记录")
    @RequestMapping(value = "/user/menu/history/update", method = RequestMethod.POST)
    public CommonResult<Object> updateUserMenuHistory(@RequestBody PageLayoutUserMenuHistory history) {
        if (pageLayoutUserMenuHistoryService.updateHistory(history)) {
            return CommonResult.success("更新成功");
        }
        return CommonResult.failed("更新失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:history:save')")
    @ApiOperation(value = "保存开屏广告为历史模版")
    @RequestMapping(value = "/splash/ad/history/save", method = RequestMethod.POST)
    public CommonResult<Object> saveSplashAdHistory(
            @ApiParam(value = "模版名称", required = true) @RequestParam(value = "templateName") String templateName,
            @RequestBody SplashAdConfigVo configVo) {
        if (pageLayoutSplashAdHistoryService.saveHistoryTemplate(templateName, configVo)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:history:list')")
    @ApiOperation(value = "分页查询开屏广告历史记录")
    @RequestMapping(value = "/splash/ad/history/list", method = RequestMethod.GET)
    public CommonResult<IPage<PageLayoutSplashAdHistory>> getSplashAdHistoryList(
            @ApiParam(value = "页码", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", required = true) @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        IPage<PageLayoutSplashAdHistory> historyPage = pageLayoutSplashAdHistoryService.getHistoryPage(page, limit);
        return CommonResult.success(historyPage);
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:history:get')")
    @ApiOperation(value = "获取开屏广告历史记录详情")
    @RequestMapping(value = "/splash/ad/history/detail/{id}", method = RequestMethod.GET)
    public CommonResult<PageLayoutSplashAdHistory> getSplashAdHistory(
            @ApiParam(value = "历史记录ID", required = true)  @PathVariable Integer id) {
        PageLayoutSplashAdHistory history = pageLayoutSplashAdHistoryService.getById(id);
        return CommonResult.success(history);
    }

   // @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:history:set:default')")
    @ApiOperation(value = "设置默认开屏广告配置")
    @RequestMapping(value = "/splash/ad/history/set/default", method = RequestMethod.POST)
    public CommonResult<Object> setSplashAdHistoryDefault(
            @ApiParam(value = "历史记录ID", required = true) @RequestParam(value = "id") Integer id) {
        if (pageLayoutSplashAdHistoryService.setDefaultAndApply(id)) {
            return CommonResult.success("设置成功");
        }
        return CommonResult.failed("设置失败");
    }

  //  @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:history:delete')")
    @ApiOperation(value = "删除开屏广告历史记录")
    @RequestMapping(value = "/splash/ad/history/delete", method = RequestMethod.POST)
    public CommonResult<Object> deleteSplashAdHistory(
            @ApiParam(value = "历史记录ID", required = true) @RequestParam(value = "id") Integer id) {
        if (pageLayoutSplashAdHistoryService.deleteHistory(id)) {
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }

  //  @PreAuthorize("hasAuthority('platform:page:layout:splash:ad:history:update')")
    @ApiOperation(value = "更新开屏广告历史记录")
    @RequestMapping(value = "/splash/ad/history/update", method = RequestMethod.POST)
    public CommonResult<Object> updateSplashAdHistory(@RequestBody PageLayoutSplashAdHistory history) {
        if (pageLayoutSplashAdHistoryService.updateHistory(history)) {
            return CommonResult.success("更新成功");
        }
        return CommonResult.failed("更新失败");
    }
}
