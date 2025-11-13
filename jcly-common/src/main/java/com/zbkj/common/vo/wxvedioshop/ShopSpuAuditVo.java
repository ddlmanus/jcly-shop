package com.zbkj.common.vo.wxvedioshop;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 *  商品审核信息Vo对象
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
public class ShopSpuAuditVo {

    /** 上一次提交时间, yyyy-MM-dd HH:mm:ss */
    @TableField(value = "submit_time")
    private String submitTime;

    /** 上一次审核时间, yyyy-MM-dd HH:mm:ss */
    @TableField(value = "audit_time")
    private String auditTime;

    /** 拒绝理由，只有edit_status为3时出现 */
    @TableField(value = "reject_reason")
    private String rejectReason;

    /** 审核单id */
    @TableField(value = "audit_id")
    private String auditId;

}
