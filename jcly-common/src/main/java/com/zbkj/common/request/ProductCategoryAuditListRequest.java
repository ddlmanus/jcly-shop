package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 商品分类审核列表请求对象
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
@ApiModel(value="ProductCategoryAuditListRequest对象", description="商品分类审核列表请求对象")
public class ProductCategoryAuditListRequest extends PageParamRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分类名称")
    private String name;
    
    @ApiModelProperty(value = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;
    
    @ApiModelProperty(value = "申请开始时间")
    private String startTime;
    
    @ApiModelProperty(value = "申请结束时间")
    private String endTime;
    
    @ApiModelProperty(value = "申请商户名称")
    private String merchantName;
} 