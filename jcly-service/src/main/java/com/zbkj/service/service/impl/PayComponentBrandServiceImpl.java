package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.wechat.video.PayComponentBrand;
import com.zbkj.service.dao.PayComponentBrandDao;
import com.zbkj.service.service.PayComponentBrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Service
public class PayComponentBrandServiceImpl extends ServiceImpl<PayComponentBrandDao, PayComponentBrand> implements PayComponentBrandService {

    private final Logger logger = LoggerFactory.getLogger(PayComponentBrandServiceImpl.class);

    @Resource
    private PayComponentBrandDao dao;

//    @Autowired
//    private WechatVideoSpuService wechatVideoSpuService;

//    @Autowired
//    private TransactionTemplate transactionTemplate;

    /**
     * 更新数据
     */
//    @Override
//    public void updateData() {
//        List<ShopBrandVo> shopBrandList = wechatVideoSpuService.getShopBrandList();
//        if (CollUtil.isEmpty(shopBrandList)) {
//            return ;
//        }
//        List<PayComponentBrand> brandList = shopBrandList.stream().map(e -> {
//            PayComponentBrand brand = new PayComponentBrand();
//            BeanUtils.copyProperties(e, brand);
//            return brand;
//        }).collect(Collectors.toList());
//
//        Boolean execute = transactionTemplate.execute(e -> {
//            saveBatch(brandList);
//            return Boolean.TRUE;
//        });
//        if (!execute) {
//            throw new CrmebException("更新自定义交易品牌列表,操作数据库时出错");
//        }
//    }
}

