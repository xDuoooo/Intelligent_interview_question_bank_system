package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.mapper.UserQuestionHistoryMapper;
import com.xduo.springbootinit.model.entity.UserQuestionHistory;
import com.xduo.springbootinit.service.UserQuestionHistoryService;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.QuestionFavour;
import com.xduo.springbootinit.model.entity.UserLearningGoal;
import com.xduo.springbootinit.model.vo.QuestionVO;
import com.xduo.springbootinit.model.vo.UserQuestionHistoryVO;
import com.xduo.springbootinit.service.QuestionFavourService;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.UserLearningGoalService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 用户刷题轨迹服务实现
 */
@Service
public class UserQuestionHistoryServiceImpl extends ServiceImpl<UserQuestionHistoryMapper, UserQuestionHistory>
        implements UserQuestionHistoryService {

    @Resource
    private QuestionFavourService questionFavourService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserLearningGoalService userLearningGoalService;

    @Override
    public boolean addQuestionHistory(long userId, long questionId, int status) {
        if (status < 0 || status > 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "刷题状态不合法");
        }
        // 先查询是否已经有记录
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        UserQuestionHistory oldHistory = this.getOne(queryWrapper);

        if (oldHistory != null) {
            // 状态值本身不是业务优先级，掌握应高于困难，困难高于浏览
            if (shouldReplaceStatus(oldHistory.getStatus(), status)) {
                oldHistory.setStatus(status);
            }
            oldHistory.setUpdateTime(new Date());
            return this.updateById(oldHistory);
        } else {
            // 插入新记录
            UserQuestionHistory newHistory = new UserQuestionHistory();
            newHistory.setUserId(userId);
            newHistory.setQuestionId(questionId);
            newHistory.setStatus(status);
            return this.save(newHistory);
        }
    }

    @Override
    public Page<QuestionVO> listMyFavourQuestionByPage(Page<Question> page, long userId, HttpServletRequest request) {
        // 先查询收藏记录
        QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.eq("userId", userId);
        favourQueryWrapper.orderByDesc("createTime");
        Page<QuestionFavour> favourPage = questionFavourService.page(new Page<>(page.getCurrent(), page.getSize()), favourQueryWrapper);
        
        List<QuestionFavour> favourList = favourPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(favourPage.getCurrent(), favourPage.getSize(), favourPage.getTotal());
        if (favourList.isEmpty()) {
            return questionVOPage;
        }
        
        // 根据题目 id 查询题目详情
        Set<Long> questionIdSet = favourList.stream().map(QuestionFavour::getQuestionId).collect(Collectors.toSet());
        Map<Long, Question> questionMap = questionService.listByIds(questionIdSet).stream()
                .collect(Collectors.toMap(Question::getId, question -> question, (a, b) -> a));
        List<Question> questionList = favourList.stream()
                .map(favour -> questionMap.get(favour.getQuestionId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        // 转换为 VO 分页
        Page<Question> questionPage = new Page<>(favourPage.getCurrent(), favourPage.getSize(), favourPage.getTotal());
        questionPage.setRecords(questionList);
        return questionService.getQuestionVOPage(questionPage, request);
    }

    @Override
    public Page<UserQuestionHistoryVO> listMyQuestionHistoryByPage(Page<UserQuestionHistory> page, long userId, HttpServletRequest request) {
        QueryWrapper<UserQuestionHistory> historyQueryWrapper = new QueryWrapper<>();
        historyQueryWrapper.eq("userId", userId);
        historyQueryWrapper.orderByDesc("updateTime");
        Page<UserQuestionHistory> historyPage = this.page(page, historyQueryWrapper);
        
        List<UserQuestionHistory> historyList = historyPage.getRecords();
        Page<UserQuestionHistoryVO> voPage = new Page<>(historyPage.getCurrent(), historyPage.getSize(), historyPage.getTotal());
        if (historyList.isEmpty()) {
            return voPage;
        }
        
        Set<Long> questionIdSet = historyList.stream().map(UserQuestionHistory::getQuestionId).collect(Collectors.toSet());
        List<Question> questionList = questionService.listByIds(questionIdSet);
        Map<Long, List<Question>> questionMap = questionList.stream().collect(Collectors.groupingBy(Question::getId));
        
        List<UserQuestionHistoryVO> voList = historyList.stream().map(history -> {
            UserQuestionHistoryVO vo = new UserQuestionHistoryVO();
            org.springframework.beans.BeanUtils.copyProperties(history, vo);
            Long qId = history.getQuestionId();
            if (questionMap.containsKey(qId)) {
                vo.setQuestion(questionService.getQuestionVO(questionMap.get(qId).get(0), request));
            }
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<Map<String, Object>> getUserQuestionHistoryRecord(long userId, Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = startDate.plusYears(1);
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(updateTime) as date", "count(*) as count");
        queryWrapper.eq("userId", userId);
        queryWrapper.ge("updateTime", toDate(startDate, true));
        queryWrapper.lt("updateTime", toDate(endDate, true));
        queryWrapper.groupBy("DATE(updateTime)");
        return this.listMaps(queryWrapper);
    }

    @Override
    public Map<String, Object> getUserQuestionStats(long userId) {
        Map<String, Object> stats = new java.util.HashMap<>();

        // 总刷题量
        QueryWrapper<UserQuestionHistory> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("userId", userId);
        long totalCount = this.count(totalWrapper);
        stats.put("totalCount", totalCount);

        // 已掌握数量 (status = 1)
        QueryWrapper<UserQuestionHistory> masteredWrapper = new QueryWrapper<>();
        masteredWrapper.eq("userId", userId);
        masteredWrapper.eq("status", 1);
        long masteredCount = this.count(masteredWrapper);
        stats.put("masteredCount", masteredCount);

        // 收藏数量
        QueryWrapper<QuestionFavour> favourWrapper = new QueryWrapper<>();
        favourWrapper.eq("userId", userId);
        long favourCount = questionFavourService.count(favourWrapper);
        stats.put("favourCount", favourCount);

        // 活跃天数、连续天数
        List<LocalDate> activeDateList = getActiveDateList(userId);
        long activeDays = activeDateList.size();
        long currentStreak = calculateCurrentStreak(activeDateList);
        stats.put("activeDays", activeDays);
        stats.put("currentStreak", currentStreak);

        // 今日刷题量与学习目标
        long todayCount = getTodayQuestionCount(userId);
        UserLearningGoal learningGoal = userLearningGoalService.getOrInitByUserId(userId);
        int dailyTarget = learningGoal.getDailyTarget() == null ? 3 : learningGoal.getDailyTarget();
        boolean reminderEnabled = learningGoal.getReminderEnabled() != null && learningGoal.getReminderEnabled() == 1;
        stats.put("todayCount", todayCount);
        stats.put("dailyTarget", dailyTarget);
        stats.put("reminderEnabled", reminderEnabled);
        stats.put("goalCompletedToday", todayCount >= dailyTarget);
        stats.put("todayProgress", Math.min(todayCount, dailyTarget));

        // 成就列表
        stats.put("achievementList", buildAchievementList(totalCount, masteredCount, favourCount, activeDays, currentStreak));
        return stats;
    }

    @Override
    public long getTodayQuestionCount(long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        QueryWrapper<UserQuestionHistory> todayWrapper = new QueryWrapper<>();
        todayWrapper.eq("userId", userId);
        todayWrapper.ge("updateTime", toDate(today, true));
        todayWrapper.lt("updateTime", toDate(today.plusDays(1), true));
        return this.count(todayWrapper);
    }

    private List<LocalDate> getActiveDateList(long userId) {
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(updateTime) as date");
        queryWrapper.eq("userId", userId);
        queryWrapper.groupBy("DATE(updateTime)");
        queryWrapper.orderByDesc("DATE(updateTime)");
        List<Map<String, Object>> dateMapList = this.listMaps(queryWrapper);
        List<LocalDate> activeDateList = new ArrayList<>();
        for (Map<String, Object> item : dateMapList) {
            Object dateObj = item.get("date");
            if (dateObj != null) {
                activeDateList.add(LocalDate.parse(String.valueOf(dateObj)));
            }
        }
        return activeDateList;
    }

    private boolean shouldReplaceStatus(Integer oldStatus, Integer newStatus) {
        return getStatusPriority(newStatus) > getStatusPriority(oldStatus);
    }

    private int getStatusPriority(Integer status) {
        if (status == null) {
            return 0;
        }
        switch (status) {
            case 1:
                return 3;
            case 2:
                return 2;
            case 0:
                return 1;
            default:
                return 0;
        }
    }

    private Date toDate(LocalDate localDate, boolean startOfDay) {
        LocalDateTime localDateTime = startOfDay ? localDate.atStartOfDay() : localDate.plusDays(1).atStartOfDay();
        return Date.from(localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant());
    }

    private long calculateCurrentStreak(List<LocalDate> activeDateList) {
        if (activeDateList.isEmpty()) {
            return 0;
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate latestActiveDate = activeDateList.get(0);
        if (latestActiveDate.isBefore(today.minusDays(1))) {
            return 0;
        }
        long streak = 1;
        LocalDate previousDate = latestActiveDate;
        for (int i = 1; i < activeDateList.size(); i++) {
            LocalDate currentDate = activeDateList.get(i);
            if (currentDate.equals(previousDate.minusDays(1))) {
                streak++;
                previousDate = currentDate;
            } else {
                break;
            }
        }
        return streak;
    }

    private List<Map<String, Object>> buildAchievementList(long totalCount, long masteredCount, long favourCount,
                                                           long activeDays, long currentStreak) {
        List<Map<String, Object>> achievementList = new ArrayList<>();
        achievementList.add(buildAchievement("first_practice", "初试锋芒", "完成 1 道题，正式开始备战面试。", totalCount, 1));
        achievementList.add(buildAchievement("practice_10", "刷题新秀", "累计刷题达到 10 道。", totalCount, 10));
        achievementList.add(buildAchievement("master_20", "知识掌握者", "标记掌握题目达到 20 道。", masteredCount, 20));
        achievementList.add(buildAchievement("favour_10", "收藏达人", "收藏题目达到 10 道。", favourCount, 10));
        achievementList.add(buildAchievement("active_7", "持续学习者", "累计活跃学习达到 7 天。", activeDays, 7));
        achievementList.add(buildAchievement("streak_7", "连续冲刺", "连续学习达到 7 天。", currentStreak, 7));
        return achievementList;
    }

    private Map<String, Object> buildAchievement(String key, String title, String description, long current, long target) {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("key", key);
        achievement.put("title", title);
        achievement.put("description", description);
        achievement.put("current", current);
        achievement.put("target", target);
        achievement.put("achieved", current >= target);
        achievement.put("progress", Math.min(current, target));
        return achievement;
    }
}
