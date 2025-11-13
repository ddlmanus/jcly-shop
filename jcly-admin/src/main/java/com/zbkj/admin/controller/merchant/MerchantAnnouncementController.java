package com.zbkj.admin.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.MerchantAnnouncement;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.MerchantAnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 商户公告管理控制器
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
@RequestMapping("api/admin/merchant/announcement")
@Api(tags = "商户公告管理")
@Validated
public class MerchantAnnouncementController {

    @Autowired
    private MerchantAnnouncementService merchantAnnouncementService;

    /**
     * 分页获取公告列表
     */
  //  @PreAuthorize("hasAuthority('merchant:announcement:list')")
    @ApiOperation(value = "分页获取公告列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "公告标题", dataType = "String"),
            @ApiImplicitParam(name = "type", value = "公告类型", dataType = "int"),
            @ApiImplicitParam(name = "status", value = "公告状态", dataType = "int"),
            @ApiImplicitParam(name = "dateLimit", value = "时间范围", dataType = "String")
    })
    public CommonResult<PageInfo<MerchantAnnouncement>> getList(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "dateLimit", required = false) String dateLimit,
            @Validated PageParamRequest pageParamRequest) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        PageInfo<MerchantAnnouncement> pageInfo = merchantAnnouncementService.getPage(
                loginUserVo.getUser().getMerId(), title, type, status, dateLimit, pageParamRequest);
        return CommonResult.success(pageInfo);
    }

    /**
     * 创建公告
     */
   // @PreAuthorize("hasAuthority('merchant:announcement:create')")
    @ApiOperation(value = "创建公告")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<String> create(@RequestBody @Valid MerchantAnnouncement announcement) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        announcement.setMerId(loginUserVo.getUser().getMerId());
        announcement.setStatus(0); // 默认草稿状态
        Boolean result = merchantAnnouncementService.create(announcement);
        if (result) {
            return CommonResult.success("创建公告成功");
        }
        return CommonResult.failed("创建公告失败");
    }

    /**
     * 更新公告
     */
   // @PreAuthorize("hasAuthority('merchant:announcement:update')")
    @ApiOperation(value = "更新公告")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Valid MerchantAnnouncement announcement) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        // 验证公告是否属于当前商户
        MerchantAnnouncement existAnnouncement = merchantAnnouncementService.getDetailById(announcement.getId());
        if (!existAnnouncement.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限操作此公告");
        }
        Boolean result = merchantAnnouncementService.updateAnnouncement(announcement);
        if (result) {
            return CommonResult.success("更新公告成功");
        }
        return CommonResult.failed("更新公告失败");
    }

    /**
     * 删除公告
     */
   // @PreAuthorize("hasAuthority('merchant:announcement:delete')")
    @ApiOperation(value = "删除公告")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<String> delete(@PathVariable @NotNull Integer id) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        // 验证公告是否属于当前商户
        MerchantAnnouncement announcement = merchantAnnouncementService.getDetailById(id);
        if (!announcement.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限操作此公告");
        }
        Boolean result = merchantAnnouncementService.deleteAnnouncement(id);
        if (result) {
            return CommonResult.success("删除公告成功");
        }
        return CommonResult.failed("删除公告失败");
    }

    /**
     * 发布公告
     */
  //  @PreAuthorize("hasAuthority('merchant:announcement:publish')")
    @ApiOperation(value = "发布公告")
    @RequestMapping(value = "/publish/{id}", method = RequestMethod.POST)
    public CommonResult<String> publish(@PathVariable @NotNull Integer id) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        // 验证公告是否属于当前商户
        MerchantAnnouncement announcement = merchantAnnouncementService.getDetailById(id);
        if (!announcement.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限操作此公告");
        }
        Boolean result = merchantAnnouncementService.publishAnnouncement(id);
        if (result) {
            return CommonResult.success("发布公告成功");
        }
        return CommonResult.failed("发布公告失败");
    }

    /**
     * 撤回公告
     */
   // @PreAuthorize("hasAuthority('merchant:announcement:withdraw')")
    @ApiOperation(value = "撤回公告")
    @RequestMapping(value = "/withdraw/{id}", method = RequestMethod.POST)
    public CommonResult<String> withdraw(@PathVariable @NotNull Integer id) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        // 验证公告是否属于当前商户
        MerchantAnnouncement announcement = merchantAnnouncementService.getDetailById(id);
        if (!announcement.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限操作此公告");
        }
        Boolean result = merchantAnnouncementService.withdrawAnnouncement(id);
        if (result) {
            return CommonResult.success("撤回公告成功");
        }
        return CommonResult.failed("撤回公告失败");
    }

    /**
     * 获取公告详情
     */
   // @PreAuthorize("hasAuthority('merchant:announcement:detail')")
    @ApiOperation(value = "获取公告详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<MerchantAnnouncement> getDetail(@PathVariable @NotNull Integer id) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        MerchantAnnouncement announcement = merchantAnnouncementService.getDetailById(id);
        if (!announcement.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限查看此公告");
        }
        return CommonResult.success(announcement);
    }

    /**
     * 获取最新公告列表
     */
    //@PreAuthorize("hasAuthority('merchant:announcement:latest')")
    @ApiOperation(value = "获取最新公告列表")
    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    @ApiImplicitParam(name = "limit", value = "数量限制", dataType = "int", defaultValue = "5")
    public CommonResult<List<MerchantAnnouncement>> getLatest(
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        List<MerchantAnnouncement> list = merchantAnnouncementService.getLatestAnnouncements(
                loginUserVo.getUser().getMerId(), limit);
        return CommonResult.success(list);
    }

    /**
     * 获取公告统计信息
     */
   // @PreAuthorize("hasAuthority('merchant:announcement:statistics')")
    @ApiOperation(value = "获取公告统计信息")
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getStatistics() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Map<String, Object> statistics = merchantAnnouncementService.getAnnouncementStatistics(
                loginUserVo.getUser().getMerId());
        return CommonResult.success(statistics);
    }

    /**
     * 根据类型获取公告数量
     */
    //@PreAuthorize("hasAuthority('merchant:announcement:count')")
    @ApiOperation(value = "根据类型获取公告数量")
    @RequestMapping(value = "/count/{type}", method = RequestMethod.GET)
    public CommonResult<Integer> getCountByType(@PathVariable @NotNull Integer type) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer count = merchantAnnouncementService.getCountByType(
                loginUserVo.getUser().getMerId(), type);
        return CommonResult.success(count);
    }
}