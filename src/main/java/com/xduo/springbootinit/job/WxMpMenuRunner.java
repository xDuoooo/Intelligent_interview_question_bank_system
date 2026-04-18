package com.xduo.springbootinit.job;

import com.xduo.springbootinit.service.WxMpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 微信公众号菜单自动同步任务
 * 在服务启动成功后执行一次
 */
@Component
@Slf4j
public class WxMpMenuRunner implements CommandLineRunner {

    @Resource
    private WxMpService wxMpService;

    @Override
    public void run(String... args) {
        try {
            // 给系统一点缓冲时间，确保网络和配置完全就绪
            Thread.sleep(5000);
            wxMpService.syncMenu();
        } catch (Exception e) {
            log.error("微信菜单自启动同步失败", e);
        }
    }
}
