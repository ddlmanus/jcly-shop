package com.zbkj.common.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 运费模板详情响应对象
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
@ApiModel(value="ShippingTemplatesInfoResponse对象", description="运费模板详情响应对象")
public class ShippingTemplatesInfoResponse implements Serializable {

    private static final long serialVersionUID = -849676402721624567L;

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "模板名称")
    private String name;

    @ApiModelProperty(value = "计费方式")
    private Integer type;

    @ApiModelProperty(value = "包邮类型：0-全国包邮，1-部分包邮，2-自定义")
    private Integer appoint;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "区域运费列表")
    List<ShippingTemplatesRegionResponse> regionList;

    @ApiModelProperty(value = "包邮运费列表")
    List<ShippingTemplatesFreeResponse> freeList;
}
