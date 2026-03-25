package com.xduo.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.annotation.AuthCheck;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.model.dto.questionbankquestion.QuestionBankQuestionAddRequest;
import com.xduo.springbootinit.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.xduo.springbootinit.model.dto.questionbankquestion.QuestionBankQuestionRemoveRequest;
import com.xduo.springbootinit.model.entity.QuestionBankQuestion;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.service.QuestionBankQuestionService;
import com.xduo.springbootinit.service.QuestionBankService;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题库题目关联接口
 */
@RestController
@RequestMapping("/question_bank_question")
@Slf4j
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    /**
     * 向题库添加题目（需登录）
     *
     * @param questionBankQuestionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionBankQuestion(
            @RequestBody QuestionBankQuestionAddRequest questionBankQuestionAddRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQuestionAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = questionBankQuestionAddRequest.getQuestionBankId();
        Long questionId = questionBankQuestionAddRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(questionId == null || questionId <= 0, ErrorCode.PARAMS_ERROR);
        // 判断题库是否存在
        ThrowUtils.throwIf(questionBankService.getById(questionBankId) == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        // 判断题目是否存在
        ThrowUtils.throwIf(questionService.getById(questionId) == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        questionBankQuestion.setQuestionBankId(questionBankId);
        questionBankQuestion.setQuestionId(questionId);
        questionBankQuestion.setUserId(loginUser.getId());
        boolean result = questionBankQuestionService.save(questionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(questionBankQuestion.getId());
    }

    /**
     * 从题库移除题目（仅管理员）
     *
     * @param questionBankQuestionRemoveRequest
     * @param request
     * @return
     */
    @PostMapping("/remove")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> removeQuestionBankQuestion(
            @RequestBody QuestionBankQuestionRemoveRequest questionBankQuestionRemoveRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQuestionRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = questionBankQuestionRemoveRequest.getQuestionBankId();
        Long questionId = questionBankQuestionRemoveRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(questionId == null || questionId <= 0, ErrorCode.PARAMS_ERROR);
        // 构造查询，硬删除
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
        lambdaQueryWrapper.eq(QuestionBankQuestion::getQuestionId, questionId);
        boolean result = questionBankQuestionService.remove(lambdaQueryWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取题库题目关联列表（仅管理员可用）
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankQuestion>> listQuestionBankQuestionByPage(
            @RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        ThrowUtils.throwIf(questionBankQuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<QuestionBankQuestion> page = questionBankQuestionService
                .listQuestionBankQuestionByPage(questionBankQuestionQueryRequest);
        return ResultUtils.success(page);
    }
}
