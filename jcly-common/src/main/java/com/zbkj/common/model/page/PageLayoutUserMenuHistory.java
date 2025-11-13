package com.zbkj.common.model.page;

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
 * 页面装修-用户中心菜单历史记录表
 * </p>
 *
 * @author Claude
 * @since 2025-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_page_layout_user_menu_history")
@ApiModel(value="PageLayoutUserMenuHistory对象", description="页面装修-用户中心菜单历史记录表")
public class PageLayoutUserMenuHistory implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "模版名称")
    private String templateName;

    @ApiModelProperty(value = "菜单数据JSON")
    private String menuData;

    @ApiModelProperty(value = "是否默认（0=否，1=是）")
    private Integer isDefault;

    @ApiModelProperty(value = "用户中心我的服务布局模式")
    private String userMenuLayoutMode;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

}
