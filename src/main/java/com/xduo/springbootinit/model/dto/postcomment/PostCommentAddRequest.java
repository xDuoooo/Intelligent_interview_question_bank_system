package com.xduo.springbootinit.model.dto.postcomment;

import java.io.Serializable;
import lombok.Data;

/**
 * 发表 / 回复帖子评论请求
 */
@Data
public class PostCommentAddRequest implements Serializable {

    private Long postId;

    private Long parentId;

    private Long replyToId;

    private String content;

    private static final long serialVersionUID = 1L;
}
