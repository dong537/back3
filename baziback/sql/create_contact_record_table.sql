-- Contact interaction record table.
-- Supports anonymous visitors, so `user_id` must remain nullable.

CREATE TABLE IF NOT EXISTS `tb_contact_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` BIGINT NULL COMMENT 'User id, nullable for guests',
  `contact_type` VARCHAR(50) NOT NULL DEFAULT 'wechat' COMMENT 'wechat, phone, email',
  `contact_name` VARCHAR(100) DEFAULT NULL COMMENT 'Display name',
  `contact_info` VARCHAR(200) DEFAULT NULL COMMENT 'Contact detail',
  `source_page` VARCHAR(200) DEFAULT NULL COMMENT 'Source page path',
  `source_type` VARCHAR(50) DEFAULT NULL COMMENT 'Source module type',
  `related_record_id` BIGINT DEFAULT NULL COMMENT 'Related business record id',
  `action_type` VARCHAR(50) NOT NULL DEFAULT 'view' COMMENT 'view, click, scan',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'Client IP address',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'Client user agent',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  KEY `idx_contact_user_id` (`user_id`),
  KEY `idx_contact_type` (`contact_type`),
  KEY `idx_contact_create_time` (`create_time`),
  KEY `idx_contact_source_page` (`source_page`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Contact interaction record';
