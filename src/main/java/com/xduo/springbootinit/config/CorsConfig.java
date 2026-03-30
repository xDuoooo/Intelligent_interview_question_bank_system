package com.xduo.springbootinit.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * 全局跨域配置

 */
@Configuration
@Slf4j
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin-patterns:}")
    private String allowedOriginPatterns;

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> originPatterns = resolveAllowedOriginPatterns();
        var registration = registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
        if (originPatterns.isEmpty()) {
            log.warn("未配置可用跨域来源，当前环境不会放行任何带凭证的跨域请求");
            return;
        }
        registration.allowedOriginPatterns(originPatterns.toArray(new String[0]));
    }

    private List<String> resolveAllowedOriginPatterns() {
        String rawPatterns = StringUtils.isNotBlank(allowedOriginPatterns) ? allowedOriginPatterns : frontendUrl;
        if (StringUtils.isBlank(rawPatterns)) {
            return List.of();
        }
        return Arrays.stream(rawPatterns.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }
}
