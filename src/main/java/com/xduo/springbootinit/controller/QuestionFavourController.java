package com.xduo.springbootinit.controller;

import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.model.dto.questionfavour.QuestionFavourAddRequest;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.service.QuestionFavourService;
import com.xduo.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 题目收藏接口
 */
@RestController
@RequestMapping("/question_favour")
@Slf4j
public class QuestionFavourController {

    @Resource
    private QuestionFavourService questionFavourService;

    @Resource
    private UserService userService;

    /**
     * 收藏 / 取消收藏
     *
     * @param questionFavourAddRequest
     * @param request
     * @return resultNum 收藏变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doQuestionFavour(@RequestBody QuestionFavourAddRequest questionFavourAddRequest,
                                                 HttpServletRequest request) {
        if (questionFavourAddRequest == null || questionFavourAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        long questionId = questionFavourAddRequest.getQuestionId();
        int result = questionFavourService.doQuestionFavour(questionId, loginUser);
        return ResultUtils.success(result);
    }
}
