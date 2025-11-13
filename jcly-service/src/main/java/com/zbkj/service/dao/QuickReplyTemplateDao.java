package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.service.QuickReplyTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 快捷回复模板Mapper接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Mapper
public interface QuickReplyTemplateDao extends BaseMapper<QuickReplyTemplate> {

    /**
     * 根据分类获取模板列表
     */
    List<QuickReplyTemplate> selectByCategory(@Param("merId") Long merId, 
                                             @Param("staffId") Long staffId,
                                             @Param("category") String category);

    /**
     * 更新模板使用次数
     */
    Integer incrementUsageCount(@Param("templateId") Long templateId);

    /**
     * 获取最常用的模板
     */
    List<QuickReplyTemplate> selectMostUsed(@Param("merId") Long merId, 
                                           @Param("limit") Integer limit);
}
