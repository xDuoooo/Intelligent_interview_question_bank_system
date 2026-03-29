package com.xduo.springbootinit.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子举报视图
 */
@Data
public class PostReportVO implements Serializable {

    private Long id;

    private Long postId;

    private Long userId;

    private String reason;

    /**
     * 处理状态：0待处理 1已驳回 2已采纳
     */
    private Integer status;

    private Date createTime;

    private UserVO reporter;

    private static final long serialVersionUID = 1L;
}
