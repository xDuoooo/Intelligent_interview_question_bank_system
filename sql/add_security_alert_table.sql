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
