package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知视图
 */
@Data
public class NotificationVO implements Serializable {

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
