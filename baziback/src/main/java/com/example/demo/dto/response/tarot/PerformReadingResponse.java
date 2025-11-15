package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PerformReadingResponse {
    private String spreadType;
    private String question;
    private List<CardReading> cards;
    private String interpretation; // 综合解读
    private Map<String, String> analysis; // 元素、花色等分析

    @Data
    public static class CardReading {
        private String name;
        private String orientation;
        private String positionMeaning;
        private String generalMeaning;
    }
}
