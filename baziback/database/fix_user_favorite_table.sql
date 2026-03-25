DROP TABLE IF EXISTS `tb_user_favorite`;
use bazi;
CREATE TABLE `tb_user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `favorite_type` VARCHAR(50) NOT NULL,
  `data_id` VARCHAR(255),
  `title` VARCHAR(255) NOT NULL,
  `summary` TEXT,
  `data` LONGTEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id_type` (`user_id`, `favorite_type`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
