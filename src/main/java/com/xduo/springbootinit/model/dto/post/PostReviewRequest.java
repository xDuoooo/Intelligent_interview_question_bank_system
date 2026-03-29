package com.xduo.springbootinit.model.dto.post;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子审核请求
 */
@Data
public class PostReviewRequest implements Serializable {

    private Long id;

    private Integer reviewStatus;

    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
