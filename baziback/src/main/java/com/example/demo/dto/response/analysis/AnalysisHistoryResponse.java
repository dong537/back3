package com.example.demo.dto.response.analysis;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分析历史响应
 */
@Data
@Builder
public class AnalysisHistoryResponse {
    
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
     * 分析类型
     */
    private String analysisType;
    
    /**
     * 分析类型描述
     */
    private String analysisTypeDesc;
    
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
