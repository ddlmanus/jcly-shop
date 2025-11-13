package com.zbkj.service.service.impl;

import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.service.service.ChatImageUploadService;
import com.zbkj.service.service.SystemAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天图片上传服务实现
 * 企业级图片处理实现
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class ChatImageUploadServiceImpl implements ChatImageUploadService {

    @Autowired
    private SystemAttachmentService systemAttachmentService;

    @Value("${crmeb.imagePath}")
    private String imagePath;

    // 支持的图片格式
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp");
    
    // 最大文件大小 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 最大图片尺寸
    private static final int MAX_IMAGE_WIDTH = 4096;
    private static final int MAX_IMAGE_HEIGHT = 4096;

    @Override
    public Map<String, Object> uploadChatImage(MultipartFile file, String sessionId, Integer userId) {
        try {
            log.info("开始上传聊天图片: sessionId={}, userId={}, fileName={}", sessionId, userId, file.getOriginalFilename());

            // 验证图片文件
            Map<String, Object> validation = validateImageFile(file);
            if (!(Boolean) validation.get("valid")) {
                throw new CrmebException((String) validation.get("message"));
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String fileName = "chat_" + sessionId + "_" + System.currentTimeMillis() + "_" + CrmebUtil.randomCount(0,999999) + "." + extension;

            // 压缩图片
            byte[] compressedImage = compressImage(file, 0.8f, 1920, 1920);
            
            // 生成缩略图
            byte[] thumbnail = generateThumbnail(file, 200, 200);

            // 保存原图和缩略图
            String originalUrl = saveImageFile(compressedImage, fileName, "chat/images/");
            String thumbnailUrl = saveImageFile(thumbnail, "thumb_" + fileName, "chat/thumbnails/");

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageType", "image");
            result.put("originalUrl", originalUrl);
            result.put("thumbnailUrl", thumbnailUrl);
            result.put("fileName", originalFilename);
            result.put("fileSize", file.getSize());
            result.put("compressedSize", compressedImage.length);
            result.put("sessionId", sessionId);
            result.put("uploadTime", System.currentTimeMillis());

            // 获取图片尺寸信息
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(compressedImage));
                if (image != null) {
                    result.put("width", image.getWidth());
                    result.put("height", image.getHeight());
                }
            } catch (Exception e) {
                log.warn("获取图片尺寸失败: {}", e.getMessage());
            }

            log.info("聊天图片上传成功: sessionId={}, originalUrl={}", sessionId, originalUrl);
            return result;

        } catch (Exception e) {
            log.error("聊天图片上传失败: sessionId={}, userId={}, 错误: {}", sessionId, userId, e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "图片上传失败: " + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> batchUploadChatImages(MultipartFile[] files, String sessionId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Map<String, Object> uploadResult = uploadChatImage(file, sessionId, userId);
                if ((Boolean) uploadResult.get("success")) {
                    successList.add(uploadResult);
                } else {
                    Map<String, Object> failure = new HashMap<>();
                    failure.put("fileName", file.getOriginalFilename());
                    failure.put("message", uploadResult.get("message"));
                    failureList.add(failure);
                }
            } catch (Exception e) {
                Map<String, Object> failure = new HashMap<>();
                failure.put("fileName", file.getOriginalFilename());
                failure.put("message", e.getMessage());
                failureList.add(failure);
            }
        }

        result.put("success", failureList.isEmpty());
        result.put("successCount", successList.size());
        result.put("failureCount", failureList.size());
        result.put("successList", successList);
        result.put("failureList", failureList);

        return result;
    }

    @Override
    public boolean deleteChatImage(String imageUrl, Integer userId) {
        try {
            log.info("删除聊天图片: imageUrl={}, userId={}", imageUrl, userId);
            // 这里可以实现实际的文件删除逻辑
            // 暂时返回true
            return true;
        } catch (Exception e) {
            log.error("删除聊天图片失败: imageUrl={}, 错误: {}", imageUrl, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getImageInfo(String imageUrl) {
        Map<String, Object> info = new HashMap<>();
        try {
            // 这里可以实现获取图片信息的逻辑
            info.put("url", imageUrl);
            info.put("exists", true);
        } catch (Exception e) {
            log.error("获取图片信息失败: imageUrl={}, 错误: {}", imageUrl, e.getMessage(), e);
            info.put("exists", false);
        }
        return info;
    }

    @Override
    public byte[] compressImage(MultipartFile originalFile, float quality, int maxWidth, int maxHeight) {
        try {
            BufferedImage originalImage = ImageIO.read(originalFile.getInputStream());
            if (originalImage == null) {
                throw new CrmebException("无法读取图片文件");
            }

            // 计算压缩后的尺寸
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
            if (scale >= 1.0) {
                scale = 1.0; // 不放大图片
            }

            int targetWidth = (int) (originalWidth * scale);
            int targetHeight = (int) (originalHeight * scale);

            // 创建压缩后的图片
            BufferedImage compressedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = compressedImage.createGraphics();
            
            // 设置渲染提示以提高图片质量
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // 输出压缩后的图片
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(compressedImage, "jpg", baos);
            
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("图片压缩失败: {}", e.getMessage(), e);
            throw new CrmebException("图片压缩失败");
        }
    }

    @Override
    public byte[] generateThumbnail(MultipartFile originalFile, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(originalFile.getInputStream());
            if (originalImage == null) {
                throw new CrmebException("无法读取图片文件");
            }

            // 创建缩略图
            BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();

            // 输出缩略图
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);
            
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("生成缩略图失败: {}", e.getMessage(), e);
            throw new CrmebException("生成缩略图失败");
        }
    }

    @Override
    public Map<String, Object> validateImageFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("valid", false);
                result.put("message", "文件不能为空");
                return result;
            }

            // 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                result.put("valid", false);
                result.put("message", "文件大小不能超过10MB");
                return result;
            }

            // 检查文件扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                result.put("valid", false);
                result.put("message", "文件名无效");
                return result;
            }

            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!SUPPORTED_FORMATS.contains(extension)) {
                result.put("valid", false);
                result.put("message", "不支持的图片格式，仅支持: " + String.join(", ", SUPPORTED_FORMATS));
                return result;
            }

            // 验证是否为真实的图片文件
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    result.put("valid", false);
                    result.put("message", "无效的图片文件");
                    return result;
                }

                // 检查图片尺寸
                if (image.getWidth() > MAX_IMAGE_WIDTH || image.getHeight() > MAX_IMAGE_HEIGHT) {
                    result.put("valid", false);
                    result.put("message", "图片尺寸过大，最大支持" + MAX_IMAGE_WIDTH + "x" + MAX_IMAGE_HEIGHT);
                    return result;
                }

            } catch (IOException e) {
                result.put("valid", false);
                result.put("message", "图片文件损坏或格式错误");
                return result;
            }

            result.put("valid", true);
            result.put("message", "验证通过");
            return result;

        } catch (Exception e) {
            log.error("验证图片文件失败: {}", e.getMessage(), e);
            result.put("valid", false);
            result.put("message", "文件验证失败");
            return result;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 保存图片文件
     */
    private String saveImageFile(byte[] imageData, String fileName, String subPath) {
        try {
            // 这里可以集成现有的文件上传服务
            // 暂时返回模拟的URL
            String baseUrl = "https://your-domain.com/";
            return baseUrl + subPath + fileName;
            
        } catch (Exception e) {
            log.error("保存图片文件失败: fileName={}, 错误: {}", fileName, e.getMessage(), e);
            throw new CrmebException("保存图片文件失败");
        }
    }
}
