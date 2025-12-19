package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.List;

@Data
public class TarotDrawResultResponse {
    private String spreadType;
    private String question;
    private List<DrawnCard> cards;

    @Data
    public static class DrawnCard {
        private Integer index;
        private String name;
        private String orientation;
        private String position;
        private String positionMeaning;
    }
}
