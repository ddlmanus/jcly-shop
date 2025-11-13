package com.zbkj.common.vo.wxvedioshop.cat_brand;

import lombok.Data;

import java.util.List;

/**
 * 第一级类目
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
public class FirstCatVo {

    /** 一级类目ID */
    private Integer firstCatId;

    /** 一级类目名称 */
    private String firstCatName;

    /** 二级类目数组 */
    private List<SecondCatVo> secondCatList;

}
