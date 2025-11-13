package com.zbkj.common.vo.wxvedioshop.cat_brand;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 *
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
public class ShopBrandVo {

    /** 品牌ID */
    @TableField(value = "brand_id")
    private Integer brandId;

    /** 品牌名称 */
    @TableField(value = "brand_wording")
    private String brandWording;

}
