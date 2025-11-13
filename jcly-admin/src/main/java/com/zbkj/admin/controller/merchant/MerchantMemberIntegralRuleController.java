package com.zbkj.admin.controller.merchant;

import com.zbkj.common.model.member.MemberIntegralRule;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.MemberIntegralRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 会员积分规则控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/member/integral")
@Api(tags = "商户端-会员积分规则管理")
public class MerchantMemberIntegralRuleController {

    @Autowired
    private MemberIntegralRuleService memberIntegralRuleService;

    /**
     * 获取积分规则
     */
   // @PreAuthorize("hasAuthority('merchant:member:integral:rule')")
    @ApiOperation(value = "获取积分规则")
    @RequestMapping(value = "/rule", method = RequestMethod.GET)
    public CommonResult<MemberIntegralRule> getRule() {
        return CommonResult.success(memberIntegralRuleService.getRule());
    }

    /**
     * 保存积分规则
     */
  //  @PreAuthorize("hasAuthority('merchant:member:integral:rule:save')")
    @ApiOperation(value = "保存积分规则")
    @RequestMapping(value = "/rule/save", method = RequestMethod.POST)
    public CommonResult<String> saveRule(@RequestBody MemberIntegralRule memberIntegralRule) {
        if (memberIntegralRuleService.saveRule(memberIntegralRule)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }
}