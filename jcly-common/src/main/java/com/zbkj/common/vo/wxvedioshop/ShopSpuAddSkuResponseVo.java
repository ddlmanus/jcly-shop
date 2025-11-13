package com.zbkj.common.vo.wxvedioshop;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 *  自定义交易组件商品添加响应Vo
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
public class ShopSpuAddSkuResponseVo {

    /** 交易组件平台自定义skuID */
    @TableField(value = "sku_id")
    private String skuId;

    /** 商家自定义skuID */
    @TableField(value = "out_sku_id")
    private String outSkuId;
}
