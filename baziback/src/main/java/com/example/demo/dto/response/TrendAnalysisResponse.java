package com.example.demo.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 趋势分析响应DTO
 * 包含大运、流年、流月分析
 */
@Data
public class TrendAnalysisResponse {
    
    /**
     * 大运分析列表
     */
    private List<DayunPeriod> dayunAnalysis;
    
    /**
     * 流年分析列表
     */
    private List<LiunianPeriod> liunianAnalysis;
    
    /**
     * 流月分析列表
     */
    private List<LiuyuePeriod> liuyueAnalysis;
    
    /**
     * 重要节点提示
     */
    private List<ImportantNode> importantNodes;
    
    /**
     * 风险提示
     */
    private List<RiskWarning> riskWarnings;
    
    /**
     * AI深度解读
     */
    private String aiInsight;
    
    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;
    
    /**
     * 大运周期
     */
    @Data
    public static class DayunPeriod {
        /**
         * 起始年龄
         */
        private Integer startAge;
        
        /**
         * 结束年龄
         */
        private Integer endAge;
        
        /**
         * 起始年份
         */
        private Integer startYear;
        
        /**
         * 结束年份
         */
        private Integer endYear;
        
        /**
         * 大运干支
         */
        private String dayunGanzhi;
        
        /**
         * 运势等级：极佳、较好、一般、欠佳
         */
        private String luckLevel;
        
        /**
         * 运势评分 0-100
         */
        private Integer luckScore;
        
        /**
         * 描述
         */
        private String description;
        
        /**
         * 关键领域评分：事业、财运、感情、健康
         */
        private Map<String, Integer> keyAreas;
    }
    
    /**
     * 流年周期
     */
    @Data
    public static class LiunianPeriod {
        /**
         * 年份
         */
        private Integer year;
        
        /**
         * 流年干支
         */
        private String liunianGanzhi;
        
        /**
         * 运势等级
         */
        private String luckLevel;
        
        /**
         * 运势评分 0-100
         */
        private Integer luckScore;
        
        /**
         * 描述
         */
        private String description;
        
        /**
         * 月度运势评分（12个月）
         */
        private List<Integer> monthlyScores;
        
        /**
         * 关键事件预测
         */
        private List<String> keyEvents;
        
        /**
         * 建议
         */
        private List<String> suggestions;
    }
    
    /**
     * 流月周期
     */
    @Data
    public static class LiuyuePeriod {
        /**
         * 年份
         */
        private Integer year;
        
        /**
         * 月份
         */
        private Integer month;
        
        /**
         * 流月干支
         */
        private String liuyueGanzhi;
        
        /**
         * 运势等级
         */
        private String luckLevel;
        
        /**
         * 运势评分 0-100
         */
        private Integer luckScore;
        
        /**
         * 描述
         */
        private String description;
        
        /**
         * 适宜的活动
         */
        private List<String> suitableActivities;
        
        /**
         * 不宜的活动
         */
        private List<String> unsuitableActivities;
    }
    
    /**
     * 重要节点
     */
    @Data
    public static class ImportantNode {
        /**
         * 年份
         */
        private Integer year;
        
        /**
         * 类型：大运转换、流年机遇等
         */
        private String type;
        
        /**
         * 重要程度：高、中、低
         */
        private String level;
        
        /**
         * 描述
         */
        private String description;
    }
    
    /**
     * 风险提示
     */
    @Data
    public static class RiskWarning {
        /**
         * 年份
         */
        private Integer year;
        
        /**
         * 风险等级：高、中、低
         */
        private String riskLevel;
        
        /**
         * 风险类型
         */
        private String riskType;
        
        /**
         * 描述
         */
        private String description;
        
        /**
         * 建议
         */
        private List<String> suggestions;
    }
}
