package com.zbkj.admin.task;

import com.zbkj.service.service.JustuitanErpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 聚水潭库存同步定时任务
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
@Component("JstInventoryTask")
public class JstInventoryTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(JstInventoryTask.class);
    
    @Autowired
    private JustuitanErpService justuitanErpService;
    
    /**
     * 同步聚水潭库存信息到数据库
     */
    public void syncInventoryFromJst() {
        logger.info("开始同步聚水潭库存信息到数据库");
        try {
            Boolean result = justuitanErpService.syncInventoryFromJst();
            if (result) {
                logger.info("同步聚水潭库存信息到数据库完成");
            } else {
                logger.error("同步聚水潭库存信息到数据库失败");
            }
        } catch (Exception e) {
            logger.error("同步聚水潭库存信息到数据库异常", e);
        }
    }
}