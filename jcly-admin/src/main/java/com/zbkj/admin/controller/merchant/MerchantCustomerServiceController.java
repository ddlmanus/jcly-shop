package com.zbkj.admin.controller.merchant;

import com.zbkj.common.model.service.CustomerServiceStaff;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CustomerServiceLoginRequest;
import com.zbkj.common.request.CustomerServiceRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CustomerServiceLoginResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CustomerServiceStaffService;
import com.zbkj.service.service.SmsService;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.common.constants.SmsConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户端客服管理控制器
 *
 * @author AI Assistant
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/customer-service")
@Api(tags = "商户端 - 客服管理")
public class MerchantCustomerServiceController {

    @Autowired
    private CustomerServiceStaffService customerServiceStaffService;

    @Autowired
    private SmsService smsService;

    /**
     * 发送客服登录验证码
     */
    @ApiOperation(value = "发送客服登录验证码")
    @PostMapping("/send-code")
    public CommonResult<String> sendLoginCode(@RequestParam String phone) {
        try {
            log.info("发送客服登录验证码请求: phone={}", phone);
            Boolean result = smsService.sendCommonCode(phone, SmsConstants.VERIFICATION_CODE_SCENARIO_CUSTOMER_SERVICE_LOGIN);
            if (result) {
                return CommonResult.success("验证码发送成功");
            } else {
                return CommonResult.failed("验证码发送失败");
            }
        } catch (Exception e) {
            log.error("发送客服登录验证码失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 客服登录（独立登录接口）
     */
    @ApiOperation(value = "客服登录")
    @PostMapping("/login")
    public CommonResult<CustomerServiceLoginResponse> login(
            @RequestBody @Validated CustomerServiceLoginRequest request) {
        try {
            log.info("客服登录请求: account={}", request.getAccount());
            CustomerServiceLoginResponse response = customerServiceStaffService.login(request);
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("客服登录失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获取当前登录客服信息
     */
    @ApiOperation(value = "获取当前登录客服信息")
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('merchant:customer-service:profile')")
    public CommonResult<CustomerServiceStaff> getProfile() {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer adminId = loginUser.getUser().getId();

            CustomerServiceStaff staff = customerServiceStaffService.getByAdminId(adminId);
            if (staff == null) {
                return CommonResult.failed("客服信息不存在");
            }

            return CommonResult.success(staff);
        } catch (Exception e) {
            log.error("获取客服信息失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 更新客服在线状态
     */
    @ApiOperation(value = "更新客服在线状态")
    @PutMapping("/status")
    @PreAuthorize("hasAuthority('merchant:customer-service:status')")
    public CommonResult<Void> updateStatus(@RequestParam String status) {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer adminId = loginUser.getUser().getId();

            customerServiceStaffService.updateOnlineStatus(adminId, status);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("更新客服状态失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 创建客服账号
     */
    @ApiOperation(value = "创建客服账号")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('merchant:customer-service:create')")
    public CommonResult<CustomerServiceStaff> create(
            @RequestBody @Validated CustomerServiceRequest request) {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();

            CustomerServiceStaff staff = customerServiceStaffService.createCustomerService(request, merId);
            return CommonResult.success(staff);
        } catch (Exception e) {
            log.error("创建客服失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 编辑客服信息
     */
    @ApiOperation(value = "编辑客服信息")
    @PutMapping("/{staffId}")
    @PreAuthorize("hasAuthority('merchant:customer-service:update')")
    public CommonResult<Void> update(
            @PathVariable Integer staffId,
            @RequestBody @Validated CustomerServiceRequest request) {
        try {
            request.setStaffId(staffId);
            customerServiceStaffService.updateCustomerService(request);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("编辑客服失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 启用/禁用客服
     */
    @ApiOperation(value = "启用/禁用客服")
    @PutMapping("/{staffId}/status")
    @PreAuthorize("hasAuthority('merchant:customer-service:status:update')")
    public CommonResult<Void> updateStaffStatus(
            @PathVariable Integer staffId,
            @RequestParam Boolean status) {
        try {
            customerServiceStaffService.updateStatus(staffId, status);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("更新客服状态失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 删除客服
     */
    @ApiOperation(value = "删除客服")
    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasAuthority('merchant:customer-service:delete')")
    public CommonResult<Void> delete(@PathVariable Integer staffId) {
        try {
            customerServiceStaffService.deleteCustomerService(staffId);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("删除客服失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获取客服列表（分页）
     */
    @ApiOperation(value = "获取客服列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('merchant:customer-service:list')")
    public CommonResult<CommonPage<CustomerServiceStaff>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String onlineStatus,
            @RequestParam(required = false) Boolean status,
            PageParamRequest pageParamRequest) {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();

            CommonPage<CustomerServiceStaff> page = customerServiceStaffService.getPageList(
                    merId, keyword, onlineStatus, status, pageParamRequest);
            return CommonResult.success(page);
        } catch (Exception e) {
            log.error("获取客服列表失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获取客服详情
     */
    @ApiOperation(value = "获取客服详情")
    @GetMapping("/{staffId}")
    @PreAuthorize("hasAuthority('merchant:customer-service:detail')")
    public CommonResult<CustomerServiceStaff> getDetail(@PathVariable Integer staffId) {
        try {
            CustomerServiceStaff staff = customerServiceStaffService.getById(staffId);
            if (staff == null) {
                return CommonResult.failed("客服不存在");
            }
            return CommonResult.success(staff);
        } catch (Exception e) {
            log.error("获取客服详情失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 重置客服密码
     */
    @ApiOperation(value = "重置客服密码")
    @PutMapping("/{staffId}/reset-password")
    @PreAuthorize("hasAuthority('merchant:customer-service:reset-password')")
    public CommonResult<String> resetPassword(
            @PathVariable Integer staffId,
            @RequestParam String newPassword) {
        try {
            customerServiceStaffService.resetPassword(staffId, newPassword);
            return CommonResult.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获取在线客服列表
     */
    @ApiOperation(value = "获取在线客服列表")
    @GetMapping("/online")
    @PreAuthorize("hasAuthority('merchant:customer-service:online:list')")
    public CommonResult<List<CustomerServiceStaff>> getOnlineStaff() {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();

            List<CustomerServiceStaff> staffList = customerServiceStaffService.getOnlineStaff(merId);
            return CommonResult.success(staffList);
        } catch (Exception e) {
            log.error("获取在线客服列表失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获取可用客服列表（用于分配）
     */
    @ApiOperation(value = "获取可用客服列表")
    @GetMapping("/available")
    @PreAuthorize("hasAuthority('merchant:customer-service:available:list')")
    public CommonResult<List<CustomerServiceStaff>> getAvailableStaff() {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();

            List<CustomerServiceStaff> staffList = customerServiceStaffService.getAvailableStaff(merId.longValue());
            return CommonResult.success(staffList);
        } catch (Exception e) {
            log.error("获取可用客服列表失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获取客服工作统计
     */
    @ApiOperation(value = "获取客服工作统计")
    @GetMapping("/{staffId}/statistics")
    @PreAuthorize("hasAuthority('merchant:customer-service:statistics')")
    public CommonResult<Map<String, Object>> getStatistics(
            @PathVariable Integer staffId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> statistics = customerServiceStaffService.getStaffStatistics(
                    staffId, startDate, endDate);
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取客服统计失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 设置默认客服
     */
    @ApiOperation(value = "设置默认客服")
    @PutMapping("/{staffId}/set-default")
    @PreAuthorize("hasAuthority('merchant:customer-service:set-default')")
    public CommonResult<Void> setDefaultStaff(@PathVariable Integer staffId) {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();

            customerServiceStaffService.setDefaultStaff(staffId, merId);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("设置默认客服失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获取我的待处理会话数量
     */
    @ApiOperation(value = "获取我的待处理会话数量")
    @GetMapping("/my/pending-count")
    @PreAuthorize("hasAuthority('merchant:customer-service:my:pending-count')")
    public CommonResult<Integer> getMyPendingSessionCount() {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer adminId = loginUser.getUser().getId();

            CustomerServiceStaff staff = customerServiceStaffService.getByAdminId(adminId);
            if (staff == null) {
                return CommonResult.success(0);
            }

            Integer count = customerServiceStaffService.getPendingSessionCount(staff.getId());
            return CommonResult.success(count);
        } catch (Exception e) {
            log.error("获取待处理会话数量失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 客服工号生成建议
     */
    @ApiOperation(value = "客服工号生成建议")
    @GetMapping("/generate-staff-no")
    @PreAuthorize("hasAuthority('merchant:customer-service:generate-no')")
    public CommonResult<String> generateStaffNo() {
        try {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merId = loginUser.getUser().getMerId();

            String staffNo = customerServiceStaffService.generateStaffNo(merId);
            return CommonResult.success(staffNo);
        } catch (Exception e) {
            log.error("生成客服工号失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }
}
