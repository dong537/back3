package com.example.demo.dto.request.star;

import lombok.Data;

@Data
public class ZodiacByDateRequest {
    private Integer month; // 月份（1-12）
    private Integer day;   // 日期（1-31）
}