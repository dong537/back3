-- 为用户表添加 OAuth 支持字段
-- 支持 AgentPit OAuth2 授权登录

USE `bazi`;

-- 添加 oauth_provider 字段（如果不存在）
ALTER TABLE `tb_user`
    ADD COLUMN IF NOT EXISTS `oauth_provider` VARCHAR(50) NULL COMMENT 'OAuth提供商：agentpit',
    ADD COLUMN IF NOT EXISTS `oauth_id` VARCHAR(200) NULL COMMENT 'OAuth提供商用户ID';

-- 允许 oauth 用户的 password 为空（OAuth用户不需要密码）
ALTER TABLE `tb_user`
    MODIFY COLUMN `password` VARCHAR(255) NULL COMMENT '密码（加密，OAuth用户可为空）';

-- 添加 oauth 联合唯一索引
ALTER TABLE `tb_user`
    ADD UNIQUE KEY IF NOT EXISTS `uk_oauth` (`oauth_provider`, `oauth_id`);

-- 允许 username 对 OAuth 用户自动生成（保留原有唯一约束）
