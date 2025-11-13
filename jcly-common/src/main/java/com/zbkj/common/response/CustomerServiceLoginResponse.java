package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 客服登录响应
 * @author AI Assistant
 * @since 2025-10-27
 */
@Data
@ApiModel(value = "CustomerServiceLoginResponse对象", description = "客服登录响应")
public class CustomerServiceLoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "JWT Token")
    private String token;

    @ApiModelProperty(value = "管理员ID")
    private Integer adminId;

    @ApiModelProperty(value = "客服ID")
    private Integer staffId;

    @ApiModelProperty(value = "登录账号")
    private String account;

    @ApiModelProperty(value = "客服姓名")
    private String staffName;

    @ApiModelProperty(value = "客服头像")
    private String avatar;

    @ApiModelProperty(value = "用户类型：CUSTOMER_SERVICE")
    private String userType;

    @ApiModelProperty(value = "商户ID（0表示平台）")
    private Integer merId;

    @ApiModelProperty(value = "权限列表")
    private List<String> permissions;

    @ApiModelProperty(value = "Token过期时间（秒）")
    private Long expiresIn;

    @ApiModelProperty(value = "客服等级：JUNIOR-初级，STANDARD-标准，SENIOR-高级，EXPERT-专家")
    private String serviceLevel;

    @ApiModelProperty(value = "最大并发会话数")
    private Integer maxConcurrentSessions;

    @ApiModelProperty(value = "当前会话数")
    private Integer currentSessions;
}
