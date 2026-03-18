-- 成就系统：用户成就唯一约束（防止并发重复解锁/重复发积分）
USE `bazi`;

ALTER TABLE tb_user_achievement
  ADD UNIQUE KEY uk_user_achievement (user_id, achievement_code);
