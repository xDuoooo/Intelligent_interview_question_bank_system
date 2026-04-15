package com.xduo.springbootinit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信公众号验证码登录配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.mp")
public class WechatMpConfig {

    /**
     * 是否启用公众号验证码登录
     */
    private boolean enabled;

    /**
     * 微信公众号后台配置的开发者 Token
     */
    private String token;

    /**
     * 公众号名称，用于前端展示
     */
    private String accountName;

    /**
     * 公众号二维码图片地址
     */
    private String qrImageUrl;

    /**
     * 用户需要发送给公众号的关键字前缀
     */
    private String loginKeyword = "登录";

    /**
     * 登录口令有效期（秒）
     */
    private long ticketExpireSeconds = 600;

    /**
     * 登录验证码有效期（秒）
     */
    private long codeExpireSeconds = 300;
}
