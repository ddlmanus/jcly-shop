package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.coze.CozeSpace;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

/**
 * Coze空间服务接口
 * @author: auto-generated
 * @date: 2024/01/01
 */
public interface CozeSpaceService extends IService<CozeSpace> {

    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return CommonPage
     */
    CommonPage<CozeSpace> getList(PageParamRequest pageParamRequest);

    /**
     * 根据商户ID获取空间列表
     * @param merId 商户ID
     * @return 空间列表
     */
    List<CozeSpace> getByMerId(Integer merId);

    /**
     * 根据空间ID获取空间详情
     * @param spaceId 空间ID
     * @return 空间详情
     */
    CozeSpace getBySpaceId(String spaceId,Integer merId);

    /**
     * 同步Coze空间到本地数据库
     * @param merId 商户ID
     * @return 是否成功
     */
    Boolean syncCozeSpaces(Integer merId);

    /**
     * 删除本地空间记录
     * @param id 主键ID
     * @return 是否成功
     */
    Boolean deleteLocalSpace(Integer id);
}
