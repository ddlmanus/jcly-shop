package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建订单请求对象
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="CreateOrderRequest对象", description="创建订单请求对象")
public class CreateOrderRequest implements Serializable {

    private static final long serialVersionUID = -6133994384185333872L;

    @ApiModelProperty(value = "预下单订单号", required = true)
    @NotBlank(message = "预下单订单号不能为空")
    private String preOrderNo;

    @ApiModelProperty(value = "收货地址ID")
    private Integer addressId;

    @ApiModelProperty(value = "平台优惠券ID")
    private Integer platUserCouponId = 0;

    @ApiModelProperty(value = "是否使用积分")
    private Boolean isUseIntegral = false;

    @ApiModelProperty(value = "支付渠道:public-公众号,mini-小程序，h5-网页支付,wechatIos-微信Ios，wechatAndroid-微信Android")
    private String payChannel;

    @ApiModelProperty(value = "自定义表单数据")
    private String orderExtend;

    @ApiModelProperty(value = "商户订单信息", required = true)
    @NotEmpty(message = "商户订单信息不能为空")
    private List<OrderMerchantRequest> orderMerchantRequestList;

    @ApiModelProperty(value = "是否需要开票")
    private Boolean needInvoice = false;

    @ApiModelProperty(value = "发票信息（如果需要开票）")
    private InvoiceInfoRequest invoiceInfo;
}
