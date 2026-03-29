package com.xduo.springbootinit.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * 单题 AI 判题请求
 */
@Data
public class QuestionAnswerEvaluateRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 用户回答
     */
    private String answerContent;

    private static final long serialVersionUID = 1L;
}
