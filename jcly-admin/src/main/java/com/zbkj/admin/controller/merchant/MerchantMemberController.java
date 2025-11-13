package com.zbkj.admin.controller.merchant;

import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.member.MemberIntegralRecord;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.MemberIntegralRequest;
import com.zbkj.common.request.MemberMessagePageRequest;
import com.zbkj.common.request.MemberPageRequest;
import com.zbkj.common.request.MemberSendCouponRequest;
import com.zbkj.common.request.MemberSendMessageRequest;
import com.zbkj.common.response.MerchantMemberMessageResponse;
import com.zbkj.common.response.StoreCouponUserResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.MemberService;
import com.zbkj.service.service.MerchantMemberMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商户端会员管理控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/member")
@Api(tags = "商户端-会员管理")
public class MerchantMemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MerchantMemberMessageService merchantMemberMessageService;

    /**
     * 会员列表
     */
   // @PreAuthorize("hasAuthority('merchant:member:list')")
    @ApiOperation(value = "会员列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<Member>> getList(@Validated MemberPageRequest request) {
        return CommonResult.success(CommonPage.restPage(memberService.getList(request)));
    }

    /**
     * 会员详情
     */
  //  @PreAuthorize("hasAuthority('merchant:member:detail')")
    @ApiOperation(value = "会员详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<Member> getDetail(@PathVariable Integer id) {
        return CommonResult.success(memberService.getDetail(id));
    }

    /**
     * 会员积分变更
     */
   // @PreAuthorize("hasAuthority('merchant:member:integral:change')")
    @ApiOperation(value = "会员积分变更")
    @RequestMapping(value = "/integral/change", method = RequestMethod.POST)
    public CommonResult<String> changeIntegral(@RequestBody @Validated MemberIntegralRequest request) {
        if (memberService.changeIntegral(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed("积分变更失败");
    }

    /**
     * 会员积分记录
     */
   // @PreAuthorize("hasAuthority('merchant:member:integral:list')")
    @ApiOperation(value = "会员积分记录")
    @RequestMapping(value = "/integral/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MemberIntegralRecord>> getIntegralList(@RequestParam Integer memberId,
                                                                          @RequestParam(defaultValue = "1") Integer page,
                                                                          @RequestParam(defaultValue = "10") Integer limit) {
        return CommonResult.success(CommonPage.restPage(memberService.getIntegralList(memberId, page, limit)));
    }

    /**
     * 会员订单记录
     */
   // @PreAuthorize("hasAuthority('merchant:member:order:list')")
    @ApiOperation(value = "会员订单记录")
    @RequestMapping(value = "/order/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<Order>> getOrderList(@RequestParam Integer memberId,
                                                                  @RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "10") Integer limit) {
        return CommonResult.success(CommonPage.restPage(memberService.getOrderList(memberId, page, limit)));
    }

    /**
     * 会员优惠券记录
     */
   // @PreAuthorize("hasAuthority('merchant:member:coupon:list')")
    @ApiOperation(value = "会员优惠券记录")
    @RequestMapping(value = "/coupon/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreCouponUserResponse>> getCouponList(@RequestParam Integer memberId,
                                                                           @RequestParam(defaultValue = "1") Integer page,
                                                                           @RequestParam(defaultValue = "10") Integer limit) {
        return CommonResult.success(CommonPage.restPage(memberService.getCouponList(memberId, page, limit)));
    }

    /**
     * 发送优惠券
     */
  //  @PreAuthorize("hasAuthority('merchant:member:coupon:send')")
    @ApiOperation(value = "发送优惠券")
    @RequestMapping(value = "/coupon/send", method = RequestMethod.POST)
    public CommonResult<String> sendCoupon(@RequestBody @Validated MemberSendCouponRequest request) {
        if (memberService.sendCoupon(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed("发送优惠券失败");
    }

    /**
     * 发送消息给会员
     */
  //  @PreAuthorize("hasAuthority('merchant:member:message:send')")
    @ApiOperation(value = "发送消息给会员")
    @RequestMapping(value = "/message/send", method = RequestMethod.POST)
    public CommonResult<String> sendMessage(@RequestBody @Validated MemberSendMessageRequest request) {
        if (merchantMemberMessageService.sendMessage(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed("发送消息失败");
    }

    /**
     * 获取消息列表
     */
  //  @PreAuthorize("hasAuthority('merchant:member:message:list')")
    @ApiOperation(value = "获取消息列表")
    @RequestMapping(value = "/message/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MerchantMemberMessageResponse>> getMessageList(@Validated MemberMessagePageRequest request) {
        return CommonResult.success(CommonPage.restPage(merchantMemberMessageService.getMessageList(request)));
    }

    /**
     * 获取消息详情
     */
  //  @PreAuthorize("hasAuthority('merchant:member:message:detail')")
    @ApiOperation(value = "获取消息详情")
    @RequestMapping(value = "/message/detail/{id}", method = RequestMethod.GET)
    public CommonResult<MerchantMemberMessageResponse> getMessageDetail(@PathVariable Integer id) {
        MerchantMemberMessageResponse response = merchantMemberMessageService.getMessageDetail(id);
        if (response != null) {
            return CommonResult.success(response);
        }
        return CommonResult.failed("消息不存在");
    }

    /**
     * 标记消息为已读
     */
  //  @PreAuthorize("hasAuthority('merchant:member:message:read')")
    @ApiOperation(value = "标记消息为已读")
    @RequestMapping(value = "/message/read/{id}", method = RequestMethod.PUT)
    public CommonResult<String> markMessageAsRead(@PathVariable Integer id) {
        if (merchantMemberMessageService.markAsRead(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed("标记已读失败");
    }

    /**
     * 删除消息
     */
  //  @PreAuthorize("hasAuthority('merchant:member:message:delete')")
    @ApiOperation(value = "删除消息")
    @RequestMapping(value = "/message/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<String> deleteMessage(@PathVariable Integer id) {
        if (merchantMemberMessageService.deleteMessage(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed("删除消息失败");
    }

    /**
     * 获取会员未读消息数量
     */
  //  @PreAuthorize("hasAuthority('merchant:member:message:unread:count')")
    @ApiOperation(value = "获取会员未读消息数量")
    @RequestMapping(value = "/message/unread/count", method = RequestMethod.GET)
    public CommonResult<Integer> getUnreadCount(@RequestParam Integer uid) {
        // 这里需要获取当前商户ID，通过SecurityUtil获取
        // Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        // int count = merchantMemberMessageService.getUnreadCount(merId, uid);
        // return CommonResult.success(count);
        return CommonResult.success(0); // 临时返回
    }
}