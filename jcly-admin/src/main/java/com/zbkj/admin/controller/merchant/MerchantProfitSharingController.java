package com.zbkj.admin.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.model.merchant.MerchantProfitSharingApply;
import com.zbkj.common.model.merchant.MerchantProfitSharingConfig;
import com.zbkj.common.model.merchant.MerchantProfitSharingDetail;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.MerchantProfitSharingApplyService;
import com.zbkj.service.service.MerchantProfitSharingConfigService;
import com.zbkj.service.service.MerchantProfitSharingDetailService;
import com.zbkj.common.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 商户端分账管理控制器
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchant/profit-sharing")
@Api(tags = "商户端分账管理控制器")
@Validated
public class MerchantProfitSharingController {

    @Autowired
    private MerchantProfitSharingConfigService configService;

    @Autowired
    private MerchantProfitSharingApplyService applyService;

    @Autowired
    private MerchantProfitSharingDetailService detailService;

  //  @PreAuthorize("hasAuthority('merchant:profit-sharing:config:info')")
    @ApiOperation(value = "获取分账配置信息")
    @RequestMapping(value = "/config/info", method = RequestMethod.GET)
    public CommonResult<MerchantProfitSharingConfig> getConfigInfo() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        MerchantProfitSharingConfig config = configService.getByMerId(merId);
        return CommonResult.success(config);
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "保存分账配置")
  //  @PreAuthorize("hasAuthority('merchant:profit-sharing:config:save')")
    @ApiOperation(value = "保存分账配置")
    @RequestMapping(value = "/config/save", method = RequestMethod.POST)
    public CommonResult<String> saveConfig(@RequestBody @Validated MerchantProfitSharingConfig config) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        config.setMerId(merId);
        
        if (configService.saveOrUpdateConfig(config)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "启用/禁用分账")
  //  @PreAuthorize("hasAuthority('merchant:profit-sharing:config:toggle')")
    @ApiOperation(value = "启用/禁用分账")
    @RequestMapping(value = "/config/toggle/{isEnabled}", method = RequestMethod.POST)
    public CommonResult<String> toggleEnabled(@PathVariable Boolean isEnabled) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        
        if (configService.updateEnabled(merId, isEnabled)) {
            return CommonResult.success(isEnabled ? "分账已启用" : "分账已禁用");
        }
        return CommonResult.failed("操作失败");
    }

   // @PreAuthorize("hasAuthority('merchant:profit-sharing:apply:list')")
    @ApiOperation(value = "分账申请列表")
    @RequestMapping(value = "/apply/list", method = RequestMethod.GET)
    public CommonResult<PageInfo<MerchantProfitSharingApply>> getApplyList(@Validated PageParamRequest pageParamRequest) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        PageInfo<MerchantProfitSharingApply> pageInfo = applyService.getPage(merId, pageParamRequest);
        return CommonResult.success(pageInfo);
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "提交分账申请")
  //  @PreAuthorize("hasAuthority('merchant:profit-sharing:apply:submit')")
    @ApiOperation(value = "提交分账申请")
    @RequestMapping(value = "/apply/submit", method = RequestMethod.POST)
    public CommonResult<String> submitApply(@RequestBody @Validated MerchantProfitSharingApply apply) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        apply.setMerId(merId);
        
        if (applyService.submitApply(apply)) {
            return CommonResult.success("申请提交成功，请等待审核");
        }
        return CommonResult.failed("申请提交失败");
    }

  //  @PreAuthorize("hasAuthority('merchant:profit-sharing:detail:list')")
    @ApiOperation(value = "分账明细列表")
    @RequestMapping(value = "/detail/list", method = RequestMethod.GET)
    public CommonResult<PageInfo<MerchantProfitSharingDetail>> getDetailList(@Validated PageParamRequest pageParamRequest) {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        PageInfo<MerchantProfitSharingDetail> pageInfo = detailService.getPage(merId, pageParamRequest);
        return CommonResult.success(pageInfo);
    }

  //  @PreAuthorize("hasAuthority('merchant:profit-sharing:detail:info')")
    @ApiOperation(value = "分账明细详情")
    @RequestMapping(value = "/detail/info/{id}", method = RequestMethod.GET)
    public CommonResult<MerchantProfitSharingDetail> getDetailInfo(@PathVariable Integer id) {
        MerchantProfitSharingDetail detail = detailService.getById(id);
        return CommonResult.success(detail);
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "重试分账")
  //  @PreAuthorize("hasAuthority('merchant:profit-sharing:detail:retry')")
    @ApiOperation(value = "重试分账")
    @RequestMapping(value = "/detail/retry/{id}", method = RequestMethod.POST)
    public CommonResult<String> retrySharing(@PathVariable Integer id) {
        if (detailService.retrySharing(id)) {
            return CommonResult.success("重试成功");
        }
        return CommonResult.failed("重试失败");
    }

   // @PreAuthorize("hasAuthority('merchant:profit-sharing:stats')")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "分账统计")
    @ApiOperation(value = "分账统计")
    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getStats() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        Integer merId = loginUserVo.getUser().getMerId();
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取分账配置
            MerchantProfitSharingConfig config = configService.getByMerId(merId);
            stats.put("isEnabled", config != null && config.getIsEnabled());
            stats.put("sharingRatio", config != null ? config.getSharingRatio() : BigDecimal.ZERO);
            
            // 获取分账统计数据
            Map<String, Object> profitSharingStats = detailService.getProfitSharingStats(merId);
            stats.putAll(profitSharingStats);
            
            // 获取申请统计数据
            Map<String, Object> applyStats = applyService.getApplyStats(merId);
            stats.putAll(applyStats);
            
            return CommonResult.success(stats);
        } catch (Exception e) {
            log.error("获取分账统计数据失败", e);
            return CommonResult.failed("获取统计数据失败");
        }
    }
} 