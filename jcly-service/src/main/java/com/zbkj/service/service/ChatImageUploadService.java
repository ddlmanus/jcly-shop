package com.zbkj.service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 聊天图片上传服务接口
 * 企业级图片处理，支持多种格式、压缩、安全验证
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface ChatImageUploadService {

    /**
     * 上传聊天图片
     * @param file 图片文件
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 上传结果，包含图片URL、缩略图URL等信息
     */
    Map<String, Object> uploadChatImage(MultipartFile file, String sessionId, Integer userId);

    /**
     * 批量上传聊天图片
     * @param files 图片文件数组
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 批量上传结果
     */
    Map<String, Object> batchUploadChatImages(MultipartFile[] files, String sessionId, Integer userId);

    /**
     * 删除聊天图片
     * @param imageUrl 图片URL
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteChatImage(String imageUrl, Integer userId);

    /**
     * 获取图片信息
     * @param imageUrl 图片URL
     * @return 图片信息
     */
    Map<String, Object> getImageInfo(String imageUrl);

    /**
     * 压缩图片
     * @param originalFile 原图片文件
     * @param quality 压缩质量 (0.1-1.0)
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 压缩后的图片文件
     */
    byte[] compressImage(MultipartFile originalFile, float quality, int maxWidth, int maxHeight);

    /**
     * 生成缩略图
     * @param originalFile 原图片文件
     * @param width 缩略图宽度
     * @param height 缩略图高度
     * @return 缩略图文件
     */
    byte[] generateThumbnail(MultipartFile originalFile, int width, int height);

    /**
     * 验证图片文件
     * @param file 图片文件
     * @return 验证结果
     */
    Map<String, Object> validateImageFile(MultipartFile file);
}
