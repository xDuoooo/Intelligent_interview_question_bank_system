package com.xduo.springbootinit.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 题目生成请求
 */
@Data
public class QuestionAIGenerateRequest implements Serializable {

    /**
     * 题目方向
     */
    private String questionType;

    /**
     * 题目数量
     */
    private Integer number;

    private static final long serialVersionUID = 1L;
}
