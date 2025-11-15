package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.List;

@Data
public class CreateCustomSpreadResponse {
    private String spreadName;
    private String description;
    private List<CardPosition> cards;
    private String interpretation;

    @Data
    public static class CardPosition {
        private String positionName;
        private String cardName;
        private String orientation;
        private String meaning;
    }
}
