package com.zbkj.common.model.order;

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
 * 核销记录表
 * </p>
 *
 * @author dudl
 * @since 2025-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_verification_record")
@ApiModel(value="VerificationRecord对象", description="核销记录表")
public class VerificationRecord implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "核销码")
    private String verifyCode;

    @ApiModelProperty(value = "核销人ID")
    private Integer verifyStaffId;

    @ApiModelProperty(value = "核销人类型：1-商户端管理员，2-移动端商户管理员")
    private Integer verifyStaffType;

    @ApiModelProperty(value = "核销时间")
    private Date verifyTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
