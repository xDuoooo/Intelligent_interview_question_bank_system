package com.xduo.springbootinit.model.dto.comment;

import com.xduo.springbootinit.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论后台查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentAdminQueryRequest extends PageRequest implements Serializable {

    private Long questionId;

    private Long userId;

    private String content;

    /**
     * 0正常 1待审核 2已驳回/隐藏
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
