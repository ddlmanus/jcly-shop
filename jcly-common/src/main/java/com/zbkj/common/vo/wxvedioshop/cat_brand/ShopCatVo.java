package com.zbkj.common.vo.wxvedioshop.cat_brand;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

/**
 *  商品类型Vo对象
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
public class ShopCatVo {

    /** 错误码 */
    @TableField(value = "errcode")
    private Integer errCode;

    /** 错误信息 */
    @TableField(value = "errmsg")
    private Integer errMsg;

    /** 类目列表 */
    @TableField(value = "third_cat_list")
    private List<ShopCatDetailVo> thirdCatList;
}
