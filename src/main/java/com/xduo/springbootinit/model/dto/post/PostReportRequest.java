package com.xduo.springbootinit.model.dto.post;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子举报请求
 */
@Data
public class PostReportRequest implements Serializable {

    private Long postId;

    private String reason;

    private static final long serialVersionUID = 1L;
}
