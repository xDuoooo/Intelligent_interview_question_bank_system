package com.xduo.springbootinit.constant;

import java.util.Set;

/**
 * 题目相关常量
 */
public interface QuestionConstant {

    /**
     * 待审核
     */
    int REVIEW_STATUS_PENDING = 0;

    /**
     * 已通过
     */
    int REVIEW_STATUS_APPROVED = 1;

    /**
     * 已驳回
     */
    int REVIEW_STATUS_REJECTED = 2;

    /**
     * 允许的审核状态集合
     */
    Set<Integer> ALLOWED_REVIEW_STATUS_SET = Set.of(
            REVIEW_STATUS_PENDING,
            REVIEW_STATUS_APPROVED,
            REVIEW_STATUS_REJECTED
    );

    /**
     * 管理员审核时允许设置的状态集合
     */
    Set<Integer> ALLOWED_ADMIN_REVIEW_STATUS_SET = Set.of(
            REVIEW_STATUS_APPROVED,
            REVIEW_STATUS_REJECTED
    );
}
