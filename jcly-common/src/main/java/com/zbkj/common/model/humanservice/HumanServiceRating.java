package com.zbkj.common.model.humanservice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人工客服评价表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_human_service_rating")
@ApiModel(value = "HumanServiceRating", description = "人工客服评价表")
public class HumanServiceRating implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    @TableField("mer_id")
    private Long merId;

    @ApiModelProperty(value = "会话ID")
    @TableField("session_id")
    private String sessionId;

    @ApiModelProperty(value = "客服ID")
    @TableField("staff_id")
    private Long staffId;

    @ApiModelProperty(value = "客服姓名")
    @TableField("staff_name")
    private String staffName;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "用户昵称")
    @TableField("user_nickname")
    private String userNickname;

    @ApiModelProperty(value = "评分（1-5分）")
    @TableField("score")
    private BigDecimal score;

    @ApiModelProperty(value = "评价类型：1-好评，2-中评，3-差评")
    @TableField("rating_type")
    private Integer ratingType;

    @ApiModelProperty(value = "评价内容")
    @TableField("comment")
    private String comment;

    @ApiModelProperty(value = "评价标签，多个用逗号分隔")
    @TableField("tags")
    private String tags;

    @ApiModelProperty(value = "服务态度评分")
    @TableField("service_attitude_score")
    private BigDecimal serviceAttitudeScore;

    @ApiModelProperty(value = "专业能力评分")
    @TableField("professional_score")
    private BigDecimal professionalScore;

    @ApiModelProperty(value = "响应速度评分")
    @TableField("response_speed_score")
    private BigDecimal responseSpeedScore;

    @ApiModelProperty(value = "问题解决评分")
    @TableField("solution_score")
    private BigDecimal solutionScore;

    @ApiModelProperty(value = "是否匿名：0-否，1-是")
    @TableField("is_anonymous")
    private Boolean isAnonymous;

    @ApiModelProperty(value = "状态：0-待审核，1-已通过，2-已拒绝")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 评价类型常量
    public static final int RATING_TYPE_GOOD = 1; // 好评
    public static final int RATING_TYPE_AVERAGE = 2; // 中评
    public static final int RATING_TYPE_BAD = 3; // 差评

    // 状态常量
    public static final int STATUS_PENDING = 0; // 待审核
    public static final int STATUS_APPROVED = 1; // 已通过
    public static final int STATUS_REJECTED = 2; // 已拒绝
}


