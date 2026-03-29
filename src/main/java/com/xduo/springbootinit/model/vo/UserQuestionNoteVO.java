package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户题目私有笔记视图
 */
@Data
public class UserQuestionNoteVO implements Serializable {

    private Long id;

    private Long userId;

    private Long questionId;

    private String content;

    private Date createTime;

    private Date updateTime;

    private QuestionVO question;

    private static final long serialVersionUID = 1L;
}
