package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 全站榜单
 */
@Data
public class GlobalLeaderboardVO implements Serializable {

    private List<LeaderboardBoardVO> boardList;

    private static final long serialVersionUID = 1L;
}
