package com.xduo.springbootinit.model.dto.userquestionhistory;

import lombok.Data;
import java.io.Serializable;

/**
 * 刷题记录添加请求
 */
@Data
public class UserQuestionHistoryAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 作答状态：0-浏览, 1-掌握, 2-困难
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
