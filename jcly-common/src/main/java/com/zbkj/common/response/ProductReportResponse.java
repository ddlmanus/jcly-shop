package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品报表响应类
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
@ApiModel(value = "ProductReportResponse", description = "商品报表响应")
public class ProductReportResponse {

    @ApiModelProperty(value = "商品总数")
    private Integer connectedProducts;

    @ApiModelProperty(value = "商户数")
    private Integer connectedStores;

    @ApiModelProperty(value = "商品分类数")
    private Integer productCategories;

    @ApiModelProperty(value = "商品品牌数")
    private Integer productBrands;

    @ApiModelProperty(value = "商品数量统计")
    private ProductCountStatistics productCountStats;

    @ApiModelProperty(value = "商品状态统计")
    private ProductStatusStatistics productStatusStats;

    @ApiModelProperty(value = "商品规格统计")
    private List<ProductSpecStatistics> productSpecStats;

    @ApiModelProperty(value = "商品销量汇总")
    private ProductSalesStatistics productSalesStats;

    @ApiModelProperty(value = "商品库存统计")
    private ProductStockStatistics productStockStats;

    @Data
    @ApiModel(value = "ProductCountStatistics", description = "商品数量统计")
    public static class ProductCountStatistics {
        @ApiModelProperty(value = "已售商品数")
        private Integer soldProducts;

        @ApiModelProperty(value = "新增商品数")
        private Integer newProducts;

        @ApiModelProperty(value = "退货商品数")
        private Integer returnedProducts;

        @ApiModelProperty(value = "日商品数量趋势")
        private List<DailyProductData> dailyTrend;
    }

    @Data
    @ApiModel(value = "ProductStatusStatistics", description = "商品状态统计")
    public static class ProductStatusStatistics {
        @ApiModelProperty(value = "平台自营店铺商品数")
        private Integer platformOwnProducts;

        @ApiModelProperty(value = "其他店铺商品数")
        private Integer otherStoreProducts;

        @ApiModelProperty(value = "供应商商城商品数")
        private Integer supplierProducts;

        @ApiModelProperty(value = "在售商品数")
        private Integer onSaleProducts;
    }

    @Data
    @ApiModel(value = "ProductSpecStatistics", description = "商品规格统计")
    public static class ProductSpecStatistics {
        @ApiModelProperty(value = "规格类型")
        private String specType;

        @ApiModelProperty(value = "商品数量")
        private Integer productCount;
    }

    @Data
    @ApiModel(value = "ProductSalesStatistics", description = "商品销量汇总")
    public static class ProductSalesStatistics {
        @ApiModelProperty(value = "商品自营")
        private Integer selfOperatedSales;

        @ApiModelProperty(value = "其他店铺")
        private Integer otherStoreSales;

        @ApiModelProperty(value = "销量趋势")
        private List<DailySalesData> salesTrend;
    }

    @Data
    @ApiModel(value = "ProductStockStatistics", description = "商品库存统计")
    public static class ProductStockStatistics {
        @ApiModelProperty(value = "入库数量")
        private Integer inboundStock;

        @ApiModelProperty(value = "出库数量")
        private Integer outboundStock;

        @ApiModelProperty(value = "预警次数")
        private Integer alertCount;

        @ApiModelProperty(value = "库存详情列表")
        private List<StockDetailData> stockDetails;
    }

    @Data
    @ApiModel(value = "DailyProductData", description = "日商品数据")
    public static class DailyProductData {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "商品数量")
        private Integer productCount;
    }

    @Data
    @ApiModel(value = "DailySalesData", description = "日销量数据")
    public static class DailySalesData {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "商品自营销量")
        private Integer selfOperatedSales;

        @ApiModelProperty(value = "其他店铺销量")
        private Integer otherStoreSales;
    }

    @Data
    @ApiModel(value = "StockDetailData", description = "库存详情数据")
    public static class StockDetailData {
        @ApiModelProperty(value = "序号")
        private Integer sequence;

        @ApiModelProperty(value = "编号")
        private String code;

        @ApiModelProperty(value = "商品信息")
        private String productInfo;

        @ApiModelProperty(value = "商品规格")
        private String productSpec;

        @ApiModelProperty(value = "总库存")
        private Integer totalStock;

        @ApiModelProperty(value = "剩余库存")
        private Integer remainingStock;

        @ApiModelProperty(value = "预警时间")
        private String alertTime;
    }
} 