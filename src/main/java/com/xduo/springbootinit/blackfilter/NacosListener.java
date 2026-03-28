package com.xduo.springbootinit.blackfilter;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.Executor;

@Slf4j
@Component
@ConditionalOnBean(NacosConfigManager.class)
@ConditionalOnProperty(prefix = "spring.cloud.nacos.config", name = "enabled", havingValue = "true")
public class NacosListener implements InitializingBean {

    @Resource
    private NacosConfigManager nacosConfigManager;

    @Value("${nacos.config.data-id}")
    private String dataId;

    @Value("${nacos.config.group}")
    private String group;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("nacos 监听器启动");

        try {
            ConfigService configService = nacosConfigManager.getConfigService();
            String config = configService.getConfigAndSignListener(dataId, group, 3000L, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                // 监听后续黑名单变化
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("监听到配置信息变化：{}", configInfo);
                    try {
                        BlackIpUtils.rebuildBlackIp(configInfo);
                    } catch (Exception e) {
                        log.error("更新黑名单失败", e);
                    }
                }
            });
            // 初始化黑名单
            BlackIpUtils.rebuildBlackIp(config);
        } catch (Exception e) {
            log.error("nacos 监听器初始化失败", e);
        }
    }
}
