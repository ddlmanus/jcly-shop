package com.zbkj.service.service;

import com.qcloud.cos.COSClient;
import com.zbkj.common.vo.CloudVo;

import java.io.File;

/**
 * CosService 接口
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
public interface CosService {

    void uploadFile(CloudVo cloudVo, String webPth, String localFile, Integer id, COSClient cosClient);

}
