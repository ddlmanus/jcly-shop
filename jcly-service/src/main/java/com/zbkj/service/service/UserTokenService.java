package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.user.UserToken;

/**
 * UserTokenService 接口实现
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
public interface UserTokenService extends IService<UserToken> {

    /**
     * 获取UserToken
     * @param token 微信为openid
     * @param type 类型
     * @return UserToken
     */
    UserToken getByOpenidAndType(String token, Integer type);

    void bind(String openId, Integer type, Integer uid);

    UserToken getTokenByUserId(Integer userId, Integer type);

    /**
     * 通过用户id删除
     * @param uid 用户ID
     */
    Boolean deleteByUid(Integer uid);
}
