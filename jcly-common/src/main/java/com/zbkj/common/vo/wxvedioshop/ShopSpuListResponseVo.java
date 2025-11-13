package com.zbkj.common.vo.wxvedioshop;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

/**
 * 自定义交易组件商品列表请求Vo
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
public class ShopSpuListResponseVo {

    /** 总数 */
    @TableField(value = "total_num")
    private Integer totalNum;

    /** spu数组 */
    private List<ShopSpuVo> spus;
}
