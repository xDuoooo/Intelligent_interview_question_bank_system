package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 评论举报
 */
@Data
@TableName(value = "question_comment_report")
public class QuestionCommentReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被举报的评论 id */
    private Long commentId;

    /** 举报者 id */
    private Long userId;

    /** 举报原因 */
    private String reason;

    /** 处理状态：0待处理 1已驳回 2已删除 */
    private Integer status;

    private Date createTime;
}
