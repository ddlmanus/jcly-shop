package com.zbkj.admin.controller.merchant;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.model.user.UserMerchantCollect;
import com.zbkj.admin.service.ShopFavoriteService;
import com.zbkj.common.result.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/shop/favorite")
@Api(tags = "商户端-店铺收藏管理")
public class MerchantShopFavoriteController {

    @Autowired
    private ShopFavoriteService shopFavoriteService;

    /**
     * 店铺收藏列表
     */
    @PreAuthorize("hasAuthority('merchant:shop:favorite:list')")
  //  @ApiOperation(value = "店铺收藏列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserMerchantCollect>> getList(
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "dateLimit", required = false) String dateLimit,
            PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(shopFavoriteService.getList(searchType, content, dateLimit, pageParamRequest)));
    }
}