package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日运势详情实体类
 * 包含爱情、事业、财富、健康、学习等各方面的详细运势分析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_daily_fortune_detail")
public class DailyFortuneDetail {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 运势日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private LocalDate fortuneDate;
    
    /**
     * 爱情运势分数 (0-100)
     */
    private Integer loveScore;
    
    /**
     * 爱情运势详细分析
     */
    private String loveAnalysis;
    
    /**
     * 事业运势分数 (0-100)
     */
    private Integer careerScore;
    
    /**
     * 事业运势详细分析
     */
    private String careerAnalysis;
    
    /**
     * 财富运势分数 (0-100)
     */
    private Integer wealthScore;
    
    /**
     * 财富运势详细分析
     */
    private String wealthAnalysis;
    
    /**
     * 健康运势分数 (0-100)
     */
    private Integer healthScore;
    
    /**
     * 健康运势详细分析
     */
    private String healthAnalysis;
    
    /**
     * 学习运势分数 (0-100)
     */
    private Integer studyScore;
    
    /**
     * 学习运势详细分析
     */
    private String studyAnalysis;
    
    /**
     * 人际运势分数 (0-100)
     */
    private Integer relationshipScore;
    
    /**
     * 人际运势详细分析
     */
    private String relationshipAnalysis;
    
    /**
     * 幸运颜色
     */
    private String luckyColor;
    
    /**
     * 幸运数字
     */
    private String luckyNumber;
    
    /**
     * 幸运方位
     */
    private String luckyDirection;
    
    /**
     * 幸运时间（如：上午9-11点）
     */
    private String luckyTime;
    
    /**
     * 今日宜做事项（JSON数组字符串）
     */
    private String suitableActions;
    
    /**
     * 今日忌做事项（JSON数组字符串）
     */
    private String unsuitableActions;
    
    /**
     * 综合运势建议
     */
    private String overallAdvice;
    
    /**
     * 今日关键词
     */
    private String keywords;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}
