-- 增加用户联系方式字段
ALTER TABLE user ADD COLUMN phone VARCHAR(128) NULL COMMENT '手机号' AFTER userProfile;
ALTER TABLE user ADD COLUMN email VARCHAR(128) NULL COMMENT '邮箱' AFTER phone;

-- 增加题目收藏表
CREATE TABLE IF NOT EXISTS question_favour
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    questionId BIGINT                             NOT NULL COMMENT '题目 id',
    userId     BIGINT                             NOT NULL COMMENT '用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_questionId (questionId),
    INDEX idx_userId (userId)
) COMMENT '题目收藏';

-- 增加用户刷题记录表
CREATE TABLE IF NOT EXISTS user_question_history
(
    id          BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userId      BIGINT                             NOT NULL COMMENT '用户 id',
    questionId  BIGINT                             NOT NULL COMMENT '题目 id',
    status      TINYINT  DEFAULT 0                 NOT NULL COMMENT '作答状态：0-浏览, 1-掌握, 2-困难',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_question (userId, questionId),
    INDEX idx_userId (userId)
) COMMENT '用户刷题轨迹';
