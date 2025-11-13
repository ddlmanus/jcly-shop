package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.bill.UserBill;
import com.zbkj.service.dao.UserBillDao;
import com.zbkj.service.service.UserBillService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * UserBillServiceImpl 接口实现
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
public class UserBillServiceImpl extends ServiceImpl<UserBillDao, UserBill> implements UserBillService {

    @Resource
    private UserBillDao dao;

}

