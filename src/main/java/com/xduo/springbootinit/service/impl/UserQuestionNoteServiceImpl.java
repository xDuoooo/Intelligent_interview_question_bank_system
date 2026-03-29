package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.mapper.UserQuestionNoteMapper;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.UserQuestionNote;
import com.xduo.springbootinit.model.vo.QuestionVO;
import com.xduo.springbootinit.model.vo.UserQuestionNoteVO;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.UserQuestionNoteService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户题目笔记服务实现
 */
@Service
public class UserQuestionNoteServiceImpl extends ServiceImpl<UserQuestionNoteMapper, UserQuestionNote>
        implements UserQuestionNoteService {

    @Resource
    private QuestionService questionService;

    @Override
    public boolean saveMyNote(long userId, long questionId, String content) {
        String trimmedContent = StringUtils.trimToEmpty(content);
        ThrowUtils.throwIf(questionId <= 0, ErrorCode.PARAMS_ERROR, "题目不存在");
        ThrowUtils.throwIf(StringUtils.isBlank(trimmedContent), ErrorCode.PARAMS_ERROR, "笔记内容不能为空");
        ThrowUtils.throwIf(trimmedContent.length() > 5000, ErrorCode.PARAMS_ERROR, "笔记内容最多 5000 个字符");
        Question question = questionService.getById(questionId);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");

        QueryWrapper<UserQuestionNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        UserQuestionNote oldNote = this.getOne(queryWrapper);
        if (oldNote != null) {
            oldNote.setContent(trimmedContent);
            return this.updateById(oldNote);
        }
        UserQuestionNote note = new UserQuestionNote();
        note.setUserId(userId);
        note.setQuestionId(questionId);
        note.setContent(trimmedContent);
        return this.save(note);
    }

    @Override
    public UserQuestionNoteVO getMyNoteByQuestionId(long userId, long questionId, HttpServletRequest request) {
        QueryWrapper<UserQuestionNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        UserQuestionNote note = this.getOne(queryWrapper);
        return buildNoteVO(note, request, Collections.emptyMap());
    }

    @Override
    public Page<UserQuestionNoteVO> listMyNoteByPage(long userId, Page<UserQuestionNote> page, HttpServletRequest request) {
        QueryWrapper<UserQuestionNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.orderByDesc("updateTime");
        Page<UserQuestionNote> notePage = this.page(page, queryWrapper);
        Page<UserQuestionNoteVO> voPage = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        List<UserQuestionNote> noteList = notePage.getRecords();
        if (noteList.isEmpty()) {
            return voPage;
        }
        Set<Long> questionIdSet = noteList.stream()
                .map(UserQuestionNote::getQuestionId)
                .collect(Collectors.toSet());
        Map<Long, Question> questionMap = questionService.listByIds(questionIdSet).stream()
                .collect(Collectors.toMap(Question::getId, question -> question, (left, right) -> left));
        voPage.setRecords(noteList.stream()
                .map(note -> buildNoteVO(note, request, questionMap))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public boolean deleteMyNote(long userId, long questionId) {
        QueryWrapper<UserQuestionNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        return this.remove(queryWrapper);
    }

    private UserQuestionNoteVO buildNoteVO(UserQuestionNote note,
                                           HttpServletRequest request,
                                           Map<Long, Question> questionMap) {
        if (note == null) {
            return null;
        }
        UserQuestionNoteVO vo = new UserQuestionNoteVO();
        BeanUtils.copyProperties(note, vo);
        Question question = questionMap.get(note.getQuestionId());
        if (question != null) {
            QuestionVO questionVO = questionService.getQuestionVO(question, request);
            vo.setQuestion(questionVO);
        }
        return vo;
    }
}
