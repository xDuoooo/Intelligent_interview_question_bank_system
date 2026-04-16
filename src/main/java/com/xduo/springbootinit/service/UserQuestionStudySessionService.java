package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.UserQuestionStudySession;

import java.util.Map;

/**
 * 用户题目学习时长会话服务
 */
public interface UserQuestionStudySessionService extends IService<UserQuestionStudySession> {

    boolean recordStudySession(long userId, long questionId, int durationSeconds);

    long getTotalStudyDurationSeconds(long userId);

    long getTodayStudyDurationSeconds(long userId);

    long countStudySessions(long userId);

    Map<String, Long> getStudyStats(long userId);
}
