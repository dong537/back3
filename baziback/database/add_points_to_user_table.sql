-- 在用户表中添加积分字段
-- 如果字段已存在则不会重复添加

USE `bazi`;

-- 检查并添加积分字段
DELIMITER $$

DROP PROCEDURE IF EXISTS `add_points_to_user_table`$$
CREATE PROCEDURE `add_points_to_user_table`()
BEGIN
    -- 检查并添加 current_points 字段（当前积分余额）
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'current_points'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `current_points` INT NOT NULL DEFAULT 0 COMMENT '当前积分余额' AFTER `avatar`;
    END IF;

    -- 检查并添加 total_points 字段（累计获得积分）
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'total_points'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `total_points` INT NOT NULL DEFAULT 0 COMMENT '累计获得积分' AFTER `current_points`;
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL `add_points_to_user_table`();

-- 删除存储过程
DROP PROCEDURE IF EXISTS `add_points_to_user_table`;

-- 为现有用户初始化积分（如果字段刚添加）
UPDATE `tb_user` SET `current_points` = COALESCE(`current_points`, 0), `total_points` = COALESCE(`total_points`, 0);
