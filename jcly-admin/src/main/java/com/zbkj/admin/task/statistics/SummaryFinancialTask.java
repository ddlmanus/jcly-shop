package com.zbkj.admin.task.statistics;

import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.service.service.StatisticsTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 财务流水定时任务（每天一点）
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
@Component("SummaryFinancialTask")
public class SummaryFinancialTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(SummaryFinancialTask.class);

    @Autowired
    private StatisticsTaskService statisticsTaskService;

    /**
     * 每日财务流水定时任务
     * 每天1点执行
     */
    public void dailyStatement() {
        // cron : 0 0 1 * * ?
        logger.info("---SummaryFinancialTask dailyStatement------produce Data with fixed rate task: Start Time - {}", CrmebDateUtil.nowDateTime());
        try {
            statisticsTaskService.dailySummaryFinancialStatement();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("DailyStatementTask.dailyStatement" + " | msg : " + e.getMessage());
        }
        logger.info("---SummaryFinancialTask dailyStatement------produce Data with fixed rate task: End Time - {}", CrmebDateUtil.nowDateTime());
    }

}
