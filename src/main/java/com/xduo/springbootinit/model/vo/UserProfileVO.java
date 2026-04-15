package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

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
     * 已通过审核的公开题目数
     */
    private Long approvedQuestionCount;

    /**
     * 已通过审核的公开题库数
     */
    private Long approvedQuestionBankCount;

    /**
     * 粉丝数
     */
    private Long followerCount;

    /**
     * 关注数
     */
    private Long followingCount;

    /**
     * 当前登录用户是否已关注该用户
     */
    private Boolean hasFollowed;

    /**
     * 最近动态
     */
    private List<UserActivityVO> recentActivityList;

    private static final long serialVersionUID = 1L;
}
