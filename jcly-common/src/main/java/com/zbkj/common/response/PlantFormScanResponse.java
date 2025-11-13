package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="PlatformHomeRateResponse对象", description="主页统计数据对象")
public class PlantFormScanResponse {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "今日销售额")
    private BigDecimal sales;
    @ApiModelProperty(value = "销售总额")
    private BigDecimal yearSales;

    @ApiModelProperty(value = "昨日销售额")
    private BigDecimal yesterdaySales;

    @ApiModelProperty(value = "今日访问量")
    private Integer pageviews;

    @ApiModelProperty(value = "昨日访问量")
    private Integer yesterdayPageviews;

    @ApiModelProperty(value = "今日订单量")
    private Integer orderNum;

    @ApiModelProperty(value = "今日完成订单量")
    private Integer finishOrderNum;

    @ApiModelProperty(value = "昨日订单量")
    private Integer yesterdayOrderNum;

    @ApiModelProperty(value = "全部用户数")
    private Integer userNum;

    @ApiModelProperty(value = "认证用户数")
    private Integer userPassNum;

    @ApiModelProperty(value = "全部商户数")
    private Integer merchantNum;

    @ApiModelProperty(value = "今日新增用户数")
    private Integer todayNewUserNum;

    @ApiModelProperty(value = "昨日新增用户数")
    private Integer yesterdayNewUserNum;

    @ApiModelProperty(value = "今日新增商户数")
    private Integer todayNewMerchantNum;

    @ApiModelProperty(value = "昨日新增商户数")
    private Integer yesterdayNewMerchantNum;

    @ApiModelProperty(value = "交易订单")
    private List<OrderTop10Response> orderList;

    @ApiModelProperty(value = "近七日新增用户商户数")
    private List<PlatformHomeUserResponse>  userResponse;

    @ApiModelProperty(value = "累计新增用户数")
    private Integer newAllUser;

    @ApiModelProperty(value = "近七日销售统计")
    private List<PlatformHomeSaleResponse>  saleList;
    private List<PlatformHomeAreaResponse> areas;

}
