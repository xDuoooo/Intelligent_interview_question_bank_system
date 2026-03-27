-- 通知表
create table if not exists notification
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                             not null comment '获知通知的用户 id',
    title       varchar(512)                       not null comment '标题',
    content     text                               not null comment '内容',
    type        varchar(256)                       null comment '类型：system, user, post, etc.',
    status      tinyint  default 0                 not null comment '状态：0-未读, 1-已读',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '通知' collate = utf8mb4_unicode_ci;
