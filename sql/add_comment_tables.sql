-- =============================================
-- 笔记与讨论模块 - 数据库迁移脚本
-- 执行前请先切换到对应数据库
-- use Intelligent_interview_question_bank_system;
-- =============================================

-- ----------------------------
-- 题目评论表
-- ----------------------------
CREATE TABLE IF NOT EXISTS question_comment
(
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    questionId BIGINT       NOT NULL                           COMMENT '题目 id',
    userId     BIGINT       NOT NULL                           COMMENT '发表者 id',
    parentId   BIGINT       DEFAULT NULL                       COMMENT '父评论 id（NULL=顶级评论）',
    replyToId  BIGINT       DEFAULT NULL                       COMMENT '回复的具体评论 id（用于 @提及）',
    content    TEXT         NOT NULL                           COMMENT '内容（纯文本/Markdown，最长2000字）',
    likeNum    INT          DEFAULT 0                          COMMENT '点赞数（冗余字段，异步同步）',
    reportNum  INT          DEFAULT 0                          COMMENT '被举报次数',
    isPinned   TINYINT      DEFAULT 0                          COMMENT '是否置顶（管理员操作）：0否 1是',
    isOfficial TINYINT      DEFAULT 0                          COMMENT '是否官方解答：0否 1是',
    status     TINYINT      DEFAULT 0                          COMMENT '状态：0正常 1待审核（举报>=3次） 2已隐藏',
    editTime   DATETIME     DEFAULT CURRENT_TIMESTAMP          COMMENT '编辑时间',
    createTime DATETIME     DEFAULT CURRENT_TIMESTAMP          COMMENT '创建时间',
    updateTime DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT      DEFAULT 0                          COMMENT '是否已删除（软删除）',
    INDEX idx_questionId (questionId),
    INDEX idx_parentId (parentId),
    INDEX idx_userId (userId),
    INDEX idx_createTime (createTime),
    INDEX idx_likeNum (likeNum)
) COMMENT '题目评论' COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- 评论点赞表（唯一索引保证幂等）
-- ----------------------------
CREATE TABLE IF NOT EXISTS question_comment_like
(
    id         BIGINT   AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    commentId  BIGINT   NOT NULL COMMENT '评论 id',
    userId     BIGINT   NOT NULL COMMENT '点赞用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_comment_user (commentId, userId)
) COMMENT '评论点赞' COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- 举报记录表（唯一索引防重复举报）
-- ----------------------------
CREATE TABLE IF NOT EXISTS question_comment_report
(
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    commentId  BIGINT       NOT NULL     COMMENT '被举报的评论 id',
    userId     BIGINT       NOT NULL     COMMENT '举报者 id',
    reason     VARCHAR(512) NOT NULL     COMMENT '举报原因',
    status     TINYINT      DEFAULT 0   COMMENT '处理状态：0待处理 1已驳回 2已删除',
    createTime DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comment_user (commentId, userId),
    INDEX idx_commentId (commentId)
) COMMENT '评论举报' COLLATE = utf8mb4_unicode_ci;
