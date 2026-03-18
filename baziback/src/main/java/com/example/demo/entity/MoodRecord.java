package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心情记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodRecord {
    private Long id;
    private Long userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;
    
    private String moodType; // happy, sad, anxious, calm, excited等
    private Integer moodScore; // 1-10
    private String moodDesc;
    private String relatedFortune; // bazi, yijing, tarot等
    private Object fortuneData; // JSON数据
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
