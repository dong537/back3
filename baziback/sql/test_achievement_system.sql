-- ============================================
-- 成就系统测试SQL脚本
-- 用于排查占卜成就无法解锁的问题
-- ============================================

-- 1. 查看用户占卜记录统计（替换 YOUR_USER_ID 为实际用户ID）
SELECT 
    user_id,
    COUNT(*) as total_count,
    COUNT(CASE WHEN record_type = 'yijing' THEN 1 END) as yijing_count,
    COUNT(CASE WHEN record_type = 'tarot' THEN 1 END) as tarot_count,
    COUNT(CASE WHEN record_type = 'bazi' THEN 1 END) as bazi_count,
    MIN(create_time) as first_record_time,
    MAX(create_time) as last_record_time
FROM tb_calculation_record
WHERE user_id = 5  -- 替换为实际用户ID
GROUP BY user_id;

-- 2. 查看用户最近10条占卜记录
SELECT 
    id,
    user_id,
    record_type,
    record_title,
    question,
    create_time
FROM tb_calculation_record
WHERE user_id = 5  -- 替换为实际用户ID
ORDER BY create_time DESC
LIMIT 10;

-- 3. 查看用户已解锁的成就
SELECT 
    ua.id,
    ua.user_id,
    a.achievement_code,
    a.achievement_name,
    a.achievement_description,
    ua.points_earned,
    ua.unlocked_time
FROM tb_user_achievement ua
JOIN tb_achievement a ON ua.achievement_id = a.id
WHERE ua.user_id = 5  -- 替换为实际用户ID
ORDER BY ua.unlocked_time DESC;

-- 4. 检查占卜相关成就配置
SELECT 
    id,
    achievement_code,
    achievement_name,
    achievement_description,
    achievement_type,
    points_reward,
    is_active,
    sort_order
FROM tb_achievement
WHERE achievement_code IN ('first_divination', 'divination_10', 'divination_50', 'divination_100')
ORDER BY sort_order;

-- 5. 检查用户是否应该解锁某个成就（示例：检查是否应该解锁"占卜10次"）
SELECT 
    ua.user_id,
    COUNT(cr.id) as divination_count,
    CASE 
        WHEN COUNT(cr.id) >= 10 AND ua.id IS NULL THEN '应该解锁但未解锁'
        WHEN COUNT(cr.id) >= 10 AND ua.id IS NOT NULL THEN '已解锁'
        ELSE '未达到解锁条件'
    END as status
FROM tb_calculation_record cr
LEFT JOIN tb_user_achievement ua ON cr.user_id = ua.user_id 
    AND ua.achievement_code = 'divination_10'
LEFT JOIN tb_achievement a ON ua.achievement_id = a.id
WHERE cr.user_id = 5  -- 替换为实际用户ID
GROUP BY ua.user_id, ua.id;

-- 6. 查看所有用户的占卜次数和成就解锁情况
SELECT 
    cr.user_id,
    COUNT(cr.id) as divination_count,
    COUNT(DISTINCT CASE WHEN ua.achievement_code = 'first_divination' THEN ua.id END) as has_first,
    COUNT(DISTINCT CASE WHEN ua.achievement_code = 'divination_10' THEN ua.id END) as has_10,
    COUNT(DISTINCT CASE WHEN ua.achievement_code = 'divination_50' THEN ua.id END) as has_50
FROM tb_calculation_record cr
LEFT JOIN tb_user_achievement ua ON cr.user_id = ua.user_id
WHERE cr.user_id IS NOT NULL
GROUP BY cr.user_id
ORDER BY divination_count DESC;

-- ============================================
-- 手动触发成就检查（如果需要）
-- ============================================

-- 注意：以下SQL仅用于数据修复，正常情况下应该通过后端服务触发

-- 如果发现用户应该解锁某个成就但未解锁，可以手动插入（谨慎使用）
-- INSERT INTO tb_user_achievement (user_id, achievement_id, achievement_code, unlocked_time, points_earned)
-- SELECT 
--     5,  -- 用户ID
--     a.id,  -- 成就ID
--     'divination_10',  -- 成就代码
--     NOW(),  -- 解锁时间
--     a.points_reward  -- 积分奖励
-- FROM tb_achievement a
-- WHERE a.achievement_code = 'divination_10'
-- AND NOT EXISTS (
--     SELECT 1 FROM tb_user_achievement ua 
--     WHERE ua.user_id = 5 AND ua.achievement_code = 'divination_10'
-- );

-- ============================================
