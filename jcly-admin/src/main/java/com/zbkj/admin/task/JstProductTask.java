package com.zbkj.admin.task;

import com.zbkj.service.service.ProductManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("JstProductTask")
public class JstProductTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(JstProductTask.class);
     @Autowired
     private ProductManagerService productManagerService;
    /**
     * 同步聚水潭商品信息到数据库
     */

    public void fullSyncProductFromJst() {
        logger.info("开始同步聚水潭商品信息到数据库");
        productManagerService.fullSyncProductFromJst();
        logger.info("同步聚水潭商品信息到数据库完成");
    }
}
