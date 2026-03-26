package com.xduo.springbootinit.model.dto.comment;

import com.xduo.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分页查询评论请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentQueryRequest extends PageRequest implements Serializable {

    /** 题目 id */
    private Long questionId;

    /**
     * 排序字段：createTime（最新） | likeNum（最热）
     * 默认 createTime
     */
    private String sortField;

    /**
     * 排序方式：descend / ascend
     * 默认 descend
     */
    private String sortOrder;

    private static final long serialVersionUID = 1L;
}
