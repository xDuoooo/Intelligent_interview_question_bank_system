package com.xduo.springbootinit.model.dto.questionfavour;

import lombok.Data;
import java.io.Serializable;

/**
 * 题目收藏请求
 */
@Data
public class QuestionFavourAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}
