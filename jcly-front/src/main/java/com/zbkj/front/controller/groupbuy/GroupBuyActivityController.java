package com.zbkj.front.controller.groupbuy;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.groupbuy.GroupBuyProductCategorySearchRequest;
import com.zbkj.common.response.groupbuy.GroupBuyActivityFrontResponse;
import com.zbkj.common.response.groupbuy.GroupBuyActivityResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.ProCategoryCacheVo;
import com.zbkj.service.service.groupbuy.GroupBuyActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 拼团活动表 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("api/front/groupbuy/activity")
@Api(tags = "拼团活动") //配合swagger使用
public class GroupBuyActivityController {

    @Autowired
    private GroupBuyActivityService groupBuyActivityService;

    /**
     * 分页显示拼团活动表
     * @author dazongzi
     * @since 2024-08-13
     */
    @ApiOperation(value = "拼团 - 首页卡片数据获取") //配合swagger使用
    @RequestMapping(value = "/list/{limit}", method = RequestMethod.GET)
    public CommonResult<GroupBuyActivityFrontResponse> getList(@PathVariable(value = "limit") Integer limit) {
        if (ObjectUtil.isNull(limit)) throw new CrmebException("limit 不能为空");
        if (limit < 1 || limit > 6) throw new CrmebException("limit 只能在 1-6 之间");
        return CommonResult.success(groupBuyActivityService.getGroupBuyActivityFrontIndex(limit));
    }

    @ApiOperation(value = "商户首页拼团卡片数据获取") //配合swagger使用
    @RequestMapping(value = "/merchant/{merId}/page/{limit}", method = RequestMethod.GET)
    public CommonResult<GroupBuyActivityFrontResponse> getList(@PathVariable(value = "merId") Integer merId,
                                                               @PathVariable(value = "limit") Integer limit) {
        if (limit < 1 || limit > 6) {
            throw new CrmebException("limit 只能在 1-6 之间");
        }
        return CommonResult.success(groupBuyActivityService.getGroupBuyActivityMerchantFrontIndex(merId, limit));
    }

    @ApiOperation(value = "通过商品平台分类id查询拼团商品列表") //配合swagger使用
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    public CommonResult<PageInfo<GroupBuyActivityResponse>> getGroupBuyProductListByCategoryId(
            @Valid @ModelAttribute GroupBuyProductCategorySearchRequest request) {
        return CommonResult.success(groupBuyActivityService.getGroupBuyProductListByCategoryId(request));
    }

    @ApiOperation(value = "获取所有团购商品的父级分类列表") //配合swagger使用
    @RequestMapping(value = "/parent-categories", method = RequestMethod.GET)
    public CommonResult<List<ProCategoryCacheVo>> getParentCategoryIdsByGroupBuyProducts() {
        return CommonResult.success(groupBuyActivityService.getParentCategoryIdsByGroupBuyProducts());
    }
}



