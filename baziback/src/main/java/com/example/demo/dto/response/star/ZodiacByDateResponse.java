package com.example.demo.dto.response.star;

import lombok.Data;

@Data
public class ZodiacByDateResponse {
    private String zodiac;        // 星座名称
    private String englishName;   // 英文名称
    private String dateRange;     // 日期范围
}
