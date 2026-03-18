package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 八字解释实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaziInterpretation {
    private Integer id;
    private String godType;          // 十神类型（正财、偏财、正官等）
    private String ganzhiPosition;    // 干支位置（年干、年支、月干等）
    private String basicDef;         // 基本定义
    private String mainContent;      // 主要内容
    private String supportContent;    // 生扶状态内容
    private String restrictContent;   // 制约状态内容
    private String genderDiff;        // 性别差异
    private String tag;               // 标签（逗号分隔）
    private Integer helpCount;       // 有帮助数
    private Integer unhelpCount;     // 无帮助数
    private Integer commentCount;     // 评论数
    
    // 新增详细字段
    private String loveAdvice;        // 爱情提醒
    private String careerAdvice;      // 事业建议
    private String wealthAdvice;      // 财运建议
    private String healthAdvice;      // 健康提醒
    private String suggestions;       // 建议（逗号分隔）
    private String avoidances;        // 避免（逗号分隔）
    private Integer overallScore;     // 综合评分（0-100）
    private Integer loveScore;        // 爱情评分
    private Integer careerScore;      // 事业评分
    private Integer wealthScore;      // 财运评分
    private Integer healthScore;      // 健康评分
    private Integer socialScore;      // 人际评分
}
