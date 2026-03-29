package com.xduo.springbootinit.model.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * 评论提交结果
 */
@Data
public class CommentSubmitResultVO implements Serializable {

    private Long id;

    /**
     * 0正常 1待审核 2已驳回/隐藏
     */
    private Integer status;

    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
