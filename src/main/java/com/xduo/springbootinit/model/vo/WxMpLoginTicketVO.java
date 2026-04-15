package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 公众号验证码登录票据信息
 */
@Data
public class WxMpLoginTicketVO implements Serializable {

    /**
     * 登录口令
     */
    private String ticket;

    /**
     * 发给公众号的完整关键字
     */
    private String keyword;

    /**
     * 公众号名称
     */
    private String accountName;

    /**
     * 公众号二维码图片地址
     */
    private String qrImageUrl;

    /**
     * 过期时间戳（毫秒）
     */
    private Long expireAt;

    private static final long serialVersionUID = 1L;
}
