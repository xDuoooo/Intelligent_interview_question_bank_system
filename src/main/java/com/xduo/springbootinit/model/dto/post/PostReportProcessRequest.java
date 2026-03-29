package com.xduo.springbootinit.model.dto.post;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子举报处理请求
 */
@Data
public class PostReportProcessRequest implements Serializable {

    /**
     * 举报记录 id
     */
    private Long id;

    /**
     * 处理状态：1已驳回 2已采纳
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
