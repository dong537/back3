package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.List;

@Data
public class FindSimilarCardsResponse {
    private String targetCard;
    private List<SimilarCard> similarCards;

    @Data
    public static class SimilarCard {
        private String name;
        private String similarityReason;
        private String briefMeaning;
    }
}
