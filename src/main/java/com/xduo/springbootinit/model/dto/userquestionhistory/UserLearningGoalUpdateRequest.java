package com.xduo.springbootinit.model.dto.userquestionhistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新用户学习目标请求
 */
@Data
public class UserLearningGoalUpdateRequest implements Serializable {

    /**
     * 每日刷题目标
     */
    private Integer dailyTarget;

    /**
     * 是否开启提醒
     */
    private Boolean reminderEnabled;

    private static final long serialVersionUID = 1L;
}
