use Intelligent_interview_question_bank_system;

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
