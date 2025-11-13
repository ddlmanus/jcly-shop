package com.zbkj.common.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品导入错误数据导出VO对象
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
@ApiModel(value = "ProductImportErrorExportVo对象", description = "商品导入错误数据导出VO对象")
public class ProductImportErrorExportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "行号", index = 0)
    @ApiModelProperty(value = "行号")
    private Integer rowIndex;

    @ExcelProperty(value = "商品名称", index = 1)
    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ExcelProperty(value = "错误信息", index = 2)
    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ExcelProperty(value = "导入时间", index = 3)
    @ApiModelProperty(value = "导入时间")
    private String importTime;

    @ExcelProperty(value = "错误分类", index = 4)
    @ApiModelProperty(value = "错误分类")
    private String errorType;

    @ExcelProperty(value = "建议解决方案", index = 5)
    @ApiModelProperty(value = "建议解决方案")
    private String suggestion;
} 