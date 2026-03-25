-- 易经占卜系统基础静态表结构
-- 用于存储卦象、纳甲规则、卦爻辞等静态数据
-- 对应流程图中的：查本卦、纳甲规则、卦爻辞等环节

USE `bazi`;

-- ============================================
-- 1. 卦象基础信息表（静态）
-- ============================================
-- 对应流程图中的「查本卦」/「查变卦」节点
-- 存储64卦的基础信息，包括卦名、上下卦、宫属等

CREATE TABLE IF NOT EXISTS `base_hexagram` (
    `id` TINYINT UNSIGNED NOT NULL COMMENT '卦序 1-64',
    `name` VARCHAR(10) NOT NULL COMMENT '卦名（如：乾为天）',
    `name_short` VARCHAR(4) NOT NULL COMMENT '卦名简称（如：乾）',
    `upper_gua` CHAR(1) NOT NULL COMMENT '上卦（乾坎艮震巽离坤兑）',
    `lower_gua` CHAR(1) NOT NULL COMMENT '下卦',
    `palace` CHAR(1) NOT NULL COMMENT '宫属（八宫之一）',
    `palace_nature` CHAR(1) NOT NULL COMMENT '卦宫五行（金木水火土）',
    `position_in_palace` TINYINT NOT NULL COMMENT '宫中位置 1-8',
    `hexagram_type` ENUM('首卦','一世','二世','三世','四世','五世','游魂','归魂') NOT NULL COMMENT '卦类型',
    `sequence_code` CHAR(3) NOT NULL COMMENT '卦序编码（如：乾1）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_palace` (`palace`),
    KEY `idx_upper_lower` (`upper_gua`, `lower_gua`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卦象基础信息表（静态）';

-- ============================================
-- 2. 爻位静态规则表（核心装卦规则）
-- ============================================
-- 对应流程图中的「纳甲规则」节点
-- 存储单卦纳甲规则，用于程序化装卦

CREATE TABLE IF NOT EXISTS `base_yao_rule` (
    `gua` CHAR(1) NOT NULL COMMENT '单卦（乾坎艮震巽离坤兑）',
    `gua_nature` CHAR(1) NOT NULL COMMENT '单卦五行',
    `is_upper` BOOLEAN NOT NULL COMMENT '是否为上卦（1上卦，0下卦）',
    `position_in_hexagram` TINYINT NOT NULL COMMENT '在重卦中位置（1-3下卦，4-6上卦）',
    `stem` CHAR(1) NOT NULL COMMENT '纳干',
    `branch` CHAR(1) NOT NULL COMMENT '纳支',
    `branch_order` TINYINT NOT NULL COMMENT '地支序（子1丑2...亥12）',
    PRIMARY KEY (`gua`, `is_upper`, `position_in_hexagram`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单卦纳甲规则表（静态，用于程序化装卦）';

-- ============================================
-- 3. 卦爻辞表（静态）
-- ============================================
-- 存储64卦的卦辞、彖传、大象、小象、爻辞等原文内容

CREATE TABLE IF NOT EXISTS `base_hexagram_text` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `hexagram_id` TINYINT UNSIGNED NOT NULL COMMENT '卦ID',
    `text_type` ENUM('卦辞','彖传','大象','小象','爻辞') NOT NULL,
    `yao_position` TINYINT DEFAULT NULL COMMENT '爻位（1-6，卦辞时为NULL）',
    `content` TEXT NOT NULL COMMENT '内容',
    `explanation` TEXT COMMENT '白话解释',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hexagram_text_yao` (`hexagram_id`, `text_type`, `yao_position`),
    KEY `idx_text_type` (`text_type`),
    KEY `idx_hexagram_id` (`hexagram_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卦爻辞原文表（静态）';

-- ============================================
-- 4. 卦象表（实例表，用于存储具体卦象信息）
-- ============================================
-- 对应流程图中的「查本卦」/「查变卦」节点
-- 存储64卦的实例信息

CREATE TABLE IF NOT EXISTS `tb_hexagram` (
    `id` TINYINT UNSIGNED NOT NULL COMMENT '卦序 1-64',
    `name` VARCHAR(10) NOT NULL COMMENT '卦名（如：乾为天）',
    `name_short` VARCHAR(4) NOT NULL COMMENT '卦名简称（如：乾）',
    `upper_gua` CHAR(1) NOT NULL COMMENT '上卦（乾坎艮震巽离坤兑）',
    `lower_gua` CHAR(1) NOT NULL COMMENT '下卦',
    `palace_nature` CHAR(1) NOT NULL COMMENT '卦宫五行（金木水火土）',
    `description` VARCHAR(20) DEFAULT NULL COMMENT '描述信息（如：乾宫首卦）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_upper_lower` (`upper_gua`, `lower_gua`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卦象表（实例表）';

-- ============================================
-- 5. 卦爻表（实例表，用于存储具体爻的信息）
-- ============================================
-- 对应流程图中的「爻例」节点
-- 存储每个卦的6个爻的详细信息，包括纳甲、六亲、世应等

CREATE TABLE IF NOT EXISTS `tb_hexagram_yao` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `hexagram_id` TINYINT UNSIGNED NOT NULL COMMENT '卦ID',
    `yao_position` TINYINT NOT NULL COMMENT '爻位（1-6，从下往上）',
    `yao_type` ENUM('阳','阴') NOT NULL COMMENT '爻类型（阳爻/阴爻）',
    `stem` CHAR(1) NOT NULL COMMENT '纳干（天干）',
    `branch` CHAR(1) NOT NULL COMMENT '纳支（地支）',
    `liu_qin` VARCHAR(10) NOT NULL COMMENT '六亲（父母、兄弟、子孙、妻财、官鬼）',
    `is_shi` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为世爻（1是，0否）',
    `is_ying` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为应爻（1是，0否）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hexagram_yao` (`hexagram_id`, `yao_position`),
    KEY `idx_hexagram_id` (`hexagram_id`),
    KEY `idx_yao_position` (`yao_position`),
    KEY `idx_is_shi` (`is_shi`),
    KEY `idx_is_ying` (`is_ying`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卦爻表（实例表，存储每个卦的6个爻信息）';

-- ============================================
-- 完成
-- ============================================

SELECT '易经占卜基础静态表创建完成！' AS message;





-- 卦信息
use bazi;-- ========================乾宫八卦 (五行属金，ID:1-8)========================
-- 1. 乾为天（乾宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (1, '乾为天', '乾', '乾', '乾', '金', '乾宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                                                                                                                (1, 1, '阳', '甲', '子', '子孙', 0, 0),
                                                                                                                                (1, 2, '阳', '甲', '寅', '妻财', 0, 0),
                                                                                                                                (1, 3, '阳', '甲', '辰', '父母', 0, 1),
                                                                                                                                (1, 4, '阳', '壬', '午', '官鬼', 0, 0),
                                                                                                                                (1, 5, '阳', '壬', '申', '兄弟', 0, 0),
                                                                                                                                (1, 6, '阳', '壬', '戌', '父母', 1, 0);

-- 2. 天风姤（乾宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (2, '天风姤', '乾', '巽', '乾', '金', '乾宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (2, 1, '阴', '辛', '丑', '父母', 0, 0),
                                  (2, 2, '阳', '辛', '亥', '妻财', 1, 0),
                                  (2, 3, '阳', '辛', '酉', '兄弟', 0, 0),
                                  (2, 4, '阳', '壬', '午', '官鬼', 0, 0),
                                  (2, 5, '阳', '壬', '申', '兄弟', 0, 1),
                                  (2, 6, '阳', '壬', '戌', '父母', 0, 0);

-- 3. 天山遁（乾宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (3, '天山遁', '乾', '艮', '乾', '金', '乾宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (3, 1, '阴', '丙', '辰', '父母', 0, 0),
                                  (3, 2, '阳', '丙', '午', '官鬼', 0, 0),
                                  (3, 3, '阳', '丙', '申', '兄弟', 1, 0),
                                  (3, 4, '阳', '壬', '午', '官鬼', 0, 0),
                                  (3, 5, '阳', '壬', '申', '兄弟', 0, 0),
                                  (3, 6, '阳', '壬', '戌', '父母', 0, 1);

-- 4. 天地否（乾宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (4, '天地否', '乾', '坤', '乾', '金', '乾宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (4, 1, '阴', '乙', '未', '父母', 0, 1),
                                  (4, 2, '阴', '乙', '巳', '官鬼', 0, 0),
                                  (4, 3, '阴', '乙', '卯', '妻财', 0, 0),
                                  (4, 4, '阳', '壬', '午', '官鬼', 1, 0),
                                  (4, 5, '阳', '壬', '申', '兄弟', 0, 0),
                                  (4, 6, '阳', '壬', '戌', '父母', 0, 0);

-- 5. 风地观（乾宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (5, '风地观', '巽', '坤', '乾', '金', '乾宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (5, 1, '阴', '乙', '未', '父母', 0, 0),
                                  (5, 2, '阴', '乙', '巳', '官鬼', 0, 1),
                                  (5, 3, '阴', '乙', '卯', '妻财', 0, 0),
                                  (5, 4, '阴', '辛', '未', '父母', 0, 0),
                                  (5, 5, '阳', '辛', '巳', '官鬼', 1, 0),
                                  (5, 6, '阳', '辛', '卯', '妻财', 0, 0);

-- 6. 山地剥（乾宫游魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (6, '山地剥', '艮', '坤', '乾', '金', '乾宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (6, 1, '阴', '乙', '未', '妻财', 0, 1),
                                  (6, 2, '阴', '乙', '巳', '官鬼', 0, 0),
                                  (6, 3, '阴', '乙', '卯', '父母', 0, 0),
                                  (6, 4, '阴', '丙', '戌', '妻财', 1, 0),
                                  (6, 5, '阴', '丙', '子', '子孙', 0, 0),
                                  (6, 6, '阳', '丙', '寅', '兄弟', 0, 0);

-- 7. 火地晋（乾宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (7, '火地晋', '离', '坤', '乾', '金', '乾宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (7, 1, '阴', '乙', '未', '父母', 0, 0),
                                  (7, 2, '阴', '乙', '巳', '官鬼', 0, 0),
                                  (7, 3, '阴', '乙', '卯', '妻财', 1, 0),
                                  (7, 4, '阴', '己', '酉', '父母', 0, 0),
                                  (7, 5, '阴', '己', '未', '官鬼', 0, 0),
                                  (7, 6, '阳', '己', '巳', '妻财', 0, 1);

-- 8. 火天大有（乾宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (8, '火天大有', '离', '乾', '乾', '金', '乾宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (8, 1, '阳', '甲', '子', '子孙', 0, 0),
                                  (8, 2, '阳', '甲', '寅', '妻财', 0, 0),
                                  (8, 3, '阳', '甲', '辰', '父母', 1, 0),
                                  (8, 4, '阴', '己', '酉', '父母', 0, 0),
                                  (8, 5, '阴', '己', '未', '官鬼', 0, 0),
                                  (8, 6, '阳', '己', '巳', '妻财', 0, 1);

-- ========================坎宫八卦 (五行属水，ID:9-16)========================
-- 9. 坎为水（坎宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (9, '坎为水', '坎', '坎', '坎', '水', '坎宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (9, 1, '阳', '戊', '寅', '兄弟', 0, 0),
                                  (9, 2, '阴', '戊', '辰', '官鬼', 0, 0),
                                  (9, 3, '阳', '戊', '午', '父母', 0, 1),
                                  (9, 4, '阴', '丙', '申', '妻财', 0, 0),
                                  (9, 5, '阳', '丙', '戌', '兄弟', 0, 0),
                                  (9, 6, '阴', '丙', '子', '子孙', 1, 0);

-- 10. 水泽节（坎宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (10, '水泽节', '坎', '兑', '坎', '水', '坎宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (10, 1, '阴', '丁', '丑', '官鬼', 0, 0),
                                  (10, 2, '阳', '丁', '亥', '子孙', 1, 0),
                                  (10, 3, '阴', '丁', '酉', '妻财', 0, 0),
                                  (10, 4, '阴', '丙', '申', '妻财', 0, 0),
                                  (10, 5, '阳', '丙', '戌', '兄弟', 0, 1),
                                  (10, 6, '阴', '丙', '子', '子孙', 0, 0);

-- 11. 水雷屯（坎宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (11, '水雷屯', '坎', '震', '坎', '水', '坎宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (11, 1, '阳', '庚', '子', '子孙', 0, 0),
                                  (11, 2, '阴', '庚', '寅', '兄弟', 0, 0),
                                  (11, 3, '阳', '庚', '辰', '官鬼', 1, 0),
                                  (11, 4, '阴', '丙', '申', '妻财', 0, 0),
                                  (11, 5, '阳', '丙', '戌', '兄弟', 0, 0),
                                  (11, 6, '阴', '丙', '子', '子孙', 0, 1);

-- 12. 水火既济（坎宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (12, '水火既济', '坎', '离', '坎', '水', '坎宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (12, 1, '阳', '壬', '子', '兄弟', 0, 1),
                                  (12, 2, '阴', '壬', '寅', '官鬼', 0, 0),
                                  (12, 3, '阳', '壬', '辰', '父母', 0, 0),
                                  (12, 4, '阴', '戊', '午', '子孙', 1, 0),
                                  (12, 5, '阳', '戊', '申', '妻财', 0, 0),
                                  (12, 6, '阴', '戊', '戌', '兄弟', 0, 0);

-- 13. 泽火革（坎宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (13, '泽火革', '兑', '离', '坎', '水', '坎宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (13, 1, '阴', '己', '未', '官鬼', 0, 0),
                                  (13, 2, '阳', '己', '巳', '父母', 0, 1),
                                  (13, 3, '阴', '己', '卯', '兄弟', 0, 0),
                                  (13, 4, '阳', '丁', '午', '子孙', 0, 0),
                                  (13, 5, '阴', '丁', '亥', '妻财', 1, 0),
                                  (13, 6, '阳', '丁', '丑', '官鬼', 0, 0);

-- 14. 雷火丰（坎宫游魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (14, '雷火丰', '震', '离', '坎', '水', '坎宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (14, 1, '阳', '庚', '戌', '父母', 0, 1),
                                  (14, 2, '阴', '庚', '申', '妻财', 0, 0),
                                  (14, 3, '阳', '庚', '午', '兄弟', 0, 0),
                                  (14, 4, '阴', '己', '亥', '子孙', 1, 0),
                                  (14, 5, '阳', '己', '丑', '官鬼', 0, 0),
                                  (14, 6, '阴', '己', '卯', '父母', 0, 0);

-- 15. 地火明夷（坎宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (15, '地火明夷', '坤', '离', '坎', '水', '坎宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (15, 1, '阴', '癸', '酉', '兄弟', 0, 0),
                                  (15, 2, '阳', '癸', '亥', '子孙', 0, 0),
                                  (15, 3, '阴', '癸', '丑', '官鬼', 1, 0),
                                  (15, 4, '阳', '辛', '卯', '父母', 0, 0),
                                  (15, 5, '阴', '辛', '巳', '兄弟', 0, 0),
                                  (15, 6, '阳', '辛', '未', '妻财', 0, 1);

-- 16. 水地比（坎宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (16, '水地比', '坎', '坤', '坎', '水', '坎宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (16, 1, '阴', '戊', '子', '子孙', 0, 0),
                                  (16, 2, '阳', '戊', '寅', '兄弟', 0, 0),
                                  (16, 3, '阴', '戊', '辰', '官鬼', 1, 0),
                                  (16, 4, '阳', '丙', '午', '父母', 0, 0),
                                  (16, 5, '阴', '丙', '申', '妻财', 0, 0),
                                  (16, 6, '阳', '丙', '戌', '兄弟', 0, 1);

-- ========================艮宫八卦 (五行属土，ID:17-24)========================
-- 17. 艮为山（艮宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (17, '艮为山', '艮', '艮', '艮', '土', '艮宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (17, 1, '阳', '丙', '辰', '官鬼', 0, 0),
                                  (17, 2, '阴', '丙', '午', '父母', 0, 0),
                                  (17, 3, '阳', '丙', '申', '兄弟', 0, 1),
                                  (17, 4, '阴', '壬', '戌', '子孙', 0, 0),
                                  (17, 5, '阳', '壬', '子', '妻财', 0, 0),
                                  (17, 6, '阴', '壬', '寅', '兄弟', 1, 0);

-- 18. 山火贲（艮宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (18, '山火贲', '艮', '离', '艮', '土', '艮宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (18, 1, '阴', '丁', '丑', '父母', 0, 0),
                                  (18, 2, '阳', '丁', '亥', '子孙', 1, 0),
                                  (18, 3, '阴', '丁', '酉', '妻财', 0, 0),
                                  (18, 4, '阴', '壬', '戌', '子孙', 0, 0),
                                  (18, 5, '阳', '壬', '子', '妻财', 0, 1),
                                  (18, 6, '阴', '壬', '寅', '兄弟', 0, 0);

-- 19. 山天大畜（艮宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (19, '山天大畜', '艮', '乾', '艮', '土', '艮宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (19, 1, '阳', '甲', '子', '妻财', 0, 0),
                                  (19, 2, '阳', '甲', '寅', '兄弟', 0, 0),
                                  (19, 3, '阳', '甲', '辰', '官鬼', 1, 0),
                                  (19, 4, '阴', '壬', '午', '父母', 0, 0),
                                  (19, 5, '阳', '壬', '申', '子孙', 0, 0),
                                  (19, 6, '阴', '壬', '戌', '兄弟', 0, 1);

-- 20. 山泽损（艮宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (20, '山泽损', '艮', '兑', '艮', '土', '艮宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (20, 1, '阴', '丁', '未', '父母', 0, 1),
                                  (20, 2, '阳', '丁', '巳', '兄弟', 0, 0),
                                  (20, 3, '阴', '丁', '卯', '子孙', 0, 0),
                                  (20, 4, '阳', '辛', '丑', '官鬼', 1, 0),
                                  (20, 5, '阴', '辛', '亥', '父母', 0, 0),
                                  (20, 6, '阳', '辛', '酉', '妻财', 0, 0);

-- 21. 风泽中孚（艮宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (21, '风泽中孚', '巽', '兑', '艮', '土', '艮宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (21, 1, '阴', '丁', '未', '父母', 0, 0),
                                  (21, 2, '阳', '丁', '巳', '兄弟', 0, 1),
                                  (21, 3, '阴', '丁', '卯', '子孙', 0, 0),
                                  (21, 4, '阳', '辛', '丑', '官鬼', 0, 0),
                                  (21, 5, '阴', '辛', '亥', '父母', 1, 0),
                                  (21, 6, '阳', '辛', '酉', '妻财', 0, 0);

-- 22. 风山渐（艮宫游魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (22, '风山渐', '巽', '艮', '艮', '土', '艮宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (22, 1, '阴', '丙', '辰', '官鬼', 0, 1),
                                  (22, 2, '阳', '丙', '午', '父母', 0, 0),
                                  (22, 3, '阴', '丙', '申', '兄弟', 0, 0),
                                  (22, 4, '阳', '壬', '戌', '子孙', 1, 0),
                                  (22, 5, '阴', '壬', '子', '妻财', 0, 0),
                                  (22, 6, '阳', '壬', '寅', '兄弟', 0, 0);

-- 23. 雷山小过（艮宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (23, '雷山小过', '震', '艮', '艮', '土', '艮宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (23, 1, '阳', '庚', '戌', '父母', 0, 0),
                                  (23, 2, '阴', '庚', '申', '兄弟', 0, 0),
                                  (23, 3, '阳', '庚', '午', '子孙', 1, 0),
                                  (23, 4, '阴', '丙', '辰', '官鬼', 0, 0),
                                  (23, 5, '阳', '丙', '寅', '兄弟', 0, 0),
                                  (23, 6, '阴', '丙', '子', '妻财', 0, 1);

-- 24. 地山谦（艮宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (24, '地山谦', '坤', '艮', '艮', '土', '艮宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (24, 1, '阴', '癸', '酉', '子孙', 0, 0),
                                  (24, 2, '阴', '癸', '亥', '妻财', 0, 0),
                                  (24, 3, '阳', '癸', '丑', '兄弟', 1, 0),
                                  (24, 4, '阳', '辛', '卯', '官鬼', 0, 0),
                                  (24, 5, '阴', '辛', '巳', '父母', 0, 0),
                                  (24, 6, '阳', '辛', '未', '兄弟', 0, 1);

-- ========================震宫八卦 (五行属木，ID:25-32)========================
-- 25. 震为雷（震宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (25, '震为雷', '震', '震', '震', '木', '震宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (25, 1, '阳', '戊', '子', '妻财', 0, 0),
                                  (25, 2, '阴', '戊', '寅', '兄弟', 0, 0),
                                  (25, 3, '阳', '戊', '辰', '父母', 0, 1),
                                  (25, 4, '阴', '丙', '午', '子孙', 0, 0),
                                  (25, 5, '阳', '丙', '申', '妻财', 0, 0),
                                  (25, 6, '阴', '丙', '戌', '兄弟', 1, 0);

-- 26. 雷地豫（震宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (26, '雷地豫', '震', '坤', '震', '木', '震宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (26, 1, '阴', '乙', '未', '父母', 0, 0),
                                  (26, 2, '阳', '乙', '巳', '兄弟', 1, 0),
                                  (26, 3, '阴', '乙', '卯', '子孙', 0, 0),
                                  (26, 4, '阴', '癸', '亥', '妻财', 0, 0),
                                  (26, 5, '阳', '癸', '酉', '官鬼', 0, 1),
                                  (26, 6, '阴', '癸', '未', '父母', 0, 0);

-- 27. 雷水解（震宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (27, '雷水解', '震', '坎', '震', '木', '震宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (27, 1, '阳', '庚', '子', '官鬼', 0, 0),
                                  (27, 2, '阴', '庚', '寅', '父母', 0, 0),
                                  (27, 3, '阳', '庚', '辰', '妻财', 1, 0),
                                  (27, 4, '阴', '丙', '午', '子孙', 0, 0),
                                  (27, 5, '阳', '丙', '申', '兄弟', 0, 0),
                                  (27, 6, '阴', '丙', '戌', '父母', 0, 1);

-- 28. 雷风恒（震宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (28, '雷风恒', '震', '巽', '震', '木', '震宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (28, 1, '阳', '戊', '子', '妻财', 0, 1),
                                  (28, 2, '阴', '戊', '寅', '兄弟', 0, 0),
                                  (28, 3, '阳', '戊', '辰', '父母', 0, 0),
                                  (28, 4, '阴', '辛', '亥', '子孙', 1, 0),
                                  (28, 5, '阳', '辛', '酉', '官鬼', 0, 0),
                                  (28, 6, '阴', '辛', '未', '父母', 0, 0);

-- 29. 地风升（震宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (29, '地风升', '坤', '巽', '震', '木', '震宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (29, 1, '阴', '乙', '未', '父母', 0, 0),
                                  (29, 2, '阴', '乙', '巳', '兄弟', 0, 1),
                                  (29, 3, '阳', '乙', '卯', '子孙', 0, 0),
                                  (29, 4, '阴', '癸', '亥', '妻财', 0, 0),
                                  (29, 5, '阳', '癸', '酉', '官鬼', 1, 0),
                                  (29, 6, '阴', '癸', '未', '父母', 0, 0);

-- 30. 水风井（震宫游魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (30, '水风井', '坎', '巽', '震', '木', '震宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (30, 1, '阴', '甲', '子', '妻财', 0, 1),
                                  (30, 2, '阳', '甲', '寅', '兄弟', 0, 0),
                                  (30, 3, '阴', '甲', '辰', '父母', 0, 0),
                                  (30, 4, '阴', '壬', '午', '子孙', 1, 0),
                                  (30, 5, '阳', '壬', '申', '妻财', 0, 0),
                                  (30, 6, '阴', '壬', '戌', '兄弟', 0, 0);

-- 31. 泽风大过（震宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (31, '泽风大过', '兑', '巽', '震', '木', '震宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (31, 1, '阴', '丁', '未', '父母', 0, 0),
                                  (31, 2, '阳', '丁', '巳', '兄弟', 0, 0),
                                  (31, 3, '阴', '丁', '卯', '子孙', 1, 0),
                                  (31, 4, '阳', '辛', '丑', '官鬼', 0, 0),
                                  (31, 5, '阴', '辛', '亥', '父母', 0, 0),
                                  (31, 6, '阳', '辛', '酉', '妻财', 0, 1);

-- 32. 泽雷随（震宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (32, '泽雷随', '兑', '震', '震', '木', '震宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (32, 1, '阳', '戊', '子', '妻财', 0, 0),
                                  (32, 2, '阴', '戊', '寅', '兄弟', 0, 0),
                                  (32, 3, '阳', '戊', '辰', '父母', 1, 0),
                                  (32, 4, '阴', '丁', '酉', '妻财', 0, 0),
                                  (32, 5, '阳', '丁', '亥', '子孙', 0, 0),
                                  (32, 6, '阴', '丁', '丑', '官鬼', 0, 1);

-- ========================巽宫八卦 (五行属木，ID:33-40)========================
-- 33. 巽为风（巽宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (33, '巽为风', '巽', '巽', '巽', '木', '巽宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (33, 1, '阴', '辛', '丑', '官鬼', 0, 0),
                                  (33, 2, '阳', '辛', '亥', '父母', 0, 0),
                                  (33, 3, '阴', '辛', '酉', '妻财', 0, 1),
                                  (33, 4, '阳', '乙', '未', '官鬼', 0, 0),
                                  (33, 5, '阴', '乙', '巳', '父母', 0, 0),
                                  (33, 6, '阳', '乙', '卯', '妻财', 1, 0);

-- 34. 风天小畜（巽宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (34, '风天小畜', '巽', '乾', '巽', '木', '巽宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (34, 1, '阳', '甲', '子', '子孙', 0, 0),
                                  (34, 2, '阳', '甲', '寅', '妻财', 1, 0),
                                  (34, 3, '阳', '甲', '辰', '兄弟', 0, 0),
                                  (34, 4, '阳', '乙', '未', '官鬼', 0, 0),
                                  (34, 5, '阴', '乙', '巳', '父母', 0, 1),
                                  (34, 6, '阳', '乙', '卯', '妻财', 0, 0);

-- 35. 风火家人（巽宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (35, '风火家人', '巽', '离', '巽', '木', '巽宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (35, 1, '阴', '己', '卯', '兄弟', 0, 0),
                                  (35, 2, '阳', '己', '丑', '子孙', 0, 0),
                                  (35, 3, '阴', '己', '亥', '父母', 1, 0),
                                  (35, 4, '阳', '乙', '未', '官鬼', 0, 0),
                                  (35, 5, '阴', '乙', '巳', '父母', 0, 0),
                                  (35, 6, '阳', '乙', '卯', '妻财', 0, 1);

-- 36. 风雷益（巽宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (36, '风雷益', '巽', '震', '巽', '木', '巽宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (36, 1, '阴', '辛', '丑', '官鬼', 0, 1),
                                  (36, 2, '阳', '辛', '亥', '父母', 0, 0),
                                  (36, 3, '阴', '辛', '酉', '妻财', 0, 0),
                                  (36, 4, '阴', '丙', '辰', '兄弟', 1, 0),
                                  (36, 5, '阳', '丙', '午', '子孙', 0, 0),
                                  (36, 6, '阴', '丙', '申', '妻财', 0, 0);

-- 37. 天雷无妄（巽宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (37, '天雷无妄', '乾', '震', '巽', '木', '巽宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (37, 1, '阳', '戊', '子', '妻财', 0, 0),
                                  (37, 2, '阴', '戊', '寅', '兄弟', 0, 1),
                                  (37, 3, '阳', '戊', '辰', '父母', 0, 0),
                                  (37, 4, '阳', '壬', '午', '官鬼', 0, 0),
                                  (37, 5, '阴', '壬', '申', '妻财', 1, 0),
                                  (37, 6, '阳', '壬', '戌', '兄弟', 0, 0);

-- 38. 火雷噬嗑（巽宫游魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (38, '火雷噬嗑', '离', '震', '巽', '木', '巽宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (38, 1, '阳', '庚', '子', '妻财', 0, 1),
                                  (38, 2, '阴', '庚', '寅', '兄弟', 0, 0),
                                  (38, 3, '阳', '庚', '辰', '父母', 0, 0),
                                  (38, 4, '阴', '己', '巳', '官鬼', 1, 0),
                                  (38, 5, '阳', '己', '未', '父母', 0, 0),
                                  (38, 6, '阴', '己', '酉', '妻财', 0, 0);

-- 39. 山雷颐（巽宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (39, '山雷颐', '艮', '震', '巽', '木', '巽宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (39, 1, '阳', '丙', '子', '妻财', 0, 0),
                                  (39, 2, '阴', '丙', '寅', '兄弟', 0, 0),
                                  (39, 3, '阳', '丙', '辰', '父母', 1, 0),
                                  (39, 4, '阳', '壬', '申', '妻财', 0, 0),
                                  (39, 5, '阴', '壬', '戌', '兄弟', 0, 0),
                                  (39, 6, '阳', '壬', '子', '子孙', 0, 1);

-- 40. 风地观（巽宫归魂卦）- 注意：与ID 5同名但不同宫
INSERT IGNORE INTO `tb_hexagram` VALUES (40, '风地观(巽)', '巽', '坤', '巽', '木', '巽宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (40, 1, '阴', '乙', '未', '父母', 0, 0),
                                  (40, 2, '阴', '乙', '巳', '官鬼', 0, 0),
                                  (40, 3, '阳', '乙', '卯', '妻财', 1, 0),
                                  (40, 4, '阳', '辛', '丑', '官鬼', 0, 0),
                                  (40, 5, '阴', '辛', '亥', '父母', 0, 0),
                                  (40, 6, '阳', '辛', '酉', '妻财', 0, 1);

-- ========================离宫八卦 (五行属火，ID:41-48)========================
-- 41. 离为火（离宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (41, '离为火', '离', '离', '离', '火', '离宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (41, 1, '阳', '己', '卯', '兄弟', 0, 0),
                                  (41, 2, '阴', '己', '丑', '子孙', 0, 0),
                                  (41, 3, '阳', '己', '亥', '父母', 0, 1),
                                  (41, 4, '阴', '丁', '酉', '妻财', 0, 0),
                                  (41, 5, '阳', '丁', '未', '官鬼', 0, 0),
                                  (41, 6, '阴', '丁', '巳', '父母', 1, 0);

-- 42. 火山旅（离宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (42, '火山旅', '离', '艮', '离', '火', '离宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (42, 1, '阴', '丙', '辰', '官鬼', 0, 0),
                                  (42, 2, '阳', '丙', '午', '父母', 1, 0),
                                  (42, 3, '阴', '丙', '申', '妻财', 0, 0),
                                  (42, 4, '阴', '丁', '酉', '妻财', 0, 0),
                                  (42, 5, '阳', '丁', '未', '官鬼', 0, 1),
                                  (42, 6, '阴', '丁', '巳', '父母', 0, 0);

-- 43. 火风鼎（离宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (43, '火风鼎', '离', '巽', '离', '火', '离宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (43, 1, '阴', '辛', '丑', '官鬼', 0, 0),
                                  (43, 2, '阳', '辛', '亥', '父母', 0, 0),
                                  (43, 3, '阴', '辛', '酉', '妻财', 1, 0),
                                  (43, 4, '阴', '丁', '酉', '妻财', 0, 0),
                                  (43, 5, '阳', '丁', '未', '官鬼', 0, 0),
                                  (43, 6, '阴', '丁', '巳', '父母', 0, 1);

-- 44. 火水未济（离宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (44, '火水未济', '离', '坎', '离', '火', '离宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (44, 1, '阳', '己', '卯', '兄弟', 0, 1),
                                  (44, 2, '阴', '己', '丑', '子孙', 0, 0),
                                  (44, 3, '阳', '己', '亥', '父母', 0, 0),
                                  (44, 4, '阳', '戊', '午', '官鬼', 1, 0),
                                  (44, 5, '阴', '戊', '申', '父母', 0, 0),
                                  (44, 6, '阳', '戊', '戌', '妻财', 0, 0);

-- 45. 山水蒙（离宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (45, '山水蒙', '艮', '坎', '离', '火', '离宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (45, 1, '阳', '戊', '子', '子孙', 0, 0),
                                  (45, 2, '阴', '戊', '寅', '妻财', 0, 1),
                                  (45, 3, '阳', '戊', '辰', '兄弟', 0, 0),
                                  (45, 4, '阴', '丙', '午', '官鬼', 0, 0),
                                  (45, 5, '阳', '丙', '申', '父母', 1, 0),
                                  (45, 6, '阴', '丙', '戌', '妻财', 0, 0);

-- 46. 风水涣（离宫游魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (46, '风水涣', '巽', '坎', '离', '火', '离宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (46, 1, '阳', '庚', '子', '子孙', 0, 1),
                                  (46, 2, '阴', '庚', '寅', '妻财', 0, 0),
                                  (46, 3, '阳', '庚', '辰', '兄弟', 0, 0),
                                  (46, 4, '阴', '辛', '亥', '父母', 1, 0),
                                  (46, 5, '阳', '辛', '酉', '妻财', 0, 0),
                                  (46, 6, '阴', '辛', '未', '官鬼', 0, 0);

-- 47. 天水讼（离宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (47, '天水讼', '乾', '坎', '离', '火', '离宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (47, 1, '阳', '戊', '子', '子孙', 0, 0),
                                  (47, 2, '阴', '戊', '寅', '妻财', 0, 0),
                                  (47, 3, '阳', '戊', '辰', '兄弟', 1, 0),
                                  (47, 4, '阳', '壬', '午', '官鬼', 0, 0),
                                  (47, 5, '阴', '壬', '申', '父母', 0, 0),
                                  (47, 6, '阳', '壬', '戌', '妻财', 0, 1);

-- 48. 天火同人（离宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (48, '天火同人', '离', '乾', '离', '火', '离宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (48, 1, '阳', '甲', '子', '子孙', 0, 0),
                                  (48, 2, '阳', '甲', '寅', '妻财', 0, 0),
                                  (48, 3, '阳', '甲', '辰', '兄弟', 1, 0),
                                  (48, 4, '阴', '丁', '酉', '妻财', 0, 0),
                                  (48, 5, '阳', '丁', '未', '官鬼', 0, 0),
                                  (48, 6, '阴', '丁', '巳', '父母', 0, 1);

-- ========================坤宫八卦 (五行属土，ID:49-56)========================
-- 49. 坤为地（坤宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (49, '坤为地', '坤', '坤', '坤', '土', '坤宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (49, 1, '阴', '乙', '未', '官鬼', 0, 0),
                                  (49, 2, '阴', '乙', '巳', '父母', 0, 0),
                                  (49, 3, '阴', '乙', '卯', '兄弟', 0, 1),
                                  (49, 4, '阴', '癸', '丑', '官鬼', 0, 0),
                                  (49, 5, '阴', '癸', '亥', '父母', 0, 0),
                                  (49, 6, '阴', '癸', '酉', '兄弟', 1, 0);

-- 50. 地雷复（坤宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (50, '地雷复', '坤', '震', '坤', '土', '坤宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (50, 1, '阳', '戊', '子', '子孙', 1, 0),
                                  (50, 2, '阴', '戊', '寅', '妻财', 0, 0),
                                  (50, 3, '阳', '戊', '辰', '兄弟', 0, 0),
                                  (50, 4, '阴', '癸', '丑', '官鬼', 0, 0),
                                  (50, 5, '阴', '癸', '亥', '父母', 0, 1),
                                  (50, 6, '阴', '癸', '酉', '兄弟', 0, 0);

-- 51. 地泽临（坤宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (51, '地泽临', '坤', '兑', '坤', '土', '坤宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (51, 1, '阴', '丁', '未', '父母', 0, 0),
                                  (51, 2, '阳', '丁', '巳', '兄弟', 0, 0),
                                  (51, 3, '阴', '丁', '卯', '官鬼', 1, 0),
                                  (51, 4, '阴', '癸', '丑', '官鬼', 0, 0),
                                  (51, 5, '阴', '癸', '亥', '父母', 0, 0),
                                  (51, 6, '阴', '癸', '酉', '兄弟', 0, 1);

-- 52. 地天泰（坤宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (52, '地天泰', '坤', '乾', '坤', '土', '坤宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (52, 1, '阳', '甲', '子', '子孙', 0, 1),
                                  (52, 2, '阳', '甲', '寅', '妻财', 0, 0),
                                  (52, 3, '阳', '甲', '辰', '兄弟', 0, 0),
                                  (52, 4, '阴', '壬', '午', '官鬼', 1, 0),
                                  (52, 5, '阳', '壬', '申', '父母', 0, 0),
                                  (52, 6, '阳', '壬', '戌', '妻财', 0, 0);

-- 53. 雷天大壮（坤宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (53, '雷天大壮', '震', '乾', '坤', '土', '坤宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (53, 1, '阳', '甲', '子', '子孙', 0, 0),
                                  (53, 2, '阳', '甲', '寅', '妻财', 0, 1),
                                  (53, 3, '阳', '甲', '辰', '兄弟', 0, 0),
                                  (53, 4, '阴', '丙', '午', '官鬼', 0, 0),
                                  (53, 5, '阳', '丙', '申', '父母', 1, 0),
                                  (53, 6, '阴', '丙', '戌', '妻财', 0, 0);

-- 54. 泽天夬（坤宫游魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (54, '泽天夬', '兑', '乾', '坤', '土', '坤宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (54, 1, '阳', '甲', '子', '子孙', 0, 1),
                                  (54, 2, '阳', '甲', '寅', '妻财', 0, 0),
                                  (54, 3, '阳', '甲', '辰', '兄弟', 0, 0),
                                  (54, 4, '阴', '丁', '巳', '父母', 1, 0),
                                  (54, 5, '阳', '丁', '未', '兄弟', 0, 0),
                                  (54, 6, '阴', '丁', '酉', '官鬼', 0, 0);

-- 55. 水天需（坤宫归魂卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (55, '水天需', '坎', '乾', '坤', '土', '坤宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (55, 1, '阳', '甲', '子', '子孙', 0, 0),
                                  (55, 2, '阳', '甲', '寅', '妻财', 0, 0),
                                  (55, 3, '阳', '甲', '辰', '兄弟', 1, 0),
                                  (55, 4, '阴', '丙', '申', '父母', 0, 0),
                                  (55, 5, '阳', '丙', '戌', '妻财', 0, 0),
                                  (55, 6, '阴', '丙', '子', '子孙', 0, 1);

-- 56. 水地比（坤宫归魂卦）- 注意：与ID 16同名但不同宫
INSERT IGNORE INTO `tb_hexagram` VALUES (56, '水地比(坤)', '坎', '坤', '坤', '土', '坤宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (56, 1, '阴', '乙', '未', '官鬼', 0, 0),
                                  (56, 2, '阴', '乙', '巳', '父母', 0, 0),
                                  (56, 3, '阴', '乙', '卯', '兄弟', 1, 0),
                                  (56, 4, '阳', '丙', '午', '父母', 0, 0),
                                  (56, 5, '阴', '丙', '申', '妻财', 0, 0),
                                  (56, 6, '阳', '丙', '戌', '子孙', 0, 1);

-- ========================兑宫八卦 (五行属金，ID:57-64)========================
-- 57. 兑为泽（兑宫首卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (57, '兑为泽', '兑', '兑', '兑', '金', '兑宫首卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (57, 1, '阴', '丁', '巳', '父母', 0, 0),
                                  (57, 2, '阳', '丁', '卯', '妻财', 0, 0),
                                  (57, 3, '阴', '丁', '丑', '兄弟', 0, 1),
                                  (57, 4, '阳', '辛', '亥', '父母', 0, 0),
                                  (57, 5, '阴', '辛', '酉', '妻财', 0, 0),
                                  (57, 6, '阳', '辛', '未', '兄弟', 1, 0);

-- 58. 泽水困（兑宫二世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (58, '泽水困', '兑', '坎', '兑', '金', '兑宫二世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (58, 1, '阴', '戊', '子', '子孙', 1, 0),
                                  (58, 2, '阳', '戊', '寅', '兄弟', 0, 0),
                                  (58, 3, '阴', '戊', '辰', '官鬼', 0, 0),
                                  (58, 4, '阳', '辛', '亥', '父母', 0, 0),
                                  (58, 5, '阴', '辛', '酉', '妻财', 0, 1),
                                  (58, 6, '阳', '辛', '未', '兄弟', 0, 0);

-- 59. 泽地萃（兑宫三世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (59, '泽地萃', '兑', '坤', '兑', '金', '兑宫三世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (59, 1, '阴', '乙', '未', '兄弟', 0, 0),
                                  (59, 2, '阴', '乙', '巳', '官鬼', 0, 0),
                                  (59, 3, '阳', '乙', '卯', '父母', 1, 0),
                                  (59, 4, '阳', '辛', '亥', '父母', 0, 0),
                                  (59, 5, '阴', '辛', '酉', '妻财', 0, 0),
                                  (59, 6, '阳', '辛', '未', '兄弟', 0, 1);

-- 60. 泽山咸（兑宫四世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (60, '泽山咸', '兑', '艮', '兑', '金', '兑宫四世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (60, 1, '阳', '丙', '辰', '官鬼', 0, 1),
                                  (60, 2, '阴', '丙', '午', '父母', 0, 0),
                                  (60, 3, '阳', '丙', '申', '妻财', 0, 0),
                                  (60, 4, '阴', '丁', '卯', '妻财', 1, 0),
                                  (60, 5, '阳', '丁', '丑', '兄弟', 0, 0),
                                  (60, 6, '阴', '丁', '巳', '父母', 0, 0);

-- 61. 水山蹇（兑宫五世卦）
INSERT IGNORE INTO `tb_hexagram` VALUES (61, '水山蹇', '坎', '艮', '兑', '金', '兑宫五世卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (61, 1, '阳', '丙', '辰', '官鬼', 0, 0),
                                  (61, 2, '阴', '丙', '午', '父母', 0, 1),
                                  (61, 3, '阳', '丙', '申', '妻财', 0, 0),
                                  (61, 4, '阴', '戊', '戌', '兄弟', 0, 0),
                                  (61, 5, '阳', '戊', '子', '子孙', 1, 0),
                                  (61, 6, '阴', '戊', '寅', '妻财', 0, 0);

-- 62. 地山谦（兑宫游魂卦）- 注意：与ID 24同名但不同宫
INSERT IGNORE INTO `tb_hexagram` VALUES (62, '地山谦(兑)', '坤', '艮', '兑', '金', '兑宫游魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (62, 1, '阳', '丙', '辰', '官鬼', 0, 1),
                                  (62, 2, '阴', '丙', '午', '父母', 0, 0),
                                  (62, 3, '阳', '丙', '申', '妻财', 0, 0),
                                  (62, 4, '阴', '癸', '酉', '兄弟', 1, 0),
                                  (62, 5, '阴', '癸', '亥', '子孙', 0, 0),
                                  (62, 6, '阳', '癸', '丑', '父母', 0, 0);

-- 63. 雷山小过（兑宫归魂卦）- 注意：与ID 23同名但不同宫
INSERT IGNORE INTO `tb_hexagram` VALUES (63, '雷山小过(兑)', '震', '艮', '兑', '金', '兑宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (63, 1, '阳', '丙', '辰', '官鬼', 0, 0),
                                  (63, 2, '阴', '丙', '午', '父母', 0, 0),
                                  (63, 3, '阳', '丙', '申', '妻财', 1, 0),
                                  (63, 4, '阳', '庚', '戌', '父母', 0, 0),
                                  (63, 5, '阴', '庚', '申', '妻财', 0, 0),
                                  (63, 6, '阳', '庚', '午', '兄弟', 0, 1);

-- 64. 泽雷随（兑宫归魂卦）- 注意：与ID 32同名但不同宫
INSERT IGNORE INTO `tb_hexagram` VALUES (64, '泽雷随(兑)', '兑', '震', '兑', '金', '兑宫归魂卦');
INSERT IGNORE INTO `tb_hexagram_yao` (`hexagram_id`, `yao_position`, `yao_type`, `stem`, `branch`, `liu_qin`, `is_shi`, `is_ying`) VALUES
                                  (64, 1, '阳', '戊', '子', '子孙', 0, 0),
                                  (64, 2, '阴', '戊', '寅', '妻财', 0, 0),
                                  (64, 3, '阳', '戊', '辰', '兄弟', 1, 0),
                                  (64, 4, '阴', '丁', '酉', '妻财', 0, 0),
                                  (64, 5, '阳', '丁', '亥', '子孙', 0, 0),
                                  (64, 6, '阴', '丁', '丑', '父母', 0, 1);