package com.zbkj.common.vo;

import lombok.Data;

/**
 * 文件信息
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
@Data
public class CloudVo {

    //域名空间
    private String domain;

    //accessKey
    private String accessKey;

    //secretKey
    private String secretKey;

    //bucketName
    private String bucketName;

    //节点
    private String region;
}
