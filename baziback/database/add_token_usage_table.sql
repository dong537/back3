-- Token 消耗上报表
-- 用于外部开发者的 Agent 应用在执行完成后将 token 消耗数据回传给平台
CREATE TABLE IF NOT EXISTS `tb_token_usage` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `agent_id` VARCHAR(255) NOT NULL COMMENT 'Agent ID',
    `application_id` VARCHAR(255) DEFAULT NULL COMMENT '应用 ID',
    `user_id` BIGINT NOT NULL COMMENT '上报用户 ID',
    `tokens_used` INT NOT NULL COMMENT '总 token 消耗量',
    `input_tokens` INT DEFAULT NULL COMMENT '输入 token 数',
    `output_tokens` INT DEFAULT NULL COMMENT '输出 token 数',
    `started_at` DATETIME NOT NULL COMMENT '调用开始时间',
    `ended_at` DATETIME NOT NULL COMMENT '调用结束时间',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT 'AI 模型名称',
    `request_id` VARCHAR(255) DEFAULT NULL COMMENT '请求追踪 ID',
    `metadata` JSON DEFAULT NULL COMMENT '扩展 JSON 数据',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_agent_id` (`agent_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_application_id` (`application_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token 消耗上报记录';
