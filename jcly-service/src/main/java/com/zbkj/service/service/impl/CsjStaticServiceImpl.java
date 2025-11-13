package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.csj.CsjStatic;
import com.zbkj.service.dao.CsjStaticDao;
import com.zbkj.service.service.CsjStaticService;
import org.springframework.stereotype.Service;

@Service
public class CsjStaticServiceImpl extends ServiceImpl<CsjStaticDao, CsjStatic>implements CsjStaticService {
}
