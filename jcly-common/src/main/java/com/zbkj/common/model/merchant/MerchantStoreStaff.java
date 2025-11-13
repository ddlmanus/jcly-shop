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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门店员工关联实体类
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("eb_merchant_store_staff")
@ApiModel(value = "MerchantStoreStaff", description = "门店员工关联")
public class MerchantStoreStaff implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "门店ID", required = true)
    @NotNull(message = "门店ID不能为空")
    @TableField("store_id")
    private Integer storeId;

    @ApiModelProperty(value = "员工姓名", required = true)
    @NotBlank(message = "员工姓名不能为空")
    @TableField("staff_name")
    private String staffName;

    @ApiModelProperty(value = "员工电话", required = true)
    @NotBlank(message = "员工电话不能为空")
    @TableField("staff_phone")
    private String staffPhone;

    @ApiModelProperty(value = "职位")
    @TableField("staff_position")
    private String staffPosition;

    @ApiModelProperty(value = "是否门店经理：0-否，1-是")
    @TableField("is_manager")
    private Integer isManager;

    @ApiModelProperty(value = "状态：0-离职，1-在职")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "入职时间")
    @TableField("join_time")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 非数据库字段
    @ApiModelProperty(value = "门店名称")
    @TableField(exist = false)
    private String storeName;

    @ApiModelProperty(value = "状态名称")
    @TableField(exist = false)
    private String statusName;

    @ApiModelProperty(value = "工作年限")
    @TableField(exist = false)
    private Integer workYears;

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) {
            return "";
        }
        return status == 1 ? "在职" : "离职";
    }

    /**
     * 获取职位类型名称
     */
    public String getPositionTypeName() {
        if (isManager == null) {
            return "普通员工";
        }
        return isManager == 1 ? "门店经理" : "普通员工";
    }

    /**
     * 计算工作年限
     */
    public Integer getWorkYears() {
        if (joinTime == null || status == 0) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        return now.getYear() - joinTime.getYear();
    }
} 