package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日测试实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTest {
    private Long id;
    private Long userId;
    private String testType;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;
    
    private String question;
    private Object resultData; // JSON数据
    private Integer score;
    private String summary;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
