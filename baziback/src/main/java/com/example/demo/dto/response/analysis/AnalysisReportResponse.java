package com.example.demo.dto.response.analysis;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分析报告响应
 */
@Data
@Builder
public class AnalysisReportResponse {
    
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
     * 报告类型
     */
    private String reportType;
    
    /**
     * 报告类型描述
     */
    private String reportTypeDesc;
    
    /**
     * 报告标题
     */
    private String reportTitle;
    
    /**
     * 报告内容（Markdown格式）
     */
    private String reportContent;
    
    /**
     * 报告结构化数据（JSON格式）
     */
    private String reportData;
    
    /**
     * 报告版本号
     */
    private Integer version;
    
    /**
     * 状态：0-草稿，1-已发布，2-已归档
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 导出次数
     */
    private Integer exportCount;
    
    /**
     * 最后查看时间
     */
    private LocalDateTime lastViewTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
