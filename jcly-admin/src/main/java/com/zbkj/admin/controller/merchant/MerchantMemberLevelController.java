package com.zbkj.admin.controller.merchant;

import com.zbkj.common.model.member.MemberLevel;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.MemberLevelRequest;
import com.zbkj.common.request.MemberLevelSearchRequest;
import com.zbkj.common.request.MemberLevelStatusRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.MemberLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商户端会员等级管理控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/member/level")
@Api(tags = "商户端-会员等级管理")
public class MerchantMemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    /**
     * 会员等级列表
     */
   // @PreAuthorize("hasAuthority('merchant:member:level:list')")
    @ApiOperation(value = "会员等级列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MemberLevel>> getList(@Validated MemberLevelSearchRequest request) {
        // 设置当前商户ID
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        request.setMerId(systemAdmin.getMerId());
        return CommonResult.success(CommonPage.restPage(memberLevelService.getList(request)));
    }

    /**
     * 会员等级详情
     */
   // @PreAuthorize("hasAuthority('merchant:member:level:detail')")
    @ApiOperation(value = "会员等级详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<MemberLevel> getDetail(@PathVariable Integer id) {
        return CommonResult.success(memberLevelService.getDetail(id));
    }

    /**
     * 新增会员等级
     */
  //  @PreAuthorize("hasAuthority('merchant:member:level:add')")
    @ApiOperation(value = "新增会员等级")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<String> add(@RequestBody @Validated MemberLevelRequest request) {
        if (memberLevelService.add(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed("新增会员等级失败");
    }

    /**
     * 编辑会员等级
     */
   // @PreAuthorize("hasAuthority('merchant:member:level:edit')")
    @ApiOperation(value = "编辑会员等级")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public CommonResult<String> edit(@RequestBody @Validated MemberLevelRequest request) {
        if (memberLevelService.edit(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed("编辑会员等级失败");
    }

    /**
     * 删除会员等级
     */
  //  @PreAuthorize("hasAuthority('merchant:member:level:delete')")
    @ApiOperation(value = "删除会员等级")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<String> delete(@PathVariable Integer id) {
        if (memberLevelService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed("删除会员等级失败");
    }

    /**
     * 修改会员等级状态
     */
  //  @PreAuthorize("hasAuthority('merchant:member:level:status')")
    @ApiOperation(value = "修改会员等级状态")
    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public CommonResult<String> updateStatus(@RequestBody @Validated MemberLevelStatusRequest request) {
        if (memberLevelService.updateStatus(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed("修改会员等级状态失败");
    }
}