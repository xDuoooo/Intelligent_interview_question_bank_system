package com.xduo.springbootinit.model.dto.userfollow;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户关注请求
 */
@Data
public class UserFollowRequest implements Serializable {

    /**
     * 目标用户 id
     */
    private Long followUserId;

    private static final long serialVersionUID = 1L;
}
