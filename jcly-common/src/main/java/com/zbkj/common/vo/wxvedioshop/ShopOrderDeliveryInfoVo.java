package com.zbkj.common.vo.wxvedioshop;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 生成订单Vo对象
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
public class ShopOrderDeliveryInfoVo {

    /** 快递公司ID，通过获取快递公司列表获取 */
    @TableField(value = "delivery_id")
    private Integer deliveryId;

    /** 快递单号 */
    @TableField(value = "waybill_id")
    private Integer waybillId;
}
