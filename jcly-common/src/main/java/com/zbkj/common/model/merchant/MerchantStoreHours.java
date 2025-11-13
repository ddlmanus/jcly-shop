package com.zbkj.common.model.merchant;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 门店营业时间配置实体类
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("eb_merchant_store_hours")
@ApiModel(value = "MerchantStoreHours", description = "门店营业时间配置")
public class MerchantStoreHours implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "门店ID", required = true)
    @NotNull(message = "门店ID不能为空")
    @TableField("store_id")
    private Integer storeId;

    @ApiModelProperty(value = "星期几：1-7表示周一到周日", required = true)
    @NotNull(message = "星期几不能为空")
    @TableField("day_of_week")
    private Integer dayOfWeek;

    @ApiModelProperty(value = "是否营业：0-不营业，1-营业")
    @TableField("is_open")
    private Integer isOpen;

    @ApiModelProperty(value = "开始营业时间")
    @TableField("open_time")
    private LocalTime openTime;

    @ApiModelProperty(value = "结束营业时间")
    @TableField("close_time")
    private LocalTime closeTime;

    @ApiModelProperty(value = "休息开始时间")
    @TableField("break_start_time")
    private LocalTime breakStartTime;

    @ApiModelProperty(value = "休息结束时间")
    @TableField("break_end_time")
    private LocalTime breakEndTime;

    @ApiModelProperty(value = "备注说明")
    @TableField("remarks")
    private String remarks;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 非数据库字段
    @ApiModelProperty(value = "星期几中文名称")
    @TableField(exist = false)
    private String dayOfWeekName;

    /**
     * 获取星期几的中文名称
     */
    public String getDayOfWeekName() {
        if (dayOfWeek == null) {
            return "";
        }
        switch (dayOfWeek) {
            case 1: return "周一";
            case 2: return "周二";
            case 3: return "周三";
            case 4: return "周四";
            case 5: return "周五";
            case 6: return "周六";
            case 7: return "周日";
            default: return "";
        }
    }

    /**
     * 检查当前时间是否在营业时间内
     */
    public boolean isInBusinessHours() {
        if (isOpen == null || isOpen == 0 || openTime == null || closeTime == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        
        // 检查是否在休息时间
        if (breakStartTime != null && breakEndTime != null) {
            if (now.isAfter(breakStartTime) && now.isBefore(breakEndTime)) {
                return false; // 在休息时间内
            }
        }
        
        return now.isAfter(openTime) && now.isBefore(closeTime);
    }
} 