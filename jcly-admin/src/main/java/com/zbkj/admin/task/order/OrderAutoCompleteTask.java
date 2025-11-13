package com.zbkj.admin.task.order;

import cn.hutool.core.date.DateUtil;
import com.zbkj.service.service.OrderTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单自动完成Task
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
@Component("OrderAutoCompleteTask")
public class OrderAutoCompleteTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCompleteTask.class);

    @Autowired
    private OrderTaskService orderTaskService;

    /**
     * 每小时同步一次数据
     */
    public void autoComplete() {
        // cron : 0 0 */1 * * ?
        logger.info("---OrderAutoCompleteTask task------produce Data with fixed rate task: Execution Time - {}", DateUtil.date());
        try {
            orderTaskService.autoComplete();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("OrderAutoCompleteTask.task" + " | msg : " + e.getMessage());
        }
    }

}
