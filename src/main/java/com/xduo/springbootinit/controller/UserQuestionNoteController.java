package com.xduo.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.model.dto.userquestionnote.UserQuestionNoteQueryRequest;
import com.xduo.springbootinit.model.dto.userquestionnote.UserQuestionNoteSaveRequest;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.entity.UserQuestionNote;
import com.xduo.springbootinit.model.vo.UserQuestionNoteVO;
import com.xduo.springbootinit.service.UserQuestionNoteService;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户题目私有笔记接口
 */
@RestController
@RequestMapping("/user_question_note")
public class UserQuestionNoteController {

    @Resource
    private UserQuestionNoteService userQuestionNoteService;

    @Resource
    private UserService userService;

    @PostMapping("/save")
    public BaseResponse<Boolean> saveMyNote(@RequestBody UserQuestionNoteSaveRequest saveRequest,
                                            HttpServletRequest request) {
        if (saveRequest == null || saveRequest.getQuestionId() == null || saveRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userQuestionNoteService.saveMyNote(
                loginUser.getId(),
                saveRequest.getQuestionId(),
                saveRequest.getContent()
        ));
    }

    @GetMapping("/get/my")
    public BaseResponse<UserQuestionNoteVO> getMyNote(@RequestParam Long questionId, HttpServletRequest request) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userQuestionNoteService.getMyNoteByQuestionId(loginUser.getId(), questionId, request));
    }

    @PostMapping("/delete/my")
    public BaseResponse<Boolean> deleteMyNote(@RequestBody UserQuestionNoteSaveRequest saveRequest, HttpServletRequest request) {
        if (saveRequest == null || saveRequest.getQuestionId() == null || saveRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userQuestionNoteService.deleteMyNote(loginUser.getId(), saveRequest.getQuestionId()));
    }

    @PostMapping("/my/list/page")
    public BaseResponse<Page<UserQuestionNoteVO>> listMyNoteByPage(@RequestBody UserQuestionNoteQueryRequest queryRequest,
                                                                   HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        if (current < 1 || pageSize < 1 || pageSize > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数不合法");
        }
        User loginUser = userService.getLoginUser(request);
        Page<UserQuestionNoteVO> notePage = userQuestionNoteService.listMyNoteByPage(
                loginUser.getId(),
                new Page<UserQuestionNote>(current, pageSize),
                request
        );
        return ResultUtils.success(notePage);
    }
}
