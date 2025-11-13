package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员分页请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberPageRequest对象", description="会员分页请求对象")
public class MemberPageRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商户ID", required = true)
  //  @NotNull(message = "商户ID不能为空")
    private Integer merId;

    @ApiModelProperty(value = "关键词，可以是会员昵称或手机号")
    private String keyword;

    @ApiModelProperty(value = "搜索内容（前端传递）")
    private String content;

    @ApiModelProperty(value = "搜索类型（前端传递）")
    private String searchType;

    @ApiModelProperty(value = "会员等级ID")
    private Integer levelId;

    @ApiModelProperty(value = "会员等级ID（前端传递）")
    private Integer level_id;

    @ApiModelProperty(value = "日期范围字符串（前端传递）")
    private String dateLimit;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer limit = 10;
}