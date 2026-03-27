package com.xduo.springbootinit.model.dto.notification;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建通知请求
 */
@Data
public class NotificationAddRequest implements Serializable {

    /**
     * 获知通知的用户 id
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
     * 类型：system, user, post, etc.
     */
    private String type;

    /**
     * 关联业务 ID
     */
    private Long targetId;

    private static final long serialVersionUID = 1L;
}
