-- ============================================
-- 减少成就积分奖励SQL脚本
-- ============================================
-- 说明：适当减少完成成就后获得的积分
-- 执行前请先备份数据库！
-- ============================================

-- 备份成就表（可选，但强烈推荐）
-- CREATE TABLE tb_achievement_backup_20260117 AS SELECT * FROM tb_achievement;

-- ============================================
-- 更新占卜相关成就的积分奖励
-- ============================================
use bazi;
-- 更新"第一次占卜"成就积分：20 -> 10
UPDATE tb_achievement 
SET points_reward = 10,
    update_time = NOW()
WHERE achievement_code = 'first_divination';

-- 更新"占卜10次"成就积分：50 -> 30
UPDATE tb_achievement 
SET points_reward = 30,
    update_time = NOW()
WHERE achievement_code = 'divination_10';

-- 更新"占卜50次"成就积分：200 -> 100
UPDATE tb_achievement 
SET points_reward = 100,
    update_time = NOW()
WHERE achievement_code = 'divination_50';

-- ============================================
-- 验证更新结果
-- ============================================
SELECT 
    achievement_code,
    achievement_name,
    points_reward,
    update_time
FROM tb_achievement 
WHERE achievement_code IN ('first_divination', 'divination_10', 'divination_50')
ORDER BY sort_order;

-- ============================================
-- 更新完成！
-- ============================================
-- 调整后的积分奖励：
-- - 第一次占卜: 10积分 (原20积分)
-- - 占卜10次: 30积分 (原50积分)
-- - 占卜50次: 100积分 (原200积分)
-- ============================================
