package com.xduo.springbootinit.model.dto.systemconfig;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置更新请求
 */
@Data
public class SystemConfigUpdateRequest implements Serializable {

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * SEO 关键词
     */
    private String seoKeywords;

    /**
     * 系统公告
     */
    private String announcement;

    /**
     * 是否开放注册
     */
    private Boolean allowRegister;

    /**
     * 是否强制图形验证码
     */
    private Boolean requireCaptcha;

    /**
     * 是否开启维护模式
     */
    private Boolean maintenanceMode;

    private static final long serialVersionUID = 1L;
}
