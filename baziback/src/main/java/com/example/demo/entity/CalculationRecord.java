package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 测算记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRecord {
    private Long id;
    private Long userId;
    private String recordType; // bazi, yijing, tarot, compatibility等
    private String recordTitle;
    private String question;
    private Object inputData; // JSON数据
    private Object resultData; // JSON数据
    private String summary;
    private String tags; // 逗号分隔
    private Boolean isFavorite;
    private Boolean isShared;
    private String shareUrl;
    private Integer downloadCount;
    private Integer viewCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastViewedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
