package com.zbkj.common.model.merchant;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 商户门店信息实体类
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("eb_merchant_store")
@ApiModel(value = "MerchantStore", description = "商户门店信息")
public class MerchantStore implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID", required = true)
    @NotNull(message = "商户ID不能为空")
    @TableField("mer_id")
    private Integer merId;

    @ApiModelProperty(value = "门店编码", required = true)
    @NotBlank(message = "门店编码不能为空")
    @TableField("store_code")
    private String storeCode;

    @ApiModelProperty(value = "门店名称", required = true)
    @NotBlank(message = "门店名称不能为空")
    @TableField("store_name")
    private String storeName;

    @ApiModelProperty(value = "门店类型：PHYSICAL-实体店，ONLINE-线上店，WAREHOUSE-仓库")
    @TableField("store_type")
    private String storeType;

    @ApiModelProperty(value = "联系人姓名", required = true)
    @NotBlank(message = "联系人姓名不能为空")
    @TableField("contact_person")
    private String contactPerson;

    @ApiModelProperty(value = "联系电话", required = true)
    @NotBlank(message = "联系电话不能为空")
    @TableField("contact_phone")
    private String contactPhone;

    @ApiModelProperty(value = "联系邮箱")
    @TableField("contact_email")
    private String contactEmail;

    // 地址信息
    @ApiModelProperty(value = "省份", required = true)
    @NotBlank(message = "省份不能为空")
    @TableField("province")
    private String province;

    @ApiModelProperty(value = "城市", required = true)
    @NotBlank(message = "城市不能为空")
    @TableField("city")
    private String city;

    @ApiModelProperty(value = "区县", required = true)
    @NotBlank(message = "区县不能为空")
    @TableField("district")
    private String district;

    @ApiModelProperty(value = "详细地址", required = true)
    @NotBlank(message = "详细地址不能为空")
    @TableField("address_detail")
    private String addressDetail;

    @ApiModelProperty(value = "完整地址")
    @TableField("full_address")
    private String fullAddress;

    @ApiModelProperty(value = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @ApiModelProperty(value = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @ApiModelProperty(value = "地址编码")
    @TableField("address_code")
    private String addressCode;

    // 营业信息
    @ApiModelProperty(value = "营业开始时间")
    @TableField("business_hours_start")
    private LocalTime businessHoursStart;

    @ApiModelProperty(value = "营业结束时间")
    @TableField("business_hours_end")
    private LocalTime businessHoursEnd;

    @ApiModelProperty(value = "营业日期：1-7表示周一到周日")
    @TableField("business_days")
    private String businessDays;

    @ApiModelProperty(value = "门店面积（平方米）")
    @TableField("store_area")
    private BigDecimal storeArea;

    @ApiModelProperty(value = "员工数量")
    @TableField("employee_count")
    private Integer employeeCount;

    // 状态信息
    @ApiModelProperty(value = "门店状态：0-停用，1-启用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "是否主门店：0-否，1-是")
    @TableField("is_main")
    private Integer isMain;

    @ApiModelProperty(value = "排序值")
    @TableField("sort")
    private Integer sort;

    // 其他信息
    @ApiModelProperty(value = "门店描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "门店图片，JSON格式存储")
    @TableField("images")
    private String images;

    @ApiModelProperty(value = "门店设施，JSON格式存储")
    @TableField("facilities")
    private String facilities;

    @ApiModelProperty(value = "门店标签，逗号分隔")
    @TableField("tags")
    private String tags;

    // 统计信息
    @ApiModelProperty(value = "总订单数")
    @TableField("total_orders")
    private Integer totalOrders;

    @ApiModelProperty(value = "总销售额")
    @TableField("total_sales")
    private BigDecimal totalSales;

    @ApiModelProperty(value = "最后一单时间")
    @TableField("last_order_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOrderTime;

    // 时间信息
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人ID")
    @TableField("create_user_id")
    private Integer createUserId;

    @ApiModelProperty(value = "更新人ID")
    @TableField("update_user_id")
    private Integer updateUserId;

    // 非数据库字段 - 用于关联查询
    @ApiModelProperty(value = "门店营业时间配置")
    @TableField(exist = false)
    private List<MerchantStoreHours> storeHours;

    @ApiModelProperty(value = "门店员工列表")
    @TableField(exist = false)
    private List<MerchantStoreStaff> storeStaff;

    @ApiModelProperty(value = "配送范围列表")
    @TableField(exist = false)
    private List<MerchantStoreDeliveryArea> deliveryAreas;

    @ApiModelProperty(value = "商户名称")
    @TableField(exist = false)
    private String merchantName;

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
            return fullAddress;
        }
        return (province != null ? province : "") +
               (city != null ? city : "") +
               (district != null ? district : "") +
               (addressDetail != null ? addressDetail : "");
    }

    /**
     * 检查是否在营业时间内
     */
    public boolean isBusinessTime() {
        if (businessHoursStart == null || businessHoursEnd == null) {
            return true; // 如果没有设置营业时间，默认为营业中
        }
        LocalTime now = LocalTime.now();
        return now.isAfter(businessHoursStart) && now.isBefore(businessHoursEnd);
    }

    /**
     * 检查今天是否营业
     */
    public boolean isBusinessDay() {
        if (businessDays == null || businessDays.trim().isEmpty()) {
            return true; // 如果没有设置营业日期，默认为营业
        }
        int todayOfWeek = LocalDateTime.now().getDayOfWeek().getValue(); // 1-7
        return businessDays.contains(String.valueOf(todayOfWeek));
    }
} 