package com.xduo.springbootinit.constant;

import java.util.Set;

/**
 * 帖子相关常量
 */
public interface PostConstant {

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

    Set<Integer> ALLOWED_REVIEW_STATUS_SET = Set.of(
            REVIEW_STATUS_PENDING,
            REVIEW_STATUS_APPROVED,
            REVIEW_STATUS_REJECTED
    );

    Set<Integer> ALLOWED_ADMIN_REVIEW_STATUS_SET = Set.of(
            REVIEW_STATUS_APPROVED,
            REVIEW_STATUS_REJECTED
    );
}
