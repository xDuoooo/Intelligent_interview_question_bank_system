package com.xduo.springbootinit.model.dto.postcomment;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子评论审核请求
 */
@Data
public class PostCommentReviewRequest implements Serializable {

    private Long id;

    /**
     * 0 通过 2 驳回
     */
    private Integer status;

    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
