package com.zbkj.admin.controller.merchant;

import com.zbkj.common.model.merchant.MerchantAnnouncement;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.MerchantAnnouncementService;
import com.zbkj.service.service.MerchantTodoItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户首页Dashboard控制器
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
@RequestMapping("api/admin/merchant/dashboard")
@Api(tags = "商户首页Dashboard")
@Validated
public class MerchantDashboardController {

    @Autowired
    private MerchantAnnouncementService merchantAnnouncementService;

    @Autowired
    private MerchantTodoItemService merchantTodoItemService;

    /**
     * 获取首页公告和待办事项概览数据
     */
   // @PreAuthorize("hasAuthority('merchant:dashboard:overview')")
    @ApiOperation(value = "获取首页公告和待办事项概览数据")
    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getOverview() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取公告统计数据
        Map<String, Object> announcementStats = merchantAnnouncementService.getAnnouncementStatistics(merId);
        result.put("announcementStats", announcementStats);
        
        // 获取待办事项统计数据
        Map<String, Object> todoStats = merchantTodoItemService.getTodoStatistics(merId);
        result.put("todoStats", todoStats);
        
        // 获取今日已办事项数量
        Integer todayCompletedCount = merchantTodoItemService.getTodayCompletedCount(merId);
        result.put("todayCompletedCount", todayCompletedCount);
        
        return CommonResult.success(result);
    }

    /**
     * 获取首页最新公告列表
     */
  //  @PreAuthorize("hasAuthority('merchant:dashboard:latest:announcements')")
    @ApiOperation(value = "获取首页最新公告列表")
    @RequestMapping(value = "/latest/announcements", method = RequestMethod.GET)
    @ApiImplicitParam(name = "limit", value = "数量限制", dataType = "int", defaultValue = "3")
    public CommonResult<List<MerchantAnnouncement>> getLatestAnnouncements(
            @RequestParam(value = "limit", defaultValue = "3") Integer limit) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        List<MerchantAnnouncement> announcements = merchantAnnouncementService.getLatestAnnouncements(
                loginUserVo.getUser().getMerId(), limit);
        return CommonResult.success(announcements);
    }

    /**
     * 获取公告卡片数据
     */
  //  @PreAuthorize("hasAuthority('merchant:dashboard:announcement:card')")
    @ApiOperation(value = "获取公告卡片数据")
    @RequestMapping(value = "/announcement/card", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getAnnouncementCard() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取公告统计数据
        Map<String, Object> statistics = merchantAnnouncementService.getAnnouncementStatistics(merId);
        result.putAll(statistics);
        
        // 获取最新3条公告
        List<MerchantAnnouncement> latestAnnouncements = merchantAnnouncementService.getLatestAnnouncements(merId, 3);
        result.put("latestAnnouncements", latestAnnouncements);
        
        return CommonResult.success(result);
    }

    /**
     * 获取待办事项卡片数据
     */
   // @PreAuthorize("hasAuthority('merchant:dashboard:todo:card')")
    @ApiOperation(value = "获取待办事项卡片数据")
    @RequestMapping(value = "/todo/card", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getTodoCard() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取待办事项统计数据
        Map<String, Object> statistics = merchantTodoItemService.getTodoStatistics(merId);
        result.putAll(statistics);
        
        // 获取各类型待办事项数量
        Integer orderCount = merchantTodoItemService.getCountByType(merId, "order");
        Integer messageCount = merchantTodoItemService.getCountByType(merId, "message");
        Integer productCount = merchantTodoItemService.getCountByType(merId, "product");
        Integer refundCount = merchantTodoItemService.getCountByType(merId, "refund");
        
        Map<String, Integer> typeCount = new HashMap<>();
        typeCount.put("order", orderCount);
        typeCount.put("message", messageCount);
        typeCount.put("product", productCount);
        typeCount.put("refund", refundCount);
        result.put("typeCount", typeCount);
        
        return CommonResult.success(result);
    }

    /**
     * 获取已办事项卡片数据
     */
  //  @PreAuthorize("hasAuthority('merchant:dashboard:completed:card')")
    @ApiOperation(value = "获取已办事项卡片数据")
    @RequestMapping(value = "/completed/card", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getCompletedCard() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取今日已办事项数量
        Integer todayCompletedCount = merchantTodoItemService.getTodayCompletedCount(merId);
        result.put("todayCompletedCount", todayCompletedCount);
        
        // 获取总的已完成事项数量
        Map<String, Object> todoStats = merchantTodoItemService.getTodoStatistics(merId);
        result.put("totalCompletedCount", todoStats.get("completedCount"));
        
        // 计算完成率
        Integer totalCount = (Integer) todoStats.get("totalCount");
        Integer completedCount = (Integer) todoStats.get("completedCount");
        if (totalCount > 0) {
            double completionRate = (double) completedCount / totalCount * 100;
            result.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        } else {
            result.put("completionRate", 0.0);
        }
        
        return CommonResult.success(result);
    }

    /**
     * 获取首页快捷统计数据
     */
    //@PreAuthorize("hasAuthority('merchant:dashboard:quick:stats')")
    @ApiOperation(value = "获取首页快捷统计数据")
    @RequestMapping(value = "/quick/stats", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getQuickStats() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        
        Map<String, Object> result = new HashMap<>();
        
        // 待处理事项总数
        Map<String, Object> todoStats = merchantTodoItemService.getTodoStatistics(merId);
        result.put("pendingTodoCount", todoStats.get("pendingCount"));
        result.put("highPriorityCount", todoStats.get("highPriorityCount"));
        
        // 今日发布公告数
        Map<String, Object> announcementStats = merchantAnnouncementService.getAnnouncementStatistics(merId);
        result.put("todayAnnouncementCount", announcementStats.get("todayCount"));
        
        // 今日已办事项数
        Integer todayCompletedCount = merchantTodoItemService.getTodayCompletedCount(merId);
        result.put("todayCompletedCount", todayCompletedCount);
        
        return CommonResult.success(result);
    }
}