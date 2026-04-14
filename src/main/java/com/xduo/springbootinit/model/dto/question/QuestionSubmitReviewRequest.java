package com.xduo.springbootinit.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目提交审核请求
 */
@Data
public class QuestionSubmitReviewRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
