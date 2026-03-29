package com.xduo.springbootinit.model.dto.post;

import com.xduo.springbootinit.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子举报查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostReportQueryRequest extends PageRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 处理状态：0待处理 1已驳回 2已采纳
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
