package com.example.demo.dto.request.tarot;

import lombok.Data;

@Data
public class FindSimilarCardsRequest {
    private String cardName;
    private Integer limit;
}
