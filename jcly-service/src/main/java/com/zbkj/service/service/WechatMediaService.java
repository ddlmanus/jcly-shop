package com.zbkj.service.service;

import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Auther: 大粽子
 * @Date: 2025/3/9 18:24
 * @Description: 微信素材管理
 */
public interface WechatMediaService {
    /**
     * 上传素材到微信端
     * @param type type	是	媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @param multipart media	是	form-data中媒体文件标识，有filename、filelength、content-type等信息
     * @return 微信素材上传结果
     * @throws WxErrorException 微信素材上传异常
     */
    WxMediaUploadResult uploadMedia(String type, MultipartFile multipart) throws WxErrorException;


    /**
     * 上传素材到微信端 用本地图片换mediaId
     * @param type type	是	媒体文件类型，分别有图片（image）
     * @param imagePath 本地图片路径
     * @return 微信素材上传结果
     */
    WxMediaUploadResult uploadMediaByLocal(String type, String imagePath);

    /**
     * 根据素材id 获取已经上传的微信端素材
     * @param mediaId 媒体id
     * @return 当前id对应的文件资源
     * @throws WxErrorException 获取资源时的异常
     */
    File getFileByMediaId(String mediaId) throws WxErrorException;
}
