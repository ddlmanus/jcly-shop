package com.zbkj.common.validation;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.annotation.StringContains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 校验一个字符串是否包含在指定字符数组内
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
public class StringContainsValidator implements ConstraintValidator<StringContains, String> {

    private Set<String> limitValues;

    @Override
    public void initialize (StringContains constraintAnnotation) {
        limitValues = Arrays.stream(constraintAnnotation.limitValues()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StrUtil.isBlank(value)) {
            return true;
        }
        return limitValues.contains(value.trim());
    }
}
