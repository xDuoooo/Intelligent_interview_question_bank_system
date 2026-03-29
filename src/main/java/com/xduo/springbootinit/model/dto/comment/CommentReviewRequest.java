package com.xduo.springbootinit.model.dto.comment;

import java.io.Serializable;
import lombok.Data;

/**
 * 评论审核请求
 */
@Data
public class CommentReviewRequest implements Serializable {

    private Long id;

    /**
     * 0通过 2驳回
     */
    private Integer status;

    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
