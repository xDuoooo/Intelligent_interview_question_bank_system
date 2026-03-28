package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.mapper.UserLearningGoalMapper;
import com.xduo.springbootinit.model.entity.UserLearningGoal;
import com.xduo.springbootinit.service.UserLearningGoalService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户学习目标服务实现
 */
@Service
public class UserLearningGoalServiceImpl extends ServiceImpl<UserLearningGoalMapper, UserLearningGoal>
        implements UserLearningGoalService {

    private static final int DEFAULT_DAILY_TARGET = 3;

    @Override
    public UserLearningGoal getOrInitByUserId(long userId) {
        QueryWrapper<UserLearningGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        UserLearningGoal goal = this.getOne(queryWrapper);
        if (goal != null) {
            return goal;
        }
        UserLearningGoal newGoal = new UserLearningGoal();
        newGoal.setUserId(userId);
        newGoal.setDailyTarget(DEFAULT_DAILY_TARGET);
        newGoal.setReminderEnabled(1);
        this.save(newGoal);
        return newGoal;
    }

    @Override
    public boolean updateUserLearningGoal(long userId, int dailyTarget, boolean reminderEnabled) {
        UserLearningGoal goal = getOrInitByUserId(userId);
        goal.setDailyTarget(dailyTarget);
        goal.setReminderEnabled(reminderEnabled ? 1 : 0);
        return this.updateById(goal);
    }

    @Override
    public List<UserLearningGoal> listReminderEnabledGoals() {
        QueryWrapper<UserLearningGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reminderEnabled", 1);
        queryWrapper.gt("dailyTarget", 0);
        return this.list(queryWrapper);
    }
}
