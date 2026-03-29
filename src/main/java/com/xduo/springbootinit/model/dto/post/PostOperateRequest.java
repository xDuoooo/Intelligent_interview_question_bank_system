package com.xduo.springbootinit.model.dto.post;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子运营操作请求
 */
@Data
public class PostOperateRequest implements Serializable {

    private Long id;

    private Integer isTop;

    private Integer isFeatured;

    private static final long serialVersionUID = 1L;
}
