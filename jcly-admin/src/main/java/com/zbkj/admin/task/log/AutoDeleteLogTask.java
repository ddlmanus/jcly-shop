package com.zbkj.admin.task.log;

import com.zbkj.admin.service.ScheduleJobLogService;
import com.zbkj.common.utils.CrmebDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 自动删除不需要的历史日志
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
@Component("AutoDeleteLogTask")
public class AutoDeleteLogTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(AutoDeleteLogTask.class);

    @Autowired
    private ScheduleJobLogService scheduleJobLogService;

    /**
     * 每天0点执行
     */
    public void autoDeleteLog() {
        // cron : 0 0 0 */1 * ?
        logger.info("---AutoDeleteLogTask------autoDeleteLog task: Execution Time - {}", CrmebDateUtil.nowDateTime());
        try {
            scheduleJobLogService.autoDeleteLog();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("AutoDeleteLogTask" + " | msg : " + e.getMessage());
        }
    }

}
