package com.xduo.springbootinit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * IP 维度接口限流注解。
 * <p>
 * 基于 Redis 滑动窗口，限制同一 IP 在指定时间窗口内的请求次数。
 * 超限后抛出 BusinessException，返回 "请求过于频繁" 提示。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流 key 前缀（默认使用方法签名自动生成）
     */
    String key() default "";

    /**
     * 时间窗口内允许的最大请求次数
     */
    int maxRequests() default 60;

    /**
     * 时间窗口大小（秒）
     */
    int windowSeconds() default 60;

    /**
     * 被限流时的提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
}
