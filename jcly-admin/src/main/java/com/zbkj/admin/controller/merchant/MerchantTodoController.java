package com.zbkj.admin.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.MerchantTodoItem;
import com.zbkj.common.model.merchant.TodoOperationLog;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.MerchantTodoItemService;
import com.zbkj.service.service.TodoOperationLogService;
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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 商户待办事项管理控制器
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
@RequestMapping("api/admin/merchant/todo")
@Api(tags = "商户待办事项管理")
@Validated
public class MerchantTodoController {

    @Autowired
    private MerchantTodoItemService merchantTodoItemService;

    @Autowired
    private TodoOperationLogService todoOperationLogService;

    /**
     * 分页获取待办事项列表
     */
   // @PreAuthorize("hasAuthority('merchant:todo:list')")
    @ApiOperation(value = "分页获取待办事项列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "事项类型", dataType = "String"),
            @ApiImplicitParam(name = "priority", value = "优先级", dataType = "int"),
            @ApiImplicitParam(name = "status", value = "状态", dataType = "int")
    })
    public CommonResult<CommonPage<MerchantTodoItem>> getList(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "priority", required = false) Integer priority,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        PageInfo<MerchantTodoItem> pageInfo = merchantTodoItemService.getPage(
                loginUserVo.getUser().getMerId(), type, priority, status, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(pageInfo));
    }

    /**
     * 创建待办事项
     */
   // @PreAuthorize("hasAuthority('merchant:todo:create')")
    @ApiOperation(value = "创建待办事项")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<String> create(@RequestBody @Valid MerchantTodoItem todoItem) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        todoItem.setMerId(loginUserVo.getUser().getMerId());
        Boolean result = merchantTodoItemService.create(todoItem);
        if (result) {
            return CommonResult.success("创建待办事项成功");
        }
        return CommonResult.failed("创建待办事项失败");
    }

    /**
     * 标记事项为已完成
     */
   // @PreAuthorize("hasAuthority('merchant:todo:complete')")
    @ApiOperation(value = "标记事项为已完成")
    @RequestMapping(value = "/complete/{id}", method = RequestMethod.POST)
    public CommonResult<String> complete(@PathVariable @NotNull Integer id) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        // 验证待办事项是否属于当前商户
        MerchantTodoItem todoItem = merchantTodoItemService.getDetailById(id);
        if (!todoItem.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限操作此待办事项");
        }
        Boolean result = merchantTodoItemService.complete(id, loginUserVo.getUser().getId());
        if (result) {
            return CommonResult.success("标记完成成功");
        }
        return CommonResult.failed("标记完成失败");
    }

    /**
     * 批量标记事项为已完成
     */
   // @PreAuthorize("hasAuthority('merchant:todo:batch:complete')")
    @ApiOperation(value = "批量标记事项为已完成")
    @RequestMapping(value = "/batch/complete", method = RequestMethod.POST)
    public CommonResult<String> batchComplete(@RequestBody @NotEmpty List<Integer> ids) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        // 验证所有待办事项是否属于当前商户
        for (Integer id : ids) {
            MerchantTodoItem todoItem = merchantTodoItemService.getDetailById(id);
            if (!todoItem.getMerId().equals(loginUserVo.getUser().getMerId())) {
                return CommonResult.failed("无权限操作待办事项ID: " + id);
            }
        }
        Boolean result = merchantTodoItemService.batchComplete(ids, loginUserVo.getUser().getId());
        if (result) {
            return CommonResult.success("批量标记完成成功");
        }
        return CommonResult.failed("批量标记完成失败");
    }

    /**
     * 获取待办事项详情
     */
   // @PreAuthorize("hasAuthority('merchant:todo:detail')")
    @ApiOperation(value = "获取待办事项详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<MerchantTodoItem> getDetail(@PathVariable @NotNull Integer id) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        MerchantTodoItem todoItem = merchantTodoItemService.getDetailById(id);
        if (!todoItem.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限查看此待办事项");
        }
        return CommonResult.success(todoItem);
    }

    /**
     * 获取待办事项统计信息
     */
  //  @PreAuthorize("hasAuthority('merchant:todo:statistics')")
    @ApiOperation(value = "获取待办事项统计信息")
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getStatistics() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Map<String, Object> statistics = merchantTodoItemService.getTodoStatistics(
                loginUserVo.getUser().getMerId());
        return CommonResult.success(statistics);
    }

    /**
     * 获取今日已办事项数量
     */
    @PreAuthorize("hasAuthority('merchant:todo:today:completed')")
    @ApiOperation(value = "获取今日已办事项数量")
    @RequestMapping(value = "/today/completed", method = RequestMethod.GET)
    public CommonResult<Integer> getTodayCompletedCount() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer count = merchantTodoItemService.getTodayCompletedCount(
                loginUserVo.getUser().getMerId());
        return CommonResult.success(count);
    }

    /**
     * 根据类型获取待办事项数量
     */
   // @PreAuthorize("hasAuthority('merchant:todo:count')")
    @ApiOperation(value = "根据类型获取待办事项数量")
    @RequestMapping(value = "/count/{type}", method = RequestMethod.GET)
    public CommonResult<Integer> getCountByType(@PathVariable @NotNull String type) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer count = merchantTodoItemService.getCountByType(
                loginUserVo.getUser().getMerId(), type);
        return CommonResult.success(count);
    }

    /**
     * 自动创建订单相关待办事项
     */
   // @PreAuthorize("hasAuthority('merchant:todo:create:order')")
    @ApiOperation(value = "自动创建订单相关待办事项")
    @RequestMapping(value = "/create/order", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "事项类型", required = true, dataType = "String")
    })
    public CommonResult<String> createOrderTodoItem(
            @RequestParam @NotNull String orderId,
            @RequestParam @NotNull String type) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Boolean result = merchantTodoItemService.createOrderTodoItem(
                loginUserVo.getUser().getMerId(), orderId, type);
        if (result) {
            return CommonResult.success("创建订单待办事项成功");
        }
        return CommonResult.failed("创建订单待办事项失败");
    }

    /**
     * 自动创建商品相关待办事项
     */
   // @PreAuthorize("hasAuthority('merchant:todo:create:product')")
    @ApiOperation(value = "自动创建商品相关待办事项")
    @RequestMapping(value = "/create/product", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "商品ID", required = true, dataType = "int"),
            @ApiImplicitParam(name = "type", value = "事项类型", required = true, dataType = "String")
    })
    public CommonResult<String> createProductTodoItem(
            @RequestParam @NotNull Integer productId,
            @RequestParam @NotNull String type) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Boolean result = merchantTodoItemService.createProductTodoItem(
                loginUserVo.getUser().getMerId(), productId, type);
        if (result) {
            return CommonResult.success("创建商品待办事项成功");
        }
        return CommonResult.failed("创建商品待办事项失败");
    }

    /**
     * 获取操作日志列表
     */
   // @PreAuthorize("hasAuthority('merchant:todo:operation:log')")
    @ApiOperation(value = "获取操作日志列表")
    @RequestMapping(value = "/operation/log", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "todoItemId", value = "待办事项ID", dataType = "int"),
            @ApiImplicitParam(name = "operationType", value = "操作类型", dataType = "String")
    })
    public CommonResult<PageInfo<TodoOperationLog>> getOperationLogs(
            @RequestParam(value = "todoItemId", required = false) Integer todoItemId,
            @RequestParam(value = "operationType", required = false) String operationType,
            @Validated PageParamRequest pageParamRequest) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        PageInfo<TodoOperationLog> pageInfo = todoOperationLogService.getPage(
                todoItemId, operationType, loginUserVo.getUser().getId(), pageParamRequest);
        return CommonResult.success(pageInfo);
    }

    /**
     * 根据待办事项ID获取操作日志
     */
   // @PreAuthorize("hasAuthority('merchant:todo:operation:log:detail')")
    @ApiOperation(value = "根据待办事项ID获取操作日志")
    @RequestMapping(value = "/operation/log/{todoItemId}", method = RequestMethod.GET)
    public CommonResult<List<TodoOperationLog>> getOperationLogsByTodoItemId(
            @PathVariable @NotNull Integer todoItemId) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        // 验证待办事项是否属于当前商户
        MerchantTodoItem todoItem = merchantTodoItemService.getDetailById(todoItemId);
        if (!todoItem.getMerId().equals(loginUserVo.getUser().getMerId())) {
            return CommonResult.failed("无权限查看此待办事项的操作日志");
        }
        List<TodoOperationLog> logs = todoOperationLogService.getByTodoItemId(todoItemId);
        return CommonResult.success(logs);
    }
}