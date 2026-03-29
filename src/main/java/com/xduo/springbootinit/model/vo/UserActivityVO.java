package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户公开动态视图
 */
@Data
public class UserActivityVO implements Serializable {

    /**
     * 动态类型
     */
    private String type;

    /**
     * 动态标题
     */
    private String title;

    /**
     * 动态描述
     */
    private String description;

    /**
     * 关联资源 id
     */
    private Long targetId;

    /**
     * 关联链接
     */
    private String targetUrl;

    /**
     * 展示标签
     */
    private String badge;

    /**
     * 动态时间
     */
    private Date activityTime;

    private static final long serialVersionUID = 1L;
}
