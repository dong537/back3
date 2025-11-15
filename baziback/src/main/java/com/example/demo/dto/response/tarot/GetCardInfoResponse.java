package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.Map;

@Data
public class GetCardInfoResponse {
    private String cardName;
    private String orientation;
    private String symbolism;   // 象征意义
    private String astrology;   // 占星学信息
    private String numerology;  // 数字命理
    private Map<String, String> meanings; // 各领域含义（爱情、事业等）
}
