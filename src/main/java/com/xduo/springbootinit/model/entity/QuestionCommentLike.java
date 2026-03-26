package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 评论点赞
 */
@Data
@TableName(value = "question_comment_like")
public class QuestionCommentLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论 id */
    private Long commentId;

    /** 点赞用户 id */
    private Long userId;

    private Date createTime;
}
