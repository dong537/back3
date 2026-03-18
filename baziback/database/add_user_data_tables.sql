-- 用户数据相关表
-- 执行时间: 2026-01-13
-- 用于存储用户历史记录、收藏、积分、签到等数据
use bazi;

-- 1. 用户历史记录表（占卜历史）
CREATE TABLE IF NOT EXISTS tb_user_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '类型: yijing/tarot/bazi/zodiac',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '内容摘要',
    data JSON COMMENT '完整数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户历史记录表';

-- 2. 用户收藏表
CREATE TABLE IF NOT EXISTS tb_user_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '类型: yijing/tarot/bazi/zodiac/post',
    data_id VARCHAR(100) COMMENT '数据ID',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '内容摘要',
    data JSON COMMENT '完整数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_type_data (user_id, type, data_id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 3. 用户积分表
CREATE TABLE IF NOT EXISTS tb_user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_points INT DEFAULT 0 COMMENT '总积分',
    available_points INT DEFAULT 0 COMMENT '可用积分',
    used_points INT DEFAULT 0 COMMENT '已使用积分',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

-- 4. 积分变动历史表
CREATE TABLE IF NOT EXISTS tb_points_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '类型: earn/spend',
    amount INT NOT NULL COMMENT '变动数量',
    reason VARCHAR(200) COMMENT '变动原因',
    balance INT COMMENT '变动后余额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分变动历史表';

-- 5. 用户签到表
CREATE TABLE IF NOT EXISTS tb_user_checkin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_date DATE NOT NULL COMMENT '签到日期',
    streak INT DEFAULT 1 COMMENT '连续签到天数',
    reward INT DEFAULT 10 COMMENT '获得积分',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, checkin_date),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到表';

-- 6. 用户成就表
CREATE TABLE IF NOT EXISTS tb_user_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    achievement_id VARCHAR(50) NOT NULL COMMENT '成就ID',
    name VARCHAR(100) NOT NULL COMMENT '成就名称',
    description VARCHAR(500) COMMENT '成就描述',
    reward INT DEFAULT 0 COMMENT '奖励积分',
    unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    UNIQUE KEY uk_user_achievement (user_id, achievement_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- 7. 用户设置表
CREATE TABLE IF NOT EXISTS tb_user_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    theme VARCHAR(20) DEFAULT 'auto' COMMENT '主题: auto/light/dark',
    language VARCHAR(20) DEFAULT 'zh-CN' COMMENT '语言',
    animations TINYINT(1) DEFAULT 1 COMMENT '是否开启动画',
    notifications TINYINT(1) DEFAULT 1 COMMENT '是否开启通知',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';
