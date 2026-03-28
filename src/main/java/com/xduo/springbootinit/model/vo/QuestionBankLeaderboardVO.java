package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题库榜单
 */
@Data
public class QuestionBankLeaderboardVO implements Serializable {

    private Long questionBankId;

    private String questionBankTitle;

    private String description;

    private String metricLabel;

    private List<LeaderboardUserVO> rankingList;

    private LeaderboardUserVO currentUserItem;

    private static final long serialVersionUID = 1L;
}
