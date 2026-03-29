package com.xduo.springbootinit.model.dto.userquestionhistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 学习时长会话上报请求
 */
@Data
public class UserQuestionStudySessionReportRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 学习时长（秒）
     */
    private Integer durationSeconds;

    private static final long serialVersionUID = 1L;
}
