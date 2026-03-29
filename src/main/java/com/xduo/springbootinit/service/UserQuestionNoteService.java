package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.UserQuestionNote;
import com.xduo.springbootinit.model.vo.UserQuestionNoteVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户题目笔记服务
 */
public interface UserQuestionNoteService extends IService<UserQuestionNote> {

    /**
     * 保存或更新当前用户对题目的私有笔记
     */
    boolean saveMyNote(long userId, long questionId, String content);

    /**
     * 获取当前用户在某道题下的笔记
     */
    UserQuestionNoteVO getMyNoteByQuestionId(long userId, long questionId, HttpServletRequest request);

    /**
     * 获取当前用户的笔记分页
     */
    Page<UserQuestionNoteVO> listMyNoteByPage(long userId, Page<UserQuestionNote> page, HttpServletRequest request);

    /**
     * 删除当前用户的某条笔记
     */
    boolean deleteMyNote(long userId, long questionId);
}
