package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户测算记录表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbCalculationRecord {
    private Long id;
    private Long userId;
    private String recordType;
    private String recordTitle;
    private String question;
    private String summary;
    private String data;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
