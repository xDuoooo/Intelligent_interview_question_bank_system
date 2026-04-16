package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.mapper.UserQuestionStudySessionMapper;
import com.xduo.springbootinit.model.entity.UserQuestionStudySession;
import com.xduo.springbootinit.service.UserQuestionStudySessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户题目学习时长会话服务实现
 */
@Service
public class UserQuestionStudySessionServiceImpl extends ServiceImpl<UserQuestionStudySessionMapper, UserQuestionStudySession>
        implements UserQuestionStudySessionService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean recordStudySession(long userId, long questionId, int durationSeconds) {
        if (userId <= 0 || questionId <= 0 || durationSeconds <= 0) {
            return false;
        }
        UserQuestionStudySession session = new UserQuestionStudySession();
        session.setUserId(userId);
        session.setQuestionId(questionId);
        session.setDurationSeconds(durationSeconds);
        return this.save(session);
    }

    @Override
    public long getTotalStudyDurationSeconds(long userId) {
        return sumDurationByRange(userId, null, null);
    }

    @Override
    public long getTodayStudyDurationSeconds(long userId) {
        LocalDate today = LocalDate.now(ZONE_ID);
        return sumDurationByRange(userId, toDate(today.atStartOfDay()), toDate(today.plusDays(1).atStartOfDay()));
    }

    @Override
    public long countStudySessions(long userId) {
        QueryWrapper<UserQuestionStudySession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        return this.count(queryWrapper);
    }

    @Override
    public Map<String, Long> getStudyStats(long userId) {
        LocalDate today = LocalDate.now(ZONE_ID);
        String todayStart = SQL_DATE_TIME_FORMATTER.format(today.atStartOfDay());
        String tomorrowStart = SQL_DATE_TIME_FORMATTER.format(today.plusDays(1).atStartOfDay());
        QueryWrapper<UserQuestionStudySession> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(
                "COALESCE(SUM(durationSeconds), 0) AS totalDurationSeconds",
                "COALESCE(SUM(CASE WHEN createTime >= '" + todayStart + "' AND createTime < '" + tomorrowStart + "' THEN durationSeconds ELSE 0 END), 0) AS todayDurationSeconds",
                "COUNT(*) AS sessionCount"
        );
        queryWrapper.eq("userId", userId);
        Map<String, Object> result = this.getMap(queryWrapper);
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalDurationSeconds", parseLong(result == null ? null : result.get("totalDurationSeconds")));
        stats.put("todayDurationSeconds", parseLong(result == null ? null : result.get("todayDurationSeconds")));
        stats.put("sessionCount", parseLong(result == null ? null : result.get("sessionCount")));
        return stats;
    }

    private long sumDurationByRange(long userId, Date startTime, Date endTime) {
        QueryWrapper<UserQuestionStudySession> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("COALESCE(SUM(durationSeconds), 0) AS totalDuration");
        queryWrapper.eq("userId", userId);
        queryWrapper.ge(startTime != null, "createTime", startTime);
        queryWrapper.lt(endTime != null, "createTime", endTime);
        Map<String, Object> result = this.getMap(queryWrapper);
        if (result == null || result.get("totalDuration") == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(result.get("totalDuration")));
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
