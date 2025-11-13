package com.zbkj.common.exception;

import com.zbkj.common.result.IResultEnum;

/**
 * @ClassName BusinessException
 * @Description 业务异常类
 * @Author dudl
 * @Date 2025/2/22 12:34
 * @Version 1.0
 */
public class BusinessException extends CrmebException {

    private static final long serialVersionUID = 1L;

    public BusinessException(IResultEnum iResultEnum) {
        super(iResultEnum);
    }

    public BusinessException(IResultEnum iResultEnum, Throwable throwable) {
        super(iResultEnum, throwable);
    }

}
