package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.user.UserBankCard;
import com.zbkj.common.vo.BankCardVerifyRequestVo;
import com.zbkj.common.vo.BankCardVerifyResponseVo;
import com.zbkj.service.dao.UserBankCardDao;
import com.zbkj.service.service.UnionPayService;
import com.zbkj.service.service.UserBankCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * 用户银行卡信息服务实现
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
@Service
public class UserBankCardServiceImpl extends ServiceImpl<UserBankCardDao, UserBankCard> implements UserBankCardService {

    private static final Logger logger = LoggerFactory.getLogger(UserBankCardServiceImpl.class);
    
    // 加密密钥，实际项目中应该从配置文件读取
    private static final String ENCRYPT_KEY = "CRMEB_BANKCARD_KEY_2024";

    @Autowired
    private UnionPayService unionPayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankCardVerifyResponseVo verifyAndSaveBankCard(Integer uid, BankCardVerifyRequestVo request) {
        logger.info("开始验证并保存银行卡，用户ID: {}, 验证类型: {}", uid, request.getVerifyType());
        
        // 1. 调用银联验证接口
        BankCardVerifyResponseVo verifyResponse = unionPayService.verifyBankCard(request);
        
        if (!verifyResponse.getVerifyResult()) {
            logger.warn("银行卡验证失败，用户ID: {}, 错误信息: {}", uid, verifyResponse.getMessage());
            return verifyResponse;
        }
        
        // 2. 检查是否已存在相同银行卡
        String cardNoLast4 = request.getCardNo().substring(request.getCardNo().length() - 4);
        if (checkBankCardExists(uid, cardNoLast4)) {
            logger.warn("用户已存在相同银行卡，用户ID: {}, 卡号后4位: {}", uid, cardNoLast4);
            verifyResponse.setMessage("该银行卡已存在");
            verifyResponse.setResponseCode("01");
            verifyResponse.setVerifyResult(false);
            return verifyResponse;
        }
        
        try {
            // 3. 保存银行卡信息
            UserBankCard bankCard = new UserBankCard();
            bankCard.setUid(uid);
            bankCard.setCardNo(encrypt(request.getCardNo()));
            bankCard.setCardNoLast4(cardNoLast4);
            bankCard.setCardholderName(encrypt(request.getCardholderName()));
            bankCard.setIdCard(encrypt(request.getIdCard()));
            
            if (!StringUtils.isEmpty(request.getMobile())) {
                bankCard.setMobile(encrypt(request.getMobile()));
            }
            
            bankCard.setBankName(verifyResponse.getBankName());
            bankCard.setCardType(verifyResponse.getCardType());
            bankCard.setVerifyType(request.getVerifyType());
            bankCard.setStatus(1);
            bankCard.setCreateTime(new Date());
            bankCard.setUpdateTime(new Date());
            
            // 4. 如果是用户第一张银行卡，设为默认卡
            List<UserBankCard> userCards = getUserBankCards(uid);
            if (userCards.isEmpty()) {
                bankCard.setIsDefault(1);
            } else {
                bankCard.setIsDefault(0);
            }
            
            boolean saved = save(bankCard);
            if (saved) {
                logger.info("银行卡保存成功，用户ID: {}, 银行卡ID: {}", uid, bankCard.getId());
                verifyResponse.setDetail("银行卡验证成功并已保存");
            } else {
                logger.error("银行卡保存失败，用户ID: {}", uid);
                verifyResponse.setMessage("银行卡保存失败");
                verifyResponse.setResponseCode("99");
                verifyResponse.setVerifyResult(false);
            }
            
            return verifyResponse;
            
        } catch (Exception e) {
            logger.error("保存银行卡信息异常，用户ID: " + uid, e);
            verifyResponse.setMessage("保存银行卡信息异常");
            verifyResponse.setResponseCode("99");
            verifyResponse.setVerifyResult(false);
            return verifyResponse;
        }
    }

    @Override
    public List<UserBankCard> getUserBankCards(Integer uid) {
        LambdaQueryWrapper<UserBankCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBankCard::getUid, uid)
               .eq(UserBankCard::getStatus, 1)
               .orderByDesc(UserBankCard::getIsDefault)
               .orderByDesc(UserBankCard::getCreateTime);
        return list(wrapper);
    }

    @Override
    public UserBankCard getDefaultBankCard(Integer uid) {
        LambdaQueryWrapper<UserBankCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBankCard::getUid, uid)
               .eq(UserBankCard::getStatus, 1)
               .eq(UserBankCard::getIsDefault, 1);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefaultBankCard(Integer uid, Integer cardId) {
        // 1. 取消当前默认卡
        LambdaUpdateWrapper<UserBankCard> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserBankCard::getUid, uid)
                    .eq(UserBankCard::getIsDefault, 1)
                    .set(UserBankCard::getIsDefault, 0)
                    .set(UserBankCard::getUpdateTime, new Date());
        update(updateWrapper);
        
        // 2. 设置新的默认卡
        LambdaUpdateWrapper<UserBankCard> setDefaultWrapper = new LambdaUpdateWrapper<>();
        setDefaultWrapper.eq(UserBankCard::getId, cardId)
                        .eq(UserBankCard::getUid, uid)
                        .eq(UserBankCard::getStatus, 1)
                        .set(UserBankCard::getIsDefault, 1)
                        .set(UserBankCard::getUpdateTime, new Date());
        return update(setDefaultWrapper);
    }

    @Override
    public Boolean deleteBankCard(Integer uid, Integer cardId) {
        LambdaUpdateWrapper<UserBankCard> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserBankCard::getId, cardId)
               .eq(UserBankCard::getUid, uid)
               .set(UserBankCard::getStatus, 0)
               .set(UserBankCard::getUpdateTime, new Date());
        return update(wrapper);
    }

    @Override
    public Boolean checkBankCardExists(Integer uid, String cardNoLast4) {
        LambdaQueryWrapper<UserBankCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBankCard::getUid, uid)
               .eq(UserBankCard::getCardNoLast4, cardNoLast4)
               .eq(UserBankCard::getStatus, 1);
        return count(wrapper) > 0;
    }

    @Override
    public UserBankCard getBankCardById(Integer uid, Integer cardId) {
        LambdaQueryWrapper<UserBankCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBankCard::getId, cardId)
               .eq(UserBankCard::getUid, uid)
               .eq(UserBankCard::getStatus, 1);
        UserBankCard bankCard = getOne(wrapper);
        if (bankCard == null) {
            throw new CrmebException("银行卡不存在或无权限访问");
        }
        return bankCard;
    }

    /**
     * 加密敏感信息
     */
    private String encrypt(String plainText) {
        if (StringUtils.isEmpty(plainText)) {
            return plainText;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPT_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("加密失败", e);
            throw new CrmebException("数据加密失败");
        }
    }

    /**
     * 解密敏感信息
     */
    public String decrypt(String encryptedText) {
        if (StringUtils.isEmpty(encryptedText)) {
            return encryptedText;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPT_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted);
        } catch (Exception e) {
            logger.error("解密失败", e);
            throw new CrmebException("数据解密失败");
        }
    }
}
