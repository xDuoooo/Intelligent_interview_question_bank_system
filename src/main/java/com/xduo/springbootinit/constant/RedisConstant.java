package com.xduo.springbootinit.constant;

public interface RedisConstant {

    /**
     * 用户签到记录的 Redis Key 前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "user:signins";

    /**
     * 获取用户签到记录的 Redis Key
     * @param year 年份
     * @param userId 用户 id
     * @return 拼接好的 Redis Key
     */
    static String getUserSignInRedisKey(int year, long userId) {
        return String.format("%s:%s:%s", USER_SIGN_IN_REDIS_KEY_PREFIX, year, userId);
    }

    /**
     * 用户登录验证码 Redis Key 前缀
     */
    String USER_LOGIN_CODE_KEY_PREFIX = "user:login:code:";

    /**
     * 用户验证码发送频率限制 Redis Key 前缀 (60s)
     */
    String USER_CODE_SEND_LIMIT_KEY_PREFIX = "user:login:limit:";

    /**
     * 图形验证码 Redis Key 前缀
     */
    String USER_CAPTCHA_KEY_PREFIX = "user:captcha:";

    /**
     * IP 发送次数限制 Redis Key 前缀 (每日)
     */
    String USER_IP_LIMIT_KEY_PREFIX = "user:ip:limit:";

    /**
     * 手机号发送次数限制 Redis Key 前缀 (每日)
     */
    String USER_PHONE_LIMIT_KEY_PREFIX = "user:phone:limit:";

    /**
     * 获取用户登录验证码 Redis Key
     */
    static String getUserLoginCodeRedisKey(String target) {
        return USER_LOGIN_CODE_KEY_PREFIX + target;
    }

    /**
     * 获取用户验证码发送限制 Redis Key
     */
    static String getUserCodeSendLimitRedisKey(String target) {
        return USER_CODE_SEND_LIMIT_KEY_PREFIX + target;
    }

    /**
     * 获取图形验证码 Redis Key
     */
    static String getUserCaptchaRedisKey(String uuid) {
        return USER_CAPTCHA_KEY_PREFIX + uuid;
    }

    /**
     * 获取 IP 限制 Redis Key
     */
    static String getUserIpLimitRedisKey(String ip) {
        return USER_IP_LIMIT_KEY_PREFIX + ip;
    }

    /**
     * 获取手机号限制 Redis Key
     */
    static String getUserPhoneLimitRedisKey(String phone) {
        return USER_PHONE_LIMIT_KEY_PREFIX + phone;
    }
}
