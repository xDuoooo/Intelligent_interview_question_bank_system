-- 添加默认管理员账号
-- 账号：admin
-- 密码：12345678 (MD5("xduo12345678") = dd50d8eff62344ecb394e73af0dd1eb1)

USE Intelligent_interview_question_bank_system;

INSERT INTO user (userAccount, userPassword, userName, userRole)
VALUES ('admin', 'dd50d8eff62344ecb394e73af0dd1eb1', '管理员', 'admin');
