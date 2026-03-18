-- ============================================
-- 创建每日塔罗牌抽牌记录表
-- ============================================
-- 如果表不存在则创建，如果存在则跳过
-- 执行方法：在MySQL客户端中执行此SQL文件

USE `bazi`;

-- 检查表是否存在，如果不存在则创建
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

-- 验证表是否创建成功
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM 
    INFORMATION_SCHEMA.TABLES 
WHERE 
    TABLE_SCHEMA = 'bazi' 
    AND TABLE_NAME = 'tb_tarot_daily_draw';
