package com.xduo.springbootinit.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 发表/回复评论请求
 */
@Data
public class CommentAddRequest implements Serializable {

    /** 题目 id */
    private Long questionId;

    /** 父评论 id（null 表示顶级评论） */
    private Long parentId;

    /** 回复的具体评论 id（用于 @ 提及） */
    private Long replyToId;

    /** 内容（最长 2000 字） */
    private String content;

    private static final long serialVersionUID = 1L;
}
