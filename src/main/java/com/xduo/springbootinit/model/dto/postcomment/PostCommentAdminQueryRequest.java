package com.xduo.springbootinit.model.dto.postcomment;

import com.xduo.springbootinit.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台帖子评论审核分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostCommentAdminQueryRequest extends PageRequest implements Serializable {

    private Long postId;

    private Long userId;

    private Integer status;

    private String content;

    private static final long serialVersionUID = 1L;
}
