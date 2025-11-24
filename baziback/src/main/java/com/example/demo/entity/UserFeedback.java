package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户反馈实体
 */
@Data
public class UserFeedback {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 反馈类型：analysis-分析反馈，report-报告反馈，system-系统反馈，suggestion-建议
     */
    private String feedbackType;
    
    /**
     * 关联ID（分析历史ID或报告ID）
     */
    private Long relatedId;
    
    /**
     * 评分：1-5星
     */
    private Integer rating;
    
    /**
     * 反馈内容
     */
    private String content;
    
    /**
     * 标签（JSON数组）
     */
    private String tags;
    
    /**
     * 处理状态：0-待处理，1-已处理，2-已关闭
     */
    private Integer status;
    
    /**
     * 管理员回复
     */
    private String adminReply;
    
    /**
     * 回复时间
     */
    private LocalDateTime replyTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
