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
import java.util.Date;

/**
 * <p>
 * 分账接收方表
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_merchant_profit_sharing_receiver")
@ApiModel(value = "MerchantProfitSharingReceiver对象", description = "分账接收方表")
public class MerchantProfitSharingReceiver implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "接收方编号")
    private String receiverNo;

    @ApiModelProperty(value = "接收方类型：MERCHANT_ID-商户号，PERSONAL_OPENID-个人openid")
    private String type;

    @ApiModelProperty(value = "接收方账户")
    private String account;

    @ApiModelProperty(value = "接收方姓名")
    private String name;

    @ApiModelProperty(value = "与分账方的关系类型")
    private String relationType;

    @ApiModelProperty(value = "自定义的分账关系")
    private String customRelation;

    @ApiModelProperty(value = "是否默认接收方：0-否，1-是")
    private Boolean isDefault;

    @ApiModelProperty(value = "状态：ACTIVE-有效，INACTIVE-无效，DELETED-已删除")
    private String status;

    @ApiModelProperty(value = "微信侧状态：ACTIVE-生效中，INACTIVE-失效")
    private String wechatStatus;

    @ApiModelProperty(value = "绑定时间")
    private Date bindTime;

    @ApiModelProperty(value = "解绑时间")
    private Date unbindTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 