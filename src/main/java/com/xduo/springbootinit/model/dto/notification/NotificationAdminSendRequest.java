package com.xduo.springbootinit.model.dto.notification;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员发送通知请求
 */
@Data
public class NotificationAdminSendRequest implements Serializable {

    /**
     * 发送范围：single / user / all
     */
    private String scope;

    /**
     * 单用户发送时的目标用户 id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 关联业务 id
     */
    private Long targetId;

    private static final long serialVersionUID = 1L;
}
