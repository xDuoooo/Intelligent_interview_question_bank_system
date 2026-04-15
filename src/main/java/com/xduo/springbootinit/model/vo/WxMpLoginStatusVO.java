package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 公众号验证码登录状态
 */
@Data
public class WxMpLoginStatusVO implements Serializable {

    /**
     * 当前状态：pending / code_sent / used / expired
     */
    private String status;

    /**
     * 是否已经向公众号回发验证码
     */
    private Boolean codeSent;

    /**
     * 状态提示文案
     */
    private String message;

    /**
     * 票据过期时间（毫秒）
     */
    private Long expireAt;

    private static final long serialVersionUID = 1L;
}
