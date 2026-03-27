package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户刷题记录视图
 */
@Data
public class UserQuestionHistoryVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 作答状态：0-浏览, 1-掌握, 2-困难
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 关联的题目信息
     */
    private QuestionVO question;

    private static final long serialVersionUID = 1L;
}
