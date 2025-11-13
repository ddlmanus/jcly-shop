package com.zbkj.common.request;

import com.zbkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 订单列表请求对象
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
@ApiModel(value = "OrderSearchRequest对象", description = "订单列表请求对象")
public class OrderSearchRequest extends UserCommonSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "创建时间区间")
    private String dateLimit;

    @ApiModelProperty(value = "订单状态（all 全部； 未支付 unPaid； 未发货 notShipped；待收货 spike；已收货 receiving;已完成 complete；已退款:refunded；已删除:deleted；待核销：awaitVerification；已取消:cancel")
    @StringContains(limitValues = {"", "all", "unPaid", "notShipped", "spike", "receiving", "complete", "refunded", "deleted", "awaitVerification", "cancel"}, message = "未知的订单状态")
    private String status;

    @ApiModelProperty(value = "商户id, 平台端查询值有效")
    private Integer merId;

    @ApiModelProperty(value = "订单类型:0-基础订单,1-秒杀订单,2-拼团订单")
    private Integer type;

    @ApiModelProperty(value = "订单二级类型:0-普通订单，1-积分订单，2-虚拟订单，4-视频号订单，5-云盘订单，6-卡密订单")
    private Integer secondType;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品编号")
    private String productCode;

    @ApiModelProperty(value = "买家昵称")
    private String buyerNickname;

    @ApiModelProperty(value = "买家手机号")
    private String buyerPhone;

    @ApiModelProperty(value = "支付方式:weixin-微信支付,alipay-支付宝,balance-余额支付")
    @StringContains(limitValues = {"weixin", "alipay", "balance", ""}, message = "未知的支付方式")
    private String payType;

    @ApiModelProperty(value = "收货地址")
    private String receiverAddress;

    @ApiModelProperty(value = "快递单号")
    private String expressNo;

    @ApiModelProperty(value = "发货方式:express-快递配送,pickup-到店自提,local-同城配送")
    @StringContains(limitValues = {"express", "pickup", "local", ""}, message = "未知的发货方式")
    private String deliveryMethod;

    @ApiModelProperty(value = "用户类型:normal-普通用户,vip-VIP用户,svip-SVIP用户")
    @StringContains(limitValues = {"normal", "vip", "svip", ""}, message = "未知的用户类型")
    private String userType;

    @ApiModelProperty(value = "活动类型:0-普通,1-秒杀,2-拼团")
    private Integer activityType;
}
