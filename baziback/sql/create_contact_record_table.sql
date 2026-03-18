-- ============================================
-- 创建联系方式记录表
-- ============================================
-- 用于记录用户通过微信等联系方式联系的行为
-- ============================================

CREATE TABLE IF NOT EXISTS `tb_contact_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `contact_type` VARCHAR(50) NOT NULL DEFAULT 'wechat' COMMENT '联系方式类型：wechat, phone, email等',
  `contact_name` VARCHAR(100) DEFAULT NULL COMMENT '联系人姓名',
  `contact_info` VARCHAR(200) DEFAULT NULL COMMENT '联系信息（微信ID、手机号、邮箱等）',
  `source_page` VARCHAR(200) DEFAULT NULL COMMENT '来源页面（如：/yijing, /tarot等）',
  `source_type` VARCHAR(50) DEFAULT NULL COMMENT '来源类型（如：divination_result, homepage等）',
  `related_record_id` BIGINT DEFAULT NULL COMMENT '关联的记录ID（如占卜记录ID）',
  `action_type` VARCHAR(50) DEFAULT 'view' COMMENT '操作类型：view（查看）, click（点击）, scan（扫码）',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_contact_type` (`contact_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_source_page` (`source_page`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='联系方式记录表';

-- ============================================
-- 表创建完成
-- ============================================
