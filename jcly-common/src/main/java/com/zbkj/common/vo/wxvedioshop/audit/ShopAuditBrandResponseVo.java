package com.zbkj.common.vo.wxvedioshop.audit;

import com.baomidou.mybatisplus.annotation.TableField;
import com.zbkj.common.vo.wxvedioshop.BaseResultResponseVo;
import lombok.Data;

/**
 *
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
public class ShopAuditBrandResponseVo extends BaseResultResponseVo {
    // 审核单id
    @TableField(value = "audit_id")
    private String auditId;
}
