package com.zbkj.admin.controller.platform;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeSpace;
import com.zbkj.common.model.coze.CozeSpaceMember;
import com.zbkj.common.model.coze.CozeWorkflow;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.coze.CozeGetSpaceListRequest;
import com.zbkj.common.request.coze.CozeGetSpaceMembersRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.CozeSpaceService;
import com.zbkj.service.service.CozeSpaceMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台端 - Coze空间管理控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/coze/space")
@Api(tags = "平台端 - Coze空间管理")
public class PlatformCozeSpaceController {

    @Autowired
    private CozeService cozeService;
    
    @Autowired
    private CozeSpaceService cozeSpaceService;
    
    @Autowired
    private CozeSpaceMemberService cozeSpaceMemberService;

    @ApiOperation(value = "查看空间列表")
    // @PreAuthorize("hasAuthority('platform:coze:space:list')")
    @GetMapping("/list")
    public CommonResult<Object> getSpaceList(
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String cozeAccountId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        CozeGetSpaceListRequest request = new CozeGetSpaceListRequest();
        request.setEnterpriseId(enterpriseId);
        request.setUserId(userId);
        request.setCozeAccountId(cozeAccountId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        Object response = cozeService.getSpaceList(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看空间成员")
    // @PreAuthorize("hasAuthority('platform:coze:space:members')")
    @GetMapping("/{spaceId}/members")
    public CommonResult<Object> getSpaceMembers(@PathVariable String spaceId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "20") Integer pageSize) {
        CozeGetSpaceMembersRequest request = new CozeGetSpaceMembersRequest();
        request.setWorkspaceId(spaceId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        Object response = cozeService.getSpaceMembers(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "同步空间成员")
    // @PreAuthorize("hasAuthority('platform:coze:space:member:sync')")
    @PostMapping("/{spaceId}/members/sync")
    public CommonResult<Boolean> syncSpaceMembers(@PathVariable String spaceId) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeSpaceMemberService.syncSpaceMembersFromCoze(merchantId, spaceId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地空间成员列表")
    // @PreAuthorize("hasAuthority('platform:coze:space:member:list')")
    @GetMapping("/{spaceId}/members/local")
    public CommonResult<CommonPage<CozeSpaceMember>> getLocalSpaceMembers(@PathVariable String spaceId,
                                                                          PageParamRequest pageParamRequest) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeSpaceMember> response = cozeSpaceMemberService.getList(merchantId, spaceId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "删除空间成员")
    // @PreAuthorize("hasAuthority('platform:coze:space:member:delete')")
    @DeleteMapping("/member/{memberId}")
    public CommonResult<Boolean> deleteSpaceMember(@PathVariable Integer memberId) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeSpaceMemberService.deleteById(memberId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "同步Coze空间到本地")
    // @PreAuthorize("hasAuthority('platform:coze:space:sync')")
    @PostMapping("/sync")
    public CommonResult<String> syncSpaces() {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean result = cozeSpaceService.syncCozeSpaces(merchantId);
        if (result) {
            return CommonResult.success("同步成功");
        } else {
            return CommonResult.failed("同步失败");
        }
    }

    @ApiOperation(value = "获取本地空间列表")
    // @PreAuthorize("hasAuthority('platform:coze:space:local:list')")
    @GetMapping("/local/list")
    public CommonResult<CommonPage<CozeSpace>> getLocalSpaceList(@Validated PageParamRequest pageParamRequest) {
        CommonPage<CozeSpace> list = cozeSpaceService.getList(pageParamRequest);
        return CommonResult.success(list);
    }

    @ApiOperation(value = "获取当前平台的空间列表")
    // @PreAuthorize("hasAuthority('platform:coze:space:platform:list')")
    @GetMapping("/platform/list")
    public CommonResult<List<CozeSpace>> getPlatformSpaceList() {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        List<CozeSpace> list = cozeSpaceService.getByMerId(merchantId);
        return CommonResult.success(list);
    }

    @ApiOperation(value = "获取空间详情")
    // @PreAuthorize("hasAuthority('platform:coze:space:detail')")
    @GetMapping("/{spaceId}")
    public CommonResult<CozeSpace> getSpaceDetail(@PathVariable String spaceId) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeSpace space = cozeSpaceService.getBySpaceId(spaceId, merchantId);
        return CommonResult.success(space);
    }

    @ApiOperation(value = "删除本地空间记录")
    // @PreAuthorize("hasAuthority('platform:coze:space:delete')")
    @DeleteMapping("/local/{id}")
    public CommonResult<String> deleteLocalSpace(@PathVariable Integer id) {
        Boolean result = cozeSpaceService.deleteLocalSpace(id);
        if (result) {
            return CommonResult.success("删除成功");
        } else {
            return CommonResult.failed("删除失败");
        }
    }
}