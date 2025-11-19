package com.example.demo.dto.request.star;

import lombok.Data;

@Data
public class ZodiacInfoRequest {
    private String zodiac; // 星座名称（中文或英文，如"白羊座"或"aries"）
}