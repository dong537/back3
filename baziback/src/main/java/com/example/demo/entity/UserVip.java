package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVip {
    private Long id;
    private Long userId;
    private String vipType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String source;
    private Long sourceId;
    private Integer isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
