package com.zbkj.service.service;


/**
 * 京东云 Service
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
public interface JdCloudService {

    /**
     * 文件上传
     * @param fileName 文件名称
     * @param localFilePath 本地文件地址
     * @param bucket 存储桶名称
     */
    void uploadFile(String fileName, String localFilePath, String bucket);
}
