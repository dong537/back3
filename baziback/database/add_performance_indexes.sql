-- 性能优化：添加数据库索引
-- 执行时间: 2026-01-15
-- 用于优化查询性能

USE bazi;

-- ==================== 帖子表索引 ====================
-- 用户帖子查询优化
CREATE INDEX IF NOT EXISTS idx_post_user_created ON tb_post(user_id, created_at DESC);

-- 分类帖子查询优化
CREATE INDEX IF NOT EXISTS idx_post_category_created ON tb_post(category, created_at DESC);

-- 删除状态查询优化
CREATE INDEX IF NOT EXISTS idx_post_deleted_created ON tb_post(deleted, created_at DESC);

-- ==================== 评论表索引 ====================
-- 帖子评论查询优化
CREATE INDEX IF NOT EXISTS idx_comment_post_created ON tb_comment(post_id, created_at DESC);

-- 用户评论查询优化
CREATE INDEX IF NOT EXISTS idx_comment_user_created ON tb_comment(user_id, created_at DESC);

-- ==================== 收藏表索引 ====================
-- 用户收藏查询优化
CREATE INDEX IF NOT EXISTS idx_favorite_user_type ON tb_user_favorite(user_id, type, data_id);

-- 收藏类型查询优化
CREATE INDEX IF NOT EXISTS idx_favorite_type_created ON tb_user_favorite(type, created_at DESC);

-- ==================== 测算记录表索引 ====================
-- 用户记录查询优化
CREATE INDEX IF NOT EXISTS idx_record_user_created ON tb_calculation_record(user_id, created_at DESC);

-- 记录类型查询优化
CREATE INDEX IF NOT EXISTS idx_record_type_created ON tb_calculation_record(record_type, created_at DESC);

-- ==================== 点赞表索引 ====================
-- 用户点赞查询优化
CREATE INDEX IF NOT EXISTS idx_like_user_target ON tb_like(user_id, target_type, target_id);

-- 目标点赞查询优化
CREATE INDEX IF NOT EXISTS idx_like_target ON tb_like(target_type, target_id);

-- ==================== 通知表索引 ====================
-- 用户通知查询优化
CREATE INDEX IF NOT EXISTS idx_notification_user_read ON tb_notification(user_id, is_read, created_at DESC);

-- ==================== 用户表索引 ====================
-- 用户名查询优化
CREATE INDEX IF NOT EXISTS idx_user_username ON tb_user(username);

-- 邮箱查询优化
CREATE INDEX IF NOT EXISTS idx_user_email ON tb_user(email);

-- ==================== 签到表索引 ====================
-- 用户签到查询优化
CREATE INDEX IF NOT EXISTS idx_checkin_user_date ON tb_user_checkin(user_id, checkin_date DESC);

-- ==================== 成就表索引 ====================
-- 用户成就查询优化
CREATE INDEX IF NOT EXISTS idx_achievement_user ON tb_user_achievement(user_id, achievement_id);

-- ==================== 查看现有索引 ====================
-- 执行以下命令查看表的所有索引
-- SHOW INDEX FROM tb_post;
-- SHOW INDEX FROM tb_comment;
-- SHOW INDEX FROM tb_user_favorite;
-- SHOW INDEX FROM tb_calculation_record;

-- ==================== 分析查询性能 ====================
-- 执行以下命令分析查询性能
-- EXPLAIN SELECT * FROM tb_post WHERE user_id = 123 ORDER BY created_at DESC LIMIT 10;
-- EXPLAIN SELECT * FROM tb_comment WHERE post_id = 456 ORDER BY created_at DESC LIMIT 20;
-- EXPLAIN SELECT * FROM tb_user_favorite WHERE user_id = 789 AND type = 'tarot';

-- ==================== 优化建议 ====================
-- 1. 定期运行 ANALYZE TABLE 更新统计信息
-- ANALYZE TABLE tb_post;
-- ANALYZE TABLE tb_comment;
-- ANALYZE TABLE tb_user_favorite;

-- 2. 定期运行 OPTIMIZE TABLE 整理表空间
-- OPTIMIZE TABLE tb_post;
-- OPTIMIZE TABLE tb_comment;
-- OPTIMIZE TABLE tb_user_favorite;

-- 3. 监控慢查询日志
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 0.5;
