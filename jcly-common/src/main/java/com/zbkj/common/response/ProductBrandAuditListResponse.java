package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品品牌审核列表响应对象
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
@ApiModel(value="ProductBrandAuditListResponse对象", description="商品品牌审核列表响应对象")
public class ProductBrandAuditListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "品牌ID")
    private Integer id;
    
    @ApiModelProperty(value = "品牌名称")
    private String name;
    
    @ApiModelProperty(value = "品牌图标")
    private String icon;
    
    @ApiModelProperty(value = "排序")
    private Integer sort;
    
    @ApiModelProperty(value = "关联分类ID")
    private String categoryIds;
    
    @ApiModelProperty(value = "关联分类名称")
    private String categoryNames;
    
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
} 