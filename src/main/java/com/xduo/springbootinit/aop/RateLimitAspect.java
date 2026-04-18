package com.xduo.springbootinit.aop;

import com.xduo.springbootinit.annotation.RateLimit;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.utils.NetUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;
import java.util.Collections;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * IP 维度接口限流切面。
 * <p>
 * 使用 Redis 计数器 + 过期时间实现简单的固定窗口限流。
 * 同一 IP 在 windowSeconds 内对同一接口的请求次数超过 maxRequests 时，
 * 拒绝请求并返回友好提示。
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String LIMIT_LUA = "local current = redis.call('incr', KEYS[1])\n" +
            "if current == 1 then\n" +
            "    redis.call('expire', KEYS[1], ARGV[1])\n" +
            "end\n" +
            "return current;";

    @Around("@annotation(rateLimit)")
    public Object doRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 获取请求 IP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = NetUtils.getIpAddress(request);
        // IPv6 本地环回统一为 127.0.0.1
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        // 构建 Redis key
        String limitKey = buildRedisKey(joinPoint, rateLimit, ip);

        // 使用 Lua 脚本保证计数和设置过期的原子性
        RedisScript<Long> redisScript = new DefaultRedisScript<>(LIMIT_LUA, Long.class);
        Long currentCount = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(limitKey),
                String.valueOf(rateLimit.windowSeconds())
        );

        if (currentCount != null && currentCount > rateLimit.maxRequests()) {
            log.warn("IP 限流触发: ip={}, key={}, count={}, limit={}/{}s",
                    ip, limitKey, currentCount, rateLimit.maxRequests(), rateLimit.windowSeconds());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, rateLimit.message());
        }

        return joinPoint.proceed();
    }

    private String buildRedisKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit, String ip) {
        String keyPrefix = rateLimit.key();
        if (keyPrefix.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            keyPrefix = signature.getDeclaringTypeName() + ":" + signature.getMethod().getName();
        }
        return "rate_limit:" + keyPrefix + ":" + ip;
    }
}
