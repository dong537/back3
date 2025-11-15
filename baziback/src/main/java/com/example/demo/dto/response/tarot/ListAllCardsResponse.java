package com.example.demo.dto.response.tarot;


import lombok.Data;

import java.util.List;

@Data
public class ListAllCardsResponse {
    private List<CardInfo> cards;

    @Data
    public static class CardInfo {
        private String name;
        private String category;
        private String keywords;
        private String briefDescription;
    }
}
