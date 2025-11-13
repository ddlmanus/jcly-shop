package com.zbkj.common.vo.wxvedioshop.audit;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 上传类目资质Request
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Data
public class ShopAuditCategoryRequestVo {

    @ApiModelProperty(value = "类目资质审核对象")
    private ShopAuditCategoryRequestItemVo audit_req;
}
