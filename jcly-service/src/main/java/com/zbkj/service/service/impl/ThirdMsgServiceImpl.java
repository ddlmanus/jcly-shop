package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.msg.ThirdMsg;
import com.zbkj.service.dao.ThirdMsgDao;
import com.zbkj.service.service.ThirdMsgService;
import org.springframework.stereotype.Service;

@Service
public class ThirdMsgServiceImpl extends ServiceImpl<ThirdMsgDao, ThirdMsg> implements ThirdMsgService {
}
