package com.zbkj.common.vo.wxvedioshop;

import lombok.Data;

import java.util.List;

/**
 *  自定义交易组件商品详情Vo
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
public class ShopSpuInfoVo {

    /** 商品详情图文 */
    private String desc;

    /** 商品详情图片 */
    private List<String> imgs;

}
