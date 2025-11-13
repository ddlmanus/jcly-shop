package com.zbkj.common.request;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 平台门店管理请求类
 */
@Data
@ApiModel(value = "StoreRequest", description = "平台门店管理请求")
public class StoreRequest {

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "门店编码")
    private String storeCode;

    @ApiModelProperty(value = "门店名称", required = true)
    @NotBlank(message = "门店名称不能为空")
    private String storeName;

    @ApiModelProperty(value = "门店类型：OFFLINE-线下门店，ONLINE-线上店")
    private String storeType = "OFFLINE";
    
    @ApiModelProperty(value = "是否支持线上购物：0-不支持，1-支持")
    private Integer supportOnlineShopping = 0;

    @ApiModelProperty(value = "联系人姓名", required = true)
    @NotBlank(message = "联系人姓名不能为空")
    private String contactPerson;

    @ApiModelProperty(value = "联系电话", required = true)
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @ApiModelProperty(value = "联系邮箱")
    private String contactEmail;

    // 地址信息
    @ApiModelProperty(value = "省份", required = true)
    @NotBlank(message = "省份不能为空")
    private String province;
    @ApiModelProperty(value = "省份ID")
    private Long provinceId;

    @ApiModelProperty(value = "城市", required = true)
    @NotBlank(message = "城市不能为空")
    private String city;
    @ApiModelProperty(value = "城市ID")
    private Long cityId;

    @ApiModelProperty(value = "区县", required = true)
    @NotBlank(message = "区县不能为空")
    private String district;
    @ApiModelProperty(value = "区县ID")
    private Long districtId;
    @ApiModelProperty(value = "街道")
    private String street;
    @ApiModelProperty(value = "街道ID")
    private Long streetId;
    @ApiModelProperty(value = "详细地址", required = true)
    @NotBlank(message = "详细地址不能为空")
    private String addressDetail;

    @ApiModelProperty(value = "纬度")
    private BigDecimal latitude;

    @ApiModelProperty(value = "经度")
    private BigDecimal longitude;

    // 营业信息
    @ApiModelProperty(value = "营业开始时间")
    private LocalTime businessHoursStart;

    @ApiModelProperty(value = "营业结束时间")
    private LocalTime businessHoursEnd;

    @ApiModelProperty(value = "营业日期：1-7表示周一到周日，逗号分隔")
    private String businessDays;

    @ApiModelProperty(value = "门店状态：0-停用，1-启用")
    private Integer status = 1;

    @ApiModelProperty(value = "排序值")
    private Integer sort = 0;

    @ApiModelProperty(value = "门店描述")
    private String description;

    @ApiModelProperty(value = "门店图片，JSON格式存储")
    private String images;

    @ApiModelProperty(value = "门店标签，逗号分隔")
    private String tags;
}