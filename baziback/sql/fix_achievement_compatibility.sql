-- Normalize divination achievement codes so old and new code paths behave consistently.
-- Safe to run multiple times.

START TRANSACTION;

UPDATE tb_achievement
SET achievement_code = 'divination_10',
    achievement_name = '占卜大师',
    achievement_description = '完成10次占卜',
    points_reward = 100,
    sort_order = 2
WHERE achievement_code = 'divination_master'
  AND NOT EXISTS (
      SELECT 1
      FROM (
          SELECT achievement_code
          FROM tb_achievement
          WHERE achievement_code = 'divination_10'
      ) AS existing_divination_10
  );

UPDATE tb_achievement
SET achievement_code = 'divination_50',
    achievement_name = '占卜专家',
    achievement_description = '完成50次占卜',
    points_reward = 200,
    sort_order = 3
WHERE achievement_code = 'divination_expert'
  AND NOT EXISTS (
      SELECT 1
      FROM (
          SELECT achievement_code
          FROM tb_achievement
          WHERE achievement_code = 'divination_50'
      ) AS existing_divination_50
  );

INSERT INTO tb_achievement (
    achievement_code,
    achievement_name,
    achievement_description,
    achievement_type,
    points_reward,
    sort_order,
    is_active
)
SELECT
    'divination_100',
    '通灵宗师',
    '完成100次占卜',
    'divination',
    500,
    4,
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_achievement
    WHERE achievement_code = 'divination_100'
);

UPDATE tb_user_achievement ua
JOIN tb_achievement a ON a.id = ua.achievement_id
SET ua.achievement_code = a.achievement_code
WHERE ua.achievement_code <> a.achievement_code;

COMMIT;
