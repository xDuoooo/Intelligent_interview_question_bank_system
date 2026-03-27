package com.xduo.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户发送验证码请求
 */
@Data
public class UserSendCodeRequest implements Serializable {

    /**
     * 目标（手机号或邮箱）
     */
    private String target;

    /**
     * 类型 (1-邮箱, 2-手机)
     */
    private Integer type;

    /**
     * 图形验证码
     */
    private String captcha;

    /**
     * 图形验证码唯一标识
     */
    private String captchaUuid;

    private static final long serialVersionUID = 1L;
}
