package com.xduo.springbootinit.model.dto.questionbank;

import lombok.Data;

import java.io.Serializable;

/**
 * 题库提交审核请求
 */
@Data
public class QuestionBankSubmitReviewRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
