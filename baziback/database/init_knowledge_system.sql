-- 个人专属知识库系统 - 数据库初始化脚本

USE `bazi`;

-- 用户八字信息表
DROP TABLE IF EXISTS `tb_user_bazi_info`;
CREATE TABLE `tb_user_bazi_info` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT(1) NOT NULL COMMENT '性别：0-女，1-男',
    `birth_year` INT(11) NOT NULL COMMENT '出生年份',
    `birth_month` INT(11) NOT NULL COMMENT '出生月份',
    `birth_day` INT(11) NOT NULL COMMENT '出生日期',
    `birth_hour` INT(11) NOT NULL COMMENT '出生时辰（0-23）',
    `birth_minute` INT(11) NOT NULL DEFAULT 0 COMMENT '出生分钟',
    `is_lunar` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否农历：0-公历，1-农历',
    `timezone` VARCHAR(50) NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '时区',
    `birthplace` VARCHAR(200) NULL COMMENT '出生地',
    `bazi_data` TEXT NULL COMMENT '八字数据（JSON格式）',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认八字：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户八字信息表';

-- 分析历史记录表
DROP TABLE IF EXISTS `tb_analysis_history`;
CREATE TABLE `tb_analysis_history` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `bazi_info_id` BIGINT(20) NULL COMMENT '关联的八字信息ID',
    `analysis_type` VARCHAR(50) NOT NULL COMMENT '分析类型：bazi-八字，tarot-塔罗，yijing-易经，ziwei-紫微，zodiac-星座',
    `request_data` TEXT NOT NULL COMMENT '请求数据（JSON格式）',
    `response_data` TEXT NOT NULL COMMENT '响应数据（JSON格式）',
    `report_id` BIGINT(20) NULL COMMENT '关联的报告ID',
    `analysis_duration` INT(11) NULL COMMENT '分析耗时（毫秒）',
    `model_version` VARCHAR(50) NULL COMMENT '模型版本',
    `is_favorite` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否收藏：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_bazi_info_id` (`bazi_info_id`),
    KEY `idx_analysis_type` (`analysis_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_is_favorite` (`is_favorite`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析历史记录表';

-- 深度分析报告表
DROP TABLE IF EXISTS `tb_analysis_report`;
CREATE TABLE `tb_analysis_report` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `bazi_info_id` BIGINT(20) NULL COMMENT '关联的八字信息ID',
    `report_type` VARCHAR(50) NOT NULL COMMENT '报告类型：comprehensive-综合，career-事业，love-感情，health-健康，wealth-财运',
    `report_title` VARCHAR(200) NOT NULL COMMENT '报告标题',
    `report_content` LONGTEXT NOT NULL COMMENT '报告内容（Markdown格式）',
    `report_data` TEXT NULL COMMENT '报告结构化数据（JSON格式）',
    `version` INT(11) NOT NULL DEFAULT 1 COMMENT '报告版本号',
    `status` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已归档',
    `view_count` INT(11) NOT NULL DEFAULT 0 COMMENT '查看次数',
    `export_count` INT(11) NOT NULL DEFAULT 0 COMMENT '导出次数',
    `last_view_time` DATETIME NULL COMMENT '最后查看时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_bazi_info_id` (`bazi_info_id`),
    KEY `idx_report_type` (`report_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='深度分析报告表';

-- 用户反馈表
DROP TABLE IF EXISTS `tb_user_feedback`;
CREATE TABLE `tb_user_feedback` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `feedback_type` VARCHAR(50) NOT NULL COMMENT '反馈类型：analysis-分析反馈，report-报告反馈，system-系统反馈，suggestion-建议',
    `related_id` BIGINT(20) NULL COMMENT '关联ID（分析历史ID或报告ID）',
    `rating` TINYINT(2) NULL COMMENT '评分：1-5星',
    `content` TEXT NULL COMMENT '反馈内容',
    `tags` VARCHAR(500) NULL COMMENT '标签（JSON数组）',
    `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理，1-已处理，2-已关闭',
    `admin_reply` TEXT NULL COMMENT '管理员回复',
    `reply_time` DATETIME NULL COMMENT '回复时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_feedback_type` (`feedback_type`),
    KEY `idx_related_id` (`related_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- 用户偏好设置表
DROP TABLE IF EXISTS `tb_user_preference`;
CREATE TABLE `tb_user_preference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `preference_key` VARCHAR(100) NOT NULL COMMENT '偏好键',
    `preference_value` TEXT NOT NULL COMMENT '偏好值（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_key` (`user_id`, `preference_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好设置表';

-- 知识库分类表
DROP TABLE IF EXISTS `tb_knowledge_category`;
CREATE TABLE `tb_knowledge_category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(50) NOT NULL COMMENT '分类编码',
    `description` VARCHAR(500) NULL COMMENT '分类描述',
    `icon` VARCHAR(200) NULL COMMENT '分类图标',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库分类表';

-- 知识库文章表
DROP TABLE IF EXISTS `tb_knowledge_article`;
CREATE TABLE `tb_knowledge_article` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `subtitle` VARCHAR(500) NULL COMMENT '副标题',
    `author` VARCHAR(100) NULL COMMENT '作者',
    `cover_image` VARCHAR(500) NULL COMMENT '封面图片',
    `summary` VARCHAR(1000) NULL COMMENT '摘要',
    `content` LONGTEXT NOT NULL COMMENT '文章内容（Markdown格式）',
    `tags` VARCHAR(500) NULL COMMENT '标签（JSON数组）',
    `view_count` INT(11) NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞次数',
    `collect_count` INT(11) NOT NULL DEFAULT 0 COMMENT '收藏次数',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已下架',
    `publish_time` DATETIME NULL COMMENT '发布时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文章表';

-- 用户收藏表
DROP TABLE IF EXISTS `tb_user_collection`;
CREATE TABLE `tb_user_collection` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `collection_type` VARCHAR(50) NOT NULL COMMENT '收藏类型：article-文章，analysis-分析，report-报告',
    `related_id` BIGINT(20) NOT NULL COMMENT '关联ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_type_id` (`user_id`, `collection_type`, `related_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_collection_type` (`collection_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 插入默认知识库分类
INSERT INTO `tb_knowledge_category` (`parent_id`, `category_name`, `category_code`, `description`, `sort_order`, `status`) 
VALUES 
    (0, '八字命理', 'bazi', '传统八字命理知识', 1, 1),
    (0, '紫微斗数', 'ziwei', '紫微斗数命理知识', 2, 1),
    (0, '易经占卜', 'yijing', '易经卦象与占卜', 3, 1),
    (0, '塔罗占卜', 'tarot', '塔罗牌占卜知识', 4, 1),
    (0, '星座运势', 'zodiac', '西方星座运势', 5, 1),
    (0, '入门指南', 'guide', '命理学习入门指南', 6, 1),
    (0, '常见问题', 'faq', '常见问题解答', 7, 1);

-- 插入示例知识库文章
INSERT INTO `tb_knowledge_article` (`category_id`, `title`, `subtitle`, `author`, `summary`, `content`, `tags`, `status`, `publish_time`) 
VALUES 
    (1, '什么是八字命理', '了解八字的基本概念', '参天AI', '八字命理是中国传统命理学的重要组成部分，通过分析一个人出生的年、月、日、时四个时间要素来推算命运。', 
     '# 什么是八字命理\n\n八字命理，又称四柱命理，是中国传统命理学的重要组成部分。\n\n## 基本概念\n\n八字由年柱、月柱、日柱、时柱四柱组成，每柱包含天干和地支，共八个字，因此称为"八字"。\n\n## 五行理论\n\n八字命理基于五行（金、木、水、火、土）的生克制化关系，分析个人的性格特征、运势走向。', 
     '["八字", "命理", "入门"]', 1, NOW()),
    (6, '如何开始学习命理', '命理学习的第一步', '参天AI', '本文将指导您如何开始学习传统命理知识，从基础概念到实践应用。', 
     '# 如何开始学习命理\n\n## 第一步：了解基础概念\n\n学习命理需要先掌握基本的阴阳五行理论。\n\n## 第二步：选择学习方向\n\n可以从八字、紫微、易经等不同方向入手。', 
     '["入门", "学习指南"]', 1, NOW());
