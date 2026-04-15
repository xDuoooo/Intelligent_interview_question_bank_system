package com.xduo.springbootinit.model.dto.wxmp;

import lombok.Data;

import java.io.Serializable;

/**
 * 公众号验证码登录请求
 */
@Data
public class WxMpCodeLoginRequest implements Serializable {

    /**
     * 公众号对话里收到的 6 位验证码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
