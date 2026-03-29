package com.xduo.springbootinit.model.dto.userquestionnote;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存题目私有笔记请求
 */
@Data
public class UserQuestionNoteSaveRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 笔记内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
