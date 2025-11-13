package com.zbkj.service.service;

import com.qiniu.storage.UploadManager;

import java.io.File;

/**
 * QiNiuService 接口
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
public interface QiNiuService {

    void uploadFile(UploadManager uploadManager, String upToken, String webPth, String localFile, File file);
}
