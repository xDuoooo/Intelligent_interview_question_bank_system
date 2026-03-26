package com.xduo.springbootinit.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 举报评论请求
 */
@Data
public class CommentReportRequest implements Serializable {

    /** 被举报的评论 id */
    private Long commentId;

    /** 举报原因（广告、辱骂、无关内容等） */
    private String reason;

    private static final long serialVersionUID = 1L;
}
