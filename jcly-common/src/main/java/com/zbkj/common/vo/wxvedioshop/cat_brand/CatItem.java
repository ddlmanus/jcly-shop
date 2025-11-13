package com.zbkj.common.vo.wxvedioshop.cat_brand;

import lombok.Data;

import java.util.List;

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
public class CatItem {

    private Integer value;

    private String label;

    private String qualification;

    private Integer qualificationType;

    private String productQualification;

    private Integer productQualificationType;

    private List<CatItem> children;

    private Integer status;

    public CatItem() {
    }

    public CatItem(Integer value, String label, List<CatItem> childrens) {
        this.value = value;
        this.label = label;
        this.children = childrens;
    }
}
