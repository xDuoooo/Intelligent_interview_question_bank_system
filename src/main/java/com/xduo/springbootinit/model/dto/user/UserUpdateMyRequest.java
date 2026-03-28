package com.xduo.springbootinit.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求

 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 所在城市
     */
    private String city;

    /**
     * GitHub 唯一标识
     */
    private String githubId;

    /**
     * Gitee 唯一标识
     */
    private String giteeId;

    /**
     * Google 唯一标识
     */
    private String googleId;

    private static final long serialVersionUID = 1L;
}
