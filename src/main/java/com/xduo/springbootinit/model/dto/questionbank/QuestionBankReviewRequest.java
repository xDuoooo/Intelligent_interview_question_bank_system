package com.xduo.springbootinit.model.dto.questionbank;

import lombok.Data;

import java.io.Serializable;

/**
 * 题库审核请求
 */
@Data
public class QuestionBankReviewRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long id;

    /**
     * 审核状态：1-通过 2-驳回
     */
    private Integer reviewStatus;

    /**
     * 审核意见
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
