package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.wechat.WechatReply;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.WechatReplyRequest;
import com.zbkj.common.request.WechatReplySearchRequest;

import java.util.List;

/**
 * WechatReplyService 接口
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
public interface WechatReplyService extends IService<WechatReply> {

    /**
     * 列表
     *
     * @param request          请求参数
     * @param pageParamRequest 分页类参数
     * @return List<WechatReply>
     */
    List<WechatReply> getList(WechatReplySearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增微信关键字回复表
     *
     * @param wechatReplyRequest 新增参数
     */
    Boolean create(WechatReplyRequest wechatReplyRequest);

    /**
     * 根据关键字查询数据
     *
     * @param keywords 新增参数
     * @return WechatReply
     */
    WechatReply getVoByKeywords(String keywords);

    /**
     * 根据关键字查询数据
     *
     * @param keywords 新增参数
     * @param status 是否禁用
     * @return WechatReply
     */
    WechatReply getVoByKeywords(String keywords, Boolean status);

    /**
     * 查询微信关键字回复表信息
     *
     * @param id Integer
     */
    WechatReply getInfo(Integer id);

    /**
     * 修改微信关键字回复表
     *
     * @param wechatReplyRequest 修改参数
     */
    Boolean updateReply(WechatReplyRequest wechatReplyRequest);

    /**
     * 修改状态
     *
     * @param id     integer id
     */
    Boolean updateStatus(Integer id);

    /**
     * 根据关键字查询数据
     *
     * @param keywords 新增参数
     * @return WechatReply
     */
    WechatReply keywordsInfo(String keywords);
}
