package com.xduo.springbootinit.model.dto.notification;

import com.xduo.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询通知请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NotificationQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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
     * 状态：0-未读, 1-已读
     */
    private Integer status;

    /**
     * 关联业务 ID
     */
    private Long targetId;

    private static final long serialVersionUID = 1L;
}
