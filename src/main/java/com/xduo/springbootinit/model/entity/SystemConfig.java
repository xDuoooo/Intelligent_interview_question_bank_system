package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统配置
 */
@TableName(value = "system_config")
@Data
public class SystemConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

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
    private Integer allowRegister;

    /**
     * 是否强制图形验证码
     */
    private Integer requireCaptcha;

    /**
     * 是否开启维护模式
     */
    private Integer maintenanceMode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
