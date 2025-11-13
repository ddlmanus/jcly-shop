package com.zbkj.common.model.product;

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
 * 商品分类表
 * </p>
 *
 * @author dudl
 * @since 2022-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_product_category")
@ApiModel(value="ProductCategory对象", description="商品分类表")
public class ProductCategory implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "父级ID")
    private Integer pid;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "icon")
    private String icon;

    @ApiModelProperty(value = "级别:1，2，3")
    private Integer level;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "显示状态")
    private Boolean isShow;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "申请商户ID，如果是商户申请的平台分类")
    private Integer applyMerId;

    @ApiModelProperty(value = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty(value = "申请时间")
    private Date applyTime;

    @ApiModelProperty(value = "拒绝原因")
    private String rejectReason;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核人员ID")
    private Integer auditorId;

    @ApiModelProperty(value = "申请理由")
    private String applyReason;

    @ApiModelProperty(value = "聚水潭分类ID")
    private String jstCategoryId;

}
