package com.xduo.springbootinit.model.dto.comment;

import com.xduo.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 评论足迹分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentActivityQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
}
