package com.zbkj.common.model.merchant;

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
 * 商户待办事项表
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_merchant_todo_item")
@ApiModel(value = "MerchantTodoItem对象", description = "商户待办事项表")
public class MerchantTodoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "事项标题")
    private String title;

    @ApiModelProperty(value = "事项内容")
    private String content;

    @ApiModelProperty(value = "事项类型：order-订单，message-消息，product-商品，refund-退款")
    private String type;

    @ApiModelProperty(value = "优先级：1-高，2-中，3-低")
    private Integer priority;

    @ApiModelProperty(value = "目标跳转URL")
    private String targetUrl;

    @ApiModelProperty(value = "关联业务ID")
    private String relatedId;

    @ApiModelProperty(value = "事项状态：0-待处理，1-已处理")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "完成时间")
    private Date completeTime;

    @ApiModelProperty(value = "是否删除：0-否，1-是")
    private Boolean isDelete;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}