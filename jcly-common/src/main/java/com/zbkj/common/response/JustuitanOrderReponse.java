package com.zbkj.common.response;

import com.zbkj.common.model.order.Order;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class JustuitanOrderReponse extends Order {
    @ApiModelProperty(value = "聚水潭ERP订单状态（status字段）")
    private String justuitanOrderStatus;
    @ApiModelProperty(value = "聚水潭平台订单状态（shop_status字段）")
    private String jstShopStatus;
    @ApiModelProperty(value = "聚水潭快递单号")
    private String jstLogisticsNo;
    @ApiModelProperty(value = "聚水潭快递公司")
    private String jstLogisticsCompany;
    @ApiModelProperty(value = "发货时间")
    private String jstShipTime;
}
