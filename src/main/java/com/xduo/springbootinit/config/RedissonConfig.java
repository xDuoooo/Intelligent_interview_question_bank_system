package com.xduo.springbootinit.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String host;

    private Integer port;

    private Integer database;

    private String username;

    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = StringUtils.startsWithAny(host, "redis://", "rediss://")
                ? host
                : "redis://" + host + ":" + port;
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(database == null ? 0 : database);
        if (StringUtils.isNotBlank(username)) {
            singleServerConfig.setUsername(username.trim());
        }
        if (StringUtils.isNotBlank(password)) {
            singleServerConfig.setPassword(password);
        }
        return Redisson.create(config);
    }
}
