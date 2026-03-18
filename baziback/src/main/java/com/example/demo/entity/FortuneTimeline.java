package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运势时间轴节点实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FortuneTimeline {
    private Long id;
    private Long userId;
    private String timelineType; // dayun, liunian, liuyue等
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String nodeType; // wealth_peak, career_turn, love_opportunity, health_warning等
    private String nodeTitle;
    private String nodeDesc;
    private Object fortuneData; // JSON数据
    private String aiInterpretation;
    private String actionSuggestion;
    private Integer importance; // 1-10
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
