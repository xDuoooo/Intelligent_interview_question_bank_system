# 数据库初始化


-- 创建库
create database if not exists Intelligent_interview_question_bank_system;

-- 切换库
use Intelligent_interview_question_bank_system;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '社交平台唯一标识',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    phone        varchar(128)                           null comment '手机号',
    email        varchar(128)                           null comment '邮箱',
    githubId     varchar(256)                           null comment 'GitHub 唯一标识',
    giteeId      varchar(256)                           null comment 'Gitee 唯一标识',
    googleId     varchar(256)                           null comment 'Google 唯一标识',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
    ) comment '用户' collate = utf8mb4_unicode_ci;

-- 题库表
create table if not exists question_bank
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(256)                       null comment '标题',
    description text                               null comment '描述',
    picture     varchar(2048)                      null comment '图片',
    userId      bigint                             not null comment '创建用户 id',
    editTime    datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_title (title)
    ) comment '题库' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(256)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    answer     text                               null comment '推荐答案',
    userId     bigint                             not null comment '创建用户 id',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_title (title),
    index idx_userId (userId)
    ) comment '题目' collate = utf8mb4_unicode_ci;

-- 题库题目表（硬删除）
create table if not exists question_bank_question
(
    id             bigint auto_increment comment 'id' primary key,
    questionBankId bigint                             not null comment '题库 id',
    questionId     bigint                             not null comment '题目 id',
    userId         bigint                             not null comment '创建用户 id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE (questionBankId, questionId)
    ) comment '题库题目' collate = utf8mb4_unicode_ci;

-- 题目收藏表（硬删除）
create table if not exists question_favour
(
    id         bigint auto_increment comment 'id' primary key,
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目收藏';

-- 刷题记录表（用户题目的记录，硬删除）
create table if not exists user_question_history
(
    id         bigint auto_increment comment 'id' primary key,
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '用户 id',
    status     int      default 0                 not null comment '状态：0-未开始, 1-浏览中, 2-已过, 3-困难',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '刷题记录';

-- AI 模拟面试表
create table if not exists mock_interview
(
    id             bigint auto_increment comment 'id' primary key,
    jobPosition    varchar(256)                       not null comment '目标岗位',
    workExperience varchar(128)                       null comment '工作年限',
    difficulty     varchar(128)                       null comment '难度',
    messages       longtext                           null comment '消息记录（json 数组）',
    status         int      default 0                 not null comment '状态：0-待开始, 1-进行中, 2-已结束',
    userId         bigint                             not null comment '创建用户 id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),
    index idx_status (status),
    index idx_jobPosition (jobPosition)
) comment 'AI 模拟面试';

-- 用户学习目标表
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

-- 管理员操作日志表
create table if not exists admin_operation_log
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '管理员 id',
    userName   varchar(256)                       null comment '管理员名称',
    operation  varchar(512)                       null comment '操作描述',
    method     varchar(512)                       null comment '方法名',
    params     longtext                           null comment '请求参数',
    ip         varchar(128)                       null comment 'IP 地址',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),
    index idx_createTime (createTime)
) comment '管理员操作日志';

-- 题目搜索日志表
create table if not exists question_search_log
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                             null comment '搜索用户 id',
    searchText  varchar(128)                       not null comment '搜索关键词',
    source      varchar(64)  default 'question'    not null comment '搜索来源',
    resultCount int          default 0             not null comment '搜索命中数量',
    hasNoResult tinyint      default 0             not null comment '是否无结果：0-否 1-是',
    ip          varchar(128)                       null comment 'IP 地址',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0             not null comment '是否删除',
    index idx_searchText (searchText),
    index idx_createTime (createTime),
    index idx_source (source)
) comment '题目搜索日志';

-- 安全告警表
create table if not exists security_alert
(
    id            bigint auto_increment comment 'id' primary key,
    userId        bigint                             null comment '关联用户 id',
    userName      varchar(256)                       null comment '关联用户名',
    alertType     varchar(128)                       not null comment '告警类型',
    riskLevel     varchar(64)  default 'medium'      not null comment '风险等级',
    reason        varchar(512)                       null comment '告警原因',
    detail        longtext                           null comment '告警详情',
    ip            varchar(128)                       null comment 'IP 地址',
    status        int          default 0             not null comment '状态：0-待处理 1-已处理 2-已忽略',
    handlerUserId bigint                             null comment '处理人 id',
    handleAction  varchar(128)                       null comment '处理动作',
    handleTime    datetime                           null comment '处理时间',
    createTime    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint      default 0             not null comment '是否删除',
    index idx_userId (userId),
    index idx_status (status),
    index idx_alertType (alertType),
    index idx_createTime (createTime)
) comment '安全告警';
