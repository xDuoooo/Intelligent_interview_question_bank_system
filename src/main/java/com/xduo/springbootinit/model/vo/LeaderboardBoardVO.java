package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 榜单板块
 */
@Data
public class LeaderboardBoardVO implements Serializable {

    private String key;

    private String title;

    private String description;

    private String metricLabel;

    private List<LeaderboardUserVO> rankingList;

    private LeaderboardUserVO currentUserItem;

    private static final long serialVersionUID = 1L;
}
