-- 每日塔罗牌抽牌记录表
-- 记录每个用户每天的抽牌记录，确保每天只能抽一次

USE `bazi`;

-- ============================================
-- 每日塔罗牌抽牌记录表
-- ============================================
-- 如果表已存在则删除（可选，用于重新创建）
-- DROP TABLE IF EXISTS `tb_tarot_daily_draw`;

CREATE TABLE IF NOT EXISTS `tb_tarot_daily_draw` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `draw_date` DATE NOT NULL COMMENT '抽牌日期',
    `card_id` TINYINT UNSIGNED NOT NULL COMMENT '抽到的塔罗牌ID',
    `is_reversed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否逆位 (0=正位, 1=逆位)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `draw_date`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_draw_date` (`draw_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户每日塔罗牌抽牌记录表';
