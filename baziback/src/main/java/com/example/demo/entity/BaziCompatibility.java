package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 八字合盘记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziCompatibility {
    private Long id;
    private Long userId;
    private String partnerType; // friend, lover, spouse等
    private String partnerName;
    private String userBazi;
    private String partnerBazi;
    private Integer compatibilityScore; // 0-100
    private Object compatibilityData; // JSON数据
    private Object visualizationData; // JSON数据
    private String aiAnalysis;
    private String suggestion;
    private Boolean isShared;
    private String shareUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
