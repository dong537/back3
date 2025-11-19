package com.example.demo.dto.request.star;

import lombok.Data;

@Data
public class DailyHoroscopeRequest {
    private String zodiac; // 星座名称
    private String category; // 类别：love, career, health, wealth, luck
}