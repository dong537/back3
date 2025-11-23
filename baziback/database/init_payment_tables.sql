-- 支付宝支付系统 - 数据库初始化脚本

USE `bazi`;

-- 订单表
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `order_type` TINYINT(2) NOT NULL COMMENT '订单类型：1-普通支付，2-会员购买',
    `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `product_desc` VARCHAR(500) NULL COMMENT '商品描述',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已退款',
    `trade_no` VARCHAR(64) NULL COMMENT '支付宝交易号',
    `pay_time` DATETIME NULL COMMENT '支付时间',
    `cancel_time` DATETIME NULL COMMENT '取消时间',
    `refund_time` DATETIME NULL COMMENT '退款时间',
    `expire_time` DATETIME NOT NULL COMMENT '订单过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_trade_no` (`trade_no`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 会员表
DROP TABLE IF EXISTS `tb_membership`;
CREATE TABLE `tb_membership` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '会员ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `membership_type` TINYINT(2) NOT NULL COMMENT '会员类型：1-月度会员，2-季度会员，3-年度会员',
    `start_time` DATETIME NOT NULL COMMENT '会员开始时间',
    `end_time` DATETIME NOT NULL COMMENT '会员结束时间',
    `status` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '会员状态：0-已过期，1-正常',
    `order_id` BIGINT(20) NOT NULL COMMENT '关联订单ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_status` (`status`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- 支付日志表
DROP TABLE IF EXISTS `tb_payment_log`;
CREATE TABLE `tb_payment_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `trade_no` VARCHAR(64) NULL COMMENT '支付宝交易号',
    `notify_type` VARCHAR(50) NULL COMMENT '通知类型',
    `notify_data` TEXT NULL COMMENT '通知数据',
    `notify_time` DATETIME NULL COMMENT '通知时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_trade_no` (`trade_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付日志表';

-- 会员套餐配置表
DROP TABLE IF EXISTS `tb_membership_package`;
CREATE TABLE `tb_membership_package` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `package_name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `package_type` TINYINT(2) NOT NULL COMMENT '套餐类型：1-月度会员，2-季度会员，3-年度会员',
    `duration_days` INT(11) NOT NULL COMMENT '有效天数',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `sale_price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `description` VARCHAR(500) NULL COMMENT '套餐描述',
    `features` TEXT NULL COMMENT '套餐特权（JSON格式）',
    `status` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_package_type` (`package_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员套餐配置表';

-- 插入默认会员套餐
INSERT INTO `tb_membership_package` (`package_name`, `package_type`, `duration_days`, `original_price`, `sale_price`, `description`, `features`, `status`, `sort_order`) 
VALUES 
    ('月度会员', 1, 30, 99.00, 88.00, '享受一个月的会员特权', '["无限次算命", "专属客服", "优先体验新功能"]', 1, 1),
    ('季度会员', 2, 90, 297.00, 238.00, '享受三个月的会员特权，更优惠', '["无限次算命", "专属客服", "优先体验新功能", "季度专属报告"]', 1, 2),
    ('年度会员', 3, 365, 1188.00, 888.00, '享受一年的会员特权，最划算', '["无限次算命", "专属客服", "优先体验新功能", "年度专属报告", "生日专属礼物"]', 1, 3);
