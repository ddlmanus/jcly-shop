package com.zbkj.admin.test;

import com.zbkj.service.service.CaiShiJiaPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 采食家数据同步测试类
 */
@Slf4j
public class CsjDataSyncTest {

    @Autowired
    private CaiShiJiaPlatformService caiShiJiaPlatformService;

    public void testSyncData() {
        try {
            log.info("开始测试采食家数据同步...");
            
            // 测试登录
            String account = "test_account";
            String password = "test_password";
            String token = caiShiJiaPlatformService.loginToPlatform(account, password);
            log.info("登录测试成功，获取token: {}", token);
            
            // 测试数据同步
            boolean result = caiShiJiaPlatformService.syncAndSaveData();
            log.info("数据同步测试结果: {}", result);
            
        } catch (Exception e) {
            log.error("测试失败", e);
        }
    }
}