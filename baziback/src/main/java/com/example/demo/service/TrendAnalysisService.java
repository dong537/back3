package com.example.demo.service;

import com.example.demo.dto.response.TrendAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 趋势分析服务 - 大运、流年、流月分析
 * 对标 cantian.ai 的趋势解读功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrendAnalysisService {

    private final DeepSeekService deepSeekService;

    /**
     * 获取完整的趋势分析（大运+流年+流月）
     */
    public TrendAnalysisResponse getCompleteTrendAnalysis(
            String bazi, 
            String gender, 
            LocalDate birthDate,
            Integer startAge,
            Integer endAge) {
        
        log.info("开始趋势分析: bazi={}, gender={}, birthDate={}", bazi, gender, birthDate);
        
        TrendAnalysisResponse response = new TrendAnalysisResponse();
        
        // 1. 大运分析
        List<TrendAnalysisResponse.DayunPeriod> dayunList = analyzeDayun(bazi, gender, birthDate, startAge, endAge);
        response.setDayunAnalysis(dayunList);
        
        // 2. 流年分析（当前年和未来5年）
        List<TrendAnalysisResponse.LiunianPeriod> liunianList = analyzeLiunian(bazi, gender, birthDate);
        response.setLiunianAnalysis(liunianList);
        
        // 3. 流月分析（当前年12个月）
        List<TrendAnalysisResponse.LiuyuePeriod> liuyueList = analyzeLiuyue(bazi, gender, birthDate);
        response.setLiuyueAnalysis(liuyueList);
        
        // 4. 重要节点提示
        List<TrendAnalysisResponse.ImportantNode> importantNodes = identifyImportantNodes(dayunList, liunianList);
        response.setImportantNodes(importantNodes);
        
        // 5. 风险提示
        List<TrendAnalysisResponse.RiskWarning> riskWarnings = identifyRisks(dayunList, liunianList);
        response.setRiskWarnings(riskWarnings);
        
        // 6. 生成AI深度解读
        String aiInsight = generateAIInsight(bazi, gender, dayunList, liunianList, liuyueList);
        response.setAiInsight(aiInsight);
        
        response.setAnalysisTime(LocalDateTime.now());
        
        log.info("趋势分析完成");
        return response;
    }

    /**
     * 大运分析 - 10年一个大运
     */
    private List<TrendAnalysisResponse.DayunPeriod> analyzeDayun(
            String bazi, String gender, LocalDate birthDate, Integer startAge, Integer endAge) {
        
        List<TrendAnalysisResponse.DayunPeriod> dayunList = new ArrayList<>();
        
        // 计算起运年龄（简化算法，实际需要根据八字精确计算）
        int qiyunAge = calculateQiyunAge(bazi, gender);
        
        // 生成大运周期
        int currentAge = startAge != null ? startAge : 0;
        int maxAge = endAge != null ? endAge : 80;
        
        for (int age = qiyunAge; age <= maxAge; age += 10) {
            TrendAnalysisResponse.DayunPeriod period = new TrendAnalysisResponse.DayunPeriod();
            period.setStartAge(age);
            period.setEndAge(age + 9);
            period.setStartYear(birthDate.getYear() + age);
            period.setEndYear(birthDate.getYear() + age + 9);
            
            // 计算大运干支（简化版本）
            String dayunGanzhi = calculateDayunGanzhi(bazi, age / 10);
            period.setDayunGanzhi(dayunGanzhi);
            
            // 评估大运吉凶
            period.setLuckLevel(evaluateLuckLevel(bazi, dayunGanzhi));
            period.setLuckScore(calculateLuckScore(bazi, dayunGanzhi));
            
            // 生成大运描述
            period.setDescription(generateDayunDescription(dayunGanzhi, period.getLuckLevel()));
            
            // 关键领域分析
            Map<String, Integer> keyAreas = new HashMap<>();
            keyAreas.put("事业", calculateAreaScore(bazi, dayunGanzhi, "career"));
            keyAreas.put("财运", calculateAreaScore(bazi, dayunGanzhi, "wealth"));
            keyAreas.put("感情", calculateAreaScore(bazi, dayunGanzhi, "relationship"));
            keyAreas.put("健康", calculateAreaScore(bazi, dayunGanzhi, "health"));
            period.setKeyAreas(keyAreas);
            
            dayunList.add(period);
        }
        
        return dayunList;
    }

    /**
     * 流年分析 - 每年的运势
     */
    private List<TrendAnalysisResponse.LiunianPeriod> analyzeLiunian(
            String bazi, String gender, LocalDate birthDate) {
        
        List<TrendAnalysisResponse.LiunianPeriod> liunianList = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        
        // 分析当前年和未来5年
        for (int i = 0; i < 6; i++) {
            int year = currentYear + i;
            TrendAnalysisResponse.LiunianPeriod period = new TrendAnalysisResponse.LiunianPeriod();
            period.setYear(year);
            
            // 计算流年干支
            String liunianGanzhi = calculateYearGanzhi(year);
            period.setLiunianGanzhi(liunianGanzhi);
            
            // 评估流年吉凶
            period.setLuckLevel(evaluateLuckLevel(bazi, liunianGanzhi));
            period.setLuckScore(calculateLuckScore(bazi, liunianGanzhi));
            
            // 生成流年描述
            period.setDescription(generateLiunianDescription(year, liunianGanzhi, period.getLuckLevel()));
            
            // 月度运势概览
            List<Integer> monthlyScores = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                monthlyScores.add(calculateMonthScore(bazi, year, month));
            }
            period.setMonthlyScores(monthlyScores);
            
            // 关键事件预测
            period.setKeyEvents(predictKeyEvents(bazi, year, liunianGanzhi));
            
            // 建议
            period.setSuggestions(generateYearlySuggestions(bazi, year, liunianGanzhi));
            
            liunianList.add(period);
        }
        
        return liunianList;
    }

    /**
     * 流月分析 - 当前年的12个月
     */
    private List<TrendAnalysisResponse.LiuyuePeriod> analyzeLiuyue(
            String bazi, String gender, LocalDate birthDate) {
        
        List<TrendAnalysisResponse.LiuyuePeriod> liuyueList = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        
        for (int month = 1; month <= 12; month++) {
            TrendAnalysisResponse.LiuyuePeriod period = new TrendAnalysisResponse.LiuyuePeriod();
            period.setYear(currentYear);
            period.setMonth(month);
            
            // 计算流月干支
            String liuyueGanzhi = calculateMonthGanzhi(currentYear, month);
            period.setLiuyueGanzhi(liuyueGanzhi);
            
            // 评估流月吉凶
            period.setLuckLevel(evaluateLuckLevel(bazi, liuyueGanzhi));
            period.setLuckScore(calculateMonthScore(bazi, currentYear, month));
            
            // 生成流月描述
            period.setDescription(generateLiuyueDescription(month, liuyueGanzhi));
            
            // 适宜和不宜的事项
            period.setSuitableActivities(getSuitableActivities(bazi, liuyueGanzhi));
            period.setUnsuitableActivities(getUnsuitableActivities(bazi, liuyueGanzhi));
            
            liuyueList.add(period);
        }
        
        return liuyueList;
    }

    /**
     * 识别重要节点
     */
    private List<TrendAnalysisResponse.ImportantNode> identifyImportantNodes(
            List<TrendAnalysisResponse.DayunPeriod> dayunList,
            List<TrendAnalysisResponse.LiunianPeriod> liunianList) {
        
        List<TrendAnalysisResponse.ImportantNode> nodes = new ArrayList<>();
        
        // 从大运中识别重要转折点
        for (TrendAnalysisResponse.DayunPeriod dayun : dayunList) {
            if (dayun.getLuckScore() >= 85) {
                TrendAnalysisResponse.ImportantNode node = new TrendAnalysisResponse.ImportantNode();
                node.setYear(dayun.getStartYear());
                node.setType("大运转换");
                node.setLevel("高");
                node.setDescription("进入" + dayun.getDayunGanzhi() + "大运，运势极佳，是事业发展的黄金时期");
                nodes.add(node);
            }
        }
        
        // 从流年中识别重要年份
        for (TrendAnalysisResponse.LiunianPeriod liunian : liunianList) {
            if (liunian.getLuckScore() >= 90) {
                TrendAnalysisResponse.ImportantNode node = new TrendAnalysisResponse.ImportantNode();
                node.setYear(liunian.getYear());
                node.setType("流年机遇");
                node.setLevel("中");
                node.setDescription(liunian.getYear() + "年运势极佳，把握机会可有重大突破");
                nodes.add(node);
            }
        }
        
        return nodes;
    }

    /**
     * 识别风险
     */
    private List<TrendAnalysisResponse.RiskWarning> identifyRisks(
            List<TrendAnalysisResponse.DayunPeriod> dayunList,
            List<TrendAnalysisResponse.LiunianPeriod> liunianList) {
        
        List<TrendAnalysisResponse.RiskWarning> warnings = new ArrayList<>();
        
        // 从大运中识别风险期
        for (TrendAnalysisResponse.DayunPeriod dayun : dayunList) {
            if (dayun.getLuckScore() <= 40) {
                TrendAnalysisResponse.RiskWarning warning = new TrendAnalysisResponse.RiskWarning();
                warning.setYear(dayun.getStartYear());
                warning.setRiskLevel("高");
                warning.setRiskType("大运不利");
                warning.setDescription(dayun.getStartYear() + "-" + dayun.getEndYear() + "年大运不利，需谨慎行事");
                warning.setSuggestions(Arrays.asList("保守理财", "稳定为主", "避免重大决策"));
                warnings.add(warning);
            }
        }
        
        // 从流年中识别风险年份
        for (TrendAnalysisResponse.LiunianPeriod liunian : liunianList) {
            if (liunian.getLuckScore() <= 35) {
                TrendAnalysisResponse.RiskWarning warning = new TrendAnalysisResponse.RiskWarning();
                warning.setYear(liunian.getYear());
                warning.setRiskLevel("中");
                warning.setRiskType("流年不利");
                warning.setDescription(liunian.getYear() + "年运势欠佳，需要特别注意");
                warning.setSuggestions(Arrays.asList("注意健康", "避免投资", "低调行事"));
                warnings.add(warning);
            }
        }
        
        return warnings;
    }

    /**
     * 生成AI深度解读
     */
    private String generateAIInsight(
            String bazi, String gender,
            List<TrendAnalysisResponse.DayunPeriod> dayunList,
            List<TrendAnalysisResponse.LiunianPeriod> liunianList,
            List<TrendAnalysisResponse.LiuyuePeriod> liuyueList) {
        
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("请作为专业命理师，基于以下八字和趋势数据，生成深度解读：\n\n");
            prompt.append("八字：").append(bazi).append("\n");
            prompt.append("性别：").append(gender).append("\n\n");
            
            prompt.append("大运情况：\n");
            for (TrendAnalysisResponse.DayunPeriod dayun : dayunList) {
                prompt.append(String.format("- %d-%d岁（%d-%d年）：%s，运势评分%d\n",
                        dayun.getStartAge(), dayun.getEndAge(),
                        dayun.getStartYear(), dayun.getEndYear(),
                        dayun.getDayunGanzhi(), dayun.getLuckScore()));
            }
            
            prompt.append("\n流年情况：\n");
            for (TrendAnalysisResponse.LiunianPeriod liunian : liunianList) {
                prompt.append(String.format("- %d年：%s，运势评分%d\n",
                        liunian.getYear(), liunian.getLiunianGanzhi(), liunian.getLuckScore()));
            }
            
            prompt.append("\n请提供：\n");
            prompt.append("1. 整体运势走向分析\n");
            prompt.append("2. 关键转折点解读\n");
            prompt.append("3. 各阶段发展建议\n");
            prompt.append("4. 需要特别注意的时期\n");
            
            return deepSeekService.chat(prompt.toString());
        } catch (Exception e) {
            log.error("生成AI解读失败", e);
            return "AI解读生成失败，请稍后重试";
        }
    }

    // ==================== 辅助计算方法 ====================
    
    private int calculateQiyunAge(String bazi, String gender) {
        // 简化算法：阳男阴女顺排，阴男阳女逆排
        // 实际需要根据出生日到节气的天数精确计算
        return 3; // 默认3岁起运
    }
    
    private String calculateDayunGanzhi(String bazi, int dayunIndex) {
        // 简化版本：从月柱开始顺推或逆推
        String[] ganList = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        String[] zhiList = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        int ganIndex = dayunIndex % 10;
        int zhiIndex = dayunIndex % 12;
        return ganList[ganIndex] + zhiList[zhiIndex];
    }
    
    private String calculateYearGanzhi(int year) {
        // 计算年份对应的干支
        String[] ganList = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        String[] zhiList = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        int ganIndex = (year - 4) % 10;
        int zhiIndex = (year - 4) % 12;
        return ganList[ganIndex] + zhiList[zhiIndex];
    }
    
    private String calculateMonthGanzhi(int year, int month) {
        // 简化版本：根据年干和月份计算月干支
        String[] ganList = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        String[] zhiList = {"寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥", "子", "丑"};
        int ganIndex = ((year - 4) * 12 + month - 1) % 10;
        int zhiIndex = (month + 1) % 12;
        return ganList[ganIndex] + zhiList[zhiIndex];
    }
    
    private String evaluateLuckLevel(String bazi, String ganzhi) {
        // 简化评估：根据干支五行生克关系
        int score = calculateLuckScore(bazi, ganzhi);
        if (score >= 80) return "极佳";
        if (score >= 60) return "较好";
        if (score >= 40) return "一般";
        return "欠佳";
    }
    
    private int calculateLuckScore(String bazi, String ganzhi) {
        // 简化算法：实际需要复杂的五行生克计算
        // 这里使用随机数模拟，实际应该根据八字理论计算
        return 40 + (Math.abs(bazi.hashCode() + ganzhi.hashCode()) % 50);
    }
    
    private int calculateAreaScore(String bazi, String ganzhi, String area) {
        // 简化算法：根据领域和干支计算得分
        return 40 + (Math.abs((bazi + ganzhi + area).hashCode()) % 50);
    }
    
    private int calculateMonthScore(String bazi, int year, int month) {
        String monthGanzhi = calculateMonthGanzhi(year, month);
        return calculateLuckScore(bazi, monthGanzhi);
    }
    
    private String generateDayunDescription(String ganzhi, String luckLevel) {
        return String.format("%s大运，运势%s。此期间整体运势呈现%s态势。",
                ganzhi, luckLevel, luckLevel.equals("极佳") || luckLevel.equals("较好") ? "上升" : "平稳");
    }
    
    private String generateLiunianDescription(int year, String ganzhi, String luckLevel) {
        return String.format("%d年为%s年，运势%s。", year, ganzhi, luckLevel);
    }
    
    private String generateLiuyueDescription(int month, String ganzhi) {
        return String.format("%d月为%s月，适合把握机会，稳步前进。", month, ganzhi);
    }
    
    private List<String> predictKeyEvents(String bazi, int year, String ganzhi) {
        // 简化版本：根据运势预测关键事件
        List<String> events = new ArrayList<>();
        int score = calculateLuckScore(bazi, ganzhi);
        if (score >= 70) {
            events.add("事业有突破机会");
            events.add("可能有贵人相助");
        } else if (score <= 40) {
            events.add("需注意人际关系");
            events.add("避免重大投资");
        }
        return events;
    }
    
    private List<String> generateYearlySuggestions(String bazi, int year, String ganzhi) {
        List<String> suggestions = new ArrayList<>();
        int score = calculateLuckScore(bazi, ganzhi);
        if (score >= 70) {
            suggestions.add("积极把握机会，勇于尝试");
            suggestions.add("可考虑事业拓展或投资");
        } else {
            suggestions.add("保持谨慎，稳健为主");
            suggestions.add("注重健康和人际关系维护");
        }
        return suggestions;
    }
    
    private List<String> getSuitableActivities(String bazi, String ganzhi) {
        return Arrays.asList("学习进修", "社交活动", "健康养生");
    }
    
    private List<String> getUnsuitableActivities(String bazi, String ganzhi) {
        return Arrays.asList("重大投资", "激进决策", "频繁变动");
    }
}
