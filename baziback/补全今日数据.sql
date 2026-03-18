-- 补充今日（2026-01-14）的运势数据
USE `bazi`;

-- 先删除可能存在的旧的或不完整的今日数据，防止重复插入
DELETE FROM `tb_daily_fortune_detail` WHERE `fortune_date` = '2026-01-14';

-- 插入详细的今日运势
INSERT INTO `tb_daily_fortune_detail` (
    `fortune_date`, `love_score`, `love_analysis`, `career_score`, `career_analysis`,
    `wealth_score`, `wealth_analysis`, `health_score`, `health_analysis`,
    `study_score`, `study_analysis`, `relationship_score`, `relationship_analysis`,
    `lucky_color`, `lucky_number`, `lucky_direction`, `lucky_time`,
    `suitable_actions`, `unsuitable_actions`, `overall_advice`, `keywords`
) VALUES
('2026-01-14',
 85, -- love_score
 '今日感情运势非常不错，单身者桃花运旺盛，容易在社交场合或工作环境中遇到心仪对象。已有伴侣者感情更加甜蜜，适合与伴侣一起规划未来，增进彼此了解。建议主动表达内心想法，增进感情深度。',
 78, -- career_score
 '工作方面进展顺利，适合制定新的计划和目标。与同事的协作关系良好，团队氛围融洽。注意把握机会，展现自己的能力，可能会得到上司的认可。',
 82, -- wealth_score
 '财运稳定上升，适合理性投资和理财规划。避免冲动消费，保持理性判断。可能会有意外的收入机会，比如奖金或投资收益。',
 83, -- health_score
 '身体状况良好，精力充沛。注意规律作息，适当运动有益健康。保持心情愉悦，避免过度劳累。适合进行户外运动，如散步、慢跑或瑜伽。',
 88, -- study_score
 '学习状态极佳，适合学习新知识和技能。记忆力增强，理解能力提升，能够快速掌握新内容。制定学习计划，把握黄金学习时间。',
 80, -- relationship_score
 '人际关系和谐，适合参加社交活动，拓展人脉。与朋友交流愉快，可能会遇到贵人相助。适合主动联系久未联系的朋友，增进友谊。',
 '玫瑰红', -- lucky_color
 '7', -- lucky_number
 '东南方', -- lucky_direction
 '上午9-11点', -- lucky_time
 '["保持积极心态", "规划未来", "与他人交流", "学习新知识", "制定理财计划", "户外运动"]', -- suitable_actions
 '["过度焦虑", "冲动决策", "忽视健康", "熬夜", "冲动消费", "忽视细节"]', -- unsuitable_actions
 '今日运势整体良好，各方面都有不错的表现。这是一个充满活力的开始，保持乐观心态，把握机会，会有不错的收获。', -- overall_advice
 '积极,规划,交流,活力,机会' -- keywords
);
