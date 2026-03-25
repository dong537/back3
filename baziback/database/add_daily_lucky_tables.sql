-- 每日幸运功能数据库表结构
-- 用于存储每日的幸运数字、颜色、星座、食物以及宜忌事项

USE `bazi`;

-- ============================================
-- 每日幸运表
-- ============================================
CREATE TABLE IF NOT EXISTS `tb_daily_lucky` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `lucky_date` DATE NOT NULL COMMENT '日期',
    `lucky_number` VARCHAR(50) NOT NULL COMMENT '幸运数字',
    `lucky_color` VARCHAR(50) NOT NULL COMMENT '幸运颜色',
    `lucky_constellation` VARCHAR(50) NOT NULL COMMENT '幸运星座',
    `lucky_food` VARCHAR(100) NOT NULL COMMENT '幸运食物',
    `suitable_actions` TEXT NOT NULL COMMENT '宜：适合做的事情（多个用空格或逗号分隔）',
    `unsuitable_actions` TEXT NOT NULL COMMENT '忌：不适合做的事情（多个用空格或逗号分隔）',
    `description` TEXT NULL COMMENT '描述或备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lucky_date` (`lucky_date`),
    KEY `idx_lucky_date` (`lucky_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日幸运表';

-- ============================================
-- 插入测试数据
-- ============================================

-- 插入未来30天的测试数据
-- 使用 INSERT IGNORE 避免重复插入错误
INSERT IGNORE INTO `tb_daily_lucky` (`lucky_date`, `lucky_number`, `lucky_color`, `lucky_constellation`, `lucky_food`, `suitable_actions`, `unsuitable_actions`, `description`) VALUES
-- 今天
(CURDATE(), '4', '葡萄紫', '金牛座', '红烧排骨', '散步 复盘总结 做计划', '情绪对抗 情绪逃避', '今日运势平稳，适合规划未来'),

-- 明天
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '7', '天空蓝', '双子座', '清蒸鱼', '学习新技能 社交聚会 创意工作', '拖延症 过度消费', '明日运势上升，适合拓展人脉'),

-- 后天
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '9', '翡翠绿', '天秤座', '白切鸡', '决策重要事项 签订合同 团队合作', '冲动决定 争吵', '后日运势佳，适合重要决策'),

-- 第4天
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '3', '玫瑰红', '狮子座', '糖醋里脊', '展示才华 领导团队 公开演讲', '过度自信 忽视细节', '运势旺盛，适合展现自我'),

-- 第5天
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '6', '柠檬黄', '处女座', '麻婆豆腐', '整理收纳 细节优化 健康检查', '完美主义 过度挑剔', '适合处理细节工作'),

-- 第6天
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '8', '深海蓝', '天蝎座', '宫保鸡丁', '深度思考 研究学习 秘密计划', '猜疑他人 过度保密', '适合深度思考和规划'),

-- 第7天
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '2', '樱花粉', '射手座', '水煮肉片', '旅行计划 探索新领域 乐观思考', '过度乐观 不切实际', '运势活跃，适合探索'),

-- 第8天
(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '5', '薄荷绿', '摩羯座', '回锅肉', '制定目标 努力工作 承担责任', '工作狂 忽视休息', '适合专注工作'),

-- 第9天
(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '1', '象牙白', '水瓶座', '鱼香肉丝', '创新思维 社交活动 独立行动', '特立独行 忽视他人', '适合创新和社交'),

-- 第10天
(DATE_ADD(CURDATE(), INTERVAL 9 DAY), '0', '薰衣草紫', '双鱼座', '蒜蓉西兰花', '艺术创作 情感表达 冥想放松', '过度敏感 逃避现实', '适合艺术和情感表达'),

-- 第11-20天
(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '4', '珊瑚橙', '白羊座', '干煸四季豆', '开始新项目 积极行动 勇敢尝试', '冲动行事 缺乏耐心', '适合开启新计划'),
(DATE_ADD(CURDATE(), INTERVAL 11 DAY), '7', '宝石蓝', '金牛座', '红烧肉', '稳定发展 享受生活 理财规划', '过度保守 拒绝变化', '适合稳定发展'),
(DATE_ADD(CURDATE(), INTERVAL 12 DAY), '9', '橄榄绿', '双子座', '口水鸡', '沟通交流 信息收集 灵活应变', '三心二意 浅尝辄止', '适合沟通和学习'),
(DATE_ADD(CURDATE(), INTERVAL 13 DAY), '3', '珍珠白', '巨蟹座', '蒸蛋羹', '家庭聚会 情感关怀 回忆过去', '过度怀旧 情绪化', '适合家庭和情感'),
(DATE_ADD(CURDATE(), INTERVAL 14 DAY), '6', '金色', '狮子座', '烤鸭', '展现魅力 娱乐活动 享受生活', '过度炫耀 自我中心', '适合展现魅力'),
(DATE_ADD(CURDATE(), INTERVAL 15 DAY), '8', '银灰色', '处女座', '白粥', '健康管理 细节完善 服务他人', '过度完美 吹毛求疵', '适合健康管理'),
(DATE_ADD(CURDATE(), INTERVAL 16 DAY), '2', '深紫色', '天秤座', '沙拉', '平衡关系 艺术欣赏 和谐相处', '优柔寡断 过度妥协', '适合平衡关系'),
(DATE_ADD(CURDATE(), INTERVAL 17 DAY), '5', '暗红色', '天蝎座', '麻辣烫', '深度探索 情感交流 神秘活动', '过度控制 猜忌', '适合深度探索'),
(DATE_ADD(CURDATE(), INTERVAL 18 DAY), '1', '天蓝色', '射手座', '烧烤', '冒险旅行 哲学思考 自由行动', '不负责任 缺乏承诺', '适合冒险探索'),
(DATE_ADD(CURDATE(), INTERVAL 19 DAY), '0', '土黄色', '摩羯座', '炖汤', '长期规划 承担责任 稳步前进', '过于严肃 缺乏乐趣', '适合长期规划'),

-- 第21-30天
(DATE_ADD(CURDATE(), INTERVAL 20 DAY), '4', '青草绿', '水瓶座', '凉拌菜', '创新实验 科技探索 人道主义', '脱离实际 过于理想', '适合创新实验'),
(DATE_ADD(CURDATE(), INTERVAL 21 DAY), '7', '海洋蓝', '双鱼座', '海鲜', '艺术创作 直觉判断 情感疗愈', '过度幻想 逃避责任', '适合艺术创作'),
(DATE_ADD(CURDATE(), INTERVAL 22 DAY), '9', '火焰红', '白羊座', '火锅', '积极行动 竞争比赛 快速决策', '过于急躁 缺乏思考', '适合积极行动'),
(DATE_ADD(CURDATE(), INTERVAL 23 DAY), '3', '大地棕', '金牛座', '牛排', '物质享受 稳定投资 慢节奏生活', '过于固执 拒绝改变', '适合稳定投资'),
(DATE_ADD(CURDATE(), INTERVAL 24 DAY), '6', '彩虹色', '双子座', '寿司', '多元学习 社交网络 信息分享', '注意力分散 缺乏深度', '适合多元学习'),
(DATE_ADD(CURDATE(), INTERVAL 25 DAY), '8', '月光银', '巨蟹座', '家常菜', '情感交流 家庭时光 温馨回忆', '过度敏感 情绪波动', '适合情感交流'),
(DATE_ADD(CURDATE(), INTERVAL 26 DAY), '2', '阳光金', '狮子座', '大餐', '领导团队 表演展示 享受赞美', '过度自信 忽视他人', '适合领导展示'),
(DATE_ADD(CURDATE(), INTERVAL 27 DAY), '5', '森林绿', '处女座', '轻食', '健康饮食 细节管理 服务精神', '过度挑剔 完美主义', '适合健康管理'),
(DATE_ADD(CURDATE(), INTERVAL 28 DAY), '1', '玫瑰金', '天秤座', '精致料理', '优雅社交 艺术欣赏 和谐平衡', '优柔寡断 缺乏主见', '适合优雅社交'),
(DATE_ADD(CURDATE(), INTERVAL 29 DAY), '0', '星空黑', '天蝎座', '暗黑料理', '深度思考 秘密计划 情感探索', '过度神秘 控制欲强', '适合深度思考');

-- 注意：使用 INSERT IGNORE 可以避免重复插入错误
-- 如果某日期的数据已存在，则不会插入新数据
