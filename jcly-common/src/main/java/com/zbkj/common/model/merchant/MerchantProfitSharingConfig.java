package com.zbkj.common.model.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 商户分账配置表
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_merchant_profit_sharing_config")
@ApiModel(value = "MerchantProfitSharingConfig对象", description = "商户分账配置表")
public class MerchantProfitSharingConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "是否启用分账：0-禁用，1-启用")
    private Boolean isEnabled;

    @ApiModelProperty(value = "子商户号（微信分账接收方）")
    private String subMchId;

    @ApiModelProperty(value = "分账比例（0-100%）")
    private BigDecimal sharingRatio;

    @ApiModelProperty(value = "分账类型：MERCHANT_ID-商户号，PERSONAL_OPENID-个人openid，PERSONAL_SUB_OPENID-个人sub_openid")
    private String sharingType;

    @ApiModelProperty(value = "分账接收方账户")
    private String account;

    @ApiModelProperty(value = "分账接收方姓名")
    private String name;

    @ApiModelProperty(value = "与分账方的关系类型：STORE-门店，STAFF-员工，STORE_OWNER-店主，PARTNER-合作伙伴，HEADQUARTER-总部，BRAND-品牌方，DISTRIBUTOR-分销商，USER-用户，SUPPLIER-供应商，CUSTOM-自定义")
    private String relationType;

    @ApiModelProperty(value = "自定义的分账关系")
    private String customRelation;

    @ApiModelProperty(value = "是否自动分账：0-手动，1-自动")
    private Boolean isAutoSharing;

    @ApiModelProperty(value = "分账延迟天数（订单完成后多少天执行分账）")
    private Integer sharingDelayDays;

    @ApiModelProperty(value = "配置状态：0-禁用，1-启用")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 