package com.zbkj.common.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单Excel VO对象
 * @Author 莫名
 * @Date 2025/6/28 12:24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderExcelVo", description = "订单Excel VO对象")
public class OrderExcelVo implements Serializable {

    private static final long serialVersionUID = -8330957183745338822L;

    @ExcelProperty(value = "订单类型", index = 0)
    @ApiModelProperty(value = "订单类型:0-普通订单，1-视频号订单,2-秒杀订单")
    private String type;

    @ExcelProperty(value = "订单号", index = 1)
    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ExcelProperty(value = "商户ID", index = 2)
    @ApiModelProperty(value = "商户名称")
    private String merName;

    @ExcelProperty(value = "用户ID", index = 3)
    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ExcelProperty(value = "实际支付金额", index = 4)
    @ApiModelProperty(value = "实际支付金额")
    private String payPrice;

    @ExcelProperty(value = "支付状态", index = 5)
    @ApiModelProperty(value = "支付状态")
    private String paidStr;

    @ExcelProperty(value = "支付类型", index = 6)
    @ApiModelProperty(value = "支付方式:weixin,alipay,yue")
    private String payType;

    @ExcelProperty(value = "支付渠道", index = 7)
    @ApiModelProperty(value = "支付渠道：public-公众号,mini-小程序，h5-网页支付,yue-余额，wechatIos-微信Ios，wechatAndroid-微信Android,alipay-支付宝，alipayApp-支付宝App")
    private String payChannel;

    @ExcelProperty(value = "订单状态", index = 8)
    @ApiModelProperty(value = "订单状态（0：待支付，1：待发货,2：部分发货， 3：待核销，4：待收货,5：已收货,6：已完成，9：已取消）")
    private String status;

    @ExcelProperty(value = "退款状态", index = 9)
    @ApiModelProperty(value = "退款状态：0 未退款 1 申请中 2 部分退款 3 已退款")
    private String refundStatus;

    @ExcelProperty(value = "创建时间", index = 10)
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ExcelProperty(value = "商品信息", index = 11)
    @ApiModelProperty(value = "商品信息")
    private String productInfo;

    @ExcelProperty(value = "收货人姓名", index = 12)
    @ApiModelProperty(value = "收货人姓名")
    private String realName;

    @ExcelProperty(value = "收货人电话", index = 13)
    @ApiModelProperty(value = "收货人电话")
    private String userPhone;

    @ExcelProperty(value = "收货人地址", index = 14)
    @ApiModelProperty(value = "收货详细地址")
    private String userAddress;

    @ExcelProperty(value = "用户备注", index = 15)
    @ApiModelProperty(value = "用户备注")
    private String userRemark;

    @ExcelProperty(value = "商户备注", index = 16)
    @ApiModelProperty(value = "商户备注")
    private String merchantRemark;
}
