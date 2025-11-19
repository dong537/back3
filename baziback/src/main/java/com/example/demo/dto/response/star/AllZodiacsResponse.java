package com.example.demo.dto.response.star;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AllZodiacsResponse {
    private List<ZodiacSimple> zodiacs;

    @Data
    @Builder
    public static class ZodiacSimple {
        private String name;        // 中文名称
        private String englishName; // 英文名称
        private String dateRange;   // 日期范围
    }
}