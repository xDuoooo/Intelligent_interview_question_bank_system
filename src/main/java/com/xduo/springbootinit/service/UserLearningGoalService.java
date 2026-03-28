package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.UserLearningGoal;

import java.util.List;

/**
 * 用户学习目标服务
 */
public interface UserLearningGoalService extends IService<UserLearningGoal> {

    /**
     * 获取用户学习目标，不存在时自动初始化默认值
     */
    UserLearningGoal getOrInitByUserId(long userId);

    /**
     * 更新用户学习目标
     */
    boolean updateUserLearningGoal(long userId, int dailyTarget, boolean reminderEnabled);

    /**
     * 查询已开启提醒的学习目标
     */
    List<UserLearningGoal> listReminderEnabledGoals();
}
