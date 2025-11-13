package com.zbkj.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.vo.LoginUserVo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * security工具类
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
public class SecurityUtil {

    /**
     * 获取管理员信息（从security中）
     */
    public static LoginUserVo getLoginUserVo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUserVo loginUserVo = (LoginUserVo) authentication.getPrincipal();
        if (ObjectUtil.isNull(loginUserVo)) {
            throw new CrmebException("登录信息已过期，请重新登录");
        }
        return loginUserVo;
    }

}
