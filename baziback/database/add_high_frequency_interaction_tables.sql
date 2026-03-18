-- 高频互动功能数据库表
-- 包括：每日测试、心情记录、运势时间轴、测算记录、提醒等

    use bazi;
-- 1. 每日测试表
CREATE TABLE IF NOT EXISTS `tb_daily_test` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `test_type` VARCHAR(50) NOT NULL COMMENT '测试类型：bazi_fortune, yijing_divination, tarot_draw等',
  `test_date` DATE NOT NULL COMMENT '测试日期',
  `question` VARCHAR(500) COMMENT '测试问题',
  `result_data` JSON COMMENT '测试结果数据',
  `score` INT COMMENT '测试得分（如有）',
  `summary` VARCHAR(500) COMMENT '测试摘要',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_date` (`user_id`, `test_date`),
  INDEX `idx_test_type` (`test_type`),
  CONSTRAINT `fk_daily_test_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日测试记录表';

-- 2. 心情记录表
CREATE TABLE IF NOT EXISTS `tb_mood_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `mood_type` VARCHAR(20) NOT NULL COMMENT '心情类型：happy, sad, anxious, calm, excited等',
  `mood_score` INT NOT NULL COMMENT '心情评分：1-10',
  `mood_desc` VARCHAR(500) COMMENT '心情描述',
  `related_fortune` VARCHAR(50) COMMENT '关联运势：bazi, yijing, tarot等',
  `fortune_data` JSON COMMENT '关联的运势数据',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `record_date`),
  INDEX `idx_user` (`user_id`),
  CONSTRAINT `fk_mood_record_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心情记录表';

-- 3. 运势时间轴节点表
CREATE TABLE IF NOT EXISTS `tb_fortune_timeline` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `timeline_type` VARCHAR(50) NOT NULL COMMENT '时间轴类型：dayun, liunian, liuyue等',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE COMMENT '结束日期',
  `node_type` VARCHAR(50) NOT NULL COMMENT '节点类型：wealth_peak, career_turn, love_opportunity, health_warning等',
  `node_title` VARCHAR(200) NOT NULL COMMENT '节点标题',
  `node_desc` TEXT COMMENT '节点描述',
  `fortune_data` JSON COMMENT '运势数据',
  `ai_interpretation` TEXT COMMENT 'AI解读',
  `action_suggestion` TEXT COMMENT '行动建议',
  `importance` INT DEFAULT 5 COMMENT '重要性：1-10',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_timeline` (`user_id`, `timeline_type`, `start_date`),
  INDEX `idx_node_type` (`node_type`),
  CONSTRAINT `fk_fortune_timeline_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运势时间轴节点表';

-- 4. 测算记录表（扩展历史记录功能）
CREATE TABLE IF NOT EXISTS `tb_calculation_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `record_type` VARCHAR(50) NOT NULL COMMENT '记录类型：bazi, yijing, tarot, compatibility等',
  `record_title` VARCHAR(200) NOT NULL COMMENT '记录标题',
  `question` VARCHAR(500) COMMENT '测算问题',
  `input_data` JSON COMMENT '输入数据',
  `result_data` JSON NOT NULL COMMENT '结果数据',
  `summary` VARCHAR(500) COMMENT '摘要',
  `tags` VARCHAR(200) COMMENT '标签，逗号分隔',
  `is_favorite` TINYINT DEFAULT 0 COMMENT '是否收藏：0否，1是',
  `is_shared` TINYINT DEFAULT 0 COMMENT '是否分享：0否，1是',
  `share_url` VARCHAR(500) COMMENT '分享链接',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `view_count` INT DEFAULT 0 COMMENT '查看次数',
  `last_viewed_at` DATETIME COMMENT '最后查看时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_type` (`user_id`, `record_type`),
  INDEX `idx_favorite` (`user_id`, `is_favorite`),
  INDEX `idx_created` (`created_at`),
  CONSTRAINT `fk_calculation_record_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测算记录表';

-- 5. 复测算提醒表
CREATE TABLE IF NOT EXISTS `tb_calculation_reminder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `reminder_type` VARCHAR(50) NOT NULL COMMENT '提醒类型：monthly_fortune, weekly_fortune, daily_test等',
  `reminder_title` VARCHAR(200) NOT NULL COMMENT '提醒标题',
  `reminder_desc` VARCHAR(500) COMMENT '提醒描述',
  `reminder_date` DATE NOT NULL COMMENT '提醒日期',
  `reminder_time` TIME COMMENT '提醒时间',
  `is_sent` TINYINT DEFAULT 0 COMMENT '是否已发送：0否，1是',
  `sent_at` DATETIME COMMENT '发送时间',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0否，1是',
  `related_record_id` BIGINT COMMENT '关联的记录ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_date` (`user_id`, `reminder_date`),
  INDEX `idx_sent` (`is_sent`, `reminder_date`),
  CONSTRAINT `fk_reminder_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复测算提醒表';

-- 6. 八字合盘记录表（扩展）
CREATE TABLE IF NOT EXISTS `tb_bazi_compatibility` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `partner_type` VARCHAR(20) NOT NULL COMMENT '合盘对象类型：friend, lover, spouse等',
  `partner_name` VARCHAR(100) COMMENT '合盘对象姓名',
  `user_bazi` VARCHAR(50) NOT NULL COMMENT '用户八字',
  `partner_bazi` VARCHAR(50) NOT NULL COMMENT '合盘对象八字',
  `compatibility_score` INT COMMENT '契合度评分：0-100',
  `compatibility_data` JSON COMMENT '契合度详细数据',
  `visualization_data` JSON COMMENT '可视化数据',
  `ai_analysis` TEXT COMMENT 'AI分析',
  `suggestion` TEXT COMMENT '相处建议',
  `is_shared` TINYINT DEFAULT 0 COMMENT '是否分享：0否，1是',
  `share_url` VARCHAR(500) COMMENT '分享链接',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_shared` (`is_shared`),
  CONSTRAINT `fk_compatibility_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='八字合盘记录表';

-- 插入示例数据（可选）
-- INSERT INTO `tb_daily_test` (`user_id`, `test_type`, `test_date`, `question`, `summary`) VALUES
-- (1, 'bazi_fortune', CURDATE(), '今日运势如何？', '今日运势良好');
