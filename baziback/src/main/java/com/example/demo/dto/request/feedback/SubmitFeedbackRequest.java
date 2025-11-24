package com.example.demo.dto.request.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交反馈请求
 */
@Data
public class SubmitFeedbackRequest {
    
    /**
     * 反馈类型：analysis-分析反馈，report-报告反馈，system-系统反馈，suggestion-建议
     */
    @NotBlank(message = "反馈类型不能为空")
    private String feedbackType;
    
    /**
     * 关联ID（分析历史ID或报告ID）
     */
    private Long relatedId;
    
    /**
     * 评分：1-5星
     */
    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer rating;
    
    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    private String content;
    
    /**
     * 标签
     */
    private List<String> tags;
}
