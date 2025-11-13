package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户门店搜索请求类
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "MerchantStoreSearchRequest", description = "商户门店搜索请求")
public class MerchantStoreSearchRequest extends PageParamRequest {

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "门店编码")
    private String storeCode;

    @ApiModelProperty(value = "门店名称")
    private String storeName;

    @ApiModelProperty(value = "门店类型：PHYSICAL-实体店，ONLINE-线上店，WAREHOUSE-仓库")
    private String storeType;

    @ApiModelProperty(value = "联系人姓名")
    private String contactPerson;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "省份")
    private String province;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "区县")
    private String district;

    @ApiModelProperty(value = "门店状态：0-停用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "是否主门店：0-否，1-是")
    private Integer isMain;

    @ApiModelProperty(value = "关键字搜索（门店名称/联系人/电话）")
    private String keyword;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "排序字段")
    private String orderBy;

    @ApiModelProperty(value = "排序方式：asc-升序，desc-降序")
    private String orderDirection = "desc";
} 