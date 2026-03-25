package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 复测算提醒实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationReminder {
    private Long id;
    private Long userId;
    private String reminderType; // monthly_fortune, weekly_fortune, daily_test等
    private String reminderTitle;
    private String reminderDesc;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reminderDate;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime reminderTime;
    
    private Boolean isSent;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;
    
    private Boolean isEnabled;
    private Long relatedRecordId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
