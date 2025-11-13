package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.wechat.video.PayComponentCat;
import org.apache.ibatis.annotations.Update;

/**
 * 组件类目表 Mapper 接口
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
public interface PayComponentCatDao extends BaseMapper<PayComponentCat> {

    @Update("truncate table eb_pay_component_cat")
    void deleteAll();

}
