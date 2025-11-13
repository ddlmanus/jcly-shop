package com.zbkj.common.vo.wxvedioshop.audit;

import java.util.List;

/**
 * 获取小程序提交过的入驻资质信息 ItemData
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
public class ShopAuditGetMinCerBrandInfoItemDataVo {
    // 品牌名
    private String brand_wording;
    // 商标注册证
    private List<String> sale_authorization;
    // 商标授权书
    private List<String> trademark_registration_certificate;

}
