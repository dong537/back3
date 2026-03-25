-- 推荐/邀请系统数据库表结构
-- 用于支持用户裂变、积分、签到、任务、成就等功能

USE `bazi`;

-- ============================================
-- 1. 推荐/邀请相关表
-- ============================================

-- 用户推荐关系表
CREATE TABLE IF NOT EXISTS `tb_user_referral` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `referral_code` VARCHAR(32) NOT NULL COMMENT '推荐码（唯一）',
    `referred_by` BIGINT(20) NULL COMMENT '推荐人用户ID（谁邀请的）',
    `referral_code_used` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '推荐码是否已使用：0-未使用，1-已使用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_referral_code` (`referral_code`),
    KEY `idx_referred_by` (`referred_by`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户推荐关系表';

-- 邀请记录表
CREATE TABLE IF NOT EXISTS `tb_invite_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `inviter_id` BIGINT(20) NOT NULL COMMENT '邀请人用户ID',
    `invitee_id` BIGINT(20) NULL COMMENT '被邀请人用户ID（注册后填充）',
    `referral_code` VARCHAR(32) NOT NULL COMMENT '使用的推荐码',
    `invite_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邀请状态：0-待注册，1-已注册，2-已首次占卜，3-已完成',
    `register_time` DATETIME NULL COMMENT '注册时间',
    `first_divination_time` DATETIME NULL COMMENT '首次占卜时间',
    `reward_given` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '奖励是否已发放：0-未发放，1-已发放',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_inviter_id` (`inviter_id`),
    KEY `idx_invitee_id` (`invitee_id`),
    KEY `idx_referral_code` (`referral_code`),
    KEY `idx_invite_status` (`invite_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请记录表';

-- ============================================
-- 2. 积分系统扩展表
-- ============================================

-- 积分奖励规则表（可选，用于配置奖励规则）
CREATE TABLE IF NOT EXISTS `tb_points_rule` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型：daily_checkin, share, invite_register, invite_divination, task, achievement',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `points_amount` INT NOT NULL COMMENT '积分数量',
    `max_daily` INT NULL COMMENT '每日最大奖励次数（NULL表示无限制）',
    `max_total` INT NULL COMMENT '总最大奖励次数（NULL表示无限制）',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `description` VARCHAR(255) NULL COMMENT '规则描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_type` (`rule_type`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分奖励规则表';

-- 初始化积分奖励规则
INSERT INTO `tb_points_rule` (`rule_type`, `rule_name`, `points_amount`, `max_daily`, `max_total`, `description`) VALUES
('daily_checkin_1_2', '每日签到（第1-2天）', 10, 1, NULL, '连续签到第1-2天，每天10积分'),
('daily_checkin_3_6', '每日签到（第3-6天）', 20, 1, NULL, '连续签到第3-6天，每天20积分'),
('daily_checkin_7_plus', '每日签到（第7天及以上）', 30, 1, NULL, '连续签到第7天及以上，每天30积分'),
('share_result', '分享占卜结果', 10, 10, NULL, '每次分享占卜结果获得10积分，每日最多10次'),
('invite_register', '好友注册奖励', 20, NULL, NULL, '好友使用邀请码注册，双方各获得20积分'),
('invite_first_divination', '好友首次占卜奖励', 30, NULL, NULL, '好友完成首次占卜，推荐人获得30积分'),
('task_complete', '完成任务奖励', 10, NULL, NULL, '完成每日任务获得10积分'),
('achievement_unlock', '解锁成就奖励', 20, NULL, NULL, '解锁成就获得20-100积分（根据成就类型）');

-- ============================================
-- 3. 每日签到表
-- ============================================

-- 每日签到记录表
CREATE TABLE IF NOT EXISTS `tb_daily_checkin` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `checkin_date` DATE NOT NULL COMMENT '签到日期',
    `checkin_time` DATETIME NOT NULL COMMENT '签到时间',
    `streak_days` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
    `points_earned` INT NOT NULL COMMENT '本次签到获得的积分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_checkin_date` (`checkin_date`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日签到记录表';

-- ============================================
-- 4. 任务系统表
-- ============================================

-- 任务定义表
CREATE TABLE IF NOT EXISTS `tb_task` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_code` VARCHAR(50) NOT NULL COMMENT '任务代码（唯一）',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型：daily-每日任务，long_term-长期任务',
    `task_description` VARCHAR(255) NULL COMMENT '任务描述',
    `target_value` INT NOT NULL DEFAULT 1 COMMENT '目标值（需要完成多少次）',
    `points_reward` INT NOT NULL COMMENT '积分奖励',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_code` (`task_code`),
    KEY `idx_task_type` (`task_type`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务定义表';

-- 初始化任务数据
INSERT INTO `tb_task` (`task_code`, `task_name`, `task_type`, `task_description`, `target_value`, `points_reward`, `sort_order`) VALUES
('daily_checkin', '每日签到', 'daily', '完成每日签到', 1, 10, 1),
('daily_divination', '每日占卜', 'daily', '进行1次占卜', 1, 10, 2),
('daily_favorite', '收藏结果', 'daily', '收藏1个占卜结果', 1, 10, 3),
('daily_share', '分享结果', 'daily', '分享1次占卜结果', 1, 10, 4),
('divination_5', '占卜达人', 'long_term', '完成5次占卜', 5, 50, 10),
('divination_10', '占卜大师', 'long_term', '完成10次占卜', 10, 100, 11),
('favorite_3', '收藏家', 'long_term', '收藏3个结果', 3, 30, 20),
('favorite_10', '收藏大师', 'long_term', '收藏10个结果', 10, 100, 21),
('checkin_streak_7', '持之以恒', 'long_term', '连续签到7天', 7, 50, 30),
('checkin_streak_30', '坚持不懈', 'long_term', '连续签到30天', 30, 200, 31),
('share_5', '分享达人', 'long_term', '分享5次结果', 5, 50, 40),
('invite_3', '邀请达人', 'long_term', '成功邀请3位好友', 3, 100, 50);

-- 用户任务进度表
CREATE TABLE IF NOT EXISTS `tb_user_task_progress` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `task_id` BIGINT(20) NOT NULL COMMENT '任务ID',
    `task_code` VARCHAR(50) NOT NULL COMMENT '任务代码',
    `current_progress` INT NOT NULL DEFAULT 0 COMMENT '当前进度',
    `target_value` INT NOT NULL COMMENT '目标值',
    `is_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已完成：0-未完成，1-已完成',
    `completed_time` DATETIME NULL COMMENT '完成时间',
    `points_earned` INT NULL COMMENT '获得的积分',
    `last_update_date` DATE NULL COMMENT '最后更新日期（用于每日任务重置）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_task` (`user_id`, `task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_task_code` (`task_code`),
    KEY `idx_is_completed` (`is_completed`),
    KEY `idx_last_update_date` (`last_update_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户任务进度表';

-- ============================================
-- 5. 成就系统表
-- ============================================

-- 成就定义表
CREATE TABLE IF NOT EXISTS `tb_achievement` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `achievement_code` VARCHAR(50) NOT NULL COMMENT '成就代码（唯一）',
    `achievement_name` VARCHAR(100) NOT NULL COMMENT '成就名称',
    `achievement_description` VARCHAR(255) NULL COMMENT '成就描述',
    `achievement_type` VARCHAR(50) NOT NULL COMMENT '成就类型：divination-占卜，favorite-收藏，invite-邀请，checkin-签到，points-积分',
    `points_reward` INT NOT NULL COMMENT '积分奖励',
    `icon_url` VARCHAR(255) NULL COMMENT '成就图标URL',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_achievement_code` (`achievement_code`),
    KEY `idx_achievement_type` (`achievement_type`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就定义表';

-- 初始化成就数据
INSERT INTO `tb_achievement` (`achievement_code`, `achievement_name`, `achievement_description`, `achievement_type`, `points_reward`, `sort_order`) VALUES
('first_divination', '初窥天机', '完成第一次占卜', 'divination', 20, 1),
('divination_master', '占卜达人', '完成10次占卜', 'divination', 50, 2),
('divination_expert', '占卜专家', '完成50次占卜', 'divination', 200, 3),
('collector', '收藏家', '收藏5个结果', 'favorite', 30, 10),
('collector_master', '收藏大师', '收藏20个结果', 'favorite', 100, 11),
('inviter', '邀请达人', '成功邀请3位好友', 'invite', 100, 20),
('inviter_master', '邀请大师', '成功邀请10位好友', 'invite', 500, 21),
('checkin_week', '持之以恒', '连续签到7天', 'checkin', 50, 30),
('checkin_month', '坚持不懈', '连续签到30天', 'checkin', 200, 31),
('points_rich', '积分富翁', '累计获得500积分', 'points', 100, 40),
('points_millionaire', '积分大亨', '累计获得2000积分', 'points', 500, 41);

-- 用户成就记录表
CREATE TABLE IF NOT EXISTS `tb_user_achievement` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `achievement_id` BIGINT(20) NOT NULL COMMENT '成就ID',
    `achievement_code` VARCHAR(50) NOT NULL COMMENT '成就代码',
    `unlocked_time` DATETIME NOT NULL COMMENT '解锁时间',
    `points_earned` INT NOT NULL COMMENT '获得的积分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_achievement_id` (`achievement_id`),
    KEY `idx_achievement_code` (`achievement_code`),
    KEY `idx_unlocked_time` (`unlocked_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就记录表';

-- ============================================
-- 6. 分享记录表
-- ============================================

-- 分享记录表
CREATE TABLE IF NOT EXISTS `tb_share_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `share_type` VARCHAR(50) NOT NULL COMMENT '分享类型：yijing-易经，tarot-塔罗，bazi-八字，zodiac-星座',
    `share_content_id` VARCHAR(100) NULL COMMENT '分享内容ID',
    `share_platform` VARCHAR(50) NULL COMMENT '分享平台：wechat, weibo, qq, copy_link等',
    `points_earned` INT NOT NULL DEFAULT 0 COMMENT '获得的积分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_share_type` (`share_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享记录表';

-- ============================================
-- 7. 用户行为统计表（可选，用于数据分析）
-- ============================================

-- 用户行为统计表
CREATE TABLE IF NOT EXISTS `tb_user_behavior_stats` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `divination_count` INT NOT NULL DEFAULT 0 COMMENT '占卜次数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
    `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享次数',
    `checkin_count` INT NOT NULL DEFAULT 0 COMMENT '签到次数（0或1）',
    `points_earned` INT NOT NULL DEFAULT 0 COMMENT '当日获得积分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为统计表';

-- ============================================
-- 8. 扩展用户表字段（如果需要）
-- ============================================

-- 为用户表添加推荐相关字段（如果还没有）
-- 注意：MySQL不支持 ADD COLUMN IF NOT EXISTS，使用存储过程来检查

DELIMITER $$

-- 检查并添加字段的存储过程
DROP PROCEDURE IF EXISTS `add_user_table_columns`$$
CREATE PROCEDURE `add_user_table_columns`()
BEGIN
    -- 检查并添加 referral_code 字段
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'referral_code'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `referral_code` VARCHAR(32) NULL COMMENT '推荐码' AFTER `avatar`;
    END IF;

    -- 检查并添加 total_points 字段
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'total_points'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `total_points` INT NOT NULL DEFAULT 0 COMMENT '累计获得积分' AFTER `referral_code`;
    END IF;

    -- 检查并添加 current_points 字段
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'current_points'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `current_points` INT NOT NULL DEFAULT 0 COMMENT '当前积分余额' AFTER `total_points`;
    END IF;

    -- 检查并添加 checkin_streak 字段
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'checkin_streak'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `checkin_streak` INT NOT NULL DEFAULT 0 COMMENT '连续签到天数' AFTER `current_points`;
    END IF;

    -- 检查并添加 last_checkin_date 字段
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'last_checkin_date'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `last_checkin_date` DATE NULL COMMENT '最后签到日期' AFTER `checkin_streak`;
    END IF;

    -- 检查并添加索引
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND INDEX_NAME = 'idx_referral_code'
    ) THEN
        ALTER TABLE `tb_user` ADD INDEX `idx_referral_code` (`referral_code`);
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL `add_user_table_columns`();

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS `add_user_table_columns`;

-- ============================================
-- 完成
-- ============================================

SELECT '推荐系统数据库表创建完成！' AS message;
