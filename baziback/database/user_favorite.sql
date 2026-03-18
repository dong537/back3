-- 用户收藏表
CREATE TABLE IF NOT EXISTS `tb_user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `favorite_type` VARCHAR(50) NOT NULL COMMENT '收藏类型 (tarot, yijing, bazi, etc.)',
  `data_id` VARCHAR(255) COMMENT '数据唯一标识 (例如：塔罗牌ID、卦象ID)',
  `title` VARCHAR(255) NOT NULL COMMENT '收藏标题',
  `summary` TEXT COMMENT '收藏摘要',
  `data` JSON COMMENT '原始数据 (用于恢复详情)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id_type` (`user_id`, `favorite_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';
