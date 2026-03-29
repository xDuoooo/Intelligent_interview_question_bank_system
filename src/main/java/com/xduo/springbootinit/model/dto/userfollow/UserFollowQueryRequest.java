package com.xduo.springbootinit.model.dto.userfollow;

import com.xduo.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户关注关系列表查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFollowQueryRequest extends PageRequest implements Serializable {

    /**
     * 查询目标用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
