package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分析历史记录实体
 */
@Data
public class AnalysisHistory {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 关联的八字信息ID
     */
    private Long baziInfoId;
    
    /**
     * 分析类型：bazi-八字，tarot-塔罗，yijing-易经，ziwei-紫微，zodiac-星座
     */
    private String analysisType;
    
    /**
     * 请求数据（JSON格式）
     */
    private String requestData;
    
    /**
     * 响应数据（JSON格式）
     */
    private String responseData;
    
    /**
     * 关联的报告ID
     */
    private Long reportId;
    
    /**
     * 分析耗时（毫秒）
     */
    private Integer analysisDuration;
    
    /**
     * 模型版本
     */
    private String modelVersion;
    
    /**
     * 是否收藏：0-否，1-是
     */
    private Integer isFavorite;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
