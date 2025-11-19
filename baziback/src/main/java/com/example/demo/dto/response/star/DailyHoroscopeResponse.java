package com.example.demo.dto.response.star;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyHoroscopeResponse {
    private String zodiac;    // 星座
    private String category;  // 类别
    private String date;      // 日期（如"2025-11-18"）
    private String fortune;   // 运势描述
    private Integer score;    // 评分（1-10）
}