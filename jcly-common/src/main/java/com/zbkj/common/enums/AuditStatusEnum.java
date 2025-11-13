package com.zbkj.common.enums;

/**
 * 审核状态枚举
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
public enum AuditStatusEnum {
    
    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核拒绝");
    
    private Integer code;
    private String message;
    
    AuditStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据code获取枚举
     * @param code 状态码
     * @return AuditStatusEnum
     */
    public static AuditStatusEnum getByCode(Integer code) {
        for (AuditStatusEnum status : AuditStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
} 