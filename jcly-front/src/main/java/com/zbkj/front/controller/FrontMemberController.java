package com.zbkj.front.controller;

import com.zbkj.common.model.member.MemberLevel;
import com.zbkj.common.model.merchant.MerchantMemberMessage;
import com.zbkj.common.request.MemberSendSmsRequest;
import com.zbkj.common.response.MemberInfoResponse;
import com.zbkj.common.request.MemberRegisterRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.front.service.MerchantMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 前端会员控制器
 */
@Slf4j
@RestController
@RequestMapping("api/front/merchant-member")
@Api(tags = "前端 -- 商户会员管理")
@Validated
public class FrontMemberController {

    @Autowired
    private MerchantMemberService merchantMemberService;

    @ApiOperation(value = "会员注册")
    @PostMapping("/register")
    public CommonResult<MemberInfoResponse> register(@RequestBody @Valid MemberRegisterRequest request) {
        return CommonResult.success(merchantMemberService.register(request));
    }

    @ApiOperation(value = "会员等级")
    @GetMapping("/level")
    public CommonResult< List<MemberLevel>> getLevel(@RequestParam @NotNull(message = "商户ID不能为空") Integer merId) {
        return CommonResult.success(merchantMemberService.getLevel(merId));
    }
    /**
     * 获取会员的所有店铺消息
     * @return
     */
    @ApiOperation(value = "获取会员的所有店铺消息")
    @GetMapping("/get-merchant-message")
    public CommonResult<List<MerchantMemberMessage>> getMerchantMessage() {
        return CommonResult.success(merchantMemberService.getMerchantMessage());
    }

    /**
     * 根据店铺会员消息ID查看详情
     * @param id
     * @return
     */
    @ApiOperation(value = "根据店铺会员消息ID查看详情")
    @GetMapping("/get-merchant-message-info")
    public CommonResult<MerchantMemberMessage> getMerchantMessageInfo(@RequestParam @NotNull(message = "会员消息ID不能为空") Integer id) {
        return CommonResult.success(merchantMemberService.getMerchantMessageInfo(id));
    }

    @ApiOperation(value = "获取会员信息")
    @GetMapping("/info")
    public CommonResult<MemberInfoResponse> getMemberInfo(@RequestParam @NotNull(message = "商户ID不能为空") Integer merId) {
        return CommonResult.success(merchantMemberService.getMemberInfo(merId));
    }

    @ApiOperation(value = "发送注册验证码")
    @PostMapping("/send-register-sms")
    public CommonResult<String> sendRegisterSms(@RequestBody @Valid MemberSendSmsRequest request)  {
        merchantMemberService.sendRegisterSms(request.getPhone(), request.getMerId());
        return CommonResult.success("验证码发送成功");
    }

    @ApiOperation(value = "检查手机号是否已注册")
    @GetMapping("/check-phone")
    public CommonResult<Boolean> checkPhone(@RequestBody @Valid MemberSendSmsRequest request) {
        boolean exists = merchantMemberService.checkPhoneExists(request.getPhone(), request.getMerId());
        return CommonResult.success(exists);
    }

    /**
     * 设置商户会员消息为已读
     * @param id 消息ID
     * @return
     */
    @ApiOperation(value = "设置商户会员消息为已读")
    @GetMapping("/mark-message-read/{id}")
    public CommonResult<Boolean> markMessageAsRead(@PathVariable @NotNull(message = "消息ID不能为空") Integer id) {
        boolean success = merchantMemberService.markMessageAsRead(id);
        return CommonResult.success(success);
    }
    /**
     * 统计会员未读消息数量
     */
    @ApiOperation(value = "统计会员未读消息数量")
    @GetMapping("/unread-message-count")
    public CommonResult<Integer> unreadMessageCount() {
        return CommonResult.success(merchantMemberService.getUnreadMessageCount());
    }
}