package com.zbkj.common.constants;

/**
 * 用户常量表
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
public class UserConstants {

    /** 注册类型——H5 */
    public static final String REGISTER_TYPE_H5 = "h5";
    /** 注册类型——公众号 */
    public static final String REGISTER_TYPE_WECHAT = "wechat";
    /** 注册类型——小程序 */
    public static final String REGISTER_TYPE_ROUTINE = "routine";
    /** 注册类型——微信ios */
    public static final String REGISTER_TYPE_IOS_WX = "iosWx";
    /** 注册类型——微信安卓 */
    public static final String REGISTER_TYPE_ANDROID_WX = "androidWx";
    /** 注册类型——ios */
    public static final String REGISTER_TYPE_IOS = "ios";
    /** 注册类型——微信网站应用PC */
    public static final String REGISTER_TYPE_WEB_PC_WX = "pcWx";

    /**
     * =========================================================
     * UserToken部分
     * =========================================================
     */
    /** 用户Token类型——公众号 */
    public static final Integer USER_TOKEN_TYPE_WECHAT = 1;
    /** 用户Token类型——小程序 */
    public static final Integer USER_TOKEN_TYPE_ROUTINE = 2;
    /** 用户Token类型——微信ios */
    public static final Integer USER_TOKEN_TYPE_IOS_WX  = 5;
    /** 用户Token类型——微信android */
    public static final Integer USER_TOKEN_TYPE_ANDROID_WX = 6;
    /** 用户Token类型——ios */
    public static final Integer USER_TOKEN_TYPE_IOS  = 7;
    /** 用户Token类型——网站应用PC */
    public static final Integer USER_TOKEN_TYPE_WEB_PC_WX = 8;

    /**
     * =========================================================
     * 用户搜索部分
     * =========================================================
     */

    /** 用户搜索类型——all */
    public static final String USER_SEARCH_TYPE_ALL = "all";
    /** 用户搜索类型——UID 精准匹配 */
    public static final String USER_SEARCH_TYPE_UID = "uid";
    /** 用户搜索类型——用户昵称 全模糊匹配 */
    public static final String USER_SEARCH_TYPE_NICKNAME = "nickname";
    /** 用户搜索类型——手机号 精准匹配 */
    public static final String USER_SEARCH_TYPE_PHONE = "phone";
}
