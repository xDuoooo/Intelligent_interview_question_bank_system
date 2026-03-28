package com.xduo.springbootinit.controller;

import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.GlobalLeaderboardVO;
import com.xduo.springbootinit.model.vo.QuestionBankLeaderboardVO;
import com.xduo.springbootinit.service.LeaderboardService;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 榜单接口
 */
@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    @Resource
    private LeaderboardService leaderboardService;

    @Resource
    private UserService userService;

    @GetMapping("/global")
    public BaseResponse<GlobalLeaderboardVO> getGlobalLeaderboard(HttpServletRequest request) {
        User loginUser = userService.getLoginUserPermitNull(request);
        return ResultUtils.success(leaderboardService.getGlobalLeaderboard(loginUser == null ? null : loginUser.getId()));
    }

    @GetMapping("/bank")
    public BaseResponse<QuestionBankLeaderboardVO> getQuestionBankLeaderboard(Long questionBankId,
                                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        return ResultUtils.success(leaderboardService.getQuestionBankLeaderboard(
                questionBankId,
                loginUser == null ? null : loginUser.getId()
        ));
    }
}
