package com.xduo.springbootinit.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * 简历驱动推荐请求
 */
@Data
public class QuestionResumeRecommendRequest implements Serializable {

    /**
     * 简历文本
     */
    private String resumeText;

    /**
     * 返回数量
     */
    private Integer size;

    private static final long serialVersionUID = 1L;
}
