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
     * 手机验证码会话 OutId Redis Key 前缀
     */
    String USER_PHONE_VERIFY_OUT_ID_KEY_PREFIX = "user:phone:verify:out-id:";

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
     * 密码登录失败次数 Redis Key 前缀
     */
    String USER_PASSWORD_LOGIN_FAIL_KEY_PREFIX = "user:password:login:fail:";

    /**
     * 密码登录临时封禁 Redis Key 前缀
     */
    String USER_PASSWORD_LOGIN_BLOCK_KEY_PREFIX = "user:password:login:block:";

    /**
     * 系统配置缓存 Redis Key
     */
    String SYSTEM_CONFIG_CACHE_KEY = "system:config:current";

    /**
     * 微信公众号网页登录票据 Redis Key 前缀
     */
    String WX_MP_LOGIN_TICKET_KEY_PREFIX = "wxmp:login:ticket:";

    /**
     * 获取用户登录验证码 Redis Key
     */
    static String getUserLoginCodeRedisKey(String target) {
        return USER_LOGIN_CODE_KEY_PREFIX + target;
    }

    /**
     * 获取手机号验证码会话 OutId Redis Key
     */
    static String getUserPhoneVerifyOutIdRedisKey(String phone) {
        return USER_PHONE_VERIFY_OUT_ID_KEY_PREFIX + phone;
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

    /**
     * 获取密码登录失败次数 Redis Key
     */
    static String getUserPasswordLoginFailRedisKey(String identifier) {
        return USER_PASSWORD_LOGIN_FAIL_KEY_PREFIX + identifier;
    }

    /**
     * 获取密码登录临时封禁 Redis Key
     */
    static String getUserPasswordLoginBlockRedisKey(String identifier) {
        return USER_PASSWORD_LOGIN_BLOCK_KEY_PREFIX + identifier;
    }

    /**
     * 获取系统配置缓存 Redis Key
     */
    static String getSystemConfigCacheKey() {
        return SYSTEM_CONFIG_CACHE_KEY;
    }

    /**
     * 获取微信公众号网页登录票据 Redis Key
     */
    static String getWxMpLoginTicketRedisKey(String ticket) {
        return WX_MP_LOGIN_TICKET_KEY_PREFIX + ticket;
    }
}
