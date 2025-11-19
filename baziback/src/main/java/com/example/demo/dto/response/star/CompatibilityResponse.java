package com.example.demo.dto.response.star;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompatibilityResponse {
    private String zodiac1;    // 第一个星座
    private String zodiac2;    // 第二个星座
    private Integer matchRate; // 匹配度（0-100）
    private String analysis;   // 分析描述
}