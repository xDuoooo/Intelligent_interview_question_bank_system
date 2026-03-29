package com.xduo.springbootinit.model.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子评论提交结果
 */
@Data
public class PostCommentSubmitResultVO implements Serializable {

    private Long id;

    /**
     * 0正常 1待审核 2已驳回
     */
    private Integer status;

    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
