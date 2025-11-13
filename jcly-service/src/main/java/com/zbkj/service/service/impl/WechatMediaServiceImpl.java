package com.zbkj.service.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.zbkj.common.config.CrmebConfig;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.common.utils.UploadUtil;
import com.zbkj.service.service.WechatMediaService;
import com.zbkj.service.service.WechatService;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


/**
 * @Auther: 大粽子
 * @Date: 2025/3/9 18:29
 * @Description: 微信素材上传和获取路径
 */
@Service
public class WechatMediaServiceImpl implements WechatMediaService {
    private static final Logger logger = LoggerFactory.getLogger(WechatMediaService.class);

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private CrmebConfig crmebConfig;

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Autowired
    private WechatService wechatService;

    /**
     * 上传素材到微信端
     * @param type type	是	媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @param multipart media	是	form-data中媒体文件标识，有filename、filelength、content-type等信息
     * @return 微信素材上传结果
     * @throws WxErrorException 微信素材上传异常
     */
    @Override
    public WxMediaUploadResult uploadMedia(String type, MultipartFile multipart) {
        WxMediaUploadResult wxMediaUploadResult;
        // 创建转存文件
        String rootPath = crmebConfig.getImagePath().trim();
        // 模块
        String modelPath = "public/wechat/";
        // 变更文件名
        String newFileName = UploadUtil.fileName(FilenameUtils.getExtension(multipart.getOriginalFilename()).toLowerCase());
        // 创建目标文件的名称，规则：  子目录/年/月/日.后缀名
        String webPath = modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        // 文件分隔符转化为当前系统的格式
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + newFileName;

        try {
            // 创建文件
            File file = UploadUtil.createFile(destPath);
            multipart.transferTo(file);

            wxMediaUploadResult = wxMaService.getMediaService().uploadMedia(type, file);
        }catch (Exception e){
            logger.error("上传微信素材出错:{}", e.getMessage());
            throw new CrmebException(StrUtil.format("上传微信素材出错:{}", e.getMessage()));
        }
        return wxMediaUploadResult;
    }

    /**
     * 上传素材到微信端 用本地图片换mediaId
     *
     * @param type      type	是	媒体文件类型，分别有图片（image）
     * @param imagePath 本地图片路径
     * @return 微信素材上传结果
     */
    @Override
    public WxMediaUploadResult uploadMediaByLocal(String type, String imagePath) {
        WxMediaUploadResult wxMediaUploadResult;
        // 替换路径找到本地素材
//        String localImage = crmebConfig.getImagePath().trim() + systemAttachmentService.clearPrefix(imagePath);
        // 创建转存文件
        String rootPath = crmebConfig.getImagePath().trim();
        // 模块
        String modelPath = "/crmebimage/public/wechat/";
        // 变更文件名
        String newFileName = UploadUtil.fileName(FilenameUtils.getExtension(imagePath).toLowerCase());
        // 创建目标文件的名称，规则：  子目录/年/月/日.后缀名
        String webPath = modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        // 文件分隔符转化为当前系统的格式
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + newFileName ;
        // 创建文件
        File file = new File(destPath);
        HttpUtil.downloadFile(imagePath, file);
        try {
            wxMediaUploadResult = wxMaService.getMediaService().uploadMedia(type, file);
            return wxMediaUploadResult;
        }catch (Exception e){
            logger.error("上传微信素材出错:{}", e.getMessage());
            throw new CrmebException(StrUtil.format("上传微信素材出错:{}", e.getMessage()));
        }
    }

    /**
     * 根据素材id 获取已经上传的微信端素材
     * @param mediaId 媒体id
     * @return 当前id对应的文件资源
     * @throws WxErrorException 获取资源时的异常
     */
    @Override
    public File getFileByMediaId(String mediaId) throws WxErrorException {
        return wxMaService.getMediaService().getMedia(mediaId);
    }
}
