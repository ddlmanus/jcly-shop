package com.zbkj.common.model.invoice;

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
 * 发票说明配置表
 * </p>
 *
 * @author dudl
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_invoice_description")
@ApiModel(value = "InvoiceDescription对象", description = "发票说明配置表")
public class InvoiceDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "说明内容（富文本）")
    private String content;

    @ApiModelProperty(value = "是否显示：0-否，1-是")
    private Boolean isShow;

    @ApiModelProperty(value = "排序值")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 