package com.example.demo.service;

import com.example.demo.entity.DailyFortuneDetail;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.DailyFortuneDetailMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.demo.util.AuthUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * 每日运势详情服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DailyFortuneDetailService {
    
    private final DailyFortuneDetailMapper dailyFortuneDetailMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取指定日期的每日运势详情（旧逻辑，保留但不直接使用）
     */
    public DailyFortuneDetail getDailyFortuneDetail(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        DailyFortuneDetail detail = dailyFortuneDetailMapper.findByDate(date);
        if (detail == null) {
            log.info("日期 {} 的每日运势详情数据不存在，生成默认值", date);
            return getDefaultDailyFortuneDetail(date);
        }
        return detail;
    }
    
    /**
     * 获取今天的每日运势详情，为每个用户每日随机抽取一条
     * @param userId 可选的用户ID
     * @return 随机抽取的运势详情
     */
    @Cacheable(
            cacheNames = "dailyFortuneDetail",
            key = "'today:' + T(java.time.LocalDate).now() + ':' + (#userId != null ? #userId : 'guest')",
            condition = "#userId != null"
    )
    public DailyFortuneDetail getTodayFortuneDetail(Long userId) {
        long total;
        try {
            total = dailyFortuneDetailMapper.countTotal();
            log.info("[运势详情] 数据库中总记录数: {}", total);
        } catch (Exception e) {
            log.error("[运势详情] 查询总记录数失败", e);
            throw new BusinessException("数据库查询失败: " + e.getMessage());
        }

        if (total == 0) {
            log.warn("[运势详情] 数据库中没有任何运势数据，返回默认值");
            return getDefaultDailyFortuneDetail(LocalDate.now());
        }

        Random random;
        if (userId != null) {
            long seed = userId + LocalDate.now().toEpochDay();
            random = new Random(seed);
            log.info("[运势详情] 已登录用户 (ID: {})，使用种子 {} 生成随机数", userId, seed);
        } else {
            random = new Random();
            log.info("[运势详情] 未登录用户，使用完全随机");
        }

        // 修复：确保 total > 0，避免 nextInt 参数为0或负数
        long offset = total > 0 ? random.nextInt((int) total) : 0;
        log.info("[运势详情] 生成的随机偏移量: {}", offset);
        return dailyFortuneDetailMapper.findByOffset(offset);
    }
    
    /**
     * 获取未来几天的每日运势详情
     */
    public List<DailyFortuneDetail> getFutureFortuneDetails(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days - 1);
        return dailyFortuneDetailMapper.findByDateRange(today, endDate);
    }
    
    /**
     * 获取日期范围的每日运势详情
     */
    public List<DailyFortuneDetail> getFortuneDetailsByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailyFortuneDetailMapper.findByDateRange(startDate, endDate);
    }
    
    /**
     * 创建或更新每日运势详情
     */
    @Transactional
    @CacheEvict(cacheNames = "dailyFortuneDetail", allEntries = true)
    public DailyFortuneDetail saveDailyFortuneDetail(DailyFortuneDetail detail) {
        if (detail == null) {
            throw new BusinessException("每日运势详情数据不能为空");
        }
        
        // 数据验证
        validateDailyFortuneDetail(detail);
        
        if (detail.getId() == null) {
            // 检查该日期是否已存在
            DailyFortuneDetail existing = dailyFortuneDetailMapper.findByDate(detail.getFortuneDate());
            if (existing != null) {
                // 更新现有记录
                detail.setId(existing.getId());
                dailyFortuneDetailMapper.update(detail);
                log.info("更新日期 {} 的运势详情", detail.getFortuneDate());
                return detail;
            } else {
                // 插入新记录
                dailyFortuneDetailMapper.insert(detail);
                log.info("创建日期 {} 的运势详情", detail.getFortuneDate());
                return detail;
            }
        } else {
            // 更新现有记录
            dailyFortuneDetailMapper.update(detail);
            log.info("更新ID {} 的运势详情", detail.getId());
            return detail;
        }
    }
    
    /**
     * 验证每日运势详情数据
     */
    private void validateDailyFortuneDetail(DailyFortuneDetail detail) {
        if (detail.getFortuneDate() == null) {
            throw new BusinessException("运势日期不能为空");
        }
        
        // 验证分数范围
        int[] scores = {
            detail.getLoveScore() != null ? detail.getLoveScore() : 0,
            detail.getCareerScore() != null ? detail.getCareerScore() : 0,
            detail.getWealthScore() != null ? detail.getWealthScore() : 0,
            detail.getHealthScore() != null ? detail.getHealthScore() : 0,
            detail.getStudyScore() != null ? detail.getStudyScore() : 0,
            detail.getRelationshipScore() != null ? detail.getRelationshipScore() : 0
        };
        
        for (int score : scores) {
            if (score < 0 || score > 100) {
                throw new BusinessException("运势分数必须在0-100之间");
            }
        }
    }
    
    /**
     * 批量保存每日运势详情
     */
    @Transactional
    @CacheEvict(cacheNames = "dailyFortuneDetail", allEntries = true)
    public int batchSave(List<DailyFortuneDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        
        // 验证所有数据
        for (DailyFortuneDetail detail : details) {
            validateDailyFortuneDetail(detail);
        }
        
        int count = dailyFortuneDetailMapper.batchInsert(details);
        log.info("批量保存 {} 条运势详情", count);
        return count;
    }
    
    /**
     * 生成随机运势详情（公开方法，用于随机生成接口）
     */
    public DailyFortuneDetail generateRandomFortuneDetail(LocalDate date) {
        return getDefaultDailyFortuneDetail(date);
    }
    
    /**
     * 获取默认的每日运势详情（当数据库中没有数据时，随机生成）
     */
    private DailyFortuneDetail getDefaultDailyFortuneDetail(LocalDate date) {
        // 根据日期生成稳定的默认值（相同日期总是返回相同结果）
        int dayOfYear = date.getDayOfYear();
        int year = date.getYear();
        long seed = year * 1000L + dayOfYear;
        Random random = new Random(seed);
        
        // 更丰富的分析文本库 - 爱情运势
        String[] loveAnalyses = {
            "今日感情运势平稳，适合与伴侣沟通交流，单身者有机会遇到心仪对象。",
            "感情运势不错，适合表达内心想法，增进彼此了解。已有伴侣者感情更加甜蜜。",
            "今日感情运势很好，单身者桃花运旺盛，容易遇到心仪对象。适合主动出击。",
            "感情运势一般，需要多花时间陪伴伴侣。注意沟通方式，避免误会。",
            "感情运势极佳，单身者容易遇到心仪对象，已有伴侣者感情升温，适合浪漫约会。",
            "感情运势上升，适合与伴侣一起规划未来，增进彼此了解。单身者需要主动出击。",
            "感情运势平稳，适合与伴侣沟通交流，解决之前的小问题。注意表达方式。",
            "感情运势不错，适合表达内心想法，增进彼此了解。已有伴侣者感情更加甜蜜。",
            "今日感情运势很好，单身者桃花运旺盛，容易遇到心仪对象。适合主动出击，不要错过机会。",
            "感情运势一般，需要多花时间陪伴伴侣。注意沟通方式，避免误会。单身者需要主动出击。"
        };
        
        // 事业运势分析文本库
        String[] careerAnalyses = {
            "工作方面进展顺利，适合制定新的计划，注意与同事的协作。",
            "工作压力较大，需要合理安排时间。注意与上司的沟通，及时汇报工作进展。",
            "工作进展非常顺利，可能会得到上司的认可和赞赏。适合提出新的想法和建议。",
            "工作进展顺利，适合制定新的计划和目标。与同事的协作关系良好，团队氛围融洽。",
            "工作方面进展顺利，适合制定新的计划和目标。与同事的协作关系良好，团队氛围融洽。注意把握机会，展现自己的能力。",
            "工作压力较大，需要合理安排时间。注意与上司的沟通，及时汇报工作进展。保持耐心，坚持就是胜利。",
            "工作进展非常顺利，可能会得到上司的认可和赞赏。适合提出新的想法和建议，展现创新能力。",
            "工作进展顺利，适合制定新的计划和目标。与同事的协作关系良好，团队氛围融洽。注意把握机会。"
        };
        
        // 财富运势分析文本库
        String[] wealthAnalyses = {
            "财运稳定，适合理性投资，避免冲动消费。",
            "财运稳定上升，适合理性投资和理财规划。避免冲动消费，保持理性判断。",
            "财运上升，可能会有意外的收入或投资回报。适合理性投资，但需要谨慎分析。",
            "财运一般，需要谨慎理财。避免高风险投资，保持稳健的财务策略。",
            "财运稳定上升，适合理性投资和理财规划。避免冲动消费，保持理性判断。可能会有意外的收入机会。",
            "财运上升，可能会有意外的收入或投资回报。适合理性投资，但需要谨慎分析。避免高风险投资。",
            "财运一般，需要谨慎理财。避免高风险投资，保持稳健的财务策略。可能会有一些必要的支出。",
            "财运稳定，适合理性投资，避免冲动消费。保持理性判断，不要被短期利益诱惑。"
        };
        
        // 健康运势分析文本库
        String[] healthAnalyses = {
            "身体状况良好，注意规律作息，适当运动有益健康。",
            "身体状况良好，精力充沛。适合进行户外运动，呼吸新鲜空气。保持心情愉悦。",
            "身体状况良好，但需要注意休息。避免过度劳累，保持充足的睡眠。适当运动有助于缓解压力。",
            "身体状况良好，精力充沛。注意规律作息，适当运动有益健康。保持心情愉悦，避免过度劳累。",
            "身体状况良好，精力充沛。适合进行户外运动，呼吸新鲜空气。保持心情愉悦，注意规律作息。",
            "身体状况良好，但需要注意休息。避免过度劳累，保持充足的睡眠。适当运动有助于缓解压力，保持心情愉悦。",
            "身体状况良好，注意规律作息，适当运动有益健康。保持心情愉悦，避免过度劳累。",
            "身体状况良好，精力充沛。适合进行户外运动，呼吸新鲜空气。注意规律作息，保持心情愉悦。"
        };
        
        // 学习运势分析文本库
        String[] studyAnalyses = {
            "学习状态不错，适合复习旧知识，制定学习计划。",
            "学习状态极佳，适合学习新知识和技能。记忆力增强，理解能力提升。制定学习计划，把握黄金学习时间。",
            "学习效率高，适合学习新知识和技能。记忆力增强，理解能力提升。",
            "学习状态不错，适合复习旧知识。理解能力提升，但需要多练习巩固。制定合理的学习计划。",
            "学习状态极佳，适合学习新知识和技能。记忆力增强，理解能力提升。制定学习计划，把握黄金学习时间。",
            "学习效率高，适合学习新知识和技能。记忆力增强，理解能力提升。把握学习机会，制定合理的学习计划。",
            "学习状态不错，适合复习旧知识。理解能力提升，但需要多练习巩固。制定合理的学习计划，保持学习热情。",
            "学习状态极佳，适合学习新知识和技能。记忆力增强，理解能力提升。把握学习机会，制定学习计划。"
        };
        
        // 人际运势分析文本库
        String[] relationshipAnalyses = {
            "人际关系和谐，适合参加社交活动，拓展人脉。",
            "人际关系非常和谐，适合参加社交活动。可能会遇到贵人相助，拓展人脉资源。",
            "人际关系平稳，适合与朋友聚会。注意言行举止，避免不必要的误会。",
            "人际关系和谐，适合参加社交活动，拓展人脉。与朋友交流愉快，可能会遇到贵人相助。",
            "人际关系非常和谐，适合参加社交活动。可能会遇到贵人相助，拓展人脉资源。与朋友交流愉快。",
            "人际关系平稳，适合与朋友聚会。注意言行举止，避免不必要的误会。可能会有新的社交机会。",
            "人际关系和谐，适合参加社交活动，拓展人脉。与朋友交流愉快，可能会遇到贵人相助。注意言行举止。",
            "人际关系非常和谐，适合参加社交活动。可能会遇到贵人相助，拓展人脉资源。与朋友交流愉快，注意言行举止。"
        };
        
        // 综合建议文本库
        String[] advices = {
            "今日运势平稳，保持乐观心态，把握机会。",
            "今日运势整体良好，各方面都有不错的表现。保持乐观心态，把握机会，会有不错的收获。",
            "今日运势极佳，各方面都有出色的表现。保持积极心态，把握机会，会有意想不到的收获。",
            "今日运势一般，需要保持耐心和坚持。注意时间管理，合理安排各项事务。",
            "今日运势平稳，保持乐观心态，把握机会。注意时间管理，合理安排各项事务。",
            "今日运势整体良好，各方面都有不错的表现。保持乐观心态，把握机会，会有不错的收获。注意细节。",
            "今日运势极佳，各方面都有出色的表现。保持积极心态，把握机会，会有意想不到的收获。注意细节。",
            "今日运势一般，需要保持耐心和坚持。注意时间管理，合理安排各项事务。保持乐观心态。"
        };
        
        // 关键词文本库
        String[] keywordsList = {
            "平稳,沟通,规划",
            "积极,规划,交流",
            "极佳,机会,收获",
            "耐心,坚持,管理",
            "积极,机会,规划",
            "良好,沟通,坚持",
            "极佳,收获,机会",
            "平稳,管理,沟通"
        };
        
        // 宜做事项文本库
        String[][] suitableActionsList = {
            {"保持积极心态", "规划未来", "与他人交流"},
            {"保持积极心态", "规划未来", "与他人交流", "学习新知识"},
            {"保持耐心", "合理安排时间", "与朋友交流", "适当休息"},
            {"主动出击", "展现能力", "理性投资", "户外运动"},
            {"多陪伴伴侣", "合理安排时间", "谨慎理财", "适当休息"},
            {"保持积极心态", "规划未来", "理性投资", "学习新知识"},
            {"主动出击", "展现能力", "理性投资", "户外运动", "社交活动"},
            {"规划未来", "制定计划", "理性投资", "学习新知识"}
        };
        
        // 忌做事项文本库
        String[][] unsuitableActionsList = {
            {"过度焦虑", "冲动决策", "忽视健康"},
            {"过度焦虑", "冲动决策", "忽视健康", "熬夜"},
            {"过度劳累", "冲动消费", "忽视沟通", "熬夜学习"},
            {"过度自信", "忽视细节", "冲动决策"},
            {"过度焦虑", "冲动消费", "忽视沟通"},
            {"过度焦虑", "冲动消费", "忽视健康", "熬夜"},
            {"过度自信", "忽视细节", "冲动决策"},
            {"过度焦虑", "冲动消费", "忽视健康"}
        };
        
        // 为每个维度随机选择不同的索引，增加随机性
        int loveIndex = random.nextInt(loveAnalyses.length);
        int careerIndex = random.nextInt(careerAnalyses.length);
        int wealthIndex = random.nextInt(wealthAnalyses.length);
        int healthIndex = random.nextInt(healthAnalyses.length);
        int studyIndex = random.nextInt(studyAnalyses.length);
        int relationshipIndex = random.nextInt(relationshipAnalyses.length);
        int adviceIndex = random.nextInt(advices.length);
        int keywordsIndex = random.nextInt(keywordsList.length);
        int actionsIndex = random.nextInt(suitableActionsList.length);
        
        // 随机生成分数（每个维度独立随机）
        int loveScore = 60 + random.nextInt(35);  // 60-95
        int careerScore = 60 + random.nextInt(35);  // 60-95
        int wealthScore = 60 + random.nextInt(35);  // 60-95
        int healthScore = 65 + random.nextInt(30);  // 65-95
        int studyScore = 60 + random.nextInt(35);  // 60-95
        int relationshipScore = 60 + random.nextInt(35);  // 60-95
        
        // 将宜忌事项转换为JSON
        String suitableActionsJson;
        String unsuitableActionsJson;
        try {
            suitableActionsJson = objectMapper.writeValueAsString(suitableActionsList[actionsIndex]);
            unsuitableActionsJson = objectMapper.writeValueAsString(unsuitableActionsList[actionsIndex]);
        } catch (Exception e) {
            log.warn("转换宜忌事项为JSON失败", e);
            suitableActionsJson = "[\"保持积极心态\", \"规划未来\", \"与他人交流\"]";
            unsuitableActionsJson = "[\"过度焦虑\", \"冲动决策\", \"忽视健康\"]";
        }
        
        return DailyFortuneDetail.builder()
                .fortuneDate(date)
                .loveScore(loveScore)
                .loveAnalysis(loveAnalyses[loveIndex])
                .careerScore(careerScore)
                .careerAnalysis(careerAnalyses[careerIndex])
                .wealthScore(wealthScore)
                .wealthAnalysis(wealthAnalyses[wealthIndex])
                .healthScore(healthScore)
                .healthAnalysis(healthAnalyses[healthIndex])
                .studyScore(studyScore)
                .studyAnalysis(studyAnalyses[studyIndex])
                .relationshipScore(relationshipScore)
                .relationshipAnalysis(relationshipAnalyses[relationshipIndex])
                .luckyColor(getRandomColor(random))
                .luckyNumber(String.valueOf(1 + random.nextInt(9)))
                .luckyDirection(getRandomDirection(random))
                .luckyTime(getRandomTime(random))
                .suitableActions(suitableActionsJson)
                .unsuitableActions(unsuitableActionsJson)
                .overallAdvice(advices[adviceIndex])
                .keywords(keywordsList[keywordsIndex])
                .build();
    }
    
    private String getRandomTime(Random random) {
        String[] times = {
            "上午9-11点", "下午2-4点", "上午10-12点", 
            "下午3-5点", "上午8-10点", "下午1-3点",
            "上午7-9点", "下午5-7点", "中午11-13点",
            "晚上7-9点", "上午6-8点", "下午4-6点"
        };
        return times[random.nextInt(times.length)];
    }
    
    private String getRandomColor(Random random) {
        String[] colors = {
            "葡萄紫", "天空蓝", "翡翠绿", "玫瑰红", "柠檬黄", 
            "深海蓝", "樱花粉", "薄荷绿", "象牙白", "薰衣草紫",
            "宝石蓝", "珊瑚红", "橄榄绿", "琥珀黄", "珍珠白",
            "紫罗兰", "天青色", "朱砂红", "墨绿色", "金色"
        };
        return colors[random.nextInt(colors.length)];
    }
    
    private String getRandomDirection(Random random) {
        String[] directions = {
            "东方", "南方", "西方", "北方", 
            "东南方", "西南方", "东北方", "西北方",
            "正东", "正南", "正西", "正北"
        };
        return directions[random.nextInt(directions.length)];
    }
    
    /**
     * 格式化每日运势详情数据为前端需要的格式
     */
    public Map<String, Object> formatDailyFortuneDetail(DailyFortuneDetail detail) {
        if (detail == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", detail.getFortuneDate().toString());
        
        // 各维度运势
        Map<String, Object> aspects = new HashMap<>();
        aspects.put("love", Map.of(
                "score", detail.getLoveScore() != null ? detail.getLoveScore() : 0,
                "analysis", detail.getLoveAnalysis() != null ? detail.getLoveAnalysis() : ""
        ));
        aspects.put("career", Map.of(
                "score", detail.getCareerScore() != null ? detail.getCareerScore() : 0,
                "analysis", detail.getCareerAnalysis() != null ? detail.getCareerAnalysis() : ""
        ));
        aspects.put("wealth", Map.of(
                "score", detail.getWealthScore() != null ? detail.getWealthScore() : 0,
                "analysis", detail.getWealthAnalysis() != null ? detail.getWealthAnalysis() : ""
        ));
        aspects.put("health", Map.of(
                "score", detail.getHealthScore() != null ? detail.getHealthScore() : 0,
                "analysis", detail.getHealthAnalysis() != null ? detail.getHealthAnalysis() : ""
        ));
        aspects.put("study", Map.of(
                "score", detail.getStudyScore() != null ? detail.getStudyScore() : 0,
                "analysis", detail.getStudyAnalysis() != null ? detail.getStudyAnalysis() : ""
        ));
        aspects.put("relationship", Map.of(
                "score", detail.getRelationshipScore() != null ? detail.getRelationshipScore() : 0,
                "analysis", detail.getRelationshipAnalysis() != null ? detail.getRelationshipAnalysis() : ""
        ));
        result.put("aspects", aspects);
        
        // 幸运元素
        Map<String, String> luckyElements = new HashMap<>();
        luckyElements.put("color", detail.getLuckyColor() != null ? detail.getLuckyColor() : "");
        luckyElements.put("number", detail.getLuckyNumber() != null ? detail.getLuckyNumber() : "");
        luckyElements.put("direction", detail.getLuckyDirection() != null ? detail.getLuckyDirection() : "");
        luckyElements.put("time", detail.getLuckyTime() != null ? detail.getLuckyTime() : "");
        result.put("luckyElements", luckyElements);
        
        // 宜忌事项
        try {
            List<String> suitable = parseJsonArray(detail.getSuitableActions());
            List<String> unsuitable = parseJsonArray(detail.getUnsuitableActions());
            result.put("suitableActions", suitable);
            result.put("unsuitableActions", unsuitable);
        } catch (Exception e) {
            log.warn("解析宜忌事项失败", e);
            result.put("suitableActions", Arrays.asList("保持积极心态", "规划未来", "与他人交流"));
            result.put("unsuitableActions", Arrays.asList("过度焦虑", "冲动决策", "忽视健康"));
        }
        
        result.put("overallAdvice", detail.getOverallAdvice() != null ? detail.getOverallAdvice() : "");
        
        // 关键词
        if (detail.getKeywords() != null && !detail.getKeywords().isEmpty()) {
            result.put("keywords", Arrays.asList(detail.getKeywords().split("[,，]")));
        } else {
            result.put("keywords", Collections.emptyList());
        }
        
        return result;
    }
    
    /**
     * 解析JSON数组字符串
     */
    private List<String> parseJsonArray(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 如果不是JSON格式，尝试按逗号分割
            return Arrays.asList(jsonStr.split("[,，]"));
        }
    }
    
    /**
     * 获取运势趋势分析（对比今天和昨天的运势变化）
     */
    public Map<String, Object> getFortuneTrend(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        DailyFortuneDetail today = getDailyFortuneDetail(date);
        DailyFortuneDetail yesterday = getDailyFortuneDetail(date.minusDays(1));
        
        Map<String, Object> trend = new HashMap<>();
        trend.put("date", date.toString());
        
        // 计算各维度变化
        Map<String, Object> changes = new HashMap<>();
        changes.put("love", calculateChange(today.getLoveScore(), yesterday.getLoveScore()));
        changes.put("career", calculateChange(today.getCareerScore(), yesterday.getCareerScore()));
        changes.put("wealth", calculateChange(today.getWealthScore(), yesterday.getWealthScore()));
        changes.put("health", calculateChange(today.getHealthScore(), yesterday.getHealthScore()));
        changes.put("study", calculateChange(today.getStudyScore(), yesterday.getStudyScore()));
        changes.put("relationship", calculateChange(today.getRelationshipScore(), yesterday.getRelationshipScore()));
        
        trend.put("changes", changes);
        trend.put("today", formatDailyFortuneDetail(today));
        trend.put("yesterday", formatDailyFortuneDetail(yesterday));
        
        return trend;
    }
    
    /**
     * 计算运势变化
     */
    private Map<String, Object> calculateChange(Integer todayScore, Integer yesterdayScore) {
        Map<String, Object> change = new HashMap<>();
        if (todayScore == null) todayScore = 0;
        if (yesterdayScore == null) yesterdayScore = 0;
        
        int diff = todayScore - yesterdayScore;
        change.put("diff", diff);
        change.put("trend", diff > 0 ? "up" : (diff < 0 ? "down" : "stable"));
        change.put("percentage", yesterdayScore > 0 ? (diff * 100.0 / yesterdayScore) : 0);
        
        return change;
    }
    
    /**
     * 获取平均运势分数
     */
    public double getAverageScore(DailyFortuneDetail detail) {
        if (detail == null) {
            return 0;
        }
        
        int sum = 0;
        int count = 0;
        
        if (detail.getLoveScore() != null) { sum += detail.getLoveScore(); count++; }
        if (detail.getCareerScore() != null) { sum += detail.getCareerScore(); count++; }
        if (detail.getWealthScore() != null) { sum += detail.getWealthScore(); count++; }
        if (detail.getHealthScore() != null) { sum += detail.getHealthScore(); count++; }
        if (detail.getStudyScore() != null) { sum += detail.getStudyScore(); count++; }
        if (detail.getRelationshipScore() != null) { sum += detail.getRelationshipScore(); count++; }
        
        return count > 0 ? (double) sum / count : 0;
    }
    
    /**
     * 获取最佳运势维度
     */
    public String getBestAspect(DailyFortuneDetail detail) {
        if (detail == null) {
            return null;
        }
        
        Map<String, Integer> scores = new HashMap<>();
        scores.put("love", detail.getLoveScore() != null ? detail.getLoveScore() : 0);
        scores.put("career", detail.getCareerScore() != null ? detail.getCareerScore() : 0);
        scores.put("wealth", detail.getWealthScore() != null ? detail.getWealthScore() : 0);
        scores.put("health", detail.getHealthScore() != null ? detail.getHealthScore() : 0);
        scores.put("study", detail.getStudyScore() != null ? detail.getStudyScore() : 0);
        scores.put("relationship", detail.getRelationshipScore() != null ? detail.getRelationshipScore() : 0);
        
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * 获取最弱运势维度
     */
    public String getWeakestAspect(DailyFortuneDetail detail) {
        if (detail == null) {
            return null;
        }
        
        Map<String, Integer> scores = new HashMap<>();
        scores.put("love", detail.getLoveScore() != null ? detail.getLoveScore() : 0);
        scores.put("career", detail.getCareerScore() != null ? detail.getCareerScore() : 0);
        scores.put("wealth", detail.getWealthScore() != null ? detail.getWealthScore() : 0);
        scores.put("health", detail.getHealthScore() != null ? detail.getHealthScore() : 0);
        scores.put("study", detail.getStudyScore() != null ? detail.getStudyScore() : 0);
        scores.put("relationship", detail.getRelationshipScore() != null ? detail.getRelationshipScore() : 0);
        
        return scores.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
