package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.user.UserBankCard;
import com.zbkj.common.vo.BankCardVerifyRequestVo;
import com.zbkj.common.vo.BankCardVerifyResponseVo;

import java.util.List;

/**
 * 用户银行卡信息服务接口
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
public interface UserBankCardService extends IService<UserBankCard> {

    /**
     * 验证并保存银行卡信息
     * @param uid 用户ID
     * @param request 银行卡验证请求
     * @return 验证结果和保存的银行卡信息
     */
    BankCardVerifyResponseVo verifyAndSaveBankCard(Integer uid, BankCardVerifyRequestVo request);

    /**
     * 获取用户的银行卡列表
     * @param uid 用户ID
     * @return 银行卡列表
     */
    List<UserBankCard> getUserBankCards(Integer uid);

    /**
     * 获取用户的默认银行卡
     * @param uid 用户ID
     * @return 默认银行卡信息
     */
    UserBankCard getDefaultBankCard(Integer uid);

    /**
     * 设置默认银行卡
     * @param uid 用户ID
     * @param cardId 银行卡ID
     * @return 是否设置成功
     */
    Boolean setDefaultBankCard(Integer uid, Integer cardId);

    /**
     * 删除银行卡
     * @param uid 用户ID
     * @param cardId 银行卡ID
     * @return 是否删除成功
     */
    Boolean deleteBankCard(Integer uid, Integer cardId);

    /**
     * 检查用户是否已添加相同银行卡
     * @param uid 用户ID
     * @param cardNoLast4 银行卡后4位
     * @return 是否存在相同银行卡
     */
    Boolean checkBankCardExists(Integer uid, String cardNoLast4);

    /**
     * 根据ID获取用户银行卡（需要验证用户权限）
     * @param uid 用户ID
     * @param cardId 银行卡ID
     * @return 银行卡信息
     */
    UserBankCard getBankCardById(Integer uid, Integer cardId);
}
