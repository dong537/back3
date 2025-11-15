package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.List;

public class SearchCardsResponse {
    private List<CardInfo> matchedCards;

    @Data
    public static class CardInfo {
        private String name;
        private String suit;
        private String arcana;
        private String element;
        private String orientation;
        private String relevanceScore;
        private String briefMeaning;
    }
}
