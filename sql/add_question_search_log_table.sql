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
