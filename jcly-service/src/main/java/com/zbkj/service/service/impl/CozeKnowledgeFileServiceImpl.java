package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeKnowledgeFile;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.coze.CozeCreateKnowledgeFileRequest;
import com.zbkj.common.response.coze.CozeCreateKnowledgeFileResponse;
import com.zbkj.common.response.coze.CozeKnowledgeFileListResponse;
import com.zbkj.common.vo.FileResultVo;
import com.zbkj.service.dao.CozeKnowledgeFileDao;
import com.zbkj.service.service.CozeKnowledgeFileService;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.impl.CozeServiceImpl;
import com.zbkj.service.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * CozeKnowledgeFile 知识库文件服务实现类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CozeKnowledgeFileServiceImpl extends ServiceImpl<CozeKnowledgeFileDao, CozeKnowledgeFile> implements CozeKnowledgeFileService {

    @Autowired
    private CozeService cozeService;

    @Autowired
    private UploadService uploadService;

    @Value("${file.upload.path:/tmp/coze-uploads}")
    private String uploadPath;

    @Override
    public CozeKnowledgeFile getByCozeFileId(String cozeFileId) {
        LambdaQueryWrapper<CozeKnowledgeFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledgeFile::getCozeFileId, cozeFileId);
        wrapper.eq(CozeKnowledgeFile::getStatus, 1);
        return getOne(wrapper);
    }

    @Override
    public PageInfo<CozeKnowledgeFile> getByKnowledgeId(String cozeKnowledgeId, PageParamRequest pageParamRequest) {
        Page<CozeKnowledgeFile> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeKnowledgeFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledgeFile::getCozeKnowledgeId, cozeKnowledgeId);
        wrapper.eq(CozeKnowledgeFile::getStatus, 1);
        wrapper.orderByDesc(CozeKnowledgeFile::getCreateTime);
        List<CozeKnowledgeFile> cozeKnowledgeFiles = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(page,cozeKnowledgeFiles);
    }

    @Override
    public PageInfo<CozeKnowledgeFile> getByKnowledgeIdWithSearch(String cozeKnowledgeId, PageParamRequest pageParamRequest, String name, String type, Integer status) {
        Page<CozeKnowledgeFile> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeKnowledgeFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledgeFile::getCozeKnowledgeId, cozeKnowledgeId);
        wrapper.eq(CozeKnowledgeFile::getStatus, 1);
        
        // 添加搜索条件
        if (org.springframework.util.StringUtils.hasText(name)) {
            wrapper.like(CozeKnowledgeFile::getFileName, name);
        }
        if (org.springframework.util.StringUtils.hasText(type)) {
            wrapper.eq(CozeKnowledgeFile::getFileType, type);
        }
        if (status != null) {
            wrapper.eq(CozeKnowledgeFile::getProcessStatus, status);
        }
        
        wrapper.orderByDesc(CozeKnowledgeFile::getCreateTime);
        List<CozeKnowledgeFile> cozeKnowledgeFiles = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(page,cozeKnowledgeFiles);
    }

    @Override
    public CozeKnowledgeFile uploadFileToKnowledge(String cozeKnowledgeId, MultipartFile file, Integer merchantId) {
        try {
            // 1. 将文件转换为Base64编码
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            String fileType = getFileExtension(file.getOriginalFilename());
            
            // 2. 构建Coze API请求
            CozeCreateKnowledgeFileRequest.SourceInfo sourceInfo = CozeCreateKnowledgeFileRequest.SourceInfo.builder()
                    .fileBase64(base64Content)
                    .fileType(fileType)
                    .documentSource(0) // 0：本地文件上传
                    .build();
            
            CozeCreateKnowledgeFileRequest.DocumentBase documentBase = CozeCreateKnowledgeFileRequest.DocumentBase.builder()
                    .name(file.getOriginalFilename())
                    .sourceInfo(sourceInfo)
                    .updateRule(null) // 本地文件上传不需要更新规则
                    .build();
            
            // 3. 设置切片策略（使用默认自动分段）
            CozeCreateKnowledgeFileRequest.ChunkStrategy chunkStrategy = CozeCreateKnowledgeFileRequest.ChunkStrategy.builder()
                    .chunkType(0)  // 自动分段与清洗
                    .build();
            
            CozeCreateKnowledgeFileRequest request = CozeCreateKnowledgeFileRequest.builder()
                    .datasetId(cozeKnowledgeId)
                    .documentBases(Arrays.asList(documentBase))
                    .chunkStrategy(chunkStrategy)
                    .formatType(0)  // 文本类型
                    .build();
            
            // 4. 调用Coze API创建文件
            CozeCreateKnowledgeFileResponse response = cozeService.createKnowledgeFile(request);
            
            // 5. 从响应中获取第一个文档信息（通常只有一个）
            if (response != null && response.getDocumentInfos() != null && !response.getDocumentInfos().isEmpty()) {
                CozeCreateKnowledgeFileResponse.DocumentInfo docInfo = response.getDocumentInfos().get(0);
                
                // 6. 等待一下让事务提交，然后查找已保存的文件记录
                try {
                    Thread.sleep(500); // 等待500ms确保数据库操作完成
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                CozeKnowledgeFile savedFile = getByCozeFileId(docInfo.getDocumentId());
                if (savedFile != null) {
                    log.info("成功获取已保存的文件记录，文件ID: {}, 本地ID: {}", docInfo.getDocumentId(), savedFile.getId());
                    return savedFile;
                } else {
                    log.warn("无法从数据库获取文件记录，文件ID: {}", docInfo.getDocumentId());
                    
                    // 如果查询不到，创建一个临时对象返回
                    CozeKnowledgeFile tempFile = new CozeKnowledgeFile();
                    tempFile.setCozeKnowledgeId(cozeKnowledgeId);
                    tempFile.setCozeFileId(docInfo.getDocumentId());
                    tempFile.setFileName(docInfo.getName());
                    tempFile.setFileSize(docInfo.getSize());
                    tempFile.setFileType(docInfo.getType());
                    tempFile.setUploadStatus(1);
                    tempFile.setProcessStatus(docInfo.getStatus());
                    tempFile.setMerchantId(merchantId);
                    return tempFile;
                }
            }
            
            throw new RuntimeException("文件上传失败，无法获取文件信息");
            
        } catch (Exception e) {
            log.error("上传文件到知识库失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public CozeKnowledgeFile uploadLocalFileToKnowledge(String cozeKnowledgeId, java.io.File file, Integer merchantId) {
        try {
            // 1. 读取文件内容并转换为Base64编码
            byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);
            String fileType = getFileExtension(file.getName());
            
            // 2. 构建Coze API请求
            CozeCreateKnowledgeFileRequest.SourceInfo sourceInfo = CozeCreateKnowledgeFileRequest.SourceInfo.builder()
                    .fileBase64(base64Content)
                    .fileType(fileType)
                    .documentSource(0) // 0：本地文件上传
                    .build();
            
            CozeCreateKnowledgeFileRequest.DocumentBase documentBase = CozeCreateKnowledgeFileRequest.DocumentBase.builder()
                    .name(file.getName())
                    .sourceInfo(sourceInfo)
                    .updateRule(null) // 本地文件上传不需要更新规则
                    .build();
            
            // 3. 设置切片策略（使用默认自动分段）
            CozeCreateKnowledgeFileRequest.ChunkStrategy chunkStrategy = CozeCreateKnowledgeFileRequest.ChunkStrategy.builder()
                    .chunkType(0)  // 自动分段与清洗
                    .build();
            
            CozeCreateKnowledgeFileRequest request = CozeCreateKnowledgeFileRequest.builder()
                    .datasetId(cozeKnowledgeId)
                    .documentBases(Arrays.asList(documentBase))
                    .chunkStrategy(chunkStrategy)
                    .formatType(0)  // 文本类型
                    .build();
            
            // 4. 调用Coze API创建文件
            CozeCreateKnowledgeFileResponse response = cozeService.createKnowledgeFile(request);
            
            // 5. 从响应中获取第一个文档信息（通常只有一个）
            if (response != null && response.getDocumentInfos() != null && !response.getDocumentInfos().isEmpty()) {
                CozeCreateKnowledgeFileResponse.DocumentInfo docInfo = response.getDocumentInfos().get(0);
                
                // 6. 等待一下让事务提交，然后查找已保存的文件记录
                try {
                    Thread.sleep(500); // 等待500ms确保数据库操作完成
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                CozeKnowledgeFile savedFile = getByCozeFileId(docInfo.getDocumentId());
                if (savedFile != null) {
                    log.info("成功获取已保存的本地文件记录，文件ID: {}, 本地ID: {}", docInfo.getDocumentId(), savedFile.getId());
                    return savedFile;
                } else {
                    log.warn("无法从数据库获取本地文件记录，文件ID: {}", docInfo.getDocumentId());
                    
                    // 如果查询不到，创建一个临时对象返回
                    CozeKnowledgeFile tempFile = new CozeKnowledgeFile();
                    tempFile.setCozeKnowledgeId(cozeKnowledgeId);
                    tempFile.setCozeFileId(docInfo.getDocumentId());
                    tempFile.setFileName(docInfo.getName());
                    tempFile.setFileSize(docInfo.getSize());
                    tempFile.setFileType(docInfo.getType());
                    tempFile.setUploadStatus(1);
                    tempFile.setProcessStatus(docInfo.getStatus());
                    tempFile.setMerchantId(merchantId);
                    return tempFile;
                }
            }
            
            throw new RuntimeException("本地文件上传失败，无法获取文件信息");
            
        } catch (Exception e) {
            log.error("上传本地文件到知识库失败", e);
            throw new RuntimeException("上传本地文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<CozeKnowledgeFile> uploadMultipleFilesToKnowledge(String cozeKnowledgeId, MultipartFile[] files, Integer merchantId) {
        try {
            log.info("开始批量上传文件到知识库，知识库ID：{}，文件数量：{}", cozeKnowledgeId, files.length);
            
            // 1. 构建文档基础信息列表和原始文件大小映射
            List<CozeCreateKnowledgeFileRequest.DocumentBase> documentBases = new ArrayList<>();
            Map<String, Long> originalFileSizes = new HashMap<>();
            Map<String, String> fileDownloadUrls = new HashMap<>(); // 存储阿里云下载URL
            
            for (MultipartFile file : files) {
                // 将文件转换为Base64编码
                String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
                String fileType = getFileExtension(file.getOriginalFilename());
                
                // 同时上传到阿里云对象存储
                try {
                    FileResultVo uploadResult = uploadService.fileUpload(file, "product", 0);
                    if (uploadResult != null && uploadResult.getUrl() != null) {
                        fileDownloadUrls.put(file.getOriginalFilename(), uploadResult.getUrl());
                        log.info("文件上传到阿里云成功：{}，URL：{}", file.getOriginalFilename(), uploadResult.getUrl());
                    } else {
                        log.warn("文件上传到阿里云失败：{}", file.getOriginalFilename());
                    }
                } catch (Exception e) {
                    log.error("文件上传到阿里云异常：{}，错误：{}", file.getOriginalFilename(), e.getMessage());
                }
                
                // 构建文档基础信息
                CozeCreateKnowledgeFileRequest.DocumentBase documentBase = CozeCreateKnowledgeFileRequest.DocumentBase.builder()
                        .name(file.getOriginalFilename())
                        .sourceInfo(CozeCreateKnowledgeFileRequest.SourceInfo.builder()
                                .fileBase64(base64Content)
                                .fileType(fileType)
                                .documentSource(0) // 本地文件上传
                                .build())
                        .build();
                        
                documentBases.add(documentBase);
                // 记录原始文件大小
                originalFileSizes.put(file.getOriginalFilename(), file.getSize());
                log.info("准备上传文件：{}，大小：{} bytes，类型：{}", file.getOriginalFilename(), file.getSize(), fileType);
            }
            
            // 2. 构建Coze API请求
            CozeCreateKnowledgeFileRequest request = CozeCreateKnowledgeFileRequest.builder()
                    .datasetId(cozeKnowledgeId)
                    .documentBases(documentBases)
                    .chunkStrategy(CozeCreateKnowledgeFileRequest.ChunkStrategy.builder()
                            .chunkType(0) // 自动分段与清洗
                            .build())
                    .formatType(0) // 文本类型
                    .build();
            
            // 3. 调用Coze API创建文件，传递原始文件大小映射和下载URL
            CozeCreateKnowledgeFileResponse response = cozeService.createKnowledgeFile(request, originalFileSizes, fileDownloadUrls);
            
            // 4. 处理响应，返回保存的文件列表
            List<CozeKnowledgeFile> savedFiles = new ArrayList<>();
            
            if (response != null && response.getDocumentInfos() != null && !response.getDocumentInfos().isEmpty()) {
                log.info("Coze API返回{}个文件信息", response.getDocumentInfos().size());
                
                // 等待一下让事务提交
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                for (CozeCreateKnowledgeFileResponse.DocumentInfo docInfo : response.getDocumentInfos()) {
                    // 尝试从数据库获取已保存的文件记录
                    CozeKnowledgeFile savedFile = getByCozeFileId(docInfo.getDocumentId());
                    if (savedFile != null) {
                        savedFiles.add(savedFile);
                        log.info("成功获取已保存的文件记录，文件名：{}，文件ID: {}", savedFile.getFileName(), docInfo.getDocumentId());
                    } else {
                        log.warn("无法从数据库获取文件记录，文件ID: {}，文件名：{}", docInfo.getDocumentId(), docInfo.getName());
                        
                        // 如果查询不到，创建一个临时对象返回
                        CozeKnowledgeFile tempFile = new CozeKnowledgeFile();
                        tempFile.setCozeKnowledgeId(cozeKnowledgeId);
                        tempFile.setCozeFileId(docInfo.getDocumentId());
                        tempFile.setFileName(docInfo.getName());
                        tempFile.setFileSize(docInfo.getSize());
                        tempFile.setFileType(docInfo.getType());
                        tempFile.setUploadStatus(1);
                        tempFile.setProcessStatus(docInfo.getStatus());
                        tempFile.setMerchantId(merchantId);
                        savedFiles.add(tempFile);
                    }
                }
            } else {
                throw new RuntimeException("批量文件上传失败，Coze API返回数据为空");
            }
            
            log.info("批量文件上传完成，成功上传：{} 个文件", savedFiles.size());
            return savedFiles;
            
        } catch (Exception e) {
            log.error("批量上传文件到知识库失败", e);
            throw new RuntimeException("批量上传文件失败: " + e.getMessage());
        }
    }



    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    @Override
    public Boolean updateFileStatus(String cozeFileId, Integer uploadStatus, Integer processStatus, Integer processProgress, String errorMessage) {
        LambdaQueryWrapper<CozeKnowledgeFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledgeFile::getCozeFileId, cozeFileId);
        
        CozeKnowledgeFile file = getOne(wrapper);
        if (file == null) {
            return false;
        }
        
        if (uploadStatus != null) file.setUploadStatus(uploadStatus);
        if (processStatus != null) file.setProcessStatus(processStatus);
        if (processProgress != null) file.setProcessProgress(processProgress);
        if (errorMessage != null) file.setErrorMessage(errorMessage);
        file.setUpdateTime(new Date());
        
        return updateById(file);
    }

    @Override
    public Boolean deleteByFileId(String cozeFileId, Integer merchantId) {
        LambdaQueryWrapper<CozeKnowledgeFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledgeFile::getCozeFileId, cozeFileId);
        wrapper.eq(CozeKnowledgeFile::getMerchantId, merchantId);
        
        CozeKnowledgeFile file = getOne(wrapper);
        if (file == null) {
            return false;
        }
        
        // 先调用Coze API删除文件
        try {
            deleteFromCozeAPI(file.getCozeKnowledgeId(), cozeFileId);
        } catch (Exception e) {
            log.warn("从Coze删除文件失败，继续删除本地记录", e);
        }
        

        
        // 软删除数据库记录
        file.setStatus(0);
        file.setUpdateTime(new Date());
        return updateById(file);
    }

    /**
     * 从Coze删除文件
     */
    private void deleteFromCozeAPI(String knowledgeId, String fileId) {
        try {
            // 调用CozeService删除文件
            List<String> documentIds = Arrays.asList(fileId);
            cozeService.deleteKnowledgeFile(documentIds);
        } catch (Exception e) {
            log.error("从Coze删除文件失败", e);
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<CozeKnowledgeFile> getAllByKnowledgeId(String cozeKnowledgeId) {
        LambdaQueryWrapper<CozeKnowledgeFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledgeFile::getCozeKnowledgeId, cozeKnowledgeId);
        wrapper.eq(CozeKnowledgeFile::getStatus, 1);
        wrapper.orderByDesc(CozeKnowledgeFile::getCreateTime);
        return list(wrapper);
    }

    @Override
    public CozeKnowledgeFile checkFileProgress(String cozeFileId) {
        CozeKnowledgeFile file = getByCozeFileId(cozeFileId);
        if (file == null) {
            return null;
        }
        
        // 如果文件正在处理中，可以调用Coze API检查进度
        if (file.getProcessStatus() == 1) {
            try {
                // 调用Coze API获取文件处理进度
                updateProgressFromCozeAPI(file);
            } catch (Exception e) {
                log.warn("获取文件处理进度失败", e);
            }
        }
        
        return file;
    }

    /**
     * 从Coze API更新文件处理进度
     */
    private void updateProgressFromCozeAPI(CozeKnowledgeFile file) {
        try {
            // 这里可以调用Coze Service的查看知识库文件上传进度API
            // 暂时简化处理
            file.setProcessProgress(Math.min(file.getProcessProgress() + 10, 100));
            if (file.getProcessProgress() >= 100) {
                file.setProcessStatus(2); // 处理完成
            }
            file.setUpdateTime(new Date());
            updateById(file);
            
        } catch (Exception e) {
            log.error("更新文件处理进度失败", e);
        }
    }

    @Override
    public Boolean syncKnowledgeFileListFromCoze(Integer merchantId, String cozeKnowledgeId) {
        try {
            log.info("开始同步知识库文件列表，商户ID：{}，知识库ID：{}", merchantId, cozeKnowledgeId);
            
            // 获取Coze平台的知识库文件列表（分页获取所有数据）
            int pageNum = 1;
            int pageSize = 100;
            boolean hasMore = true;
            
            while (hasMore) {
                CozeKnowledgeFileListResponse response = getKnowledgeFileListFromCoze(cozeKnowledgeId, pageNum, pageSize);
                
                if (response != null && !CollectionUtils.isEmpty(response.getDocumentInfos())) {
                    // 保存到本地数据库
                    Boolean saveResult = saveKnowledgeFileFromCozeResponse(merchantId, cozeKnowledgeId, response.getDocumentInfos());
                    if (!saveResult) {
                        log.error("保存知识库文件数据失败，商户ID：{}，页码：{}", merchantId, pageNum);
                        return false;
                    }
                    
                    // 判断是否还有更多数据
                    hasMore = response.getDocumentInfos().size() >= pageSize;
                    pageNum++;
                } else {
                    hasMore = false;
                }
            }
            
            log.info("知识库文件列表同步完成，商户ID：{}，知识库ID：{}", merchantId, cozeKnowledgeId);
            return true;
        } catch (Exception e) {
            log.error("同步知识库文件列表失败，商户ID：{}，知识库ID：{}，错误信息：{}", merchantId, cozeKnowledgeId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public CozeKnowledgeFileListResponse getKnowledgeFileListFromCoze(String datasetId, Integer pageNum, Integer pageSize) {
        try {
            return cozeService.getKnowledgeFileListTyped(datasetId, pageNum, pageSize);
        } catch (Exception e) {
            log.error("调用Coze API获取知识库文件列表失败，知识库ID：{}，错误信息：{}", datasetId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Boolean saveKnowledgeFileFromCozeResponse(Integer merchantId, String cozeKnowledgeId, List<CozeKnowledgeFileListResponse.DocumentInfo> documentInfos) {
        if (CollectionUtils.isEmpty(documentInfos)) {
            return true;
        }
        
        try {
            Date now = new Date();
            
            for (CozeKnowledgeFileListResponse.DocumentInfo docInfo : documentInfos) {
                // 检查本地是否已存在该文件
                CozeKnowledgeFile existingFile = getByCozeFileId(docInfo.getDocumentId());
                
                CozeKnowledgeFile file;
                if (existingFile != null) {
                    // 更新现有文件
                    file = existingFile;
                } else {
                    // 创建新文件记录
                    file = new CozeKnowledgeFile();
                    file.setMerchantId(merchantId);
                    file.setCozeKnowledgeId(cozeKnowledgeId);
                    file.setCozeFileId(docInfo.getDocumentId());
                    file.setCreateTime(now);
                    file.setStatus(1);
                }
                
                // 更新文件信息
                file.setFileName(docInfo.getName());
                file.setFileSize(docInfo.getSize());
                file.setFileType(docInfo.getType());
                file.setCharCount(docInfo.getCharCount());
                file.setSliceCount(docInfo.getSliceCount());
                file.setHitCount(docInfo.getHitCount());
                file.setFormatType(docInfo.getFormatType());
                file.setSourceType(docInfo.getSourceType());
                file.setUpdateInterval(docInfo.getUpdateInterval());
                file.setUpdateType(docInfo.getUpdateType());
                file.setTosUri(docInfo.getTosUri());
                file.setCozeFileUrl(docInfo.getWebUrl());
                
                // 处理JSON字段
                if (docInfo.getChunkStrategy() != null) {
                    file.setChunkStrategy(com.alibaba.fastjson.JSON.toJSONString(docInfo.getChunkStrategy()));
                }
                
                // 映射处理状态
                file.setUploadStatus(mapCozeStatusToLocal(docInfo.getStatus()));
                file.setProcessStatus(mapCozeStatusToProcessStatus(docInfo.getStatus()));
                file.setProcessProgress(docInfo.getStatus() == 1 ? 100 : 0);
                
                file.setCozeCreateTime(docInfo.getCreateTime());
                file.setCozeUpdateTime(docInfo.getUpdateTime());
                file.setSyncTime(now);
                file.setUpdateTime(now);
                
                // 保存或更新
                saveOrUpdate(file);
            }
            
            return true;
        } catch (Exception e) {
            log.error("保存知识库文件数据失败，商户ID：{}，知识库ID：{}，错误信息：{}", merchantId, cozeKnowledgeId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 映射Coze状态到本地上传状态
     * @param cozeStatus Coze状态：0-处理中，1-处理完毕，9-处理失败
     * @return 本地状态：0-上传中，1-成功，2-失败
     */
    private Integer mapCozeStatusToLocal(Integer cozeStatus) {
        if (cozeStatus == null) {
            return 0;
        }
        switch (cozeStatus) {
            case 0: return 0; // 处理中 -> 上传中
            case 1: return 1; // 处理完毕 -> 成功
            case 9: return 2; // 处理失败 -> 失败
            default: return 0;
        }
    }

    /**
     * 映射Coze状态到本地处理状态
     * @param cozeStatus Coze状态：0-处理中，1-处理完毕，9-处理失败
     * @return 本地处理状态：0-待处理，1-处理中，2-处理成功，3-处理失败
     */
    private Integer mapCozeStatusToProcessStatus(Integer cozeStatus) {
        if (cozeStatus == null) {
            return 0;
        }
        switch (cozeStatus) {
            case 0: return 1; // 处理中
            case 1: return 2; // 处理成功
            case 9: return 3; // 处理失败
            default: return 0;
        }
    }
}
