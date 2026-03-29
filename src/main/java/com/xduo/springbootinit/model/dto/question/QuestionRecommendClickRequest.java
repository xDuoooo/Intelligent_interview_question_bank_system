package com.xduo.springbootinit.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * 记录推荐点击请求
 */
@Data
public class QuestionRecommendClickRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 推荐来源：personal / related / resume
     */
    private String source;

    private static final long serialVersionUID = 1L;
}
