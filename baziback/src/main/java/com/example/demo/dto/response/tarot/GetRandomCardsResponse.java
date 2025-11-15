package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.List;

@Data
public class GetRandomCardsResponse {

    private List<RandomCard> cards;

    @Data
    public static class RandomCard {
        private String name;
        private String suit;
        private String arcana;
        private String element;
        private String orientation;
    }
}
