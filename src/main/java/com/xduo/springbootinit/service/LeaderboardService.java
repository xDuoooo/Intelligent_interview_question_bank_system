package com.xduo.springbootinit.service;

import com.xduo.springbootinit.model.vo.GlobalLeaderboardVO;
import com.xduo.springbootinit.model.vo.QuestionBankLeaderboardVO;

/**
 * 榜单服务
 */
public interface LeaderboardService {

    /**
     * 获取全站榜单
     */
    GlobalLeaderboardVO getGlobalLeaderboard(Long loginUserId);

    /**
     * 获取题库榜单
     */
    QuestionBankLeaderboardVO getQuestionBankLeaderboard(long questionBankId, Long loginUserId);
}
