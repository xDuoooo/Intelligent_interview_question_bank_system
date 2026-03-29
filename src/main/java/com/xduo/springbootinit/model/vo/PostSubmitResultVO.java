package com.xduo.springbootinit.model.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子提交结果
 */
@Data
public class PostSubmitResultVO implements Serializable {

    /**
     * 帖子 id
     */
    private Long id;

    /**
     * 审核状态：0-待审核 1-已通过 2-已驳回
     */
    private Integer reviewStatus;

    /**
     * 审核说明
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
