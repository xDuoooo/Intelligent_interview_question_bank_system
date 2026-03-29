package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 公开用户主页视图
 */
@Data
public class UserProfileVO implements Serializable {

    /**
     * 用户公开信息
     */
    private UserVO user;

    /**
     * 累计刷题数
     */
    private Long totalQuestionCount;

    /**
     * 已掌握题目数
     */
    private Long masteredQuestionCount;

    /**
     * 活跃天数
     */
    private Long activeDays;

    /**
     * 连续学习天数
     */
    private Long currentStreak;

    /**
     * 已通过审核的投稿题目数
     */
    private Long approvedQuestionCount;

    private static final long serialVersionUID = 1L;
}
