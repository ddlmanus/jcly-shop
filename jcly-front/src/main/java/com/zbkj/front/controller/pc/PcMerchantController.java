package com.zbkj.front.controller.pc;

import com.zbkj.common.response.MerchantPcIndexResponse;
import com.zbkj.common.response.PcMerchantRecommendProductResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.front.service.FrontProductService;
import com.zbkj.service.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * PC商户控制器
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
@RequestMapping("api/front/merchant/pc")
@Api(tags = "PC商户控制器")
public class PcMerchantController {

    @Autowired
    private MerchantService merchantService;
    @Autowired
    private FrontProductService frontProductService;

    @ApiOperation(value = "店铺首页信息")
    @RequestMapping(value = "/index/{id}", method = RequestMethod.GET)
    public CommonResult<MerchantPcIndexResponse> getIndexInfo(@PathVariable Integer id) {
        return CommonResult.success(merchantService.getPcIndexByMerId(id));
    }

    @ApiOperation(value = "店铺推荐商品")
    @RequestMapping(value = "/{id}/recommend/product", method = RequestMethod.GET)
    public CommonResult<List<PcMerchantRecommendProductResponse>> getRecommendProduct(@PathVariable Integer id) {
        return CommonResult.success(frontProductService.getRecommendProductByMerId(id));
    }
}
