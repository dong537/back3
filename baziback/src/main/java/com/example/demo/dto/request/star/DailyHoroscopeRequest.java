package com.example.demo.dto.request.star;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyHoroscopeRequest {
    private String zodiac; // 星座名称
    private String category; // 类别：love, career, health, wealth, luck
    @JsonFormat(pattern = "yyyy-MM-dd")  // 添加这行
    private LocalDate date;
}