package com.zbkj.common.response;


import com.zbkj.common.model.article.Article;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * 页面设计底部导航响应对象
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PageLayoutBottomNavigationResponse对象", description = "页面设计底部导航响应对象")
public class PageLayoutBottomNavigationResponse implements Serializable {

    private static final long serialVersionUID = 8350218800271787826L;

    @ApiModelProperty(value = "底部导航")
    private List<HashMap<String, Object>> bottomNavigationList;

    @ApiModelProperty(value = "是否自定义")
    private String isCustom;

    @ApiModelProperty(value = "中间按钮")
   private String centerButtonIndex;
    @ApiModelProperty(value = "按钮样式")
    private String tabBarStyleMode;

}
