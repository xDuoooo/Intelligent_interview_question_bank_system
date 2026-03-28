package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户学习目标
 */
@TableName(value = "user_learning_goal")
@Data
public class UserLearningGoal implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 每日刷题目标
     */
    private Integer dailyTarget;

    /**
     * 是否开启提醒
     */
    private Integer reminderEnabled;

    /**
     * 上次提醒时间
     */
    private Date lastReminderTime;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
