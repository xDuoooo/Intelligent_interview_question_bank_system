package com.xduo.springbootinit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth 登录配置 (GitHub / Gitee)
 *
 * @author xDuoooo
 */
@Configuration
@ConfigurationProperties(prefix = "social")
@Data
public class OAuthConfig {

    private OAuthClientConfig github;
    private OAuthClientConfig gitee;
    private OAuthClientConfig google;

    @Data
    public static class OAuthClientConfig {
        /**
         * Client ID
         */
        private String id;

        /**
         * Client Secret
         */
        private String secret;

        /**
         * Callback Redirect URI
         */
        private String redirectUri;
    }
}
