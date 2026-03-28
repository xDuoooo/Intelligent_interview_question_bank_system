package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 榜单用户项
 */
@Data
public class LeaderboardUserVO implements Serializable {

    private Long userId;

    private String userName;

    private String userAvatar;

    private String userRole;

    private Long rank;

    private Long metricValue;

    private String metricText;

    private static final long serialVersionUID = 1L;
}
