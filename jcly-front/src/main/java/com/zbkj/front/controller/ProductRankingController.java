package com.zbkj.front.controller;

import com.zbkj.common.model.product.Product;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品排行榜控制器
 */
@Slf4j
@RestController
@RequestMapping("api/front/product/ranking")
@Api(tags = "商品排行榜")
public class ProductRankingController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品销量排行榜（前50名）
     */
    @ApiOperation(value = "获取商品销量排行榜")
    @RequestMapping(value = "/sales", method = RequestMethod.GET)
    public CommonResult<CommonPage<Product>> getSalesRanking(PageParamRequest pageParamRequest) {
        // 限制最多返回50条
        if (pageParamRequest.getLimit() > 50) {
            pageParamRequest.setLimit(50);
        }
        
        List<Product> rankingList = productService.getSalesRanking(pageParamRequest);
        return CommonResult.success(CommonPage.restPage(rankingList));
    }

    /**
     * 获取商品收藏排行榜（前50名）
     */
    @ApiOperation(value = "获取商品收藏排行榜")
    @RequestMapping(value = "/collect", method = RequestMethod.GET)
    public CommonResult<CommonPage<Product>> getCollectRanking(PageParamRequest pageParamRequest) {
        // 限制最多返回50条
        if (pageParamRequest.getLimit() > 50) {
            pageParamRequest.setLimit(50);
        }
        
        List<Product> rankingList = productService.getCollectRanking(pageParamRequest);
        return CommonResult.success(CommonPage.restPage(rankingList));
    }

    /**
     * 获取商品浏览排行榜（前50名）
     */
    @ApiOperation(value = "获取商品浏览排行榜")
    @RequestMapping(value = "/browse", method = RequestMethod.GET)
    public CommonResult<CommonPage<Product>> getBrowseRanking(PageParamRequest pageParamRequest) {
        // 限制最多返回50条
        if (pageParamRequest.getLimit() > 50) {
            pageParamRequest.setLimit(50);
        }
        
        List<Product> rankingList = productService.getBrowseRanking(pageParamRequest);
        return CommonResult.success(CommonPage.restPage(rankingList));
    }

    /**
     * 获取分类下的商品销量排行榜
     */
    @ApiOperation(value = "获取分类商品销量排行榜")
    @RequestMapping(value = "/category-sales", method = RequestMethod.GET)
    public CommonResult<CommonPage<Product>> getCategorySalesRanking(
            @RequestParam Integer categoryId,
            PageParamRequest pageParamRequest) {
        // 限制最多返回50条
        if (pageParamRequest.getLimit() > 50) {
            pageParamRequest.setLimit(50);
        }
        
        List<Product> rankingList = productService.getCategorySalesRanking(categoryId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(rankingList));
    }
}

