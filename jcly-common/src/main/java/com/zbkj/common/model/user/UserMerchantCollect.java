package com.zbkj.common.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户店铺收藏表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_merchant_collect")
@ApiModel(value="UserMerchantCollect对象", description="用户店铺收藏表")
public class UserMerchantCollect implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "用户昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "用户头像")
    @TableField(exist = false)
    private String avatar;

    @ApiModelProperty(value = "用户手机号")
    @TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "收藏时间（格式化后的创建时间）")
    @TableField(exist = false)
    private String create_time;
}