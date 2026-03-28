use Intelligent_interview_question_bank_system;

create table if not exists user_learning_goal
(
    id               bigint auto_increment comment 'id' primary key,
    userId           bigint                             not null comment '用户 id',
    dailyTarget      int      default 3                 not null comment '每日刷题目标',
    reminderEnabled  tinyint  default 1                 not null comment '是否开启提醒',
    lastReminderTime datetime                           null comment '上次提醒时间',
    createTime       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint  default 0                 not null comment '是否删除',
    unique key uk_userId (userId)
) comment '用户学习目标';
