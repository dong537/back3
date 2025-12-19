-- 参天AI用户管理系统 - 充值与积分功能数据库脚本

USE `bazi`;

-- 用户积分/余额表
CREATE TABLE IF NOT EXISTS `tb_credit` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID，关联tb_user.id',
    `balance` INT NOT NULL DEFAULT 0 COMMENT '当前积分余额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分余额表';

-- 充值订单表
CREATE TABLE IF NOT EXISTS `tb_recharge_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '系统唯一订单号',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额（单位：元）',
    `credits_to_add` INT NOT NULL COMMENT '本次充值获得的积分数量',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付, 1-已完成, 2-已失败, 3-已取消',
    `payment_method` VARCHAR(50) NULL COMMENT '支付方式，如alipay, wechat_pay',
    `third_party_txn_id` VARCHAR(128) NULL COMMENT '第三方支付网关的流水号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    `payment_time` DATETIME NULL COMMENT '支付确认时间',
    `completion_time` DATETIME NULL COMMENT '订单完成时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值订单表';

-- 积分流水表
CREATE TABLE IF NOT EXISTS `tb_credit_transaction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `transaction_type` TINYINT(1) NOT NULL COMMENT '流水类型：1-充值, 2-消费, 3-退款, 4-系统调整',
    `amount` INT NOT NULL COMMENT '变动积分数（正数为增加，负数为减少）',
    `balance_before` INT NOT NULL COMMENT '变动前余额',
    `balance_after` INT NOT NULL COMMENT '变动后余额',
    `related_order_id` BIGINT(20) NULL COMMENT '关联的充值订单ID',
    `description` VARCHAR(255) NULL COMMENT '流水描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_transaction_type` (`transaction_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';
