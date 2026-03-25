-- 积分兑换系统数据库表结构
-- 用于支持用户使用积分兑换商品和服务

USE `bazi`;

-- ============================================
-- 1. 兑换商品表
-- ============================================

CREATE TABLE IF NOT EXISTS `tb_exchange_product` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品代码（唯一）',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_type` VARCHAR(50) NOT NULL COMMENT '商品类型：divination_count-占卜次数，vip_days-VIP天数，feature-功能解锁',
    `product_description` VARCHAR(255) NULL COMMENT '商品描述',
    `points_cost` INT NOT NULL COMMENT '所需积分',
    `product_value` INT NOT NULL COMMENT '商品价值（如：占卜次数、VIP天数等）',
    `icon_url` VARCHAR(255) NULL COMMENT '商品图标URL',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `stock_limit` INT NULL COMMENT '库存限制（NULL表示无限制）',
    `daily_limit` INT NULL COMMENT '每日兑换限制（NULL表示无限制）',
    `user_limit` INT NULL COMMENT '每用户兑换限制（NULL表示无限制）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`product_code`),
    KEY `idx_product_type` (`product_type`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换商品表';

-- 初始化兑换商品数据
INSERT INTO `tb_exchange_product` 
(`product_code`, `product_name`, `product_type`, `product_description`, `points_cost`, `product_value`, `sort_order`) VALUES
('divination_1', '单次占卜', 'divination_count', '兑换1次免费占卜', 10, 1, 1),
('divination_5', '5次占卜包', 'divination_count', '兑换5次免费占卜', 45, 5, 2),
('divination_10', '10次占卜包', 'divination_count', '兑换10次免费占卜', 80, 10, 3),
('vip_7', '7天VIP', 'vip_days', '兑换7天VIP会员', 100, 7, 10),
('vip_30', '30天VIP', 'vip_days', '兑换30天VIP会员', 350, 30, 11),
('ai_unlimited', 'AI无限次', 'feature', '解锁AI对话无限次使用（7天）', 200, 7, 20),
('premium_report', '高级报告', 'feature', '解锁高级占卜报告功能（1次）', 50, 1, 21);

-- ============================================
-- 2. 兑换记录表
-- ============================================

CREATE TABLE IF NOT EXISTS `tb_exchange_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品代码',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `points_cost` INT NOT NULL COMMENT '消耗积分',
    `product_value` INT NOT NULL COMMENT '商品价值',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待发放，1-已发放，2-已使用，3-已过期',
    `expire_time` DATETIME NULL COMMENT '过期时间（NULL表示永不过期）',
    `used_time` DATETIME NULL COMMENT '使用时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_product_code` (`product_code`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换记录表';

-- ============================================
-- 3. 用户VIP记录表
-- ============================================

CREATE TABLE IF NOT EXISTS `tb_user_vip` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `vip_type` VARCHAR(50) NOT NULL DEFAULT 'NORMAL' COMMENT 'VIP类型：NORMAL-普通，VIP-VIP会员',
    `start_time` DATETIME NOT NULL COMMENT 'VIP开始时间',
    `end_time` DATETIME NOT NULL COMMENT 'VIP结束时间',
    `source` VARCHAR(50) NOT NULL COMMENT '来源：exchange-兑换，recharge-充值，gift-赠送',
    `source_id` BIGINT(20) NULL COMMENT '来源ID（如兑换记录ID、订单ID等）',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效：0-无效，1-有效',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_vip_type` (`vip_type`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户VIP记录表';

-- ============================================
-- 4. 用户功能解锁记录表
-- ============================================

CREATE TABLE IF NOT EXISTS `tb_user_feature_unlock` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `feature_code` VARCHAR(50) NOT NULL COMMENT '功能代码',
    `feature_name` VARCHAR(100) NOT NULL COMMENT '功能名称',
    `unlock_type` VARCHAR(50) NOT NULL COMMENT '解锁类型：exchange-兑换，vip-VIP，permanent-永久',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NULL COMMENT '结束时间（NULL表示永久）',
    `source` VARCHAR(50) NOT NULL COMMENT '来源',
    `source_id` BIGINT(20) NULL COMMENT '来源ID',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_feature` (`user_id`, `feature_code`, `is_active`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_feature_code` (`feature_code`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户功能解锁记录表';

SELECT '积分兑换系统数据库表创建完成！' AS message;
