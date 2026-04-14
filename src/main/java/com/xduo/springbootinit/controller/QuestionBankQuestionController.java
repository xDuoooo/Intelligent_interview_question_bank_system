package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.QuestionBank;
import com.xduo.springbootinit.model.dto.question.QuestionBatchDeleteRequest;
import com.xduo.springbootinit.model.dto.questionbankquestion.*;
import com.xduo.springbootinit.model.entity.QuestionBankQuestion;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.QuestionVO;
import com.xduo.springbootinit.model.vo.QuestionBankQuestionVO;
import com.xduo.springbootinit.service.QuestionBankQuestionService;
import com.xduo.springbootinit.service.QuestionBankService;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库题目关联接口
 */
@RestController
@RequestMapping("/questionBankQuestion")
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
        // 判断题目是否存在
        Question question = questionService.getById(questionId);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ensureCanManageQuestionBank(questionBankId, loginUser, request);
        ensureCanManageQuestion(question, loginUser, request);
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        questionBankQuestion.setQuestionBankId(questionBankId);
        questionBankQuestion.setQuestionId(questionId);
        questionBankQuestion.setUserId(loginUser.getId());
        boolean result = questionBankQuestionService.save(questionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(questionBankQuestion.getId());
    }

    /**
     * 获取题库中的题目详情（校验题目必须属于该题库）
     */
    @GetMapping("/get/question/vo")
    public BaseResponse<QuestionVO> getQuestionVOInBank(@RequestParam Long questionBankId,
                                                        @RequestParam Long questionId,
                                                        HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库非法");
        ThrowUtils.throwIf(questionId == null || questionId <= 0, ErrorCode.PARAMS_ERROR, "题目非法");
        User loginUser = userService.getLoginUserPermitNull(request);
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        ThrowUtils.throwIf(!questionBankService.canViewQuestionBank(questionBank, loginUser), ErrorCode.NOT_FOUND_ERROR);

        LambdaQueryWrapper<QuestionBankQuestion> relationQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .eq(QuestionBankQuestion::getQuestionId, questionId);
        QuestionBankQuestion relation = questionBankQuestionService.getOne(relationQueryWrapper, false);
        ThrowUtils.throwIf(relation == null, ErrorCode.NOT_FOUND_ERROR, "该题目不属于当前题库");

        Question question = questionService.getById(questionId);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        ThrowUtils.throwIf(!questionService.canViewQuestion(question, loginUser), ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取题库题目关联列表（仅管理员可用）
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankQuestion>> listQuestionBankQuestionByPage(
            @RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        ThrowUtils.throwIf(questionBankQuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<QuestionBankQuestion> page = questionBankQuestionService
                .listQuestionBankQuestionByPage(questionBankQuestionQueryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 分页获取题库题目关联列表（封装类，仅管理员）
     */
    @PostMapping("/list/page/vo")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankQuestionVO>> listQuestionBankQuestionVOByPage(
            @RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = questionBankQuestionQueryRequest.getCurrent();
        long size = questionBankQuestionQueryRequest.getPageSize();
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 100, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<QuestionBankQuestion> page = questionBankQuestionService.listQuestionBankQuestionByPage(questionBankQuestionQueryRequest);
        return ResultUtils.success(questionBankQuestionService.getQuestionBankQuestionVOPage(page, request));
    }

    /**
     * 分页获取当前用户可管理的题库题目关联列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listMyQuestionBankQuestionVOByPage(
            @RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "请选择题库");
        ensureCanManageQuestionBank(questionBankId, loginUser, request);
        long current = questionBankQuestionQueryRequest.getCurrent();
        long size = questionBankQuestionQueryRequest.getPageSize();
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 200, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<QuestionBankQuestion> page = questionBankQuestionService.listQuestionBankQuestionByPage(questionBankQuestionQueryRequest);
        return ResultUtils.success(questionBankQuestionService.getQuestionBankQuestionVOPage(page, request));
    }

    @PostMapping("/add/batch")
    public BaseResponse<Boolean> batchAddQuestionsToBank(
            @RequestBody QuestionBankQuestionBatchAddRequest questionBankQuestionBatchAddRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(questionBankQuestionBatchAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long questionBankId = questionBankQuestionBatchAddRequest.getQuestionBankId();
        List<Long> questionIdList = questionBankQuestionBatchAddRequest.getQuestionIdList();
        ensureCanManageQuestionBank(questionBankId, loginUser, request);
        ensureCanManageQuestionList(questionIdList, loginUser, request);
        questionBankQuestionService.batchAddQuestionsToBank(questionIdList, questionBankId, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/remove/batch")
    public BaseResponse<Boolean> batchRemoveQuestionsFromBank(
            @RequestBody QuestionBankQuestionBatchRemoveRequest questionBankQuestionBatchRemoveRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(questionBankQuestionBatchRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long questionBankId = questionBankQuestionBatchRemoveRequest.getQuestionBankId();
        List<Long> questionIdList = questionBankQuestionBatchRemoveRequest.getQuestionIdList();
        ensureCanManageQuestionBank(questionBankId, loginUser, request);
        questionBankQuestionService.batchRemoveQuestionsFromBank(questionIdList, questionBankId);
        return ResultUtils.success(true);
    }

    @PostMapping("/remove")
    public BaseResponse<Boolean> removeQuestionBankQuestion(
            @RequestBody QuestionBankQuestionRemoveRequest questionBankQuestionRemoveRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(questionBankQuestionRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long questionBankId = questionBankQuestionRemoveRequest.getQuestionBankId();
        Long questionId = questionBankQuestionRemoveRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null || questionId == null, ErrorCode.PARAMS_ERROR);
        ensureCanManageQuestionBank(questionBankId, loginUser, request);
        // 构造查询
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionId, questionId)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
        boolean result = questionBankQuestionService.remove(lambdaQueryWrapper);
        return ResultUtils.success(result);
    }
    @PostMapping("/delete/batch")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchDeleteQuestions(@RequestBody QuestionBatchDeleteRequest questionBatchDeleteRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(questionBatchDeleteRequest == null, ErrorCode.PARAMS_ERROR);
        questionService.batchDeleteQuestions(questionBatchDeleteRequest.getQuestionIdList());
        return ResultUtils.success(true);
    }

    private void ensureCanManageQuestionBank(Long questionBankId, User loginUser, HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库非法");
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        if (!userService.isAdmin(request) && !questionBank.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能管理自己的题库");
        }
    }

    private void ensureCanManageQuestion(Question question, User loginUser, HttpServletRequest request) {
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        if (!userService.isAdmin(request) && !question.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能把自己的题目加入题库");
        }
    }

    private void ensureCanManageQuestionList(List<Long> questionIdList, User loginUser, HttpServletRequest request) {
        ThrowUtils.throwIf(questionIdList == null || questionIdList.isEmpty(), ErrorCode.PARAMS_ERROR, "题目列表为空");
        if (userService.isAdmin(request)) {
            return;
        }
        List<Question> questionList = questionService.listByIds(questionIdList);
        ThrowUtils.throwIf(questionList.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        Set<Long> ownedQuestionIdSet = questionList.stream()
                .filter(question -> question.getUserId() != null && question.getUserId().equals(loginUser.getId()))
                .map(Question::getId)
                .collect(Collectors.toSet());
        ThrowUtils.throwIf(ownedQuestionIdSet.size() != questionIdList.size(), ErrorCode.NO_AUTH_ERROR, "只能管理自己创建的题目");
    }
}
