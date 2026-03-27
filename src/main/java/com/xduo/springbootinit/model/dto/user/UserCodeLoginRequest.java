package com.xduo.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户验证码登录请求
 */
@Data
public class UserCodeLoginRequest implements Serializable {

    /**
     * 目标（手机号或邮箱）
     */
    private String target;

    /**
     * 验证码
     */
    private String code;

    /**
     * 类型 (1-邮箱, 2-手机)
     */
    private Integer type;

    private static final long serialVersionUID = 1L;
}
