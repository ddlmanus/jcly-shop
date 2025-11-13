package com.zbkj.common.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品导出VO对象
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
@ApiModel(value = "ProductExcelVo对象", description = "商品导出VO对象")
public class ProductExcelVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "商品ID", index = 0)
    @ApiModelProperty(value = "商品ID")
    private String id;

    @ExcelProperty(value = "商品名称", index = 1)
    @ApiModelProperty(value = "商品名称")
    private String name;

    @ExcelProperty(value = "商品分类", index = 2)
    @ApiModelProperty(value = "商品分类")
    private String categoryName;

    @ExcelProperty(value = "商品类型", index = 3)
    @ApiModelProperty(value = "商品类型")
    private String productType;

    @ExcelProperty(value = "商户名称", index = 4)
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ExcelProperty(value = "商户类别", index = 5)
    @ApiModelProperty(value = "商户类别")
    private String merchantType;

    @ExcelProperty(value = "商品价格", index = 6)
    @ApiModelProperty(value = "商品价格")
    private String price;

    @ExcelProperty(value = "销量", index = 7)
    @ApiModelProperty(value = "销量")
    private String sales;

    @ExcelProperty(value = "库存", index = 8)
    @ApiModelProperty(value = "库存")
    private String stock;

    @ExcelProperty(value = "虚拟销量", index = 9)
    @ApiModelProperty(value = "虚拟销量")
    private String ficti;

    @ExcelProperty(value = "商品状态", index = 10)
    @ApiModelProperty(value = "商品状态")
    private String isShow;

    @ExcelProperty(value = "审核状态", index = 11)
    @ApiModelProperty(value = "审核状态")
    private String auditStatus;

    @ExcelProperty(value = "拒绝原因", index = 12)
    @ApiModelProperty(value = "拒绝原因")
    private String reason;

    @ExcelProperty(value = "规格类型", index = 13)
    @ApiModelProperty(value = "规格类型")
    private String specType;

    @ExcelProperty(value = "创建时间", index = 14)
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ExcelProperty(value = "更新时间", index = 15)
    @ApiModelProperty(value = "更新时间")
    private String updateTime;
} 