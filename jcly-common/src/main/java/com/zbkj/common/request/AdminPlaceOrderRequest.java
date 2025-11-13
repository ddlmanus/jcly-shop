package com.zbkj.common.request;

import com.zbkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 管理员代客下单请求对象
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
@ApiModel(value = "AdminPlaceOrderRequest对象", description = "管理员代客下单请求对象")
public class AdminPlaceOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @ApiModelProperty(value = "预下单类型（buyNow:立即购买，shoppingCart:购物车下单）", required = true)
    @NotBlank(message = "预下单类型不能为空")
    @StringContains(limitValues = {"buyNow", "shoppingCart"}, message = "代客下单仅支持立即购买和购物车下单")
    private String preOrderType;

    @ApiModelProperty(value = "商品列表", required = true)
    @NotEmpty(message = "商品列表不能为空")
    private List<PreOrderDetailRequest> orderDetails;

    @ApiModelProperty(value = "收货地址ID", required = true)
    @NotNull(message = "收货地址ID不能为空")
    private Integer addressId;

    @ApiModelProperty(value = "平台优惠券ID")
    private Integer platUserCouponId = 0;

    @ApiModelProperty(value = "是否使用积分")
    private Boolean isUseIntegral = false;

    @ApiModelProperty(value = "商户订单信息", required = true)
    @NotEmpty(message = "商户订单信息不能为空")
    private List<OrderMerchantRequest> orderMerchantRequestList;

    @ApiModelProperty(value = "支付方式：yue-余额支付（默认），weixin-微信，alipay-支付宝")
    @StringContains(limitValues = {"yue", "weixin", "alipay"}, message = "不支持的支付方式")
    private String payType = "yue";

    @ApiModelProperty(value = "是否立即支付（仅余额支付支持）")
    private Boolean payNow = false;

    @ApiModelProperty(value = "管理员备注")
    private String adminRemark;

    @ApiModelProperty(value = "自定义表单数据")
    private String orderExtend;

    @ApiModelProperty(value = "是否需要开票")
    private Boolean needInvoice = false;

    @ApiModelProperty(value = "发票信息（如果需要开票）")
    private InvoiceInfoRequest invoiceInfo;
}
