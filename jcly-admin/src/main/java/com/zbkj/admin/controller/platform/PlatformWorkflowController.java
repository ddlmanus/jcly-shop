package com.zbkj.admin.controller.platform;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeWorkflow;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.coze.CozeExecuteWorkflowRequest;
import com.zbkj.common.request.coze.CozeGetWorkflowListRequest;
import com.zbkj.common.response.coze.CozeExecuteWorkflowResponse;
import com.zbkj.common.response.coze.CozeGetWorkflowListResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.CozeWorkflowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("api/admin/platform/coze/workflow")
@Api(tags = "平台端 - 工作流管理管理")
public class PlatformWorkflowController {
    @Resource
    private CozeWorkflowService cozeWorkflowService;
    @Resource
    private CozeService cozeService;

    @ApiOperation(value = "查询工作流列表 (Coze API)")
    //  @PreAuthorize("hasAuthority('merchant:coze:workflow:list')")
    @GetMapping("/list")
    public CommonResult<CozeGetWorkflowListResponse> getWorkflowList(@RequestParam String workspaceId,
                                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                                     @RequestParam(required = false) String workflowMode,
                                                                     @RequestParam(required = false) String appId,
                                                                     @RequestParam(required = false) String publishStatus) {
        CozeGetWorkflowListRequest request = CozeGetWorkflowListRequest.builder()
                .workspaceId(workspaceId)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .workflowMode(workflowMode)
                .appId(appId)
                .publishStatus(publishStatus)
                .build();
        CozeGetWorkflowListResponse response = cozeService.getWorkflowList(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地工作流列表")
    //  @PreAuthorize("hasAuthority('merchant:coze:workflow:view')")
    @GetMapping("/local/list")
    public CommonResult<CommonPage<CozeWorkflow>> getLocalWorkflowList(PageParamRequest pageParamRequest) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeWorkflow> response = cozeWorkflowService.getByMerchantId(merchantId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "获取本地工作流详情")
    //   @PreAuthorize("hasAuthority('merchant:coze:workflow:view')")
    @GetMapping("/local/detail/{cozeWorkflowId}")
    public CommonResult<CozeWorkflow> getLocalWorkflowDetail(@PathVariable String cozeWorkflowId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeWorkflow response = cozeWorkflowService.getByCozeWorkflowId(cozeWorkflowId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除本地工作流")
    //  @PreAuthorize("hasAuthority('merchant:coze:workflow:delete')")
    @DeleteMapping("/local/delete/{cozeWorkflowId}")
    public CommonResult<Boolean> deleteLocalWorkflow(@PathVariable String cozeWorkflowId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeWorkflowService.deleteByCozeWorkflowId(cozeWorkflowId, merchantId);
        return CommonResult.success(response);
    }
}
