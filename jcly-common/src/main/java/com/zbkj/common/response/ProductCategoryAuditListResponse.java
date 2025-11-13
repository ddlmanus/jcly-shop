package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品分类审核列表响应对象
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
@ApiModel(value="ProductCategoryAuditListResponse对象", description="商品分类审核列表响应对象")
public class ProductCategoryAuditListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分类ID")
    private Integer id;
    
    @ApiModelProperty(value = "分类名称")
    private String name;
    
    @ApiModelProperty(value = "分类图标")
    private String icon;
    
    @ApiModelProperty(value = "分类级别")
    private Integer level;
    
    @ApiModelProperty(value = "父级分类ID")
    private Integer pid;
    
    @ApiModelProperty(value = "父级分类名称")
    private String parentName;
    
    @ApiModelProperty(value = "排序")
    private Integer sort;
    
    @ApiModelProperty(value = "申请商户ID")
    private Integer applyMerId;
    
    @ApiModelProperty(value = "申请商户名称")
    private String applyMerchantName;
    
    @ApiModelProperty(value = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;
    
    @ApiModelProperty(value = "申请时间")
    private Date applyTime;
    
    @ApiModelProperty(value = "审核时间")
    private Date auditTime;
    
    @ApiModelProperty(value = "拒绝原因")
    private String rejectReason;
    
    @ApiModelProperty(value = "审核人ID")
    private Integer auditorId;
    
    @ApiModelProperty(value = "审核人名称")
    private String auditorName;
    
    @ApiModelProperty(value = "申请理由")
    private String applyReason;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 