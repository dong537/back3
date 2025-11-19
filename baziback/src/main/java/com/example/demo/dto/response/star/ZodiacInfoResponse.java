package com.example.demo.dto.response.star;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZodiacInfoResponse {
    private String name;        // 中文名称
    private String englishName; // 英文名称
    private String dateRange;   // 日期范围（如"3月21日-4月19日"）
    private String personality; // 性格描述
}