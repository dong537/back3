-- 检查今日运势详情数据
USE `bazi`;

-- 1. 检查今日是否有数据
SELECT 
    COUNT(*) AS today_count,
    CASE 
        WHEN COUNT(*) > 0 THEN '今日有数据'
        ELSE '今日无数据'
    END AS status
FROM `tb_daily_fortune_detail`
WHERE `fortune_date` = CURDATE();

-- 2. 查看今日数据详情
SELECT 
    id,
    fortune_date,
    love_score,
    career_score,
    wealth_score,
    health_score,
    study_score,
    relationship_score,
    lucky_color,
    lucky_number,
    lucky_direction,
    lucky_time,
    LEFT(love_analysis, 50) AS love_analysis_preview,
    LEFT(overall_advice, 50) AS overall_advice_preview
FROM `tb_daily_fortune_detail`
WHERE `fortune_date` = CURDATE()
LIMIT 1;

-- 3. 如果没有今日数据，查看最近的数据
SELECT 
    fortune_date,
    COUNT(*) AS count
FROM `tb_daily_fortune_detail`
GROUP BY fortune_date
ORDER BY fortune_date DESC
LIMIT 10;

-- 4. 如果今日无数据，可以手动插入一条（使用SQL脚本中的数据）
-- 或者执行完整SQL脚本：database/daily_fortune_detail_extended.sql
