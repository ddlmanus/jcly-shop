package com.zbkj.common.vo.wxvedioshop.order;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 生成订单Vo对象
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Data
public class ShopOrderDetailAddVo {

    /** 商品详情数组 */
    @TableField(value = "product_infos")
    @ApiModelProperty(value = "商品列表")
    @NotEmpty
    private List<ShopOrderProductInfoAddVo> productInfos;

    /** 支付详情数组 */
    @TableField(value = "pay_info")
    @ApiModelProperty(value = "价格信息")
    @NotEmpty
    private ShopOrderPayInfoAddVo payInfo;

    /** 价格详情数组 */
    @TableField(value = "price_info")
    @ApiModelProperty(value = "fund_type = 0 必传")
    private ShopOrderPriceInfoVo priceInfo;
}
