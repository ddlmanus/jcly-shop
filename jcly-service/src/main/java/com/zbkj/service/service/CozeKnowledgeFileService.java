package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeKnowledgeFile;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.coze.CozeKnowledgeFileListResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * CozeKnowledgeFile 知识库文件服务类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
public interface CozeKnowledgeFileService extends IService<CozeKnowledgeFile> {

    /**
     * 根据Coze File ID查找
     */
    CozeKnowledgeFile getByCozeFileId(String cozeFileId);

    /**
     * 根据知识库ID获取文件列表
     */
    PageInfo<CozeKnowledgeFile> getByKnowledgeId(String cozeKnowledgeId, PageParamRequest pageParamRequest);

    /**
     * 根据知识库ID获取文件列表（带搜索条件）
     */
    PageInfo<CozeKnowledgeFile> getByKnowledgeIdWithSearch(String cozeKnowledgeId, PageParamRequest pageParamRequest, String name, String type, Integer status);

    /**
     * 上传文件到知识库
     */
    CozeKnowledgeFile uploadFileToKnowledge(String cozeKnowledgeId, MultipartFile file, Integer merchantId);

    /**
     * 上传本地文件到知识库
     */
    CozeKnowledgeFile uploadLocalFileToKnowledge(String cozeKnowledgeId, java.io.File file, Integer merchantId);

    /**
     * 批量上传文件到知识库
     */
    List<CozeKnowledgeFile> uploadMultipleFilesToKnowledge(String cozeKnowledgeId, MultipartFile[] files, Integer merchantId);

    /**
     * 更新文件处理状态
     */
    Boolean updateFileStatus(String cozeFileId, Integer uploadStatus, Integer processStatus, Integer processProgress, String errorMessage);

    /**
     * 删除知识库文件
     */
    Boolean deleteByFileId(String cozeFileId, Integer merchantId);

    /**
     * 获取知识库的所有文件
     */
    List<CozeKnowledgeFile> getAllByKnowledgeId(String cozeKnowledgeId);

    /**
     * 检查文件上传进度
     */
    CozeKnowledgeFile checkFileProgress(String cozeFileId);

    /**
     * 从Coze平台同步知识库文件列表到本地数据库
     * @param merchantId 商户ID
     * @param cozeKnowledgeId 知识库ID
     * @return 同步结果
     */
    Boolean syncKnowledgeFileListFromCoze(Integer merchantId, String cozeKnowledgeId);

    /**
     * 调用Coze API获取知识库文件列表
     * @param datasetId 知识库ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return Coze API响应
     */
    CozeKnowledgeFileListResponse getKnowledgeFileListFromCoze(String datasetId, Integer pageNum, Integer pageSize);

    /**
     * 将Coze API响应的文件数据保存到本地数据库
     * @param merchantId 商户ID
     * @param cozeKnowledgeId 知识库ID
     * @param documentInfos 文件数据列表
     * @return 保存结果
     */
    Boolean saveKnowledgeFileFromCozeResponse(Integer merchantId, String cozeKnowledgeId, List<CozeKnowledgeFileListResponse.DocumentInfo> documentInfos);
}
