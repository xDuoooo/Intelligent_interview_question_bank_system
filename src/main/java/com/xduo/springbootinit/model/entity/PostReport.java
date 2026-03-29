package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 帖子举报
 */
@Data
@TableName(value = "post_report")
public class PostReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    private String reason;

    /**
     * 处理状态：0待处理 1已驳回 2已采纳
     */
    private Integer status;

    private Date createTime;
}
