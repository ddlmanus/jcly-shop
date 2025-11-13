package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.QRCodeUtil;
import com.zbkj.common.vo.QrCodeVo;
import com.zbkj.service.service.QrCodeService;
import com.zbkj.service.service.WechatService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * QrCodeServiceImpl 接口实现
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
public class QrCodeServiceImpl implements QrCodeService {

    @Autowired
    private WechatService wechatService;

    /**
     * 二维码
     *
     * @return QrCodeVo
     */
    @Override
    public QrCodeVo getWecahtQrCode(JSONObject data) {
        if (ObjectUtil.isNull(data) || data.isEmpty())
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "生成微信参数不能为空");
        QrCodeVo vo = new QrCodeVo();
        vo.setCode(wechatService.createQrCode(data));
        return vo;
    }

    /**
     * 远程图片转base64
     *
     * @param url 图片链接地址
     * @return QrCodeVo
     */
    @Override
    public QrCodeVo urlToBase64(String url) {
        HttpResponse httpResponse = HttpRequest.get(url).execute();
        byte[] bytes = httpResponse.bodyBytes();
        ;
        String base64Image = CrmebUtil.getBase64Image(Base64.encodeBase64String(bytes));
        QrCodeVo vo = new QrCodeVo();
        vo.setCode(base64Image);
        return vo;
    }

    /**
     * 字符串转base64
     *
     * @param text 待转换字符串
     * @return QrCodeVo base64格式
     */
    @Override
    public QrCodeVo strToBase64(String text, Integer width, Integer height) {
        if ((width < 50 || height < 50) && (width > 500 || height > 500) && text.length() >= 999) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "生成二维码参数不合法");
        }
        String base64Image;
        try {
            base64Image = QRCodeUtil.crateQRCode(text, width, height);
        } catch (Exception e) {
            throw new CrmebException("生成二维码异常");
        }
        QrCodeVo vo = new QrCodeVo();
        vo.setCode(base64Image);
        return vo;
    }
}

