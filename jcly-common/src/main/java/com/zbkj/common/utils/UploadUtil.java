package com.zbkj.common.utils;

import cn.hutool.core.util.RandomUtil;
import com.zbkj.common.exception.CrmebException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * 上传工具类
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
public class UploadUtil {

    /**
     * 根据文件的绝对路径创建一个文件对象.
     *
     * @return 返回创建的这个文件对象
     * @since 2020-05-08
     */
    public static File createFile(String filePath) throws IOException {
        // 获取文件的完整目录
        String fileDir = FilenameUtils.getFullPath(filePath);
        // 判断目录是否存在，不存在就创建一个目录
        File file = new File(fileDir);
        if (!file.isDirectory()) {
            //创建失败返回null
            if (!file.mkdirs()) {
                throw new CrmebException("文件目录创建失败...");
            }
        }
        // 判断这个文件是否存在，不存在就创建
        file = new File(filePath);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new CrmebException("目标文件创建失败...");
            }
        }
        return file;
    }

    public static String fileName(String extName) {
        return CrmebUtil.getUuid() + RandomUtil.randomString(10) + "." + extName;
    }

    public static String generateFileName(String fileExtension) {
        return CrmebUtil.getUuid() + "." + fileExtension;
    }
}
