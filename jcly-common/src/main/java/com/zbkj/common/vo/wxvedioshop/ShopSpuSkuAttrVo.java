package com.zbkj.common.vo.wxvedioshop;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 *  自定义交易组件商品SkuAttrVo
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
public class ShopSpuSkuAttrVo {

    /** 销售属性key（自定义） */
    @TableField(value = "attr_key")
    private String attrKey;

    /** 销售属性value（自定义） */
    @TableField(value = "attr_value")
    private String attrValue;
}
