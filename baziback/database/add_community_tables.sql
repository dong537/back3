-- 社区功能数据库表
-- 执行时间: 2026-01-12
use bazi;

-- 0. 为用户表添加level字段（使用存储过程安全添加）
DELIMITER $$
DROP PROCEDURE IF EXISTS add_level_column$$
CREATE PROCEDURE add_level_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'tb_user' 
        AND COLUMN_NAME = 'level'
    ) THEN
        ALTER TABLE `tb_user` ADD COLUMN `level` INT DEFAULT 1 COMMENT '用户等级';
    END IF;
END$$
DELIMITER ;
CALL add_level_column();
DROP PROCEDURE IF EXISTS add_level_column;

-- 1. 帖子/动态表
CREATE TABLE IF NOT EXISTS tb_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    content TEXT NOT NULL COMMENT '内容',
    title VARCHAR(200) COMMENT '标题(可选)',
    category VARCHAR(50) NOT NULL DEFAULT 'share' COMMENT '分类: share/question/discuss/tree_hole',
    images JSON COMMENT '图片URL列表',
    tags JSON COMMENT '标签列表',
    is_anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名',
    likes_count INT DEFAULT 0 COMMENT '点赞数',
    comments_count INT DEFAULT 0 COMMENT '评论数',
    shares_count INT DEFAULT 0 COMMENT '分享数',
    views_count INT DEFAULT 0 COMMENT '浏览数',
    is_top TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    is_hot TINYINT(1) DEFAULT 0 COMMENT '是否热门',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常 2-审核中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子/动态表';

-- 2. 评论表
CREATE TABLE IF NOT EXISTS tb_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID(回复)',
    reply_to_user_id BIGINT COMMENT '回复目标用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    likes_count INT DEFAULT 0 COMMENT '点赞数',
    is_anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    FOREIGN KEY (post_id) REFERENCES tb_post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 3. 点赞表
CREATE TABLE IF NOT EXISTS tb_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型: post/comment',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 4. 收藏表
CREATE TABLE IF NOT EXISTS tb_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (post_id) REFERENCES tb_post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 5. 关注表
CREATE TABLE IF NOT EXISTS tb_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL COMMENT '关注者ID',
    following_id BIGINT NOT NULL COMMENT '被关注者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, following_id),
    INDEX idx_follower (follower_id),
    INDEX idx_following (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 6. 话题表
CREATE TABLE IF NOT EXISTS tb_topic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '话题名称',
    description VARCHAR(500) COMMENT '话题描述',
    icon VARCHAR(50) COMMENT '图标emoji',
    color VARCHAR(20) COMMENT '颜色',
    posts_count INT DEFAULT 0 COMMENT '帖子数',
    followers_count INT DEFAULT 0 COMMENT '关注数',
    is_hot TINYINT(1) DEFAULT 0 COMMENT '是否热门',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

-- 7. 帖子话题关联表
CREATE TABLE IF NOT EXISTS tb_post_topic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    UNIQUE KEY uk_post_topic (post_id, topic_id),
    FOREIGN KEY (post_id) REFERENCES tb_post(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES tb_topic(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子话题关联表';

-- 8. 通知表
CREATE TABLE IF NOT EXISTS tb_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    from_user_id BIGINT COMMENT '发送用户ID',
    type VARCHAR(30) NOT NULL COMMENT '类型: like/comment/follow/mention/system',
    target_type VARCHAR(20) COMMENT '目标类型: post/comment',
    target_id BIGINT COMMENT '目标ID',
    content VARCHAR(500) COMMENT '通知内容',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 初始化热门话题数据（使用 INSERT IGNORE 避免重复插入报错）
INSERT IGNORE INTO tb_topic (name, description, icon, color, is_hot, sort_order) VALUES
('水逆期自救指南', '分享水逆期的应对方法和心得', '🌊', 'blue', 1, 1),
('塔罗牌入门', '塔罗牌学习交流', '🃏', 'purple', 1, 2),
('八字看姻缘', '八字合婚、感情分析', '💕', 'pink', 1, 3),
('2026运势预测', '新年运势讨论', '✨', 'orange', 1, 4),
('每日一卡', '每日塔罗牌分享', '🎴', 'indigo', 0, 5),
('易经智慧', '易经学习与应用', '📖', 'amber', 0, 6),
('求解读', '占卜结果求解读', '❓', 'cyan', 0, 7),
('新手求助', '新手问题解答', '🆘', 'green', 0, 8);


-- 更新话题帖子数（模拟数据）
UPDATE tb_topic SET posts_count = 23000 WHERE name = '水逆期自救指南';
UPDATE tb_topic SET posts_count = 18000 WHERE name = '塔罗牌入门';
UPDATE tb_topic SET posts_count = 15000 WHERE name = '八字看姻缘';
UPDATE tb_topic SET posts_count = 32000 WHERE name = '2026运势预测';
UPDATE tb_topic SET posts_count = 8500 WHERE name = '每日一卡';
UPDATE tb_topic SET posts_count = 6200 WHERE name = '易经智慧';
UPDATE tb_topic SET posts_count = 4300 WHERE name = '求解读';
UPDATE tb_topic SET posts_count = 3100 WHERE name = '新手求助';

-- 插入示例帖子（需要先有用户数据）
-- INSERT INTO tb_post (user_id, content, title, category, tags, is_anonymous, likes_count, comments_count, shares_count, views_count, is_hot)
-- VALUES 
-- (1, '今天给自己抽了一张塔罗牌「星星」正位 ✨\n\n这张牌代表希望、灵感和内心的平静。最近经历了一些困难，但这张牌告诉我，一切都会好起来的。\n\n分享给同样在努力的你们，保持希望，星光会指引我们前行 🌟', NULL, 'share', '["塔罗分享", "每日一卡"]', 0, 328, 45, 12, 1520, 1),
-- (1, '日主，就是你出生那天的天干，代表"你自己"。\n\n比如你是甲木日主，性格就像大树一样正直向上；乙木日主则像藤蔓，柔韧灵活。\n\n了解自己的日主，是学习八字的第一步～', '【八字入门】什么是日主？', 'share', '["八字科普", "新手必看"]', 0, 892, 156, 234, 5680, 1);
