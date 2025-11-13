package com.zbkj.common.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品规格配置VO对象
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
@ApiModel(value = "ProductSpecConfigVo对象", description = "商品规格配置VO对象")
public class ProductSpecConfigVo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ExcelProperty(value = "商品名称", index = 0)
    @ApiModelProperty(value = "商品名称")
    private String productName;
    
    @ExcelProperty(value = "规格组合", index = 1)
    @ApiModelProperty(value = "规格组合")
    private String specCombination;
    
    @ExcelProperty(value = "规格图片", index = 2)
    @ApiModelProperty(value = "规格图片URL")
    private String specImage;
    
    @ExcelProperty(value = "售价", index = 3)
    @ApiModelProperty(value = "售价")
    private String price;
    
    @ExcelProperty(value = "成本价", index = 4)
    @ApiModelProperty(value = "成本价")
    private String cost;
    
    @ExcelProperty(value = "划线价", index = 5)
    @ApiModelProperty(value = "划线价")
    private String otPrice;
    
    @ExcelProperty(value = "会员价", index = 6)
    @ApiModelProperty(value = "会员价")
    private String vipPrice;
    
    @ExcelProperty(value = "库存", index = 7)
    @ApiModelProperty(value = "库存")
    private String stock;
    
    @ExcelProperty(value = "商品编码", index = 8)
    @ApiModelProperty(value = "商品编码")
    private String barCode;
    
    @ExcelProperty(value = "商品条码", index = 9)
    @ApiModelProperty(value = "商品条码")
    private String itemNumber;
    
    @ExcelProperty(value = "重量(KG)", index = 10)
    @ApiModelProperty(value = "重量")
    private String weight;
    
    @ExcelProperty(value = "体积(m³)", index = 11)
    @ApiModelProperty(value = "体积")
    private String volume;
    
    @ExcelProperty(value = "一级返佣(%)", index = 12)
    @ApiModelProperty(value = "一级返佣")
    private String brokerage;
    
    @ExcelProperty(value = "二级返佣(%)", index = 13)
    @ApiModelProperty(value = "二级返佣")
    private String brokerageTwo;
    
    @ExcelProperty(value = "默认选中", index = 14)
    @ApiModelProperty(value = "是否默认选中")
    private String isDefault;
    
    @ExcelProperty(value = "是否显示", index = 15)
    @ApiModelProperty(value = "是否显示")
    private String isShow;
} 