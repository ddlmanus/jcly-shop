package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;

/**
 * 个人中心Banner响应对象
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/4/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserCenterMenuResponse", description="个人中心Banner响应对象")
public class UserCenterMenuResponse {

    @ApiModelProperty(value = "个人中心服务")
    private List<HashMap<String, Object>> centerMenu;

    @ApiModelProperty(value = "是否为推广员")
    private Boolean isPromoter = false;

    @ApiModelProperty(value = "是否移动端商家管理员")
    private Boolean isEmployee = false;

    @ApiModelProperty(value = "用户中心我的服务布局模式 0-网格模式（4列）, 1-列表模式（1列）")
    private String userMenuLayoutMode;

}
