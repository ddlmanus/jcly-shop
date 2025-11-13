package com.zbkj.front.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CsjProductSearchRequest;
import com.zbkj.common.response.CsjProductInfoResponse;
import com.zbkj.common.response.CsjProductListResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CsjProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/front/csj/product")
@Api(tags = "小程序-采食家商品控制器")
public class AppCsjProductController {


    @Autowired
    private CsjProductService csjProductService;
    @ApiOperation(value = "采食家商品分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CsjProductListResponse>> getList(@Validated CsjProductSearchRequest request) {
        return CommonResult.success(CommonPage.restPage(csjProductService.getList(request)));
    }
    @ApiOperation(value = "采食家商品详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<CsjProductInfoResponse> info(@PathVariable Integer id) {
        return CommonResult.success(csjProductService.getInfo(id));
    }

}
