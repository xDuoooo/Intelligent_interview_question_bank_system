package com.xduo.springbootinit.model.dto.userquestionnote;

import com.xduo.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户题目笔记分页请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQuestionNoteQueryRequest extends PageRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}
