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
 * 待办事项操作日志表
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_todo_operation_log")
@ApiModel(value = "TodoOperationLog对象", description = "待办事项操作日志表")
public class TodoOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "待办事项ID")
    private Integer todoItemId;

    @ApiModelProperty(value = "操作人ID")
    private Integer operatorId;

    @ApiModelProperty(value = "操作类型：create-创建，complete-完成，update-更新")
    private String operationType;

    @ApiModelProperty(value = "操作人类型：merchant-商户，admin-管理员")
    private String operatorType;

    @ApiModelProperty(value = "操作备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}