-- ==========================================
-- 参天AI - 数据库清理和初始化脚本
-- ==========================================

-- 第一步：删除旧的test数据库中的表（如果存在）
USE test;
DROP TABLE IF EXISTS `tb_user_session`;
DROP TABLE IF EXISTS `tb_user`;

-- 第二步：删除test数据库（可选，如果不需要test数据库）
-- DROP DATABASE IF EXISTS `test`;

-- 第三步：创建新的bazi数据库
CREATE DATABASE IF NOT EXISTS `bazi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 第四步：使用bazi数据库
USE `bazi`;

-- 第五步：创建用户表
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `email` VARCHAR(100) NULL COMMENT '邮箱',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `nickname` VARCHAR(50) NULL COMMENT '昵称',
    `avatar` VARCHAR(255) NULL COMMENT '头像URL',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) NULL COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 第六步：创建用户会话表
DROP TABLE IF EXISTS `tb_user_session`;
CREATE TABLE `tb_user_session` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `token` VARCHAR(255) NOT NULL COMMENT '会话token',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 第七步：插入测试用户
-- 密码都是：123456（MD5加密后）
INSERT INTO `tb_user` (`username`, `password`, `email`, `phone`, `nickname`, `status`) 
VALUES 
    ('admin', MD5(CONCAT('123456', 'cantian_salt')), 'admin@cantian.ai', '13800138000', '管理员', 1),
    ('test', MD5(CONCAT('123456', 'cantian_salt')), 'test@cantian.ai', '13800138001', '测试用户', 1);

-- 完成！
SELECT '数据库初始化完成！' AS message;
SELECT '数据库名称：bazi' AS info;
SELECT COUNT(*) AS 用户数量 FROM tb_user;

