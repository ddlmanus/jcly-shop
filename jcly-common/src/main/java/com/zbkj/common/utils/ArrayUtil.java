package com.zbkj.common.utils;

import java.util.List;

/**
 * ArrayUtil
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
public class ArrayUtil {

    /**
     * list转为字符串，专用于sql中in函数
     *
     * @param list 参数列表
     * @return String
     */
    public static String strListToSqlJoin(List<String> list) {
        if (null == list || list.size() < 1) {
            return "";
        }
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                temp.append(",");
            }
            temp.append("'").append(list.get(i)).append("'");
        }
        return temp.toString();
    }
}
