package com.zbkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品导入结果VO对象
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
@ApiModel(value = "ProductImportResultVo对象", description = "商品导入结果VO对象")
public class ProductImportResultVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总处理数量")
    private Integer totalCount;

    @ApiModelProperty(value = "成功数量")
    private Integer successCount;

    @ApiModelProperty(value = "失败数量")
    private Integer failCount;

    @ApiModelProperty(value = "失败详情列表")
    private List<ProductImportErrorVo> errorList;

    @Data
    @ApiModel(value = "ProductImportErrorVo对象", description = "商品导入错误VO对象")
    public static class ProductImportErrorVo implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "行号")
        private Integer rowIndex;

        @ApiModelProperty(value = "商品名称")
        private String productName;

        @ApiModelProperty(value = "错误信息")
        private String errorMessage;
    }
} 