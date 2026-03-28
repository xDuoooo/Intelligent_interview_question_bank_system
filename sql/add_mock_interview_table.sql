use Intelligent_interview_question_bank_system;

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
