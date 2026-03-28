package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 安全告警
 */
@TableName(value = "security_alert")
@Data
public class SecurityAlert implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户 id
     */
    private Long userId;

    /**
     * 关联用户名
     */
    private String userName;

    /**
     * 告警类型
     */
    private String alertType;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 告警原因
     */
    private String reason;

    /**
     * 告警详情
     */
    private String detail;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 状态：0-待处理 1-已处理 2-已忽略
     */
    private Integer status;

    /**
     * 处理人 id
     */
    private Long handlerUserId;

    /**
     * 处理动作
     */
    private String handleAction;

    /**
     * 处理时间
     */
    private Date handleTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
